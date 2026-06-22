"""
Text-to-Speech Service using AI4Bharat Indic-TTS.
Supports: Hindi (hi), Marathi (mr), Telugu (te), Kannada (kn), English (en).
"""

import os
import io
import logging
import struct

logger = logging.getLogger("krishimitra-sidecar.tts")

# AI4Bharat TTS voice configurations
TTS_VOICE_MAP = {
    "hi": {
        "female": "v1/hi/female",
        "male": "v1/hi/male",
    },
    "mr": {
        "female": "v1/mr/female",
        "male": "v1/mr/male",
    },
    "te": {
        "female": "v1/te/female",
        "male": "v1/te/male",
    },
    "kn": {
        "female": "v1/kn/female",
        "male": "v1/kn/male",
    },
    "en": {
        "female": "v1/en/female",
        "male": "v1/en/male",
    },
}


class TTSService:
    """Text-to-Speech service using AI4Bharat Indic-TTS / VITS models."""

    def __init__(self):
        """Initialize TTS models."""
        logger.info("Initializing TTS Service...")

        self.sample_rate = 22050
        self.models = {}

        try:
            # Try loading AI4Bharat TTS models via their library
            self._load_models()
            logger.info("TTS models loaded successfully")
        except Exception as e:
            logger.warning(f"Failed to load TTS models: {e}. Using fallback sine-wave generator.")
            self.models = {}

    def _load_models(self):
        """Attempt to load AI4Bharat VITS TTS models."""
        try:
            from TTS.api import TTS as CoquiTTS

            # Load a multilingual TTS model as fallback
            model_name = os.getenv("TTS_MODEL", "tts_models/multilingual/multi-dataset/your_tts")
            logger.info(f"Loading TTS model: {model_name}")
            self.coqui_tts = CoquiTTS(model_name=model_name, progress_bar=False)
            self.use_coqui = True
            logger.info("Coqui TTS loaded as backend")
        except ImportError:
            logger.info("Coqui TTS not available, trying pyttsx3...")
            self.use_coqui = False

            try:
                import torch
                from transformers import VitsModel, AutoTokenizer

                # Try loading Facebook MMS-TTS models (open source, supports Indian languages)
                lang_model_map = {
                    "hi": "facebook/mms-tts-hin",
                    "mr": "facebook/mms-tts-mar",
                    "te": "facebook/mms-tts-tel",
                    "kn": "facebook/mms-tts-kan",
                    "en": "facebook/mms-tts-eng",
                }

                for lang, model_id in lang_model_map.items():
                    try:
                        logger.info(f"Loading MMS-TTS model for {lang}: {model_id}")
                        self.models[lang] = {
                            "model": VitsModel.from_pretrained(model_id),
                            "tokenizer": AutoTokenizer.from_pretrained(model_id),
                        }
                        logger.info(f"✅ MMS-TTS model loaded for {lang}")
                    except Exception as e:
                        logger.warning(f"Failed to load MMS-TTS for {lang}: {e}")

            except ImportError:
                logger.warning("Neither Coqui TTS nor transformers available for TTS")

    def synthesize(self, text: str, language: str = "hi", speaker: str = "female") -> bytes:
        """
        Synthesize speech from text.

        Args:
            text: Text to convert to speech
            language: Language code (hi, mr, te, kn, en)
            speaker: Speaker type (male, female)

        Returns:
            WAV audio bytes
        """
        if not text or not text.strip():
            raise ValueError("Text cannot be empty")

        # Try Coqui TTS first
        if hasattr(self, 'use_coqui') and self.use_coqui:
            return self._synthesize_coqui(text, language, speaker)

        # Try MMS-TTS models
        if language in self.models:
            return self._synthesize_mms(text, language)

        # Fallback: generate a simple sine wave as placeholder audio
        logger.warning(f"No TTS model available for {language}, generating placeholder audio")
        return self._generate_placeholder_audio(text)

    def _synthesize_coqui(self, text: str, language: str, speaker: str) -> bytes:
        """Synthesize using Coqui TTS."""
        import soundfile as sf
        import numpy as np

        wav = self.coqui_tts.tts(text=text, language=language)
        wav_array = np.array(wav, dtype=np.float32)

        buffer = io.BytesIO()
        sf.write(buffer, wav_array, self.sample_rate, format="WAV")
        buffer.seek(0)
        return buffer.read()

    def _synthesize_mms(self, text: str, language: str) -> bytes:
        """Synthesize using Facebook MMS-TTS."""
        import torch
        import numpy as np

        model_data = self.models[language]
        model = model_data["model"]
        tokenizer = model_data["tokenizer"]

        inputs = tokenizer(text, return_tensors="pt")

        with torch.no_grad():
            output = model(**inputs).waveform

        waveform = output.squeeze().numpy()

        # Normalize to 16-bit PCM
        waveform = np.clip(waveform, -1.0, 1.0)
        pcm_data = (waveform * 32767).astype(np.int16)

        return self._create_wav(pcm_data, model.config.sampling_rate)

    def _generate_placeholder_audio(self, text: str) -> bytes:
        """Generate placeholder sine wave audio when no TTS model is available."""
        import numpy as np

        duration = max(1.0, min(len(text) * 0.05, 10.0))  # ~50ms per char, max 10s
        t = np.linspace(0, duration, int(self.sample_rate * duration), dtype=np.float32)

        # Generate a simple tone sequence
        frequencies = [440, 494, 523, 587, 659]  # A4, B4, C5, D5, E5
        samples_per_note = len(t) // len(frequencies)

        audio = np.zeros_like(t)
        for i, freq in enumerate(frequencies):
            start = i * samples_per_note
            end = min((i + 1) * samples_per_note, len(t))
            audio[start:end] = 0.3 * np.sin(2 * np.pi * freq * t[start:end])

        # Apply fade in/out
        fade_len = min(1000, len(audio) // 10)
        audio[:fade_len] *= np.linspace(0, 1, fade_len)
        audio[-fade_len:] *= np.linspace(1, 0, fade_len)

        pcm_data = (audio * 32767).astype(np.int16)
        return self._create_wav(pcm_data, self.sample_rate)

    def _create_wav(self, pcm_data, sample_rate: int) -> bytes:
        """Create a WAV file from PCM data."""
        buffer = io.BytesIO()
        num_samples = len(pcm_data)
        data_size = num_samples * 2  # 16-bit = 2 bytes per sample

        # Write WAV header
        buffer.write(b"RIFF")
        buffer.write(struct.pack("<I", 36 + data_size))  # File size - 8
        buffer.write(b"WAVE")
        buffer.write(b"fmt ")
        buffer.write(struct.pack("<I", 16))  # Chunk size
        buffer.write(struct.pack("<H", 1))   # PCM format
        buffer.write(struct.pack("<H", 1))   # Mono
        buffer.write(struct.pack("<I", sample_rate))
        buffer.write(struct.pack("<I", sample_rate * 2))  # Byte rate
        buffer.write(struct.pack("<H", 2))   # Block align
        buffer.write(struct.pack("<H", 16))  # Bits per sample
        buffer.write(b"data")
        buffer.write(struct.pack("<I", data_size))
        buffer.write(pcm_data.tobytes())

        buffer.seek(0)
        return buffer.read()
