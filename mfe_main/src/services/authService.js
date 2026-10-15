const MSF_AUTH_BASE_URL = 'http://localhost:8080';

const TOKEN_KEY = 'auth_token';
const USER_KEY = 'auth_user';

/**
 * Perform login through MSF_auth microservice
 * @param {string} loginId - Username or Email
 * @param {string} password - Raw password
 * @returns {Promise<Object>}
 */
export async function login(loginId, password) {
  const response = await fetch(`${MSF_AUTH_BASE_URL}/api/v1/auth/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    },
    body: JSON.stringify({
      loginId: loginId.trim(),
      password: password
    })
  });

  if (!response.ok) {
    let errorMsg = 'Login gagal: Username atau password salah';
    try {
      const errJson = await response.json();
      if (errJson.message) {
        errorMsg = errJson.message;
      }
    } catch {
      // ignore
    }
    throw new Error(errorMsg);
  }

  const result = await response.json();
  if (!result.success || !result.data) {
    throw new Error(result.message || 'Login gagal');
  }

  const data = result.data;
  const token = data.accessToken;
  const user = {
    loginId: data.loginId,
    name: data.name,
    roles: data.roles || []
  };

  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(USER_KEY, JSON.stringify(user));

  return { token, user };
}

export function logout() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function getCurrentUser() {
  const raw = localStorage.getItem(USER_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

export function isAuthenticated() {
  return !!getToken();
}
