"use client";

import { useState } from "react";
import { 
  Landmark, Send, Wallet, CreditCard, Bot, Sparkles, LogOut, CheckCircle2,
  Lock, Unlock, ShieldAlert, Sliders, History, MessageSquare, X, ArrowUpRight, ArrowDownLeft
} from "lucide-react";

export default function Dashboard() {
  // Navigation & Page State
  const [activeTab, setActiveTab] = useState("accounts");
  const [showChat, setShowChat] = useState(false);

  // Success/Error panels
  const [successMsg, setSuccessMsg] = useState("");
  const [errorMsg, setErrorMsg] = useState("");

  // Account Balances
  const [savingsBal, setSavingsBal] = useState(45200.50);
  const [currentBal, setCurrentBal] = useState(120000.00);
  const [fdBal, setFdBal] = useState(250000.00);

  // Cards state
  const [cardStatus, setCardStatus] = useState("ACTIVE"); // ACTIVE, BLOCKED, FROZEN
  const [cardLimit, setCardLimit] = useState(50000);
  const [oldPin, setOldPin] = useState("");
  const [newPin, setNewPin] = useState("");

  // Transfer form
  const [recipientAcc, setRecipientAcc] = useState("");
  const [amount, setAmount] = useState("");
  const [channel, setChannel] = useState("UPI");
  const [description, setDescription] = useState("");

  // Chat message states
  const [chatInput, setChatInput] = useState("");
  const [chatLog, setChatLog] = useState([
    { isUser: false, text: "Welcome to Aura AI assistance. How may I help you configure your accounts today?" }
  ]);

  // Transaction history records
  const [txns, setTxns] = useState([
    { id: 1, type: "DEBIT", ref: "TXN19842104", dest: "ACC9082348", amount: 1500.00, date: "2026-06-12 14:30", desc: "Zomato Food Order", channel: "UPI" },
    { id: 2, type: "CREDIT", ref: "TXN98321042", dest: "ACC10000021", amount: 50000.00, date: "2026-06-10 10:15", desc: "Monthly Salary Credit", channel: "NEFT" },
    { id: 3, type: "DEBIT", ref: "TXN23098412", dest: "ACC32489082", amount: 12000.00, date: "2026-06-08 18:45", desc: "House Rent", channel: "IMPS" }
  ]);

  // Submit transfer
  const handleTransfer = (e: React.FormEvent) => {
    e.preventDefault();
    setSuccessMsg("");
    setErrorMsg("");

    const transferAmt = parseFloat(amount);
    if (isNaN(transferAmt) || transferAmt <= 0) {
      setErrorMsg("Please enter a valid transfer amount.");
      return;
    }

    if (transferAmt > savingsBal) {
      setErrorMsg("Insufficient funds in your Savings Account.");
      return;
    }

    // Process payment simulation
    setSavingsBal(prev => prev - transferAmt);
    const newTxn = {
      id: Date.now(),
      type: "DEBIT",
      ref: "TXN" + Math.floor(100000000 + Math.random() * 900000000),
      dest: recipientAcc,
      amount: transferAmt,
      date: new Date().toISOString().replace('T', ' ').substring(0, 16),
      desc: description || "Funds Transfer",
      channel: channel
    };
    
    setTxns([newTxn, ...txns]);
    setSuccessMsg(`Transfer of Rs. ${transferAmt.toFixed(2)} to ${recipientAcc} successful! (Reference: ${newTxn.ref})`);
    
    // Clear forms
    setRecipientAcc("");
    setAmount("");
    setDescription("");
  };

  // Submit PIN change
  const handlePinChange = (e: React.FormEvent) => {
    e.preventDefault();
    setSuccessMsg("");
    setErrorMsg("");
    if (!oldPin || !newPin) {
      setErrorMsg("PIN fields cannot be empty.");
      return;
    }
    setSuccessMsg("Debit Card PIN updated successfully! (Audit recorded)");
    setOldPin("");
    setNewPin("");
  };

  // Chat message submit
  const sendChatMessage = (e: React.FormEvent) => {
    e.preventDefault();
    if (!chatInput.trim()) return;

    const userMessage = chatInput;
    setChatLog(prev => [...prev, { isUser: true, text: userMessage }]);
    setChatInput("");

    // Simulate AI response based on keyword matching
    setTimeout(() => {
      let reply;
      const lower = userMessage.toLowerCase();
      if (lower.includes("balance") || lower.includes("money")) {
        reply = `Your available Savings balance is Rs. ${savingsBal.toLocaleString('en-IN')}, and Current balance is Rs. ${currentBal.toLocaleString('en-IN')}.`;
      } else if (lower.includes("limit") || lower.includes("card")) {
        reply = `Your current daily debit card spending limit is set to Rs. ${cardLimit.toLocaleString('en-IN')}. You can modify this in the Cards panel.`;
      } else if (lower.includes("freeze") || lower.includes("block")) {
        reply = "You can immediately freeze or block your active debit card under the Card Control section to protect against fraud.";
      } else if (lower.includes("insight") || lower.includes("spend")) {
        reply = "AI Financial Insight: 42% of your expenses this week went into Food and Dining. We suggest moving Rs. 5,000 into RD to save better.";
      } else {
        reply = "I'm Aura, your AI Banking companion. I can help track your balances, set limits, or give budget analysis. What else can I assist with?";
      }
      setChatLog(prev => [...prev, { isUser: false, text: reply }]);
    }, 800);
  };

  return (
    <div className="min-h-screen flex bg-[#0f172a] text-slate-100 font-sans relative">
      
      {/* Sidebar Navigation */}
      <aside className="w-64 border-r border-white/5 bg-[#0b0f19] flex flex-col justify-between p-6 select-none">
        <div className="space-y-8">
          <div className="flex items-center space-x-2">
            <Landmark className="h-7 w-7 text-indigo-500" />
            <span className="font-outfit font-bold text-xl tracking-wider text-white">AURA</span>
          </div>

          <nav className="space-y-1">
            <button 
              onClick={() => setActiveTab("accounts")}
              className={`w-full flex items-center space-x-3 px-4 py-3 rounded-lg text-sm font-semibold transition ${activeTab === "accounts" ? "bg-indigo-600 text-white" : "text-slate-400 hover:bg-white/5 hover:text-white"}`}
            >
              <Wallet className="h-4.5 w-4.5" />
              <span>Accounts</span>
            </button>

            <button 
              onClick={() => setActiveTab("transfer")}
              className={`w-full flex items-center space-x-3 px-4 py-3 rounded-lg text-sm font-semibold transition ${activeTab === "transfer" ? "bg-indigo-600 text-white" : "text-slate-400 hover:bg-white/5 hover:text-white"}`}
            >
              <Send className="h-4.5 w-4.5" />
              <span>Send Money</span>
            </button>

            <button 
              onClick={() => setActiveTab("cards")}
              className={`w-full flex items-center space-x-3 px-4 py-3 rounded-lg text-sm font-semibold transition ${activeTab === "cards" ? "bg-indigo-600 text-white" : "text-slate-400 hover:bg-white/5 hover:text-white"}`}
            >
              <CreditCard className="h-4.5 w-4.5" />
              <span>Cards</span>
            </button>

            <button 
              onClick={() => setActiveTab("history")}
              className={`w-full flex items-center space-x-3 px-4 py-3 rounded-lg text-sm font-semibold transition ${activeTab === "history" ? "bg-indigo-600 text-white" : "text-slate-400 hover:bg-white/5 hover:text-white"}`}
            >
              <History className="h-4.5 w-4.5" />
              <span>History</span>
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
      <main className="flex-1 p-8 overflow-y-auto max-w-5xl mx-auto space-y-8">
        
        {/* Header Block */}
        <div className="flex items-center justify-between border-b border-white/5 pb-6">
          <div>
            <h1 className="text-2xl font-bold text-white">Welcome back, John Doe</h1>
            <p className="text-xs text-slate-400">Account status: APPROVED | Secured by PCI-DSS encryption standards</p>
          </div>
          <button 
            onClick={() => setShowChat(true)}
            className="flex items-center space-x-2 px-4 py-2 rounded-lg bg-indigo-500/10 border border-indigo-500/20 text-indigo-300 hover:bg-indigo-500/20 text-sm font-medium transition"
          >
            <Bot className="h-4 w-4" />
            <span>Talk to Aura AI</span>
          </button>
        </div>

        {/* Dynamic Alert Banner */}
        {successMsg && (
          <div className="px-4 py-3 rounded-lg border border-emerald-500/20 bg-emerald-500/10 text-sm text-emerald-300 flex items-center space-x-2">
            <CheckCircle2 className="h-4.5 w-4.5 text-emerald-400 shrink-0" />
            <span>{successMsg}</span>
          </div>
        )}
        {errorMsg && (
          <div className="px-4 py-3 rounded-lg border border-red-500/20 bg-red-500/10 text-sm text-red-300 flex items-center space-x-2">
            <ShieldAlert className="h-4.5 w-4.5 text-red-400 shrink-0" />
            <span>{errorMsg}</span>
          </div>
        )}

        {/* Tab 1: Accounts Overview */}
        {activeTab === "accounts" && (
          <div className="space-y-8">
            {/* Balances list */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div className="glass-card p-6 flex flex-col space-y-2 border-l-4 border-indigo-500">
                <span className="text-xs text-slate-400 uppercase tracking-wider font-semibold">Savings Balance</span>
                <span className="text-2xl font-bold text-white">Rs. {savingsBal.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
                <span className="text-[10px] text-slate-500">ACC10029384 • Available Instantly</span>
              </div>
              <div className="glass-card p-6 flex flex-col space-y-2 border-l-4 border-emerald-500">
                <span className="text-xs text-slate-400 uppercase tracking-wider font-semibold">Current Balance</span>
                <span className="text-2xl font-bold text-white">Rs. {currentBal.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
                <span className="text-[10px] text-slate-500">ACC90823481 • Corporate Wallet</span>
              </div>
              <div className="glass-card p-6 flex flex-col space-y-2 border-l-4 border-purple-500">
                <span className="text-xs text-slate-400 uppercase tracking-wider font-semibold">Fixed Deposits</span>
                <span className="text-2xl font-bold text-white">Rs. {fdBal.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
                <span className="text-[10px] text-slate-500">FD309248 • Earning 7.25% p.a.</span>
              </div>
            </div>

            {/* Spending Insights Chart Section */}
            <div className="glass-card p-6 space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="font-bold text-white">Expense Distribution</h3>
                  <p className="text-[10px] text-slate-400">Monthly categorized expenses processed by AI analytics</p>
                </div>
                <div className="flex items-center space-x-2 text-xs text-indigo-400">
                  <Sparkles className="h-4 w-4" />
                  <span>Optimal budget suggested</span>
                </div>
              </div>

              {/* Simple layout simulating a bar chart */}
              <div className="space-y-3 pt-2">
                <div>
                  <div className="flex justify-between text-xs mb-1">
                    <span>Food & Dining (32%)</span>
                    <span>Rs. 14,464.00</span>
                  </div>
                  <div className="w-full bg-slate-950 h-2 rounded-full overflow-hidden">
                    <div className="bg-indigo-500 h-full" style={{ width: "32%" }} />
                  </div>
                </div>
                <div>
                  <div className="flex justify-between text-xs mb-1">
                    <span>House Rent (28%)</span>
                    <span>Rs. 12,656.00</span>
                  </div>
                  <div className="w-full bg-slate-950 h-2 rounded-full overflow-hidden">
                    <div className="bg-purple-500 h-full" style={{ width: "28%" }} />
                  </div>
                </div>
                <div>
                  <div className="flex justify-between text-xs mb-1">
                    <span>Investments / FD (25%)</span>
                    <span>Rs. 11,300.00</span>
                  </div>
                  <div className="w-full bg-slate-950 h-2 rounded-full overflow-hidden">
                    <div className="bg-emerald-500 h-full" style={{ width: "25%" }} />
                  </div>
                </div>
                <div>
                  <div className="flex justify-between text-xs mb-1">
                    <span>Others / Shopping (15%)</span>
                    <span>Rs. 6,780.00</span>
                  </div>
                  <div className="w-full bg-slate-950 h-2 rounded-full overflow-hidden">
                    <div className="bg-pink-500 h-full" style={{ width: "15%" }} />
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Tab 2: Send Money Form */}
        {activeTab === "transfer" && (
          <div className="max-w-xl mx-auto glass-card p-8 space-y-6">
            <h3 className="text-lg font-bold text-white flex items-center space-x-2">
              <Send className="h-5 w-5 text-indigo-500" />
              <span>Initiate Funds Transfer</span>
            </h3>

            <form onSubmit={handleTransfer} className="space-y-5">
              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Recipient Account No</label>
                <input 
                  type="text" 
                  placeholder="e.g. ACC1009842" 
                  value={recipientAcc}
                  onChange={(e) => setRecipientAcc(e.target.value)}
                  className="w-full px-4 py-2.5 rounded-lg bg-slate-900/60 border border-white/10 text-white focus:outline-none focus:border-indigo-500 text-sm"
                  required
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Amount (INR)</label>
                  <input 
                    type="number" 
                    placeholder="0.00" 
                    value={amount}
                    onChange={(e) => setAmount(e.target.value)}
                    className="w-full px-4 py-2.5 rounded-lg bg-slate-900/60 border border-white/10 text-white focus:outline-none focus:border-indigo-500 text-sm"
                    required
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Payment Channel</label>
                  <select 
                    value={channel}
                    onChange={(e) => setChannel(e.target.value)}
                    className="w-full px-4 py-2.5 rounded-lg bg-slate-900/60 border border-white/10 text-white focus:outline-none focus:border-indigo-500 text-sm cursor-pointer"
                  >
                    <option value="UPI">UPI (Immediate)</option>
                    <option value="IMPS">IMPS (Real-time)</option>
                    <option value="NEFT">NEFT (Batch settlement)</option>
                    <option value="RTGS">RTGS (High Value)</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Description / Purpose</label>
                <input 
                  type="text" 
                  placeholder="e.g. Rent Payment" 
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  className="w-full px-4 py-2.5 rounded-lg bg-slate-900/60 border border-white/10 text-white focus:outline-none focus:border-indigo-500 text-sm"
                />
              </div>

              <button 
                type="submit" 
                className="w-full py-3 rounded-lg bg-indigo-600 hover:bg-indigo-500 text-white font-semibold transition glow-button text-sm"
              >
                Authorize & Broadcast Payment
              </button>
            </form>
          </div>
        )}

        {/* Tab 3: Cards Management */}
        {activeTab === "cards" && (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
            {/* Visual Card Component */}
            <div className="space-y-4">
              <h3 className="text-lg font-bold text-white">Your Card Controls</h3>
              
              {/* Premium Gradient Credit Card representation */}
              <div className="relative w-full h-48 rounded-2xl bg-gradient-to-tr from-indigo-800 via-indigo-600 to-purple-600 p-6 flex flex-col justify-between overflow-hidden shadow-2xl border border-white/10 select-none">
                <div className="absolute top-[-30%] right-[-10%] w-48 h-48 rounded-full bg-white/5 blur-[50px]" />
                <div className="flex justify-between items-start">
                  <div className="flex flex-col">
                    <span className="text-[10px] text-indigo-200 tracking-wider">DEBIT CARD</span>
                    <span className="text-xs font-semibold mt-1">AURA SECURED</span>
                  </div>
                  <Landmark className="h-6 w-6 text-white/80" />
                </div>

                <div className="space-y-2">
                  <div className="text-lg font-mono tracking-widest text-white">4111 9840 2104 3821</div>
                  <div className="flex justify-between items-center text-[10px] text-indigo-100">
                    <div>
                      <span>CARD HOLDER</span>
                      <div className="font-semibold text-xs text-white">JOHN DOE</div>
                    </div>
                    <div>
                      <span>VALID THRU</span>
                      <div className="font-semibold text-xs text-white">12/31</div>
                    </div>
                    <div>
                      <span>CVV</span>
                      <div className="font-semibold text-xs text-white">***</div>
                    </div>
                  </div>
                </div>
              </div>

              {/* Status control */}
              <div className="glass-card p-6 flex items-center justify-between">
                <div className="flex items-center space-x-3">
                  <div className={`h-8 w-8 rounded-full flex items-center justify-center ${cardStatus === "ACTIVE" ? "bg-emerald-500/10 text-emerald-400" : "bg-red-500/10 text-red-400"}`}>
                    {cardStatus === "ACTIVE" ? <Unlock className="h-4.5 w-4.5" /> : <Lock className="h-4.5 w-4.5" />}
                  </div>
                  <div>
                    <div className="font-semibold text-sm">Status: {cardStatus}</div>
                    <p className="text-[10px] text-slate-400">Locking disables ATM and Web transactions instantly</p>
                  </div>
                </div>
                <button 
                  onClick={() => setCardStatus(prev => prev === "ACTIVE" ? "FROZEN" : "ACTIVE")}
                  className={`px-4 py-2 rounded-lg text-xs font-semibold border transition ${cardStatus === "ACTIVE" ? "border-red-500/20 bg-red-500/10 text-red-400 hover:bg-red-500/20" : "border-emerald-500/20 bg-emerald-500/10 text-emerald-400 hover:bg-emerald-500/20"}`}
                >
                  {cardStatus === "ACTIVE" ? "Freeze Card" : "Unfreeze Card"}
                </button>
              </div>
            </div>

            {/* Slider and PIN Form */}
            <div className="space-y-6">
              {/* Daily spending limit slider */}
              <div className="glass-card p-6 space-y-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-2 text-sm font-semibold">
                    <Sliders className="h-4.5 w-4.5 text-indigo-400" />
                    <span>Daily Transaction Limit</span>
                  </div>
                  <span className="text-xs text-indigo-400 font-mono font-bold">Rs. {cardLimit.toLocaleString('en-IN')}</span>
                </div>
                <input 
                  type="range" 
                  min="10000" 
                  max="100000" 
                  step="5000" 
                  value={cardLimit}
                  onChange={(e) => setCardLimit(parseInt(e.target.value))}
                  className="w-full accent-indigo-500 cursor-pointer"
                />
                <p className="text-[10px] text-slate-400 text-center">Changes are updated instantly across all virtual terminals</p>
              </div>

              {/* Secure pin reset form */}
              <div className="glass-card p-6 space-y-4">
                <h4 className="font-semibold text-sm text-white">Reset Secure ATM PIN</h4>
                <form onSubmit={handlePinChange} className="space-y-4">
                  <div className="grid grid-cols-2 gap-4">
                    <input 
                      type="password" 
                      placeholder="Old PIN" 
                      maxLength={4}
                      value={oldPin}
                      onChange={(e) => setOldPin(e.target.value)}
                      className="px-4 py-2 rounded-lg bg-slate-900/60 border border-white/10 text-white text-xs focus:outline-none focus:border-indigo-500"
                      required
                    />
                    <input 
                      type="password" 
                      placeholder="New PIN" 
                      maxLength={4}
                      value={newPin}
                      onChange={(e) => setNewPin(e.target.value)}
                      className="px-4 py-2 rounded-lg bg-slate-900/60 border border-white/10 text-white text-xs focus:outline-none focus:border-indigo-500"
                      required
                    />
                  </div>
                  <button type="submit" className="w-full py-2.5 rounded-lg bg-white/5 border border-white/10 hover:bg-white/10 text-xs font-semibold text-slate-300 transition">
                    Update PIN Number
                  </button>
                </form>
              </div>
            </div>
          </div>
        )}

        {/* Tab 4: Ledger History */}
        {activeTab === "history" && (
          <div className="glass-card p-6 space-y-6">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="font-bold text-white">Transaction Logs</h3>
                <p className="text-[10px] text-slate-400">Ledger balance updates synchronized with Oracle packages</p>
              </div>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm text-slate-300">
                <thead>
                  <tr className="border-b border-white/5 text-slate-500 text-xs uppercase font-semibold">
                    <th className="pb-3">Reference</th>
                    <th className="pb-3">Details</th>
                    <th className="pb-3">Channel</th>
                    <th className="pb-3">Date</th>
                    <th className="pb-3 text-right">Amount</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-white/5">
                  {txns.map(tx => (
                    <tr key={tx.id} className="hover:bg-white/5 transition">
                      <td className="py-4 font-mono text-xs">{tx.ref}</td>
                      <td className="py-4">
                        <div className="font-semibold text-white">{tx.desc}</div>
                        <div className="text-[10px] text-slate-500">Beneficiary: {tx.dest}</div>
                      </td>
                      <td className="py-4 text-xs font-medium">{tx.channel}</td>
                      <td className="py-4 text-xs text-slate-500">{tx.date}</td>
                      <td className={`py-4 text-right font-bold ${tx.type === "DEBIT" ? "text-red-400" : "text-emerald-400"}`}>
                        {tx.type === "DEBIT" ? "-" : "+"} Rs. {tx.amount.toFixed(2)}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </main>

      {/* Floating Chat Trigger Button */}
      {!showChat && (
        <button 
          onClick={() => setShowChat(true)}
          className="fixed bottom-6 right-6 p-4 rounded-full bg-indigo-600 text-white shadow-2xl hover:bg-indigo-500 transition duration-300 glow-button z-40"
        >
          <MessageSquare className="h-6 w-6" />
        </button>
      )}

      {/* Floating Chat Drawer UI */}
      {showChat && (
        <div className="fixed bottom-6 right-6 w-96 h-[480px] rounded-2xl bg-slate-900 border border-white/10 flex flex-col justify-between shadow-2xl overflow-hidden z-50">
          {/* Header */}
          <div className="px-4 py-3 bg-[#0b0f19] border-b border-white/5 flex items-center justify-between">
            <div className="flex items-center space-x-2">
              <Bot className="h-5 w-5 text-indigo-500" />
              <div>
                <div className="text-xs font-bold text-white">Aura AI Agent</div>
                <div className="text-[9px] text-emerald-400 flex items-center space-x-1">
                  <span className="h-1.5 w-1.5 rounded-full bg-emerald-500 animate-pulse" />
                  <span>Online Assistance</span>
                </div>
              </div>
            </div>
            <button onClick={() => setShowChat(false)} className="text-slate-400 hover:text-white transition">
              <X className="h-4.5 w-4.5" />
            </button>
          </div>

          {/* Messages Logs */}
          <div className="flex-1 p-4 space-y-4 overflow-y-auto text-xs">
            {chatLog.map((logItem, idx) => (
              <div key={idx} className={`flex ${logItem.isUser ? "justify-end" : "justify-start"}`}>
                <div className={`max-w-[80%] rounded-xl px-3.5 py-2.5 leading-relaxed ${logItem.isUser ? "bg-indigo-600 text-white rounded-br-none" : "bg-white/5 text-slate-300 rounded-bl-none border border-white/5"}`}>
                  {logItem.text}
                </div>
              </div>
            ))}
          </div>

          {/* Form input */}
          <form onSubmit={sendChatMessage} className="p-3 bg-[#0b0f19] border-t border-white/5 flex space-x-2">
            <input 
              type="text" 
              placeholder="Ask about spending reports, balances..." 
              value={chatInput}
              onChange={(e) => setChatInput(e.target.value)}
              className="flex-grow px-3 py-2 rounded-lg bg-slate-950 border border-white/10 text-white text-xs focus:outline-none focus:border-indigo-500"
            />
            <button type="submit" className="p-2 rounded-lg bg-indigo-600 hover:bg-indigo-500 text-white transition">
              <Send className="h-3.5 w-3.5" />
            </button>
          </form>
        </div>
      )}
    </div>
  );
}
