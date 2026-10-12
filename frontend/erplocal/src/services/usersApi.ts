import { authHeaders } from "./session.ts";

export interface UserResponse {
  id: string;
  name: string;
  email: string;
  role: "ADMIN" | "USER";
  active: boolean;
  consentAt: string | null;
  createdAt: string;
  permissions: string[];
}

export interface UsersPage {
  page: number;
  size: number;
  total: number;
  items: UserResponse[];
}

export class ApiError extends Error {
  status: number;
  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

const API_BASE = "http://localhost:8080";

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    ...init,
    headers: { Accept: "application/json", ...authHeaders(), ...(init?.headers ?? {}) },
  });
  if (res.status === 204) return undefined as T;
  const data = (await res.json().catch(() => undefined)) as T | undefined;
  if (!res.ok) {
    const msg =
      (data as { error?: string } | undefined)?.error ?? `Backend respondeu ${res.status}`;
    throw new ApiError(res.status, msg);
  }
  return data as T;
}

export function listUsers(page = 0, size = 100): Promise<UsersPage> {
  return request<UsersPage>(`/api/users?page=${page}&size=${size}`);
}

export function createUser(input: {
  name: string;
  email: string;
  password: string;
  role?: string;
}): Promise<UserResponse> {
  return request<UserResponse>("/api/users", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(input),
  });
}

export function updateUser(
  id: string,
  input: { name?: string; role?: string; active?: boolean; password?: string },
): Promise<UserResponse> {
  return request<UserResponse>(`/api/users/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(input),
  });
}

export function deactivateUser(id: string): Promise<UserResponse> {
  return request<UserResponse>(`/api/users/${id}`, { method: "DELETE" });
}

export function setUserPermissions(id: string, codes: string[]): Promise<UserResponse> {
  return request<UserResponse>(`/api/users/${id}/permissions`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(codes),
  });
}
