import { useState, useEffect, useRef, useCallback } from 'react';
import { Mic, MicOff, X, Volume2, VolumeX, Send, Cpu, User } from 'lucide-react';
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
  en: 'en-IN',
};

const GREETINGS: Record<string, string> = {
  hi: 'नमस्ते! मैं कृषिमित्र हूँ। मैं आपकी कैसे मदद कर सकता हूँ?',
  mr: 'नमस्कार! मी कृषि मित्र आहे. मी तुमची कशी मदत करू शकतो?',
  te: 'నమస్కారం! నేను కృషిమిత్రను. నేను మీకు ఎలా సహాయపడగలను?',
  kn: 'ನಮಸ್ಕಾರ! ನಾನು ಕೃಷಿಮಿತ್ರ. ನಾನು ನಿಮಗೆ ಹೇಗೆ ಸಹಾಯ ಮಾಡಬಲ್ಲೆ?',
  en: 'Hello! I am KrishiMitra. How can I help you today?',
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

  // ── Browser TTS with improved voice selection & modulation ────────────────
  const speakText = useCallback((text: string, msgId: string) => {
    if (!window.speechSynthesis) return;
    window.speechSynthesis.cancel();

    // Clean text: remove markdown, emojis, special chars for cleaner speech
    const cleanText = text
      .replace(/\*\*/g, '')           // bold markdown
      .replace(/[🌾🌤️🐛📋🙏]/g, '') // emoji clutter
      .replace(/\n{2,}/g, '. ')       // paragraph breaks → pauses
      .replace(/\n/g, ', ')           // line breaks → commas for natural pause
      .replace(/\s{2,}/g, ' ')        // extra whitespace
      .trim();

    const bcp47 = LANG_BCP47[language] ?? 'hi-IN';

    // Select best matching voice for the language from available voices
    const availableVoices = voices.length > 0 ? voices : window.speechSynthesis.getVoices();
    let bestVoice: SpeechSynthesisVoice | null = null;

    // Prefer female/warm voices for a friendly feel
    const langVoices = availableVoices.filter(v => v.lang.startsWith(bcp47.split('-')[0]) || v.lang.replace('_', '-').startsWith(bcp47.split('-')[0]));
    bestVoice = langVoices.find(v => v.name.toLowerCase().includes('female') || v.name.toLowerCase().includes('woman') || v.name.toLowerCase().includes('zira') || v.name.toLowerCase().includes('heera') || v.name.toLowerCase().includes('kalpana')) 
             || langVoices.find(v => v.name.includes('Google') || v.name.includes('Microsoft'))
             || langVoices[0]
             || null;

    const utterance = new SpeechSynthesisUtterance(cleanText);
    utterance.lang = bcp47;
    if (bestVoice) utterance.voice = bestVoice;

    // Natural, clear modulation — slightly slower for regional languages to ensure clarity
    const isEnglish = language === 'en';
    utterance.rate = isEnglish ? 0.95 : 0.92;   // perfectly balanced speed (not too robotic/slow, not too fast)
    utterance.pitch = 1.05;                       // slightly higher pitch for warm, friendly tone
    utterance.volume = 1.0;                       // full volume

    setIsSpeaking(msgId);
    utterance.onend = () => setIsSpeaking(null);
    utterance.onerror = () => setIsSpeaking(null);
    utteranceRef.current = utterance;
    window.speechSynthesis.speak(utterance);
  }, [language, voices]);

  const stopSpeaking = useCallback(() => {
    window.speechSynthesis?.cancel();
    setIsSpeaking(null);
  }, []);

  // ── Backend Query ────────────────────────────────────────────────────────────
  const queryAdvisory = useCallback(async (queryText: string) => {
    setLoading(true);
    try {
      const data = await api.textChat(queryText, language, farmerId);
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
  }, [language, farmerId, addAssistantMessage, speakText]);

  // ── Speech Recognition ──────────────────────────────────────────────────────
  const stopRecording = useCallback(() => {
    if (recognitionRef.current) {
      try { recognitionRef.current.stop(); } catch (_) { /* ignore */ }
      recognitionRef.current = null;
    }
    setRecording(false);
    setInterimText('');
  }, []);

  const startRecording = useCallback(() => {
    if (!SpeechRecognitionAPI) {
      addAssistantMessage('⚠️ Your browser does not support speech recognition. Please use Chrome or Edge, or type your question below.');
      return;
    }

    // Cancel any running speech output before listening
    window.speechSynthesis?.cancel();
    setIsSpeaking(null);

    const recognition = new SpeechRecognitionAPI();
    recognition.lang = LANG_BCP47[language] ?? 'hi-IN';
    recognition.interimResults = true;
    recognition.maxAlternatives = 1;
    recognition.continuous = false;

    recognition.onstart = () => {
      setRecording(true);
      setInterimText('');
    };

    recognition.onresult = (event: any) => {
      let interim = '';
      let finalText = '';
      for (let i = event.resultIndex; i < event.results.length; i++) {
        const transcript = event.results[i][0].transcript;
        if (event.results[i].isFinal) {
          finalText += transcript;
        } else {
          interim += transcript;
        }
      }
      setInterimText(interim || finalText);
      if (finalText) {
        setInterimText('');
        // Add user message
        const userMsgId = Date.now().toString();
        setMessages(prev => [
          ...prev,
          { id: userMsgId, sender: 'user', text: finalText, timestamp: new Date() },
        ]);
        queryAdvisory(finalText);
      }
    };

    recognition.onerror = (event: any) => {
      console.error('SpeechRecognition error', event.error);
      setRecording(false);
      setInterimText('');
      if (event.error !== 'aborted') {
        const msg =
          event.error === 'not-allowed'
            ? 'Microphone access denied. Please allow microphone in browser settings.'
            : `Speech recognition error: ${event.error}`;
        addAssistantMessage('⚠️ ' + msg);
      }
    };

    recognition.onend = () => {
      setRecording(false);
      setInterimText('');
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

      // Speak the greeting with improved voice selection, then auto-start listening
      if (window.speechSynthesis) {
        window.speechSynthesis.cancel();
        const bcp47 = LANG_BCP47[language] ?? 'hi-IN';
        const utterance = new SpeechSynthesisUtterance(greetingMsg);
        utterance.lang = bcp47;

        // Select best voice (same logic as speakText)
        const availableVoices = voices.length > 0 ? voices : window.speechSynthesis.getVoices();
        const langVoices = availableVoices.filter(v => v.lang.startsWith(bcp47.split('-')[0]) || v.lang.replace('_', '-').startsWith(bcp47.split('-')[0]));
        const bestVoice = langVoices.find(v => v.name.toLowerCase().includes('female') || v.name.toLowerCase().includes('woman') || v.name.toLowerCase().includes('zira') || v.name.toLowerCase().includes('heera') || v.name.toLowerCase().includes('kalpana'))
                       || langVoices.find(v => v.name.includes('Google') || v.name.includes('Microsoft'))
                       || langVoices[0]
                       || null;
        if (bestVoice) utterance.voice = bestVoice;

        const isEnglish = language === 'en';
        utterance.rate = isEnglish ? 0.95 : 0.92;
        utterance.pitch = 1.05;
        utterance.volume = 1.0;

        setIsSpeaking(msgId);

        utterance.onend = () => {
          setIsSpeaking(null);
          if (browserSTTSupported) {
            startRecording();
          }
        };

        utterance.onerror = () => {
          setIsSpeaking(null);
          if (browserSTTSupported) {
            startRecording();
          }
        };

        utteranceRef.current = utterance;
        setTimeout(() => {
          window.speechSynthesis.speak(utterance);
        }, 400);
      }
    }
  }, [isOpen, language, browserSTTSupported, startRecording, voices]);

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
                <p className="text-sm leading-relaxed whitespace-pre-wrap">{msg.text}</p>

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
              Listening… speak now in {LANG_BCP47[language]}
              {interimText && (
                <span className="text-muted-foreground font-normal ml-1 truncate max-w-[200px]">
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
