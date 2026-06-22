KrishiMitra AI — Backend & AI Implementation Plan
Build the complete backend, ML pipeline, voice assistant, translation engine, and climate intelligence for a voice-first agriculture platform targeting Indian farmers.

User Review Required
IMPORTANT

No frontend work — You confirmed the frontend is already built. This plan covers: Spring Boot backend, ML models (crop recommendation + disease detection), voice assistant (STT/TTS), translation engine, climate intelligence, and government scheme recommendation.

IMPORTANT

Open-source models only — No paid APIs (OpenAI, Gemini). All ML models are open-source: Whisper for STT, AI4Bharat Indic-TTS for speech synthesis, IndicTrans2 for translation, Random Forest/MobileNetV2 for crop/disease ML.

WARNING

Python sidecar required — Heavy AI models (Whisper, IndicTrans2, Indic-TTS) cannot run efficiently in Java. The architecture uses a Python FastAPI sidecar alongside Spring Boot. This means the project has two runtime components: a Java backend and a Python AI service.

CAUTION

GPU recommended for AI models — Whisper (medium) and IndicTrans2 require a GPU for acceptable latency. CPU inference is possible but will be slow (~10-30s per request). For development, Whisper small model works on CPU.

Open Questions
IMPORTANT

Java version: Should I target Java 17 (LTS, widely supported) or Java 21 (latest LTS, virtual threads)?
Spring Boot version: 3.5.x (stable) or 4.1.x (latest)?
Image storage: Should I use MinIO (self-hosted S3-compatible) for disease detection images, or local filesystem for now?
Docker: Should I create Docker Compose for the full stack (PostgreSQL + Spring Boot + Python sidecar)?
Do you have PostgreSQL installed, or should I include H2 as an in-memory fallback for development?
Architecture Overview

┌─────────────────────────────────────────────────────────────┐
│                   Spring Boot Modular Monolith               │
│  ┌──────┐ ┌────────┐ ┌──────┐ ┌─────────┐ ┌──────────────┐ │
│  │ Auth │ │ Farmer │ │ Farm │ │ Weather │ │  Analytics   │ │
│  └──────┘ └────────┘ └──────┘ └─────────┘ └──────────────┘ │
│  ┌────────────────┐ ┌────────────────┐ ┌─────────────────┐  │
│  │ CropRecommend. │ │ SchemeRecomm.  │ │ VoiceAssistant │  │
│  │  (ONNX/DJL)    │ │ (Rules Engine) │ │  (WebClient)   │  │
│  └────────────────┘ └────────────────┘ └─────────────────┘  │
│  ┌────────────────┐ ┌────────────────┐                      │
│  │DiseaseDetect.  │ │  Translation   │                      │
│  │  (ONNX/DJL)    │ │  (WebClient)   │                      │
│  └────────────────┘ └────────────────┘                      │
└──────────────┬──────────────────────────────────────────────┘
               │ REST (localhost:8000)
┌──────────────┴──────────────────────────────────────────────┐
│                Python FastAPI Sidecar                         │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────────────┐ │
│  │ Whisper STT  │ │ IndicTrans2  │ │ AI4Bharat Indic-TTS │ │
│  └──────────────┘ └──────────────┘ └──────────────────────┘ │
└──────────────┬──────────────────────────────────────────────┘
               │
┌──────────────┴──────┐     ┌──────────────────┐
│   PostgreSQL         │     │ MinIO / Local FS  │
│   (+ PostGIS)        │     │ (Image Storage)   │
└─────────────────────┘     └──────────────────┘
Key Design Decisions:

Lightweight ML (Crop RF, Disease MobileNetV2) → ONNX models served directly in Java via DJL
Heavy ML (Whisper, IndicTrans2, Indic-TTS) → Python FastAPI sidecar
Weather → Free Open-Meteo API (no key required)
Scheme Recommendation → Rules engine matching farmer profile against JSONB eligibility criteria
Spring Modulith → Clean module boundaries, domain events for inter-module communication
Proposed Changes
Component 1: Project Scaffold & Configuration
[NEW] 
pom.xml
Maven project with Spring Boot 3.5.x, Spring Modulith, Spring Security, Spring Data JPA, PostgreSQL, WebFlux (WebClient), WebSocket, jjwt, DJL + ONNX Runtime, Lombok, and test dependencies.

[NEW] 
application.yml
Configuration profiles (dev, prod), database connection, JWT settings, AI sidecar URL, Open-Meteo base URL, file upload limits, CORS configuration.

[NEW] 
application-dev.yml
Development-specific overrides (H2 fallback, debug logging, relaxed CORS).

[NEW] 
KrishiMitraApplication.java
Main Spring Boot application entry point.

Component 2: Shared Kernel
[NEW] shared/dto/ApiResponse.java
Generic API response wrapper with success, message, data, errors fields.

[NEW] shared/dto/PagedResponse.java
Paginated response DTO.

[NEW] shared/exception/GlobalExceptionHandler.java
@ControllerAdvice with handlers for validation errors, entity not found, auth failures, AI service timeouts.

[NEW] shared/exception/ResourceNotFoundException.java, BadRequestException.java, AIServiceException.java
Custom exception classes.

[NEW] shared/config/WebClientConfig.java
WebClient beans for AI sidecar and weather API communication.

[NEW] shared/config/CorsConfig.java
CORS configuration for frontend origin.

[NEW] shared/config/AuditConfig.java
JPA auditing configuration (@EnableJpaAuditing).

Component 3: Auth Module
[NEW] auth/AuthController.java
POST /api/v1/auth/register — Register user (phone + password)
POST /api/v1/auth/login — Login, returns JWT
POST /api/v1/auth/refresh — Refresh token
[NEW] auth/AuthService.java
Registration, login, token refresh logic.

[NEW] auth/dto/RegisterRequest.java, LoginRequest.java, AuthResponse.java
Request/response DTOs.

[NEW] auth/internal/UserEntity.java
JPA entity: id (UUID), phoneNumber, email, passwordHash, role, preferredLanguage, isActive, createdAt, updatedAt.

[NEW] auth/internal/UserRepository.java
Spring Data JPA repository.

[NEW] auth/internal/JwtTokenProvider.java
JWT generation, validation, claim extraction using jjwt 0.12.x.

[NEW] auth/internal/SecurityConfig.java
Spring Security config: stateless sessions, JWT filter, public endpoints (/auth/**), CORS.

[NEW] auth/internal/JwtAuthenticationFilter.java
OncePerRequestFilter that validates JWT from Authorization header.

Component 4: Farmer Module
[NEW] farmer/FarmerController.java
GET /api/v1/farmers — List all (paginated, admin/agronomist only)
GET /api/v1/farmers/{id} — Get by ID
GET /api/v1/farmers/me — Get current user's farmer profile
POST /api/v1/farmers — Create farmer profile
PUT /api/v1/farmers/{id} — Update profile
[NEW] farmer/FarmerService.java
CRUD operations, profile validation, event publishing.

[NEW] farmer/dto/FarmerRequest.java, FarmerResponse.java
DTOs with validation annotations.

[NEW] farmer/events/FarmerRegisteredEvent.java
Domain event published when a farmer profile is created.

[NEW] farmer/internal/FarmerEntity.java
JPA entity: id (UUID), userId, fullName, aadharNumber, state, district, village, pincode, landHoldingHectares, incomeCategory, createdAt, updatedAt.

[NEW] farmer/internal/FarmerRepository.java
Repository with custom queries for district/state filtering.

Component 5: Farm Module
[NEW] farm/FarmController.java
GET /api/v1/farms — List farmer's farms
POST /api/v1/farms — Add a farm
PUT /api/v1/farms/{id} — Update farm details (soil data, irrigation)
DELETE /api/v1/farms/{id} — Remove farm
[NEW] farm/FarmService.java
Farm CRUD, soil data validation.

[NEW] farm/dto/FarmRequest.java, FarmResponse.java
DTOs including soil NPK values, pH, irrigation type, area.

[NEW] farm/internal/FarmEntity.java
JPA entity: id, farmerId, farmName, areaHectares, latitude, longitude, soilType, irrigationType, soilPh, nitrogenKgHa, phosphorusKgHa, potassiumKgHa, createdAt, updatedAt.

[NEW] farm/internal/FarmRepository.java
Repository with methods to find by farmer ID.

Component 6: Crop Recommendation Module (ML)
[NEW] croprecommendation/CropRecommendationController.java
POST /api/v1/recommendations/generate — Generate crop recommendations for a farm
GET /api/v1/recommendations/{farmId} — Get past recommendations
PATCH /api/v1/recommendations/{id}/accept — Mark recommendation as accepted
[NEW] croprecommendation/CropRecommendationService.java
Orchestrates: fetch farm soil data → fetch weather → run ONNX model → persist results → return top-N crops with confidence scores.

[NEW] croprecommendation/internal/CropEntity.java
JPA entity: id, cropName, cropNameHi/Mr/Te/Kn, cropType (kharif/rabi/zaid), idealTempMin/Max, idealHumidityMin/Max, idealPhMin/Max, idealRainfallMm, growingSeasonDays.

[NEW] croprecommendation/internal/CropRecommendationEntity.java
JPA entity: id, farmId, cropId, confidenceScore, modelVersion, inputFeatures (JSONB), season, isAccepted, createdAt.

[NEW] croprecommendation/internal/CropOnnxModelService.java
Loads crop_recommendation.onnx via ONNX Runtime, runs inference with input features [N, P, K, temperature, humidity, pH, rainfall], returns predicted crop + confidence.

[NEW] croprecommendation/internal/CropRepository.java, CropRecommendationRepository.java
[NEW] ml-models/crop/train_crop_model.py
Python script: trains Random Forest on Kaggle Crop Recommendation Dataset, exports to ONNX format.

[NEW] models/crop_recommendation.onnx
Pre-trained ONNX model (generated by training script).

[NEW] resources/data/crop_seed_data.sql
SQL seed data for the crops table with Indian crop names in 4 languages + ideal growing conditions.

Component 7: Weather & Climate Intelligence Module
[NEW] weather/WeatherController.java
GET /api/v1/weather/current?lat={lat}&lon={lon} — Current weather
GET /api/v1/weather/forecast?lat={lat}&lon={lon}&days={days} — Forecast
GET /api/v1/weather/risk/{farmId} — Climate risk analysis for a farm
[NEW] weather/WeatherService.java
Calls Open-Meteo API via WebClient, parses JSON response, caches results.

[NEW] weather/ClimateRiskService.java
Analyzes weather data to compute:

Rainfall risk: Below/above normal for the region/season
Drought risk: Extended dry periods, soil moisture deficit
Water stress: Humidity + rainfall + temperature analysis
Returns risk scores (low/medium/high/critical) with explanations.
[NEW] weather/dto/WeatherResponse.java, ClimateRiskResponse.java, ForecastDay.java
[NEW] weather/internal/WeatherRecordEntity.java
JPA entity for caching weather data.

[NEW] weather/internal/WeatherRecordRepository.java
Component 8: Disease Detection Module (ML)
[NEW] diseasedetection/DiseaseDetectionController.java
POST /api/v1/disease/analyze — Upload crop image, get disease diagnosis
GET /api/v1/disease/reports/{farmId} — Past disease reports
[NEW] diseasedetection/DiseaseDetectionService.java
Receives image → preprocesses (resize to 224×224, normalize) → runs MobileNetV2 ONNX model → returns disease name + confidence + treatment recommendation.

[NEW] diseasedetection/internal/DiseaseOnnxModelService.java
Loads plant_disease.onnx, performs image preprocessing and inference via ONNX Runtime.

[NEW] diseasedetection/internal/DiseaseReportEntity.java
JPA entity: id, farmId, cropId, imageUrl, detectedDisease, confidenceScore, modelVersion, severity, recommendedAction, isConfirmed, createdAt.

[NEW] diseasedetection/internal/DiseaseReportRepository.java
[NEW] diseasedetection/internal/TreatmentKnowledgeBase.java
Static lookup of treatment recommendations per disease (38 PlantVillage classes → treatment text in English + Hindi).

[NEW] ml-models/disease/train_disease_model.py
Python script: fine-tunes MobileNetV2 on PlantVillage dataset, exports to ONNX.

[NEW] models/plant_disease.onnx
Pre-trained ONNX model.

Component 9: Voice Assistant Module
[NEW] voiceassistant/VoiceController.java
POST /api/v1/voice/transcribe — Upload audio → returns text transcription
POST /api/v1/voice/synthesize — Send text → returns audio (WAV/MP3)
POST /api/v1/voice/chat — Full voice loop: audio in → STT → AI advisory → TTS → audio out
[NEW] voiceassistant/VoiceAssistantService.java
Orchestrates the voice pipeline:

Receive audio from mobile app
Forward to Python sidecar (Whisper STT)
Get transcribed text
Route to appropriate service (crop advisory, weather, scheme info)
Generate response text
Forward to Python sidecar (Indic-TTS)
Return synthesized audio
[NEW] voiceassistant/AdvisoryOrchestrator.java
Intent detection + routing: parses transcribed farmer query and routes to the correct module (crop recommendation, weather, disease, scheme) to generate a contextual advisory response.

[NEW] voiceassistant/dto/TranscriptionRequest.java, TranscriptionResponse.java, TTSRequest.java, VoiceChatRequest.java, VoiceChatResponse.java
[NEW] voiceassistant/internal/AdvisoryLogEntity.java
JPA entity: id, farmerId, advisoryType, queryText, responseText, queryLanguage, responseLanguage, inputMode, sessionId, responseTimeMs, createdAt.

[NEW] voiceassistant/internal/AdvisoryLogRepository.java
Component 10: Translation Module
[NEW] translation/TranslationController.java
POST /api/v1/translate — Translate text between languages
[NEW] translation/TranslationService.java
Forwards requests to Python sidecar running IndicTrans2 (200M distilled model). Supports: English ↔ Hindi ↔ Marathi ↔ Telugu ↔ Kannada.

[NEW] translation/dto/TranslationRequest.java, TranslationResponse.java
Component 11: Government Scheme Recommendation Module
[NEW] schemerecommendation/SchemeController.java
GET /api/v1/schemes — List all active schemes
GET /api/v1/schemes/{id} — Scheme details
GET /api/v1/schemes/recommended/{farmerId} — Personalized scheme recommendations
PATCH /api/v1/schemes/recommendations/{id}/status — Update status (viewed/applied/dismissed)
[NEW] schemerecommendation/SchemeRecommendationService.java
Rules engine: matches farmer profile (state, district, land holding, income category, crop type) against scheme eligibility criteria stored as JSONB. Returns ranked list of eligible schemes.

[NEW] schemerecommendation/internal/GovernmentSchemeEntity.java
JPA entity: id, schemeName, schemeNameHi, description, descriptionHi, schemeType, eligibilityCriteria (JSONB), benefits, applicationUrl, validFrom, validUntil, isActive.

[NEW] schemerecommendation/internal/SchemeRecommendationEntity.java
JPA entity: id, farmerId, schemeId, matchScore, matchReasons (JSONB), status, createdAt.

[NEW] schemerecommendation/internal/SchemeRepository.java, SchemeRecommendationRepository.java
[NEW] resources/data/scheme_seed_data.sql
Seed data for major Indian agriculture schemes: PM-Kisan, PMFBY, KCC, Soil Health Card, NFSM, RKVY, etc.

Component 12: Analytics Module
[NEW] analytics/AnalyticsController.java
GET /api/v1/analytics/dashboard — Platform-wide stats (admin)
GET /api/v1/analytics/farmer/{farmerId} — Farmer-specific analytics
[NEW] analytics/AnalyticsService.java
Aggregates: total farmers, recommendations generated, disease detections, scheme applications, active users, crop distribution.

[NEW] analytics/dto/DashboardResponse.java, FarmerAnalyticsResponse.java
Component 13: Python AI Sidecar
[NEW] ai-sidecar/requirements.txt
Dependencies: fastapi, uvicorn, openai-whisper, transformers, torch, IndicTransToolkit, soundfile, numpy, pydub.

[NEW] ai-sidecar/main.py
FastAPI application with three endpoints:

POST /transcribe — Whisper STT (accepts audio file + language)
POST /translate — IndicTrans2 translation
POST /synthesize — Indic-TTS text-to-speech
[NEW] ai-sidecar/services/stt_service.py
Whisper model loading + transcription logic. Configurable model size (small for dev, medium for prod).

[NEW] ai-sidecar/services/translation_service.py
IndicTrans2 model loading + translation logic. Uses distilled 200M model.

[NEW] ai-sidecar/services/tts_service.py
AI4Bharat Indic-TTS integration for Hindi, Marathi, Telugu, Kannada voice synthesis.

[NEW] ai-sidecar/Dockerfile
Python 3.11 image with CUDA support (optional), installs requirements, runs uvicorn.

Component 14: Database Migrations
[NEW] resources/db/migration/V1__init_schema.sql
Complete Flyway migration with all tables, indexes, constraints, and PostGIS extension.

[NEW] resources/db/migration/V2__seed_crops.sql
Seed data for 20+ Indian crops with multilingual names and ideal conditions.

[NEW] resources/db/migration/V3__seed_schemes.sql
Seed data for 10+ government agriculture schemes with eligibility criteria.

Component 15: Docker & DevOps
[NEW] docker-compose.yml
Full stack: PostgreSQL 16 + PostGIS, Spring Boot app, Python AI sidecar, MinIO (for image storage).

[NEW] Dockerfile (Spring Boot)
Multi-stage build: Maven build → JRE 21 slim runtime.

[NEW] .env.example
Template for environment variables (DB credentials, JWT secret, sidecar URL).

Verification Plan
Automated Tests
bash

# Module structure verification (Spring Modulith)
mvn test -Dtest=KrishiMitraModulesTests
# Unit tests for all services
mvn test
# Integration tests with Testcontainers (PostgreSQL)
mvn test -Dtest=*IntegrationTest
Manual Verification
Auth flow: Register → Login → Access protected endpoints with JWT
Farmer CRUD: Create farmer profile → Add farm with soil data
Crop recommendation: Submit farm data → Get ranked crop suggestions from ONNX model
Weather: Query weather by coordinates → Verify Open-Meteo data parsing
Climate risk: Generate risk analysis for a farm location
Disease detection: Upload a test plant image → Get disease classification
Voice pipeline: Upload audio → Get transcription → Get TTS audio back
Translation: Translate text between English/Hindi/Marathi
Scheme recommendation: Create farmer with specific profile → Get matching government schemes
Docker Compose: docker-compose up → All services start and communicate
API Testing
Provide a Postman collection or http request files for all endpoints
Include sample request/response payloads for each endpoint