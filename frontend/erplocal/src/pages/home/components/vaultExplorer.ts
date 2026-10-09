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
import { getCurrentUser, isAdmin } from "../../../services/session.ts";
import { ApiError } from "../../../services/usersApi.ts";
import {
  deleteFile,
  downloadFile,
  formatSize,
  kindIcon,
  listFiles,
  listProjects,
  updateFile,
  uploadFile,
  type ProjectGroup,
  type VaultFile,
} from "../../../services/vaultApi.ts";
import { confirmDialog } from "./userDialogs.ts";
import { detailsDialog, editMetadataDialog, uploadDialog } from "./vaultDialogs.ts";

ModuleRegistry.registerModules([AllCommunityModule]);

type GridTheme = ReturnType<typeof themeQuartz.withPart>;
type View = "icons" | "details";
type ProjectFilter = { kind: "all" } | { kind: "none" } | { kind: "project"; code: string };

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

function canUseVault(): boolean {
  if (isAdmin()) return true;
  return getCurrentUser()?.permissions.includes("CanUseVault") ?? false;
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

function errorMessage(e: unknown): string {
  if (e instanceof ApiError) {
    if (e.status === 403) return "Sem permissão para esta ação.";
    return e.message;
  }
  return "Falha de comunicação com o backend.";
}

let currentApi: GridApi | null = null;
let themeObserver: MutationObserver | null = null;

function destroyGrid(): void {
  themeObserver?.disconnect();
  themeObserver = null;
  currentApi?.destroy();
  currentApi = null;
}

function statusBadge(params: ICellRendererParams<VaultFile>): string {
  const file = params.data;
  if (!file) return "";
  const cls =
    file.status === "APPLICATION"
      ? "bg-success"
      : file.status === "TBO"
        ? "bg-danger"
        : "bg-secondary";
  const pending = file.pendingApproval ? ' <span class="badge bg-warning text-dark">pendente</span>' : "";
  return `<span class="badge ${cls}">${escapeHtml(file.status)}</span>${pending}`;
}

function actionsCell(params: ICellRendererParams<VaultFile>): string {
  const row = params.data;
  if (!row) return "";
  const id = escapeHtml(row.id);
  const adminBtns = isAdmin()
    ? `<button type="button" class="btn btn-outline-primary" data-action="edit" data-id="${id}">Editar</button>
       <button type="button" class="btn btn-outline-danger" data-action="delete" data-id="${id}">Excluir</button>`
    : "";
  return `<div class="btn-group btn-group-sm" role="group">
    <button type="button" class="btn btn-outline-success" data-action="download" data-id="${id}">Baixar</button>
    <button type="button" class="btn btn-outline-secondary" data-action="details" data-id="${id}">Detalhes</button>
    ${adminBtns}
  </div>`;
}

const columnDefs: ColDef<VaultFile>[] = [
  {
    field: "name",
    headerName: "Nome",
    filter: true,
    cellRenderer: (p: ICellRendererParams<VaultFile>) =>
      `${kindIcon(p.data?.kind ?? "")} ${escapeHtml(p.value as string)}`,
  },
  { field: "projectCode", headerName: "Projeto", width: 140, filter: true },
  { field: "kind", headerName: "Tipo", width: 130 },
  {
    field: "sizeBytes",
    headerName: "Tamanho",
    width: 120,
    valueFormatter: (p) => formatSize((p.value as number) ?? 0),
  },
  { field: "versionNumber", headerName: "Versão", width: 90, valueFormatter: (p) => `v${p.value as number}` },
  {
    headerName: "Status",
    width: 160,
    valueGetter: (p) => p.data?.status,
    cellRenderer: statusBadge,
  },
  {
    field: "createdAt",
    headerName: "Criado em",
    width: 170,
    valueFormatter: (p) => (p.value ? new Date(p.value as string).toLocaleString("pt-BR") : "—"),
  },
  {
    headerName: "Ações",
    width: 330,
    sortable: false,
    filter: false,
    resizable: false,
    cellRenderer: actionsCell,
  },
];

interface ExplorerState {
  projects: ProjectGroup[];
  items: VaultFile[];
  project: ProjectFilter;
  search: string;
  sort: "name" | "recent" | "size";
  view: View;
  iconSize: number;
}

function filteredItems(state: ExplorerState): VaultFile[] {
  const term = state.search.trim().toLowerCase();
  let items = state.items;
  if (term) {
    items = items.filter(
      (f) =>
        f.name.toLowerCase().includes(term) ||
        (f.projectCode ?? "").toLowerCase().includes(term),
    );
  }
  const sorted = [...items];
  switch (state.sort) {
    case "name":
      sorted.sort((a, b) => a.name.localeCompare(b.name, "pt-BR"));
      break;
    case "recent":
      sorted.sort((a, b) => b.createdAt.localeCompare(a.createdAt));
      break;
    case "size":
      sorted.sort((a, b) => b.sizeBytes - a.sizeBytes);
      break;
  }
  return sorted;
}

function breadcrumbLabel(project: ProjectFilter): string {
  if (project.kind === "project") return project.code;
  if (project.kind === "none") return "Sem projeto";
  return "Todos";
}

export async function renderVault(main: HTMLElement): Promise<void> {
  destroyGrid();

  if (!canUseVault()) {
    toast(main, "Sem permissão para acessar o Cofre.", "warning");
    main.innerHTML = `<div class="alert alert-warning" role="alert">Sem permissão para acessar o Cofre.</div>`;
    window.setTimeout(() => {
      document.querySelector<HTMLButtonElement>('[data-section="inicio"]')?.click();
    }, 1200);
    return;
  }

  const state: ExplorerState = {
    projects: [],
    items: [],
    project: { kind: "all" },
    search: "",
    sort: "name",
    view: "icons",
    iconSize: 56,
  };

  main.innerHTML = `
    <div class="d-flex justify-content-between align-items-center mb-3">
      <div>
        <h1 class="mb-0">Cofre</h1>
        <p class="text-muted mb-0">Arquivos CAD e documentos por projeto.</p>
      </div>
      <button type="button" id="vault-upload" class="btn btn-primary">Enviar arquivo</button>
    </div>
    <div class="vault-layout">
      <aside class="vault-tree">
        <h6 class="vault-tree-title">Projetos</h6>
        <ul class="vault-tree-list" id="vault-tree"></ul>
      </aside>
      <div class="vault-main">
        <nav aria-label="breadcrumb"><ol class="breadcrumb" id="vault-crumb"></ol></nav>
        <div class="vault-toolbar">
          <input class="form-control vault-search" id="vault-search" placeholder="Pesquisar nesta pasta…" />
          <select class="form-select vault-sort" id="vault-sort">
            <option value="name">Nome A–Z</option>
            <option value="recent">Mais recentes</option>
            <option value="size">Maiores</option>
          </select>
          <div class="btn-group" role="group" aria-label="Modo de exibição">
            <button type="button" class="btn btn-outline-secondary active" id="vault-view-icons" title="Ícones">🔲</button>
            <button type="button" class="btn btn-outline-secondary" id="vault-view-details" title="Detalhes">☰</button>
          </div>
          <label class="vault-zoom" id="vault-zoom-wrap" title="Tamanho do ícone">
            🔍 <input type="range" id="vault-zoom" min="36" max="96" step="4" value="56" />
          </label>
        </div>
        <div id="vault-icons" class="vault-icons"></div>
        <div id="vault-grid" style="width: 100%;"></div>
      </div>
    </div>
  `;

  const treeEl = main.querySelector<HTMLElement>("#vault-tree")!;
  const crumbEl = main.querySelector<HTMLElement>("#vault-crumb")!;
  const iconsEl = main.querySelector<HTMLElement>("#vault-icons")!;
  const gridEl = main.querySelector<HTMLElement>("#vault-grid")!;
  const searchEl = main.querySelector<HTMLInputElement>("#vault-search")!;
  const sortEl = main.querySelector<HTMLSelectElement>("#vault-sort")!;
  const iconsBtn = main.querySelector<HTMLButtonElement>("#vault-view-icons")!;
  const detailsBtn = main.querySelector<HTMLButtonElement>("#vault-view-details")!;
  const zoomEl = main.querySelector<HTMLInputElement>("#vault-zoom")!;
  const zoomWrap = main.querySelector<HTMLElement>("#vault-zoom-wrap")!;

  const gridOptions: GridOptions<VaultFile> = {
    theme: gridTheme(),
    columnDefs,
    defaultColDef: { sortable: true, filter: true, resizable: true, flex: 1, minWidth: 110 },
    pagination: true,
    paginationPageSize: 20,
    domLayout: "autoHeight",
  };
  const api = createGrid(gridEl, gridOptions);
  currentApi = api;

  themeObserver = new MutationObserver(() => {
    api.setGridOption("theme", gridTheme());
  });
  themeObserver.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ["data-bs-theme"],
  });

  function paint(): void {
    // Árvore
    const ungrouped = state.items.filter((f) => !f.projectCode).length;
    treeEl.innerHTML = `
      <li><button type="button" data-proj="all" class="${state.project.kind === "all" ? "active" : ""}">📁 Todos</button></li>
      ${state.projects
        .map(
          (p) => `<li><button type="button" data-proj="${escapeHtml(p.code)}" class="${
            state.project.kind === "project" && state.project.code === p.code ? "active" : ""
          }">📁 ${escapeHtml(p.code)} <span class="badge bg-secondary">${p.fileCount}</span></button></li>`,
        )
        .join("")}
      <li><button type="button" data-proj="__none__" class="${state.project.kind === "none" ? "active" : ""}">📁 Sem projeto (${ungrouped})</button></li>
    `;
    // Breadcrumb
    crumbEl.innerHTML = `
      <li class="breadcrumb-item">Cofre</li>
      <li class="breadcrumb-item active">${escapeHtml(breadcrumbLabel(state.project))}</li>
    `;
    // Visões
    const showIcons = state.view === "icons";
    iconsEl.style.display = showIcons ? "" : "none";
    gridEl.style.display = showIcons ? "none" : "";
    zoomWrap.style.visibility = showIcons ? "visible" : "hidden";
    iconsBtn.classList.toggle("active", showIcons);
    detailsBtn.classList.toggle("active", !showIcons);

    const items = filteredItems(state);
    if (showIcons) {
      iconsEl.style.setProperty("--vault-icon-size", `${state.iconSize}px`);
      iconsEl.innerHTML =
        items.length === 0
          ? `<p class="text-muted">Nenhum arquivo aqui.</p>`
          : items
              .map(
                (f) => `
            <div class="vault-card" data-id="${escapeHtml(f.id)}" title="${escapeHtml(f.name)}">
              <div class="vault-card-icon">${kindIcon(f.kind)}</div>
              <div class="vault-card-name">${escapeHtml(f.name)}</div>
              <div class="vault-card-meta">${escapeHtml(f.kind)} • ${formatSize(f.sizeBytes)}</div>
              <div class="vault-card-actions">
                <button type="button" class="btn btn-sm btn-outline-success" data-action="download" data-id="${escapeHtml(f.id)}">Baixar</button>
                <button type="button" class="btn btn-sm btn-outline-secondary" data-action="details" data-id="${escapeHtml(f.id)}">Detalhes</button>
                ${
                  isAdmin()
                    ? `<button type="button" class="btn btn-sm btn-outline-primary" data-action="edit" data-id="${escapeHtml(f.id)}">Editar</button>
                       <button type="button" class="btn btn-sm btn-outline-danger" data-action="delete" data-id="${escapeHtml(f.id)}">Excluir</button>`
                    : ""
                }
              </div>
            </div>`,
              )
              .join("");
    } else {
      api.setGridOption("rowData", items);
      if (items.length === 0) api.showNoRowsOverlay();
      else api.hideOverlay();
    }
  }

  function findItem(id: string): VaultFile | undefined {
    return state.items.find((f) => f.id === id);
  }

  async function reload(): Promise<void> {
    try {
      state.projects = await listProjects();
      const projectCode =
        state.project.kind === "project"
          ? state.project.code
          : state.project.kind === "none"
            ? "__none__"
            : null;
      if (projectCode === "__none__") {
        const page = await listFiles(null);
        state.items = page.items.filter((f) => !f.projectCode);
      } else {
        const page = await listFiles(projectCode);
        state.items = page.items;
      }
    } catch (e) {
      toast(main, errorMessage(e), "danger");
      state.items = [];
    }
    paint();
  }

  async function onAction(action: string, id: string): Promise<void> {
    const file = findItem(id);
    if (!file) return;
    switch (action) {
      case "download":
        try {
          await downloadFile(file);
        } catch (e) {
          toast(main, errorMessage(e), "danger");
        }
        break;
      case "details":
        detailsDialog(file, file.uploadedBy.slice(0, 8));
        break;
      case "edit": {
        const patch = await editMetadataDialog(file, state.projects);
        if (!patch) return;
        try {
          await updateFile(file.id, patch);
          toast(main, "Arquivo atualizado.", "success");
          await reload();
        } catch (e) {
          toast(main, errorMessage(e), "danger");
        }
        break;
      }
      case "delete": {
        const ok = await confirmDialog({
          title: `Excluir ${file.name}?`,
          body: `O arquivo <strong>${escapeHtml(file.name)}</strong> será removido do cofre (bytes + metadados).`,
          confirmLabel: "Excluir",
          confirmClass: "btn-danger",
        });
        if (!ok) return;
        try {
          await deleteFile(file.id);
          toast(main, "Arquivo excluído.", "success");
          await reload();
        } catch (e) {
          toast(main, errorMessage(e), "danger");
        }
        break;
      }
    }
  }

  function onActionClick(ev: Event): void {
    const btn = (ev.target as HTMLElement).closest<HTMLElement>("[data-action]");
    if (btn?.dataset.action && btn.dataset.id) {
      void onAction(btn.dataset.action, btn.dataset.id);
    }
  }

  iconsEl.addEventListener("click", onActionClick);
  gridEl.addEventListener("click", onActionClick);

  treeEl.addEventListener("click", (ev) => {
    const proj = (ev.target as HTMLElement).closest<HTMLElement>("[data-proj]");
    if (!proj?.dataset.proj) return;
    const key = proj.dataset.proj;
    state.project =
      key === "all" ? { kind: "all" } : key === "__none__" ? { kind: "none" } : { kind: "project", code: key };
    void reload();
  });

  searchEl.addEventListener("input", () => {
    state.search = searchEl.value;
    paint();
  });
  sortEl.addEventListener("change", () => {
    state.sort = sortEl.value as ExplorerState["sort"];
    paint();
  });
  iconsBtn.addEventListener("click", () => {
    state.view = "icons";
    paint();
  });
  detailsBtn.addEventListener("click", () => {
    state.view = "details";
    paint();
  });
  zoomEl.addEventListener("input", () => {
    state.iconSize = Number(zoomEl.value);
    paint();
  });

  main
    .querySelector<HTMLButtonElement>("#vault-upload")
    ?.addEventListener("click", () => {
      void (async () => {
        const data = await uploadDialog(state.projects);
        if (!data) return;
        try {
          await uploadFile(data.file, data.projectCode, data.externalReference);
          toast(main, "Arquivo enviado.", "success");
          await reload();
        } catch (e) {
          toast(main, errorMessage(e), "danger");
        }
      })();
    });

  await reload();
}
