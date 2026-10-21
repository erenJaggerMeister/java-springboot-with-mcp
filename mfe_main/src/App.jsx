import React, { useState, Suspense } from 'react';
import Login from './components/Login';
import Header from './components/Header';
import Sidebar from './components/Sidebar';
import RemoteErrorBoundary from './components/RemoteErrorBoundary';
import { getToken, getCurrentUser, logout } from './services/authService';
import { Loader2 } from 'lucide-react';

// Federated Remote Microfrontends
const RemoteStudentApp = React.lazy(() => import('mfe_student/StudentApp'));
const RemoteUserApp = React.lazy(() => import('mfe_user/UserApp'));
const RemoteChatApp = React.lazy(() => import('mfe_chat/ChatApp'));
const RemoteAiConfigApp = React.lazy(() => import('mfe_ai_config/AiConfigApp'));

function ModuleLoadingFallback({ moduleName }) {
  return (
    <div className="module-loading-fallback">
      <Loader2 size={36} className="spin text-blue-500" />
      <p>Sedang menghubungkan ke remote module <strong>{moduleName}</strong>...</p>
      <span className="text-muted text-xs">Memuat artefak Module Federation via Vite</span>
    </div>
  );
}

export default function App() {
  const [token, setToken] = useState(() => getToken());
  const [user, setUser] = useState(() => getCurrentUser());
  const [activeMenu, setActiveMenu] = useState('student'); // Default direct to student menu on login!

  const handleLoginSuccess = (newToken, newUser) => {
    setToken(newToken);
    setUser(newUser);
    setActiveMenu('student');
  };

  const handleLogout = () => {
    logout();
    setToken(null);
    setUser(null);
    setActiveMenu('student');
  };

  // 1 & 2: Jika belum login, tampilkan form login
  if (!token) {
    return <Login onLoginSuccess={handleLoginSuccess} />;
  }

  // Layout utama
  return (
    <div className="app-layout">
      {/* Sidebar sebelah kiri */}
      <Sidebar 
        activeMenu={activeMenu} 
        onSelectMenu={(menuId) => setActiveMenu(menuId)} 
      />

      <div className="app-main-wrapper">
        {/* Header dengan judul & info login nama + role di kanan atas */}
        <Header 
          user={user} 
          onLogout={handleLogout} 
        />

        {/* Konten Microfrontend dinamis */}
        <main className="app-content-body">
          {activeMenu === 'student' && (
            <RemoteErrorBoundary key="boundary-student" moduleName="mfe_student" port={3001}>
              <Suspense fallback={<ModuleLoadingFallback moduleName="mfe_student" />}>
                <RemoteStudentApp token={token} user={user} />
              </Suspense>
            </RemoteErrorBoundary>
          )}

          {activeMenu === 'user' && (
            <RemoteErrorBoundary key="boundary-user" moduleName="mfe_user" port={3002}>
              <Suspense fallback={<ModuleLoadingFallback moduleName="mfe_user" />}>
                <RemoteUserApp token={token} user={user} />
              </Suspense>
            </RemoteErrorBoundary>
          )}

          {activeMenu === 'chat' && (
            <RemoteErrorBoundary key="boundary-chat" moduleName="mfe_chat" port={3003}>
              <Suspense fallback={<ModuleLoadingFallback moduleName="mfe_chat" />}>
                <RemoteChatApp token={token} user={user} />
              </Suspense>
            </RemoteErrorBoundary>
          )}

          {activeMenu === 'ai-config' && (
            <RemoteErrorBoundary key="boundary-ai-config" moduleName="mfe_ai_config" port={3004}>
              <Suspense fallback={<ModuleLoadingFallback moduleName="mfe_ai_config" />}>
                <RemoteAiConfigApp token={token} user={user} />
              </Suspense>
            </RemoteErrorBoundary>
          )}
        </main>
      </div>
    </div>
  );
}
