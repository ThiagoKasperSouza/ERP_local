import "./style.css";
import htmlContent from "./index.html?raw";
import { login, loginWithGoogle, loginWithGoogleCode, ApiError } from "../../services/authApi.ts";
import { saveSession } from "../../services/session.ts";
import {
  googleClientId,
  isTauriNative,
  renderGoogleButton,
  signInWithGoogleNative,
} from "../../services/google.ts";

export function getLoginPage(): string {
  document.addEventListener("DOMContentLoaded", () => {
    void initLoginPage();
  });
  return htmlContent;
}

function showError(message: string): void {
  const el = document.querySelector<HTMLElement>("#login-error");
  if (!el) return;
  el.textContent = message;
  el.classList.remove("d-none");
}

async function initLoginPage(): Promise<void> {
  const form = document.querySelector<HTMLFormElement>("#login-form");
  if (!form) return;

  form.addEventListener("submit", (ev) => {
    ev.preventDefault();
    const email = document.querySelector<HTMLInputElement>("#login-email")!.value.trim();
    const password = document.querySelector<HTMLInputElement>("#login-password")!.value;
    if (!email || !password) {
      showError("Informe e-mail e senha.");
      return;
    }
    login(email, password)
      .then((result) => {
        saveSession(result.token, result.user);
        window.location.href = "/home";
      })
      .catch((e: unknown) => {
        showError(e instanceof ApiError && e.status === 401 ? "E-mail ou senha inválidos." : "Falha no login.");
      });
  });

  const container = document.querySelector<HTMLElement>("#google-button");
  if (!container) return;

  if (isTauriNative()) {
    // App instalado: PKCE + navegador do sistema (GIS não funciona em tauri://).
    if (!googleClientId()) {
      container.innerHTML = `<p class="text-muted small">Login com Google indisponível (VITE_GOOGLE_CLIENT_ID vazio).</p>`;
      return;
    }
    const btn = document.createElement("button");
    btn.type = "button";
    btn.className = "btn btn-outline-secondary w-100";
    btn.textContent = "Entrar com Google";
    btn.addEventListener("click", () => {
      btn.disabled = true;
      btn.textContent = "Aguardando o navegador…";
      signInWithGoogleNative(googleClientId())
        .then((native) => loginWithGoogleCode(native.code, native.codeVerifier, native.redirectUri))
        .then((result) => {
          saveSession(result.token, result.user);
          window.location.href = "/home";
        })
        .catch(() => {
          showError("Falha no login com Google.");
          btn.disabled = false;
          btn.textContent = "Entrar com Google";
        });
    });
    container.appendChild(btn);
    return;
  }

  const idToken = await renderGoogleButton(container);
  if (idToken) {
    try {
      const result = await loginWithGoogle(idToken);
      saveSession(result.token, result.user);
      window.location.href = "/home";
    } catch {
      showError("Falha no login com Google.");
    }
  }
}
