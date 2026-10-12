import './styles.css' // css geral
import { initTheme } from './services/theme.ts'
import { isAuthenticated } from './services/session.ts'
import { getHomePage } from './pages/home/home.ts'
import { getLoginPage } from './pages/login/login.ts'
import { getRegisterPage } from './pages/register/register.ts'
import { getErrorPage } from './pages/error/error.ts'

// 1. Define que cada rota deve ser uma função que retorna uma string HTML
type PageRenderFn = () => string;

// 2. Mapeamento das rotas (HashMap)
const routes: Record<string, PageRenderFn> = {
  '/home': getHomePage,
  '/login': getLoginPage,
  '/register': getRegisterPage,
};

// 3. Renderizador da página 404
const getNotFoundPage: PageRenderFn = () => getErrorPage();

function guard(path: string): string {
  if (path === '/') return isAuthenticated() ? '/home' : '/login';
  if ((path === '/home') && !isAuthenticated()) return '/login';
  if ((path === '/login' || path === '/register') && isAuthenticated()) return '/home';
  return path;
}

const target = guard(window.location.pathname);
if (target !== window.location.pathname) {
  window.location.href = target;
} else {
  initTheme();

  document.querySelector<HTMLDivElement>('#app')!.innerHTML =
    (routes[target] ?? getNotFoundPage)();
}
