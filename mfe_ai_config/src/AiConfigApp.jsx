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
  ShieldCheck, 
  Radio, 
  Sliders, 
  Eye, 
  EyeOff, 
  Sparkles, 
  Bot 
} from 'lucide-react';
import { 
  detectLlmProviderAndModels,
  getActiveLlmConfig,
  saveActiveLlmConfig,
  testLlmConnection,
  getAllMcpConfigs, 
  createMcpConfig, 
  toggleMcpStatus, 
  deleteMcpConfig, 
  testMcpConnection 
} from './services/aiConfigService';
import './index.css';

export default function AiConfigApp({ token: propToken, user: propUser }) {
  const token = propToken || localStorage.getItem('auth_token') || '';
  const [activeTab, setActiveTab] = useState('llm'); // 'llm' | 'mcp'

  // Tab 1 States (Auto-Detect Multi-LLM)
  const [apiKey, setApiKey] = useState('');
  const [showApiKey, setShowApiKey] = useState(false);
  const [detectedProvider, setDetectedProvider] = useState('gemini');
  const [detectedProviderName, setDetectedProviderName] = useState('Google Gemini');
  const [model, setModel] = useState('gemini-1.5-flash');
  const [modelList, setModelList] = useState(['gemini-1.5-flash', 'gemini-1.5-pro', 'gemini-2.0-flash']);
  const [fetchingModels, setFetchingModels] = useState(false);
  const [llmLoading, setLlmLoading] = useState(false);
  const [llmStatus, setLlmStatus] = useState(null); // { success: boolean, message: string }

  // Tab 2 States (MCP)
  const [mcpList, setMcpList] = useState([]);
  const [mcpName, setMcpName] = useState('');
  const [mcpUrl, setMcpUrl] = useState('');
  const [mcpLoading, setMcpLoading] = useState(false);
  const [mcpTestResults, setMcpTestResults] = useState({});
  const [formError, setFormError] = useState('');

  // Initial Load
  useEffect(() => {
    if (!token) return;
    loadInitialLlmConfig();
    loadMcpConfigs();
  }, [token]);

  const loadInitialLlmConfig = async () => {
    try {
      const config = await getActiveLlmConfig(token);
      const activeProv = config.active_provider || 'gemini';
      const activeKey = config.active_api_key || config[`${activeProv}_api_key`] || config.gemini_api_key || '';
      const activeModel = config.active_model || config[`${activeProv}_model`] || config.gemini_model || 'gemini-1.5-flash';
      const provName = config.provider_name || (activeProv === 'gemini' ? 'Google Gemini' : activeProv.toUpperCase());

      setApiKey(activeKey);
      setDetectedProvider(activeProv);
      setDetectedProviderName(provName);
      setModel(activeModel);

      // If active key is present, detect models
      if (activeKey) {
        try {
          const det = await detectLlmProviderAndModels(activeKey, token);
          if (det.success && det.models && det.models.length > 0) {
            setDetectedProvider(det.provider);
            setDetectedProviderName(det.providerName);
            const merged = Array.from(new Set([activeModel, ...det.models]));
            setModelList(merged);
          }
        } catch {
          // fallback
          setModelList(prev => Array.from(new Set([activeModel, ...prev])));
        }
      }
    } catch (err) {
      console.warn('Gagal memuat konfigurasi LLM aktif:', err);
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

  // Detect Provider & Fetch Models from API Key
  const handleDetectAndFetchModels = async () => {
    if (!apiKey.trim()) {
      setLlmStatus({ success: false, message: 'Harap masukkan API Key terlebih dahulu.' });
      return;
    }

    setFetchingModels(true);
    setLlmStatus(null);

    try {
      const res = await detectLlmProviderAndModels(apiKey.trim(), token);
      if (res.success) {
        setDetectedProvider(res.provider);
        setDetectedProviderName(res.providerName);

        if (res.models && res.models.length > 0) {
          setModelList(res.models);
          const chosen = res.models.includes(model) ? model : (res.defaultModel || res.models[0]);
          setModel(chosen);
        }

        setLlmStatus({
          success: true,
          message: res.message || `Provider terdeteksi: ${res.providerName} (${res.models?.length || 0} model tersedia)`
        });
      } else {
        setLlmStatus({
          success: false,
          message: res.message || 'Gagal mendeteksi provider untuk API Key ini.'
        });
      }
    } catch (err) {
      setLlmStatus({ success: false, message: 'Gagal mendeteksi provider: ' + err.message });
    } finally {
      setFetchingModels(false);
    }
  };

  // Test Connection
  const handleTestConnection = async () => {
    if (!apiKey.trim()) {
      setLlmStatus({ success: false, message: 'API Key wajib diisi untuk menguji koneksi.' });
      return;
    }

    setLlmLoading(true);
    setLlmStatus(null);
    try {
      const res = await testLlmConnection({
        provider: detectedProvider,
        apiKey: apiKey.trim(),
        model: model.trim()
      }, token);

      const isSuccess = res.data?.success ?? (res.data === true);
      const message = res.data?.message || (isSuccess ? 'Koneksi ke LLM Berhasil!' : 'Koneksi gagal. Periksa kembali API Key dan Model.');

      if (res.data?.availableModels && res.data.availableModels.length > 0) {
        const merged = Array.from(new Set([...modelList, ...res.data.availableModels]));
        setModelList(merged);
      }

      setLlmStatus({
        success: isSuccess,
        message: message
      });
    } catch (err) {
      setLlmStatus({ success: false, message: 'Gagal menguji koneksi: ' + err.message });
    } finally {
      setLlmLoading(false);
    }
  };

  // Save Configuration
  const handleSaveConfig = async (e) => {
    e.preventDefault();
    if (!apiKey.trim()) {
      setLlmStatus({ success: false, message: 'API Key wajib diisi.' });
      return;
    }

    setLlmLoading(true);
    setLlmStatus(null);
    try {
      await saveActiveLlmConfig({
        provider: detectedProvider,
        apiKey: apiKey.trim(),
        model: model.trim()
      }, token);

      setLlmStatus({ 
        success: true, 
        message: `Konfigurasi ${detectedProviderName} (Model: '${model}') berhasil disimpan dan siap digunakan!` 
      });
    } catch (err) {
      setLlmStatus({ success: false, message: err.message || 'Gagal menyimpan konfigurasi' });
    } finally {
      setLlmLoading(false);
    }
  };

  // --- TAB 2 ACTIONS (MCP) ---
  const handleAddMcp = async (e) => {
    e.preventDefault();
    if (!mcpName.trim() || !mcpUrl.trim()) {
      setFormError('Nama MCP dan URL MCP wajib diisi');
      return;
    }
    setFormError('');
    setMcpLoading(true);
    try {
      const newMcp = await createMcpConfig({ name: mcpName, url: mcpUrl, isActive: true }, token);
      setMcpList(prev => [...prev, newMcp]);
      setMcpName('');
      setMcpUrl('');
    } catch (err) {
      setFormError('Gagal menambah MCP: ' + err.message);
    } finally {
      setMcpLoading(false);
    }
  };

  const handleToggleMcp = async (id, currentStatus) => {
    try {
      const updated = await toggleMcpStatus(id, !currentStatus, token);
      setMcpList(prev => prev.map(item => item.id === id ? { ...item, isActive: updated.isActive } : item));
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
            <p className="module-subtitle">Kelola Engine LLM Multi-Provider dan Plug-in / Plug-out MCP Server</p>
          </div>
        </div>
      </div>

      {/* Tabs Nav */}
      <div className="config-tabs-nav">
        <button 
          className={`config-tab-btn ${activeTab === 'llm' ? 'active' : ''}`}
          onClick={() => setActiveTab('llm')}
        >
          <Bot size={18} />
          <span>Tab 1: Pengaturan Model LLM</span>
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

      {/* Tab 1 Content: Auto-Detect Multi-Provider LLM */}
      {activeTab === 'llm' && (
        <div className="tab-pane-content card-box">
          <div className="pane-header">
            <h3><Key size={20} className="inline-icon text-indigo-600" /> Kredensial & Pilihan Model LLM</h3>
            <p className="text-muted">
              Masukkan API Key Anda langsung di bawah. Sistem akan secara otomatis mendeteksi provider (Google Gemini, OpenAI, Claude, DeepSeek, Mistral, Nvidia NIM) beserta daftar model yang tersedia.
            </p>
          </div>

          <form onSubmit={handleSaveConfig} className="config-form">
            {/* 1. API Key Input with Password Masking & Eye Toggle */}
            <div className="form-group">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <label htmlFor="apiKeyInput">API Key:</label>
                {detectedProviderName && (
                  <span className="detected-provider-badge">
                    <Sparkles size={13} className="text-indigo-600" />
                    <span>Terdeteksi: <strong>{detectedProviderName}</strong></span>
                  </span>
                )}
              </div>
              <div className="input-password-wrapper">
                <input 
                  id="apiKeyInput"
                  type={showApiKey ? "text" : "password"}
                  value={apiKey} 
                  onChange={(e) => {
                    setApiKey(e.target.value);
                    setLlmStatus(null);
                  }}
                  placeholder="Masukkan API Key (Gemini, OpenAI, Claude, DeepSeek, Mistral, dll)..."
                  className="form-control font-mono"
                  autoComplete="off"
                  spellCheck="false"
                  required
                />
                <button
                  type="button"
                  onClick={() => setShowApiKey(!showApiKey)}
                  className="btn-toggle-eye"
                  title={showApiKey ? "Sembunyikan API Key" : "Tampilkan API Key"}
                >
                  {showApiKey ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
              <small className="help-text">
                API Key disembunyikan secara default demi keamanan pengguna. Klik ikon mata untuk mengintip.
              </small>
            </div>

            {/* 2. Model Selector & Auto-Detect Refresh Button */}
            <div className="form-group">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.25rem' }}>
                <label htmlFor="modelSelect" style={{ marginBottom: 0 }}>Model LLM Terpilih:</label>
                <button
                  type="button"
                  onClick={handleDetectAndFetchModels}
                  disabled={fetchingModels || !apiKey.trim()}
                  title="Deteksi provider dan ambil daftar model yang didukung dari API Key Anda"
                  style={{ 
                    background: 'none', 
                    border: 'none', 
                    cursor: fetchingModels || !apiKey.trim() ? 'not-allowed' : 'pointer', 
                    color: '#4f46e5',
                    fontSize: '0.8rem',
                    display: 'inline-flex',
                    alignItems: 'center',
                    gap: '4px',
                    padding: 0
                  }}
                >
                  <RefreshCw size={13} className={fetchingModels ? "spin" : ""} />
                  <span>{fetchingModels ? 'Mendeteksi model...' : 'Deteksi & Muat Model'}</span>
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
                    {m}
                  </option>
                ))}
                {!modelList.includes(model) && model && (
                  <option value={model}>{model}</option>
                )}
              </select>
              <small className="help-text">Model terpilih akan digunakan oleh <code>msf_ai</code> untuk chat dan eksekusi function calling dengan MCP.</small>
            </div>

            {/* Status Banner */}
            {llmStatus && (
              <div className={`status-banner ${llmStatus.success ? 'banner-success' : 'banner-error'}`}>
                {llmStatus.success ? <CheckCircle2 size={18} /> : <XCircle size={18} />}
                <span>{llmStatus.message}</span>
              </div>
            )}

            {/* Action Buttons */}
            <div className="form-actions">
              <button 
                type="submit" 
                className="btn btn-primary"
                disabled={llmLoading}
              >
                {llmLoading ? <RefreshCw size={16} className="spin" /> : <Settings size={16} />}
                Simpan Konfigurasi
              </button>

              <button 
                type="button" 
                onClick={handleTestConnection}
                className="btn btn-secondary"
                disabled={llmLoading || !apiKey.trim()}
              >
                {llmLoading ? <RefreshCw size={16} className="spin" /> : <Radio size={16} />}
                Test Koneksi LLM
              </button>
            </div>
          </form>

          <div className="info-box-security mt-4">
            <ShieldCheck size={20} className="text-emerald-600" />
            <div>
              <strong>Keamanan Bearer Token & Multi-LLM Terjamin:</strong>
              <p>Header <code>Authorization</code> pengguna pada UI akan selalu diteruskan secara otomatis ke tool MCP saat model AI manapun melakukan query data.</p>
            </div>
          </div>
        </div>
      )}

      {/* Tab 2 Content: Plug-in & Plug-out MCP (Restored with Original Table Styling) */}
      {activeTab === 'mcp' && (
        <div className="tab-pane-content card-box">
          <div className="pane-header">
            <h3><Cpu size={20} className="inline-icon text-indigo-600" /> Manajemen MCP Server (Plug-in / Plug-out)</h3>
            <p className="text-muted">Hubungkan atau putuskan server MCP pihak ketiga atau modul mikroservis lainnya untuk memperluas tool dan context AI.</p>
          </div>

          {/* Form Tambah MCP */}
          <form onSubmit={handleAddMcp} className="mb-4">
            <div className="section-subtitle">
              <PlusCircle size={16} />
              <span>Daftarkan Endpoint MCP Baru</span>
            </div>
            {formError && <div className="text-danger text-sm mb-2">{formError}</div>}
            <div className="inline-form-grid">
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label htmlFor="mcpName">Nama MCP:</label>
                <input 
                  id="mcpName"
                  type="text" 
                  value={mcpName} 
                  onChange={(e) => setMcpName(e.target.value)}
                  placeholder="Contoh: msf-student-mcp"
                  className="form-control"
                  required
                />
              </div>
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label htmlFor="mcpUrl">URL Endpoint MCP:</label>
                <input 
                  id="mcpUrl"
                  type="text" 
                  value={mcpUrl} 
                  onChange={(e) => setMcpUrl(e.target.value)}
                  placeholder="http://localhost:8084/api/msf_student/mcp"
                  className="form-control font-mono"
                  required
                />
              </div>
              <button 
                type="submit" 
                className="btn btn-primary btn-sm" 
                style={{ height: '38px', padding: '0 1.25rem' }} 
                disabled={mcpLoading}
              >
                <PlusCircle size={16} />
                <span>Pasang MCP</span>
              </button>
            </div>
          </form>

          {/* List Header */}
          <div className="list-header mt-4">
            <div className="section-subtitle" style={{ marginBottom: 0 }}>
              <Cpu size={16} />
              <span>Daftar MCP Server Terdaftar</span>
            </div>
            <button 
              type="button" 
              onClick={loadMcpConfigs} 
              className="btn-icon-refresh" 
              title="Refresh daftar MCP"
            >
              <RefreshCw size={14} className={mcpLoading ? "spin" : ""} />
            </button>
          </div>

          {/* MCP Table with Original Styling */}
          <div className="table-responsive">
            <table className="mcp-table">
              <thead>
                <tr>
                  <th style={{ width: '150px' }}>Status</th>
                  <th>Nama MCP</th>
                  <th>Endpoint URL</th>
                  <th style={{ width: '160px', textAlign: 'center' }}>Aksi</th>
                </tr>
              </thead>
              <tbody>
                {mcpList.length === 0 ? (
                  <tr>
                    <td colSpan="4" className="text-center text-muted py-4" style={{ textAlign: 'center', padding: '2rem' }}>
                      Belum ada MCP server yang terdaftar.
                    </td>
                  </tr>
                ) : (
                  mcpList.map((mcp) => {
                    const testResult = mcpTestResults[mcp.id];
                    return (
                      <React.Fragment key={mcp.id}>
                        <tr className={mcp.isActive ? 'row-active' : 'row-inactive'}>
                          <td>
                            <button
                              type="button"
                              onClick={() => handleToggleMcp(mcp.id, mcp.isActive)}
                              className={`plug-toggle-badge ${mcp.isActive ? 'badge-plugged-in' : 'badge-plugged-out'}`}
                              title={mcp.isActive ? "Klik untuk Plug Out (Nonaktifkan)" : "Klik untuk Plug In (Aktifkan)"}
                            >
                              <Power size={13} />
                              <span>{mcp.isActive ? 'Plugged In (Aktif)' : 'Plugged Out'}</span>
                            </button>
                          </td>
                          <td>
                            <strong className="mcp-name-text">{mcp.name}</strong>
                          </td>
                          <td>
                            <code className="mcp-url-code">{mcp.url}</code>
                          </td>
                          <td>
                            <div className="action-buttons-cell">
                              <button
                                type="button"
                                onClick={() => handleTestMcp(mcp)}
                                className="btn-sm btn-test"
                                title="Tes Koneksi MCP"
                                disabled={testResult?.loading}
                              >
                                {testResult?.loading ? (
                                  <RefreshCw size={13} className="spin" />
                                ) : (
                                  <Radio size={13} />
                                )}
                                <span>Tes</span>
                              </button>
                              <button
                                type="button"
                                onClick={() => handleDeleteMcp(mcp.id)}
                                className="btn-sm btn-delete"
                                title="Hapus MCP"
                              >
                                <Trash2 size={13} />
                                <span>Hapus</span>
                              </button>
                            </div>
                          </td>
                        </tr>
                        {testResult && (
                          <tr className="test-result-row">
                            <td colSpan="4">
                              <div className={`test-feedback ${testResult.success ? 'feedback-success' : 'feedback-error'}`}>
                                {testResult.loading ? (
                                  <span style={{ display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
                                    <RefreshCw size={12} className="spin" /> Menguji koneksi MCP...
                                  </span>
                                ) : (
                                  <>
                                    {testResult.success ? <CheckCircle2 size={15} /> : <XCircle size={15} />}
                                    <span>{testResult.message}</span>
                                  </>
                                )}
                              </div>
                            </td>
                          </tr>
                        )}
                      </React.Fragment>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
