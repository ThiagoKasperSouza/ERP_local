// Learn more about Tauri commands at https://tauri.app/develop/calling-rust/
#[tauri::command]
fn greet(name: &str) -> String {
    format!("Hello, {}! You've been greeted from Rust!", name)
}

use std::io::{Read, Write};
use std::net::TcpListener;
use std::sync::Mutex;
use std::time::{Duration, Instant};

/// Guarda o listener loopback entre os comandos oauth_bind e oauth_wait_code.
struct OAuthListener(Mutex<Option<TcpListener>>);

/// Portas fixas para o redirect do Google (registre todas no Console — porta
/// efêmera não dá para registrar e o Google exige match exato do redirect_uri).
const OAUTH_PORTS: [u16; 3] = [41007, 41017, 41027];

/// Abre 127.0.0.1 na primeira porta livre da lista e devolve a porta.
/// Só loopback — nada é exposto na rede.
#[tauri::command]
fn oauth_bind(state: tauri::State<OAuthListener>) -> Result<u16, String> {
    for port in OAUTH_PORTS {
        if let Ok(listener) = TcpListener::bind(("127.0.0.1", port)) {
            *state.0.lock().map_err(|e| e.to_string())? = Some(listener);
            return Ok(port);
        }
    }
    Err("Nenhuma porta livre para o login (41007/41017/41027)".to_string())
}

fn percent_decode(input: &str) -> String {
    let mut out = Vec::with_capacity(input.len());
    let bytes = input.as_bytes();
    let mut i = 0;
    while i < bytes.len() {
        if bytes[i] == b'%' && i + 2 < bytes.len() {
            if let Ok(hex) = u8::from_str_radix(&input[i + 1..i + 3], 16) {
                out.push(hex);
                i += 3;
                continue;
            }
        }
        if bytes[i] == b'+' {
            out.push(b' ');
        } else {
            out.push(bytes[i]);
        }
        i += 1;
    }
    String::from_utf8_lossy(&out).into_owned()
}

fn wait_code_blocking(listener: TcpListener) -> Result<serde_json::Value, String> {
    listener
        .set_nonblocking(true)
        .map_err(|e| e.to_string())?;
    let deadline = Instant::now() + Duration::from_secs(180);
    loop {
        match listener.accept() {
            Ok((mut stream, _)) => {
                stream
                    .set_read_timeout(Some(Duration::from_secs(10)))
                    .map_err(|e| e.to_string())?;
                let mut raw = Vec::new();
                let mut chunk = [0u8; 4096];
                loop {
                    match stream.read(&mut chunk) {
                        Ok(0) => break,
                        Ok(n) => {
                            raw.extend_from_slice(&chunk[..n]);
                            if raw.len() > 8192 {
                                break;
                            }
                            if raw.windows(4).any(|w| w == b"\r\n\r\n") {
                                break;
                            }
                        }
                        Err(_) => break,
                    }
                }
                let request = String::from_utf8_lossy(&raw);
                let path = request
                    .lines()
                    .next()
                    .unwrap_or("")
                    .split_whitespace()
                    .nth(1)
                    .unwrap_or("");
                let query = path.split_once('?').map(|(_, q)| q).unwrap_or("");
                let mut code: Option<String> = None;
                let mut state: Option<String> = None;
                let mut denied: Option<String> = None;
                for pair in query.split('&') {
                    let (k, v) = pair.split_once('=').unwrap_or((pair, ""));
                    match k {
                        "code" => code = Some(percent_decode(v)),
                        "state" => state = Some(percent_decode(v)),
                        "error" => denied = Some(percent_decode(v)),
                        _ => {}
                    }
                }
                let page = "<html><body style='font-family:sans-serif'><h3>Login conclu&iacute;do</h3><p>Pode fechar esta aba e voltar ao app.</p></body></html>";
                let response = format!(
                    "HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=utf-8\r\nContent-Length: {}\r\nConnection: close\r\n\r\n{}",
                    page.len(),
                    page
                );
                let _ = stream.write_all(response.as_bytes());
                if let Some(err) = denied {
                    return Err(format!("Google negou o acesso: {err}"));
                }
                match (code, state) {
                    (Some(c), Some(s)) => {
                        return Ok(serde_json::json!({ "code": c, "state": s }));
                    }
                    _ => return Err("Resposta do Google sem codigo".to_string()),
                }
            }
            Err(e) if e.kind() == std::io::ErrorKind::WouldBlock => {
                if Instant::now() > deadline {
                    return Err("Tempo esgotado aguardando o login no navegador".to_string());
                }
                std::thread::sleep(Duration::from_millis(50));
            }
            Err(e) => return Err(e.to_string()),
        }
    }
}

/// Aguarda o redirect do Google no loopback e devolve { code, state }.
/// Chamar depois de oauth_bind + abrir o navegador; expira em ~3 min.
#[tauri::command]
async fn oauth_wait_code(
    state: tauri::State<'_, OAuthListener>,
) -> Result<serde_json::Value, String> {
    let listener = state
        .0
        .lock()
        .map_err(|e| e.to_string())?
        .take()
        .ok_or_else(|| "oauth_bind precisa rodar antes".to_string())?;
    tauri::async_runtime::spawn_blocking(move || wait_code_blocking(listener))
        .await
        .map_err(|e| e.to_string())?
}

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        .plugin(tauri_plugin_opener::init())
        .manage(OAuthListener(Mutex::new(None)))
        .invoke_handler(tauri::generate_handler![greet, oauth_bind, oauth_wait_code])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
