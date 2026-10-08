export interface PermissionResponse {
  code: string;
  description: string;
}

const API_BASE = "http://localhost:8080";

export async function fetchPermissions(): Promise<PermissionResponse[]> {
  const res = await fetch(`${API_BASE}/api/permissions`, {
    headers: { Accept: "application/json" },
  });
  if (!res.ok) {
    throw new Error(`Backend respondeu ${res.status}`);
  }
  return (await res.json()) as PermissionResponse[];
}
