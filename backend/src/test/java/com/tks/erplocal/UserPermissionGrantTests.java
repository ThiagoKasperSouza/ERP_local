package com.tks.erplocal;

import com.tks.erplocal.infrastructure.adapters.users.UserPermissionJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class UserPermissionGrantTests {

    @Autowired
    WebApplicationContext context;

    MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Autowired
    UserPermissionJpaRepository grants;

    @Test
    void grantPersistsAuditAndInverseFinderWorks() throws Exception {
        String email = "grant-" + UUID.randomUUID() + "@local";
        MvcResult created = mvc.perform(post("/api/users").header("X-User-Role", "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"G\",\"email\":\"" + email + "\",\"password\":\"pw123456\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        String id = com.jayway.jsonpath.JsonPath.read(created.getResponse().getContentAsString(), "$.id");

        mvc.perform(put("/api/users/" + id + "/permissions").header("X-User-Role", "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"CanManagePcp\"]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissions[0]").value("CanManagePcp"));

        List<UUID> holders = grants.findUserIdsByPermissionCode("CanManagePcp");
        assertThat(holders).contains(UUID.fromString(id));
        assertThat(grants.findByUserId(UUID.fromString(id)))
                .hasSize(1)
                .allSatisfy(g -> {
                    assertThat(g.getGrantedAt()).isNotNull();
                    assertThat(g.getPermissionCode()).isEqualTo("CanManagePcp");
                });
        assertThat(grants.findUserIdsByPermissionCode("CanManageOp"))
                .doesNotContain(UUID.fromString(id));
    }
}
