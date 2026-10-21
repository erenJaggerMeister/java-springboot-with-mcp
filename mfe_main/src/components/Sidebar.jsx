import React from 'react';
import { GraduationCap, Users, Bot, Sliders, Layers } from 'lucide-react';

export default function Sidebar({ activeMenu, onSelectMenu }) {
  const menuItems = [
    {
      id: 'student',
      label: 'mfe_student',
      title: 'Data Siswa',
      icon: GraduationCap,
      port: 3001,
      badge: 'Port 3001'
    },
    {
      id: 'user',
      label: 'mfe_user',
      title: 'Data Pengguna',
      icon: Users,
      port: 3002,
      badge: 'Port 3002'
    },
    {
      id: 'chat',
      label: 'mfe_chat',
      title: 'Chat AI & MCP',
      icon: Bot,
      port: 3003,
      badge: 'Port 3003'
    },
    {
      id: 'ai-config',
      label: 'mfe_ai_config',
      title: 'Konfigurasi AI',
      icon: Sliders,
      port: 3004,
      badge: 'Port 3004'
    }
  ];

  return (
    <aside className="main-sidebar">
      <div className="sidebar-brand">
        <div className="brand-logo">
          <Layers size={22} className="text-blue-400" />
        </div>
        <div className="brand-text">
          <span className="brand-name">MFE Container</span>
          <span className="brand-sub">Microservice Host</span>
        </div>
      </div>

      <div className="sidebar-section-title">
        <span>NAVIGASI MODUL</span>
      </div>

      <nav className="sidebar-nav">
        {menuItems.map((item) => {
          const Icon = item.icon;
          const isActive = activeMenu === item.id;
          return (
            <button
              key={item.id}
              type="button"
              className={`nav-item ${isActive ? 'active' : ''}`}
              onClick={() => onSelectMenu(item.id)}
            >
              <div className="nav-item-icon">
                <Icon size={20} />
              </div>
              <div className="nav-item-content">
                <span className="nav-item-title">{item.title}</span>
                <span className="nav-item-code">{item.label}</span>
              </div>
              <span className={`nav-port-pill ${isActive ? 'active-pill' : ''}`}>
                {item.port}
              </span>
            </button>
          );
        })}
      </nav>

      <div className="sidebar-footer">
        <div className="mfe-status-card">
          <div className="status-indicator-dot online"></div>
          <div className="status-text">
            <strong>Microfrontend Mode</strong>
            <span>Module Federation Active</span>
          </div>
        </div>
      </div>
    </aside>
  );
}
