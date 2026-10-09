import { authHeaders } from "./session.ts";
import { ApiError } from "./usersApi.ts";

const API_BASE = import.meta.env.VITE_API_BASE ?? "http://localhost:8080";

export interface VaultFile {
  id: string;
  name: string;
  kind: string;
  mimeType: string;
  sizeBytes: number;
  versionNumber: number;
  status: "TBO" | "PROTOTYPE" | "APPLICATION";
  pendingApproval: boolean;
  uploadedBy: string;
  approver: string | null;
  externalReference: string | null;
  projectCode: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface ProjectGroup {
  code: string;
  fileCount: number;
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    ...init,
    headers: { Accept: "application/json", ...authHeaders(), ...(init?.headers ?? {}) },
  });
  if (res.status === 204) return undefined as T;
  const data = (await res.json().catch(() => undefined)) as T | undefined;
  if (!res.ok) {
    const msg =
      (data as { error?: string } | undefined)?.error ?? `Cofre respondeu ${res.status}`;
    throw new ApiError(res.status, msg);
  }
  return data as T;
}

export interface FilesPage {
  page: number;
  size: number;
  items: VaultFile[];
}

export function listFiles(project?: string | null): Promise<FilesPage> {
  const query = project ? `?project=${encodeURIComponent(project)}&size=100` : "?size=100";
  return request<FilesPage>(`/api/vault/files${query}`);
}

export function listProjects(): Promise<ProjectGroup[]> {
  return request<ProjectGroup[]>("/api/vault/files/projects");
}

export async function uploadFile(
  file: File,
  projectCode?: string,
  externalReference?: string,
): Promise<VaultFile> {
  const form = new FormData();
  form.append("file", file);
  if (projectCode?.trim()) form.append("projectCode", projectCode.trim());
  if (externalReference?.trim()) form.append("externalReference", externalReference.trim());
  const res = await fetch(`${API_BASE}/api/vault/files`, {
    method: "POST",
    headers: { Accept: "application/json", ...authHeaders() },
    body: form,
  });
  const data = (await res.json().catch(() => undefined)) as
    | (VaultFile & { error?: string })
    | undefined;
  if (!res.ok) {
    throw new ApiError(res.status, data?.error ?? `Upload respondeu ${res.status}`);
  }
  return data as VaultFile;
}

export function updateFile(
  id: string,
  patch: { name?: string; status?: string; projectCode?: string | null; externalReference?: string | null },
): Promise<VaultFile> {
  return request<VaultFile>(`/api/vault/files/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(patch),
  });
}

export function deleteFile(id: string): Promise<void> {
  return request<void>(`/api/vault/files/${id}`, { method: "DELETE" });
}

export async function downloadFile(file: VaultFile): Promise<void> {
  const res = await fetch(`${API_BASE}/api/vault/files/${file.id}/download`, {
    headers: { ...authHeaders() },
  });
  if (!res.ok) {
    throw new ApiError(res.status, `Download respondeu ${res.status}`);
  }
  const blob = await res.blob();
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement("a");
  anchor.href = url;
  anchor.download = file.name;
  document.body.appendChild(anchor);
  anchor.click();
  anchor.remove();
  window.setTimeout(() => URL.revokeObjectURL(url), 5000);
}

export function formatSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  if (bytes < 1024 * 1024 * 1024) return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
  return `${(bytes / 1024 / 1024 / 1024).toFixed(2)} GB`;
}

const KIND_ICONS: Record<string, string> = {
  SOLIDWORKS: "⚙️",
  INVENTOR: "⚙️",
  CREO: "⚙️",
  STEP: "🧊",
  DWG: "📐",
  PDF: "📕",
  CSV: "📊",
  SPREADSHEET: "📊",
  EBOM: "🗂️",
};

export function kindIcon(kind: string): string {
  return KIND_ICONS[kind] ?? "📄";
}
