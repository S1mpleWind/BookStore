package com.reins.bookstore.service;

import com.reins.bookstore.dto.response.UserLoginResponse;
import com.reins.bookstore.entity.User;
import com.reins.bookstore.entity.UserAuth;
import com.reins.bookstore.repository.UserAuthRepository;
import com.reins.bookstore.repository.UserRepository;
import com.reins.bookstore.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAuthRepository userAuthRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserAuth testUserAuth;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setNickname("TestUser");
        testUser.setBalance(0L);
        testUser.setEmail("test@example.com");

        testUserAuth = new UserAuth();
        testUserAuth.setId(1L);
        testUserAuth.setUsername("testuser");
        testUserAuth.setPassword("password123");
        testUserAuth.setUserId(1L);
        testUserAuth.setIdentity(0);
        testUserAuth.setEnable(true);
    }

    // ========== REGISTER TESTS ==========

    @Test
    void register_shouldSucceedWithValidInput() {
        when(userAuthRepository.findByUsername("newuser")).thenReturn(null);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userAuthRepository.save(any(UserAuth.class))).thenReturn(testUserAuth);

        Map<String, Object> result = userService.register("newuser", "pass123", "pass123", "NewUser", "new@example.com");

        assertNotNull(result);
        assertEquals("注册成功", result.get("message"));
        assertNull(result.get("error"));
        verify(userRepository, times(1)).save(any(User.class));
        verify(userAuthRepository, times(1)).save(any(UserAuth.class));
    }

    @Test
    void register_shouldUseUsernameAsNicknameWhenNicknameIsNull() {
        when(userAuthRepository.findByUsername("newuser")).thenReturn(null);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userAuthRepository.save(any(UserAuth.class))).thenReturn(testUserAuth);

        Map<String, Object> result = userService.register("newuser", "pass123", "pass123", null, "new@example.com");

        assertEquals("注册成功", result.get("message"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldFailWhenUsernameIsNull() {
        Map<String, Object> result = userService.register(null, "pass123", "pass123", "Nick", "a@b.com");

        assertEquals("用户名不能为空", result.get("error"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldFailWhenUsernameIsEmpty() {
        Map<String, Object> result = userService.register("   ", "pass123", "pass123", "Nick", "a@b.com");

        assertEquals("用户名不能为空", result.get("error"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldFailWhenPasswordIsNull() {
        Map<String, Object> result = userService.register("newuser", null, "confirm", "Nick", "a@b.com");

        assertEquals("密码不能为空", result.get("error"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldFailWhenPasswordIsEmpty() {
        Map<String, Object> result = userService.register("newuser", "", "confirm", "Nick", "a@b.com");

        assertEquals("密码不能为空", result.get("error"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldFailWhenPasswordsDoNotMatch() {
        Map<String, Object> result = userService.register("newuser", "pass123", "different", "Nick", "a@b.com");

        assertEquals("两次输入的密码不一致", result.get("error"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldFailWhenEmailIsInvalid() {
        Map<String, Object> result = userService.register("newuser", "pass123", "pass123", "Nick", "not-an-email");

        assertEquals("邮箱格式不正确", result.get("error"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldFailWhenEmailIsEmpty() {
        Map<String, Object> result = userService.register("newuser", "pass123", "pass123", "Nick", "");

        assertEquals("邮箱不能为空", result.get("error"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldFailWhenEmailIsNull() {
        Map<String, Object> result = userService.register("newuser", "pass123", "pass123", "Nick", null);

        assertEquals("邮箱不能为空", result.get("error"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldFailWhenUsernameAlreadyExists() {
        when(userAuthRepository.findByUsername("existinguser")).thenReturn(testUserAuth);

        Map<String, Object> result = userService.register("existinguser", "pass123", "pass123", "Nick", "a@b.com");

        assertEquals("用户名已存在", result.get("error"));
        verify(userRepository, never()).save(any());
    }

    // ========== LOGIN TESTS ==========

    @Test
    void login_shouldSucceedWithValidCredentials() {
        when(userAuthRepository.findByUsername("testuser")).thenReturn(testUserAuth);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserLoginResponse result = userService.login("testuser", "password123");

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("testuser", result.getUsername());
        assertEquals("TestUser", result.getNickname());
        assertEquals(0, result.getIdentity());
    }

    @Test
    void login_shouldReturnNullWhenUserNotFound() {
        when(userAuthRepository.findByUsername("unknown")).thenReturn(null);

        UserLoginResponse result = userService.login("unknown", "password");

        assertNull(result);
    }

    @Test
    void login_shouldReturnNullWhenPasswordIsWrong() {
        when(userAuthRepository.findByUsername("testuser")).thenReturn(testUserAuth);

        UserLoginResponse result = userService.login("testuser", "wrongpassword");

        assertNull(result);
    }

    @Test
    void login_shouldReturnIdentityMinusOneWhenDisabled() {
        testUserAuth.setEnable(false);
        when(userAuthRepository.findByUsername("testuser")).thenReturn(testUserAuth);

        UserLoginResponse result = userService.login("testuser", "password123");

        assertNotNull(result);
        assertEquals(-1, result.getIdentity());
        assertEquals("", result.getNickname());
    }

    @Test
    void login_shouldReturnNullWhenUserAuthHasNoCorrespondingUser() {
        when(userAuthRepository.findByUsername("testuser")).thenReturn(testUserAuth);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        UserLoginResponse result = userService.login("testuser", "password123");

        assertNull(result);
    }

    // ========== LIST ALL USERS TESTS ==========

    @Test
    void listAllUsers_shouldReturnAllUsers() {
        UserAuth auth2 = new UserAuth();
        auth2.setId(2L);
        auth2.setUsername("admin");
        auth2.setUserId(2L);
        auth2.setIdentity(1);
        auth2.setEnable(true);

        User user2 = new User();
        user2.setId(2L);
        user2.setNickname("Admin");
        user2.setEmail("admin@example.com");

        when(userAuthRepository.findAll()).thenReturn(Arrays.asList(testUserAuth, auth2));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));

        List<Map<String, Object>> result = userService.listAllUsers();

        assertEquals(2, result.size());
        assertEquals("testuser", result.get(0).get("username"));
        assertEquals("TestUser", result.get(0).get("nickname"));
        assertEquals("admin", result.get(1).get("username"));
        assertEquals("Admin", result.get(1).get("nickname"));
        assertEquals(1, result.get(1).get("identity"));
    }

    @Test
    void listAllUsers_shouldReturnEmptyListWhenNoUsers() {
        when(userAuthRepository.findAll()).thenReturn(Collections.emptyList());

        List<Map<String, Object>> result = userService.listAllUsers();

        assertTrue(result.isEmpty());
    }

    @Test
    void listAllUsers_shouldHandleMissingUserId() {
        when(userAuthRepository.findAll()).thenReturn(Arrays.asList(testUserAuth));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        List<Map<String, Object>> result = userService.listAllUsers();

        assertEquals(1, result.size());
        assertEquals("", result.get(0).get("nickname"));
        assertEquals("", result.get(0).get("email"));
    }

    // ========== TOGGLE USER STATUS TESTS ==========

    @Test
    void toggleUserStatus_shouldDisableEnabledUser() {
        when(userAuthRepository.findByUserId(1L)).thenReturn(testUserAuth);
        when(userAuthRepository.save(any(UserAuth.class))).thenReturn(testUserAuth);

        Map<String, Object> result = userService.toggleUserStatus(1L);

        assertEquals("用户已禁用", result.get("message"));
        assertFalse((Boolean) result.get("enable"));
    }

    @Test
    void toggleUserStatus_shouldEnableDisabledUser() {
        testUserAuth.setEnable(false);
        when(userAuthRepository.findByUserId(1L)).thenReturn(testUserAuth);
        when(userAuthRepository.save(any(UserAuth.class))).thenReturn(testUserAuth);

        Map<String, Object> result = userService.toggleUserStatus(1L);

        assertEquals("用户已启用", result.get("message"));
        assertTrue((Boolean) result.get("enable"));
    }

    @Test
    void toggleUserStatus_shouldReturnErrorWhenUserNotFound() {
        when(userAuthRepository.findByUserId(999L)).thenReturn(null);

        Map<String, Object> result = userService.toggleUserStatus(999L);

        assertEquals("用户不存在", result.get("error"));
        verify(userAuthRepository, never()).save(any());
    }
}