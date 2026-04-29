package com.alibaba.fastjson3.spring;

import com.alibaba.fastjson3.JSONArray;
import com.alibaba.fastjson3.JSONObject;
import com.alibaba.fastjson3.annotation.JSONField;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.Map;

/**
 * Spring Boot test application that wires {@link Fastjson3HttpMessageConverter}
 * as the sole JSON converter (Jackson auto-configuration excluded). Exercises
 * a small REST surface that touches every reader / writer path we want to
 * exercise end-to-end.
 */
@SpringBootApplication(exclude = {
        JacksonAutoConfiguration.class,
        HttpMessageConvertersAutoConfiguration.class
})
public class SpringIntegrationApp {
    public static void main(String[] args) {
        SpringApplication.run(SpringIntegrationApp.class, args);
    }

    @Configuration
    public static class Cfg implements WebMvcConfigurer {
        @Bean
        public Fastjson3HttpMessageConverter fastjson3HttpMessageConverter() {
            return new Fastjson3HttpMessageConverter();
        }

        @Override
        public void configureMessageConverters(
                java.util.List<org.springframework.http.converter.HttpMessageConverter<?>> converters) {
            // Strip Jackson / String defaults wired by HttpMessageConvertersAutoConfiguration's
            // fallback chain so we exercise fastjson3 exclusively.
            converters.clear();
            converters.add(new org.springframework.http.converter.ByteArrayHttpMessageConverter());
            converters.add(new org.springframework.http.converter.StringHttpMessageConverter(java.nio.charset.StandardCharsets.UTF_8));
            converters.add(fastjson3HttpMessageConverter());
        }
    }

    public enum Role { USER, ADMIN, GUEST }

    public static class User {
        public Long id;
        public String name;
        public int age;
        @JSONField(name = "email_addr")
        public String email;
        public Role role;
        public java.time.LocalDateTime createdAt;
        public java.math.BigDecimal balance;
        public byte[] avatar;

        public User() {}

        public User(Long id, String name, int age, String email) {
            this.id = id;
            this.name = name;
            this.age = age;
            this.email = email;
        }
    }

    public static class ErrorResponse {
        public String code;
        public String message;

        public ErrorResponse() {}

        public ErrorResponse(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }

    public static class Holder {
        public List<User> users;
        public Map<String, Integer> counts;
        public Object payload;
        public JSONObject meta;
        public JSONArray tags;
    }

    public record Box<T>(T value, int version) {}

    @RestController
    @RequestMapping("/api")
    public static class Api {
        @PostMapping("/echo-user")
        public User echoUser(@RequestBody User u) {
            return u;
        }

        @PostMapping("/echo-list")
        public List<User> echoList(@RequestBody List<User> us) {
            return us;
        }

        @PostMapping("/echo-map")
        public Map<String, Integer> echoMap(@RequestBody Map<String, Integer> m) {
            return m;
        }

        @PostMapping("/echo-holder")
        public Holder echoHolder(@RequestBody Holder h) {
            return h;
        }

        @PostMapping("/echo-jsonobject")
        public JSONObject echoJsonObject(@RequestBody JSONObject obj) {
            return obj;
        }

        @PostMapping("/echo-jsonarray")
        public JSONArray echoJsonArray(@RequestBody JSONArray arr) {
            return arr;
        }

        @PostMapping("/echo-object")
        public Object echoObject(@RequestBody Object payload) {
            return payload;
        }

        @PostMapping("/echo-record")
        public Box<User> echoRecord(@RequestBody Box<User> box) {
            return box;
        }

        @GetMapping("/user/{id}")
        public ResponseEntity<User> getUser(@PathVariable("id") Long id) {
            if (id < 0) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(new User(id, "user-" + id, 30, "u" + id + "@e.com"));
        }

        @PostMapping("/throw")
        public User triggerError(@RequestBody User u) {
            throw new IllegalArgumentException("rejected: " + u.name);
        }

        @PostMapping("/header-trace")
        public Map<String, String> headerTrace(@RequestHeader("X-Trace-Id") String traceId,
                                                @RequestBody User u) {
            return Map.of("trace", traceId, "name", u.name);
        }

        @GetMapping(value = "/plain", produces = "text/plain")
        public String plainText() {
            // Returning a String. fastjson3 converter excludes String, so
            // StringHttpMessageConverter handles this — body should be raw,
            // NOT JSON-quoted.
            return "hello world";
        }

        @GetMapping(value = "/bytes", produces = "application/octet-stream")
        public byte[] rawBytes() {
            return new byte[]{1, 2, 3, 4, 5};
        }
    }

    /**
     * Exception advice — returns a custom POJO (ErrorResponse) so the
     * fastjson3 message converter must be invoked at error time too. If
     * the converter is absent / broken at the error path, the body would
     * come back empty — a silent prod-only failure mode.
     */
    @RestControllerAdvice
    public static class GlobalErrorAdvice {
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> onArg(IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("E_ARG", e.getMessage()));
        }
    }
}
