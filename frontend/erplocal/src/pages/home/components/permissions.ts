import { fetchPermissions } from "../../../services/permissionsApi.ts";
import {
  Permission,
  PermissionDescriptions,
} from "../../../types/permissions.ts";

function escapeHtml(value: string): string {
  return value
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

export async function renderPermissions(main: HTMLElement): Promise<void> {
  main.innerHTML = `<p>Carregando permissões…</p>`;

  try {
    const items = await fetchPermissions();
    main.innerHTML = `
      <h1>Permissões</h1>
      <table class="table table-striped">
        <thead>
          <tr><th>Código</th><th>Descrição</th></tr>
        </thead>
        <tbody>
          ${items
            .map(
              (p) => `
            <tr>
              <td><code>${escapeHtml(p.code)}</code></td>
              <td>${escapeHtml(p.description)}</td>
            </tr>`,
            )
            .join("")}
        </tbody>
      </table>
    `;
  } catch {
    // Fallback: backend fora do ar → mesma lista via enum local (permissions.ts)
    const fallback = (Object.values(Permission) as Permission[]).map(
      (code) => ({ code, description: PermissionDescriptions[code] }),
    );
    main.innerHTML = `
      <h1>Permissões</h1>
      <div class="alert alert-warning" role="alert">
        Backend indisponível — exibindo lista local.
        <button type="button" id="permissions-retry" class="btn btn-sm btn-outline-secondary ms-2">Tentar de novo</button>
      </div>
      <table class="table table-striped">
        <thead>
          <tr><th>Código</th><th>Descrição</th></tr>
        </thead>
        <tbody>
          ${fallback
            .map(
              (p) => `
            <tr>
              <td><code>${escapeHtml(p.code)}</code></td>
              <td>${escapeHtml(p.description)}</td>
            </tr>`,
            )
            .join("")}
        </tbody>
      </table>
    `;
    document
      .querySelector<HTMLButtonElement>("#permissions-retry")
      ?.addEventListener("click", () => void renderPermissions(main));
  }
}
