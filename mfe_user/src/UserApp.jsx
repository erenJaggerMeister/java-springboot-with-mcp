import React, { useState, useEffect, useCallback } from 'react';
import { getUserPaging } from './services/userService';
import { 
  ChevronLeft, 
  ChevronRight, 
  ChevronsLeft, 
  ChevronsRight, 
  ArrowUpDown, 
  ArrowUp, 
  ArrowDown, 
  RefreshCw, 
  Users, 
  AlertCircle,
  ShieldCheck,
  CheckCircle2,
  XCircle
} from 'lucide-react';

export default function UserApp({ token: propToken, user: propUser }) {
  const token = propToken || localStorage.getItem('auth_token') || '';
  const [users, setUsers] = useState([]);
  const [page, setPage] = useState(0);
  const [rows, setRows] = useState(10);
  const [sortBy, setSortBy] = useState('id');
  const [sortDir, setSortDir] = useState('asc');
  
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [isFirst, setIsFirst] = useState(true);
  const [isLast, setIsLast] = useState(true);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const loadUsers = useCallback(async () => {
    if (!token) {
      setError('Token autentikasi tidak ditemukan. Silakan login terlebih dahulu.');
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const pagingData = await getUserPaging({
        page,
        rows,
        sortBy,
        sortDir,
        token
      });

      setUsers(pagingData.data || []);
      setTotalElements(pagingData.totalElements || 0);
      setTotalPages(pagingData.totalPages || 0);
      setIsFirst(pagingData.isFirst ?? (page === 0));
      setIsLast(pagingData.isLast ?? (page >= (pagingData.totalPages || 1) - 1));
    } catch (err) {
      setError(err.message || 'Terjadi kesalahan saat memuat data pengguna');
    } finally {
      setLoading(false);
    }
  }, [page, rows, sortBy, sortDir, token]);

  useEffect(() => {
    loadUsers();
  }, [loadUsers]);

  const handleSort = (field) => {
    if (sortBy === field) {
      setSortDir(prev => prev === 'asc' ? 'desc' : 'asc');
    } else {
      setSortBy(field);
      setSortDir('asc');
    }
    setPage(0);
  };

  const renderSortIcon = (field) => {
    if (sortBy !== field) {
      return <ArrowUpDown size={14} className="sort-icon inactive" />;
    }
    return sortDir === 'asc' 
      ? <ArrowUp size={14} className="sort-icon active" /> 
      : <ArrowDown size={14} className="sort-icon active" />;
  };

  const startRecord = totalElements === 0 ? 0 : page * rows + 1;
  const endRecord = Math.min((page + 1) * rows, totalElements);

  return (
    <div className="mfe-module-container">
      <div className="module-header">
        <div className="title-area">
          <div className="icon-wrapper user-accent">
            <Users size={24} />
          </div>
          <div>
            <h2 className="module-title">Data Pengguna (mfe_user)</h2>
            <p className="module-subtitle">Menampilkan daftar data user terdaftar melalui orkestrasi MSF_user & pagination</p>
          </div>
        </div>

        <div className="header-actions">
          <button 
            type="button"
            className="btn btn-secondary"
            onClick={loadUsers}
            disabled={loading}
            title="Muat Ulang Data"
          >
            <RefreshCw size={16} className={loading ? 'spin' : ''} />
            <span>Segarkan</span>
          </button>
        </div>
      </div>

      {/* Toolbar / Status Bar */}
      <div className="toolbar-card">
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#475569', fontSize: '0.875rem' }}>
          <ShieldCheck size={18} className="text-purple-600" />
          <span>Endpoint: <code>/api/msf_user/getDataUser/paging</code></span>
        </div>

        <div className="active-sort-badge">
          Urutan: <strong>{sortBy}</strong> ({sortDir.toUpperCase()})
        </div>
      </div>

      {/* Error Alert */}
      {error && (
        <div className="alert alert-error">
          <AlertCircle size={20} />
          <div className="alert-content">
            <strong>Terjadi Kendala:</strong> {error}
          </div>
        </div>
      )}

      {/* Table Card */}
      <div className="table-card">
        <div className="table-wrapper">
          <table className="data-table">
            <thead>
              <tr>
                <th style={{ width: '60px' }}>No</th>
                <th style={{ width: '220px' }} onClick={() => handleSort('id')} className="sortable-th">
                  <div className="th-content">ID User (UUID) {renderSortIcon('id')}</div>
                </th>
                <th onClick={() => handleSort('name')} className="sortable-th">
                  <div className="th-content">Nama Pengguna {renderSortIcon('name')}</div>
                </th>
                <th onClick={() => handleSort('loginId')} className="sortable-th">
                  <div className="th-content">Login ID / Username {renderSortIcon('loginId')}</div>
                </th>
                <th style={{ width: '140px' }} onClick={() => handleSort('role')} className="sortable-th">
                  <div className="th-content">Hak Akses (Role) {renderSortIcon('role')}</div>
                </th>
                <th style={{ width: '110px' }}>Status</th>
                <th style={{ width: '130px' }}>Dibuat Oleh</th>
                <th style={{ width: '170px' }} onClick={() => handleSort('createdDate')} className="sortable-th">
                  <div className="th-content">Tanggal Registrasi {renderSortIcon('createdDate')}</div>
                </th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="8" className="text-center py-8">
                    <div className="loading-state">
                      <RefreshCw size={24} className="spin text-purple-500 mb-2" />
                      <span>Sedang memuat data dari MSF_user...</span>
                    </div>
                  </td>
                </tr>
              ) : users.length === 0 ? (
                <tr>
                  <td colSpan="8" className="text-center py-8 empty-row">
                    <Users size={40} className="text-gray-300 mb-2" />
                    <p className="text-gray-600 font-medium">Tidak ada data pengguna ditemukan.</p>
                    <span className="text-sm text-gray-400">Pastikan modul msm_user dan msf_user sudah aktif.</span>
                  </td>
                </tr>
              ) : (
                users.map((item, idx) => (
                  <tr key={item.id || idx}>
                    <td className="text-muted">{page * rows + idx + 1}</td>
                    <td className="font-mono text-xs text-muted" title={item.id}>
                      {item.id ? `${item.id.substring(0, 8)}...${item.id.substring(item.id.length - 4)}` : '-'}
                    </td>
                    <td className="font-semibold text-primary">{item.name}</td>
                    <td className="font-mono text-sm text-gray-700">{item.loginId}</td>
                    <td>
                      <span className={`badge ${
                        item.role?.toLowerCase().includes('admin') ? 'badge-purple' : 'badge-info'
                      }`}>
                        {item.role || 'USER'}
                      </span>
                    </td>
                    <td>
                      {item.active !== false ? (
                        <span className="badge badge-success" style={{ display: 'inline-flex', alignItems: 'center', gap: '3px' }}>
                          <CheckCircle2 size={12} /> Aktif
                        </span>
                      ) : (
                        <span className="badge badge-secondary" style={{ display: 'inline-flex', alignItems: 'center', gap: '3px' }}>
                          <XCircle size={12} /> Nonaktif
                        </span>
                      )}
                    </td>
                    <td className="text-sm text-muted">{item.createdBy || 'SYSTEM'}</td>
                    <td className="text-xs text-muted">
                      {(item.createdDate || item.createdAt) ? (item.createdDate || item.createdAt).replace('T', ' ') : '-'}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination Bar */}
        <div className="pagination-bar">
          <div className="pagination-info">
            Menampilkan <strong>{startRecord}</strong> - <strong>{endRecord}</strong> dari <strong>{totalElements}</strong> pengguna
          </div>

          <div className="pagination-controls">
            <div className="rows-selector">
              <label htmlFor="user-rows">Baris per halaman:</label>
              <select 
                id="user-rows"
                value={rows} 
                onChange={(e) => {
                  setRows(Number(e.target.value));
                  setPage(0);
                }}
                className="select-input"
              >
                <option value={5}>5</option>
                <option value={10}>10</option>
                <option value={20}>20</option>
                <option value={50}>50</option>
              </select>
            </div>

            <div className="page-buttons">
              <button 
                type="button"
                className="btn-page"
                onClick={() => setPage(0)}
                disabled={isFirst || loading}
                title="Halaman Pertama"
              >
                <ChevronsLeft size={16} />
              </button>
              <button 
                type="button"
                className="btn-page"
                onClick={() => setPage(prev => Math.max(0, prev - 1))}
                disabled={isFirst || loading}
                title="Halaman Sebelumnya"
              >
                <ChevronLeft size={16} />
              </button>

              <span className="page-indicator">
                Halaman <strong>{totalPages === 0 ? 0 : page + 1}</strong> dari <strong>{totalPages}</strong>
              </span>

              <button 
                type="button"
                className="btn-page"
                onClick={() => setPage(prev => prev + 1)}
                disabled={isLast || loading || totalPages === 0}
                title="Halaman Berikutnya"
              >
                <ChevronRight size={16} />
              </button>
              <button 
                type="button"
                className="btn-page"
                onClick={() => setPage(totalPages - 1)}
                disabled={isLast || loading || totalPages === 0}
                title="Halaman Terakhir"
              >
                <ChevronsRight size={16} />
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
