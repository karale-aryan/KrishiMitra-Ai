-- =====================================================
-- KrishiMitra AI - Initial Database Schema
-- Flyway Migration V1
-- =====================================================

-- ==================== USERS ====================
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    phone_number VARCHAR(15) NOT NULL UNIQUE,
    email VARCHAR(255) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'FARMER',
    preferred_language VARCHAR(5) NOT NULL DEFAULT 'en',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_phone ON users(phone_number);
CREATE INDEX idx_users_role ON users(role);

-- ==================== FARMERS ====================
CREATE TABLE IF NOT EXISTS farmers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    full_name VARCHAR(255) NOT NULL,
    aadhar_number VARCHAR(20),
    state VARCHAR(100) NOT NULL,
    district VARCHAR(100) NOT NULL,
    village VARCHAR(100),
    pincode VARCHAR(6) NOT NULL,
    land_holding_hectares DOUBLE PRECISION NOT NULL,
    income_category VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_farmers_user ON farmers(user_id);
CREATE INDEX idx_farmers_state ON farmers(state);
CREATE INDEX idx_farmers_district ON farmers(district);
CREATE INDEX idx_farmers_pincode ON farmers(pincode);

-- ==================== FARMS ====================
CREATE TABLE IF NOT EXISTS farms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farmer_id UUID NOT NULL REFERENCES farmers(id) ON DELETE CASCADE,
    farm_name VARCHAR(255) NOT NULL,
    area_hectares DOUBLE PRECISION NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    soil_type VARCHAR(30) NOT NULL,
    irrigation_type VARCHAR(30) NOT NULL,
    soil_ph DOUBLE PRECISION,
    nitrogen_kg_ha DOUBLE PRECISION,
    phosphorus_kg_ha DOUBLE PRECISION,
    potassium_kg_ha DOUBLE PRECISION,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_farms_farmer ON farms(farmer_id);

-- ==================== CROPS (Reference Data) ====================
CREATE TABLE IF NOT EXISTS crops (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    crop_name VARCHAR(100) NOT NULL UNIQUE,
    crop_name_hi VARCHAR(100),
    crop_name_mr VARCHAR(100),
    crop_name_te VARCHAR(100),
    crop_name_kn VARCHAR(100),
    crop_type VARCHAR(20) NOT NULL,
    ideal_temp_min DOUBLE PRECISION,
    ideal_temp_max DOUBLE PRECISION,
    ideal_humidity_min DOUBLE PRECISION,
    ideal_humidity_max DOUBLE PRECISION,
    ideal_ph_min DOUBLE PRECISION,
    ideal_ph_max DOUBLE PRECISION,
    ideal_rainfall_mm DOUBLE PRECISION,
    growing_season_days INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==================== CROP RECOMMENDATIONS ====================
CREATE TABLE IF NOT EXISTS crop_recommendations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id UUID NOT NULL REFERENCES farms(id) ON DELETE CASCADE,
    crop_id UUID NOT NULL REFERENCES crops(id),
    confidence_score DOUBLE PRECISION NOT NULL,
    model_version VARCHAR(50),
    season VARCHAR(20),
    input_features TEXT,
    is_accepted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_crop_rec_farm ON crop_recommendations(farm_id);
CREATE INDEX idx_crop_rec_crop ON crop_recommendations(crop_id);
CREATE INDEX idx_crop_rec_created ON crop_recommendations(created_at DESC);

-- ==================== DISEASE REPORTS ====================
CREATE TABLE IF NOT EXISTS disease_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id UUID NOT NULL REFERENCES farms(id) ON DELETE CASCADE,
    crop_name VARCHAR(100),
    image_url VARCHAR(500),
    detected_disease VARCHAR(255),
    confidence_score DOUBLE PRECISION,
    model_version VARCHAR(50),
    severity VARCHAR(20),
    recommended_action TEXT,
    recommended_action_hi TEXT,
    is_confirmed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_disease_farm ON disease_reports(farm_id);
CREATE INDEX idx_disease_created ON disease_reports(created_at DESC);

-- ==================== WEATHER RECORDS (Cache) ====================
CREATE TABLE IF NOT EXISTS weather_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    temperature DOUBLE PRECISION,
    humidity DOUBLE PRECISION,
    precipitation DOUBLE PRECISION,
    wind_speed DOUBLE PRECISION,
    recorded_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_weather_location ON weather_records(latitude, longitude);
CREATE INDEX idx_weather_recorded ON weather_records(recorded_at DESC);

-- ==================== GOVERNMENT SCHEMES ====================
CREATE TABLE IF NOT EXISTS government_schemes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    scheme_name VARCHAR(255) NOT NULL,
    scheme_name_hi VARCHAR(255),
    description TEXT,
    description_hi TEXT,
    scheme_type VARCHAR(50) NOT NULL,
    eligibility_criteria TEXT,
    benefits TEXT,
    application_url VARCHAR(500),
    valid_from DATE,
    valid_until DATE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_schemes_active ON government_schemes(is_active);
CREATE INDEX idx_schemes_type ON government_schemes(scheme_type);

-- ==================== SCHEME RECOMMENDATIONS ====================
CREATE TABLE IF NOT EXISTS scheme_recommendations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farmer_id UUID NOT NULL REFERENCES farmers(id) ON DELETE CASCADE,
    scheme_id UUID NOT NULL REFERENCES government_schemes(id),
    match_score INTEGER NOT NULL DEFAULT 0,
    match_reasons TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_scheme_rec_farmer ON scheme_recommendations(farmer_id);
CREATE INDEX idx_scheme_rec_scheme ON scheme_recommendations(scheme_id);
CREATE INDEX idx_scheme_rec_score ON scheme_recommendations(match_score DESC);

-- ==================== ADVISORY LOGS ====================
CREATE TABLE IF NOT EXISTS advisory_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farmer_id UUID REFERENCES farmers(id) ON DELETE SET NULL,
    advisory_type VARCHAR(50),
    query_text TEXT,
    response_text TEXT,
    query_language VARCHAR(5),
    response_language VARCHAR(5),
    input_mode VARCHAR(10),
    session_id VARCHAR(100),
    response_time_ms BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_advisory_farmer ON advisory_logs(farmer_id);
CREATE INDEX idx_advisory_type ON advisory_logs(advisory_type);
CREATE INDEX idx_advisory_created ON advisory_logs(created_at DESC);
