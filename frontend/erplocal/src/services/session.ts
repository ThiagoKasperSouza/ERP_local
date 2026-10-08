// Temporário até existir login/JWT: papel fixo do usuário atual.
// Quando o login chegar, trocar por leitura do token/sessão real.
export type CurrentRole = "admin" | "user";

export function getCurrentRole(): CurrentRole {
  return "admin";
}

export function isAdmin(): boolean {
  return getCurrentRole() === "admin";
}

/** Headers de auth temporários (espelham o RequireAdminInterceptor do backend). */
export function authHeaders(): Record<string, string> {
  return { "X-User-Role": getCurrentRole() };
}
