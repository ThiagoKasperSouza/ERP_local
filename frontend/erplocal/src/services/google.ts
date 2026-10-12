// Login com Google com detecção de ambiente:
// - navegador / `tauri dev` (http://localhost:...) → botão GIS (idToken direto);
// - app Tauri instalado (tauri://...) → Authorization Code + PKCE: abre o
//   navegador do sistema, captura o code no loopback 127.0.0.1 (comando Rust)
//   e o backend troca pelo idToken com o client_secret (nunca no front).

declare global {
  interface Window {
    google?: {
      accounts: {
        id: {
          initialize(opts: {
            client_id: string;
            callback: (resp: { credential?: string }) => void;
            use_fedcm_for_prompt?: boolean;
          }): void;
          renderButton(el: HTMLElement, opts?: Record<string, unknown>): void;
        };
      };
    };
  }
}

let scriptPromise: Promise<void> | null = null;

function loadGis(): Promise<void> {
  if (window.google?.accounts?.id) return Promise.resolve();
  if (!scriptPromise) {
    scriptPromise = new Promise((resolve, reject) => {
      const script = document.createElement("script");
      script.src = "https://accounts.google.com/gsi/client";
      script.async = true;
      script.defer = true;
      script.onload = () => resolve();
      script.onerror = () => reject(new Error("Falha ao carregar Google Identity Services"));
      document.head.appendChild(script);
    });
  }
  return scriptPromise;
}

export function googleClientId(): string {
  return (import.meta.env.VITE_GOOGLE_CLIENT_ID as string | undefined)?.trim() ?? "";
}

/** true quando roda dentro do runtime Tauri (dev ou instalado). No `tauri dev`
 * o protocolo ainda é http:, mas o webview bloqueia popups do GIS — por isso
 * o nativo (PKCE + navegador do sistema) vale para os dois casos. */
export function isTauriNative(): boolean {
  return (
    window.location.protocol === "tauri:" ||
    "__TAURI_INTERNALS__" in window ||
    "__TAURI__" in window
  );
}

/** Renderiza o botão do Google no elemento; resolve com o idToken ou null se cancelar/indisponível. */
export async function renderGoogleButton(container: HTMLElement): Promise<string | null> {
  const clientId = googleClientId();
  if (!clientId) {
    container.innerHTML = `<p class="text-muted small">Login com Google indisponível (VITE_GOOGLE_CLIENT_ID vazio).</p>`;
    return null;
  }
  try {
    await loadGis();
  } catch {
    container.innerHTML = `<p class="text-muted small">Não foi possível carregar o login do Google.</p>`;
    return null;
  }
  return new Promise((resolve) => {
    window.google!.accounts.id.initialize({
      client_id: clientId,
      callback: (resp) => resolve(resp.credential ?? null),
      // Prefere FedCM (sem popup) onde o navegador suporta
      use_fedcm_for_prompt: true,
    });
    window.google!.accounts.id.renderButton(container, {
      theme: "outline",
      size: "large",
      text: "signin_with",
      width: 280,
    });
  });
}

function base64Url(bytes: Uint8Array): string {
  let binary = "";
  bytes.forEach((b) => {
    binary += String.fromCharCode(b);
  });
  return btoa(binary).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}

function randomString(length: number): string {
  const bytes = new Uint8Array(length);
  crypto.getRandomValues(bytes);
  return base64Url(bytes).slice(0, length);
}

export interface NativeCode {
  code: string;
  codeVerifier: string;
  redirectUri: string;
}

/**
 * Fluxo nativo (app instalado): PKCE + navegador do sistema + loopback.
 * Abre o Google, aguarda o redirect em 127.0.0.1 e devolve o code
 * (o state é conferido aqui; expira em ~3 min no Rust).
 */
export async function signInWithGoogleNative(clientId: string): Promise<NativeCode> {
  const { invoke } = await import("@tauri-apps/api/core");
  const { openUrl } = await import("@tauri-apps/plugin-opener");

  const codeVerifier = randomString(64);
  const challengeBytes = new Uint8Array(
    await crypto.subtle.digest("SHA-256", new TextEncoder().encode(codeVerifier)),
  );
  const state = randomString(32);

  const port = (await invoke<number>("oauth_bind")) as number;
  const redirectUri = `http://127.0.0.1:${port}/callback`;
  const authUrl =
    `https://accounts.google.com/o/oauth2/v2/auth?response_type=code` +
    `&client_id=${encodeURIComponent(clientId)}` +
    `&redirect_uri=${encodeURIComponent(redirectUri)}` +
    `&scope=${encodeURIComponent("openid email profile")}` +
    `&state=${encodeURIComponent(state)}` +
    `&code_challenge=${encodeURIComponent(base64Url(challengeBytes))}` +
    `&code_challenge_method=S256` +
    `&access_type=online`;

  await openUrl(authUrl);
  const result = (await invoke<{ code: string; state: string }>("oauth_wait_code")) as {
    code: string;
    state: string;
  };
  if (result.state !== state) {
    throw new Error("Resposta de login inválida (state).");
  }
  return { code: result.code, codeVerifier, redirectUri };
}
