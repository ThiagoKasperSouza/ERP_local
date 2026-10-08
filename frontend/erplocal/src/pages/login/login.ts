import "./style.css";
import htmlContent from "./index.html?raw";

export function getLoginPage(): string {
  return htmlContent;
}
