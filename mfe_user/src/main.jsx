import React, { useState } from 'react';
import ReactDOM from 'react-dom/client';
import UserApp from './UserApp';
import './index.css';

function StandaloneWrapper() {
  const [tokenInput, setTokenInput] = useState(localStorage.getItem('auth_token') || '');
  const [savedToken, setSavedToken] = useState(localStorage.getItem('auth_token') || '');

  const handleSaveToken = (e) => {
    e.preventDefault();
    localStorage.setItem('auth_token', tokenInput);
    setSavedToken(tokenInput);
  };

  return (
    <div style={{ padding: '2rem', maxWidth: '1200px', margin: '0 auto' }}>
      <div style={{
        background: '#1e293b', 
        color: 'white', 
        padding: '1rem 1.5rem', 
        borderRadius: '8px', 
        marginBottom: '1.5rem',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        flexWrap: 'wrap',
        gap: '1rem'
      }}>
        <div>
          <h1 style={{ fontSize: '1.25rem', fontWeight: 'bold' }}>MFE User (Standalone Mode)</h1>
          <p style={{ fontSize: '0.85rem', color: '#94a3b8' }}>Port: 3002 • Remote Module: ./UserApp</p>
        </div>
        <form onSubmit={handleSaveToken} style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
          <input 
            type="text" 
            placeholder="Bearer Token..."
            value={tokenInput}
            onChange={(e) => setTokenInput(e.target.value)}
            style={{
              padding: '0.4rem 0.75rem',
              borderRadius: '4px',
              border: '1px solid #475569',
              background: '#0f172a',
              color: 'white',
              fontSize: '0.85rem',
              width: '260px'
            }}
          />
          <button type="submit" className="btn btn-primary" style={{ padding: '0.4rem 0.8rem' }}>
            Set Token
          </button>
        </form>
      </div>

      <UserApp token={savedToken} />
    </div>
  );
}

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <StandaloneWrapper />
  </React.StrictMode>
);
