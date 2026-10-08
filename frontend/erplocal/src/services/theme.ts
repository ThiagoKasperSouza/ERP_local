export type Theme = "light" | "dark";

const STORAGE_KEY = "erplocal-theme";

export function getTheme(): Theme {
  try {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored === "light" || stored === "dark") return stored;
  } catch {
    // localStorage indisponível (ex. webview restrito) → segue no claro
  }
  return "light";
}

export function applyTheme(theme: Theme): void {
  document.documentElement.setAttribute("data-bs-theme", theme);
  try {
    localStorage.setItem(STORAGE_KEY, theme);
  } catch {
    // ignora falha de persistência
  }
}

/** Aplica o tema salvo antes da primeira renderização (evita flash). */
export function initTheme(): Theme {
  const theme = getTheme();
  document.documentElement.setAttribute("data-bs-theme", theme);
  return theme;
}

export function toggleTheme(): Theme {
  const next: Theme = getTheme() === "dark" ? "light" : "dark";
  applyTheme(next);
  return next;
}
