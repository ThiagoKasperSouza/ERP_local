import type { UserResponse } from "./usersApi.ts";

const API_BASE = import.meta.env.VITE_API_BASE ?? "http://localhost:8080";

export interface AuthResult {
  token: string;
  user: UserResponse;
}

export class ApiError extends Error {
  status: number;
  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

async function request<T>(path: string, body: unknown): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    method: "POST",
    headers: { "Content-Type": "application/json", Accept: "application/json" },
    body: JSON.stringify(body),
  });
  const data = (await res.json().catch(() => undefined)) as
    | (T & { error?: string })
    | undefined;
  if (!res.ok) {
    throw new ApiError(res.status, data?.error ?? `Auth respondeu ${res.status}`);
  }
  return data as T;
}

export function login(email: string, password: string): Promise<AuthResult> {
  return request<AuthResult>("/api/auth/login", { email, password });
}

export function register(name: string, email: string, password: string): Promise<AuthResult> {
  return request<AuthResult>("/api/auth/register", { name, email, password });
}

export function loginWithGoogle(idToken: string): Promise<AuthResult> {
  return request<AuthResult>("/api/auth/google", { idToken });
}

export function loginWithGoogleCode(
  code: string,
  codeVerifier: string,
  redirectUri: string,
): Promise<AuthResult> {
  return request<AuthResult>("/api/auth/google/code", { code, codeVerifier, redirectUri });
}
