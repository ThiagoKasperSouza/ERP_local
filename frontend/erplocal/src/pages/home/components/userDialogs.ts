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

/** Confirmação genérica (ex.: desativar usuário). Resolve false ao fechar sem confirmar. */
export function confirmDialog(options: {
  title: string;
  body: string;
  confirmLabel: string;
  confirmClass?: string;
}): Promise<boolean> {
  return new Promise((resolve) => {
    const modalEl = mountModal(
      options.title,
      `<p>${options.body}</p>`,
      `<button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
       <button type="button" class="btn ${options.confirmClass ?? "btn-primary"}" id="dlg-confirm">${options.confirmLabel}</button>`,
    );
    let done = false;
    modalEl
      .querySelector<HTMLButtonElement>("#dlg-confirm")
      ?.addEventListener("click", () => {
        done = true;
        modalEl.querySelector<HTMLButtonElement>(".btn-close")?.click();
        resolve(true);
      });
    modalEl.addEventListener("hidden.bs.modal", () => {
      if (!done) resolve(false);
    });
    showModal(modalEl);
  });
}

export interface UserFormData {
  name: string;
  email: string;
  password: string;
  role: string;
}

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function fieldError(input: HTMLInputElement | HTMLSelectElement, message: string | null): void {
  input.classList.toggle("is-invalid", message !== null);
  const feedback = input.parentElement?.querySelector(".invalid-feedback");
  if (feedback) feedback.textContent = message ?? "";
}

/** Formulário criar/editar com validação inline. Edição omite email e senha é opcional. */
export function userFormDialog(options: {
  title: string;
  initial?: { name: string; email: string; role: string };
  mode: "create" | "edit";
}): Promise<UserFormData | null> {
  const initial = options.initial ?? { name: "", email: "", role: "USER" };
  return new Promise((resolve) => {
    const modalEl = mountModal(
      options.title,
      `<form id="dlg-user-form" novalidate>
        <div class="mb-3">
          <label class="form-label" for="dlg-name">Nome</label>
          <input class="form-control" id="dlg-name" value="${initial.name}" />
          <div class="invalid-feedback"></div>
        </div>
        ${
          options.mode === "create"
            ? `<div class="mb-3">
            <label class="form-label" for="dlg-email">E-mail</label>
            <input class="form-control" id="dlg-email" type="email" value="${initial.email}" />
            <div class="invalid-feedback"></div>
          </div>`
            : ""
        }
        <div class="mb-3">
          <label class="form-label" for="dlg-password">Senha${
            options.mode === "edit" ? " (em branco mantém a atual)" : ""
          }</label>
          <input class="form-control" id="dlg-password" type="password" autocomplete="new-password" />
          <div class="invalid-feedback"></div>
        </div>
        <div class="mb-3">
          <label class="form-label" for="dlg-role">Papel</label>
          <select class="form-select" id="dlg-role">
            <option value="USER"${initial.role === "USER" ? " selected" : ""}>user</option>
            <option value="ADMIN"${initial.role === "ADMIN" ? " selected" : ""}>admin</option>
          </select>
        </div>
      </form>`,
      `<button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
       <button type="button" class="btn btn-primary" id="dlg-save">Salvar</button>`,
    );

    const nameInput = modalEl.querySelector<HTMLInputElement>("#dlg-name")!;
    const emailInput = modalEl.querySelector<HTMLInputElement>("#dlg-email");
    const passwordInput = modalEl.querySelector<HTMLInputElement>("#dlg-password")!;
    const roleInput = modalEl.querySelector<HTMLSelectElement>("#dlg-role")!;
    let done = false;

    const finish = (value: UserFormData | null): void => {
      done = true;
      modalEl.querySelector<HTMLButtonElement>(".btn-close")?.click();
      resolve(value);
    };

    modalEl
      .querySelector<HTMLButtonElement>("#dlg-save")
      ?.addEventListener("click", () => {
        const name = nameInput.value.trim();
        const email = emailInput?.value.trim() ?? initial.email;
        const password = passwordInput.value;
        const role = roleInput.value;

        let valid = true;
        if (name.length === 0) {
          fieldError(nameInput, "Nome é obrigatório.");
          valid = false;
        } else fieldError(nameInput, null);

        if (emailInput) {
          if (!EMAIL_RE.test(email)) {
            fieldError(emailInput, "Informe um e-mail válido.");
            valid = false;
          } else fieldError(emailInput, null);
        }

        if (options.mode === "create" && password.length < 6) {
          fieldError(passwordInput, "Senha com no mínimo 6 caracteres.");
          valid = false;
        } else if (password.length > 0 && password.length < 6) {
          fieldError(passwordInput, "Senha com no mínimo 6 caracteres.");
          valid = false;
        } else fieldError(passwordInput, null);

        if (!valid) return;
        finish({ name, email, password, role });
      });

    modalEl.addEventListener("hidden.bs.modal", () => {
      if (!done) resolve(null);
    });
    showModal(modalEl);
  });
}

/** Seleção de permissões do usuário (checkboxes a partir da lista canônica). */
export function permissionsDialog(options: {
  userName: string;
  current: string[];
  all: { code: string; description: string }[];
}): Promise<string[] | null> {
  return new Promise((resolve) => {
    const currentSet = new Set(options.current);
    const modalEl = mountModal(
      `Permissões de ${options.userName}`,
      `<div class="list-group">
        ${options.all
          .map(
            (p) => `
          <label class="list-group-item">
            <input class="form-check-input me-2" type="checkbox" value="${p.code}"${
              currentSet.has(p.code) ? " checked" : ""
            } />
            <code>${p.code}</code>
            <small class="text-muted d-block">${p.description}</small>
          </label>`,
          )
          .join("")}
      </div>`,
      `<button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
       <button type="button" class="btn btn-primary" id="dlg-save">Salvar</button>`,
    );
    let done = false;
    modalEl
      .querySelector<HTMLButtonElement>("#dlg-save")
      ?.addEventListener("click", () => {
        const codes = Array.from(
          modalEl.querySelectorAll<HTMLInputElement>("input[type=checkbox]:checked"),
        ).map((c) => c.value);
        done = true;
        modalEl.querySelector<HTMLButtonElement>(".btn-close")?.click();
        resolve(codes);
      });
    modalEl.addEventListener("hidden.bs.modal", () => {
      if (!done) resolve(null);
    });
    showModal(modalEl);
  });
}
