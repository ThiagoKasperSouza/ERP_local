package com.tks.erplocal;

import com.tks.erplocal.domain.vault.model.FileStatus;
import com.tks.erplocal.domain.vault.model.VaultFile;
import com.tks.erplocal.domain.vault.ports.VaultFileRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class VaultTests {

    @Autowired
    WebApplicationContext context;

    @Autowired
    VaultFileRepositoryPort vaultFiles;

    MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private String tokenOf(String bodyJson) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(bodyJson))
                .andExpect(status().isOk())
                .andReturn();
        return com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }

    private String adminAuth() throws Exception {
        return "Bearer " + tokenOf("{\"email\":\"admin@local\",\"password\":\"changeme\"}");
    }

    private String userAuth() throws Exception {
        String email = "vault-" + UUID.randomUUID() + "@local";
        MvcResult registered = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Vault\",\"email\":\"" + email + "\",\"password\":\"pw123456\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        String id = com.jayway.jsonpath.JsonPath.read(
                registered.getResponse().getContentAsString(), "$.user.id");
        String token = tokenOf("{\"email\":\"" + email + "\",\"password\":\"pw123456\"}");
        lastUserId = UUID.fromString(id);
        return "Bearer " + token;
    }

    private UUID lastUserId;

    private void grant(String adminAuth, UUID userId, String codesJson) throws Exception {
        mvc.perform(put("/api/users/" + userId + "/permissions").header("Authorization", adminAuth)
                        .contentType(MediaType.APPLICATION_JSON).content(codesJson))
                .andExpect(status().isOk());
    }

    private String upload(String auth, String filename, byte[] bytes) throws Exception {
        return uploadInProject(auth, filename, bytes, null);
    }

    private String uploadInProject(String auth, String filename, byte[] bytes, String projectCode)
            throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", filename, "application/pdf", bytes);
        var request = multipart("/api/vault/files").file(file).header("Authorization", auth);
        if (projectCode != null) {
            request.param("projectCode", projectCode);
        }
        MvcResult result = mvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.versionNumber").value(1))
                .andExpect(jsonPath("$.status").value("PROTOTYPE"))
                .andReturn();
        return com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    }

    @Test
    void noTokenReturns401() throws Exception {
        mvc.perform(get("/api/vault/files")).andExpect(status().isUnauthorized());
    }

    @Test
    void userWithoutGrantIsForbidden() throws Exception {
        String auth = userAuth();
        mvc.perform(get("/api/vault/files").header("Authorization", auth))
                .andExpect(status().isForbidden());
        MockMultipartFile file = new MockMultipartFile("file", "a.pdf", "application/pdf", "x".getBytes());
        mvc.perform(multipart("/api/vault/files").file(file).header("Authorization", auth))
                .andExpect(status().isForbidden());
    }

    @Test
    void userWithGrantCanUploadListDownload() throws Exception {
        String admin = adminAuth();
        String auth = userAuth();
        grant(admin, lastUserId, "[\"CanUseVault\"]");

        byte[] bytes = "conteudo-teste".getBytes(StandardCharsets.UTF_8);
        String id = upload(auth, "desenho.pdf", bytes);

        mvc.perform(get("/api/vault/files").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.id == '" + id + "')]").exists());

        mvc.perform(get("/api/vault/files/" + id).header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kind").value("PDF"));

        mvc.perform(get("/api/vault/files/" + id + "/download").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(content().bytes(bytes));
    }

    @Test
    void disallowedExtensionReturns400() throws Exception {
        String admin = adminAuth();
        String auth = userAuth();
        grant(admin, lastUserId, "[\"CanUseVault\"]");
        MockMultipartFile file = new MockMultipartFile("file", "virus.exe", "application/octet-stream", "x".getBytes());
        mvc.perform(multipart("/api/vault/files").file(file).header("Authorization", auth))
                .andExpect(status().isBadRequest());
    }

    @Test
    void tboHiddenFromCommonButVisibleToAdmin() throws Exception {
        String admin = adminAuth();
        String auth = userAuth();
        grant(admin, lastUserId, "[\"CanUseVault\"]");

        String id = upload(auth, "peca.step", "step".getBytes(StandardCharsets.UTF_8));
        mvc.perform(put("/api/vault/files/" + id).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"TBO\"}"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/vault/files/" + id).header("Authorization", auth))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/vault/files").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.id == '" + id + "')]").doesNotExist());
        mvc.perform(get("/api/vault/files/" + id).header("Authorization", admin))
                .andExpect(status().isOk());
    }

    @Test
    void pendingVisibleToApproverButNotCommon() throws Exception {
        String admin = adminAuth();
        String common = userAuth();
        UUID commonId = lastUserId;
        grant(admin, commonId, "[\"CanUseVault\"]");
        String approver = userAuth();
        grant(admin, lastUserId, "[\"CanUseVault\",\"CanApprove\"]");

        String id = upload(common, "molde.sldprt", "cad".getBytes(StandardCharsets.UTF_8));
        VaultFile file = vaultFiles.findById(UUID.fromString(id)).orElseThrow();
        file.setPendingApproval(true);
        vaultFiles.save(file);

        mvc.perform(get("/api/vault/files/" + id).header("Authorization", common))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/vault/files/" + id).header("Authorization", approver))
                .andExpect(status().isOk());
        mvc.perform(get("/api/vault/files/" + id).header("Authorization", admin))
                .andExpect(status().isOk());
    }

    @Test
    void deleteRequiresAdmin() throws Exception {
        String admin = adminAuth();
        String auth = userAuth();
        grant(admin, lastUserId, "[\"CanUseVault\"]");

        String id = upload(auth, "rascunho.csv", "a,b".getBytes(StandardCharsets.UTF_8));
        mvc.perform(delete("/api/vault/files/" + id).header("Authorization", auth))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/vault/files/" + id).header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"x\"}"))
                .andExpect(status().isForbidden());

        mvc.perform(delete("/api/vault/files/" + id).header("Authorization", admin))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/vault/files/" + id).header("Authorization", admin))
                .andExpect(status().isNotFound());
    }

    @Test
    void missingFileReturns404() throws Exception {
        mvc.perform(get("/api/vault/files/" + UUID.randomUUID()).header("Authorization", adminAuth()))
                .andExpect(status().isNotFound());
    }

    @Test
    void projectFilterAndGrouping() throws Exception {
        String admin = adminAuth();
        String auth = userAuth();
        grant(admin, lastUserId, "[\"CanUseVault\"]");

        String projA = "PROJ-" + UUID.randomUUID().toString().substring(0, 8);
        String idA1 = uploadInProject(auth, "a1.pdf", "a1".getBytes(StandardCharsets.UTF_8), projA);
        String idA2 = uploadInProject(auth, "a2.pdf", "a2".getBytes(StandardCharsets.UTF_8), projA);
        uploadInProject(auth, "sem-projeto.pdf", "x".getBytes(StandardCharsets.UTF_8), null);

        mvc.perform(get("/api/vault/files?project=" + projA).header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[?(@.id == '" + idA1 + "')]").exists())
                .andExpect(jsonPath("$.items[?(@.id == '" + idA2 + "')]").exists());

        mvc.perform(get("/api/vault/files").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.id == '" + idA1 + "')]").exists());

        mvc.perform(get("/api/vault/files/projects").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code == '" + projA + "')].fileCount", hasItem(2)));
    }

    @Test
    void updateProjectCode() throws Exception {
        String admin = adminAuth();
        String auth = userAuth();
        grant(admin, lastUserId, "[\"CanUseVault\"]");

        String id = upload(auth, "mover.pdf", "m".getBytes(StandardCharsets.UTF_8));
        mvc.perform(put("/api/vault/files/" + id).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"projectCode\":\"MOTOR-X\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectCode").value("MOTOR-X"));

        mvc.perform(get("/api/vault/files/" + id).header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectCode").value("MOTOR-X"));

        mvc.perform(put("/api/vault/files/" + id).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"projectCode\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectCode").isEmpty());
    }
}
