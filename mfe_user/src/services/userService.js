const MSF_USER_BASE_URL = 'http://localhost:8083';

/**
 * Fetch paginated users from MSF_user service
 * @param {Object} params
 * @param {number} params.page - 0-based page index
 * @param {number} params.rows - number of rows per page
 * @param {string} params.sortBy - field to sort by (e.g. 'id', 'name')
 * @param {string} params.sortDir - 'asc' or 'desc'
 * @param {string} params.token - JWT Bearer token
 * @returns {Promise<Object>}
 */
export async function getUserPaging({
  page = 0,
  rows = 10,
  sortBy = 'id',
  sortDir = 'asc',
  token = ''
}) {
  const url = new URL(`${MSF_USER_BASE_URL}/api/msf_user/getDataUser/paging`);
  url.searchParams.append('page', page);
  url.searchParams.append('rows', rows);
  url.searchParams.append('sortBy', sortBy);
  url.searchParams.append('sortDir', sortDir);

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
    let errorMessage = `Gagal memuat data pengguna (${response.status})`;
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
  return result.data; // Returns PagingResult<UserDto>
}
