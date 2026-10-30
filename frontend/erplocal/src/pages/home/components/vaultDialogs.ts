import { downloadFile, formatSize, kindIcon, updateFile, type ProjectGroup, type VaultFile } from "../../../services/vaultApi.ts";
import { getCurrentUser, isAdmin } from "../../../services/session.ts";

declare global {
  interface Window {
    bootstrap: {
      Modal: new (
        el: Element,
        opts?: Record<string, unknown>,
      ) => { show(): void; hide(): void };
    };
  }
}

function mountModal(title: string, bodyHtml: string, footerHtml: string): HTMLElement {
  const wrapper = document.createElement("div");
  wrapper.innerHTML = `
    <div class="modal fade" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">${title}</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Fechar"></button>
          </div>
          <div class="modal-body">${bodyHtml}</div>
          <div class="modal-footer">${footerHtml}</div>
        </div>
      </div>
    </div>`;
  const modalEl = wrapper.firstElementChild as HTMLElement;
  document.body.appendChild(modalEl);
  return modalEl;
}

function showModal(modalEl: HTMLElement): void {
  const modal = new window.bootstrap.Modal(modalEl);
  modalEl.addEventListener("hidden.bs.modal", () => modalEl.remove(), { once: true });
  modal.show();
}

function closeModal(modalEl: HTMLElement): void {
  modalEl.querySelector<HTMLButtonElement>(".btn-close")?.click();
}

function escapeHtml(value: string): string {
  return value
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

const ALLOWED_EXT = [
  "sldprt", "sldasm", "slddrw", "ipt", "iam", "idw", "ipn", "prt", "asm", "drw",
  "dwg", "dxf", "pdf", "csv", "xls", "xlsx", "ods", "ebom", "json",
  "step", "stp", "iges", "igs",
];

export interface UploadData {
  file: File;
  ebom?: File;
  projectCode: string;
  externalReference: string;
  isAssembly: boolean;
  approverId?: string;
}
/** Upload com projeto (autocomplete) + referência externa opcional. */
export function uploadDialog(projects: ProjectGroup[], users: { id: string; name: string }[] = []): Promise<UploadData | null> {
  return new Promise((resolve) => {
    const modalEl = mountModal(
      "Enviar arquivo",
      `<form id="dlg-upload-form" novalidate>
        <div class="mb-3">
          <label class="form-label" for="dlg-file">Arquivo</label>
          <input class="form-control" id="dlg-file" type="file" />
          <div class="invalid-feedback"></div>
          <div class="form-text">Formatos: CAD (SolidWorks, Inventor, Creo, STEP), DWG, PDF, CSV, planilhas, EBOM.</div>
        </div>
        <div class="mb-3">
          <label class="form-label" for="dlg-project">Projeto (opcional)</label>
          <input class="form-control" id="dlg-project" list="dlg-project-list" placeholder="Ex.: MOTOR-X12" />
          <datalist id="dlg-project-list">
            ${projects.map((p) => `<option value="${p.code}"></option>`).join("")}
          </datalist>
        </div>
        <div class="mb-3 form-check">
          <input class="form-check-input" type="checkbox" id="dlg-is-assembly" />
          <label class="form-check-label" for="dlg-is-assembly">É uma montagem (assembly)</label>
        </div>
        <div class="mb-3" id="dlg-ebom-group" style="display:none;">
          <label class="form-label" for="dlg-ebom">EBOM (CSV obrigatório)</label>
          <input class="form-control" id="dlg-ebom" type="file" accept=".csv" />
          <div class="invalid-feedback"></div>
        </div>
        <div class="mb-3">
          <label class="form-label" for="dlg-approver">Aprovador (opcional)</label>
          <select class="form-select" id="dlg-approver">
            <option value="">— nenhum —</option>
            ${users.map((u) => `<option value="${u.id}">${escapeHtml(u.name)}</option>`).join("")}
          </select>
        </div>
      </form>`,
      `<button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
       <button type="button" class="btn btn-primary" id="dlg-save">Enviar</button>`,
    );
    const approverInput = modalEl.querySelector<HTMLSelectElement>("#dlg-approver")!;
    const fileInput = modalEl.querySelector<HTMLInputElement>("#dlg-file")!;
    const projectInput = modalEl.querySelector<HTMLInputElement>("#dlg-project")!;
    const isAssemblyInput = modalEl.querySelector<HTMLInputElement>("#dlg-is-assembly")!;
    const ebomGroup = modalEl.querySelector<HTMLElement>("#dlg-ebom-group")!;
    const ebomInput = modalEl.querySelector<HTMLInputElement>("#dlg-ebom")!;
    isAssemblyInput.addEventListener("change", () => {
      ebomGroup.style.display = isAssemblyInput.checked ? "block" : "none";
    });
    let done = false;
    const finish = (value: UploadData | null): void => {
      done = true;
      closeModal(modalEl);
      resolve(value);
    };

    modalEl
      .querySelector<HTMLButtonElement>("#dlg-save")
      ?.addEventListener("click", () => {
        const file = fileInput.files?.[0];
        const feedback = fileInput.parentElement?.querySelector(".invalid-feedback");
        if (!file) {
          fileInput.classList.add("is-invalid");
          if (feedback) feedback.textContent = "Escolha um arquivo.";
          return;
        }
        const ext = (file.name.split(".").pop() ?? "").toLowerCase();
        if (!ALLOWED_EXT.includes(ext)) {
          fileInput.classList.add("is-invalid");
          if (feedback) feedback.textContent = `Extensão .${ext || "?"} não permitida.`;
          return;
        }
        const isAssembly = isAssemblyInput.checked;
        const ebomFile = ebomInput.files?.[0];
        if (isAssembly && !ebomFile) {
          ebomInput.classList.add("is-invalid");
          const ebomFeedback = ebomInput.parentElement?.querySelector(".invalid-feedback");
          if (ebomFeedback) ebomFeedback.textContent = "EBOM obrigatório para montagem.";
          return;
        }
        if (isAssembly) {
          ebomInput.classList.remove("is-invalid");
        }
        fileInput.classList.remove("is-invalid");
        finish({ file, ebom: ebomFile ?? undefined, projectCode: projectInput.value.trim(), externalReference: "", isAssembly, approverId: approverInput.value || undefined });
      });
    modalEl.addEventListener("hidden.bs.modal", () => {
      if (!done) resolve(null);
    });
    showModal(modalEl);
  });
}

export interface MetadataPatch {
  name: string;
  projectCode: string | null;
  status: string;
  externalReference: string | null;
}

/** Edição de metadados (admin). Versão e aprovação são das US9/US10. */
export function editMetadataDialog(file: VaultFile, projects: ProjectGroup[]): Promise<MetadataPatch | null> {
  return new Promise((resolve) => {
    const modalEl = mountModal(
      `Editar ${escapeHtml(file.name)}`,
      `<form novalidate>
        <div class="mb-3">
          <label class="form-label" for="dlg-m-name">Nome</label>
          <input class="form-control" id="dlg-m-name" value="${escapeHtml(file.name)}" />
          <div class="invalid-feedback"></div>
        </div>
        <div class="mb-3">
          <label class="form-label" for="dlg-m-project">Projeto</label>
          <input class="form-control" id="dlg-m-project" list="dlg-m-project-list" value="${file.projectCode ?? ""}" />
          <datalist id="dlg-m-project-list">
            ${projects.map((p) => `<option value="${p.code}"></option>`).join("")}
          </datalist>
          <div class="form-text">Em branco = sem projeto.</div>
        </div>
        <div class="mb-3">
          <label class="form-label" for="dlg-m-status">Status</label>
          <select class="form-select" id="dlg-m-status">
            ${["PROTOTYPE", "APPLICATION", "TBO"]
              .map((s) => `<option value="${s}"${file.status === s ? " selected" : ""}>${s}</option>`)
              .join("")}
          </select>
        </div>
        <div class="mb-3">
          <label class="form-label" for="dlg-m-extref">Referência externa</label>
          <input class="form-control" id="dlg-m-extref" value="${file.externalReference ?? ""}" />
        </div>
      </form>`,
      `<button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
       <button type="button" class="btn btn-primary" id="dlg-save">Salvar</button>`,
    );
    const nameInput = modalEl.querySelector<HTMLInputElement>("#dlg-m-name")!;
    const projectInput = modalEl.querySelector<HTMLInputElement>("#dlg-m-project")!;
    const statusInput = modalEl.querySelector<HTMLSelectElement>("#dlg-m-status")!;
    const extRefInput = modalEl.querySelector<HTMLInputElement>("#dlg-m-extref")!;
    let done = false;

    modalEl
      .querySelector<HTMLButtonElement>("#dlg-save")
      ?.addEventListener("click", () => {
        const name = nameInput.value.trim();
        if (name.length === 0) {
          nameInput.classList.add("is-invalid");
          const fb = nameInput.parentElement?.querySelector(".invalid-feedback");
          if (fb) fb.textContent = "Nome é obrigatório.";
          return;
        }
        done = true;
        closeModal(modalEl);
        resolve({
          name,
          projectCode: projectInput.value.trim().length > 0 ? projectInput.value.trim() : null,
          status: statusInput.value,
          externalReference: extRefInput.value.trim().length > 0 ? extRefInput.value.trim() : null,
        });
      });
    modalEl.addEventListener("hidden.bs.modal", () => {
      if (!done) resolve(null);
    });
    showModal(modalEl);
  });
}

function row(label: string, value: string): string {
  return `<dt class="col-sm-4">${label}</dt><dd class="col-sm-8">${value}</dd>`;
}

/** Painel de metadados do arquivo (critérios US3). */
export function detailsDialog(file: VaultFile, uploaderName: string): void {
  const modalEl = mountModal(
    `${kindIcon(file.kind)} ${escapeHtml(file.name)}`,
    `<dl class="row mb-0">
      ${row("Tipo", `${file.kind} <span class="text-muted">(${escapeHtml(file.mimeType)})</span>`)}
      ${row("Tamanho", formatSize(file.sizeBytes))}
      ${row("Versão", `v${file.versionNumber}`)}
      ${row("Status", file.status + (file.pendingApproval ? ' <span class="badge bg-warning text-dark">pendente aprovação</span>' : ""))}
      ${row("Aprovação", file.pendingApproval ? "Pendente" : "Aprovado")}
      ${row("Projeto", file.projectCode ? escapeHtml(file.projectCode) : "<span class='text-muted'>—</span>")}
      ${row("Enviado por", escapeHtml(uploaderName))}
      ${row("Criado em", new Date(file.createdAt).toLocaleString("pt-BR"))}
      ${row("Alterado em", new Date(file.updatedAt).toLocaleString("pt-BR"))}
      ${row("Ref. externa", file.externalReference ? escapeHtml(file.externalReference) : "<span class='text-muted'>—</span>")}
    </dl>
    <div class="mt-3">
      <h6>Componentes / Filhos</h6>
      <div style="max-height: 200px; overflow-y: auto; border: 1px solid #dee2e6; border-radius: 4px; padding: 8px;">
        <ul class="list-group list-group-flush" id="dlg-children">
          ${
            (file.children ?? []).length === 0
              ? `<li class="list-group-item text-muted">Nenhum componente.</li>`
              : (file.children ?? [])
                  .map(
                    (c) => `<li class="list-group-item d-flex justify-content-between align-items-center">
                      <span>${kindIcon(c.kind)} ${escapeHtml(c.name)}</span>
                      <button type="button" class="btn btn-sm btn-outline-success" data-child-download="${escapeHtml(c.id)}">Baixar</button>
                    </li>`,
                  )
                  .join("")
          }
        </ul>
      </div>
    </div>`,
    `<button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Fechar</button>
   ${ (isAdmin() || (getCurrentUser()?.permissions.includes("CanApprove") ?? false)) && file.pendingApproval ? `<button type="button" class="btn btn-success" id="dlg-approve">Aprovar</button>
   <button type="button" class="btn btn-danger" id="dlg-reject">Rejeitar</button>` : ""}`,
  );
  showModal(modalEl);
  modalEl.addEventListener("click", async (ev) => {
    const btn = (ev.target as HTMLElement).closest<HTMLElement>("[data-child-download]");
    if (btn?.dataset.childDownload) {
      const child = (file.children ?? []).find((c) => c.id === btn.dataset.childDownload);
      if (child) void downloadFile(child);
      return;
    }
    const approveBtn = (ev.target as HTMLElement).closest<HTMLElement>("#dlg-approve");
    if (approveBtn) {
      try {
        await updateFile(file.id, { pendingApproval: false });
        window.location.reload();
      } catch (e) {
        // ignore
      }
      return;
    }
    const rejectBtn = (ev.target as HTMLElement).closest<HTMLElement>("#dlg-reject");
    if (rejectBtn) {
      try {
        await updateFile(file.id, { pendingApproval: false, status: "TBO" });
        window.location.reload();
      } catch (e) {
        // ignore
      }
    }
  });
}
