import React, { useState, useEffect, useRef } from 'react';
import { 
  Bot, 
  Send, 
  User, 
  Sparkles, 
  Cpu, 
  RefreshCw, 
  ShieldCheck, 
  Wrench,
  Trash2,
  Terminal,
  ArrowRight
} from 'lucide-react';
import { sendChatMessage, getActiveMcps } from './services/chatService';
import './index.css';

export default function ChatApp({ token: propToken, user: propUser }) {
  const token = propToken || localStorage.getItem('auth_token') || '';
  const currentUser = propUser || (localStorage.getItem('auth_user') ? JSON.parse(localStorage.getItem('auth_user')) : { name: 'Pengguna' });

  // Chat conversation is EPHEMERAL in React state (cleared on new session / unmount)
  const [messages, setMessages] = useState([
    {
      id: 'welcome-1',
      role: 'model',
      content: `Halo **${currentUser.name || 'Pengguna'}**! 👋 Saya asisten AI cerdas yang terhubung dengan **Gemini LLM** dan sistem **MCP (Model Context Protocol)**.\n\nRiwayat percakapan ini bersifat *sesi sementara* (tidak disimpan di database). Anda dapat menanyakan data siswa atau informasi lainnya secara langsung.`,
      toolsUsed: [],
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    }
  ]);

  const [inputPrompt, setInputPrompt] = useState('');
  const [loading, setLoading] = useState(false);
  const [activeMcps, setActiveMcps] = useState([]);
  const messagesEndRef = useRef(null);

  // Load active MCPs on mount
  useEffect(() => {
    if (token) {
      loadActiveMcps();
    }
  }, [token]);

  // Auto-scroll to bottom of chat
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, loading]);

  const loadActiveMcps = async () => {
    const list = await getActiveMcps(token);
    setActiveMcps(list);
  };

  const handleSendMessage = async (e) => {
    if (e) e.preventDefault();
    const trimmed = inputPrompt.trim();
    if (!trimmed || loading) return;

    const userMsgId = 'msg-' + Date.now();
    const timeNow = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    // 1. Append user message to UI state
    const newUserMsg = {
      id: userMsgId,
      role: 'user',
      content: trimmed,
      timestamp: timeNow
    };

    setMessages(prev => [...prev, newUserMsg]);
    setInputPrompt('');
    setLoading(true);

    try {
      // 2. Format conversation history for Gemini (excluding welcome msg)
      const history = messages
        .filter(m => m.id !== 'welcome-1')
        .map(m => ({
          role: m.role === 'user' ? 'user' : 'model',
          content: m.content
        }));

      // 3. Send to msf_ai carrying Bearer Token!
      const responseData = await sendChatMessage(trimmed, history, token);

      const aiMsgId = 'ai-' + Date.now();
      const newAiMsg = {
        id: aiMsgId,
        role: 'model',
        content: responseData.reply || 'Tidak ada respon yang diterima.',
        toolsUsed: responseData.toolsUsed || [],
        toolExecuted: responseData.toolExecuted || false,
        model: responseData.model,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      };

      setMessages(prev => [...prev, newAiMsg]);
    } catch (err) {
      const errorMsgId = 'err-' + Date.now();
      setMessages(prev => [
        ...prev,
        {
          id: errorMsgId,
          role: 'model',
          content: `⚠️ Terjadi kendala saat menghubungi AI: ${err.message}`,
          isError: true,
          timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
        }
      ]);
    } finally {
      setLoading(false);
    }
  };

  const handleClearSession = () => {
    if (window.confirm('Bersihkan percakapan sesi ini?')) {
      setMessages([
        {
          id: 'welcome-' + Date.now(),
          role: 'model',
          content: 'Sesi percakapan telah dibersihkan. Silakan ajukan pertanyaan baru!',
          timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
        }
      ]);
    }
  };

  const handleQuickPrompt = (promptText) => {
    setInputPrompt(promptText);
  };

  return (
    <div className="mfe-module-container chat-layout-container">
      {/* Header & Active MCP Bar */}
      <div className="chat-app-header">
        <div className="title-area">
          <div className="icon-wrapper bg-blue-50 text-blue-600">
            <Bot size={28} />
          </div>
          <div>
            <div className="flex-title-row">
              <h1 className="module-title">AI Assistant & MCP Chat</h1>
              <span className="session-badge">Sesi Ephemeral (In-Memory)</span>
            </div>
            <p className="module-subtitle">Didukung oleh Google Gemini LLM & Akses Live Tool via MCP Protocol</p>
          </div>
        </div>

        <div className="header-actions">
          <button 
            type="button" 
            onClick={handleClearSession} 
            className="btn-clear-chat"
            title="Bersihkan riwayat percakapan sesi ini"
          >
            <Trash2 size={16} />
            <span>Bersihkan Sesi</span>
          </button>
        </div>
      </div>

      {/* Active MCPs Banner */}
      <div className="active-mcp-status-bar">
        <div className="mcp-status-label">
          <Cpu size={16} className="text-emerald-500" />
          <span>Status MCP Plugged-In:</span>
        </div>
        <div className="mcp-badges-list">
          {activeMcps.length === 0 ? (
            <span className="no-mcp-badge">Tidak ada MCP yang aktif (Plug-in di menu Konfigurasi AI)</span>
          ) : (
            activeMcps.map(mcp => (
              <span key={mcp.id} className="active-mcp-pill">
                <span className="online-indicator"></span>
                <strong>{mcp.name}</strong> ({mcp.url})
              </span>
            ))
          )}
        </div>
        <button onClick={loadActiveMcps} className="btn-refresh-mcp" title="Refresh status MCP">
          <RefreshCw size={13} />
        </button>
      </div>

      {/* Chat Messages Window */}
      <div className="chat-window">
        {messages.map((msg) => {
          const isUser = msg.role === 'user';
          return (
            <div key={msg.id} className={`chat-message-row ${isUser ? 'user-row' : 'model-row'}`}>
              <div className="avatar-col">
                {isUser ? (
                  <div className="avatar user-avatar">
                    <User size={18} />
                  </div>
                ) : (
                  <div className="avatar model-avatar">
                    <Bot size={18} />
                  </div>
                )}
              </div>

              <div className="message-content-col">
                <div className="message-meta">
                  <span className="sender-name">{isUser ? (currentUser.name || 'Anda') : 'Gemini AI'}</span>
                  <span className="message-time">{msg.timestamp}</span>
                  {msg.model && <span className="model-tag">{msg.model}</span>}
                </div>

                {/* Tool Invocation Notice */}
                {msg.toolsUsed && msg.toolsUsed.length > 0 && (
                  <div className="tool-execution-notice">
                    <Wrench size={14} className="text-amber-600" />
                    <span>MCP Tool Dijalankan: <code>{msg.toolsUsed.join(', ')}</code></span>
                    <span className="token-relay-pill">
                      <ShieldCheck size={12} /> Bearer Token Terbawa
                    </span>
                  </div>
                )}

                <div className={`message-bubble ${isUser ? 'user-bubble' : 'model-bubble'} ${msg.isError ? 'error-bubble' : ''}`}>
                  <div className="message-text">
                    {msg.content.split('\n').map((line, i) => (
                      <p key={i}>{line}</p>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          );
        })}

        {loading && (
          <div className="chat-message-row model-row">
            <div className="avatar-col">
              <div className="avatar model-avatar pulse">
                <Bot size={18} />
              </div>
            </div>
            <div className="message-content-col">
              <div className="message-bubble model-bubble typing-bubble">
                <div className="typing-dots">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
                <span className="typing-text">Gemini sedang berpikir dan menghubungi MCP...</span>
              </div>
            </div>
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>

      {/* Prompt Suggestions */}
      <div className="quick-prompts-bar">
        <span className="quick-label">Saran Prompt:</span>
        <button 
          type="button" 
          onClick={() => handleQuickPrompt('Tolong tampilkan daftar data siswa yang terdaftar saat ini')}
          className="quick-chip"
        >
          🎓 Tampilkan data siswa
        </button>
        <button 
          type="button" 
          onClick={() => handleQuickPrompt('Cari data siswa yang bernama Budi')}
          className="quick-chip"
        >
          🔍 Cari siswa "Budi"
        </button>
        <button 
          type="button" 
          onClick={() => handleQuickPrompt('Ada berapa total siswa di sistem? Tampilkan rinciannya')}
          className="quick-chip"
        >
          📊 Rincian total siswa
        </button>
      </div>

      {/* Input Area */}
      <form onSubmit={handleSendMessage} className="chat-input-area">
        <input 
          type="text" 
          value={inputPrompt}
          onChange={(e) => setInputPrompt(e.target.value)}
          placeholder="Ketik pertanyaan untuk AI (misal: 'Tolong carikan data siswa Budi')..."
          className="chat-text-input"
          disabled={loading}
          autoFocus
        />
        <button 
          type="submit" 
          disabled={loading || !inputPrompt.trim()}
          className="btn-send-message"
        >
          {loading ? <RefreshCw size={18} className="spin" /> : <Send size={18} />}
          <span>Kirim</span>
        </button>
      </form>
    </div>
  );
}
