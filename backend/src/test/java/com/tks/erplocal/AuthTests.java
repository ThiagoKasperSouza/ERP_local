package com.tks.erplocal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AuthTests {

    @Autowired
    WebApplicationContext context;

    MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private String uniqueEmail() {
        return "auth-" + UUID.randomUUID() + "@local";
    }

    private String register(String email, String password) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Auth\",\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.user.email").value(email))
                .andExpect(jsonPath("$.user.role").value("USER"))
                .andExpect(content().string(not(containsString(password))))
                .andExpect(content().string(not(containsString("passwordHash"))))
                .andReturn();
        return com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }

    @Test
    void registerThenLoginWithJwt() throws Exception {
        String email = uniqueEmail();
        register(email, "pw123456");

        MvcResult login = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"pw123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andReturn();
        String token = com.jayway.jsonpath.JsonPath.read(login.getResponse().getContentAsString(), "$.token");

        // JWT de user comum nao abre /api/users
        mvc.perform(get("/api/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginWrongPasswordReturns401() throws Exception {
        String email = uniqueEmail();
        register(email, "pw123456");
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"errada\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void duplicateRegisterReturns409() throws Exception {
        String email = uniqueEmail();
        register(email, "pw123456");
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Auth\",\"email\":\"" + email + "\",\"password\":\"pw123456\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void googleWithFakeTokenReturns400() throws Exception {
        mvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\":\"fake\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void googleCodeWithFakeCodeReturns400() throws Exception {
        mvc.perform(post("/api/auth/google/code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"fake\",\"codeVerifier\":\"fake\",\"redirectUri\":\"http://127.0.0.1:9/callback\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminJwtOpensUsers() throws Exception {
        MvcResult login = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@local\",\"password\":\"changeme\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String token = com.jayway.jsonpath.JsonPath.read(login.getResponse().getContentAsString(), "$.token");
        mvc.perform(get("/api/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.email == 'admin@local')]").exists());
    }
}
