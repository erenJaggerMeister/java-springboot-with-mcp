import React, { useState, useEffect } from 'react';
import { 
  Key, 
  Cpu, 
  Settings, 
  PlusCircle, 
  Trash2, 
  Power, 
  CheckCircle2, 
  XCircle, 
  RefreshCw, 
  ExternalLink,
  ShieldCheck,
  Radio,
  Sliders
} from 'lucide-react';
import { 
  getGeminiConfig, 
  saveGeminiConfig, 
  testGeminiApiKey, 
  getAvailableModels,
  getAllMcpConfigs, 
  createMcpConfig, 
  toggleMcpStatus, 
  deleteMcpConfig, 
  testMcpConnection 
} from './services/aiConfigService';
import './index.css';

export default function AiConfigApp({ token: propToken, user: propUser }) {
  const token = propToken || localStorage.getItem('auth_token') || '';
  const [activeTab, setActiveTab] = useState('gemini'); // 'gemini' | 'mcp'

  // Tab 1 States
  const [apiKey, setApiKey] = useState('');
  const [model, setModel] = useState('gemini-1.5-flash');
  const [modelList, setModelList] = useState([
    'gemini-1.5-flash',
    'gemini-1.5-pro',
    'gemini-2.0-flash'
  ]);
  const [fetchingModels, setFetchingModels] = useState(false);
  const [geminiLoading, setGeminiLoading] = useState(false);
  const [geminiStatus, setGeminiStatus] = useState(null); // { success: boolean, message: string }

  // Tab 2 States
  const [mcpList, setMcpList] = useState([]);
  const [mcpName, setMcpName] = useState('');
  const [mcpUrl, setMcpUrl] = useState('');
  const [mcpLoading, setMcpLoading] = useState(false);
  const [mcpTestResults, setMcpTestResults] = useState({}); // { [mcpId]: { success: boolean, msg: string } }
  const [formError, setFormError] = useState('');

  // Initial Load
  useEffect(() => {
    if (!token) return;
    loadGeminiConfig();
    loadMcpConfigs();
  }, [token]);

  const loadGeminiConfig = async () => {
    try {
      const config = await getGeminiConfig(token);
      if (config.gemini_api_key) setApiKey(config.gemini_api_key);
      if (config.gemini_model) {
        setModel(config.gemini_model);
        setModelList(prev => prev.includes(config.gemini_model) ? prev : [...prev, config.gemini_model]);
      }
    } catch (err) {
      console.warn('Gagal memuat konfigurasi Gemini:', err);
    }
  };

  const loadMcpConfigs = async () => {
    setMcpLoading(true);
    try {
      const data = await getAllMcpConfigs(token);
      setMcpList(data);
    } catch (err) {
      console.warn('Gagal memuat daftar MCP:', err);
    } finally {
      setMcpLoading(false);
    }
  };

  const handleFetchModels = async () => {
    if (!apiKey) return;
    setFetchingModels(true);
    try {
      const models = await getAvailableModels(apiKey, token);
      if (models && models.length > 0) {
        const merged = Array.from(new Set([...modelList, ...models]));
        setModelList(merged);
        if (!merged.includes(model)) {
          setModel(merged[0]);
        }
        setGeminiStatus({ success: true, message: `Berhasil memuat ${models.length} model dari Google AI.` });
      }
    } catch (err) {
      console.warn('Gagal memuat daftar model dari Google:', err);
    } finally {
      setFetchingModels(false);
    }
  };

  // --- TAB 1 ACTIONS ---
  const handleSaveGemini = async (e) => {
    e.preventDefault();
    setGeminiLoading(true);
    setGeminiStatus(null);
    try {
      await saveGeminiConfig(apiKey, model, token);
      setGeminiStatus({ success: true, message: `Konfigurasi Gemini (API Key & Model '${model}') berhasil disimpan ke database!` });
    } catch (err) {
      setGeminiStatus({ success: false, message: err.message || 'Gagal menyimpan konfigurasi' });
    } finally {
      setGeminiLoading(false);
    }
  };

  const handleTestGemini = async () => {
    setGeminiLoading(true);
    setGeminiStatus(null);
    try {
      const res = await testGeminiApiKey(apiKey, model, token);
      const isSuccess = res.data?.success ?? (res.data === true);
      const message = res.data?.message || (isSuccess ? 'Koneksi ke Gemini AI Berhasil! API Key dan Model valid.' : 'Koneksi gagal. Periksa kembali API Key Anda.');

      if (res.data?.availableModels && res.data.availableModels.length > 0) {
        const merged = Array.from(new Set([...modelList, ...res.data.availableModels]));
        setModelList(merged);
      }

      setGeminiStatus({
        success: isSuccess,
        message: message
      });
    } catch (err) {
      setGeminiStatus({ success: false, message: 'Gagal menguji: ' + err.message });
    } finally {
      setGeminiLoading(false);
    }
  };

  // --- TAB 2 ACTIONS ---
  const handleAddMcp = async (e) => {
    e.preventDefault();
    if (!mcpName.trim() || !mcpUrl.trim()) {
      setFormError('Nama MCP dan URL MCP wajib diisi');
      return;
    }
    setFormError('');
    setMcpLoading(true);
    try {
      await createMcpConfig({ name: mcpName.trim(), url: mcpUrl.trim(), isActive: true }, token);
      setMcpName('');
      setMcpUrl('');
      await loadMcpConfigs();
    } catch (err) {
      setFormError(err.message || 'Gagal menambahkan MCP');
    } finally {
      setMcpLoading(false);
    }
  };

  const handleToggleMcp = async (id, currentStatus) => {
    try {
      await toggleMcpStatus(id, !currentStatus, token);
      setMcpList(prev => prev.map(item => item.id === id ? { ...item, isActive: !currentStatus } : item));
    } catch (err) {
      alert('Gagal mengubah status MCP: ' + err.message);
    }
  };

  const handleDeleteMcp = async (id) => {
    if (!window.confirm('Yakin ingin menghapus MCP ini?')) return;
    try {
      await deleteMcpConfig(id, token);
      setMcpList(prev => prev.filter(item => item.id !== id));
    } catch (err) {
      alert('Gagal menghapus MCP: ' + err.message);
    }
  };

  const handleTestMcp = async (mcp) => {
    setMcpTestResults(prev => ({ ...prev, [mcp.id]: { loading: true } }));
    try {
      const res = await testMcpConnection(mcp.url, token);
      setMcpTestResults(prev => ({
        ...prev,
        [mcp.id]: {
          loading: false,
          success: res.connected,
          message: res.message + (res.tools?.length ? ` [Tool: ${res.tools.join(', ')}]` : '')
        }
      }));
    } catch (err) {
      setMcpTestResults(prev => ({
        ...prev,
        [mcp.id]: {
          loading: false,
          success: false,
          message: 'Error: ' + err.message
        }
      }));
    }
  };

  return (
    <div className="mfe-module-container">
      {/* Header */}
      <div className="module-header">
        <div className="title-area">
          <div className="icon-wrapper bg-indigo-50 text-indigo-600">
            <Sliders size={26} />
          </div>
          <div>
            <h1 className="module-title">Konfigurasi AI & MCP Hub</h1>
            <p className="module-subtitle">Kelola API Key Google Gemini LLM dan Plug-in / Plug-out MCP Server</p>
          </div>
        </div>
      </div>

      {/* Tabs Nav */}
      <div className="config-tabs-nav">
        <button 
          className={`config-tab-btn ${activeTab === 'gemini' ? 'active' : ''}`}
          onClick={() => setActiveTab('gemini')}
        >
          <Key size={18} />
          <span>Tab 1: Pengaturan Gemini API Key</span>
        </button>
        <button 
          className={`config-tab-btn ${activeTab === 'mcp' ? 'active' : ''}`}
          onClick={() => setActiveTab('mcp')}
        >
          <Cpu size={18} />
          <span>Tab 2: Plug-in & Plug-out MCP</span>
          <span className="tab-counter-badge">{mcpList.filter(m => m.isActive).length}/{mcpList.length} Aktif</span>
        </button>
      </div>

      {/* Tab 1 Content: Gemini API Key */}
      {activeTab === 'gemini' && (
        <div className="tab-pane-content card-box">
          <div className="pane-header">
            <h3><Key size={20} className="inline-icon text-indigo-600" /> Kredensial Google Gemini AI</h3>
            <p className="text-muted">API Key digunakan oleh orkestrator <code>msf_ai</code> untuk memproses prompt dan mengeksekusi function calling dengan MCP.</p>
          </div>

          <form onSubmit={handleSaveGemini} className="config-form">
            <div className="form-group">
              <label htmlFor="apiKeyInput">Google Gemini API Key:</label>
              <div className="input-group">
                <input 
                  id="apiKeyInput"
                  type="text" 
                  value={apiKey} 
                  onChange={(e) => setApiKey(e.target.value)}
                  placeholder="Masukkan API Key Gemini..."
                  className="form-control font-mono"
                  required
                />
              </div>
              <small className="help-text">Dapatkan API key dari Google AI Studio atau konfigurasi via environment variable.</small>
            </div>

            <div className="form-group">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.25rem' }}>
                <label htmlFor="modelSelect" style={{ marginBottom: 0 }}>Model LLM Terpilih:</label>
                <button
                  type="button"
                  onClick={handleFetchModels}
                  disabled={fetchingModels || !apiKey}
                  title="Ambil daftar model yang didukung dari akun Google Gemini Anda"
                  style={{ 
                    background: 'none', 
                    border: 'none', 
                    cursor: fetchingModels || !apiKey ? 'not-allowed' : 'pointer', 
                    color: '#4f46e5',
                    fontSize: '0.8rem',
                    display: 'inline-flex',
                    alignItems: 'center',
                    gap: '4px',
                    padding: 0
                  }}
                >
                  <RefreshCw size={12} className={fetchingModels ? "spin" : ""} />
                  <span>{fetchingModels ? 'Memuat model...' : 'Muat Model dari Google'}</span>
                </button>
              </div>
              <select 
                id="modelSelect"
                value={model} 
                onChange={(e) => setModel(e.target.value)}
                className="form-control font-mono"
              >
                {modelList.map((m) => (
                  <option key={m} value={m}>
                    {m} {m === 'gemini-1.5-flash' ? '(Rekomendasi - Cepat & Stabil)' : m === 'gemini-1.5-pro' ? '(Penalaran Kompleks)' : m === 'gemini-2.0-flash' ? '(Generasi Baru)' : ''}
                  </option>
                ))}
                {!modelList.includes(model) && (
                  <option value={model}>{model}</option>
                )}
              </select>
              <small className="help-text">Model yang dipilih akan digunakan oleh <code>msf_ai</code> untuk chat dan eksekusi function calling dengan MCP.</small>
            </div>

            {geminiStatus && (
              <div className={`status-banner ${geminiStatus.success ? 'banner-success' : 'banner-error'}`}>
                {geminiStatus.success ? <CheckCircle2 size={18} /> : <XCircle size={18} />}
                <span>{geminiStatus.message}</span>
              </div>
            )}

            <div className="form-actions">
              <button 
                type="submit" 
                className="btn btn-primary"
                disabled={geminiLoading}
              >
                {geminiLoading ? <RefreshCw size={16} className="spin" /> : <Settings size={16} />}
                Simpan Konfigurasi
              </button>

              <button 
                type="button" 
                onClick={handleTestGemini}
                className="btn btn-secondary"
                disabled={geminiLoading}
              >
                {geminiLoading ? <RefreshCw size={16} className="spin" /> : <Radio size={16} />}
                Test Koneksi LLM
              </button>
            </div>
          </form>

          <div className="info-box-security mt-4">
            <ShieldCheck size={20} className="text-emerald-600" />
            <div>
              <strong>Keamanan Bearer Token Terjamin:</strong>
              <p>Header <code>Authorization</code> pengguna pada UI akan selalu diteruskan secara otomatis ke tool MCP saat Gemini melakukan query data.</p>
            </div>
          </div>
        </div>
      )}

      {/* Tab 2 Content: Plug-in & Plug-out MCP */}
      {activeTab === 'mcp' && (
        <div className="tab-pane-content card-box">
          <div className="pane-header">
            <h3><Cpu size={20} className="inline-icon text-indigo-600" /> Manajemen MCP Server (Model Context Protocol)</h3>
            <p className="text-muted">
              Tentukan MCP server mana yang ingin di-<strong>Plug-in</strong> (Aktif) atau di-<strong>Plug-out</strong> (Non-aktif). 
              AI LLM hanya akan memanggil tool dari MCP yang sedang terpasang aktif.
            </p>
          </div>

          {/* Form Tambah MCP */}
          <div className="add-mcp-section">
            <h4 className="section-subtitle"><PlusCircle size={18} /> Tambah Endpoint MCP Baru</h4>
            <form onSubmit={handleAddMcp} className="inline-form-grid">
              <div className="form-group">
                <label>Nama MCP:</label>
                <input 
                  type="text" 
                  value={mcpName} 
                  onChange={(e) => setMcpName(e.target.value)}
                  placeholder="Contoh: msf-student-mcp" 
                  className="form-control"
                  required
                />
              </div>

              <div className="form-group url-group">
                <label>URL MCP Koneksi:</label>
                <input 
                  type="text" 
                  value={mcpUrl} 
                  onChange={(e) => setMcpUrl(e.target.value)}
                  placeholder="Contoh: http://localhost:8084/api/msf_student/mcp" 
                  className="form-control font-mono"
                  required
                />
              </div>

              <div className="form-group btn-group-align">
                <button type="submit" className="btn btn-primary" disabled={mcpLoading}>
                  <PlusCircle size={16} /> Pasang MCP
                </button>
              </div>
            </form>
            {formError && <p className="text-danger mt-2 text-sm">{formError}</p>}
          </div>

          {/* Tabel Daftar MCP */}
          <div className="mcp-list-section mt-4">
            <div className="list-header">
              <h4 className="section-subtitle">Daftar MCP Server Terdaftar ({mcpList.length})</h4>
              <button onClick={loadMcpConfigs} className="btn-icon-refresh" title="Muat Ulang">
                <RefreshCw size={16} className={mcpLoading ? 'spin' : ''} />
              </button>
            </div>

            {mcpList.length === 0 ? (
              <div className="empty-state">
                <Cpu size={40} className="text-slate-300" />
                <p>Belum ada MCP server yang didaftarkan. Masukkan nama & URL koneksi di atas.</p>
              </div>
            ) : (
              <div className="table-responsive">
                <table className="mcp-table">
                  <thead>
                    <tr>
                      <th style={{ width: '15%' }}>Status Plug</th>
                      <th style={{ width: '25%' }}>Nama MCP</th>
                      <th style={{ width: '40%' }}>URL Koneksi</th>
                      <th style={{ width: '20%', textAlign: 'center' }}>Aksi</th>
                    </tr>
                  </thead>
                  <tbody>
                    {mcpList.map((mcp) => {
                      const testInfo = mcpTestResults[mcp.id];
                      return (
                        <React.Fragment key={mcp.id}>
                          <tr className={mcp.isActive ? 'row-active' : 'row-inactive'}>
                            <td>
                              <button 
                                type="button"
                                onClick={() => handleToggleMcp(mcp.id, mcp.isActive)}
                                className={`plug-toggle-badge ${mcp.isActive ? 'badge-plugged-in' : 'badge-plugged-out'}`}
                                title={mcp.isActive ? 'Klik untuk Plug-out' : 'Klik untuk Plug-in'}
                              >
                                <Power size={14} />
                                <span>{mcp.isActive ? 'PLUG-IN' : 'PLUG-OUT'}</span>
                              </button>
                            </td>
                            <td>
                              <strong className="mcp-name-text">{mcp.name}</strong>
                            </td>
                            <td>
                              <code className="mcp-url-code">{mcp.url}</code>
                            </td>
                            <td style={{ textAlign: 'center' }}>
                              <div className="action-buttons-cell">
                                <button 
                                  type="button" 
                                  onClick={() => handleTestMcp(mcp)}
                                  className="btn-sm btn-test"
                                  title="Test Koneksi & Cek Tool"
                                  disabled={testInfo?.loading}
                                >
                                  {testInfo?.loading ? <RefreshCw size={14} className="spin" /> : <Radio size={14} />}
                                  Test
                                </button>
                                <button 
                                  type="button" 
                                  onClick={() => handleDeleteMcp(mcp.id)}
                                  className="btn-sm btn-delete"
                                  title="Hapus MCP"
                                >
                                  <Trash2 size={14} />
                                </button>
                              </div>
                            </td>
                          </tr>
                          {testInfo && (
                            <tr className="test-result-row">
                              <td colSpan={4}>
                                <div className={`test-feedback ${testInfo.success ? 'feedback-success' : 'feedback-error'}`}>
                                  {testInfo.success ? <CheckCircle2 size={15} /> : <XCircle size={15} />}
                                  <span>{testInfo.message}</span>
                                </div>
                              </td>
                            </tr>
                          )}
                        </React.Fragment>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
