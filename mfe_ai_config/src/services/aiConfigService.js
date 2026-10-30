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

// --- MULTI-PROVIDER LLM CONFIGURATION ---

export async function detectLlmProviderAndModels(apiKey, token) {
  const res = await fetch(`${MSF_AI_BASE_URL}/api/msf_ai/config/detect`, {
    method: 'POST',
    headers: getHeaders(token),
    body: JSON.stringify({ apiKey: apiKey || '' })
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.message || 'Gagal mendeteksi provider LLM');
  }
  const data = await res.json();
  return data.data || {};
}

export async function getSupportedProviders(token) {
  try {
    const res = await fetch(`${MSF_AI_BASE_URL}/api/msf_ai/config/providers`, {
      headers: getHeaders(token)
    });
    if (!res.ok) return [];
    const data = await res.json();
    return data.data || [];
  } catch (err) {
    console.warn('Gagal memuat supported providers:', err);
    return [];
  }
}

export async function getActiveLlmConfig(token) {
  try {
    const res = await fetch(`${MSF_AI_BASE_URL}/api/msf_ai/config/active`, {
      headers: getHeaders(token)
    });
    if (!res.ok) return {};
    const data = await res.json();
    return data.data || {};
  } catch (err) {
    console.warn('Gagal memuat active LLM config:', err);
    return {};
  }
}

export async function saveActiveLlmConfig({ provider, apiKey, model }, token) {
  const res = await fetch(`${MSF_AI_BASE_URL}/api/msf_ai/config/active`, {
    method: 'POST',
    headers: getHeaders(token),
    body: JSON.stringify({ provider, apiKey, model })
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.message || 'Gagal menyimpan konfigurasi LLM');
  }
  const data = await res.json();
  return data.data;
}

export async function fetchLlmModels(provider, apiKey, token) {
  try {
    const params = new URLSearchParams();
    if (provider) params.append('provider', provider);
    if (apiKey) params.append('apiKey', apiKey);

    const res = await fetch(`${MSF_AI_BASE_URL}/api/msf_ai/config/models?${params.toString()}`, {
      headers: getHeaders(token)
    });
    if (!res.ok) return [];
    const data = await res.json();
    return data.data || [];
  } catch (err) {
    console.warn('Gagal mengambil models dari provider:', err);
    return [];
  }
}

export async function testLlmConnection({ provider, apiKey, model }, token) {
  const res = await fetch(`${MSF_AI_BASE_URL}/api/msf_ai/config/test`, {
    method: 'POST',
    headers: getHeaders(token),
    body: JSON.stringify({ provider, apiKey, model })
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.message || 'Gagal menguji koneksi LLM');
  }
  const data = await res.json();
  return data;
}

// --- LEGACY GEMINI CONFIGURATION (BACKWARD COMPATIBLE) ---

export async function getGeminiConfig(token) {
  const res = await fetch(`${MSF_AI_BASE_URL}/api/msf_ai/config/gemini`, {
    headers: getHeaders(token)
  });
  if (!res.ok) throw new Error('Gagal mengambil konfigurasi Gemini');
  const data = await res.json();
  return data.data || {};
}

export async function saveGeminiConfig(apiKey, model, token) {
  const resKey = await fetch(`${MSF_AI_BASE_URL}/api/msf_ai/config/gemini`, {
    method: 'POST',
    headers: getHeaders(token),
    body: JSON.stringify({
      configKey: 'gemini_api_key',
      configValue: apiKey
    })
  });
  if (!resKey.ok) throw new Error('Gagal menyimpan API Key Gemini');

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
