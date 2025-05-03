import axios from 'axios';

/**
 * @param {import('vue-router').Router} router
 * @param {string} url
 * @param {object} options
 */
export async function fetchWithErrorHandling(router, url, options = {}) {
  try {
    const res = await axios({ url, ...options });
    return res.data;
  } catch (error) {
    const code = error.response?.status || 500;
    router.push({ name: 'error-page', query: { code } });
    return null;
  }
}
