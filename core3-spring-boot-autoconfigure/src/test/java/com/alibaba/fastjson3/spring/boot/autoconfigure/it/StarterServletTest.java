package com.alibaba.fastjson3.spring.boot.autoconfigure.it;

import com.alibaba.fastjson3.annotation.JSONField;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end smoke test that the Spring Boot auto-discovery (META-INF
 * AutoConfiguration.imports) is correctly picked up in a real Boot
 * servlet application — *no* manual converter registration, *no*
 * Jackson exclusion.
 *
 * <p>Differentiator: the response payload's {@code email} field carries
 * {@link JSONField} {@code name = "email_addr"}. fastjson3 honors that
 * alias on write, Jackson does not. So if the response JSON has key
 * {@code email_addr}, fastjson3 handled the write; if it has {@code email},
 * Jackson did and our auto-config didn't win the chain.</p>
 */
@SpringBootTest(classes = StarterServletTest.App.class)
@AutoConfigureMockMvc
class StarterServletTest {
    @Autowired
    MockMvc mockMvc;

    @Test
    void responseGoesThroughFastjson3() throws Exception {
        mockMvc.perform(post("/echo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"name\":\"alice\",\"email_addr\":\"a@e.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email_addr").value("a@e.com"))
                .andExpect(jsonPath("$.email").doesNotExist());
    }

    @Test
    void requestParsedThroughFastjson3() throws Exception {
        // Symmetric: read side honors @JSONField too. If Jackson handled the
        // request body, "email_addr" would not bind to the email field.
        mockMvc.perform(post("/echo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":2,\"name\":\"bob\",\"email_addr\":\"b@e.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email_addr").value("b@e.com"));
    }

    @SpringBootApplication
    static class App {
        @RestController
        static class Api {
            @PostMapping("/echo")
            public User echo(@RequestBody User u) {
                return u;
            }
        }
    }

    public static class User {
        public Long id;
        public String name;
        @JSONField(name = "email_addr")
        public String email;

        public User() {
        }
    }
}
