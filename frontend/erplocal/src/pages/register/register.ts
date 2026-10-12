import "../login/style.css";
import htmlContent from "./index.html?raw";
import { register, ApiError } from "../../services/authApi.ts";
import { saveSession } from "../../services/session.ts";

export function getRegisterPage(): string {
  document.addEventListener("DOMContentLoaded", () => {
    initRegisterPage();
  });
  return htmlContent;
}

function fieldError(input: HTMLInputElement, message: string | null): void {
  input.classList.toggle("is-invalid", message !== null);
  const feedback = input.parentElement?.querySelector(".invalid-feedback");
  if (feedback) feedback.textContent = message ?? "";
}

function initRegisterPage(): void {
  const form = document.querySelector<HTMLFormElement>("#register-form");
  if (!form) return;

  form.addEventListener("submit", (ev) => {
    ev.preventDefault();
    const nameInput = document.querySelector<HTMLInputElement>("#register-name")!;
    const emailInput = document.querySelector<HTMLInputElement>("#register-email")!;
    const passwordInput = document.querySelector<HTMLInputElement>("#register-password")!;
    const errorBox = document.querySelector<HTMLElement>("#register-error")!;

    const name = nameInput.value.trim();
    const email = emailInput.value.trim();
    const password = passwordInput.value;

    let valid = true;
    if (name.length === 0) {
      fieldError(nameInput, "Nome é obrigatório.");
      valid = false;
    } else fieldError(nameInput, null);

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      fieldError(emailInput, "Informe um e-mail válido.");
      valid = false;
    } else fieldError(emailInput, null);

    if (password.length < 6) {
      fieldError(passwordInput, "Senha com no mínimo 6 caracteres.");
      valid = false;
    } else fieldError(passwordInput, null);

    if (!valid) return;

    register(name, email, password)
      .then((result) => {
        saveSession(result.token, result.user);
        window.location.href = "/home";
      })
      .catch((e: unknown) => {
        errorBox.textContent =
          e instanceof ApiError && e.status === 409
            ? "Este e-mail já está cadastrado."
            : "Falha no cadastro.";
        errorBox.classList.remove("d-none");
      });
  });
}
