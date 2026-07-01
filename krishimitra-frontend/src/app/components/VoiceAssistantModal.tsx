import { useState, useEffect, useRef, useCallback } from 'react';
import { Mic, MicOff, X, Volume2, VolumeX, Send, Cpu, User, Info } from 'lucide-react';
import { api } from '../services/api';

interface VoiceAssistantModalProps {
  isOpen: boolean;
  onClose: () => void;
  farmerId: string;
}

interface ChatMessage {
  id: string;
  sender: 'user' | 'assistant';
  text: string;
  advisoryType?: string;
  timestamp: Date;
}

// Language code → BCP-47 for Web Speech API
const LANG_BCP47: Record<string, string> = {
  hi: 'hi-IN',
  mr: 'mr-IN',
  te: 'te-IN',
  kn: 'kn-IN',
  gu: 'gu-IN',
  pa: 'pa-IN',
  ta: 'ta-IN',
  en: 'en-IN',
};

const GREETINGS: Record<string, string> = {
  hi: 'नमस्ते! मैं कृषिमित्र हूँ। मैं आपकी कैसे मदद कर सकता हूँ?',
  mr: 'नमस्कार! मी कृषि मित्र आहे. मी तुम्हाला कशी मदत करू?',
  te: 'నమస్కారం! నేను కృషిమిత్రను. నేను మీకు ఎలా సహాయపడగలను?',
  kn: 'ನಮಸ್ಕಾರ! ನಾನು ಕೃಷಿಮಿತ್ರ. ನಾನು ನಿಮಗೆ ಹೇಗೆ ಸಹಾಯ ಮಾಡಬಲ್ಲೆ?',
  gu: 'નમસ્કાર! હું કૃષિમિત્ર છું. આજે હું તમારી કેવી રીતે મદદ કરી શકું?',
  pa: 'ਸਤ ਸ੍ਰੀ ਅਕਾਲ! ਮੈਂ ਕ੃਷ਿਮਿਤ੍ਰ ਹਾਂ। ਅੱਜ ਮੈਂ ਤੁਹਾਡੀ ਕੀ ਮਦਦ ਕਰ ਸਕਦਾ ਹਾਂ?',
  ta: 'வணக்கம்! நான் கிரிஷிமித்ரா. இன்று நான் உங்களுக்கு எப்படி உதவ முடியும்?',
  en: 'Hello! I am KrishiMitra. How can I help you today?',
};

// Fallback chain: if no TTS voice is found for a language, try these alternatives.
// e.g. Marathi → Hindi (same Devanagari script), Gujarati → Hindi, etc.
const VOICE_FALLBACK_CHAIN: Record<string, string[]> = {
  mr: ['hi', 'en'],
  gu: ['hi', 'en'],
  pa: ['hi', 'en'],
  kn: ['te', 'en'],
  te: ['kn', 'en'],
  ta: ['en'],
  hi: ['en'],
  en: [],
};

// Browser-native SpeechRecognition (with vendor prefixes)
const SpeechRecognitionAPI: any =
  typeof window !== 'undefined'
    ? (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition
    : null;

export default function VoiceAssistantModal({ isOpen, onClose, farmerId }: VoiceAssistantModalProps) {
  const [language, setLanguage] = useState('hi');
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [inputText, setInputText] = useState('');
  const [recording, setRecording] = useState(false);
  const [loading, setLoading] = useState(false);
  const [isSpeaking, setIsSpeaking] = useState<string | null>(null);
  const [interimText, setInterimText] = useState('');
  const [browserSTTSupported] = useState(() => !!SpeechRecognitionAPI);
  const [voices, setVoices] = useState<SpeechSynthesisVoice[]>([]);

  const recognitionRef = useRef<any>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const hasGreetedRef = useRef<boolean>(false);
  const utteranceRef = useRef<SpeechSynthesisUtterance | null>(null);
  const audioRef = useRef<HTMLAudioElement | null>(null);
  const accumulatedTranscriptRef = useRef<string>('');
  const interimTextRef = useRef<string>('');

  // Load and listen to browser speech voices (since they load asynchronously)
  useEffect(() => {
    if (typeof window !== 'undefined' && window.speechSynthesis) {
      const updateVoices = () => {
        setVoices(window.speechSynthesis.getVoices());
      };
      updateVoices();
      window.speechSynthesis.addEventListener('voiceschanged', updateVoices);
      return () => {
        window.speechSynthesis.removeEventListener('voiceschanged', updateVoices);
      };
    }
  }, []);

  // ── Helper: add assistant message ──────────────────────────────────────────
  const addAssistantMessage = useCallback((text: string, id?: string, advisoryType?: string) => {
    const msgId = id ?? Date.now().toString() + '-msg';
    setMessages(prev => [
      ...prev,
      { id: msgId, sender: 'assistant', text, advisoryType, timestamp: new Date() },
    ]);
  }, []);

  // ── Helper: find the best TTS voice with fallback chain ────────────────────
  const findBestVoice = useCallback((targetLang: string, availableVoices: SpeechSynthesisVoice[]): { voice: SpeechSynthesisVoice | null; lang: string } => {
    // Build the list of languages to try: target first, then fallbacks
    const langsToTry = [targetLang, ...(VOICE_FALLBACK_CHAIN[targetLang] || ['en'])];

    for (const lang of langsToTry) {
      const bcp = LANG_BCP47[lang] ?? `${lang}-IN`;
      const langPrefix = bcp.split('-')[0];
      const langVoices = availableVoices.filter(v =>
        v.lang.startsWith(langPrefix) || v.lang.replace('_', '-').startsWith(langPrefix)
      );

      if (langVoices.length > 0) {
        // Prefer female/warm voices for a friendly feel
        const best = langVoices.find(v => {
          const n = v.name.toLowerCase();
          return n.includes('female') || n.includes('woman') || n.includes('zira') ||
                 n.includes('heera') || n.includes('kalpana') || n.includes('hemant');
        })
        || langVoices.find(v => v.name.includes('Google') || v.name.includes('Microsoft'))
        || langVoices[0];

        return { voice: best, lang: bcp };
      }
    }
    return { voice: null, lang: LANG_BCP47[targetLang] ?? 'hi-IN' };
  }, []);

  const fallbackToBrowserTTS = useCallback((cleanText: string, msgId: string) => {
    if (!window.speechSynthesis) {
      setIsSpeaking(null);
      return;
    }

    const availableVoices = voices.length > 0 ? voices : window.speechSynthesis.getVoices();
    const { voice: bestVoice, lang: bestLang } = findBestVoice(language, availableVoices);

    const utterance = new SpeechSynthesisUtterance(cleanText);
    utterance.lang = bestLang;
    if (bestVoice) utterance.voice = bestVoice;

    // Natural, clear modulation — slightly slower for regional languages to ensure clarity
    const isEnglish = language === 'en';
    utterance.rate = isEnglish ? 0.95 : 0.92;   // perfectly balanced speed (not too robotic/slow, not too fast)
    utterance.pitch = 1.05;                       // slightly higher pitch for warm, friendly tone
    utterance.volume = 1.0;                       // full volume

    utterance.onend = () => setIsSpeaking(null);
    utterance.onerror = () => setIsSpeaking(null);
    utteranceRef.current = utterance;
    window.speechSynthesis.speak(utterance);
  }, [language, voices, findBestVoice]);

  // ── High-Quality Backend TTS with Browser Fallback ─────────────────────────
  const speakText = useCallback(async (text: string, msgId: string) => {
    // Stop any currently playing audio
    window.speechSynthesis?.cancel();
    if (audioRef.current) {
      audioRef.current.pause();
      audioRef.current = null;
    }
    setIsSpeaking(msgId);

    // Clean text: remove markdown, emojis, special chars for cleaner speech
    const cleanText = text
      .replace(/\*\*/g, '')           // bold markdown
      .replace(/[🌾🌤️🐛📋🙏]/g, '') // emoji clutter
      .replace(/\n{2,}/g, '. ')       // paragraph breaks → pauses
      .replace(/\n/g, ', ')           // line breaks → commas for natural pause
      .replace(/\s{2,}/g, ' ')        // extra whitespace
      .trim();

    try {
      // 1. Try backend high-quality native TTS (AI4Bharat Indic-TTS)
      const audioBlob = await api.synthesizeVoice(cleanText, language);
      const audioUrl = URL.createObjectURL(audioBlob);
      const audio = new Audio(audioUrl);
      audioRef.current = audio;
      
      audio.onended = () => setIsSpeaking(null);
      audio.onerror = () => {
        setIsSpeaking(null);
        fallbackToBrowserTTS(cleanText, msgId);
      };
      
      await audio.play();
    } catch (err) {
      console.warn("Backend TTS failed, falling back to browser TTS", err);
      fallbackToBrowserTTS(cleanText, msgId);
    }
  }, [language, fallbackToBrowserTTS]);

  const stopSpeaking = useCallback(() => {
    window.speechSynthesis?.cancel();
    if (audioRef.current) {
      audioRef.current.pause();
      audioRef.current = null;
    }
    setIsSpeaking(null);
  }, []);

  // ── Backend Query ────────────────────────────────────────────────────────────
  const queryAdvisory = useCallback(async (queryText: string) => {
    setLoading(true);
    try {
      // Send history (excluding the current query which is sent separately)
      // Exclude greeting messages if they don't add value, but Gemini can handle them.
      const history = messages.map(m => ({
        role: m.sender === 'assistant' ? 'model' : 'user',
        text: m.text
      }));

      const data = await api.textChat(queryText, language, farmerId, history);
      const assistantMsgId = Date.now().toString() + '-res';
      const responseText: string =
        data?.responseText || 'माफ करें, उत्तर नहीं मिला। / Sorry, no response generated.';

      addAssistantMessage(responseText, assistantMsgId, data?.advisoryType);
      speakText(responseText, assistantMsgId);
    } catch (err: any) {
      console.error('queryAdvisory error:', err);
      addAssistantMessage('⚠️ Could not get advisory. Check your connection and try again.');
    } finally {
      setLoading(false);
    }
  }, [language, farmerId, messages, addAssistantMessage, speakText]);

  // ── Speech Recognition ──────────────────────────────────────────────────────
  const stopRecording = useCallback(() => {
    if (recognitionRef.current) {
      try { recognitionRef.current.stop(); } catch (_) { /* ignore */ }
      recognitionRef.current = null;
    }
    setRecording(false);
    
    // Combine accumulated and any pending interim text
    const finalTextToSend = (accumulatedTranscriptRef.current + ' ' + interimTextRef.current).trim();
    
    setInterimText('');
    accumulatedTranscriptRef.current = '';
    interimTextRef.current = '';

    if (finalTextToSend) {
      const userMsgId = Date.now().toString();
      setMessages(prev => [
        ...prev,
        { id: userMsgId, sender: 'user', text: finalTextToSend, timestamp: new Date() },
      ]);
      queryAdvisory(finalTextToSend);
    }
  }, [queryAdvisory]);

  const startRecording = useCallback(() => {
    if (!SpeechRecognitionAPI) {
      addAssistantMessage('⚠️ Your browser does not support speech recognition. Please use Chrome or Edge, or type your question below.');
      return;
    }

    // Cancel any running speech output before listening
    window.speechSynthesis?.cancel();
    if (audioRef.current) {
      audioRef.current.pause();
      audioRef.current = null;
    }
    setIsSpeaking(null);

    const recognition = new SpeechRecognitionAPI();
    recognition.lang = LANG_BCP47[language] ?? 'hi-IN';
    recognition.interimResults = true;
    recognition.maxAlternatives = 1;
    recognition.continuous = true; // Stay open so user can pause without being cut off!

    recognition.onstart = () => {
      setRecording(true);
      setInterimText('');
      accumulatedTranscriptRef.current = '';
      interimTextRef.current = '';
    };

    recognition.onresult = (event: any) => {
      let interim = '';
      let finalForThisEvent = '';
      for (let i = event.resultIndex; i < event.results.length; i++) {
        const transcript = event.results[i][0].transcript;
        if (event.results[i].isFinal) {
          finalForThisEvent += transcript + ' ';
        } else {
          interim += transcript;
        }
      }
      
      if (finalForThisEvent) {
        accumulatedTranscriptRef.current += finalForThisEvent;
      }
      interimTextRef.current = interim;
      
      // Update UI state so user sees what they are saying
      setInterimText((accumulatedTranscriptRef.current + ' ' + interim).trim());
    };

    recognition.onerror = (event: any) => {
      console.error('SpeechRecognition error', event.error);
      setRecording(false);
      setInterimText('');
      accumulatedTranscriptRef.current = '';
      interimTextRef.current = '';
      if (event.error !== 'aborted') {
        const msg =
          event.error === 'not-allowed'
            ? 'Microphone access denied. Please allow microphone in browser settings.'
            : `Speech recognition error: ${event.error}`;
        addAssistantMessage('⚠️ ' + msg);
      }
    };

    recognition.onend = () => {
      // If recognitionRef.current is still set, it means the browser stopped listening 
      // automatically (e.g. due to a long silence timeout). In this case, we act as 
      // if the user clicked stop so we don't lose their accumulated text.
      if (recognitionRef.current) {
        stopRecording();
      }
    };

    recognitionRef.current = recognition;
    recognition.start();
  }, [language, queryAdvisory, addAssistantMessage]);

  // ── Text Input Fallback ──────────────────────────────────────────────────────
  const handleTextSubmit = useCallback(async (e: React.FormEvent) => {
    e.preventDefault();
    const text = inputText.trim();
    if (!text) return;
    setInputText('');

    const userMsgId = Date.now().toString();
    setMessages(prev => [
      ...prev,
      { id: userMsgId, sender: 'user', text, timestamp: new Date() },
    ]);
    await queryAdvisory(text);
  }, [inputText, queryAdvisory]);

  // ── Auto-scroll ──────────────────────────────────────────────────────────────
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, loading, interimText]);

  // ── Cleanup on close ─────────────────────────────────────────────────────────
  useEffect(() => {
    if (!isOpen) {
      stopRecording();
      window.speechSynthesis?.cancel();
      setIsSpeaking(null);
      hasGreetedRef.current = false;
    }
  }, [isOpen, stopRecording]);

  // ── Initial Greeting when Modal Opens ────────────────────────────────────────
  useEffect(() => {
    if (isOpen && !hasGreetedRef.current) {
      hasGreetedRef.current = true;
      setMessages([]);
      setInputText('');
      setInterimText('');
      setRecording(false);

      const greetingMsg = GREETINGS[language] || GREETINGS['en'];
      const msgId = Date.now().toString() + '-greeting';

      setMessages([{
        id: msgId,
        sender: 'assistant',
        text: greetingMsg,
        timestamp: new Date()
      }]);

      // Speak the greeting with high-quality TTS, then auto-start listening
      speakText(greetingMsg, msgId).then(() => {
        // If we want to auto-listen after greeting, we can hook it into audio onended
        // But for simplicity with async API, let's just let user press mic.
        // Actually, we can hook it into `audio.onended` by passing a callback, 
        // but it's cleaner to let `speakText` just speak and user presses mic.
      });
    }
  }, [isOpen, language, browserSTTSupported, startRecording, speakText]);

  // ── Early return AFTER all hooks ─────────────────────────────────────────────
  if (!isOpen) return null;

  // ── Render ───────────────────────────────────────────────────────────────────
  return (
    <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-end sm:items-center justify-center p-0 sm:p-4">
      <div
        className="bg-white rounded-t-[36px] sm:rounded-[36px] border border-border shadow-2xl w-full max-w-2xl flex flex-col overflow-hidden"
        style={{ height: 'min(88vh, 680px)' }}
      >
        {/* Header */}
        <header className="px-5 py-4 border-b border-border flex justify-between items-center bg-[#f9f7ee] flex-shrink-0">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-[#125106] flex items-center justify-center text-white shadow-sm">
              <Mic size={18} />
            </div>
            <div>
              <h3 className="font-bold text-base text-[#125106]" style={{ fontFamily: 'Fraunces' }}>
                KrishiMitra Voice Advisor
              </h3>
              <p className="text-[11px] text-muted-foreground">
                {browserSTTSupported
                  ? 'Click 🎤 and speak — I listen in your language'
                  : '⚠️ Use Chrome/Edge for voice input. Type below as fallback.'}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <select
              value={language}
              onChange={e => setLanguage(e.target.value)}
              className="bg-white border border-border rounded-xl text-xs font-bold px-3 py-1.5 focus:ring-1 focus:ring-[#125106] text-[#125106] outline-none cursor-pointer"
            >
              <option value="hi">Hindi (हिन्दी)</option>
              <option value="mr">Marathi (मराठी)</option>
              <option value="te">Telugu (తెలుగు)</option>
              <option value="kn">Kannada (ಕನ್ನಡ)</option>
              <option value="gu">Gujarati (ગુજરાતી)</option>
              <option value="pa">Punjabi (ਪੰਜਾਬੀ)</option>
              <option value="ta">Tamil (தமிழ்)</option>
              <option value="en">English</option>
            </select>

            <button
              onClick={onClose}
              className="w-8 h-8 rounded-full border border-border flex items-center justify-center text-muted-foreground hover:bg-secondary transition-all"
            >
              <X size={14} />
            </button>
          </div>
        </header>

        {/* TTS Info Banner */}
        {language !== 'en' && language !== 'hi' && (
          <div className="bg-blue-50 border-b border-blue-100 px-5 py-2 flex gap-2 items-start text-[11px] text-blue-700 flex-shrink-0">
            <Info size={14} className="flex-shrink-0 mt-0.5 text-blue-500" />
            <p>
              <strong>Tip for best voice quality:</strong> Windows may not have a native voice for this language (falling back to Hindi/English). 
              For native voices, try using <strong>Google Chrome</strong> or install the <strong>Windows Language Pack</strong> for {
                language === 'mr' ? 'Marathi' :
                language === 'te' ? 'Telugu' :
                language === 'kn' ? 'Kannada' :
                language === 'gu' ? 'Gujarati' :
                language === 'pa' ? 'Punjabi' :
                language === 'ta' ? 'Tamil' : 'this language'
              } (Settings → Time & Language).
            </p>
          </div>
        )}

        {/* Conversation */}
        <div className="flex-1 overflow-y-auto p-5 space-y-4 bg-[#fcfaef]/60">
          {messages.length === 0 && !interimText && (
            <div className="h-full flex flex-col items-center justify-center text-center max-w-sm mx-auto space-y-4 py-8">
              <div
                className="w-20 h-20 bg-[#125106] rounded-full flex items-center justify-center text-white shadow-lg"
                style={{ animation: 'pulse 2.5s ease-in-out infinite' }}
              >
                <Mic size={32} />
              </div>
              <h4 className="font-bold text-xl text-[#125106]" style={{ fontFamily: 'Fraunces' }}>
                बोलिए — I'm Listening
              </h4>
              <p className="text-sm text-muted-foreground leading-relaxed">
                Press the mic button and ask in your language:<br />
                <em className="font-semibold text-[#125106]">"गेहूं में रोग कैसे रोकें?"</em>
                &nbsp;or&nbsp;
                <em className="font-semibold text-[#125106]">"PM Kisan scheme kya hai?"</em>
              </p>
            </div>
          )}

          {messages.map(msg => (
            <div
              key={msg.id}
              className={`flex gap-3 ${msg.sender === 'user' ? 'ml-auto flex-row-reverse max-w-[85%]' : 'mr-auto max-w-[88%]'}`}
            >
              <div
                className={`w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0 text-xs ${
                  msg.sender === 'user' ? 'bg-[#125106] text-white' : 'bg-[#e8f5e2] text-[#125106]'
                }`}
              >
                {msg.sender === 'user' ? <User size={14} /> : <Cpu size={14} />}
              </div>

              <div
                className={`p-4 rounded-3xl ${
                  msg.sender === 'user'
                    ? 'bg-[#125106] text-white rounded-tr-none'
                    : 'bg-white border border-border rounded-tl-none shadow-sm'
                }`}
              >
                <p className="text-sm leading-relaxed whitespace-pre-wrap">
                  {msg.text.replace(/\*\*/g, '').replace(/^\*\s+/gm, '• ')}
                </p>

                {msg.advisoryType && (
                  <span className="inline-block mt-2 text-[9px] font-bold uppercase tracking-wider bg-[#125106]/10 text-[#125106] px-2 py-0.5 rounded">
                    {msg.advisoryType.replace('_', ' ')}
                  </span>
                )}

                {msg.sender === 'assistant' && (
                  <div className="mt-2 flex justify-end">
                    {isSpeaking === msg.id ? (
                      <button
                        onClick={stopSpeaking}
                        className="flex items-center gap-1 bg-[#125106] text-white text-[10px] font-bold px-3 py-1.5 rounded-xl hover:opacity-90 active:scale-95 transition-all"
                      >
                        <VolumeX size={11} /> Stop
                      </button>
                    ) : (
                      <button
                        onClick={() => speakText(msg.text, msg.id)}
                        className="flex items-center gap-1 bg-[#e8f5e2] text-[#125106] text-[10px] font-bold px-3 py-1.5 rounded-xl hover:bg-[#125106] hover:text-white transition-all"
                      >
                        <Volume2 size={11} /> Play
                      </button>
                    )}
                  </div>
                )}
              </div>
            </div>
          ))}

          {/* Interim transcript preview */}
          {interimText && (
            <div className="ml-auto flex gap-3 flex-row-reverse max-w-[85%]">
              <div className="w-8 h-8 rounded-full bg-[#125106]/30 text-[#125106] flex items-center justify-center flex-shrink-0 text-xs">
                <User size={14} />
              </div>
              <div className="p-4 rounded-3xl bg-[#125106]/10 border border-[#125106]/20 rounded-tr-none">
                <p className="text-sm text-[#125106] italic">{interimText}…</p>
              </div>
            </div>
          )}

          {loading && (
            <div className="mr-auto flex gap-3 max-w-[80%]">
              <div className="w-8 h-8 rounded-full bg-[#e8f5e2] text-[#125106] flex items-center justify-center text-xs">
                <Cpu size={14} />
              </div>
              <div className="bg-white border border-border p-4 rounded-3xl rounded-tl-none shadow-sm flex items-center gap-2">
                <span className="w-2 h-2 rounded-full bg-[#125106]/40 animate-bounce" style={{ animationDelay: '0ms' }} />
                <span className="w-2 h-2 rounded-full bg-[#125106]/70 animate-bounce" style={{ animationDelay: '150ms' }} />
                <span className="w-2 h-2 rounded-full bg-[#125106] animate-bounce" style={{ animationDelay: '300ms' }} />
                <span className="text-xs text-muted-foreground ml-1">Generating advice…</span>
              </div>
            </div>
          )}
          <div ref={messagesEndRef} />
        </div>

        {/* Footer */}
        <footer className="p-4 border-t border-border bg-white flex flex-col items-center gap-3 flex-shrink-0">
          {/* Recording status bar */}
          {recording && (
            <div className="flex items-center gap-2 text-xs font-bold text-[#125106]">
              <span className="w-2.5 h-2.5 rounded-full bg-red-500 animate-pulse" />
              Speak now… (Click Stop when finished)
              {interimText && (
                <span className="text-muted-foreground font-normal ml-1 truncate max-w-[200px]" title={interimText}>
                  "{interimText}"
                </span>
              )}
            </div>
          )}

          <div className="w-full flex items-center gap-3">
            {/* Mic toggle button */}
            {recording ? (
              <button
                onClick={stopRecording}
                className="w-14 h-14 rounded-full bg-red-500 flex items-center justify-center text-white hover:opacity-90 active:scale-95 transition-all shadow-md flex-shrink-0"
                title="Stop recording"
              >
                <MicOff size={22} />
              </button>
            ) : (
              <button
                onClick={startRecording}
                disabled={loading || !browserSTTSupported}
                className="w-14 h-14 rounded-full bg-[#125106] flex items-center justify-center text-white hover:bg-[#1e6b14] active:scale-95 transition-all shadow-md shadow-green-900/20 disabled:opacity-50 flex-shrink-0"
                title={browserSTTSupported ? 'Start speaking' : 'Not supported in this browser'}
              >
                <Mic size={22} />
              </button>
            )}

            {/* Text input fallback */}
            <form onSubmit={handleTextSubmit} className="flex-1 flex gap-2">
              <input
                type="text"
                id="voice-text-input"
                placeholder="Or type your question here…"
                value={inputText}
                onChange={e => setInputText(e.target.value)}
                disabled={recording || loading}
                className="flex-1 bg-[#f3f1e8] text-sm border-none rounded-full px-5 py-3 focus:ring-2 focus:ring-[#125106]/30 outline-none"
              />
              <button
                type="submit"
                disabled={recording || loading || !inputText.trim()}
                className="bg-[#125106] text-white w-12 h-12 rounded-full flex items-center justify-center hover:opacity-90 active:scale-95 transition-all disabled:opacity-40 flex-shrink-0"
              >
                <Send size={16} />
              </button>
            </form>
          </div>
        </footer>
      </div>
    </div>
  );
}
