package com.reins.bookstore.service;

import com.reins.bookstore.entity.User;
import com.reins.bookstore.entity.UserAuth;
import com.reins.bookstore.repository.UserAuthRepository;
import com.reins.bookstore.repository.UserRepository;
import com.reins.bookstore.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAuthRepository userAuthRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    public void testLoginSuccess() {
        UserAuth auth = new UserAuth();
        auth.setUsername("testuser");
        auth.setPassword("password");
        auth.setUserId(1L);
        auth.setIdentity(0);

        User user = new User();
        user.setId(1L);
        user.setNickname("Tester");

        when(userAuthRepository.findByUsername("testuser")).thenReturn(auth);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Map<String, Object> result = userService.login("testuser", "password");

        assertFalse(result.containsKey("error"));
        assertEquals(1L, result.get("userId"));
        assertEquals("testuser", result.get("username"));
        assertEquals("Tester", result.get("nickname"));
    }

    @Test
    public void testLoginFailInvalidPassword() {
        UserAuth auth = new UserAuth();
        auth.setUsername("testuser");
        auth.setPassword("correctPassword");

        when(userAuthRepository.findByUsername("testuser")).thenReturn(auth);

        Map<String, Object> result = userService.login("testuser", "wrongPassword");

        assertTrue(result.containsKey("error"));
        assertEquals("Invalid username or password", result.get("error"));
    }

    @Test
    public void testRegisterSuccess() {
        when(userAuthRepository.findByUsername("newuser")).thenReturn(null);
        
        User savedUser = new User();
        savedUser.setId(10L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        Map<String, String> result = userService.register("newuser", "pass", "Nick");

        assertFalse(result.containsKey("error"));
        assertEquals("User registered successfully", result.get("message"));
        verify(userAuthRepository, times(1)).save(any(UserAuth.class));
    }
}
