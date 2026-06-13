"use client";

import Link from "next/link";
import { ArrowRight, Shield, Cpu, Landmark, ChevronRight } from "lucide-react";

export default function Home() {
  return (
    <div className="relative min-h-screen flex flex-col justify-between overflow-hidden">
      {/* Background blobs */}
      <div className="absolute top-[-20%] left-[-10%] w-[50%] h-[50%] rounded-full bg-indigo-900/20 blur-[120px] bg-glow-pulse" />
      <div className="absolute bottom-[-10%] right-[-10%] w-[50%] h-[50%] rounded-full bg-emerald-900/10 blur-[120px]" />

      {/* Header */}
      <header className="relative z-10 max-w-7xl mx-auto w-full px-6 py-6 flex items-center justify-between">
        <div className="flex items-center space-x-2">
          <Landmark className="h-8 w-8 text-indigo-500" />
          <span className="font-outfit font-bold text-2xl tracking-wider bg-clip-text text-transparent bg-gradient-to-r from-white via-indigo-200 to-indigo-500">
            AURA
          </span>
        </div>
        <div className="flex items-center space-x-4">
          <Link href="/login" className="px-5 py-2 rounded-lg text-sm font-medium border border-white/10 bg-white/5 hover:bg-white/10 hover:border-white/20 transition-all">
            Sign In
          </Link>
          <Link href="/login?tab=register" className="px-5 py-2 rounded-lg text-sm font-medium bg-indigo-600 text-white hover:bg-indigo-500 glow-button transition-all">
            Get Started
          </Link>
        </div>
      </header>

      {/* Main Hero */}
      <main className="relative z-10 max-w-7xl mx-auto w-full px-6 py-12 flex-grow flex flex-col lg:flex-row items-center justify-between gap-12">
        <div className="flex-1 space-y-8 text-center lg:text-left">
          <div className="inline-flex items-center space-x-2 px-3 py-1.5 rounded-full border border-indigo-500/30 bg-indigo-500/10 text-xs font-semibold text-indigo-300">
            <span>Next-Generation AI Core Banking</span>
            <ChevronRight className="h-3.5 w-3.5" />
          </div>
          
          <h1 className="text-4xl md:text-6xl font-extrabold tracking-tight leading-tight">
            The Autonomous <br />
            <span className="bg-clip-text text-transparent bg-gradient-to-r from-indigo-400 via-purple-400 to-pink-400">
              Future of Wealth
            </span>
          </h1>
          
          <p className="text-slate-400 max-w-xl mx-auto lg:mx-0 text-base md:text-lg leading-relaxed">
            Experience security and convenience combined with real-time financial tracking, algorithmic spending coaching, card safety parameters, and AI-powered transaction protection.
          </p>

          <div className="flex flex-col sm:flex-row justify-center lg:justify-start items-center gap-4">
            <Link href="/login" className="w-full sm:w-auto px-8 py-3.5 rounded-xl font-medium bg-indigo-600 hover:bg-indigo-500 text-white flex items-center justify-center space-x-2 glow-button">
              <span>Access Your Dashboard</span>
              <ArrowRight className="h-4 w-4" />
            </Link>
            <Link href="/admin" className="w-full sm:w-auto px-8 py-3.5 rounded-xl font-medium border border-white/10 bg-white/5 hover:bg-white/10 text-slate-300 flex items-center justify-center space-x-2">
              <span>Admin Management</span>
            </Link>
          </div>
        </div>

        {/* Feature Cards Grid */}
        <div className="flex-1 w-full grid grid-cols-1 sm:grid-cols-2 gap-6">
          <div className="glass-card p-6 flex flex-col space-y-4 hover:border-indigo-500/30 hover:scale-[1.02] transition-all duration-300">
            <div className="h-10 w-10 rounded-lg bg-indigo-500/10 flex items-center justify-center text-indigo-400">
              <Shield className="h-5 w-5" />
            </div>
            <h3 className="font-semibold text-lg">PCI-DSS Compliant Security</h3>
            <p className="text-slate-400 text-sm">
              MFA login tracking, IP analytics, instant card freeze mechanisms, and database-level optimistic version control lockouts.
            </p>
          </div>

          <div className="glass-card p-6 flex flex-col space-y-4 hover:border-emerald-500/30 hover:scale-[1.02] transition-all duration-300">
            <div className="h-10 w-10 rounded-lg bg-emerald-500/10 flex items-center justify-center text-emerald-400">
              <Cpu className="h-5 w-5" />
            </div>
            <h3 className="font-semibold text-lg">AI Financial Coach</h3>
            <p className="text-slate-400 text-sm">
              Get spending reports, transaction insights, savings suggestions, and instant answers from your LLM powered chatbot.
            </p>
          </div>

          <div className="glass-card p-6 flex flex-col space-y-4 hover:border-purple-500/30 hover:scale-[1.02] transition-all duration-300">
            <div className="h-10 w-10 rounded-lg bg-purple-500/10 flex items-center justify-center text-purple-400">
              <Landmark className="h-5 w-5" />
            </div>
            <h3 className="font-semibold text-lg">Core Transaction Engines</h3>
            <p className="text-slate-400 text-sm">
              Run transfer requests across IMPS, NEFT, RTGS, and UPI backed by Oracle stored procedures and transactional outbox.
            </p>
          </div>

          <div className="glass-card p-6 flex flex-col space-y-4 hover:border-pink-500/30 hover:scale-[1.02] transition-all duration-300">
            <div className="h-10 w-10 rounded-lg bg-pink-500/10 flex items-center justify-center text-pink-400">
              <Landmark className="h-5 w-5" />
            </div>
            <h3 className="font-semibold text-lg">Real-Time Event Streams</h3>
            <p className="text-slate-400 text-sm">
              Instant alerts and balance entries driven by Kafka event pipelines and local Redis cache stores.
            </p>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="relative z-10 max-w-7xl mx-auto w-full px-6 py-8 border-t border-white/5 flex flex-col sm:flex-row justify-between items-center text-xs text-slate-500 gap-4">
        <span>© 2026 Aura Core Platform. All rights reserved.</span>
        <div className="flex space-x-6">
          <a href="#" className="hover:text-slate-300">Security Details</a>
          <a href="#" className="hover:text-slate-300">API Documentation</a>
          <a href="#" className="hover:text-slate-300">Compliance & Regulatory</a>
        </div>
      </footer>
    </div>
  );
}
