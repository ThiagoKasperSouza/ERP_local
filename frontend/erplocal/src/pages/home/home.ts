import "./style.css";
import htmlContent from "./index.html?raw";

export function getHomePage(): string {
  return htmlContent;
}
