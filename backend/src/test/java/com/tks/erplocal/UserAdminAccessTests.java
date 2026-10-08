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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class UserAdminAccessTests {

    @Autowired
    WebApplicationContext context;

    MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@local";
    }

    @Test
    void nonAdminCannotListUsers() throws Exception {
        mvc.perform(get("/api/users")).andExpect(status().isForbidden());
        mvc.perform(get("/api/users").header("X-User-Role", "user")).andExpect(status().isForbidden());
    }

    @Test
    void nonAdminCannotCreateUser() throws Exception {
        String body = "{\"name\":\"Bob\",\"email\":\"" + uniqueEmail() + "\",\"password\":\"secret123\"}";
        mvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/users").header("X-User-Role", "user")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void nonAdminCannotUpdateOrDeactivate() throws Exception {
        UUID randomId = UUID.randomUUID();
        mvc.perform(put("/api/users/" + randomId).header("X-User-Role", "user")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"X\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/users/" + randomId).header("X-User-Role", "user"))
                .andExpect(status().isForbidden());
    }

    @Test
    void seedCreatesAdminAndAdminCanCrud() throws Exception {
        mvc.perform(get("/api/users").header("X-User-Role", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.email == 'admin@local')]").exists())
                .andExpect(content().string(not(containsString("passwordHash"))));

        String email = uniqueEmail();
        MvcResult created = mvc.perform(post("/api/users").header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ana\",\"email\":\"" + email + "\",\"password\":\"s3cr3t!\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(content().string(not(containsString("s3cr3t"))))
                .andExpect(content().string(not(containsString("password"))))
                .andReturn();
        String id = com.jayway.jsonpath.JsonPath.read(created.getResponse().getContentAsString(), "$.id");

        mvc.perform(put("/api/users/" + id).header("X-User-Role", "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ana Silva\",\"role\":\"admin\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana Silva"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        mvc.perform(put("/api/users/" + id + "/permissions").header("X-User-Role", "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"CanManagePcp\",\"CanManageOp\"]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissions.length()").value(2));

        mvc.perform(put("/api/users/" + id + "/permissions").header("X-User-Role", "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"Nope\"]"))
                .andExpect(status().isBadRequest());

        mvc.perform(delete("/api/users/" + id).header("X-User-Role", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void duplicateEmailReturns409() throws Exception {
        String email = uniqueEmail();
        String body = "{\"name\":\"Du\",\"email\":\"" + email + "\",\"password\":\"pw123456\"}";
        mvc.perform(post("/api/users").header("X-User-Role", "admin")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/users").header("X-User-Role", "admin")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }
}
