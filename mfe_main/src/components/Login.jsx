import React, { useState } from 'react';
import { login } from '../services/authService';
import { Lock, User, AlertCircle, LogIn, KeyRound } from 'lucide-react';

export default function Login({ onLoginSuccess }) {
  const [loginId, setLoginId] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!loginId.trim() || !password) {
      setErrorMessage('Harap masukkan Username / Login ID dan Password');
      return;
    }

    setLoading(true);
    setErrorMessage('');

    try {
      const { token, user } = await login(loginId, password);
      onLoginSuccess(token, user);
    } catch (err) {
      setErrorMessage(err.message || 'Login gagal. Periksa username dan password Anda.');
    } finally {
      setLoading(false);
    }
  };

  const handleUseSeedAccount = () => {
    setLoginId('marcellius@example.com');
    setPassword('password123');
    setErrorMessage('');
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-header">
          <div className="login-badge-icon">
            <KeyRound size={28} />
          </div>
          <h1 className="login-title">Autentikasi Pengguna</h1>
          <p className="login-subtitle">Penerapan MCP dengan penggunaan java - springboot</p>
        </div>

        {errorMessage && (
          <div className="alert alert-error" style={{ marginBottom: '1.25rem' }}>
            <AlertCircle size={20} />
            <div className="alert-content">{errorMessage}</div>
          </div>
        )}

        <form onSubmit={handleSubmit} className="login-form">
          <div className="form-group">
            <label htmlFor="loginId">Username / Login ID</label>
            <div className="input-group">
              <User size={18} className="input-icon" />
              <input
                id="loginId"
                type="text"
                placeholder="misal: marcellius@example.com"
                value={loginId}
                onChange={(e) => setLoginId(e.target.value)}
                disabled={loading}
                autoFocus
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <div className="input-group">
              <Lock size={18} className="input-icon" />
              <input
                id="password"
                type="password"
                placeholder="Masukkan kata sandi..."
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                disabled={loading}
                required
              />
            </div>
          </div>

          <button
            type="submit"
            className="btn btn-primary btn-block"
            disabled={loading}
            style={{ marginTop: '0.75rem', height: '44px', fontSize: '0.95rem' }}
          >
            {loading ? (
              <span>Memverifikasi kredensial di msf_auth...</span>
            ) : (
              <>
                <LogIn size={18} />
                <span>Masuk ke Sistem</span>
              </>
            )}
          </button>
        </form>

        <div className="login-footer">
          <p className="text-muted text-xs">
            Kredensial seed migrasi database:
          </p>
          <button
            type="button"
            className="seed-hint-btn"
            onClick={handleUseSeedAccount}
            title="Klik untuk mengisi data login otomatis"
          >
            <code>marcellius@example.com</code> / <code>password123</code>
          </button>
        </div>
      </div>
    </div>
  );
}
