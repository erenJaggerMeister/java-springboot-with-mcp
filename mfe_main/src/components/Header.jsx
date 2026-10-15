import React from 'react';
import { User, Shield, LogOut } from 'lucide-react';

export default function Header({ user, onLogout }) {
  const roleDisplay = user?.roles && user.roles.length > 0 
    ? user.roles.join(', ') 
    : 'User';

  return (
    <header className="main-header">
      <div className="header-left">
        <h1 className="header-title">Penerapan MCP dengan penggunaan java - springboot</h1>
      </div>

      <div className="header-right">
        <div className="user-profile-badge">
          <div className="user-avatar">
            <User size={18} />
          </div>
          <div className="user-details">
            <span className="user-name">{user?.name || user?.loginId || 'Pengguna'}</span>
            <span className="user-role">
              <Shield size={11} style={{ marginRight: '3px' }} />
              {roleDisplay}
            </span>
          </div>
        </div>

        <button 
          type="button" 
          onClick={onLogout} 
          className="btn-logout"
          title="Keluar dari akun"
        >
          <LogOut size={16} />
          <span className="logout-text">Keluar</span>
        </button>
      </div>
    </header>
  );
}
