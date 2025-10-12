
const BASE_URL = import.meta.env.VITE_BASE_URL;

let isRefreshing = false;
let failedQueue = [];

const processQueue = (error) => {
  failedQueue.forEach(p => (error ? p.reject(error) : p.resolve()));
  failedQueue = [];
};

async function refreshToken() {
  try {
    const res = await fetch(`${BASE_URL}/v2/auth/refresh`, {
      method: "POST",
      credentials: "include",
    });
    return res.ok;
  } catch (err) {
    console.error("Refresh token failed:", err);
    return false;
  }
}

export async function request(path, options = {}) {
  const headers = options.headers ? { ...options.headers } : {};
  const requestOptions = { ...options, headers, credentials: "include" };

  let res = await fetch(`${BASE_URL}${path}`, requestOptions);
  let data = null;
  try { data = await res.json(); } catch {}

  if (res.status === 401) {
    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        failedQueue.push({ resolve, reject });
      }).then(() => request(path, options));
    }

    isRefreshing = true;
    const refreshed = await refreshToken();
    isRefreshing = false;

    if (refreshed) {
      processQueue();
      res = await fetch(`${BASE_URL}${path}`, requestOptions);
      try { data = await res.json(); } catch {}
    } else {
      processQueue(new Error("Session expired"));
      window.location.href = "/login";
      throw new Error("Session expired. Please login again.");
    }
  }

  if (!res.ok) {
    const msg = (data && (data.message || data.error)) || `Request failed (${res.status})`;
    throw new Error(msg);
  }

  return  { res, data }
}
