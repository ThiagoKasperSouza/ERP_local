import './styles.css' // css geral
import { initTheme } from './services/theme.ts'
import { getHomePage } from './pages/home/home.ts'
import { getLoginPage } from './pages/login/login.ts'
import { getErrorPage } from './pages/error/error.ts'
//import { getRegisterPage } from './pages/register/register.ts'

// 1. Define que cada rota deve ser uma função que retorna uma string HTML
type PageRenderFn = () => string;

// 2. Mapeamento das rotas (HashMap)
const routes: Record<string, PageRenderFn> = {
  '/home': getHomePage,
  '/login': getLoginPage,
  '/register': getHomePage,
};

// 3. Renderizador da página 404
const getNotFoundPage: PageRenderFn = () => getErrorPage();

function renderPage(): string {
  const path: string = window.location.pathname;

  // Busca a função de renderização no HashMap; se não existir, usa a de 404
  const renderFn: PageRenderFn = routes[path] ?? getNotFoundPage;

  return renderFn();
}

initTheme();

document.querySelector<HTMLDivElement>('#app')!.innerHTML = renderPage();


if(window.location.pathname === '/') {
  window.location.href = '/login';
}
