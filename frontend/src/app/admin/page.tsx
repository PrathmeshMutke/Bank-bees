"use client";

import { useState } from "react";
import { 
  Landmark, UserCheck, ShieldAlert, FileText, AlertTriangle, Check, X,
  FileSpreadsheet, Settings, LogOut, CheckCircle2, TrendingUp, Users, DollarSign
} from "lucide-react";

export default function Admin() {
  const [successMsg, setSuccessMsg] = useState("");
  const [activeSubTab, setActiveSubTab] = useState("kyc");

  // KYC Verification Queue
  const [kycQueue, setKycQueue] = useState([
    { id: 1, name: "Alice Vance", pan: "BRPAV1284L", dob: "1994-08-12", documentType: "PAN Card", docUrl: "/docs/pan_alice.pdf", status: "PENDING" },
    { id: 2, name: "Bob Miller", pan: "CPMBM9084M", dob: "1988-11-23", documentType: "Aadhaar Card", docUrl: "/docs/aadhaar_bob.pdf", status: "PENDING" }
  ]);

  // Fraud Cases
  const [fraudCases, setFraudCases] = useState([
    { id: 101, ref: "TXN10842042", amount: 650000.00, riskScore: 85, rule: "HIGH_VALUE_TRANSFER_TRIGGERED", status: "OPEN" },
    { id: 102, ref: "TXN98321041", amount: 15000.00, riskScore: 72, rule: "GEO_LOCATION_VELOCITY_MISMATCH", status: "OPEN" }
  ]);

  const handleApproveKyc = (id: number, name: string) => {
    setKycQueue(prev => prev.filter(item => item.id !== id));
    setSuccessMsg(`Onboarding KYC profile for ${name} APPROVED. Wallet created in database.`);
  };

  const handleRejectKyc = (id: number, name: string) => {
    setKycQueue(prev => prev.filter(item => item.id !== id));
    setSuccessMsg(`Onboarding KYC profile for ${name} REJECTED.`);
  };

  const handleDismissFraud = (id: number) => {
    setFraudCases(prev => prev.filter(c => c.id !== id));
    setSuccessMsg(`Fraud case #${id} marked as safe.`);
  };

  const handleBlockAccount = (id: number) => {
    setFraudCases(prev => prev.filter(c => c.id !== id));
    setSuccessMsg(`Account linked with Fraud Case #${id} BLOCKED immediately. Audit logged.`);
  };

  return (
    <div className="min-h-screen bg-[#0f172a] text-slate-100 font-sans flex">
      {/* Sidebar Navigation */}
      <aside className="w-64 border-r border-white/5 bg-[#0b0f19] flex flex-col justify-between p-6">
        <div className="space-y-8">
          <div className="flex items-center space-x-2">
            <Landmark className="h-7 w-7 text-indigo-500" />
            <span className="font-outfit font-bold text-xl tracking-wider text-white">AURA ADMIN</span>
          </div>

          <nav className="space-y-1">
            <button 
              onClick={() => setActiveSubTab("kyc")}
              className={`w-full flex items-center space-x-3 px-4 py-3 rounded-lg text-sm font-semibold transition ${activeSubTab === "kyc" ? "bg-indigo-600 text-white" : "text-slate-400 hover:bg-white/5 hover:text-white"}`}
            >
              <UserCheck className="h-4.5 w-4.5" />
              <span>KYC Approvals</span>
            </button>

            <button 
              onClick={() => setActiveSubTab("fraud")}
              className={`w-full flex items-center space-x-3 px-4 py-3 rounded-lg text-sm font-semibold transition ${activeSubTab === "fraud" ? "bg-indigo-600 text-white" : "text-slate-400 hover:bg-white/5 hover:text-white"}`}
            >
              <ShieldAlert className="h-4.5 w-4.5" />
              <span>Fraud Cases</span>
            </button>

            <button 
              onClick={() => setActiveSubTab("reports")}
              className={`w-full flex items-center space-x-3 px-4 py-3 rounded-lg text-sm font-semibold transition ${activeSubTab === "reports" ? "bg-indigo-600 text-white" : "text-slate-400 hover:bg-white/5 hover:text-white"}`}
            >
              <FileSpreadsheet className="h-4.5 w-4.5" />
              <span>System Reports</span>
            </button>
          </nav>
        </div>

        <div>
          <a href="/login" className="w-full flex items-center space-x-3 px-4 py-3 rounded-lg text-sm font-semibold text-red-400 hover:bg-red-500/10 transition">
            <LogOut className="h-4.5 w-4.5" />
            <span>Sign Out</span>
          </a>
        </div>
      </aside>

      {/* Main Content Area */}
      <main className="flex-grow p-8 max-w-5xl mx-auto space-y-8">
        {/* Header Ribbon */}
        <div className="flex items-center justify-between border-b border-white/5 pb-6">
          <div>
            <h1 className="text-2xl font-bold text-white">System Administration Panel</h1>
            <p className="text-xs text-slate-400">Security Access Level: SUPER_ADMIN | System audits synced</p>
          </div>
          <div className="flex items-center space-x-2 text-xs text-indigo-400 font-semibold px-3 py-1.5 rounded-full border border-indigo-500/20 bg-indigo-500/10">
            <Settings className="h-4 w-4" />
            <span>Platform Node: Connected</span>
          </div>
        </div>

        {/* Success message banner */}
        {successMsg && (
          <div className="px-4 py-3 rounded-lg border border-emerald-500/20 bg-emerald-500/10 text-sm text-emerald-300 flex items-center space-x-2">
            <CheckCircle2 className="h-4.5 w-4.5 text-emerald-400 shrink-0" />
            <span>{successMsg}</span>
          </div>
        )}

        {/* Global Statistics Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-6">
          <div className="glass-card p-6 flex items-center space-x-4">
            <div className="h-10 w-10 rounded-lg bg-indigo-500/10 flex items-center justify-center text-indigo-400">
              <Users className="h-5 w-5" />
            </div>
            <div>
              <div className="text-xs text-slate-400">Onboarded Customers</div>
              <div className="text-lg font-bold text-white">14,204</div>
            </div>
          </div>

          <div className="glass-card p-6 flex items-center space-x-4">
            <div className="h-10 w-10 rounded-lg bg-emerald-500/10 flex items-center justify-center text-emerald-400">
              <TrendingUp className="h-5 w-5" />
            </div>
            <div>
              <div className="text-xs text-slate-400">Volume processed (24h)</div>
              <div className="text-lg font-bold text-white">Rs. 4.82 Cr</div>
            </div>
          </div>

          <div className="glass-card p-6 flex items-center space-x-4">
            <div className="h-10 w-10 rounded-lg bg-pink-500/10 flex items-center justify-center text-pink-400">
              <DollarSign className="h-5 w-5" />
            </div>
            <div>
              <div className="text-xs text-slate-400">Outstanding Disbursals</div>
              <div className="text-lg font-bold text-white">Rs. 84.50 Lakh</div>
            </div>
          </div>
        </div>

        {/* Tab 1: KYC Approval Queue */}
        {activeSubTab === "kyc" && (
          <div className="glass-card p-6 space-y-6">
            <div>
              <h3 className="font-bold text-white">KYC Verification Queue</h3>
              <p className="text-[10px] text-slate-400">Customer onboarding profiles requesting verification audits</p>
            </div>

            {kycQueue.length === 0 ? (
              <p className="text-xs text-slate-500 text-center py-6">All customer KYC applications are verified.</p>
            ) : (
              <div className="divide-y divide-white/5">
                {kycQueue.map(item => (
                  <div key={item.id} className="py-4 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                    <div className="space-y-1">
                      <div className="font-semibold text-white">{item.name}</div>
                      <div className="text-[10px] text-slate-500 flex space-x-4">
                        <span>PAN: {item.pan}</span>
                        <span>DOB: {item.dob}</span>
                      </div>
                      <div className="text-xs text-indigo-400 font-medium">Doc: {item.documentType} ({item.docUrl})</div>
                    </div>
                    <div className="flex space-x-2">
                      <button 
                        onClick={() => handleApproveKyc(item.id, item.name)}
                        className="p-2 rounded-lg bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-400 border border-emerald-500/20 transition"
                      >
                        <Check className="h-4 w-4" />
                      </button>
                      <button 
                        onClick={() => handleRejectKyc(item.id, item.name)}
                        className="p-2 rounded-lg bg-red-500/10 hover:bg-red-500/20 text-red-400 border border-red-500/20 transition"
                      >
                        <X className="h-4 w-4" />
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* Tab 2: Fraud cases panel */}
        {activeSubTab === "fraud" && (
          <div className="glass-card p-6 space-y-6">
            <div>
              <h3 className="font-bold text-white">Fraud Detections</h3>
              <p className="text-[10px] text-slate-400">Transaction evaluations flagged by real-time rules</p>
            </div>

            {fraudCases.length === 0 ? (
              <p className="text-xs text-slate-500 text-center py-6">No active fraud threats detected.</p>
            ) : (
              <div className="space-y-4">
                {fraudCases.map(c => (
                  <div key={c.id} className="p-4 rounded-lg border border-red-500/25 bg-red-500/5 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                    <div className="space-y-1">
                      <div className="flex items-center space-x-2 text-red-400 font-bold text-sm">
                        <AlertTriangle className="h-4 w-4" />
                        <span>High Risk Score: {c.riskScore}%</span>
                      </div>
                      <p className="text-xs text-slate-300">Txn Reference: <span className="font-mono text-white font-medium">{c.ref}</span> | Amount: Rs. {c.amount.toLocaleString('en-IN')}</p>
                      <p className="text-[10px] text-slate-500">Trigger: {c.rule}</p>
                    </div>
                    <div className="flex space-x-2">
                      <button 
                        onClick={() => handleBlockAccount(c.id)}
                        className="px-3 py-1.5 rounded-lg bg-red-600 text-white font-semibold hover:bg-red-500 text-xs transition"
                      >
                        Block Account
                      </button>
                      <button 
                        onClick={() => handleDismissFraud(c.id)}
                        className="px-3 py-1.5 rounded-lg border border-white/10 hover:bg-white/10 text-slate-400 text-xs transition"
                      >
                        Mark Safe
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* Tab 3: System Reports */}
        {activeSubTab === "reports" && (
          <div className="glass-card p-6 space-y-6">
            <div>
              <h3 className="font-bold text-white">Export Platform Reports</h3>
              <p className="text-[10px] text-slate-400">Generate structured logs and statement outputs</p>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-2">
              <button 
                onClick={() => setSuccessMsg("Generating Ledger Audit PDF statement. Sent to background generation worker.")}
                className="p-6 rounded-xl border border-white/5 bg-slate-900/60 hover:bg-slate-900 flex items-center justify-between text-left transition group"
              >
                <div className="space-y-1">
                  <div className="font-semibold text-sm group-hover:text-indigo-400 transition">Transaction Ledger (PDF)</div>
                  <p className="text-[10px] text-slate-500">Standard audit reports formatted for banking compliance</p>
                </div>
                <FileText className="h-6 w-6 text-slate-500 group-hover:text-indigo-400 transition" />
              </button>

              <button 
                onClick={() => setSuccessMsg("Exporting fraud metrics spreadsheet (CSV). Sent to background worker.")}
                className="p-6 rounded-xl border border-white/5 bg-slate-900/60 hover:bg-slate-900 flex items-center justify-between text-left transition group"
              >
                <div className="space-y-1">
                  <div className="font-semibold text-sm group-hover:text-emerald-400 transition">Fraud & Risk Index (CSV)</div>
                  <p className="text-[10px] text-slate-500">List of rule violations and geolocations exported</p>
                </div>
                <FileSpreadsheet className="h-6 w-6 text-slate-500 group-hover:text-emerald-400 transition" />
              </button>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
