"""
Translation Service using AI4Bharat IndicTrans2 (distilled 200M model).
Supports: English (en) ↔ Hindi (hi) ↔ Marathi (mr) ↔ Telugu (te) ↔ Kannada (kn)
"""

import os
import logging
from functools import lru_cache

logger = logging.getLogger("krishimitra-sidecar.translation")

# IndicTrans2 language code mapping
# IndicTrans2 uses BCP-47 style codes
INDICTRANS_LANG_MAP = {
    "en": "eng_Latn",
    "hi": "hin_Deva",
    "mr": "mar_Deva",
    "te": "tel_Telu",
    "kn": "kan_Knda",
}

# Determines translation direction
INDIC_LANGUAGES = {"hi", "mr", "te", "kn"}


class TranslationServiceImpl:
    """Translation service using AI4Bharat IndicTrans2."""

    def __init__(self):
        """Load IndicTrans2 models (en→indic and indic→en)."""
        self.model_name = os.getenv(
            "INDICTRANS_MODEL",
            "ai4bharat/indictrans2-indic-indic-dist-200M"
        )
        self.en2indic_model_name = os.getenv(
            "INDICTRANS_EN2INDIC_MODEL",
            "ai4bharat/indictrans2-en-indic-dist-200M"
        )
        self.indic2en_model_name = os.getenv(
            "INDICTRANS_INDIC2EN_MODEL",
            "ai4bharat/indictrans2-indic-en-dist-200M"
        )

        logger.info("Loading IndicTrans2 models...")

        try:
            from transformers import AutoModelForSeq2SeqLM, AutoTokenizer

            # Load English → Indic model
            logger.info(f"Loading en→indic model: {self.en2indic_model_name}")
            self.en2indic_tokenizer = AutoTokenizer.from_pretrained(
                self.en2indic_model_name, trust_remote_code=True
            )
            self.en2indic_model = AutoModelForSeq2SeqLM.from_pretrained(
                self.en2indic_model_name, trust_remote_code=True
            )

            # Load Indic → English model
            logger.info(f"Loading indic→en model: {self.indic2en_model_name}")
            self.indic2en_tokenizer = AutoTokenizer.from_pretrained(
                self.indic2en_model_name, trust_remote_code=True
            )
            self.indic2en_model = AutoModelForSeq2SeqLM.from_pretrained(
                self.indic2en_model_name, trust_remote_code=True
            )

            # Load Indic → Indic model
            logger.info(f"Loading indic→indic model: {self.model_name}")
            self.indic2indic_tokenizer = AutoTokenizer.from_pretrained(
                self.model_name, trust_remote_code=True
            )
            self.indic2indic_model = AutoModelForSeq2SeqLM.from_pretrained(
                self.model_name, trust_remote_code=True
            )

            logger.info("All IndicTrans2 models loaded successfully")

        except ImportError:
            logger.warning(
                "transformers not available. Using fallback translation (echo mode)."
            )
            self.en2indic_model = None
            self.indic2en_model = None
            self.indic2indic_model = None

        except Exception as e:
            logger.warning(f"Failed to load IndicTrans2 models: {e}. Using fallback.")
            self.en2indic_model = None
            self.indic2en_model = None
            self.indic2indic_model = None

    def translate(self, text: str, source_lang: str, target_lang: str) -> str:
        """
        Translate text between supported languages.

        Args:
            text: Source text to translate
            source_lang: Source language code (en, hi, mr, te, kn)
            target_lang: Target language code (en, hi, mr, te, kn)

        Returns:
            Translated text string
        """
        if source_lang == target_lang:
            return text

        src_code = INDICTRANS_LANG_MAP.get(source_lang)
        tgt_code = INDICTRANS_LANG_MAP.get(target_lang)

        if not src_code or not tgt_code:
            raise ValueError(f"Unsupported language pair: {source_lang} → {target_lang}")

        # Select appropriate model based on translation direction
        if source_lang == "en" and target_lang in INDIC_LANGUAGES:
            model = self.en2indic_model
            tokenizer = self.en2indic_tokenizer
        elif source_lang in INDIC_LANGUAGES and target_lang == "en":
            model = self.indic2en_model
            tokenizer = self.indic2en_tokenizer
        elif source_lang in INDIC_LANGUAGES and target_lang in INDIC_LANGUAGES:
            model = self.indic2indic_model
            tokenizer = self.indic2indic_tokenizer
        else:
            raise ValueError(f"Unsupported translation direction: {source_lang} → {target_lang}")

        # Fallback if models not loaded
        if model is None:
            logger.warning("Models not loaded, returning original text as fallback")
            return f"[{target_lang}] {text}"

        try:
            return self._run_translation(model, tokenizer, text, src_code, tgt_code)
        except Exception as e:
            logger.error(f"Translation error: {e}")
            raise

    def _run_translation(self, model, tokenizer, text: str, src_code: str, tgt_code: str) -> str:
        """Run the actual translation through the model."""
        import torch

        # Prepare input with language tags
        input_text = f"{src_code} {text} </s>"

        # Tokenize
        inputs = tokenizer(
            input_text,
            return_tensors="pt",
            padding=True,
            truncation=True,
            max_length=256,
        )

        # Generate translation
        with torch.no_grad():
            generated = model.generate(
                **inputs,
                forced_bos_token_id=tokenizer.convert_tokens_to_ids(tgt_code),
                max_length=256,
                num_beams=5,
                num_return_sequences=1,
                early_stopping=True,
            )

        # Decode output
        translated = tokenizer.decode(generated[0], skip_special_tokens=True)

        # Remove target language tag if present
        if translated.startswith(tgt_code):
            translated = translated[len(tgt_code):].strip()

        return translated
