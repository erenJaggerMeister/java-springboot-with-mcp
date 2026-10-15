const MSF_STUDENT_BASE_URL = 'http://localhost:8084';

/**
 * Fetch paginated students from MSF_student service
 * @param {Object} params
 * @param {number} params.page - 0-based page index
 * @param {number} params.rows - number of rows per page
 * @param {string} params.sortBy - field name to sort by (e.g. 'nama', 'id')
 * @param {string} params.sortDir - 'asc' or 'desc'
 * @param {string} [params.nameFilter] - optional filter for student name
 * @param {string} params.token - JWT Bearer token
 * @returns {Promise<Object>}
 */
export async function getStudentPaging({
  page = 0,
  rows = 10,
  sortBy = 'nama',
  sortDir = 'desc',
  nameFilter = '',
  token = ''
}) {
  const url = new URL(`${MSF_STUDENT_BASE_URL}/api/msf_student/getDataStudent/paging`);
  url.searchParams.append('page', page);
  url.searchParams.append('rows', rows);
  url.searchParams.append('sortBy', sortBy);
  url.searchParams.append('sortDir', sortDir);

  if (nameFilter && nameFilter.trim() !== '') {
    url.searchParams.append('filters.name.value', nameFilter.trim());
    url.searchParams.append('filters.name.matchMode', 'contains');
  }

  const headers = {
    'Accept': 'application/json'
  };

  if (token) {
    const formattedToken = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
    headers['Authorization'] = formattedToken;
  }

  const response = await fetch(url.toString(), {
    method: 'GET',
    headers
  });

  if (!response.ok) {
    let errorMessage = `Gagal memuat data siswa (${response.status})`;
    try {
      const errorJson = await response.json();
      if (errorJson.message) {
        errorMessage = errorJson.message;
      }
    } catch {
      // ignore json parse error
    }
    throw new Error(errorMessage);
  }

  const result = await response.json();
  return result.data; // Returns PagingResult<StudentDto>
}
