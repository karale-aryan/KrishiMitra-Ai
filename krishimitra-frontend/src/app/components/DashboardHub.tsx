import { useState, useEffect } from 'react';
import {
  Sprout,
  CloudSun,
  ShieldAlert,
  FileText,
  Plus,
  Trash2,
  MapPin,
  User,
  Languages,
  LogOut,
  CheckCircle,
  TrendingUp,
  Droplet,
  Thermometer,
  Cpu,
  Layers,
  ChevronRight,
  Upload,
  AlertTriangle,
  FileSearch,
  Check,
  Eye,
  X,
  Volume2
} from 'lucide-react';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  LineChart,
  Line,
  PieChart,
  Pie,
  Cell
} from 'recharts';
import { api, FarmerResponse, FarmResponse, CropRecommendationResponse, WeatherResponse, ForecastDay, ClimateRiskResponse, DiseaseDetectionResponse, SchemeRecommendationResponse } from '../services/api';

interface DashboardHubProps {
  farmer: FarmerResponse;
  onLogout: () => void;
  onUpdateProfile: (updated: FarmerResponse) => void;
  openVoiceAssistant: () => void;
}

type TabType = 'overview' | 'farms' | 'crops' | 'weather' | 'disease' | 'schemes' | 'translate' | 'analytics';

const getMitigationGuidelines = (risk: any) => {
  if (!risk) return ["Weather conditions are stable. Follow standard crop guidelines."];
  const list: string[] = [];
  if (risk.droughtRisk?.level === 'HIGH' || risk.droughtRisk?.level === 'CRITICAL' || risk.droughtRisk?.level === 'MODERATE') {
    list.push("Drought Advisory: Plan supplementary irrigation and focus water application on critical plant growth phases.");
  }
  if (risk.floodRisk?.level === 'HIGH' || risk.floodRisk?.level === 'CRITICAL' || risk.floodRisk?.level === 'MODERATE') {
    list.push("Excessive Moisture: Check soil drainage and clear all field channels immediately to avoid waterlogging.");
  }
  if (risk.heatStressRisk?.level === 'HIGH' || risk.heatStressRisk?.level === 'CRITICAL' || risk.heatStressRisk?.level === 'MODERATE') {
    list.push("Heat Stress: Increase watering intervals during early morning or evening hours and apply crop mulching.");
  }
  if (risk.waterStressRisk?.level === 'HIGH' || risk.waterStressRisk?.level === 'CRITICAL' || risk.waterStressRisk?.level === 'MODERATE') {
    list.push("Water Stress: Utilize organic manure/compost to enhance soil organic matter and water holding capacity.");
  }
  if (list.length === 0) {
    list.push("Weather conditions are favorable. Follow standard Kharif/Rabi agricultural cycle practices.");
  }
  return list;
};

export default function DashboardHub({ farmer, onLogout, onUpdateProfile, openVoiceAssistant }: DashboardHubProps) {
  const [activeTab, setActiveTab] = useState<TabType>('overview');
  const [farms, setFarms] = useState<FarmResponse[]>([]);
  const [selectedFarm, setSelectedFarm] = useState<FarmResponse | null>(null);
  const [loadingFarms, setLoadingFarms] = useState(false);

  // Modal / Form States
  const [showAddFarmModal, setShowAddFarmModal] = useState(false);
  const [newFarm, setNewFarm] = useState({
    farmName: '',
    areaHectares: '',
    latitude: '',
    longitude: '',
    soilType: 'ALLUVIAL',
    irrigationType: 'CANAL',
    soilPh: '',
    nitrogenKgHa: '',
    phosphorusKgHa: '',
    potassiumKgHa: ''
  });

  // Weather States
  const [weather, setWeather] = useState<WeatherResponse | null>(null);
  const [forecast, setForecast] = useState<ForecastDay[]>([]);
  const [climateRisk, setClimateRisk] = useState<ClimateRiskResponse | null>(null);
  const [loadingWeather, setLoadingWeather] = useState(false);

  // Recommendations States
  const [recommendations, setRecommendations] = useState<CropRecommendationResponse[]>([]);
  const [loadingRecs, setLoadingRecs] = useState(false);

  // Disease States
  const [diseaseImage, setDiseaseImage] = useState<File | null>(null);
  const [diseaseImagePreview, setDiseaseImagePreview] = useState<string | null>(null);
  const [diseaseCrop, setDiseaseCrop] = useState('');
  const [diseaseResult, setDiseaseResult] = useState<DiseaseDetectionResponse | null>(null);
  const [loadingDisease, setLoadingDisease] = useState(false);
  const [diseaseHistory, setDiseaseHistory] = useState<DiseaseDetectionResponse[]>([]);

  // Schemes States
  const [schemes, setSchemes] = useState<SchemeRecommendationResponse[]>([]);
  const [loadingSchemes, setLoadingSchemes] = useState(false);

  // Translation States
  const [translateText, setTranslateText] = useState('');
  const [translateSrc, setTranslateSrc] = useState('en');
  const [translateTgt, setTranslateTgt] = useState('hi');
  const [translatedText, setTranslatedText] = useState('');
  const [translating, setTranslating] = useState(false);
  const [playingTranslation, setPlayingTranslation] = useState(false);

  // Analytics States
  const [analytics, setAnalytics] = useState<any>(null);

  // Global Refresh Functions
  const loadFarmsList = async (selectFirst = false) => {
    setLoadingFarms(true);
    try {
      const data = await api.listFarms(farmer.id);
      setFarms(data);
      if (data.length > 0) {
        if (selectFirst || !selectedFarm) {
          setSelectedFarm(data[0]);
        } else {
          const current = data.find(f => f.id === selectedFarm.id);
          setSelectedFarm(current || data[0]);
        }
      } else {
        setSelectedFarm(null);
      }
    } catch (err) {
      console.error('Error fetching farms', err);
    } finally {
      setLoadingFarms(false);
    }
  };

  useEffect(() => {
    loadFarmsList(true);
  }, [farmer.id]);

  // Load weather and risk when active farm changes
  useEffect(() => {
    if (!selectedFarm) {
      setWeather(null);
      setForecast([]);
      setClimateRisk(null);
      setRecommendations([]);
      setDiseaseHistory([]);
      return;
    }

    const loadFarmData = async () => {
      setLoadingWeather(true);
      try {
        const lat = selectedFarm.latitude || 28.6139; // default Delhi
        const lon = selectedFarm.longitude || 77.2090;

        const [wData, fData, rData, recsData, diseaseData] = await Promise.all([
          api.getCurrentWeather(lat, lon),
          api.getWeatherForecast(lat, lon),
          api.getClimateRisk(selectedFarm.id),
          api.getCropRecommendations(selectedFarm.id),
          api.getDiseaseReports(selectedFarm.id)
        ]);

        setWeather(wData);
        setForecast(fData);
        setClimateRisk(rData);
        setRecommendations(recsData);
        setDiseaseHistory(diseaseData);
      } catch (err) {
        console.error('Error loading farm data', err);
      } finally {
        setLoadingWeather(false);
      }
    };

    loadFarmData();
  }, [selectedFarm]);

  // Load schemes
  const loadSchemesList = async () => {
    setLoadingSchemes(true);
    try {
      const data = await api.getRecommendedSchemes(farmer.id);
      setSchemes(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoadingSchemes(false);
    }
  };

  useEffect(() => {
    if (activeTab === 'schemes') {
      loadSchemesList();
    }
  }, [activeTab, farmer.id]);

  // Load analytics
  useEffect(() => {
    if (activeTab === 'analytics' || activeTab === 'overview') {
      const fetchAnalytics = async () => {
        try {
          const data = await api.getFarmerAnalytics(farmer.id);
          setAnalytics(data);
        } catch (err) {
          console.error(err);
        }
      };
      fetchAnalytics();
    }
  }, [activeTab, selectedFarm, farmer.id]);

  // Add Farm Action
  const handleAddFarm = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const payload = {
        farmerId: farmer.id,
        farmName: newFarm.farmName,
        areaHectares: parseFloat(newFarm.areaHectares),
        latitude: newFarm.latitude ? parseFloat(newFarm.latitude) : null,
        longitude: newFarm.longitude ? parseFloat(newFarm.longitude) : null,
        soilType: newFarm.soilType,
        irrigationType: newFarm.irrigationType,
        soilPh: newFarm.soilPh ? parseFloat(newFarm.soilPh) : null,
        nitrogenKgHa: newFarm.nitrogenKgHa ? parseFloat(newFarm.nitrogenKgHa) : null,
        phosphorusKgHa: newFarm.phosphorusKgHa ? parseFloat(newFarm.phosphorusKgHa) : null,
        potassiumKgHa: newFarm.potassiumKgHa ? parseFloat(newFarm.potassiumKgHa) : null
      };

      const created = await api.createFarm(payload);
      setFarms([...farms, created]);
      setSelectedFarm(created);
      setShowAddFarmModal(false);
      // Reset form
      setNewFarm({
        farmName: '',
        areaHectares: '',
        latitude: '',
        longitude: '',
        soilType: 'ALLUVIAL',
        irrigationType: 'CANAL',
        soilPh: '',
        nitrogenKgHa: '',
        phosphorusKgHa: '',
        potassiumKgHa: ''
      });
    } catch (err: any) {
      alert(err.message || 'Failed to add farm');
    }
  };

  // Delete Farm Action
  const handleDeleteFarm = async (id: string) => {
    if (!confirm('Are you sure you want to delete this farm? This action is irreversible.')) return;
    try {
      await api.deleteFarm(id);
      loadFarmsList(true);
    } catch (err: any) {
      alert(err.message || 'Failed to delete farm');
    }
  };

  // Generate Recommendations
  const handleGenerateRecommendations = async () => {
    if (!selectedFarm) return;
    setLoadingRecs(true);
    try {
      const recs = await api.generateCropRecommendations(selectedFarm.id);
      setRecommendations(recs);
    } catch (err: any) {
      alert(err.message || 'Failed to generate recommendations');
    } finally {
      setLoadingRecs(false);
    }
  };

  // Accept Recommendation
  const handleAcceptRecommendation = async (id: string) => {
    try {
      const updated = await api.acceptCropRecommendation(id);
      setRecommendations(recommendations.map(r => r.id === id ? updated : r));
    } catch (err: any) {
      alert(err.message || 'Failed to accept recommendation');
    }
  };

  // Disease Upload Action
  const handleDiseaseDiagnose = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedFarm || !diseaseImage || !diseaseCrop) {
      alert('Please upload an image and fill out all fields.');
      return;
    }
    setLoadingDisease(true);
    try {
      const formData = new FormData();
      formData.append('image', diseaseImage);
      formData.append('farmId', selectedFarm.id);
      formData.append('cropName', diseaseCrop);

      const res = await api.analyzeDisease(formData);
      setDiseaseResult(res);
      setDiseaseHistory([res, ...diseaseHistory]);
      setDiseaseImage(null);
      setDiseaseImagePreview(null);
    } catch (err: any) {
      alert(err.message || 'Disease diagnosis failed');
    } finally {
      setLoadingDisease(false);
    }
  };

  // Scheme status updates
  const handleUpdateSchemeStatus = async (id: string, newStatus: string) => {
    try {
      const updated = await api.updateSchemeStatus(id, newStatus);
      setSchemes(schemes.map(s => s.id === id ? updated : s));
    } catch (err: any) {
      alert(err.message || 'Failed to update scheme status');
    }
  };

  // Translate Action
  const handleTranslate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!translateText.trim()) return;
    setTranslating(true);
    try {
      const res = await api.translate(translateText, translateSrc, translateTgt);
      setTranslatedText(res.translatedText);
    } catch (err: any) {
      alert(err.message || 'Translation failed');
    } finally {
      setTranslating(false);
    }
  };

  const handlePlayTranslation = async () => {
    if (!translatedText) return;
    setPlayingTranslation(true);
    try {
      const blob = await api.synthesizeVoice(translatedText, translateTgt);
      const url = URL.createObjectURL(blob);
      const audio = new Audio(url);
      audio.onended = () => setPlayingTranslation(false);
      audio.play();
    } catch (err) {
      console.error(err);
      setPlayingTranslation(false);
    }
  };

  // Colors mapping for charts
  const PIE_COLORS = ['#125106', '#2c6a14', '#456556', '#94d77e', '#aff498', '#d9f5d0'];

  return (
    <div className="min-h-screen bg-background flex flex-col relative" style={{ background: "#fcfaef" }}>
      {/* Sticky Top Navigation Bar */}
      <header
        className="sticky top-0 w-full z-50 h-20 bg-white/92 backdrop-blur-md border-b border-border transition-all duration-300 flex-shrink-0"
        style={{
          background: "rgba(252,250,239,0.92)",
          backdropFilter: "blur(12px)",
          borderBottom: "1px solid #c1c9b9"
        }}
      >
        <div className="max-w-[1400px] mx-auto h-full flex justify-between items-center px-4 md:px-8 gap-4">
          {/* Logo and App Name */}
          <div className="flex items-center gap-2.5 flex-shrink-0">
            <div className="w-10 h-10 rounded-full bg-[#125106] flex items-center justify-center text-white font-bold shadow-sm">
              {farmer.fullName.charAt(0)}
            </div>
            <div className="flex flex-col">
              <span
                className="text-lg font-bold leading-none tracking-tight"
                style={{ fontFamily: "Fraunces", color: "#125106" }}
              >
                KrishiMitra <span style={{ color: "#2e6b20" }}>AI</span>
              </span>
              <span className="text-[10px] text-muted-foreground font-semibold flex items-center gap-0.5 mt-0.5" style={{ fontFamily: 'Inter' }}>
                <MapPin size={10} /> {farmer.district}, {farmer.state}
              </span>
            </div>
          </div>

          {/* Horizontal Tabs for Navigation */}
          <nav className="hidden xl:flex items-center gap-1.5 overflow-x-auto py-1 scrollbar-none">
            {[
              { id: 'overview', label: 'Overview', icon: <Layers size={15} /> },
              { id: 'farms', label: 'My Farms', icon: <MapPin size={15} /> },
              { id: 'crops', label: 'Crop Advisory', icon: <Sprout size={15} /> },
              { id: 'weather', label: 'Weather & Climate', icon: <CloudSun size={15} /> },
              { id: 'disease', label: 'Disease Lab', icon: <ShieldAlert size={15} /> },
              { id: 'schemes', label: 'Scheme Finder', icon: <FileText size={15} /> },
              { id: 'translate', label: 'Translation', icon: <Languages size={15} /> },
              { id: 'analytics', label: 'Analytics', icon: <TrendingUp size={15} /> }
            ].map(tab => (
               <button
                 key={tab.id}
                 onClick={() => setActiveTab(tab.id as TabType)}
                 className={`flex items-center gap-1.5 px-3 py-2 rounded-full text-xs font-semibold transition-all active:scale-95 whitespace-nowrap ${
                   activeTab === tab.id
                     ? 'bg-[#125106] text-white shadow-sm'
                     : 'text-muted-foreground hover:bg-[#f0eee3] hover:text-[#1b1c16]'
                 }`}
                 style={{ fontFamily: 'Inter' }}
               >
                 {tab.icon}
                 {tab.label}
               </button>
            ))}
          </nav>

          {/* Right Actions: Switcher, Mic and Logout */}
          <div className="flex items-center gap-3">
            {activeTab !== 'translate' && activeTab !== 'farms' && (
              <div className="flex items-center gap-1.5 bg-[#f0eee3] px-2.5 py-1.5 rounded-2xl border border-[#c1c9b9]/40">
                {farms.length > 0 ? (
                  <select
                    value={selectedFarm?.id || ''}
                    onChange={e => setSelectedFarm(farms.find(f => f.id === e.target.value) || null)}
                    className="bg-transparent border-none text-xs font-bold text-[#125106] outline-none cursor-pointer p-0"
                    style={{ fontFamily: 'Inter' }}
                  >
                     {farms.map(f => (
                       <option key={f.id} value={f.id}>{f.farmName}</option>
                     ))}
                  </select>
                ) : (
                  <button
                    onClick={() => setActiveTab('farms')}
                    className="text-xs font-bold text-primary"
                  >
                    + Add Farm
                  </button>
                )}
              </div>
            )}

            <button
              onClick={openVoiceAssistant}
              className="w-10 h-10 rounded-full bg-[#125106] text-white flex items-center justify-center hover:bg-[#2e6b20] active:scale-90 transition-all shadow-md"
              title="Speak to Assistant"
            >
              <Cpu size={18} />
            </button>

            <button
              onClick={onLogout}
              className="px-4 py-2 rounded-full border border-[#c1c9b9] text-destructive font-bold text-xs hover:bg-red-50 hover:border-red-200 active:scale-95 transition-all flex items-center gap-1"
              style={{ fontFamily: 'Inter' }}
            >
              <LogOut size={13} /> <span className="hidden sm:inline">Log Out</span>
            </button>
          </div>
        </div>

        {/* Mobile Tab strip */}
        <div className="xl:hidden flex items-center gap-1.5 overflow-x-auto px-4 py-2 border-t border-[#c1c9b9] bg-white/85 scrollbar-none">
          {[
            { id: 'overview', label: 'Overview', icon: <Layers size={14} /> },
            { id: 'farms', label: 'Farms', icon: <MapPin size={14} /> },
            { id: 'crops', label: 'Crops', icon: <Sprout size={14} /> },
            { id: 'weather', label: 'Weather', icon: <CloudSun size={14} /> },
            { id: 'disease', label: 'Disease', icon: <ShieldAlert size={14} /> },
            { id: 'schemes', label: 'Schemes', icon: <FileText size={14} /> },
            { id: 'translate', label: 'Translate', icon: <Languages size={14} /> },
            { id: 'analytics', label: 'Analytics', icon: <TrendingUp size={14} /> }
          ].map(tab => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id as TabType)}
              className={`flex items-center gap-1 px-2.5 py-1 rounded-full text-[11px] font-semibold transition-all whitespace-nowrap ${
                activeTab === tab.id
                  ? 'bg-[#125106] text-white'
                  : 'text-muted-foreground hover:bg-[#f0eee3]'
              }`}
            >
              {tab.icon}
              {tab.label}
            </button>
          ))}
        </div>
      </header>

      {/* Main Content Area */}
      <main className="flex-1 flex flex-col min-w-0 bg-[#fcfaef]">
        {/* Tab Subviews */}
        <div className="flex-1 overflow-y-auto p-4 md:p-8">
          
          {/* TAB 1: OVERVIEW */}
          {activeTab === 'overview' && (
            <div className="space-y-8 max-w-[1200px]">
              {/* Profile Card */}
              <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                <div className="lg:col-span-2 relative rounded-[32px] overflow-hidden p-8 text-white min-h-[240px] flex flex-col justify-end shadow-md" style={{ background: 'linear-gradient(to top, rgba(18,81,6,0.95) 0%, rgba(18,81,6,0.4) 100%)' }}>
                  <img
                    src="https://images.unsplash.com/photo-1500937386664-56d1dfef3854?w=1200&h=600&fit=crop&auto=format"
                    alt="Farmland Banner"
                    className="absolute inset-0 w-full h-full object-cover -z-10 opacity-40"
                  />
                  <div>
                    <span className="text-xs font-bold uppercase tracking-[0.2em] text-[#aff498] mb-2 block">Premium Advisor Portal</span>
                    <h2 className="text-3xl md:text-4xl font-bold mb-3 animate-fade-in" style={{ fontFamily: 'Fraunces' }}>Namaskar, {farmer.fullName}!</h2>
                    <p className="text-white/90 text-sm max-w-xl leading-relaxed mb-4" style={{ fontFamily: 'Inter' }}>
                      Your personalized rules-based ML predictions, climate risks, and government programs are consolidated for {farmer.district}, {farmer.state}.
                    </p>
                  </div>
                  <div className="flex flex-wrap gap-3">
                    <div className="bg-[#fcfaef]/15 backdrop-blur-sm px-4 py-2 rounded-2xl text-xs font-bold text-white flex items-center gap-2">
                      <MapPin size={14} className="text-[#aff498]" /> Village: {farmer.village || 'N/A'} (PIN: {farmer.pincode})
                    </div>
                    <div className="bg-[#fcfaef]/15 backdrop-blur-sm px-4 py-2 rounded-2xl text-xs font-bold text-white flex items-center gap-2">
                      <Sprout size={14} className="text-[#aff498]" /> Holdings: {farmer.landHoldingHectares} Ha
                    </div>
                  </div>
                </div>

                {/* Weather Quick Widget */}
                <div className="bg-primary text-white p-8 rounded-[32px] shadow-lg flex flex-col justify-between relative overflow-hidden" style={{ background: 'linear-gradient(135deg, #125106 0%, #2e6b20 100%)' }}>
                  {loadingWeather ? (
                    <div className="flex items-center justify-center h-full">
                      <div className="w-8 h-8 border-4 border-white/30 border-t-white rounded-full animate-spin" />
                    </div>
                  ) : selectedFarm && weather ? (
                    <>
                      <div className="flex justify-between items-start">
                        <div>
                          <p className="text-xs font-bold text-white/70 uppercase tracking-widest">{selectedFarm.farmName}</p>
                          <h4 className="text-xl font-bold mt-1">{weather.description}</h4>
                        </div>
                        <CloudSun size={36} className="opacity-90" />
                      </div>
                      <div className="mt-8">
                        <div className="text-5xl font-extrabold tracking-tight flex items-start">
                          {weather.temperature}°C
                        </div>
                        <div className="grid grid-cols-2 gap-4 mt-6 text-xs text-white/80">
                          <div className="flex items-center gap-1.5"><Droplet size={14} /> Hum: {weather.humidity}%</div>
                          <div className="flex items-center gap-1.5"><TrendingUp size={14} /> Wind: {weather.windSpeed} km/h</div>
                        </div>
                      </div>
                    </>
                  ) : (
                    <div className="flex flex-col items-center justify-center text-center h-full gap-2">
                      <CloudSun size={32} />
                      <p className="text-sm font-semibold text-white/80">Add/Select a farm to view local weather</p>
                    </div>
                  )}
                </div>
              </div>

              {/* Quick Actions Panel */}
              <div className="bg-white p-8 rounded-[32px] border border-border shadow-sm">
                <h3 className="font-semibold text-lg mb-6" style={{ fontFamily: 'Fraunces' }}>Quick Tools</h3>
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                  {[
                    { title: 'Add a New Farm', desc: 'Map land & soil parameters', action: () => setActiveTab('farms'), icon: <Plus size={20} className="text-primary" /> },
                    { title: 'Test Soil Crop', desc: 'Predict best crops with ML', action: () => setActiveTab('crops'), icon: <Sprout size={20} className="text-primary" /> },
                    { title: 'Diagnose Crop Disease', desc: 'Upload images to analyze', action: () => setActiveTab('disease'), icon: <ShieldAlert size={20} className="text-primary" /> },
                    { title: 'Eligible Subsidies', desc: 'Browse matched schemes', action: () => setActiveTab('schemes'), icon: <FileText size={20} className="text-primary" /> }
                  ].map(action => (
                    <button
                      key={action.title}
                      onClick={action.action}
                      className="p-5 rounded-2xl border border-border bg-secondary/20 hover:bg-secondary/50 hover:border-primary/20 text-left transition-all group"
                    >
                      <div className="w-10 h-10 rounded-xl bg-white flex items-center justify-center shadow-sm mb-4 group-hover:scale-105 transition-all">
                        {action.icon}
                      </div>
                      <h4 className="font-bold text-sm mb-1 text-on-surface flex items-center gap-1">
                        {action.title} <ChevronRight size={14} className="opacity-0 group-hover:opacity-100 transition-opacity" />
                      </h4>
                      <p className="text-xs text-muted-foreground">{action.desc}</p>
                    </button>
                  ))}
                </div>
              </div>

              {/* Analytics overview */}
              {analytics && (
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                  {/* Soil Chemistry card */}
                  <div className="bg-white p-8 rounded-[32px] border border-border shadow-sm">
                    <h3 className="font-semibold text-lg mb-6" style={{ fontFamily: 'Fraunces' }}>Soil NPK Values Comparison</h3>
                    {analytics.soilHealthTrend && analytics.soilHealthTrend.length > 0 ? (
                      <div className="h-64">
                        <ResponsiveContainer width="100%" height="100%">
                          <BarChart data={analytics.soilHealthTrend}>
                            <CartesianGrid strokeDasharray="3 3" vertical={false} />
                            <XAxis dataKey="farmName" tick={{ fill: '#717a6b', fontSize: 11 }} />
                            <YAxis tick={{ fill: '#717a6b', fontSize: 11 }} />
                            <Tooltip />
                            <Legend />
                            <Bar dataKey="n" fill="#125106" name="Nitrogen (N)" radius={[4, 4, 0, 0]} />
                            <Bar dataKey="p" fill="#456556" name="Phosphorus (P)" radius={[4, 4, 0, 0]} />
                            <Bar dataKey="k" fill="#aff498" name="Potassium (K)" radius={[4, 4, 0, 0]} />
                          </BarChart>
                        </ResponsiveContainer>
                      </div>
                    ) : (
                      <p className="text-sm text-muted-foreground text-center py-10">Add soil data in Farms Hub to view comparison.</p>
                    )}
                  </div>

                  {/* Recent Activities */}
                  <div className="bg-white p-8 rounded-[32px] border border-border shadow-sm flex flex-col justify-between">
                    <div>
                      <h3 className="font-semibold text-lg mb-6" style={{ fontFamily: 'Fraunces' }}>Recent Activities</h3>
                      <div className="space-y-4">
                        {analytics.recentActivity && analytics.recentActivity.length > 0 ? (
                          analytics.recentActivity.map((act: any, idx: number) => (
                            <div key={idx} className="flex gap-4 items-start">
                              <div className="w-2.5 h-2.5 rounded-full bg-primary mt-1.5 flex-shrink-0" />
                              <div>
                                <p className="text-sm text-on-surface font-medium">{act.description}</p>
                                <span className="text-xs text-muted-foreground">{new Date(act.date).toLocaleDateString()}</span>
                              </div>
                            </div>
                          ))
                        ) : (
                          <p className="text-sm text-muted-foreground text-center py-10">No recent activities log found.</p>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              )}
            </div>
          )}

          {/* TAB 2: FARMS HUB */}
          {activeTab === 'farms' && (
            <div className="space-y-8 max-w-[1200px]">
              <div className="flex justify-between items-center">
                <div>
                  <h3 className="font-bold text-lg" style={{ fontFamily: 'Fraunces' }}>Farms List</h3>
                  <p className="text-xs text-muted-foreground">Manage your farmlands and input detailed chemical components of soil.</p>
                </div>
                <button
                  onClick={() => setShowAddFarmModal(true)}
                  className="flex items-center gap-2 bg-primary text-white px-5 py-3 rounded-full font-semibold text-sm hover:opacity-90 active:scale-95 transition-all shadow-sm"
                >
                  <Plus size={16} /> Add New Farm
                </button>
              </div>

              {loadingFarms ? (
                <div className="flex justify-center py-20">
                  <div className="w-12 h-12 border-4 border-primary/20 border-t-primary rounded-full animate-spin" />
                </div>
              ) : farms.length > 0 ? (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                  {farms.map(f => (
                    <div key={f.id} className="bg-white rounded-[32px] border border-border p-6 shadow-sm flex flex-col justify-between hover:border-primary/20 transition-all">
                      <div>
                        <div className="flex justify-between items-start mb-4">
                          <h4 className="font-bold text-lg text-primary">{f.farmName}</h4>
                          <span className="text-xs bg-secondary font-bold text-primary px-3 py-1 rounded-full">{f.areaHectares} Ha</span>
                        </div>

                        <div className="space-y-2 mb-6">
                          <p className="text-xs text-muted-foreground flex items-center gap-1.5">
                            <MapPin size={12} /> Coord: {f.latitude ? `${f.latitude.toFixed(4)}, ${f.longitude?.toFixed(4)}` : 'Not Set'}
                          </p>
                          <p className="text-xs text-muted-foreground flex items-center gap-1.5">
                            <Droplet size={12} /> Irrigation: <span className="font-semibold text-on-surface">{f.irrigationType}</span>
                          </p>
                          <p className="text-xs text-muted-foreground flex items-center gap-1.5">
                            <Sprout size={12} /> Soil: <span className="font-semibold text-on-surface">{f.soilType} Soil (pH: {f.soilPh || 'N/A'})</span>
                          </p>
                        </div>

                        {/* NPK Values Indicator */}
                        <div className="bg-muted p-4 rounded-2xl mb-6 space-y-2">
                          <div className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider mb-2">Soil NPK Content (kg/ha)</div>
                          {[
                            { label: 'Nitrogen (N)', val: f.nitrogenKgHa, max: 200, color: 'bg-primary' },
                            { label: 'Phosphorus (P)', val: f.phosphorusKgHa, max: 100, color: 'bg-primary-container' },
                            { label: 'Potassium (K)', val: f.potassiumKgHa, max: 250, color: 'bg-accent' }
                          ].map(npk => (
                            <div key={npk.label} className="space-y-0.5">
                              <div className="flex justify-between text-xs font-semibold">
                                <span className="text-muted-foreground">{npk.label}</span>
                                <span>{npk.val || 0} kg/ha</span>
                              </div>
                              <div className="h-1.5 w-full bg-white rounded-full overflow-hidden">
                                <div
                                  className={`h-full ${npk.color}`}
                                  style={{ width: `${Math.min(((npk.val || 0) / npk.max) * 100, 100)}%` }}
                                />
                              </div>
                            </div>
                          ))}
                        </div>
                      </div>

                      <div className="flex gap-2">
                        <button
                          onClick={() => {
                            setSelectedFarm(f);
                            setActiveTab('crops');
                          }}
                          className="flex-1 bg-secondary text-primary font-bold text-xs py-2.5 rounded-xl text-center hover:bg-primary hover:text-white transition-all"
                        >
                          Advisory
                        </button>
                        <button
                          onClick={() => handleDeleteFarm(f.id)}
                          className="w-10 h-10 border border-border rounded-xl flex items-center justify-center text-destructive hover:bg-red-50 hover:border-red-200 transition-all"
                        >
                          <Trash2 size={16} />
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="bg-white rounded-[32px] border border-border p-12 text-center max-w-md mx-auto shadow-sm">
                  <div className="w-16 h-16 bg-primary-container/20 rounded-full flex items-center justify-center text-primary mx-auto mb-6">
                    <MapPin size={24} />
                  </div>
                  <h3 className="font-bold text-lg mb-2">No Farms Added Yet</h3>
                  <p className="text-sm text-muted-foreground mb-6">You need to add at least one farm with soil properties to receive AI recommendations.</p>
                  <button
                    onClick={() => setShowAddFarmModal(true)}
                    className="bg-primary text-white px-6 py-3 rounded-full font-semibold text-sm hover:opacity-90 active:scale-95 transition-all shadow-sm"
                  >
                    Add Your First Farm
                  </button>
                </div>
              )}
            </div>
          )}

          {/* TAB 3: CROP RECOMMENDATION */}
          {activeTab === 'crops' && (
            <div className="space-y-8 max-w-[1200px]">
              {selectedFarm ? (
                <>
                  <div className="relative rounded-[32px] overflow-hidden p-8 text-white min-h-[180px] flex flex-col justify-end shadow-md" style={{ background: 'linear-gradient(to top, rgba(18,81,6,0.9) 0%, rgba(18,81,6,0.3) 100%)' }}>
                    <img
                      src="https://images.unsplash.com/photo-1595855759920-86582396756a?w=1200&h=400&fit=crop&auto=format"
                      alt="Crop Advisory"
                      className="absolute inset-0 w-full h-full object-cover -z-10 opacity-40"
                    />
                    <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 w-full">
                      <div>
                        <span className="text-xs font-bold uppercase tracking-[0.2em] text-[#aff498] mb-1 block">ML Recommendation System</span>
                        <h3 className="font-bold text-2xl" style={{ fontFamily: 'Fraunces' }}>Crop Recommendation for {selectedFarm.farmName}</h3>
                        <p className="text-white/80 text-xs max-w-xl leading-relaxed mt-1" style={{ fontFamily: 'Inter' }}>
                          AI runs a Random Forest classifier matching local soil composition (NPK/pH) and regional meteorological trends to generate crop suitability metrics.
                        </p>
                      </div>
                      <button
                        onClick={handleGenerateRecommendations}
                        disabled={loadingRecs}
                        className="bg-[#125106] hover:bg-[#2e6b20] text-white px-6 py-3.5 rounded-full font-bold text-xs active:scale-95 transition-all shadow-md flex items-center gap-2 flex-shrink-0 disabled:opacity-50 border border-[#aff498]/30"
                      >
                        <Cpu size={14} />
                        {loadingRecs ? 'Analyzing soil...' : 'Run Soil Crop Diagnostic'}
                      </button>
                    </div>
                  </div>

                  {recommendations.length > 0 ? (
                    <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                      {/* Top Recommendations Cards */}
                      <div className="lg:col-span-2 space-y-4">
                        <h4 className="font-semibold text-lg text-primary mb-2 flex items-center gap-2">
                          <CheckCircle size={18} /> Recommended Crops
                        </h4>
                        {recommendations.map((rec, idx) => (
                          <div
                            key={rec.id}
                            className={`p-6 rounded-[28px] border bg-white flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 transition-all ${
                              rec.isAccepted ? 'border-primary shadow-sm bg-primary/5' : 'border-border hover:border-primary/20'
                            }`}
                          >
                            <div className="flex gap-4">
                              <div className="w-12 h-12 rounded-2xl bg-secondary flex items-center justify-center text-primary font-bold text-lg flex-shrink-0">
                                {idx + 1}
                              </div>
                              <div>
                                <h5 className="font-bold text-lg text-on-surface">
                                  {rec.cropName} <span className="text-sm text-muted-foreground font-semibold">({rec.cropNameHi || 'हिन्दी'})</span>
                                </h5>
                                <div className="flex items-center gap-3 mt-1.5">
                                  <span className="text-xs font-semibold px-2.5 py-0.5 rounded-full bg-secondary text-primary uppercase">
                                    {rec.season || 'KHARIF'}
                                  </span>
                                  <span className="text-xs text-muted-foreground">Confidence: <strong>{(rec.confidenceScore * 100).toFixed(1)}%</strong></span>
                                </div>
                              </div>
                            </div>

                            <div className="flex items-center gap-3 w-full sm:w-auto">
                              {rec.isAccepted ? (
                                <span className="bg-primary text-white font-bold text-xs px-4 py-2.5 rounded-xl flex items-center gap-1.5 w-full sm:w-auto justify-center">
                                  <Check size={14} /> Accepted Crop
                                </span>
                              ) : (
                                <button
                                  onClick={() => handleAcceptRecommendation(rec.id)}
                                  className="w-full sm:w-auto bg-secondary text-primary hover:bg-primary hover:text-white font-bold text-xs px-4 py-2.5 rounded-xl transition-all"
                                >
                                  Mark Accepted
                                </button>
                              )}
                            </div>
                          </div>
                        ))}
                      </div>

                      {/* Model Feature Inputs Details */}
                      <div className="space-y-6">
                        <div className="bg-white p-6 rounded-[28px] border border-border shadow-sm">
                          <h4 className="font-bold text-base mb-4 text-on-surface">Model Details</h4>
                          <div className="space-y-3 text-xs">
                            <div className="flex justify-between py-1 border-b border-border">
                              <span className="text-muted-foreground">Classifier:</span>
                              <span className="font-semibold text-on-surface">Random Forest (Kaggle)</span>
                            </div>
                            <div className="flex justify-between py-1 border-b border-border">
                              <span className="text-muted-foreground">Model Format:</span>
                              <span className="font-semibold text-on-surface">ONNX Runtime v1.16</span>
                            </div>
                            <div className="flex justify-between py-1 border-b border-border">
                              <span className="text-muted-foreground">Inference Speed:</span>
                              <span className="font-semibold text-on-surface">~12ms (Direct JNI)</span>
                            </div>
                          </div>
                        </div>

                        {/* Recommendation Chart */}
                        <div className="bg-white p-6 rounded-[28px] border border-border shadow-sm">
                          <h4 className="font-bold text-base mb-4 text-on-surface">Suitability Score Graph</h4>
                          <div className="h-48">
                            <ResponsiveContainer width="100%" height="100%">
                              <BarChart
                                data={recommendations.map(r => ({
                                  name: r.cropName,
                                  score: parseFloat((r.confidenceScore * 100).toFixed(0))
                                }))}
                                layout="vertical"
                              >
                                <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                                <XAxis type="number" domain={[0, 100]} hide />
                                <YAxis dataKey="name" type="category" width={60} tick={{ fontSize: 11 }} />
                                <Tooltip />
                                <Bar dataKey="score" fill="#125106" radius={[0, 4, 4, 0]} />
                              </BarChart>
                            </ResponsiveContainer>
                          </div>
                        </div>
                      </div>
                    </div>
                  ) : (
                    <div className="bg-white rounded-[32px] border border-border p-12 text-center max-w-md mx-auto shadow-sm">
                      <div className="w-16 h-16 bg-primary-container/20 rounded-full flex items-center justify-center text-primary mx-auto mb-6">
                        <Cpu size={24} />
                      </div>
                      <h3 className="font-bold text-lg mb-2">No Recommendations Yet</h3>
                      <p className="text-sm text-muted-foreground mb-6">Submit soil values and run the ML models to calculate optimal agricultural suitability metrics.</p>
                      <button
                        onClick={handleGenerateRecommendations}
                        disabled={loadingRecs}
                        className="bg-primary text-white px-6 py-3 rounded-full font-semibold text-sm hover:opacity-90 active:scale-95 transition-all shadow-sm"
                      >
                        {loadingRecs ? 'Running models...' : 'Run Soil Crop Diagnostic'}
                      </button>
                    </div>
                  )}
                </>
              ) : (
                <p className="text-sm text-muted-foreground text-center py-10">Add your first farm in Farm Hub to run crop recommendations.</p>
              )}
            </div>
          )}

          {/* TAB 4: WEATHER & CLIMATE */}
          {activeTab === 'weather' && (
            <div className="space-y-8 max-w-[1200px]">
              {selectedFarm ? (
                <>
                  {/* Weather banner */}
                  <div className="relative rounded-[32px] overflow-hidden p-8 text-white min-h-[160px] flex flex-col justify-end shadow-md mb-8" style={{ background: 'linear-gradient(to top, rgba(18,81,6,0.9) 0%, rgba(18,81,6,0.3) 100%)' }}>
                    <img
                      src="https://images.unsplash.com/photo-1534088568595-a066f410bcda?w=1200&h=400&fit=crop&auto=format"
                      alt="Weather Forecast"
                      className="absolute inset-0 w-full h-full object-cover -z-10 opacity-40"
                    />
                    <div>
                      <span className="text-xs font-bold uppercase tracking-[0.2em] text-[#aff498] mb-1 block">Meteorological Analytics</span>
                      <h3 className="font-bold text-2xl" style={{ fontFamily: 'Fraunces' }}>Weather & Climate Risks for {selectedFarm.farmName}</h3>
                    </div>
                  </div>

                  {/* Weather Forecast Graphs */}
                  <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                    <div className="lg:col-span-2 bg-white p-8 rounded-[32px] border border-border shadow-sm">
                      <h3 className="font-bold text-lg mb-6" style={{ fontFamily: 'Fraunces' }}>7-Day Temperature Trend (°C)</h3>
                      {forecast.length > 0 ? (
                        <div className="h-64">
                          <ResponsiveContainer width="100%" height="100%">
                            <LineChart data={forecast.map(f => ({
                              day: new Date(f.date).toLocaleDateString('en-IN', { weekday: 'short' }),
                              Max: f.maxTemp,
                              Min: f.minTemp
                            }))}>
                              <CartesianGrid strokeDasharray="3 3" vertical={false} />
                              <XAxis dataKey="day" tick={{ fill: '#717a6b', fontSize: 11 }} />
                              <YAxis tick={{ fill: '#717a6b', fontSize: 11 }} />
                              <Tooltip />
                              <Legend />
                              <Line type="monotone" dataKey="Max" stroke="#ba1a1a" strokeWidth={2.5} activeDot={{ r: 6 }} />
                              <Line type="monotone" dataKey="Min" stroke="#125106" strokeWidth={2.5} />
                            </LineChart>
                          </ResponsiveContainer>
                        </div>
                      ) : (
                        <p className="text-sm text-muted-foreground text-center py-10">Loading weather forecasting curves...</p>
                      )}
                    </div>

                    {/* Climate Risk Metrics */}
                    <div className="bg-white p-8 rounded-[32px] border border-border shadow-sm flex flex-col justify-between">
                      <div>
                        <h3 className="font-bold text-lg mb-6" style={{ fontFamily: 'Fraunces' }}>Climate Risk Evaluation</h3>
                        {climateRisk ? (
                          <div className="space-y-4">
                            {[
                              { label: 'Drought Risk', val: climateRisk.droughtRisk?.level, text: climateRisk.droughtRisk?.explanation },
                              { label: 'Rainfall Anomaly', val: climateRisk.floodRisk?.level, text: climateRisk.floodRisk?.explanation },
                              { label: 'Water Stress', val: climateRisk.waterStressRisk?.level, text: climateRisk.waterStressRisk?.explanation },
                              { label: 'Heat Stress', val: climateRisk.heatStressRisk?.level, text: climateRisk.heatStressRisk?.explanation }
                            ].map(risk => (
                              <div key={risk.label} className="p-3 bg-[#f0eee3]/60 rounded-2xl flex gap-3 items-start border border-[#c1c9b9]/20">
                                <AlertTriangle size={18} className={
                                  risk.val === 'LOW' ? 'text-green-600' :
                                  risk.val === 'MODERATE' ? 'text-amber-500' :
                                  'text-red-500'
                                } />
                                <div>
                                  <div className="flex gap-2 items-center">
                                    <span className="font-bold text-xs text-[#1b1c16]" style={{ fontFamily: 'Inter' }}>{risk.label}</span>
                                    <span className={`text-[9px] font-extrabold px-1.5 py-0.5 rounded ${
                                      risk.val === 'LOW' ? 'bg-green-100 text-green-800' :
                                      risk.val === 'MODERATE' ? 'bg-amber-100 text-amber-800' :
                                      'bg-red-100 text-red-800'
                                    }`}>{risk.val || 'LOW'}</span>
                                  </div>
                                  <p className="text-[11px] text-muted-foreground mt-0.5 leading-snug">{risk.text || 'No significant anomaly predicted.'}</p>
                                </div>
                              </div>
                            ))}
                          </div>
                        ) : (
                          <p className="text-sm text-muted-foreground text-center py-10">Running localized satellite indices check...</p>
                        )}
                      </div>
                    </div>
                  </div>

                  {/* Mitigation Recommendations */}
                  <div className="bg-white p-8 rounded-[32px] border border-border shadow-sm">
                    <h3 className="font-semibold text-lg mb-4" style={{ fontFamily: 'Fraunces' }}>Mitigation Guidelines</h3>
                    <ul className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      {getMitigationGuidelines(climateRisk).map((rec, idx) => (
                        <li key={idx} className="flex gap-3 bg-[#f0eee3]/40 p-4 rounded-2xl items-start border border-[#c1c9b9]/10">
                          <span className="w-5 h-5 rounded-full bg-[#125106] text-white text-[10px] font-bold flex items-center justify-center flex-shrink-0 mt-0.5">
                            {idx + 1}
                          </span>
                          <span className="text-xs text-[#41493d] font-medium leading-relaxed" style={{ fontFamily: 'Inter' }}>{rec}</span>
                        </li>
                      ))}
                    </ul>
                  </div>
                </>
              ) : (
                <div className="bg-white rounded-[32px] border border-border p-12 text-center max-w-md mx-auto shadow-sm">
                  <div className="w-16 h-16 bg-[#f0eee3] rounded-full flex items-center justify-center text-[#125106] mx-auto mb-6">
                    <CloudSun size={24} />
                  </div>
                  <h3 className="font-bold text-lg mb-2" style={{ fontFamily: 'Fraunces' }}>No Farm Selected</h3>
                  <p className="text-sm text-muted-foreground mb-6">Add or select a farm to request hyper-local meteorological forecasts.</p>
                  <button
                    onClick={() => setActiveTab('farms')}
                    className="bg-[#125106] text-white px-6 py-3 rounded-full font-semibold text-sm hover:bg-[#2e6b20] transition-all shadow-sm"
                  >
                    Go to Farms Hub
                  </button>
                </div>
              )}
            </div>
          )}

          {/* TAB 5: DISEASE DETECTION */}
          {activeTab === 'disease' && (
            <div className="space-y-8 max-w-[1200px]">
              {selectedFarm ? (
                <>
                  {/* Disease banner */}
                  <div className="relative rounded-[32px] overflow-hidden p-8 text-white min-h-[160px] flex flex-col justify-end shadow-md mb-8" style={{ background: 'linear-gradient(to top, rgba(18,81,6,0.9) 0%, rgba(18,81,6,0.3) 100%)' }}>
                    <img
                      src="https://images.unsplash.com/photo-1463171359979-300627e268f9?w=1200&h=400&fit=crop&auto=format"
                      alt="Disease Detection"
                      className="absolute inset-0 w-full h-full object-cover -z-10 opacity-40"
                    />
                    <div>
                      <span className="text-xs font-bold uppercase tracking-[0.2em] text-[#aff498] mb-1 block">Plant Diagnostics Lab</span>
                      <h3 className="font-bold text-2xl" style={{ fontFamily: 'Fraunces' }}>AI Crop Doctor (Disease Diagnostics)</h3>
                    </div>
                  </div>

                  <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                    {/* Diagnosis Lab Form */}
                    <div className="lg:col-span-2 bg-white p-8 rounded-[32px] border border-border shadow-sm">
                      <h3 className="font-bold text-xl mb-2" style={{ fontFamily: 'Fraunces', color: '#125106' }}>Crop Disease Laboratory</h3>
                      <p className="text-sm text-muted-foreground mb-6">Upload a clear photo of the infected crop leaf. AI analyzes 38 classes via MobileNetV2 ONNX model to output details.</p>

                      <form onSubmit={handleDiseaseDiagnose} className="space-y-6">
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
                          <div className="space-y-2">
                            <label className="text-xs font-bold text-muted-foreground" style={{ fontFamily: 'Inter' }}>Infected Crop Category</label>
                            <input
                              type="text"
                              placeholder="e.g. Wheat, Tomato, Potato"
                              value={diseaseCrop}
                              onChange={e => setDiseaseCrop(e.target.value)}
                              required
                              className="w-full bg-[#f0eee3]/60 text-sm border border-[#c1c9b9]/30 rounded-2xl px-4 py-3.5 focus:ring-1 focus:ring-primary outline-none"
                            />
                          </div>
                        </div>

                        {/* File Uploader */}
                        <div className="space-y-2">
                          <label className="text-xs font-bold text-muted-foreground" style={{ fontFamily: 'Inter' }}>Leaf Image File</label>
                          <div className="border-2 border-dashed border-[#c1c9b9] rounded-[28px] p-8 text-center hover:border-[#125106]/45 transition-colors relative">
                            {diseaseImagePreview ? (
                              <div className="space-y-4">
                                <img src={diseaseImagePreview} alt="Preview" className="max-h-60 mx-auto rounded-2xl object-cover" />
                                <button
                                  type="button"
                                  onClick={() => {
                                    setDiseaseImage(null);
                                    setDiseaseImagePreview(null);
                                  }}
                                  className="bg-red-50 border border-red-200 text-destructive text-xs font-semibold px-4 py-2 rounded-xl"
                                >
                                  Remove Image
                                </button>
                              </div>
                            ) : (
                              <div className="space-y-4 py-4">
                                <Upload size={32} className="text-muted-foreground mx-auto" />
                                <div>
                                  <p className="text-sm font-semibold" style={{ fontFamily: 'Inter' }}>Drag & drop leaf photo here, or click to browse</p>
                                  <p className="text-xs text-muted-foreground mt-1">Accepts JPEG/PNG up to 10MB</p>
                                </div>
                                <input
                                  type="file"
                                  accept="image/*"
                                  onChange={e => {
                                    if (e.target.files && e.target.files[0]) {
                                      const file = e.target.files[0];
                                      setDiseaseImage(file);
                                      setDiseaseImagePreview(URL.createObjectURL(file));
                                    }
                                  }}
                                  className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                                />
                              </div>
                            )}
                          </div>
                        </div>

                        <button
                          type="submit"
                          disabled={loadingDisease || !diseaseImage}
                          className="w-full bg-[#125106] hover:bg-[#2e6b20] text-white py-4 rounded-full font-bold text-sm active:scale-95 transition-all disabled:opacity-50 shadow-md"
                          style={{ fontFamily: 'Inter' }}
                        >
                          {loadingDisease ? 'Processing neural diagnosis...' : 'Start Diagnostic Sweep'}
                        </button>
                      </form>

                      {/* Diagnostic Result Panel */}
                      {diseaseResult && (
                        <div className="mt-8 p-6 bg-[#f0eee3]/30 rounded-[28px] border border-[#125106]/10">
                          <h4 className="font-bold text-lg text-primary flex items-center gap-2 mb-4">
                            <CheckCircle size={20} /> Diagnostic Summary
                          </h4>
                          <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
                            <div className="space-y-3">
                              <div>
                                <span className="text-[10px] text-muted-foreground uppercase font-bold tracking-wider">Detected Condition</span>
                                <p className="font-bold text-lg text-on-surface">{diseaseResult.detectedDisease}</p>
                              </div>
                              <div>
                                <span className="text-[10px] text-muted-foreground uppercase font-bold tracking-wider">Neural Confidence</span>
                                <p className="font-bold text-sm text-on-surface">{(diseaseResult.confidenceScore * 100).toFixed(1)}%</p>
                              </div>
                              <div>
                                <span className="text-[10px] text-muted-foreground uppercase font-bold tracking-wider">Severity Classification</span>
                                <span className={`inline-block text-[10px] font-bold px-2 py-0.5 rounded mt-1 ${
                                  diseaseResult.severity === 'LOW' ? 'bg-green-100 text-green-800' :
                                  diseaseResult.severity === 'MEDIUM' || diseaseResult.severity === 'MODERATE' ? 'bg-amber-100 text-amber-800' :
                                  'bg-red-100 text-red-800'
                                }`}>{diseaseResult.severity}</span>
                              </div>
                            </div>
                            <div>
                              <span className="text-[10px] text-muted-foreground uppercase font-bold tracking-wider">Mitigation & Treatments</span>
                              <p className="text-xs text-on-surface-variant leading-relaxed mt-1 bg-white p-4 rounded-2xl border border-border whitespace-pre-line max-h-48 overflow-y-auto">
                                {diseaseResult.recommendedAction}
                              </p>
                            </div>
                          </div>
                        </div>
                      )}
                    </div>

                    {/* History Sidebar */}
                    <div className="bg-white p-6 rounded-[32px] border border-border shadow-sm h-fit">
                      <h3 className="font-semibold text-base mb-4 flex items-center gap-2" style={{ fontFamily: 'Fraunces', color: '#125106' }}>
                        <FileSearch size={18} /> Diagnostic Log
                      </h3>
                      <div className="space-y-4 max-h-[500px] overflow-y-auto pr-2">
                        {diseaseHistory.length > 0 ? (
                          diseaseHistory.map((h, i) => (
                            <div key={i} className="p-3 bg-[#f0eee3]/40 rounded-2xl space-y-1.5 border border-border/45">
                              <div className="flex justify-between items-start">
                                <span className="font-bold text-xs line-clamp-1">{h.detectedDisease}</span>
                                <span className="text-[10px] text-muted-foreground">{new Date(h.createdAt).toLocaleDateString()}</span>
                              </div>
                              <div className="flex justify-between text-[10px] text-muted-foreground">
                                <span>Crop: {h.cropName}</span>
                                <span>Score: {(h.confidenceScore * 100).toFixed(0)}%</span>
                              </div>
                            </div>
                          ))
                        ) : (
                          <p className="text-xs text-muted-foreground text-center py-6">No previous logs found for this farm.</p>
                        )}
                      </div>
                    </div>
                  </div>
                </>
              ) : (
                <div className="bg-white rounded-[32px] border border-border p-12 text-center max-w-md mx-auto shadow-sm">
                  <div className="w-16 h-16 bg-[#f0eee3] rounded-full flex items-center justify-center text-[#125106] mx-auto mb-6">
                    <ShieldAlert size={24} />
                  </div>
                  <h3 className="font-bold text-lg mb-2" style={{ fontFamily: 'Fraunces' }}>No Farm Selected</h3>
                  <p className="text-sm text-muted-foreground mb-6">Please add or select a farm to upload crop leaf images for AI diagnostics.</p>
                  <button
                    onClick={() => setActiveTab('farms')}
                    className="bg-[#125106] text-white px-6 py-3 rounded-full font-semibold text-sm hover:bg-[#2e6b20] transition-all shadow-sm"
                  >
                    Go to Farms Hub
                  </button>
                </div>
              )}
            </div>
          )}

          {/* TAB 6: GOVT SCHEMES */}
          {activeTab === 'schemes' && (
            <div className="space-y-8 max-w-[1200px]">
              <div className="relative rounded-[32px] overflow-hidden p-8 text-white min-h-[160px] flex flex-col justify-end shadow-md" style={{ background: 'linear-gradient(to top, rgba(18,81,6,0.9) 0%, rgba(18,81,6,0.3) 100%)' }}>
                <img
                  src="https://images.unsplash.com/photo-1542838132-92c53300491e?w=1200&h=400&fit=crop&auto=format"
                  alt="Government Schemes"
                  className="absolute inset-0 w-full h-full object-cover -z-10 opacity-45"
                />
                <div>
                  <span className="text-xs font-bold uppercase tracking-[0.2em] text-[#aff498] mb-1 block">Government Subsidy Finder</span>
                  <h3 className="font-bold text-2xl" style={{ fontFamily: 'Fraunces' }}>Government Subsidy Matcher</h3>
                  <p className="text-white/80 text-xs max-w-xl leading-relaxed mt-1" style={{ fontFamily: 'Inter' }}>
                    Eligibility rules parsed dynamically based on land holdings, district locations, and income parameters.
                  </p>
                </div>
              </div>

              {loadingSchemes ? (
                <div className="flex justify-center py-20">
                  <div className="w-12 h-12 border-4 border-primary/20 border-t-primary rounded-full animate-spin" />
                </div>
              ) : schemes.length > 0 ? (
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                  {schemes.map(sch => (
                    <div key={sch.id} className="bg-white p-6 rounded-[28px] border border-border shadow-sm flex flex-col justify-between hover:border-primary/20 transition-all">
                      <div>
                        <div className="flex justify-between items-start gap-4 mb-3">
                          <h4 className="font-bold text-lg text-primary">{sch.scheme.schemeName}</h4>
                          <span className={`text-[10px] font-bold px-2 py-0.5 rounded capitalize ${
                            sch.status === 'APPLIED' ? 'bg-green-100 text-green-800' :
                            sch.status === 'DISMISSED' ? 'bg-red-100 text-red-800' :
                            'bg-amber-100 text-amber-800'
                          }`}>{sch.status}</span>
                        </div>
                        <p className="text-xs text-on-surface-variant mb-4 leading-relaxed line-clamp-3">{sch.scheme.description}</p>
                        
                        {/* Eligibility Reasons */}
                        <div className="mb-4">
                          <span className="text-[10px] font-bold text-muted-foreground uppercase block mb-1">Matching Criteria Met:</span>
                          <div className="flex flex-wrap gap-1.5">
                            {sch.matchReasons.map((reason, idx) => (
                              <span key={idx} className="bg-secondary/40 text-on-surface-variant text-[9px] px-2 py-0.5 rounded-md font-medium">
                                ✓ {reason}
                              </span>
                            ))}
                          </div>
                        </div>

                        <div className="text-xs border-t border-border pt-3 space-y-1 text-muted-foreground mb-6">
                          <div>Benefits: <span className="font-semibold text-on-surface">{sch.scheme.benefits}</span></div>
                          <div>Match Suitability: <span className="font-semibold text-primary">{(sch.matchScore * 100).toFixed(0)}%</span></div>
                        </div>
                      </div>

                      <div className="flex gap-2">
                        {sch.scheme.applicationUrl && (
                          <a
                            href={sch.scheme.applicationUrl}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="flex-1 bg-secondary text-primary font-bold text-xs py-2.5 rounded-xl text-center hover:bg-primary hover:text-white transition-all flex items-center justify-center gap-1"
                          >
                            Apply Portal <ChevronRight size={12} />
                          </a>
                        )}
                        {sch.status !== 'APPLIED' && (
                          <button
                            onClick={() => handleUpdateSchemeStatus(sch.id, 'APPLIED')}
                            className="bg-primary text-white font-bold text-xs px-4 py-2.5 rounded-xl"
                          >
                            Mark Applied
                          </button>
                        )}
                        {sch.status !== 'DISMISSED' && (
                          <button
                            onClick={() => handleUpdateSchemeStatus(sch.id, 'DISMISSED')}
                            className="border border-border text-destructive font-bold text-xs px-4 py-2.5 rounded-xl hover:bg-red-50 hover:border-red-200"
                          >
                            Dismiss
                          </button>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-sm text-muted-foreground text-center py-10">No active government subsidies found matched to your profile settings.</p>
              )}
            </div>
          )}

          {/* TAB 7: TRANSLATION */}
          {activeTab === 'translate' && (
            <div className="space-y-8 max-w-[800px] mx-auto">
              <div className="bg-white p-8 rounded-[32px] border border-border shadow-sm">
                <h3 className="font-bold text-xl mb-2 flex items-center gap-2" style={{ fontFamily: 'Fraunces' }}>
                  <Languages size={20} className="text-primary" /> Agricultural Translation Hub
                </h3>
                <p className="text-sm text-muted-foreground mb-6">Semantic mapping engine powered by AI4Bharat IndicTrans2 model. Translates local reports or text.</p>

                <form onSubmit={handleTranslate} className="space-y-6">
                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-1">
                      <label className="text-xs font-bold text-muted-foreground">From Language</label>
                      <select
                        value={translateSrc}
                        onChange={e => setTranslateSrc(e.target.value)}
                        className="w-full bg-secondary text-sm border-none rounded-xl px-3 py-2.5 focus:ring-1 focus:ring-primary outline-none"
                      >
                        <option value="en">English</option>
                        <option value="hi">Hindi (हिन्दी)</option>
                        <option value="mr">Marathi (मराठी)</option>
                        <option value="te">Telugu (తెలుగు)</option>
                        <option value="kn">Kannada (ಕನ್ನಡ)</option>
                      </select>
                    </div>
                    <div className="space-y-1">
                      <label className="text-xs font-bold text-muted-foreground">To Language</label>
                      <select
                        value={translateTgt}
                        onChange={e => setTranslateTgt(e.target.value)}
                        className="w-full bg-secondary text-sm border-none rounded-xl px-3 py-2.5 focus:ring-1 focus:ring-primary outline-none"
                      >
                        <option value="hi">Hindi (हिन्दी)</option>
                        <option value="en">English</option>
                        <option value="mr">Marathi (मराठी)</option>
                        <option value="te">Telugu (తెలుగు)</option>
                        <option value="kn">Kannada (ಕನ್ನಡ)</option>
                      </select>
                    </div>
                  </div>

                  <div className="space-y-1">
                    <label className="text-xs font-bold text-muted-foreground">Source Agricultural Text</label>
                    <textarea
                      rows={4}
                      value={translateText}
                      onChange={e => setTranslateText(e.target.value)}
                      placeholder="Write kheti text, crop report details, or local mandi questions..."
                      required
                      className="w-full bg-secondary text-sm border-none rounded-2xl px-4 py-3.5 focus:ring-1 focus:ring-primary outline-none resize-none"
                    />
                  </div>

                  <button
                    type="submit"
                    disabled={translating || !translateText.trim()}
                    className="w-full bg-primary text-white py-4 rounded-full font-bold text-sm hover:opacity-90 active:scale-95 transition-all disabled:opacity-50"
                  >
                    {translating ? 'Translating via IndicTrans2...' : 'Translate'}
                  </button>
                </form>

                {translatedText && (
                  <div className="mt-8 p-6 bg-secondary/15 rounded-[28px] border border-primary/10 relative">
                    <div className="absolute top-4 right-4 flex gap-2">
                      <button
                        onClick={handlePlayTranslation}
                        disabled={playingTranslation}
                        className="w-10 h-10 bg-white hover:bg-secondary border border-border rounded-xl flex items-center justify-center text-primary transition-all disabled:opacity-50"
                        title="Play Speech (Indic-TTS)"
                      >
                        <Volume2 size={16} className={playingTranslation ? 'animate-pulse' : ''} />
                      </button>
                    </div>
                    <span className="text-[10px] text-muted-foreground uppercase font-bold tracking-wider">Translated Output</span>
                    <p className="text-base font-medium text-on-surface mt-2 pr-12 leading-relaxed">{translatedText}</p>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* TAB 8: ANALYTICS */}
          {activeTab === 'analytics' && (
            <div className="space-y-8 max-w-[1200px]">
              {analytics ? (
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                  {/* General Stats Cards */}
                  <div className="lg:col-span-3 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                    {[
                      { title: 'Total Farms', val: analytics.totalFarms, desc: 'registered locations' },
                      { title: 'AI Recommendations', val: analytics.totalRecommendations, desc: 'generated advisory logs' },
                      { title: 'Accepted Suggestions', val: analytics.acceptedRecommendations, desc: 'planned crop crops' },
                      { title: 'Crop Diseases Diagnosed', val: analytics.totalDiseasesDetected, desc: 'alerts identified' }
                    ].map(card => (
                      <div key={card.title} className="bg-white p-6 rounded-[28px] border border-border shadow-sm">
                        <span className="text-xs text-muted-foreground font-semibold block">{card.title}</span>
                        <h4 className="text-4xl font-extrabold text-primary mt-2">{card.val}</h4>
                        <span className="text-[10px] text-muted-foreground block mt-1">{card.desc}</span>
                      </div>
                    ))}
                  </div>

                  {/* Farms sizes Chart */}
                  <div className="lg:col-span-2 bg-white p-8 rounded-[32px] border border-border shadow-sm">
                    <h3 className="font-bold text-lg mb-6" style={{ fontFamily: 'Fraunces' }}>Farm Sizing Breakdown (Hectares)</h3>
                    {farms.length > 0 ? (
                      <div className="h-64">
                        <ResponsiveContainer width="100%" height="100%">
                          <BarChart data={farms.map(f => ({ name: f.farmName, area: f.areaHectares }))}>
                            <CartesianGrid strokeDasharray="3 3" vertical={false} />
                            <XAxis dataKey="name" tick={{ fontSize: 11 }} />
                            <YAxis tick={{ fontSize: 11 }} />
                            <Tooltip />
                            <Bar dataKey="area" fill="#2c6a14" name="Area (Ha)" radius={[4, 4, 0, 0]} />
                          </BarChart>
                        </ResponsiveContainer>
                      </div>
                    ) : (
                      <p className="text-sm text-muted-foreground text-center py-10">Add farmlands to show layout sizes.</p>
                    )}
                  </div>

                  {/* Soil acidity pH gauge chart mapping */}
                  <div className="bg-white p-8 rounded-[32px] border border-border shadow-sm">
                    <h3 className="font-bold text-lg mb-6" style={{ fontFamily: 'Fraunces' }}>Soil Acidity (pH)</h3>
                    {farms.length > 0 ? (
                      <div className="h-64 flex flex-col justify-between">
                        <div className="space-y-4 pr-2">
                          {farms.map(f => (
                            <div key={f.id} className="space-y-1">
                              <div className="flex justify-between text-xs font-semibold">
                                <span className="text-on-surface">{f.farmName}</span>
                                <span className="text-primary font-bold">{f.soilPh ? f.soilPh.toFixed(1) : 'N/A'} pH</span>
                              </div>
                              <div className="h-2 w-full bg-secondary rounded-full overflow-hidden relative">
                                {/* Color scales: 0-7 acid (red/orange), 7 neutral (green), 7-14 alkaline (blue) */}
                                <div
                                  className="h-full bg-primary"
                                  style={{
                                    width: `${((f.soilPh || 7) / 14) * 100}%`,
                                    background: (f.soilPh || 7) < 6.5 ? '#ba1a1a' : (f.soilPh || 7) > 7.5 ? '#3b82f6' : '#125106'
                                  }}
                                />
                              </div>
                            </div>
                          ))}
                        </div>
                        <div className="flex justify-between text-[9px] text-muted-foreground border-t border-border pt-4 mt-4">
                          <span>Acidic (&lt;6.5)</span>
                          <span>Neutral (7.0)</span>
                          <span>Alkaline (&gt;7.5)</span>
                        </div>
                      </div>
                    ) : (
                      <p className="text-sm text-muted-foreground text-center py-10">No pH indices logged.</p>
                    )}
                  </div>
                </div>
              ) : (
                <p className="text-sm text-muted-foreground text-center py-10">Compiling farm statistics...</p>
              )}
            </div>
          )}

        </div>
      </main>

      {/* Add Farm Dialog Modal */}
      {showAddFarmModal && (
        <div className="fixed inset-0 bg-black/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-[36px] border border-border shadow-2xl p-8 max-w-lg w-full max-h-[90vh] overflow-y-auto space-y-6">
            <div className="flex justify-between items-start">
              <div>
                <h3 className="font-bold text-xl text-primary" style={{ fontFamily: 'Fraunces' }}>Add New Farm Profile</h3>
                <p className="text-xs text-muted-foreground">Input parameters needed by the ML models.</p>
              </div>
              <button
                onClick={() => setShowAddFarmModal(false)}
                className="w-8 h-8 rounded-full border border-border flex items-center justify-center text-muted-foreground hover:bg-secondary"
              >
                <X size={14} />
              </button>
            </div>

            <form onSubmit={handleAddFarm} className="space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="space-y-1">
                  <label className="text-[10px] font-bold text-muted-foreground uppercase">Farm Title</label>
                  <input
                    type="text"
                    placeholder="e.g. Uttara Field"
                    value={newFarm.farmName}
                    onChange={e => setNewFarm({ ...newFarm, farmName: e.target.value })}
                    required
                    className="w-full bg-secondary text-xs border-none rounded-xl px-3 py-2.5 focus:ring-1 focus:ring-primary outline-none"
                  />
                </div>
                <div className="space-y-1">
                  <label className="text-[10px] font-bold text-muted-foreground uppercase">Area Size (Hectares)</label>
                  <input
                    type="number"
                    step="0.01"
                    placeholder="e.g. 1.5"
                    value={newFarm.areaHectares}
                    onChange={e => setNewFarm({ ...newFarm, areaHectares: e.target.value })}
                    required
                    className="w-full bg-secondary text-xs border-none rounded-xl px-3 py-2.5 focus:ring-1 focus:ring-primary outline-none"
                  />
                </div>
                <div className="space-y-1">
                  <label className="text-[10px] font-bold text-muted-foreground uppercase">Latitude (Decimal)</label>
                  <input
                    type="number"
                    step="0.0001"
                    placeholder="e.g. 26.9124"
                    value={newFarm.latitude}
                    onChange={e => setNewFarm({ ...newFarm, latitude: e.target.value })}
                    className="w-full bg-secondary text-xs border-none rounded-xl px-3 py-2.5 focus:ring-1 focus:ring-primary outline-none"
                  />
                </div>
                <div className="space-y-1">
                  <label className="text-[10px] font-bold text-muted-foreground uppercase">Longitude (Decimal)</label>
                  <input
                    type="number"
                    step="0.0001"
                    placeholder="e.g. 75.7873"
                    value={newFarm.longitude}
                    onChange={e => setNewFarm({ ...newFarm, longitude: e.target.value })}
                    className="w-full bg-secondary text-xs border-none rounded-xl px-3 py-2.5 focus:ring-1 focus:ring-primary outline-none"
                  />
                </div>
                <div className="space-y-1">
                  <label className="text-[10px] font-bold text-muted-foreground uppercase">Soil Classification</label>
                  <select
                    value={newFarm.soilType}
                    onChange={e => setNewFarm({ ...newFarm, soilType: e.target.value })}
                    className="w-full bg-secondary text-xs border-none rounded-xl px-3 py-2.5 focus:ring-1 focus:ring-primary outline-none cursor-pointer"
                  >
                    <option value="ALLUVIAL">Alluvial (निक्षेप)</option>
                    <option value="BLACK">Black Cotton (काली मिट्टी)</option>
                    <option value="RED">Red Soil (लाल मिट्टी)</option>
                    <option value="LATERITE">Laterite Soil</option>
                    <option value="DESERT">Desert Soil</option>
                    <option value="MOUNTAIN">Mountain Soil</option>
                  </select>
                </div>
                <div className="space-y-1">
                  <label className="text-[10px] font-bold text-muted-foreground uppercase">Irrigation Framework</label>
                  <select
                    value={newFarm.irrigationType}
                    onChange={e => setNewFarm({ ...newFarm, irrigationType: e.target.value })}
                    className="w-full bg-secondary text-xs border-none rounded-xl px-3 py-2.5 focus:ring-1 focus:ring-primary outline-none cursor-pointer"
                  >
                    <option value="CANAL">Canal Irrigation (नहर)</option>
                    <option value="WELL">Well Irrigation (कुआं)</option>
                    <option value="TUBE_WELL">Tube-Well (नलकूप)</option>
                    <option value="DRIP">Drip Irrigation (टपक सिंचाई)</option>
                    <option value="SPRINKLER">Sprinkler (फव्वारा)</option>
                    <option value="RAIN_FED">Rain Fed (वर्षा-आधारित)</option>
                  </select>
                </div>
                <div className="space-y-1">
                  <label className="text-[10px] font-bold text-muted-foreground uppercase">Soil Acidity (pH)</label>
                  <input
                    type="number"
                    step="0.1"
                    placeholder="e.g. 6.8"
                    value={newFarm.soilPh}
                    onChange={e => setNewFarm({ ...newFarm, soilPh: e.target.value })}
                    className="w-full bg-secondary text-xs border-none rounded-xl px-3 py-2.5 focus:ring-1 focus:ring-primary outline-none"
                  />
                </div>
              </div>

              {/* Chemical specs */}
              <div className="bg-muted p-4 rounded-2xl space-y-3">
                <span className="text-[10px] font-bold text-muted-foreground uppercase tracking-wide block">NPK Chemical Spec (Optional, highly recommended for ML Model)</span>
                <div className="grid grid-cols-3 gap-2">
                  <div className="space-y-0.5">
                    <label className="text-[9px] font-bold text-muted-foreground block">Nitrogen (N)</label>
                    <input
                      type="number"
                      placeholder="mg/kg"
                      value={newFarm.nitrogenKgHa}
                      onChange={e => setNewFarm({ ...newFarm, nitrogenKgHa: e.target.value })}
                      className="w-full bg-white text-xs border border-border rounded-lg px-2 py-1.5 focus:ring-1 focus:ring-primary outline-none"
                    />
                  </div>
                  <div className="space-y-0.5">
                    <label className="text-[9px] font-bold text-muted-foreground block">Phosphorus (P)</label>
                    <input
                      type="number"
                      placeholder="mg/kg"
                      value={newFarm.phosphorusKgHa}
                      onChange={e => setNewFarm({ ...newFarm, phosphorusKgHa: e.target.value })}
                      className="w-full bg-white text-xs border border-border rounded-lg px-2 py-1.5 focus:ring-1 focus:ring-primary outline-none"
                    />
                  </div>
                  <div className="space-y-0.5">
                    <label className="text-[9px] font-bold text-muted-foreground block">Potassium (K)</label>
                    <input
                      type="number"
                      placeholder="mg/kg"
                      value={newFarm.potassiumKgHa}
                      onChange={e => setNewFarm({ ...newFarm, potassiumKgHa: e.target.value })}
                      className="w-full bg-white text-xs border border-border rounded-lg px-2 py-1.5 focus:ring-1 focus:ring-primary outline-none"
                    />
                  </div>
                </div>
              </div>

              <button
                type="submit"
                className="w-full bg-primary text-white py-3 rounded-full font-bold text-xs hover:opacity-90 active:scale-95 transition-all shadow-sm"
              >
                Register Farm Profile
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
