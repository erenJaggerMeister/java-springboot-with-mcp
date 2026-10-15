import React from 'react';
import { AlertTriangle, Terminal, RefreshCw, Layers } from 'lucide-react';

export default class RemoteErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { 
      hasError: false, 
      error: null 
    };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    console.error(`[MFE Host] Gagal memuat remote microfrontend '${this.props.moduleName}':`, error, errorInfo);
  }

  handleRetry = () => {
    this.setState({ hasError: false, error: null });
  };

  render() {
    if (this.state.hasError) {
      const { moduleName, port } = this.props;
      return (
        <div className="remote-error-wrapper">
          <div className="remote-error-card">
            <div className="remote-error-icon">
              <AlertTriangle size={36} />
            </div>

            <h3 className="remote-error-title">
              Remote Module <code>{moduleName}</code> Belum Aktif
            </h3>

            <p className="remote-error-desc">
              Host microfrontend (mfe_main) mencoba memuat remote entry dari port <strong>{port}</strong>, namun service belum merespons.
            </p>

            <div className="terminal-guide">
              <div className="terminal-header">
                <Terminal size={14} />
                <span>Jalankan remote module di terminal baru:</span>
              </div>
              <div className="terminal-body">
                <code>cd {moduleName}</code>
                <code>npm run preview</code>
                <span className="terminal-comment"># Port: {port} • URL: http://localhost:{port}/assets/remoteEntry.js</span>
              </div>
            </div>

            <div className="remote-error-actions">
              <button 
                type="button" 
                className="btn btn-primary"
                onClick={this.handleRetry}
              >
                <RefreshCw size={16} />
                <span>Coba Sambungkan Ulang</span>
              </button>
            </div>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
