"""
Speech-to-Text Service using OpenAI Whisper.
Supports: Hindi, Marathi, Telugu, Kannada, English.
"""

import os
import io
import tempfile
import logging
import numpy as np

logger = logging.getLogger("krishimitra-sidecar.stt")

# Language code mapping for Whisper
LANGUAGE_MAP = {
    "hi": "hindi",
    "mr": "marathi",
    "te": "telugu",
    "kn": "kannada",
    "en": "english",
}


class STTService:
    """Speech-to-Text service using OpenAI Whisper."""

    def __init__(self):
        """Load Whisper model. Uses 'small' for dev, 'medium' for prod."""
        model_size = os.getenv("WHISPER_MODEL_SIZE", "small")
        logger.info(f"Loading Whisper model: {model_size}")

        try:
            import whisper
            self.model = whisper.load_model(model_size)
            self.model_size = model_size
            logger.info(f"Whisper {model_size} model loaded successfully")
        except Exception as e:
            logger.error(f"Failed to load Whisper model: {e}")
            raise

    def transcribe(self, audio_bytes: bytes, language: str = None) -> dict:
        """
        Transcribe audio bytes to text.

        Args:
            audio_bytes: Raw audio file bytes (WAV, MP3, FLAC, etc.)
            language: Optional language hint (hi, mr, te, kn, en)

        Returns:
            dict with 'text', 'detected_language', 'confidence'
        """
        import whisper

        # Write audio to temporary file (Whisper requires file path)
        with tempfile.NamedTemporaryFile(suffix=".wav", delete=False) as tmp_file:
            tmp_file.write(audio_bytes)
            tmp_path = tmp_file.name

        try:
            # Build transcription options
            options = {
                "fp16": False,  # CPU-friendly
                "task": "transcribe",
            }

            if language and language in LANGUAGE_MAP:
                options["language"] = LANGUAGE_MAP[language]

            # Run transcription
            result = self.model.transcribe(tmp_path, **options)

            # Extract results
            text = result.get("text", "").strip()
            detected_lang = result.get("language", language or "unknown")

            # Map back to our language codes
            lang_reverse = {v: k for k, v in LANGUAGE_MAP.items()}
            detected_code = lang_reverse.get(detected_lang, detected_lang)

            # Calculate average confidence from segments
            segments = result.get("segments", [])
            if segments:
                avg_confidence = np.mean([
                    seg.get("avg_logprob", -1.0) for seg in segments
                ])
                # Convert log probability to a 0-1 confidence score
                confidence = min(1.0, max(0.0, 1.0 + avg_confidence))
            else:
                confidence = 0.0

            return {
                "text": text,
                "detected_language": detected_code,
                "confidence": round(confidence, 4),
            }

        finally:
            # Clean up temp file
            try:
                os.unlink(tmp_path)
            except OSError:
                pass
