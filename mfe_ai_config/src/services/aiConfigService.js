const MSF_AI_BASE_URL = 'http://localhost:8086';

function getHeaders(token) {
  const headers = {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  };
  if (token) {
    headers['Authorization'] = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
  }
  return headers;
}

// --- GEMINI CONFIGURATION ---

export async function getGeminiConfig(token) {
  const res = await fetch(`${MSF_AI_BASE_URL}/api/msf_ai/config/gemini`, {
    headers: getHeaders(token)
  });
  if (!res.ok) throw new Error('Gagal mengambil konfigurasi Gemini');
  const data = await res.json();
  return data.data || {};
}

export async function saveGeminiConfig(apiKey, model, token) {
  // 1. Simpan Gemini API Key
  const resKey = await fetch(`${MSF_AI_BASE_URL}/api/msf_ai/config/gemini`, {
    method: 'POST',
    headers: getHeaders(token),
    body: JSON.stringify({
      configKey: 'gemini_api_key',
      configValue: apiKey
    })
  });
  if (!resKey.ok) throw new Error('Gagal menyimpan API Key Gemini');

  // 2. Simpan Gemini Model jika disertakan
  if (model) {
    const resModel = await fetch(`${MSF_AI_BASE_URL}/api/msf_ai/config/gemini`, {
      method: 'POST',
      headers: getHeaders(token),
      body: JSON.stringify({
        configKey: 'gemini_model',
        configValue: model
      })
    });
    if (!resModel.ok) throw new Error('Gagal menyimpan pilihan Model Gemini');
  }

  const data = await resKey.json();
  return data.data;
}

export async function testGeminiApiKey(apiKey, model, token) {
  const res = await fetch(`${MSF_AI_BASE_URL}/api/msf_ai/config/gemini/test`, {
    method: 'POST',
    headers: getHeaders(token),
    body: JSON.stringify({ apiKey, model })
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.message || 'Gagal menguji koneksi API Key');
  }
  const data = await res.json();
  return data;
}

export async function getAvailableModels(apiKey, token) {
  try {
    const query = apiKey ? `?apiKey=${encodeURIComponent(apiKey)}` : '';
    const res = await fetch(`${MSF_AI_BASE_URL}/api/msf_ai/config/gemini/models${query}`, {
      headers: getHeaders(token)
    });
    if (!res.ok) return ['gemini-1.5-flash', 'gemini-1.5-pro', 'gemini-2.0-flash'];
    const data = await res.json();
    return data.data || ['gemini-1.5-flash', 'gemini-1.5-pro', 'gemini-2.0-flash'];
  } catch {
    return ['gemini-1.5-flash', 'gemini-1.5-pro', 'gemini-2.0-flash'];
  }
}

// --- MCP CONFIGURATION ---

export async function getAllMcpConfigs(token) {
  const res = await fetch(`${MSF_AI_BASE_URL}/api/msf_ai/config/mcp`, {
    headers: getHeaders(token)
  });
  if (!res.ok) throw new Error('Gagal mengambil daftar MCP');
  const data = await res.json();
  return data.data || [];
}

export async function createMcpConfig({ name, url, isActive }, token) {
  const res = await fetch(`${MSF_AI_BASE_URL}/api/msf_ai/config/mcp`, {
    method: 'POST',
    headers: getHeaders(token),
    body: JSON.stringify({ name, url, isActive: isActive ?? true })
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.message || 'Gagal menambahkan MCP');
  }
  const data = await res.json();
  return data.data;
}

export async function toggleMcpStatus(id, active, token) {
  const res = await fetch(`${MSF_AI_BASE_URL}/api/msf_ai/config/mcp/${id}/toggle?active=${active}`, {
    method: 'PATCH',
    headers: getHeaders(token)
  });
  if (!res.ok) throw new Error('Gagal mengubah status MCP');
  const data = await res.json();
  return data.data;
}

export async function deleteMcpConfig(id, token) {
  const res = await fetch(`${MSF_AI_BASE_URL}/api/msf_ai/config/mcp/${id}`, {
    method: 'DELETE',
    headers: getHeaders(token)
  });
  if (!res.ok) throw new Error('Gagal menghapus MCP');
  return true;
}

export async function testMcpConnection(url, token) {
  const res = await fetch(`${MSF_AI_BASE_URL}/api/msf_ai/mcp/test`, {
    method: 'POST',
    headers: getHeaders(token),
    body: JSON.stringify({ url })
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.message || 'Gagal menguji endpoint MCP');
  }
  const data = await res.json();
  return data.data;
}
