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

export async function sendChatMessage(message, conversationHistory, token) {
  const res = await fetch(`${MSF_AI_BASE_URL}/api/msf_ai/chat`, {
    method: 'POST',
    headers: getHeaders(token),
    body: JSON.stringify({
      message,
      conversationHistory: conversationHistory || []
    })
  });

  if (!res.ok) {
    let errMsg = 'Gagal menghubungi server AI';
    try {
      const errJson = await res.json();
      if (errJson.message) errMsg = errJson.message;
    } catch {}
    throw new Error(errMsg);
  }

  const result = await res.json();
  return result.data;
}

export async function getActiveMcps(token) {
  try {
    const res = await fetch(`${MSF_AI_BASE_URL}/api/msf_ai/config/mcp/active`, {
      headers: getHeaders(token)
    });
    if (!res.ok) return [];
    const data = await res.json();
    return data.data || [];
  } catch {
    return [];
  }
}
