import React, { useState, useEffect, useCallback } from 'react';
import { getStudentPaging } from './services/studentService';
import { 
  ChevronLeft, 
  ChevronRight, 
  ChevronsLeft, 
  ChevronsRight, 
  ArrowUpDown, 
  ArrowUp, 
  ArrowDown, 
  Search, 
  RefreshCw, 
  GraduationCap, 
  AlertCircle 
} from 'lucide-react';

export default function StudentApp({ token: propToken, user: propUser }) {
  const token = propToken || localStorage.getItem('auth_token') || '';
  const [students, setStudents] = useState([]);
  const [page, setPage] = useState(0);
  const [rows, setRows] = useState(10);
  const [sortBy, setSortBy] = useState('nama');
  const [sortDir, setSortDir] = useState('desc');
  const [nameFilter, setNameFilter] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [isFirst, setIsFirst] = useState(true);
  const [isLast, setIsLast] = useState(true);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const loadStudents = useCallback(async () => {
    if (!token) {
      setError('Token autentikasi tidak ditemukan. Silakan login terlebih dahulu.');
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const pagingData = await getStudentPaging({
        page,
        rows,
        sortBy,
        sortDir,
        nameFilter: searchQuery,
        token
      });

      setStudents(pagingData.data || []);
      setTotalElements(pagingData.totalElements || 0);
      setTotalPages(pagingData.totalPages || 0);
      setIsFirst(pagingData.isFirst ?? (page === 0));
      setIsLast(pagingData.isLast ?? (page >= (pagingData.totalPages || 1) - 1));
    } catch (err) {
      setError(err.message || 'Terjadi kesalahan saat memuat data siswa');
    } finally {
      setLoading(false);
    }
  }, [page, rows, sortBy, sortDir, searchQuery, token]);

  useEffect(() => {
    loadStudents();
  }, [loadStudents]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setPage(0);
    setSearchQuery(nameFilter);
  };

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
          <div className="icon-wrapper student-accent">
            <GraduationCap size={24} />
          </div>
          <div>
            <h2 className="module-title">Data Siswa (mfe_student)</h2>
            <p className="module-subtitle">Menampilkan daftar data siswa dengan integrasi MSF_student & pagination</p>
          </div>
        </div>

        <div className="header-actions">
          <button 
            type="button"
            className="btn btn-secondary"
            onClick={loadStudents}
            disabled={loading}
            title="Muat Ulang Data"
          >
            <RefreshCw size={16} className={loading ? 'spin' : ''} />
            <span>Segarkan</span>
          </button>
        </div>
      </div>

      {/* Filter / Search Bar */}
      <div className="toolbar-card">
        <form onSubmit={handleSearchSubmit} className="search-form">
          <div className="search-input-group">
            <Search size={18} className="search-icon" />
            <input 
              type="text"
              placeholder="Cari nama siswa..."
              value={nameFilter}
              onChange={(e) => setNameFilter(e.target.value)}
              className="search-input"
            />
          </div>
          <button type="submit" className="btn btn-primary" disabled={loading}>
            Cari
          </button>
          {searchQuery && (
            <button 
              type="button" 
              className="btn btn-ghost" 
              onClick={() => { setNameFilter(''); setSearchQuery(''); setPage(0); }}
            >
              Reset Filter
            </button>
          )}
        </form>

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
                  <div className="th-content">ID Siswa (UUID) {renderSortIcon('id')}</div>
                </th>
                <th onClick={() => handleSort('nama')} className="sortable-th">
                  <div className="th-content">Nama Siswa {renderSortIcon('nama')}</div>
                </th>
                <th style={{ width: '100px' }} onClick={() => handleSort('umur')} className="sortable-th">
                  <div className="th-content">Umur {renderSortIcon('umur')}</div>
                </th>
                <th style={{ width: '130px' }} onClick={() => handleSort('kelas')} className="sortable-th">
                  <div className="th-content">Kelas {renderSortIcon('kelas')}</div>
                </th>
                <th style={{ width: '140px' }} onClick={() => handleSort('jenisKelas')} className="sortable-th">
                  <div className="th-content">Jenis Kelas {renderSortIcon('jenisKelas')}</div>
                </th>
                <th style={{ width: '170px' }} onClick={() => handleSort('createdAt')} className="sortable-th">
                  <div className="th-content">Dibuat Pada {renderSortIcon('createdAt')}</div>
                </th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="7" className="text-center py-8">
                    <div className="loading-state">
                      <RefreshCw size={24} className="spin text-blue-500 mb-2" />
                      <span>Sedang memuat data dari MSF_student...</span>
                    </div>
                  </td>
                </tr>
              ) : students.length === 0 ? (
                <tr>
                  <td colSpan="7" className="text-center py-8 empty-row">
                    <GraduationCap size={40} className="text-gray-300 mb-2" />
                    <p className="text-gray-600 font-medium">Tidak ada data siswa ditemukan.</p>
                    <span className="text-sm text-gray-400">Pastikan database PostgreSQL & service MSF/MSM sudah berjalan.</span>
                  </td>
                </tr>
              ) : (
                students.map((student, idx) => (
                  <tr key={student.id || idx}>
                    <td className="text-muted">{page * rows + idx + 1}</td>
                    <td className="font-mono text-xs text-muted" title={student.id}>
                      {student.id ? `${student.id.substring(0, 8)}...${student.id.substring(student.id.length - 4)}` : '-'}
                    </td>
                    <td className="font-semibold text-primary">{student.nama}</td>
                    <td>{student.umur ? `${student.umur} Thn` : '-'}</td>
                    <td>
                      <span className="badge badge-info">{student.kelas || '-'}</span>
                    </td>
                    <td>
                      <span className={`badge ${student.jenisKelas === 'Reguler' ? 'badge-primary' : 'badge-purple'}`}>
                        {student.jenisKelas || '-'}
                      </span>
                    </td>
                    <td className="text-xs text-muted">
                      {student.createdAt ? student.createdAt.replace('T', ' ') : '-'}
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
            Menampilkan <strong>{startRecord}</strong> - <strong>{endRecord}</strong> dari <strong>{totalElements}</strong> data siswa
          </div>

          <div className="pagination-controls">
            <div className="rows-selector">
              <label htmlFor="student-rows">Baris per halaman:</label>
              <select 
                id="student-rows"
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
