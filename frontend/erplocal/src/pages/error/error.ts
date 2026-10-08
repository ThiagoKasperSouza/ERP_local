import "./style.css";
import htmlContent from "./index.html?raw";

export function getErrorPage(): string {
  return htmlContent;
}
