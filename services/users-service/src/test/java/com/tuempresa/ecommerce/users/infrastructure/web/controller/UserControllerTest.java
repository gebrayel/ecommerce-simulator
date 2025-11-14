package com.tuempresa.ecommerce.users.infrastructure.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuempresa.ecommerce.users.domain.model.User;
import com.tuempresa.ecommerce.users.domain.port.in.UserUseCase;
import com.tuempresa.ecommerce.users.infrastructure.web.dto.UserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@DisplayName("UserController Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserUseCase userUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private UserRequest userRequest;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, "test@example.com", "Test User", "+1234567890", 
                           "Test Address", "hashedPassword", LocalDateTime.now());
        userRequest = new UserRequest("Test User", "test@example.com", "+1234567890", 
                                      "Test Address", "password123");
    }

    @Test
    @DisplayName("Should return all users with status 200")
    void shouldReturnAllUsers() throws Exception {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(userUseCase.findAll()).thenReturn(users);

        // When/Then
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].email").value("test@example.com"))
                .andExpect(jsonPath("$[0].name").value("Test User"));

        verify(userUseCase, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return user by id with status 200")
    void shouldReturnUserById() throws Exception {
        // Given
        when(userUseCase.findById(1L)).thenReturn(Optional.of(testUser));

        // When/Then
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"));

        verify(userUseCase, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return 404 when user not found")
    void shouldReturn404WhenUserNotFound() throws Exception {
        // Given
        when(userUseCase.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound());

        verify(userUseCase, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should create user with status 201")
    void shouldCreateUser() throws Exception {
        // Given
        when(userUseCase.create(any(User.class))).thenReturn(testUser);

        // When/Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"));

        verify(userUseCase, times(1)).create(any(User.class));
    }

    @Test
    @DisplayName("Should return 400 when request is invalid")
    void shouldReturn400WhenRequestIsInvalid() throws Exception {
        // Given
        UserRequest invalidRequest = new UserRequest("", "", "", "", "");

        // When/Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userUseCase, never()).create(any(User.class));
    }

    @Test
    @DisplayName("Should update user with status 200")
    void shouldUpdateUser() throws Exception {
        // Given
        when(userUseCase.update(anyLong(), any(User.class))).thenReturn(testUser);

        // When/Then
        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userUseCase, times(1)).update(eq(1L), any(User.class));
    }

    @Test
    @DisplayName("Should delete user with status 204")
    void shouldDeleteUser() throws Exception {
        // Given
        doNothing().when(userUseCase).delete(1L);

        // When/Then
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());

        verify(userUseCase, times(1)).delete(1L);
    }
}

