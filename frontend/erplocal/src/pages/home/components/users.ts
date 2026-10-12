import {
  AllCommunityModule,
  ModuleRegistry,
  colorSchemeDark,
  createGrid,
  themeQuartz,
  type ColDef,
  type GridApi,
  type GridOptions,
  type ICellRendererParams,
} from "ag-grid-community";
import { getTheme } from "../../../services/theme.ts";
import { isAdmin } from "../../../services/session.ts";
import {
  ApiError,
  createUser,
  deactivateUser,
  listUsers,
  setUserPermissions,
  updateUser,
  type UserResponse,
} from "../../../services/usersApi.ts";
import { fetchPermissions } from "../../../services/permissionsApi.ts";
import { confirmDialog, permissionsDialog, userFormDialog } from "./userDialogs.ts";

ModuleRegistry.registerModules([AllCommunityModule]);

type GridTheme = ReturnType<typeof themeQuartz.withPart>;

function gridTheme(): GridTheme {
  return getTheme() === "dark" ? themeQuartz.withPart(colorSchemeDark) : themeQuartz;
}

function escapeHtml(value: string): string {
  return value
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

function toast(main: HTMLElement, message: string, kind: "success" | "danger" | "warning"): void {
  let stack = main.querySelector<HTMLDivElement>(".toast-stack");
  if (!stack) {
    stack = document.createElement("div");
    stack.className = "toast-stack";
    main.prepend(stack);
  }
  const el = document.createElement("div");
  el.className = `alert alert-${kind} alert-dismissible fade show`;
  el.setAttribute("role", "alert");
  el.innerHTML = `${escapeHtml(message)}
    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Fechar"></button>`;
  stack.appendChild(el);
  window.setTimeout(() => el.remove(), 5000);
}

function errorMessage(e: unknown): { message: string; status?: number } {
  if (e instanceof ApiError) {
    if (e.status === 403) return { message: "Sem permissão para esta ação.", status: 403 };
    return { message: e.message, status: e.status };
  }
  return { message: "Falha de comunicação com o backend." };
}

let currentApi: GridApi | null = null;
let themeObserver: MutationObserver | null = null;

function destroyGrid(): void {
  themeObserver?.disconnect();
  themeObserver = null;
  currentApi?.destroy();
  currentApi = null;
}

function roleBadge(params: ICellRendererParams<UserResponse>): string {
  const role = params.value as string;
  const cls = role === "ADMIN" ? "bg-danger" : "bg-secondary";
  return `<span class="badge ${cls}">${escapeHtml(role.toLowerCase())}</span>`;
}

function statusBadge(params: ICellRendererParams<UserResponse, boolean>): string {
  const active = params.value as boolean;
  return active
    ? `<span class="badge bg-success">ativo</span>`
    : `<span class="badge bg-warning text-dark">inativo</span>`;
}

function permissionsCell(params: ICellRendererParams<UserResponse>): string {
  const perms = (params.value as string[] | undefined) ?? [];
  if (perms.length === 0) return `<span class="text-muted">—</span>`;
  const titles = perms.map(escapeHtml).join(", ");
  return `<span class="badge bg-info text-dark" title="${titles}">${perms.length} perm.</span>`;
}

function actionsCell(params: ICellRendererParams<UserResponse>): string {
  const row = params.data;
  if (!row) return "";
  const id = escapeHtml(row.id);
  const toggle = row.active
    ? `<button type="button" class="btn btn-sm btn-outline-danger" data-action="deactivate" data-id="${id}">Desativar</button>`
    : `<button type="button" class="btn btn-sm btn-outline-success" data-action="reactivate" data-id="${id}">Reativar</button>`;
  return `<div class="btn-group btn-group-sm" role="group">
    <button type="button" class="btn btn-outline-primary" data-action="edit" data-id="${id}">Editar</button>
    <button type="button" class="btn btn-outline-secondary" data-action="permissions" data-id="${id}">Permissões</button>
    ${toggle}
  </div>`;
}

const columnDefs: ColDef<UserResponse>[] = [
  { field: "name", headerName: "Nome", filter: true },
  { field: "email", headerName: "E-mail", filter: true },
  { field: "role", headerName: "Papel", width: 110, cellRenderer: roleBadge },
  { headerName: "Status", width: 110, valueGetter: (p) => p.data?.active, cellRenderer: statusBadge },
  {
    headerName: "Permissões",
    width: 130,
    valueGetter: (p) => p.data?.permissions,
    cellRenderer: permissionsCell,
    sortable: false,
  },
  {
    field: "createdAt",
    headerName: "Criado em",
    width: 170,
    valueFormatter: (p) =>
      p.value ? new Date(p.value as string).toLocaleString("pt-BR") : "—",
  },
  {
    headerName: "Ações",
    width: 300,
    sortable: false,
    filter: false,
    resizable: false,
    cellRenderer: actionsCell,
  },
];

async function reload(api: GridApi, main: HTMLElement): Promise<void> {
  api.showLoadingOverlay();
  try {
    const page = await listUsers(0, 100);
    api.hideOverlay();
    api.setGridOption("rowData", page.items);
    if (page.items.length === 0) api.showNoRowsOverlay();
  } catch (e) {
    api.hideOverlay();
    toast(main, errorMessage(e).message, "danger");
  }
}

function findRow(api: GridApi, id: string): UserResponse | undefined {
  let found: UserResponse | undefined;
  api.forEachNode((node) => {
    if (node.data?.id === id) found = node.data;
  });
  return found;
}

export async function renderUsers(main: HTMLElement): Promise<void> {
  destroyGrid();

  // Guard: só admin acessa (espelha o 403 do backend)
  if (!isAdmin()) {
    toast(main, "Sem permissão para acessar Usuários.", "warning");
    main.innerHTML = `<div class="alert alert-warning" role="alert">Sem permissão para acessar Usuários.</div>`;
    window.setTimeout(() => {
      document.querySelector<HTMLButtonElement>('[data-section="inicio"]')?.click();
    }, 1200);
    return;
  }

  main.innerHTML = `
    <div class="d-flex justify-content-between align-items-center mb-3">
      <div>
        <h1 class="mb-0">Usuários</h1>
        <p class="text-muted mb-0">Somente admin cria, edita e desativa. Exclusão é soft (preserva p/ LGPD).</p>
      </div>
      <button type="button" id="users-create" class="btn btn-primary">Criar usuário</button>
    </div>
    <div id="users-grid" style="width: 100%;"></div>
  `;

  const gridDiv = main.querySelector<HTMLElement>("#users-grid")!;
  const options: GridOptions<UserResponse> = {
    theme: gridTheme(),
    columnDefs,
    defaultColDef: { sortable: true, filter: true, resizable: true, flex: 1, minWidth: 120 },
    pagination: true,
    paginationPageSize: 20,
    domLayout: "autoHeight",
    loadingOverlayComponentParams: { loadingMessage: "Carregando usuários…" },
  };
  const api = createGrid(gridDiv, options);
  currentApi = api;

  themeObserver = new MutationObserver(() => {
    api.setGridOption("theme", gridTheme());
  });
  themeObserver.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ["data-bs-theme"],
  });

  main
    .querySelector<HTMLButtonElement>("#users-create")
    ?.addEventListener("click", () => void onCreate(api, main));

  gridDiv.addEventListener("click", (ev) => {
    const btn = (ev.target as HTMLElement).closest<HTMLElement>("[data-action]");
    if (!btn?.dataset.action || !btn.dataset.id) return;
    const row = findRow(api, btn.dataset.id);
    if (!row) return;
    switch (btn.dataset.action) {
      case "edit":
        void onEdit(api, main, row);
        break;
      case "permissions":
        void onPermissions(api, main, row);
        break;
      case "deactivate":
        void onDeactivate(api, main, row);
        break;
      case "reactivate":
        void onReactivate(api, main, row);
        break;
    }
  });

  await reload(api, main);
}

async function onCreate(api: GridApi, main: HTMLElement): Promise<void> {
  const form = await userFormDialog({ title: "Criar usuário", mode: "create" });
  if (!form) return;
  try {
    await createUser(form);
    toast(main, "Usuário criado.", "success");
    await reload(api, main);
  } catch (e) {
    toast(main, errorMessage(e).message, "danger");
  }
}

async function onEdit(api: GridApi, main: HTMLElement, row: UserResponse): Promise<void> {
  const form = await userFormDialog({
    title: `Editar ${row.name}`,
    mode: "edit",
    initial: { name: row.name, email: row.email, role: row.role },
  });
  if (!form) return;
  try {
    await updateUser(row.id, {
      name: form.name,
      role: form.role,
      password: form.password.length > 0 ? form.password : undefined,
    });
    toast(main, "Usuário atualizado.", "success");
    await reload(api, main);
  } catch (e) {
    toast(main, errorMessage(e).message, "danger");
  }
}

async function onDeactivate(api: GridApi, main: HTMLElement, row: UserResponse): Promise<void> {
  const ok = await confirmDialog({
    title: `Desativar ${row.name}?`,
    body: `O usuário <strong>${escapeHtml(row.email)}</strong> perde o acesso, mas o registro é preservado (soft delete, LGPD).`,
    confirmLabel: "Desativar",
    confirmClass: "btn-danger",
  });
  if (!ok) return;
  try {
    await deactivateUser(row.id);
    toast(main, "Usuário desativado.", "success");
    await reload(api, main);
  } catch (e) {
    toast(main, errorMessage(e).message, "danger");
  }
}

async function onReactivate(api: GridApi, main: HTMLElement, row: UserResponse): Promise<void> {
  const ok = await confirmDialog({
    title: `Reativar ${row.name}?`,
    body: `O acesso de <strong>${escapeHtml(row.email)}</strong> será restaurado.`,
    confirmLabel: "Reativar",
    confirmClass: "btn-success",
  });
  if (!ok) return;
  try {
    await updateUser(row.id, { active: true });
    toast(main, "Usuário reativado.", "success");
    await reload(api, main);
  } catch (e) {
    toast(main, errorMessage(e).message, "danger");
  }
}

async function onPermissions(api: GridApi, main: HTMLElement, row: UserResponse): Promise<void> {
  let all: { code: string; description: string }[];
  try {
    all = await fetchPermissions();
  } catch (e) {
    toast(main, errorMessage(e).message, "danger");
    return;
  }
  const codes = await permissionsDialog({ userName: row.name, current: row.permissions, all });
  if (!codes) return;
  try {
    await setUserPermissions(row.id, codes);
    toast(main, "Permissões atualizadas.", "success");
    await reload(api, main);
  } catch (e) {
    toast(main, errorMessage(e).message, "danger");
  }
}
