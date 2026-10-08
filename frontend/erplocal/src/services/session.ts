// Sessão real via JWT (substitui o stub de papel fixo).
// Token em localStorage; papel lido do claim "role" do payload.
import type { UserResponse } from "./usersApi.ts";

const TOKEN_KEY = "erplocal-token";
const USER_KEY = "erplocal-user";

export type CurrentRole = "admin" | "user";

interface JwtPayload {
  sub?: string;
  email?: string;
  role?: string;
  exp?: number;
}

function decodePayload(token: string): JwtPayload | null {
  try {
    const part = token.split(".")[1];
    if (!part) return null;
    const json = atob(part.replace(/-/g, "+").replace(/_/g, "/"));
    return JSON.parse(json) as JwtPayload;
  } catch {
    return null;
  }
}

export function saveSession(token: string, user: UserResponse): void {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function clearSession(): void {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

export function getToken(): string | null {
  const token = localStorage.getItem(TOKEN_KEY);
  if (!token) return null;
  const payload = decodePayload(token);
  if (!payload || (payload.exp !== undefined && payload.exp * 1000 < Date.now())) {
    clearSession();
    return null;
  }
  return token;
}

export function getCurrentUser(): UserResponse | null {
  if (!getToken()) return null;
  try {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? (JSON.parse(raw) as UserResponse) : null;
  } catch {
    return null;
  }
}

export function getCurrentRole(): CurrentRole | null {
  const token = getToken();
  if (!token) return null;
  const role = decodePayload(token)?.role;
  return role === "ADMIN" ? "admin" : "user";
}

export function isAdmin(): boolean {
  return getCurrentRole() === "admin";
}

export function isAuthenticated(): boolean {
  return getToken() !== null;
}

/** Headers de auth (espelham o RequireAdminInterceptor do backend). */
export function authHeaders(): Record<string, string> {
  const token = getToken();
  return token ? { Authorization: `Bearer ${token}` } : {};
}
