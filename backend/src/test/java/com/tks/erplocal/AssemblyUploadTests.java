package com.tks.erplocal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AssemblyUploadTests {

    @Autowired
    WebApplicationContext context;

    MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void uploadAssemblyWithEbomReturnsCreated() throws Exception {
        MockMultipartFile assembly = new MockMultipartFile("assembly", "test.sldasm", "application/octet-stream", "data".getBytes());
        MockMultipartFile ebom = new MockMultipartFile("ebom", "ebom.csv", "text/csv", "partNumber,fileName\nP001,test.sldprt".getBytes(StandardCharsets.UTF_8));
        mvc.perform(multipart("/api/vault/files/assembly")
                .file(assembly)
                .file(ebom)
                .header("Authorization", "Bearer invalid"))
                .andExpect(status().isCreated());
    }

    @Test
    void uploadAssemblyWithoutEbomReturns400() throws Exception {
        MockMultipartFile assembly = new MockMultipartFile("assembly", "test.sldasm", "application/octet-stream", "data".getBytes());
        mvc.perform(multipart("/api/vault/files/assembly")
                .file(assembly)
                .header("Authorization", "Bearer invalid"))
                .andExpect(status().isBadRequest());
    }
}
