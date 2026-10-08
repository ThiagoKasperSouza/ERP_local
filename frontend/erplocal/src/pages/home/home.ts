import "./style.css";
import htmlContent from "./index.html?raw";
import { renderWelcome } from "./components/welcome.ts";
import { renderPermissions } from "./components/permissions.ts";
import { getTheme, toggleTheme } from "../../services/theme.ts";

export function getHomePage(): string {
  document.addEventListener("DOMContentLoaded", () => {
    initHomePage();
  });
  return htmlContent;
}

type SectionId = "inicio" | "permissoes";
type SectionRender = (main: HTMLElement) => void | Promise<void>;

// Para adicionar nova seção: crie o componente em ./components,
// registre aqui e adicione o <button data-section="..."> no index.html.
const sections: Record<SectionId, SectionRender> = {
  inicio: renderWelcome,
  permissoes: renderPermissions,
};

export function initHomePage(): void {
  const main = document.querySelector<HTMLElement>("#home-main");
  const buttons = document.querySelectorAll<HTMLButtonElement>(".sidebar-btn");
  if (!main || buttons.length === 0) return;

  const select = (id: SectionId): void => {
    buttons.forEach((b) =>
      b.classList.toggle("active", b.dataset.section === id),
    );
    void sections[id](main);
  };

  buttons.forEach((btn) => {
    btn.addEventListener("click", () => {
      const id = btn.dataset.section as SectionId;
      if (id in sections) select(id);
    });
  });

  select("inicio");

  const themeBtn = document.querySelector<HTMLButtonElement>("#theme-toggle");
  const syncLabel = (): void => {
    if (themeBtn) {
      themeBtn.textContent = getTheme() === "dark" ? "☀️ Tema claro" : "🌙 Tema escuro";
    }
  };
  syncLabel();
  themeBtn?.addEventListener("click", () => {
    toggleTheme();
    syncLabel();
  });
}
