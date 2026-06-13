"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { Landmark, Eye, EyeOff, ShieldAlert, CheckCircle2 } from "lucide-react";

export default function Login() {
  const router = useRouter();
  const [isRegister, setIsRegister] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [mfaRequired, setMfaRequired] = useState(false);
  const [errorMsg, setErrorMsg] = useState("");
  const [successMsg, setSuccessMsg] = useState("");

  // Form states
  const [username, setUsername] = useState("john_doe");
  const [password, setPassword] = useState("SecurePass123!");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [enableMfa, setEnableMfa] = useState(false);
  const [otp, setOtp] = useState("");

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg("");

    // Simulate login API call to backend auth-service
    if (username === "john_doe" && password === "SecurePass123!") {
      if (enableMfa || username === "john_doe") { // Force MFA simulation for demonstration
        setMfaRequired(true);
        setSuccessMsg("MFA required. Simulating SMS verification. Use OTP: 789456");
      } else {
        // Successful login
        router.push("/dashboard");
      }
    } else {
      setErrorMsg("Invalid username or password. (Hint: john_doe / SecurePass123!)");
    }
  };

  const handleVerifyOtp = (e: React.FormEvent) => {
    e.preventDefault();
    if (otp === "789456") {
      router.push("/dashboard");
    } else {
      setErrorMsg("Incorrect OTP code. Try again.");
    }
  };

  const handleRegister = (e: React.FormEvent) => {
    e.preventDefault();
    if (!username || !email || !password || !phone) {
      setErrorMsg("All fields are required.");
      return;
    }
    setSuccessMsg("Account registration success! You can now log in.");
    setIsRegister(false);
  };

  return (
    <div className="relative min-h-screen flex items-center justify-center px-4 py-12">
      {/* Background radial glow */}
      <div className="absolute top-[-10%] left-[-10%] w-[40%] h-[40%] rounded-full bg-indigo-900/10 blur-[100px]" />
      <div className="absolute bottom-[-10%] right-[-10%] w-[40%] h-[40%] rounded-full bg-indigo-900/10 blur-[100px]" />

      <div className="w-full max-w-md relative z-10">
        {/* Brand Banner */}
        <div className="text-center mb-8">
          <Link href="/" className="inline-flex items-center space-x-2 mb-3">
            <Landmark className="h-10 w-10 text-indigo-500" />
            <span className="font-outfit font-bold text-3xl tracking-wider text-white">AURA</span>
          </Link>
          <p className="text-slate-400 text-sm">Access your Core Wealth Dashboard</p>
        </div>

        {/* Card Frame */}
        <div className="glass-card p-8 border border-white/5 shadow-2xl">
          {errorMsg && (
            <div className="mb-6 px-4 py-3 rounded-lg border border-red-500/20 bg-red-500/10 text-sm text-red-300 flex items-center space-x-2">
              <ShieldAlert className="h-4.5 w-4.5 text-red-400 shrink-0" />
              <span>{errorMsg}</span>
            </div>
          )}

          {successMsg && (
            <div className="mb-6 px-4 py-3 rounded-lg border border-emerald-500/20 bg-emerald-500/10 text-sm text-emerald-300 flex items-center space-x-2">
              <CheckCircle2 className="h-4.5 w-4.5 text-emerald-400 shrink-0" />
              <span>{successMsg}</span>
            </div>
          )}

          {/* MFA OTP Screen */}
          {mfaRequired ? (
            <form onSubmit={handleVerifyOtp} className="space-y-6">
              <div className="text-center space-y-2">
                <h2 className="text-xl font-bold text-white">Enter Verification Code</h2>
                <p className="text-xs text-slate-400">A verification code has been dispatched to your mobile. Enter OTP 789456 to continue.</p>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">One-Time Password (OTP)</label>
                <input
                  type="text"
                  maxLength={6}
                  placeholder="------"
                  value={otp}
                  onChange={(e) => setOtp(e.target.value)}
                  className="w-full text-center tracking-[1em] text-lg font-bold px-4 py-3 rounded-lg bg-slate-900/60 border border-white/10 text-white focus:outline-none focus:border-indigo-500 transition-all"
                  required
                />
              </div>

              <button
                type="submit"
                className="w-full py-3 rounded-lg font-semibold bg-indigo-600 hover:bg-indigo-500 text-white transition-all glow-button"
              >
                Verify Code & Enter
              </button>

              <button
                type="button"
                onClick={() => setMfaRequired(false)}
                className="w-full text-center text-xs text-slate-400 hover:text-white transition"
              >
                Go back to login
              </button>
            </form>
          ) : (
            <>
              {/* Tab Selector */}
              <div className="flex border-b border-white/5 mb-6">
                <button
                  onClick={() => { setIsRegister(false); setErrorMsg(""); setSuccessMsg(""); }}
                  className={`flex-1 pb-3 text-sm font-semibold transition ${!isRegister ? "text-indigo-400 border-b-2 border-indigo-500" : "text-slate-400 hover:text-slate-200"}`}
                >
                  Login
                </button>
                <button
                  onClick={() => { setIsRegister(true); setErrorMsg(""); setSuccessMsg(""); }}
                  className={`flex-1 pb-3 text-sm font-semibold transition ${isRegister ? "text-indigo-400 border-b-2 border-indigo-500" : "text-slate-400 hover:text-slate-200"}`}
                >
                  Register
                </button>
              </div>

              {/* Login Form */}
              {!isRegister ? (
                <form onSubmit={handleLogin} className="space-y-6">
                  <div>
                    <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Username</label>
                    <input
                      type="text"
                      value={username}
                      onChange={(e) => setUsername(e.target.value)}
                      className="w-full px-4 py-3 rounded-lg bg-slate-900/60 border border-white/10 text-white focus:outline-none focus:border-indigo-500 transition-all text-sm"
                      required
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Password</label>
                    <div className="relative">
                      <input
                        type={showPassword ? "text" : "password"}
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        className="w-full px-4 py-3 rounded-lg bg-slate-900/60 border border-white/10 text-white focus:outline-none focus:border-indigo-500 transition-all text-sm"
                        required
                      />
                      <button
                        type="button"
                        onClick={() => setShowPassword(!showPassword)}
                        className="absolute right-3 top-3.5 text-slate-400 hover:text-white"
                      >
                        {showPassword ? <EyeOff className="h-4.5 w-4.5" /> : <Eye className="h-4.5 w-4.5" />}
                      </button>
                    </div>
                  </div>

                  <div className="flex items-center justify-between">
                    <label className="flex items-center space-x-2 text-xs text-slate-400 cursor-pointer">
                      <input
                        type="checkbox"
                        checked={enableMfa}
                        onChange={(e) => setEnableMfa(e.target.checked)}
                        className="rounded border-white/10 bg-slate-950 accent-indigo-500"
                      />
                      <span>Enable Multi-Factor (MFA)</span>
                    </label>
                    <a href="#" className="text-xs text-indigo-400 hover:underline">Forgot password?</a>
                  </div>

                  <button
                    type="submit"
                    className="w-full py-3 rounded-lg font-semibold bg-indigo-600 hover:bg-indigo-500 text-white transition-all glow-button text-sm"
                  >
                    Authenticate Account
                  </button>
                </form>
              ) : (
                /* Register Form */
                <form onSubmit={handleRegister} className="space-y-5">
                  <div>
                    <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1.5">Username</label>
                    <input
                      type="text"
                      placeholder="e.g. john_doe"
                      value={username}
                      onChange={(e) => setUsername(e.target.value)}
                      className="w-full px-4 py-2.5 rounded-lg bg-slate-900/60 border border-white/10 text-white focus:outline-none focus:border-indigo-500 transition-all text-sm"
                      required
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1.5">Email Address</label>
                    <input
                      type="email"
                      placeholder="e.g. john@domain.com"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      className="w-full px-4 py-2.5 rounded-lg bg-slate-900/60 border border-white/10 text-white focus:outline-none focus:border-indigo-500 transition-all text-sm"
                      required
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1.5">Phone Number</label>
                    <input
                      type="text"
                      placeholder="e.g. +919988776655"
                      value={phone}
                      onChange={(e) => setPhone(e.target.value)}
                      className="w-full px-4 py-2.5 rounded-lg bg-slate-900/60 border border-white/10 text-white focus:outline-none focus:border-indigo-500 transition-all text-sm"
                      required
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1.5">Password</label>
                    <input
                      type="password"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      className="w-full px-4 py-2.5 rounded-lg bg-slate-900/60 border border-white/10 text-white focus:outline-none focus:border-indigo-500 transition-all text-sm"
                      required
                    />
                  </div>

                  <button
                    type="submit"
                    className="w-full py-3 mt-2 rounded-lg font-semibold bg-indigo-600 hover:bg-indigo-500 text-white transition-all glow-button text-sm"
                  >
                    Submit Enrollment
                  </button>
                </form>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
}
