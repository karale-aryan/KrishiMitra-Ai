"""
KrishiMitra AI Sidecar - FastAPI Application
Serves heavy ML models: Whisper STT, IndicTrans2 Translation, AI4Bharat Indic-TTS
"""

import os
import io
import time
import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI, UploadFile, File, Form, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import StreamingResponse, JSONResponse
from pydantic import BaseModel, Field
from typing import Optional

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("krishimitra-sidecar")

# Service instances (lazy loaded)
stt_service = None
translation_service = None
tts_service = None


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Load ML models on startup."""
    global stt_service, translation_service, tts_service

    logger.info("=" * 60)
    logger.info("KrishiMitra AI Sidecar - Starting up...")
    logger.info("=" * 60)

    # Import and initialize services
    try:
        from services.stt_service import STTService
        stt_service = STTService()
        logger.info("✅ STT Service (Whisper) initialized")
    except Exception as e:
        logger.warning(f"⚠️ STT Service failed to initialize: {e}")
        stt_service = None

    try:
        from services.translation_service import TranslationServiceImpl
        translation_service = TranslationServiceImpl()
        logger.info("✅ Translation Service (IndicTrans2) initialized")
    except Exception as e:
        logger.warning(f"⚠️ Translation Service failed to initialize: {e}")
        translation_service = None

    try:
        from services.tts_service import TTSService
        tts_service = TTSService()
        logger.info("✅ TTS Service (Indic-TTS) initialized")
    except Exception as e:
        logger.warning(f"⚠️ TTS Service failed to initialize: {e}")
        tts_service = None

    logger.info("=" * 60)
    logger.info("KrishiMitra AI Sidecar - Ready!")
    logger.info("=" * 60)

    yield

    logger.info("Shutting down KrishiMitra AI Sidecar...")


app = FastAPI(
    title="KrishiMitra AI Sidecar",
    description="Python AI service for speech-to-text, translation, and text-to-speech",
    version="1.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ==================== Request/Response Models ====================

class TranslationRequest(BaseModel):
    source_text: str = Field(..., description="Text to translate")
    source_language: str = Field(..., description="Source language code (en, hi, mr, te, kn)")
    target_language: str = Field(..., description="Target language code (en, hi, mr, te, kn)")


class TranslationResponse(BaseModel):
    translated_text: str
    source_language: str
    target_language: str
    model_version: str = "indictrans2-200m"


class TranscriptionResponse(BaseModel):
    text: str
    detected_language: str
    confidence: float


class SynthesizeRequest(BaseModel):
    text: str = Field(..., description="Text to synthesize")
    language: str = Field("hi", description="Language code (hi, mr, te, kn, en)")
    speaker: str = Field("female", description="Speaker gender (male, female)")


# ==================== Health Check ====================

@app.get("/health")
async def health_check():
    """Health check endpoint."""
    return {
        "status": "healthy",
        "services": {
            "stt": "available" if stt_service is not None else "unavailable",
            "translation": "available" if translation_service is not None else "unavailable",
            "tts": "available" if tts_service is not None else "unavailable",
        },
    }


# ==================== Speech-to-Text (Whisper) ====================

@app.post("/transcribe", response_model=TranscriptionResponse)
async def transcribe_audio(
    audio: UploadFile = File(..., description="Audio file (WAV, MP3, FLAC, etc.)"),
    language: Optional[str] = Form(None, description="Language hint (hi, mr, te, kn, en)"),
):
    """
    Transcribe audio using OpenAI Whisper.
    Supports Hindi, Marathi, Telugu, Kannada, and English.
    """
    if stt_service is None:
        raise HTTPException(
            status_code=503,
            detail="STT service is not available. Whisper model may not be loaded.",
        )

    start_time = time.time()

    try:
        audio_bytes = await audio.read()
        result = stt_service.transcribe(audio_bytes, language)
        elapsed = time.time() - start_time
        logger.info(f"Transcription completed in {elapsed:.2f}s: {result['text'][:100]}...")

        return TranscriptionResponse(
            text=result["text"],
            detected_language=result.get("detected_language", language or "unknown"),
            confidence=result.get("confidence", 0.0),
        )
    except Exception as e:
        logger.error(f"Transcription error: {e}")
        raise HTTPException(status_code=500, detail=f"Transcription failed: {str(e)}")


# ==================== Translation (IndicTrans2) ====================

@app.post("/translate", response_model=TranslationResponse)
async def translate_text(request: TranslationRequest):
    """
    Translate text using AI4Bharat IndicTrans2.
    Supports: English (en) ↔ Hindi (hi) ↔ Marathi (mr) ↔ Telugu (te) ↔ Kannada (kn)
    """
    if translation_service is None:
        raise HTTPException(
            status_code=503,
            detail="Translation service is not available. IndicTrans2 model may not be loaded.",
        )

    # Validate languages
    supported = {"en", "hi", "mr", "te", "kn"}
    if request.source_language not in supported or request.target_language not in supported:
        raise HTTPException(
            status_code=400,
            detail=f"Unsupported language. Supported: {supported}",
        )

    if request.source_language == request.target_language:
        return TranslationResponse(
            translated_text=request.source_text,
            source_language=request.source_language,
            target_language=request.target_language,
        )

    start_time = time.time()

    try:
        translated = translation_service.translate(
            request.source_text,
            request.source_language,
            request.target_language,
        )
        elapsed = time.time() - start_time
        logger.info(f"Translation completed in {elapsed:.2f}s")

        return TranslationResponse(
            translated_text=translated,
            source_language=request.source_language,
            target_language=request.target_language,
        )
    except Exception as e:
        logger.error(f"Translation error: {e}")
        raise HTTPException(status_code=500, detail=f"Translation failed: {str(e)}")


# ==================== Text-to-Speech (Indic-TTS) ====================

@app.post("/synthesize")
async def synthesize_speech(request: SynthesizeRequest):
    """
    Synthesize speech from text using AI4Bharat Indic-TTS.
    Returns WAV audio.
    """
    if tts_service is None:
        raise HTTPException(
            status_code=503,
            detail="TTS service is not available. Indic-TTS model may not be loaded.",
        )

    supported = {"hi", "mr", "te", "kn", "en"}
    if request.language not in supported:
        raise HTTPException(
            status_code=400,
            detail=f"Unsupported language. Supported: {supported}",
        )

    start_time = time.time()

    try:
        audio_bytes = tts_service.synthesize(
            request.text,
            request.language,
            request.speaker,
        )
        elapsed = time.time() - start_time
        logger.info(f"TTS completed in {elapsed:.2f}s for {len(request.text)} chars")

        return StreamingResponse(
            io.BytesIO(audio_bytes),
            media_type="audio/wav",
            headers={"Content-Disposition": "attachment; filename=speech.wav"},
        )
    except Exception as e:
        logger.error(f"TTS error: {e}")
        raise HTTPException(status_code=500, detail=f"Speech synthesis failed: {str(e)}")


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=int(os.getenv("PORT", "8000")),
        reload=os.getenv("RELOAD", "false").lower() == "true",
        workers=1,  # Single worker since models are loaded in-memory
    )
