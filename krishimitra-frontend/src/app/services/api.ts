const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export interface ApiResponse<T = any> {
  success: boolean;
  message: string;
  data: T;
  errors: string[];
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface FarmerResponse {
  id: string;
  userId: string;
  fullName: string;
  state: string;
  district: string;
  village: string;
  pincode: string;
  landHoldingHectares: number;
  incomeCategory: 'BELOW_1_LAKH' | 'ONE_TO_THREE_LAKH' | 'THREE_TO_FIVE_LAKH' | 'ABOVE_FIVE_LAKH';
  createdAt: string;
  updatedAt: string;
}

export interface FarmResponse {
  id: string;
  farmerId: string;
  farmName: string;
  areaHectares: number;
  latitude: number | null;
  longitude: number | null;
  soilType: 'ALLUVIAL' | 'BLACK' | 'RED' | 'LATERITE' | 'DESERT' | 'MOUNTAIN';
  irrigationType: 'CANAL' | 'WELL' | 'TUBE_WELL' | 'DRIP' | 'SPRINKLER' | 'RAIN_FED';
  soilPh: number | null;
  nitrogenKgHa: number | null;
  phosphorusKgHa: number | null;
  potassiumKgHa: number | null;
  createdAt: string;
  updatedAt: string;
}

export interface CropRecommendationResponse {
  id: string;
  farmId: string;
  cropId: string;
  cropName: string;
  cropNameHi?: string;
  cropNameMr?: string;
  cropNameTe?: string;
  cropNameKn?: string;
  confidenceScore: number;
  modelVersion: string;
  inputFeatures: Record<string, any>;
  season: string;
  isAccepted: boolean;
  createdAt: string;
}

export interface WeatherResponse {
  temperature: number;
  humidity: number;
  windSpeed: number;
  precipitation: number;
  weatherCode: number;
  description: string;
}

export interface ForecastDay {
  date: string;
  maxTemp: number;
  minTemp: number;
  precipitationProb: number;
  weatherCode: number;
  description: string;
}

export interface ClimateRiskDetail {
  level: 'LOW' | 'MODERATE' | 'HIGH' | 'CRITICAL';
  score: number;
  explanation: string;
  explanationHi: string;
}

export interface ClimateRiskResponse {
  farmId: string;
  overallRisk: 'LOW' | 'MODERATE' | 'HIGH' | 'CRITICAL';
  droughtRisk: ClimateRiskDetail;
  floodRisk: ClimateRiskDetail;
  heatStressRisk: ClimateRiskDetail;
  waterStressRisk: ClimateRiskDetail;
  analysisDate: string;
  forecastDays: number;
}

export interface DiseaseDetectionResponse {
  id: string;
  farmId: string;
  cropId?: string;
  cropName: string;
  imageUrl: string;
  detectedDisease: string;
  confidenceScore: number;
  modelVersion: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  recommendedAction: string;
  isConfirmed: boolean;
  createdAt: string;
}

export interface SchemeResponse {
  id: string;
  schemeName: string;
  schemeNameHi: string;
  description: string;
  descriptionHi: string;
  schemeType: string;
  benefits: string;
  applicationUrl: string;
}

export interface SchemeRecommendationResponse {
  id: string;
  farmerId: string;
  scheme: SchemeResponse;
  matchScore: number;
  matchReasons: string[];
  status: 'RECOMMENDED' | 'VIEWED' | 'APPLIED' | 'DISMISSED';
  createdAt: string;
}

export interface TranslationResponse {
  translatedText: string;
  sourceLanguage: string;
  targetLanguage: string;
}

export interface FarmerAnalyticsResponse {
  totalFarms: number;
  totalRecommendations: number;
  acceptedRecommendations: number;
  totalDiseasesDetected: number;
  recentActivity: Array<{
    type: string;
    description: string;
    date: string;
  }>;
  soilHealthTrend: Array<{
    farmName: string;
    n: number;
    p: number;
    k: number;
    pH: number;
  }>;
}

// Token storage helpers
export const getTokens = () => {
  const accessToken = localStorage.getItem('km_access_token');
  const refreshToken = localStorage.getItem('km_refresh_token');
  return { accessToken, refreshToken };
};

export const setTokens = (accessToken: string, refreshToken: string) => {
  localStorage.setItem('km_access_token', accessToken);
  localStorage.setItem('km_refresh_token', refreshToken);
};

export const clearTokens = () => {
  localStorage.removeItem('km_access_token');
  localStorage.removeItem('km_refresh_token');
};

// Base fetch with token auto-injection & refresh
async function fetchClient<T = any>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const url = `${API_BASE_URL}${endpoint}`;
  let { accessToken } = getTokens();

  const headers = new Headers(options.headers || {});
  if (accessToken) {
    headers.set('Authorization', `Bearer ${accessToken}`);
  }
  if (!headers.has('Content-Type') && !(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json');
  }

  const response = await fetch(url, { ...options, headers });

  if (response.status === 401) {
    // Attempt token refresh
    const { refreshToken } = getTokens();
    if (refreshToken) {
      try {
        const refreshResponse = await fetch(`${API_BASE_URL}/api/v1/auth/refresh`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ refreshToken }),
        });
        if (refreshResponse.ok) {
          const refreshData: ApiResponse<AuthResponse> = await refreshResponse.json();
          if (refreshData.success && refreshData.data) {
            setTokens(refreshData.data.accessToken, refreshData.data.refreshToken);
            // Retry request
            headers.set('Authorization', `Bearer ${refreshData.data.accessToken}`);
            const retryResponse = await fetch(url, { ...options, headers });
            if (!retryResponse.ok) {
              const errData = await retryResponse.json().catch(() => ({}));
              throw new Error(errData.message || 'Request failed after refresh');
            }
            const resBody = await retryResponse.json();
            return resBody.data;
          }
        }
      } catch (err) {
        clearTokens();
        window.dispatchEvent(new Event('km_unauthorized'));
        throw new Error('Session expired. Please log in again.');
      }
    }
    clearTokens();
    window.dispatchEvent(new Event('km_unauthorized'));
    throw new Error('Unauthorized');
  }

  if (!response.ok) {
    const errData = await response.json().catch(() => ({}));
    throw new Error(errData.message || 'API request failed');
  }

  const resBody = await response.json();
  return resBody.data;
}

export const api = {
  // Auth
  async login(payload: any): Promise<AuthResponse> {
    const res = await fetch(`${API_BASE_URL}/api/v1/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || 'Login failed');
    }
    const body = await res.json();
    return body.data;
  },

  async register(payload: any): Promise<AuthResponse> {
    const res = await fetch(`${API_BASE_URL}/api/v1/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || 'Registration failed');
    }
    const body = await res.json();
    return body.data;
  },

  // Farmer profile
  async getFarmerProfile(): Promise<FarmerResponse | null> {
    try {
      return await fetchClient<FarmerResponse>('/api/v1/farmers/me');
    } catch (e: any) {
      if (e.message?.includes('not found') || e.message?.includes('No farmer profile')) {
        return null;
      }
      throw e;
    }
  },

  async createFarmerProfile(payload: any): Promise<FarmerResponse> {
    return await fetchClient<FarmerResponse>('/api/v1/farmers', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },

  async updateFarmerProfile(id: string, payload: any): Promise<FarmerResponse> {
    return await fetchClient<FarmerResponse>(`/api/v1/farmers/${id}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    });
  },

  // Farms
  async listFarms(farmerId: string): Promise<FarmResponse[]> {
    return await fetchClient<FarmResponse[]>(`/api/v1/farms?farmerId=${farmerId}`);
  },

  async createFarm(payload: any): Promise<FarmResponse> {
    return await fetchClient<FarmResponse>('/api/v1/farms', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },

  async updateFarm(id: string, payload: any): Promise<FarmResponse> {
    return await fetchClient<FarmResponse>(`/api/v1/farms/${id}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    });
  },

  async deleteFarm(id: string): Promise<void> {
    return await fetchClient<void>(`/api/v1/farms/${id}`, {
      method: 'DELETE',
    });
  },

  // Crop Recommendation
  async generateCropRecommendations(farmId: string): Promise<CropRecommendationResponse[]> {
    return await fetchClient<CropRecommendationResponse[]>('/api/v1/recommendations/generate', {
      method: 'POST',
      body: JSON.stringify({ farmId }),
    });
  },

  async getCropRecommendations(farmId: string): Promise<CropRecommendationResponse[]> {
    return await fetchClient<CropRecommendationResponse[]>(`/api/v1/recommendations/${farmId}`);
  },

  async acceptCropRecommendation(id: string): Promise<CropRecommendationResponse> {
    return await fetchClient<CropRecommendationResponse>(`/api/v1/recommendations/${id}/accept`, {
      method: 'PATCH',
    });
  },

  // Weather
  async getCurrentWeather(lat: number, lon: number): Promise<WeatherResponse> {
    return await fetchClient<WeatherResponse>(`/api/v1/weather/current?lat=${lat}&lon=${lon}`);
  },

  async getWeatherForecast(lat: number, lon: number, days = 7): Promise<ForecastDay[]> {
    return await fetchClient<ForecastDay[]>(`/api/v1/weather/forecast?lat=${lat}&lon=${lon}&days=${days}`);
  },

  async getClimateRisk(farmId: string): Promise<ClimateRiskResponse> {
    return await fetchClient<ClimateRiskResponse>(`/api/v1/weather/risk/${farmId}`);
  },

  // Disease detection
  async analyzeDisease(formData: FormData): Promise<DiseaseDetectionResponse> {
    return await fetchClient<DiseaseDetectionResponse>('/api/v1/disease/analyze', {
      method: 'POST',
      body: formData,
    });
  },

  async getDiseaseReports(farmId: string): Promise<DiseaseDetectionResponse[]> {
    return await fetchClient<DiseaseDetectionResponse[]>(`/api/v1/disease/reports/${farmId}`);
  },

  // Schemes
  async getRecommendedSchemes(farmerId: string): Promise<SchemeRecommendationResponse[]> {
    return await fetchClient<SchemeRecommendationResponse[]>(`/api/v1/schemes/recommended/${farmerId}`);
  },

  async updateSchemeStatus(id: string, status: string): Promise<SchemeRecommendationResponse> {
    return await fetchClient<SchemeRecommendationResponse>(`/api/v1/schemes/recommendations/${id}/status`, {
      method: 'PATCH',
      body: JSON.stringify({ status }),
    });
  },

  // Translation
  async translate(text: string, srcLang: string, tgtLang: string): Promise<TranslationResponse> {
    return await fetchClient<TranslationResponse>('/api/v1/translate', {
      method: 'POST',
      body: JSON.stringify({
        sourceText: text,
        sourceLanguage: srcLang,
        targetLanguage: tgtLang,
      }),
    });
  },

  // Voice Assistant
  async voiceChat(audioBlob: Blob, language = 'hi', farmerId?: string): Promise<any> {
    const formData = new FormData();
    formData.append('audio', audioBlob, 'voice_query.wav');
    formData.append('language', language);
    if (farmerId) {
      formData.append('farmerId', farmerId);
    }
    return await fetchClient<any>('/api/v1/voice/chat', {
      method: 'POST',
      body: formData,
    });
  },

  // Text-based advisory chat (uses browser Web Speech API for STT, no AI sidecar needed)
  async textChat(queryText: string, language = 'hi', farmerId?: string, history?: {role: string, text: string}[]): Promise<any> {
    return await fetchClient<any>('/api/v1/voice/text-chat', {
      method: 'POST',
      body: JSON.stringify({ queryText, language, farmerId, history }),
    });
  },

  async synthesizeVoice(text: string, language = 'hi'): Promise<Blob> {
    const url = `${API_BASE_URL}/api/v1/voice/synthesize?text=${encodeURIComponent(text)}&language=${language}`;
    const token = localStorage.getItem('km_access_token');
    const headers = new Headers();
    if (token) {
      headers.set('Authorization', `Bearer ${token}`);
    }
    const res = await fetch(url, { headers });
    if (!res.ok) {
      throw new Error('Failed to synthesize voice');
    }
    return await res.blob();
  },

  // Analytics
  async getFarmerAnalytics(farmerId: string): Promise<FarmerAnalyticsResponse> {
    return await fetchClient<FarmerAnalyticsResponse>(`/api/v1/analytics/farmer/${farmerId}`);
  },
};
