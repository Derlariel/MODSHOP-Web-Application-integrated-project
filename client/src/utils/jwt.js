export function decodeJwt(token) {
  if (!token || typeof token !== "string") return null;
  const payload = token.split(".")[1] || "";
  // base64url -> base64
  const b64 = payload.replace(/-/g, "+").replace(/_/g, "/");
  const json = typeof atob === "function"
    ? atob(b64)
    : Buffer.from(b64, "base64").toString("binary");
  const utf8 = decodeURIComponent(
    Array.prototype.map
      .call(json, c => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
      .join("")
  );
  return JSON.parse(utf8);
}
