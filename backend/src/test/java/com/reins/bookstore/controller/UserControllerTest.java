package com.reins.bookstore.controller;

import com.reins.bookstore.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    public void testLoginSuccess() throws Exception {
        when(userService.login("testuser", "password")).thenReturn(Map.of(
                "userId", 1,
                "username", "testuser",
                "nickname", "Tester",
                "identity", 0
        ));

        String json = "{\"username\": \"testuser\", \"password\": \"password\"}";

        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    public void testLoginFailure() throws Exception {
        when(userService.login("testuser", "wrong")).thenReturn(Map.of("error", "Invalid username or password"));

        String json = "{\"username\": \"testuser\", \"password\": \"wrong\"}";

        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    public void testRegisterSuccess() throws Exception {
        when(userService.register(anyString(), anyString(), nullable(String.class)))
                .thenReturn(Map.of("message", "User registered successfully"));

        String json = "{\"username\": \"newuser\", \"password\": \"pass\", \"nickname\": \"Newbie\"}";

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully"));
    }
}
