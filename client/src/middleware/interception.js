import { useAuthStore } from "@/stores/useAuthStore";

const BASE_URL = import.meta.env.VITE_BASE_URL;
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error) => {
  failedQueue.forEach(p => (error ? p.reject(error) : p.resolve()));
  failedQueue = [];
};

export async function refreshToken() {
  try {
    const res = await fetch(`${BASE_URL}/v2/auth/refresh`, {
      method: "POST",
      credentials: "include",
    });
    if (!res.ok) return false;

    const data = await res.json();
    const authStore = useAuthStore();

    if (data?.user) {
      authStore.user = {
        id: data.user.id || data.user.user_id,
        nickname: data.user.nickName || data.user.nickname || "",
        email: data.user.email || "",
        role: data.user.userType || data.user.role || "",
        fullName: data.user.fullName || data.user.fullname || "",
      };
      localStorage.setItem("userClaims", JSON.stringify(authStore.user));
    }
    return true;
  } catch (err) {
    console.error("Refresh token error:", err);
    return false;
  }
}

export async function request(path, options = {}, bypassAuth = false) {
  const headers = options.headers ? { ...options.headers } : {};
  const requestOptions = { ...options, headers, credentials: "include" };

  let res = await fetch(`${BASE_URL}${path}`, requestOptions);
  let data = null;
  try { data = await res.json(); } catch {}

  if (res.status === 401 && !bypassAuth) {
    if (isRefreshing) {
      return new Promise((resolve, reject) => { failedQueue.push({ resolve, reject }); })
        .then(() => request(path, options, bypassAuth));
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
      const authStore = useAuthStore();
      authStore.user = null;
      localStorage.removeItem("userClaims");
      throw new Error("Session expired");
    }
  }

  if (!res.ok) {
    const msg = (data && (data.message || data.error)) || `Request failed (${res.status})`;
    throw new Error(msg);
  }

  return { res, data };
}
