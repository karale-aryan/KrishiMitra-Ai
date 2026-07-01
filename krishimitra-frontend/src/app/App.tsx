import { useState, useEffect, useRef } from "react";
import { motion } from "motion/react";
import { api, FarmerResponse, setTokens, getTokens, clearTokens } from "./services/api";
import DashboardHub from "./components/DashboardHub";
import VoiceAssistantModal from "./components/VoiceAssistantModal";
import { Phone, Lock, User, Languages as LanguagesIcon } from "lucide-react";

// ─── Logo SVG (based on the circular leaf+wheat emblem) ───────────────────
function KrishiMitraLogo({ size = 40 }: { size?: number }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 120 120"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      aria-label="KrishiMitra AI logo"
    >
      <defs>
        <radialGradient id="circleGrad" cx="50%" cy="50%" r="50%">
          <stop offset="0%" stopColor="#aff498" />
          <stop offset="100%" stopColor="#125106" />
        </radialGradient>
        <linearGradient id="leafGrad" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stopColor="#5ab537" />
          <stop offset="100%" stopColor="#125106" />
        </linearGradient>
        <linearGradient id="wheatGrad" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stopColor="#94d77e" />
          <stop offset="100%" stopColor="#2e6b20" />
        </linearGradient>
      </defs>
      {/* Outer circle ring */}
      <circle cx="60" cy="60" r="56" stroke="url(#circleGrad)" strokeWidth="3" fill="none" opacity="0.6" />
      <circle cx="60" cy="60" r="50" stroke="#2e6b20" strokeWidth="1.5" fill="none" opacity="0.3" />
      {/* Background fill — subtle */}
      <circle cx="60" cy="60" r="53" fill="#f6fff2" opacity="0.5" />
      {/* Large leaf (left, curving) */}
      <path
        d="M60 90 C 30 75, 22 50, 38 30 C 42 25, 52 22, 60 28 C 52 40, 46 58, 60 90Z"
        fill="url(#leafGrad)"
        opacity="0.9"
      />
      {/* Large leaf (right, curving) */}
      <path
        d="M60 90 C 88 75, 96 52, 82 32 C 78 26, 68 24, 60 30 C 68 42, 74 60, 60 90Z"
        fill="url(#leafGrad)"
        opacity="0.75"
      />
      {/* Center stem */}
      <path d="M60 90 L60 28" stroke="#125106" strokeWidth="2" strokeLinecap="round" opacity="0.5" />
      {/* Wheat stalk — center top */}
      <path d="M60 20 L60 46" stroke="url(#wheatGrad)" strokeWidth="2.5" strokeLinecap="round" />
      {/* Wheat grains */}
      <ellipse cx="60" cy="17" rx="5" ry="8" fill="url(#wheatGrad)" transform="rotate(0 60 17)" />
      <ellipse cx="54" cy="23" rx="4" ry="7" fill="url(#wheatGrad)" transform="rotate(-25 54 23)" opacity="0.85" />
      <ellipse cx="66" cy="23" rx="4" ry="7" fill="url(#wheatGrad)" transform="rotate(25 66 23)" opacity="0.85" />
      <ellipse cx="50" cy="30" rx="3.5" ry="6" fill="url(#wheatGrad)" transform="rotate(-40 50 30)" opacity="0.7" />
      <ellipse cx="70" cy="30" rx="3.5" ry="6" fill="url(#wheatGrad)" transform="rotate(40 70 30)" opacity="0.7" />
      {/* Small decorative leaf tip */}
      <path d="M60 90 C 55 80, 50 70, 56 62" stroke="#5ab537" strokeWidth="1.5" strokeLinecap="round" fill="none" opacity="0.6" />
      <path d="M60 90 C 65 80, 70 70, 64 62" stroke="#5ab537" strokeWidth="1.5" strokeLinecap="round" fill="none" opacity="0.6" />
    </svg>
  );
}

// ─── Scroll reveal hook ────────────────────────────────────────────────────
function useReveal(threshold = 0.15) {
  const ref = useRef<HTMLDivElement>(null);
  const [visible, setVisible] = useState(false);
  useEffect(() => {
    const el = ref.current;
    if (!el) return;
    const obs = new IntersectionObserver(
      ([entry]) => { if (entry.isIntersecting) { setVisible(true); obs.disconnect(); } },
      { threshold, rootMargin: "0px 0px -50px 0px" }
    );
    obs.observe(el);
    return () => obs.disconnect();
  }, [threshold]);
  return { ref, visible };
}

function RevealUp({ children, delay = 0, className = "" }: { children: React.ReactNode; delay?: number; className?: string }) {
  const { ref, visible } = useReveal();
  return (
    <div
      ref={ref}
      className={className}
      style={{
        opacity: visible ? 1 : 0,
        transform: visible ? "translateY(0)" : "translateY(30px)",
        transition: `all 0.8s cubic-bezier(0.16,1,0.3,1) ${delay}s`,
      }}
    >
      {children}
    </div>
  );
}

// ─── Animated counter ──────────────────────────────────────────────────────
function Counter({ end, suffix = "" }: { end: number | string; suffix?: string }) {
  const [val, setVal] = useState(0);
  const { ref, visible } = useReveal(0.5);
  const started = useRef(false);
  useEffect(() => {
    if (!visible || started.current || typeof end !== "number") return;
    started.current = true;
    let n = 0;
    const step = end / 50;
    const id = setInterval(() => {
      n += step;
      if (n >= end) { setVal(end); clearInterval(id); } else setVal(Math.floor(n));
    }, 30);
    return () => clearInterval(id);
  }, [visible, end]);
  return (
    <span ref={ref}>
      {typeof end === "number" ? val.toLocaleString() : end}{suffix}
    </span>
  );
}

// ─── Wave bars animation ───────────────────────────────────────────────────
function WaveBars({ color = "#125106" }: { color?: string }) {
  const delays = [0, 0.15, 0.3, 0.15, 0.3, 0.45, 0.2];
  return (
    <div className="flex items-center gap-1 h-8">
      {delays.map((d, i) => (
        <div
          key={i}
          style={{
            width: 4,
            borderRadius: 2,
            backgroundColor: color,
            animationDelay: `${d}s`,
            animationDuration: "1.2s",
            animationIterationCount: "infinite",
            animationTimingFunction: "ease-in-out",
            animationName: "wave",
          }}
          className="wave-bar"
        />
      ))}
    </div>
  );
}

// ─── Nav ───────────────────────────────────────────────────────────────────
function Nav({ onOpenApp }: { onOpenApp: () => void }) {
  const [scrolled, setScrolled] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  useEffect(() => {
    const h = () => setScrolled(window.scrollY > 20);
    window.addEventListener("scroll", h, { passive: true });
    return () => window.removeEventListener("scroll", h);
  }, []);

  const links = [
    { label: "Features", href: "#features" },
    { label: "How it works", href: "#how-it-works" },
    { label: "Languages", href: "#languages" },
    { label: "For teams", href: "#teams" },
  ];

  return (
    <header
      className="fixed top-0 w-full z-50 h-20 transition-all duration-300"
      style={{
        background: scrolled ? "rgba(252,250,239,0.92)" : "rgba(252,250,239,0.6)",
        backdropFilter: "blur(12px)",
        borderBottom: `1px solid ${scrolled ? "#c1c9b9" : "transparent"}`,
      }}
    >
      <div className="max-w-[1200px] mx-auto h-full flex justify-between items-center px-5 md:px-10">
        <a href="#" className="flex items-center gap-2.5 no-underline">
          <KrishiMitraLogo size={44} />
          <span
            className="text-[22px] font-bold leading-none tracking-tight"
            style={{ fontFamily: "Fraunces", color: "#125106" }}
          >
            KrishiMitra <span style={{ color: "#2e6b20" }}>AI</span>
          </span>
        </a>

        <nav className="hidden lg:flex items-center gap-10">
          {links.map((l) => (
            <a
              key={l.label}
              href={l.href}
              className="text-sm font-semibold tracking-wide transition-colors"
              style={{ fontFamily: "Inter", color: "#41493d" }}
              onMouseEnter={e => (e.currentTarget.style.color = "#125106")}
              onMouseLeave={e => (e.currentTarget.style.color = "#41493d")}
            >
              {l.label}
            </a>
          ))}
        </nav>

        <div className="flex items-center gap-3">
          <button
            onClick={onOpenApp}
            className="hidden sm:block px-8 py-2.5 rounded-full text-sm font-semibold transition-all active:scale-95 shadow-sm"
            style={{ background: "#125106", color: "#fff", fontFamily: "Inter" }}
            onMouseEnter={e => (e.currentTarget.style.background = "#2e6b20")}
            onMouseLeave={e => (e.currentTarget.style.background = "#125106")}
          >
            Open the app
          </button>
          <button
            className="lg:hidden p-2 rounded-lg"
            onClick={() => setMobileOpen(!mobileOpen)}
            style={{ color: "#125106" }}
          >
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              {mobileOpen
                ? <path d="M18 6L6 18M6 6l12 12" />
                : <><path d="M3 12h18" /><path d="M3 6h18" /><path d="M3 18h18" /></>}
            </svg>
          </button>
        </div>
      </div>

      {mobileOpen && (
        <div className="lg:hidden border-t px-5 py-4 flex flex-col gap-3" style={{ background: "#fcfaef", borderColor: "#c1c9b9" }}>
          {links.map(l => (
            <a key={l.label} href={l.href} className="py-2 text-sm font-semibold" style={{ color: "#41493d", fontFamily: "Inter" }} onClick={() => setMobileOpen(false)}>
              {l.label}
            </a>
          ))}
          <button onClick={onOpenApp} className="mt-2 py-3 rounded-full text-sm font-semibold text-white" style={{ background: "#125106", fontFamily: "Inter" }}>
            Open the app
          </button>
        </div>
      )}
    </header>
  );
}

// ─── Hero ──────────────────────────────────────────────────────────────────
function Hero({ onOpenApp, onStartTalking }: { onOpenApp: () => void; onStartTalking: () => void }) {
  const [listening, setListening] = useState(false);

  return (
    <section className="relative pt-20 pb-16 md:pb-24 overflow-hidden" style={{ background: "#fcfaef" }}>
      {/* Subtle background circle ornament */}
      <div
        className="absolute -top-40 -right-40 w-[600px] h-[600px] rounded-full pointer-events-none opacity-30"
        style={{ background: "radial-gradient(circle, #aff49820 0%, transparent 70%)" }}
      />

      <div className="max-w-[1200px] mx-auto px-5 md:px-10 grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
        {/* Left copy */}
        <div className="lg:col-span-6 z-10">
          <motion.div
            initial={{ opacity: 0, y: 16 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6 }}
            className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full mb-6 text-xs font-semibold tracking-wide"
            style={{ background: "#d9f5d0", color: "#125106", fontFamily: "Inter" }}
          >
            <span className="relative flex h-2 w-2">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full opacity-75" style={{ background: "#125106" }} />
              <span className="relative inline-flex h-2 w-2 rounded-full" style={{ background: "#125106" }} />
            </span>
            Empowering 100M+ Farmers
          </motion.div>

          <motion.h1
            initial={{ opacity: 0, y: 24 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.7, delay: 0.1 }}
            className="leading-tight mb-8 text-[2.6rem] md:text-[3.2rem]"
            style={{ fontFamily: "Fraunces", fontWeight: 700, color: "#1b1c16", letterSpacing: "-0.02em" }}
          >
            Your AI kheti advisor —{" "}
            <br />
            <em className="not-italic" style={{ color: "#125106", fontStyle: "italic" }}>just ask</em>, in your own language.
          </motion.h1>

          <motion.p
            initial={{ opacity: 0, y: 16 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.2 }}
            className="text-lg leading-relaxed mb-10 max-w-lg"
            style={{ fontFamily: "Inter", color: "#41493d" }}
          >
            Speak naturally to get instant, expert advice on crop health, soil quality, and government schemes. No typing, just talking.
          </motion.p>

          <motion.div
            initial={{ opacity: 0, y: 16 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.3 }}
            className="flex flex-wrap gap-4 mb-12"
          >
            <button
              onClick={onStartTalking}
              className="px-10 py-4 rounded-full font-semibold text-lg text-white transition-all hover:-translate-y-0.5 active:scale-95"
              style={{ background: "#125106", fontFamily: "Inter", boxShadow: "0 8px 24px rgba(18,81,6,0.15)" }}
              onMouseEnter={e => (e.currentTarget.style.boxShadow = "0 12px 32px rgba(18,81,6,0.25)")}
              onMouseLeave={e => (e.currentTarget.style.boxShadow = "0 8px 24px rgba(18,81,6,0.15)")}
            >
              Start Talking Now
            </button>
            <button
              onClick={onOpenApp}
              className="px-10 py-4 rounded-full font-semibold text-lg transition-all"
              style={{ border: "1.5px solid #c1c9b9", color: "#125106", fontFamily: "Inter", background: "transparent" }}
              onMouseEnter={e => (e.currentTarget.style.background = "#f6f4e9")}
              onMouseLeave={e => (e.currentTarget.style.background = "transparent")}
            >
              Open Advisor App
            </button>
          </motion.div>

          {/* Trust signals */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.6, delay: 0.45 }}
            className="flex items-center gap-8 pt-8"
            style={{ borderTop: "1px solid #c1c9b9" }}
          >
            {[
              { val: "14+", label: "Indian Languages" },
              { val: "98%", label: "Voice Accuracy" },
              { val: "24/7", label: "Support Access" },
            ].map((s, i) => (
              <div key={s.label} className="flex items-center gap-8">
                {i > 0 && <div className="w-px h-10" style={{ background: "#c1c9b9" }} />}
                <div className="flex flex-col">
                  <span className="text-2xl font-bold" style={{ fontFamily: "Fraunces", color: "#125106" }}>{s.val}</span>
                  <span className="text-xs" style={{ fontFamily: "Inter", color: "#717a6b" }}>{s.label}</span>
                </div>
              </div>
            ))}
          </motion.div>
        </div>

        {/* Right image + voice UI */}
        <motion.div
          initial={{ opacity: 0, x: 30 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.8, delay: 0.2 }}
          className="lg:col-span-6 relative"
        >
          <div className="relative rounded-[40px] overflow-hidden border-[6px] border-white shadow-2xl aspect-[4/3] group" style={{ boxShadow: "0 32px 80px rgba(18,81,6,0.12)" }}>
            <img
              src="https://images.unsplash.com/photo-1592982537447-7440770cbfc9?w=900&h=675&fit=crop&auto=format"
              alt="Indian farmer working in field"
              className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-105"
            />
            <div className="absolute inset-0" style={{ background: "linear-gradient(to top, rgba(18,81,6,0.45) 0%, transparent 60%)" }} />

            {/* Floating voice UI card */}
            <div
              className="absolute bottom-7 right-6 left-6 p-5 rounded-3xl"
              style={{
                background: "rgba(252,250,239,0.97)",
                backdropFilter: "blur(12px)",
                border: "1px solid rgba(255,255,255,0.5)",
                boxShadow: "0 8px 32px rgba(0,0,0,0.12)",
              }}
            >
              <div className="flex items-center gap-3 mb-3">
                <button
                  onClick={() => setListening(l => !l)}
                  className="w-11 h-11 rounded-full flex items-center justify-center text-white flex-shrink-0 transition-transform active:scale-90"
                  style={{ background: "#125106" }}
                >
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round">
                    <path d="M12 2a3 3 0 0 0-3 3v7a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3z" />
                    <path d="M19 10v2a7 7 0 0 1-14 0v-2" />
                    <line x1="12" y1="19" x2="12" y2="22" />
                  </svg>
                </button>
                <div>
                  <p className="text-xs font-bold uppercase tracking-widest mb-0.5" style={{ color: "#125106", fontFamily: "Inter" }}>
                    {listening ? "Listening..." : "Tap to speak"}
                  </p>
                  <p className="text-sm font-medium" style={{ color: "#1b1c16", fontFamily: "Inter" }}>
                    "How do I treat yellow rust in wheat?"
                  </p>
                </div>
              </div>
              {listening && <WaveBars color="#125106" />}
              {!listening && (
                <div className="flex items-center gap-1 h-8">
                  {[0.6, 0.3, 0.5, 0.2, 0.4, 0.2, 0.3].map((o, i) => (
                    <div key={i} style={{ width: 4, height: 8 + i % 3 * 6, borderRadius: 2, background: `rgba(18,81,6,${o})` }} />
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Floating badge */}
          <motion.div
            animate={{ y: [0, -6, 0] }}
            transition={{ duration: 3, repeat: Infinity, ease: "easeInOut" }}
            className="absolute -top-4 -left-4 px-4 py-2.5 rounded-2xl shadow-lg flex items-center gap-2"
            style={{ background: "#fff", border: "1px solid #c1c9b9", fontFamily: "Inter" }}
          >
            <div className="w-7 h-7 rounded-full flex items-center justify-center" style={{ background: "#d9f5d0" }}>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#125106" strokeWidth="2.5"><polyline points="20 6 9 17 4 12" /></svg>
            </div>
            <span className="text-xs font-semibold" style={{ color: "#1b1c16" }}>Disease detected</span>
          </motion.div>
        </motion.div>
      </div>
    </section>
  );
}

// ─── Trust strip ───────────────────────────────────────────────────────────
function TrustStrip() {
  const orgs = ["NITI Aayog", "ICAR", "Digital India", "Bhashini", "PM-Kisan"];
  return (
    <section className="py-10 overflow-hidden border-y" style={{ background: "#f6f4e9", borderColor: "#c1c9b9" }}>
      <div className="max-w-[1200px] mx-auto px-10">
        <p className="text-center text-xs font-semibold uppercase tracking-[0.2em] mb-7" style={{ color: "#717a6b", fontFamily: "Inter" }}>
          Trusted by Leading Agricultural Organizations
        </p>
        <div className="flex flex-wrap justify-center items-center gap-10 md:gap-16 opacity-50">
          {orgs.map((o) => (
            <span key={o} className="text-base font-bold tracking-tight" style={{ fontFamily: "Fraunces", color: "#125106" }}>{o}</span>
          ))}
        </div>
      </div>
    </section>
  );
}

// ─── Features ─────────────────────────────────────────────────────────────
const features = [
  {
    icon: <path d="M12 2a3 3 0 0 0-3 3v7a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3z" />,
    icon2: <><path d="M19 10v2a7 7 0 0 1-14 0v-2" /><line x1="12" y1="19" x2="12" y2="22" /></>,
    title: "AI Voice Assistant",
    desc: "Talk to the app like a human advisor. Voice-to-text models trained on rural dialects ensure your intent is understood perfectly.",
  },
  {
    icon: <path d="M12 2a10 10 0 1 1 0 20M12 2v10l5 3" />,
    icon2: null,
    title: "Crop Recommendation",
    desc: "Suggests optimal crops based on soil health and seasonal trends. Maximize your yield with data-driven planting choices.",
  },
  {
    icon: <><path d="M17.5 19H9a7 7 0 1 1 6.71-9h1.79a4.5 4.5 0 1 1 0 9z" /><path d="M12 13l-2 4h4l-2 4" /></>,
    icon2: null,
    title: "Climate Risk Alerts",
    desc: "Hyper-local weather warnings powered by real-time satellite data. Protect your crops from sudden climatic shifts.",
  },
  {
    icon: <><rect x="3" y="3" width="18" height="18" rx="2" /><circle cx="8.5" cy="8.5" r="1.5" /><path d="m21 15-5-5L5 21" /></>,
    icon2: null,
    title: "Disease Detection",
    desc: "Identifies pests and diseases from simple photos. Computer vision trained on 10,000+ plant species for instant diagnosis.",
  },
  {
    icon: <><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" /><polyline points="14 2 14 8 20 8" /><line x1="8" y1="13" x2="16" y2="13" /></>,
    icon2: null,
    title: "Scheme Finder",
    desc: "Personalised government subsidy alerts. We map your profile to active portals to ensure no farmer is left behind.",
  },
  {
    icon: <><circle cx="12" cy="12" r="10" /><path d="M12 8v4l3 3" /><path d="M2 12h2M20 12h2M12 2v2M12 20v2" /></>,
    icon2: null,
    title: "Neural Translation",
    desc: "Bhashini-powered neural translation of technical reports and documents. Understand complex science in your native tongue.",
  },
];

function FeatureCard({ f, i }: { f: typeof features[0]; i: number }) {
  const [hovered, setHovered] = useState(false);
  return (
    <RevealUp delay={Math.floor(i / 3) * 0.05 + (i % 3) * 0.1}>
      <div
        className="p-10 rounded-[32px] border h-full flex flex-col transition-all duration-300 cursor-default"
        style={{
          background: "#fff",
          borderColor: hovered ? "rgba(18,81,6,0.3)" : "#e4e3d8",
          boxShadow: hovered ? "0 24px 60px rgba(18,81,6,0.06)" : "none",
          transform: hovered ? "translateY(-2px)" : "none",
        }}
        onMouseEnter={() => setHovered(true)}
        onMouseLeave={() => setHovered(false)}
      >
        <div
          className="w-16 h-16 rounded-2xl flex items-center justify-center mb-8 transition-transform duration-300"
          style={{ background: "rgba(175,244,152,0.2)", transform: hovered ? "scale(1.1)" : "scale(1)" }}
        >
          <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="#125106" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
            {f.icon}
            {f.icon2}
          </svg>
        </div>
        <h3 className="font-semibold text-2xl mb-4" style={{ fontFamily: "Fraunces", color: "#1b1c16" }}>{f.title}</h3>
        <p className="text-base leading-relaxed mb-8 flex-1" style={{ fontFamily: "Inter", color: "#41493d" }}>{f.desc}</p>
        <a href="#" className="inline-flex items-center gap-2 text-sm font-semibold group/link" style={{ color: "#125106", fontFamily: "Inter" }}>
          Learn more
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" className="transition-transform group-hover/link:translate-x-1">
            <path d="M5 12h14M12 5l7 7-7 7" />
          </svg>
        </a>
      </div>
    </RevealUp>
  );
}

function Features() {
  return (
    <section id="features" className="py-32 px-5 md:px-10 max-w-[1200px] mx-auto">
      <RevealUp className="text-center mb-20">
        <h2
          className="mb-6 text-[2rem] md:text-[2.4rem]"
          style={{ fontFamily: "Fraunces", fontWeight: 700, color: "#125106" }}
        >
          Grounded Intelligence for Farmers
        </h2>
        <p className="max-w-2xl mx-auto text-lg leading-relaxed" style={{ fontFamily: "Inter", color: "#41493d" }}>
          Empowering every hand that feeds the nation with real-time, expert agricultural advice through 6 core modules.
        </p>
      </RevealUp>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {features.map((f, i) => <FeatureCard key={f.title} f={f} i={i} />)}
      </div>
    </section>
  );
}

// ─── How It Works (dark) ───────────────────────────────────────────────────
const steps = [
  { n: "01", title: "Speak", desc: "Input your query naturally in your native tongue." },
  { n: "02", title: "Transcribe", desc: "High-fidelity transcription captures rural accents." },
  { n: "03", title: "Translate", desc: "Semantic mapping via Bhashini Engine.", highlight: true, badge: "Core Engine" },
  { n: "04", title: "Reason", desc: "AI model processes against agri-knowledge base." },
  { n: "05", title: "Speak back", desc: "Receives verified advice in your own voice." },
];

function HowItWorks() {
  return (
    <section id="how-it-works" className="py-32 relative overflow-hidden" style={{ background: "#30312a" }}>
      <div className="max-w-[1200px] mx-auto px-5 md:px-10 relative z-10">
        <RevealUp className="text-center mb-20">
          <h2 className="mb-6 text-[2.6rem]" style={{ fontFamily: "Fraunces", fontWeight: 700, color: "#aff498" }}>
            The Intelligence Chain
          </h2>
          <p className="text-lg max-w-2xl mx-auto opacity-70" style={{ fontFamily: "Inter", color: "#fcfaef" }}>
            Complex technology, simplified into a single seamless conversation for the end user.
          </p>
        </RevealUp>

        <div className="grid grid-cols-1 md:grid-cols-5 gap-5">
          {steps.map((s, i) => (
            <RevealUp key={s.n} delay={i * 0.1}>
              {s.highlight ? (
                <div
                  className="aspect-square p-8 rounded-3xl flex flex-col justify-between relative"
                  style={{ background: "#125106", border: "1px solid rgba(175,244,152,0.3)", boxShadow: "0 16px 40px rgba(18,81,6,0.3)" }}
                >
                  <span className="text-4xl font-bold opacity-100" style={{ fontFamily: "Fraunces", color: "#fff" }}>{s.n}</span>
                  <div>
                    <h4 className="text-xl font-bold mb-2 text-white" style={{ fontFamily: "Fraunces" }}>{s.title}</h4>
                    <p className="text-sm text-white/80" style={{ fontFamily: "Inter" }}>{s.desc}</p>
                  </div>
                  <div className="absolute -top-3 -right-3 px-2.5 py-1 rounded-lg text-[10px] font-bold uppercase tracking-wider" style={{ background: "#aff498", color: "#022100", fontFamily: "Inter" }}>
                    {s.badge}
                  </div>
                </div>
              ) : (
                <div
                  className="aspect-square p-8 rounded-3xl flex flex-col justify-between transition-all duration-300 cursor-default"
                  style={{ background: "rgba(255,255,255,0.05)", border: "1px solid rgba(255,255,255,0.1)" }}
                  onMouseEnter={e => { e.currentTarget.style.background = "rgba(18,81,6,0.2)"; e.currentTarget.style.borderColor = "rgba(175,244,152,0.3)"; }}
                  onMouseLeave={e => { e.currentTarget.style.background = "rgba(255,255,255,0.05)"; e.currentTarget.style.borderColor = "rgba(255,255,255,0.1)"; }}
                >
                  <span className="text-4xl font-bold opacity-40" style={{ fontFamily: "Fraunces", color: "#aff498" }}>{s.n}</span>
                  <div>
                    <h4 className="text-xl font-bold mb-2" style={{ fontFamily: "Fraunces", color: "#aff498" }}>{s.title}</h4>
                    <p className="text-sm opacity-60 text-white" style={{ fontFamily: "Inter" }}>{s.desc}</p>
                  </div>
                </div>
              )}
            </RevealUp>
          ))}
        </div>
      </div>
    </section>
  );
}

// ─── Languages ─────────────────────────────────────────────────────────────
const langs = ["हिन्दी", "मराठी", "తెలుగు", "ಕನ್ನಡ", "தமிழ்", "বাংলা", "ਪੰਜਾਬੀ", "ગુજરાતી", "മലയാളം", "ଓଡ଼ିଆ", "اردو", "English"];

function Languages() {
  const [active, setActive] = useState<string | null>(null);
  return (
    <section id="languages" className="py-32 overflow-hidden" style={{ background: "#fcfaef" }}>
      <div className="max-w-[1200px] mx-auto px-5 md:px-10 grid grid-cols-1 lg:grid-cols-2 gap-20 items-center">
        <RevealUp>
          <h2
            className="mb-8 text-[2.8rem] md:text-[3.2rem] leading-tight"
            style={{ fontFamily: "Fraunces", fontWeight: 700, color: "#1b1c16", letterSpacing: "-0.02em" }}
          >
            Breaking language barriers,{" "}
            <em style={{ fontStyle: "italic", color: "#125106" }}>one village at a time.</em>
          </h2>

          <div className="grid grid-cols-3 sm:grid-cols-4 gap-3 mb-10">
            {langs.map((l) => (
              <button
                key={l}
                onClick={() => setActive(active === l ? null : l)}
                className="px-3 py-2.5 rounded-xl text-center font-medium text-sm transition-all duration-200"
                style={{
                  fontFamily: "Inter",
                  background: active === l ? "#125106" : "#fff",
                  border: `1.5px solid ${active === l ? "#125106" : "#c1c9b9"}`,
                  color: active === l ? "#fff" : "#1b1c16",
                }}
              >
                {l}
              </button>
            ))}
          </div>

          <div className="p-7 rounded-3xl border" style={{ background: "#f0eee3", borderColor: "#c1c9b9" }}>
            <div className="flex items-center gap-3 mb-3">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#125106" strokeWidth="2"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" /><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" /></svg>
              <h4 className="text-xl font-bold" style={{ fontFamily: "Fraunces", color: "#125106" }}>Village Vocabulary</h4>
            </div>
            <p className="leading-relaxed" style={{ fontFamily: "Inter", color: "#41493d" }}>
              Our AI understands not just formal grammar, but the specific agricultural dialects used in mandis and fields across the country. From "Kharif" patterns to "Jeevamrut" practices, we speak your language.
            </p>
          </div>
        </RevealUp>

        <RevealUp delay={0.2}>
          <div className="relative rounded-[48px] overflow-hidden border aspect-[4/5] group" style={{ borderColor: "#c1c9b9" }}>
            <img
              src="https://images.unsplash.com/photo-1605000797499-95a51c5269ae?w=700&h=875&fit=crop&auto=format"
              alt="Farmers in village field"
              className="w-full h-full object-cover transition-transform duration-1000 group-hover:scale-105"
            />
            <div className="absolute inset-0" style={{ background: "linear-gradient(to top, rgba(0,0,0,0.75) 0%, rgba(0,0,0,0.15) 55%, transparent 100%)" }} />
            <div className="absolute bottom-10 left-10 right-10 text-white">
              <p className="text-3xl font-bold mb-2" style={{ fontFamily: "Fraunces" }}>Empowering 100M+</p>
              <p className="text-base leading-snug opacity-80" style={{ fontFamily: "Inter" }}>
                Rural livelihoods through local-first AI. Built for the grassroots, powered by world-class tech.
              </p>
            </div>
          </div>
        </RevealUp>
      </div>
    </section>
  );
}

// ─── Stats ─────────────────────────────────────────────────────────────────
function Stats() {
  const items = [
    { val: 100, suffix: "M+", label: "Farmers targeted", sub: "across 28 states" },
    { val: 14, suffix: "+", label: "Indian languages", sub: "including dialects" },
    { val: 98, suffix: "%", label: "Voice accuracy", sub: "on rural accents" },
    { val: 38, suffix: "", label: "Disease classes", sub: "detected by AI" },
    { val: 10, suffix: "+", label: "Govt schemes", sub: "auto-matched" },
    { val: 24, suffix: "/7", label: "Availability", sub: "works offline too" },
  ];
  return (
    <section className="py-24" style={{ background: "#f6f4e9", borderTop: "1px solid #c1c9b9" }}>
      <div className="max-w-[1200px] mx-auto px-5 md:px-10">
        <RevealUp className="text-center mb-16">
          <h2 className="text-[2rem] md:text-[2.5rem] font-bold mb-3" style={{ fontFamily: "Fraunces", color: "#1b1c16" }}>
            Platform at a glance
          </h2>
          <p className="text-base" style={{ fontFamily: "Inter", color: "#717a6b" }}>Real numbers, real impact.</p>
        </RevealUp>
        <div className="grid grid-cols-2 md:grid-cols-3 gap-6">
          {items.map((s, i) => (
            <RevealUp key={s.label} delay={i * 0.07}>
              <div className="p-8 rounded-3xl border text-center" style={{ background: "#fff", borderColor: "#e4e3d8" }}>
                <div className="text-4xl font-bold mb-1" style={{ fontFamily: "Fraunces", color: "#125106" }}>
                  <Counter end={s.val} suffix={s.suffix} />
                </div>
                <p className="font-semibold text-base mb-0.5" style={{ fontFamily: "Inter", color: "#1b1c16" }}>{s.label}</p>
                <p className="text-xs" style={{ fontFamily: "Inter", color: "#717a6b" }}>{s.sub}</p>
              </div>
            </RevealUp>
          ))}
        </div>
      </div>
    </section>
  );
}

// ─── CTA Banner ────────────────────────────────────────────────────────────
function CTABanner({ onOpenApp }: { onOpenApp: () => void }) {
  const [email, setEmail] = useState("");
  const [done, setDone] = useState(false);
  return (
    <section className="px-5 md:px-10 py-24 max-w-[1200px] mx-auto">
      <RevealUp>
        <div className="rounded-[56px] p-14 md:p-24 text-center text-white relative overflow-hidden group" style={{ background: "#125106" }}>
          <img
            alt="Farmland aerial view"
            src="https://images.unsplash.com/photo-1500937386664-56d1dfef3854?w=1200&h=600&fit=crop&auto=format"
            className="absolute inset-0 w-full h-full object-cover opacity-20 transition-transform duration-[10s] group-hover:scale-110"
          />
          <div className="relative z-10">
            <h2
              className="text-[2.8rem] md:text-[3.5rem] font-bold mb-6 leading-tight"
              style={{ fontFamily: "Fraunces", letterSpacing: "-0.02em" }}
            >
              Ready to grow better?
            </h2>
            <p className="text-xl md:text-2xl max-w-2xl mx-auto mb-12 opacity-80 leading-relaxed" style={{ fontFamily: "Inter" }}>
              Join thousands of farmers using KrishiMitra AI to protect their crops and increase their income today.
            </p>
            {done ? (
              <div className="inline-flex items-center gap-3 px-8 py-4 rounded-full bg-white text-green-900 font-semibold text-lg" style={{ fontFamily: "Inter" }}>
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5"><polyline points="20 6 9 17 4 12" /></svg>
                You&apos;re on the list!
              </div>
            ) : (
              <form
                onSubmit={(e) => { e.preventDefault(); if (email) setDone(true); }}
                className="flex flex-col sm:flex-row gap-4 max-w-md mx-auto mb-8"
              >
                <input
                  type="email"
                  value={email}
                  onChange={e => setEmail(e.target.value)}
                  placeholder="your@email.com"
                  required
                  className="flex-1 px-6 py-4 rounded-full text-base focus:outline-none"
                  style={{ fontFamily: "Inter", color: "#1b1c16", background: "rgba(252,250,239,0.95)" }}
                />
                <button
                  type="submit"
                  className="px-8 py-4 rounded-full font-bold text-base transition-all hover:-translate-y-0.5"
                  style={{ background: "#fcfaef", color: "#125106", fontFamily: "Inter" }}
                >
                  Get Early Access
                </button>
              </form>
            )}
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <button
                onClick={onOpenApp}
                className="px-12 py-4 rounded-full font-bold text-xl transition-all hover:-translate-y-0.5 shadow-xl"
                style={{ background: "#fff", color: "#125106", fontFamily: "Inter" }}
              >
                Open KrishiMitra App
              </button>
              <button
                onClick={onOpenApp}
                className="px-12 py-4 rounded-full font-bold text-xl transition-all hover:bg-white/10"
                style={{ border: "2px solid rgba(255,255,255,0.5)", color: "#fff", fontFamily: "Inter" }}
              >
                Launch Dashboard
              </button>
            </div>
          </div>
        </div>
      </RevealUp>
    </section>
  );
}

// ─── Footer ────────────────────────────────────────────────────────────────
function Footer() {
  const product = ["Features", "How it works", "Languages", "For teams", "Price points"];
  const company = ["About Us", "Careers", "Press Kit", "Contact", "Sustainability"];
  const legal = ["Privacy Policy", "Terms of Service", "Data Usage", "Compliance"];

  return (
    <footer className="py-20 px-5 md:px-10 border-t" style={{ background: "#30312a", borderColor: "rgba(255,255,255,0.05)" }}>
      <div className="max-w-[1200px] mx-auto">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-12 gap-12 mb-16">
          <div className="lg:col-span-4">
            <div className="flex items-center gap-2.5 mb-6">
              <KrishiMitraLogo size={40} />
              <span className="text-xl font-bold" style={{ fontFamily: "Fraunces", color: "#aff498" }}>KrishiMitra AI</span>
            </div>
            <p className="text-base leading-relaxed mb-8 opacity-60" style={{ fontFamily: "Inter", color: "#eae8de" }}>
              Grounded Intelligence for Every Farmer. Empowering India&apos;s agricultural backbone with cutting-edge, local-first AI technology.
            </p>
            <div className="flex gap-3">
              {["Globe", "Mail", "Phone"].map((icon) => (
                <a
                  key={icon}
                  href="#"
                  className="w-11 h-11 rounded-full border flex items-center justify-center transition-all"
                  style={{ borderColor: "rgba(255,255,255,0.2)", color: "#eae8de" }}
                  onMouseEnter={e => { e.currentTarget.style.borderColor = "#aff498"; e.currentTarget.style.color = "#aff498"; }}
                  onMouseLeave={e => { e.currentTarget.style.borderColor = "rgba(255,255,255,0.2)"; e.currentTarget.style.color = "#eae8de"; }}
                >
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    {icon === "Globe" && <><circle cx="12" cy="12" r="10" /><path d="M2 12h20M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z" /></>}
                    {icon === "Mail" && <><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z" /><polyline points="22,6 12,13 2,6" /></>}
                    {icon === "Phone" && <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.49 12 19.79 19.79 0 0 1 1.52 3.44 2 2 0 0 1 3.5 2h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L7.91 9.91a16 16 0 0 0 6 6l.83-.83a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z" />}
                  </svg>
                </a>
              ))}
            </div>
          </div>

          {[
            { heading: "Product", links: product },
            { heading: "Company", links: company },
            { heading: "Legal", links: legal },
          ].map((col) => (
            <div key={col.heading} className="lg:col-span-2">
              <h4 className="text-xs font-bold uppercase tracking-[0.2em] mb-6" style={{ fontFamily: "Inter", color: "#aff498" }}>
                {col.heading}
              </h4>
              <ul className="space-y-3">
                {col.links.map(l => (
                  <li key={l}>
                    <a
                      href="#"
                      className="text-sm opacity-60 transition-opacity hover:opacity-100"
                      style={{ fontFamily: "Inter", color: "#eae8de" }}
                    >
                      {l}
                    </a>
                  </li>
                ))}
              </ul>
            </div>
          ))}

          <div className="lg:col-span-2">
            <h4 className="text-xs font-bold uppercase tracking-[0.2em] mb-6" style={{ fontFamily: "Inter", color: "#aff498" }}>Contact</h4>
            <ul className="space-y-3">
              {[{ label: "1800-AGRI-AI" }, { label: "support@krishimitra.ai" }].map(c => (
                <li key={c.label} className="text-sm opacity-60" style={{ fontFamily: "Inter", color: "#eae8de" }}>{c.label}</li>
              ))}
            </ul>
          </div>
        </div>

        <div className="pt-10 border-t flex flex-col md:flex-row justify-between items-center gap-5" style={{ borderColor: "rgba(255,255,255,0.08)" }}>
          <p className="text-xs opacity-50" style={{ fontFamily: "Inter", color: "#eae8de" }}>
            © 2024 KrishiMitra AI. Grounded Intelligence for Every Farmer.
          </p>
          <div className="flex gap-6 opacity-50 text-xs" style={{ fontFamily: "Inter", color: "#eae8de" }}>
            {["Cookie Policy", "Security", "Accessibility"].map(l => (
              <a key={l} href="#" className="hover:underline">{l}</a>
            ))}
          </div>
        </div>
      </div>
    </footer>
  );
}

// ─── App root ──────────────────────────────────────────────────────────────
export default function App() {
  const [view, setView] = useState<'landing' | 'auth' | 'profile-setup' | 'dashboard'>('landing');
  const [authMode, setAuthMode] = useState<'login' | 'register'>('login');
  
  // Auth Form State
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [prefLang, setPrefLang] = useState('hi');
  const [authLoading, setAuthLoading] = useState(false);

  // Profile Setup State
  const [profileName, setProfileName] = useState('');
  const [stateName, setStateName] = useState('');
  const [districtName, setDistrictName] = useState('');
  const [villageName, setVillageName] = useState('');
  const [pincodeVal, setPincodeVal] = useState('');
  const [landHolding, setLandHolding] = useState('');
  const [incomeCat, setIncomeCat] = useState<'BELOW_1_LAKH' | 'ONE_TO_THREE_LAKH' | 'THREE_TO_FIVE_LAKH' | 'ABOVE_FIVE_LAKH'>('BELOW_1_LAKH');
  const [profileLoading, setProfileLoading] = useState(false);

  // Authenticated State
  const [farmer, setFarmer] = useState<FarmerResponse | null>(null);
  const [isVoiceOpen, setIsVoiceOpen] = useState(false);

  // Listen for unauthorized events to trigger auto-logout
  useEffect(() => {
    const handleUnauthorized = () => {
      setFarmer(null);
      setView('auth');
      setAuthMode('login');
    };
    window.addEventListener('km_unauthorized', handleUnauthorized);
    return () => window.removeEventListener('km_unauthorized', handleUnauthorized);
  }, []);

  // Check login on startup
  useEffect(() => {
    const checkLogin = async () => {
      const { accessToken } = getTokens();
      if (accessToken) {
        try {
          const profile = await api.getFarmerProfile();
          if (profile) {
            setFarmer(profile);
            setView('dashboard');
          } else {
            setView('profile-setup');
          }
        } catch (e) {
          clearTokens();
          setView('landing');
        }
      }
    };
    checkLogin();
  }, []);

  const handleLoginSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setAuthLoading(true);
    try {
      const data = await api.login({ phoneNumber: phone, password });
      setTokens(data.accessToken, data.refreshToken);
      const profile = await api.getFarmerProfile();
      if (profile) {
        setFarmer(profile);
        setView('dashboard');
      } else {
        setView('profile-setup');
      }
    } catch (err: any) {
      alert(err.message || 'Login failed');
    } finally {
      setAuthLoading(false);
    }
  };

  const handleRegisterSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setAuthLoading(true);
    try {
      const data = await api.register({
        phoneNumber: phone,
        password,
        email: email || undefined,
        preferredLanguage: prefLang
      });
      setTokens(data.accessToken, data.refreshToken);
      setProfileName(name); // autofill profile setup name
      setView('profile-setup');
    } catch (err: any) {
      alert(err.message || 'Registration failed');
    } finally {
      setAuthLoading(false);
    }
  };

  const handleProfileSetup = async (e: React.FormEvent) => {
    e.preventDefault();
    setProfileLoading(true);
    try {
      const payload = {
        fullName: profileName,
        state: stateName,
        district: districtName,
        village: villageName || undefined,
        pincode: pincodeVal,
        landHoldingHectares: parseFloat(landHolding),
        incomeCategory: incomeCat
      };
      const profile = await api.createFarmerProfile(payload);
      setFarmer(profile);
      setView('dashboard');
    } catch (err: any) {
      alert(err.message || 'Failed to create profile');
    } finally {
      setProfileLoading(false);
    }
  };

  const handleLogout = () => {
    clearTokens();
    setFarmer(null);
    setView('landing');
  };

  return (
    <div style={{ fontFamily: "Inter", background: "#fcfaef", color: "#1b1c16" }}>
      <style>{`
        @import url('https://fonts.googleapis.com/css2?family=Fraunces:ital,opsz,wght@0,9..144,400;0,9..144,600;0,9..144,700;0,9..144,800;1,9..144,400;1,9..144,600;1,9..144,700;1,9..144,800&family=Inter:wght@400;500;600;700&display=swap');
        html { scroll-behavior: smooth; }
        ::-webkit-scrollbar { width: 5px; }
        ::-webkit-scrollbar-track { background: #fcfaef; }
        ::-webkit-scrollbar-thumb { background: #c1c9b9; border-radius: 3px; }
        ::selection { background: #aff498; color: #022100; }
        @keyframes wave {
          0%, 100% { height: 8px; }
          50% { height: 32px; }
        }
        .wave-bar { animation: wave 1.2s ease-in-out infinite; }
      `}</style>

      {view === 'landing' && (
        <>
          <Nav onOpenApp={() => setView('auth')} />
          <main className="pt-20">
            <Hero onOpenApp={() => setView('auth')} onStartTalking={() => { setAuthMode('register'); setView('auth'); }} />
            <TrustStrip />
            <Features />
            <HowItWorks />
            <Languages />
            <Stats />
            <CTABanner onOpenApp={() => setView('auth')} />
          </main>
          <Footer />
        </>
      )}

      {view === 'auth' && (
        <div className="min-h-screen bg-background flex flex-col justify-center items-center p-6 relative">
          {/* Landing back button */}
          <button
            onClick={() => setView('landing')}
            className="absolute top-6 left-6 text-sm font-semibold text-primary flex items-center gap-1.5 hover:underline"
          >
            ← Back to Home
          </button>

          <div className="bg-white rounded-[40px] border border-border shadow-xl p-8 max-w-md w-full space-y-6">
            <div className="text-center">
              <h2 className="text-2xl font-bold text-primary" style={{ fontFamily: 'Fraunces' }}>
                {authMode === 'login' ? 'Welcome Back' : 'Get Started'}
              </h2>
              <p className="text-xs text-muted-foreground mt-1">
                {authMode === 'login' ? 'Login to check crop recommendations' : 'Register to unlock voice assistant services'}
              </p>
            </div>

            {authMode === 'login' ? (
              <form onSubmit={handleLoginSubmit} className="space-y-4">
                <div className="space-y-1">
                  <label className="text-[10px] font-bold text-muted-foreground uppercase flex items-center gap-1">
                    <Phone size={10} /> Phone Number
                  </label>
                  <input
                    type="tel"
                    placeholder="e.g. 9876543210"
                    required
                    value={phone}
                    onChange={e => setPhone(e.target.value)}
                    className="w-full bg-secondary text-sm border-none rounded-xl px-4 py-3 focus:ring-1 focus:ring-primary outline-none"
                  />
                </div>
                <div className="space-y-1">
                  <label className="text-[10px] font-bold text-muted-foreground uppercase flex items-center gap-1">
                    <Lock size={10} /> Password
                  </label>
                  <input
                    type="password"
                    placeholder="••••••••"
                    required
                    value={password}
                    onChange={e => setPassword(e.target.value)}
                    className="w-full bg-secondary text-sm border-none rounded-xl px-4 py-3 focus:ring-1 focus:ring-primary outline-none"
                  />
                </div>
                <button
                  type="submit"
                  disabled={authLoading}
                  className="w-full bg-primary text-white py-3.5 rounded-full font-bold text-sm hover:opacity-90 active:scale-95 transition-all shadow-md disabled:opacity-50"
                >
                  {authLoading ? 'Signing in...' : 'Sign In'}
                </button>
              </form>
            ) : (
              <form onSubmit={handleRegisterSubmit} className="space-y-4">
                <div className="space-y-1">
                  <label className="text-[10px] font-bold text-muted-foreground uppercase flex items-center gap-1">
                    <User size={10} /> Full Name
                  </label>
                  <input
                    type="text"
                    placeholder="e.g. Ramesh Kumar"
                    required
                    value={name}
                    onChange={e => setName(e.target.value)}
                    className="w-full bg-secondary text-sm border-none rounded-xl px-4 py-3 focus:ring-1 focus:ring-primary outline-none"
                  />
                </div>
                <div className="space-y-1">
                  <label className="text-[10px] font-bold text-muted-foreground uppercase flex items-center gap-1">
                    <Phone size={10} /> Phone Number
                  </label>
                  <input
                    type="tel"
                    placeholder="e.g. 9876543210"
                    required
                    value={phone}
                    onChange={e => setPhone(e.target.value)}
                    className="w-full bg-secondary text-sm border-none rounded-xl px-4 py-3 focus:ring-1 focus:ring-primary outline-none"
                  />
                </div>
                <div className="space-y-1">
                  <label className="text-[10px] font-bold text-muted-foreground uppercase flex items-center gap-1">
                    <Lock size={10} /> Password
                  </label>
                  <input
                    type="password"
                    placeholder="Min 6 characters"
                    required
                    value={password}
                    onChange={e => setPassword(e.target.value)}
                    className="w-full bg-secondary text-sm border-none rounded-xl px-4 py-3 focus:ring-1 focus:ring-primary outline-none"
                  />
                </div>
                <div className="space-y-1">
                  <label className="text-[10px] font-bold text-muted-foreground uppercase flex items-center gap-1">
                    <LanguagesIcon size={10} /> Preferred Local Dialect
                  </label>
                  <select
                    value={prefLang}
                    onChange={e => setPrefLang(e.target.value)}
                    className="w-full bg-secondary text-sm border-none rounded-xl px-3 py-3 focus:ring-1 focus:ring-primary outline-none cursor-pointer"
                  >
                    <option value="hi">Hindi (हिन्दी)</option>
                    <option value="mr">Marathi (मराठी)</option>
                    <option value="te">Telugu (తెలుగు)</option>
                    <option value="kn">Kannada (ಕನ್ನಡ)</option>
                    <option value="en">English</option>
                  </select>
                </div>
                <button
                  type="submit"
                  disabled={authLoading}
                  className="w-full bg-primary text-white py-3.5 rounded-full font-bold text-sm hover:opacity-90 active:scale-95 transition-all shadow-md disabled:opacity-50"
                >
                  {authLoading ? 'Registering...' : 'Register User'}
                </button>
              </form>
            )}

            <div className="text-center border-t border-border pt-4">
              <button
                onClick={() => setAuthMode(authMode === 'login' ? 'register' : 'login')}
                className="text-xs text-primary font-bold hover:underline"
              >
                {authMode === 'login' ? "Don't have an account? Sign Up" : 'Already registered? Log In'}
              </button>
            </div>
          </div>
        </div>
      )}

      {view === 'profile-setup' && (
        <div className="min-h-screen bg-background flex flex-col justify-center items-center p-6">
          <div className="bg-white rounded-[40px] border border-border shadow-xl p-8 max-w-md w-full space-y-6">
            <div className="text-center">
              <h2 className="text-2xl font-bold text-primary" style={{ fontFamily: 'Fraunces' }}>Farmer Profile Setup</h2>
              <p className="text-xs text-muted-foreground mt-1">Configure your location and holdings to calibrate government rules engines</p>
            </div>

            <form onSubmit={handleProfileSetup} className="space-y-4">
              <div className="space-y-1">
                <label className="text-[10px] font-bold text-muted-foreground uppercase">Farmer Full Name</label>
                <input
                  type="text"
                  required
                  value={profileName}
                  onChange={e => setProfileName(e.target.value)}
                  className="w-full bg-secondary text-sm border-none rounded-xl px-4 py-3 focus:ring-1 focus:ring-primary outline-none"
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-1">
                  <label className="text-[10px] font-bold text-muted-foreground uppercase">State</label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. Rajasthan"
                    value={stateName}
                    onChange={e => setStateName(e.target.value)}
                    className="w-full bg-secondary text-sm border-none rounded-xl px-4 py-3 focus:ring-1 focus:ring-primary outline-none"
                  />
                </div>
                <div className="space-y-1">
                  <label className="text-[10px] font-bold text-muted-foreground uppercase">District</label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. Jaipur"
                    value={districtName}
                    onChange={e => setDistrictName(e.target.value)}
                    className="w-full bg-secondary text-sm border-none rounded-xl px-4 py-3 focus:ring-1 focus:ring-primary outline-none"
                  />
                </div>
                <div className="space-y-1">
                  <label className="text-[10px] font-bold text-muted-foreground uppercase">Village</label>
                  <input
                    type="text"
                    placeholder="e.g. Uttara"
                    value={villageName}
                    onChange={e => setVillageName(e.target.value)}
                    className="w-full bg-secondary text-sm border-none rounded-xl px-4 py-3 focus:ring-1 focus:ring-primary outline-none"
                  />
                </div>
                <div className="space-y-1">
                  <label className="text-[10px] font-bold text-muted-foreground uppercase">Pincode (6 digits)</label>
                  <input
                    type="text"
                    required
                    pattern="[0-9]{6}"
                    maxLength={6}
                    placeholder="e.g. 302012"
                    value={pincodeVal}
                    onChange={e => setPincodeVal(e.target.value)}
                    className="w-full bg-secondary text-sm border-none rounded-xl px-4 py-3 focus:ring-1 focus:ring-primary outline-none"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-1">
                  <label className="text-[10px] font-bold text-muted-foreground uppercase">Land Holdings (Ha)</label>
                  <input
                    type="number"
                    step="0.01"
                    required
                    placeholder="e.g. 1.2"
                    value={landHolding}
                    onChange={e => setLandHolding(e.target.value)}
                    className="w-full bg-secondary text-sm border-none rounded-xl px-4 py-3 focus:ring-1 focus:ring-primary outline-none"
                  />
                </div>
                <div className="space-y-1">
                  <label className="text-[10px] font-bold text-muted-foreground uppercase">Income Category</label>
                  <select
                    value={incomeCat}
                    onChange={e => setIncomeCat(e.target.value as any)}
                    className="w-full bg-secondary text-sm border-none rounded-xl px-3 py-3 focus:ring-1 focus:ring-primary outline-none cursor-pointer"
                  >
                    <option value="BELOW_1_LAKH">Below 1 Lakh</option>
                    <option value="ONE_TO_THREE_LAKH">1 to 3 Lakh</option>
                    <option value="THREE_TO_FIVE_LAKH">3 to 5 Lakh</option>
                    <option value="ABOVE_FIVE_LAKH">Above 5 Lakh</option>
                  </select>
                </div>
              </div>

              <button
                type="submit"
                disabled={profileLoading}
                className="w-full bg-primary text-white py-3.5 rounded-full font-bold text-sm hover:opacity-90 active:scale-95 transition-all shadow-md disabled:opacity-50"
              >
                {profileLoading ? 'Registering profile...' : 'Complete Profile Setup'}
              </button>
            </form>
          </div>
        </div>
      )}

      {view === 'dashboard' && farmer && (
        <>
          <DashboardHub
            farmer={farmer}
            onLogout={handleLogout}
            onUpdateProfile={(updated) => setFarmer(updated)}
            openVoiceAssistant={() => setIsVoiceOpen(true)}
          />
          <VoiceAssistantModal
            isOpen={isVoiceOpen}
            onClose={() => setIsVoiceOpen(false)}
            farmerId={farmer.id}
          />
        </>
      )}
    </div>
  );
}
