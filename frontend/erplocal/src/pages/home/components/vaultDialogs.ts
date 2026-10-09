import { formatSize, kindIcon, type ProjectGroup, type VaultFile } from "../../../services/vaultApi.ts";

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
  projectCode: string;
  externalReference: string;
}

/** Upload com projeto (autocomplete) + referência externa opcional. */
export function uploadDialog(projects: ProjectGroup[]): Promise<UploadData | null> {
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
        <div class="mb-3">
          <label class="form-label" for="dlg-extref">Referência externa (opcional)</label>
          <input class="form-control" id="dlg-extref" placeholder="Código EBOM/MBOM…" />
        </div>
      </form>`,
      `<button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
       <button type="button" class="btn btn-primary" id="dlg-save">Enviar</button>`,
    );
    const fileInput = modalEl.querySelector<HTMLInputElement>("#dlg-file")!;
    const projectInput = modalEl.querySelector<HTMLInputElement>("#dlg-project")!;
    const extRefInput = modalEl.querySelector<HTMLInputElement>("#dlg-extref")!;
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
        fileInput.classList.remove("is-invalid");
        finish({ file, projectCode: projectInput.value.trim(), externalReference: extRefInput.value.trim() });
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
      ${row("Projeto", file.projectCode ? escapeHtml(file.projectCode) : "<span class='text-muted'>—</span>")}
      ${row("Enviado por", escapeHtml(uploaderName))}
      ${row("Criado em", new Date(file.createdAt).toLocaleString("pt-BR"))}
      ${row("Alterado em", new Date(file.updatedAt).toLocaleString("pt-BR"))}
      ${row("Ref. externa", file.externalReference ? escapeHtml(file.externalReference) : "<span class='text-muted'>—</span>")}
    </dl>`,
    `<button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Fechar</button>`,
  );
  showModal(modalEl);
}
