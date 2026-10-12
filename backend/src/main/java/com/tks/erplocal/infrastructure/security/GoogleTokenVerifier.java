package com.tks.erplocal.infrastructure.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tks.erplocal.domain.users.exceptions.ServerMisconfiguredException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class GoogleTokenVerifier {
    private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";

    private final GoogleIdTokenVerifier verifier;
    private final String clientId;
    private final String clientSecret;

    public GoogleTokenVerifier(@Value("${app.google.client-id:}") String clientIds,
                               @Value("${app.google.client-secret:}") String clientSecret) {
        var audience = Arrays.stream(clientIds.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(audience)
                .build();
        this.clientId = audience.stream().findFirst().orElse("");
        this.clientSecret = clientSecret;
    }

    public record GoogleAccount(String email, String name, boolean emailVerified) {}

    /** Verifica assinatura + audience (seu client_id) e retorna os dados da conta. */
    public GoogleAccount verify(String idToken) {
        try {
            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                throw new IllegalArgumentException("Invalid Google ID token");
            }
            GoogleIdToken.Payload payload = token.getPayload();
            String email = payload.getEmail();
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("Google account has no email");
            }
            Object name = payload.get("name");
            return new GoogleAccount(email, name instanceof String s ? s : email,
                    Boolean.TRUE.equals(payload.getEmailVerified()));
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not verify Google ID token", e);
        }
    }

    /**
     * Troca o authorization code (fluxo PKCE do app Tauri) pelo idToken.
     * Usa client_secret do servidor — nunca exponha esse segredo no front.
     */
    public String exchangeCodeForIdToken(String code, String codeVerifier, String redirectUri) {
        if (clientSecret.isBlank()) {
            throw new ServerMisconfiguredException("GOOGLE_CLIENT_SECRET nao configurado no servidor");
        }
        try {
            String body = "grant_type=authorization_code"
                    + "&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                    + "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                    + "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8)
                    + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                    + "&code_verifier=" + URLEncoder.encode(codeVerifier, StandardCharsets.UTF_8);
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            HttpURLConnection conn = (HttpURLConnection) new URL(TOKEN_ENDPOINT).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(10_000);
            try (OutputStream out = conn.getOutputStream()) {
                out.write(bytes);
            }
            String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
            if (!json.has("id_token")) {
                throw new IllegalArgumentException("Google recusou o codigo de autorizacao");
            }
            return json.get("id_token").getAsString();
        } catch (IllegalArgumentException | ServerMisconfiguredException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Falha na troca do codigo com o Google", e);
        }
    }
}
