package service;

import com.shop.user.dto.AuthResponse;
import com.shop.user.dto.RegisterRequest;
import com.shop.user.model.User;
import com.shop.user.repository.UserRepository;
import com.shop.user.service.JwtService;
import com.shop.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Юнит-тест для UserService
 *
 * Тестирует бизнес-логику регистрации пользователя:
 * - Проверка на существующий email
 * - Сохранение пользователя в БД
 * - Генерация JWT токена
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    private RegisterRequest registerRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {
        // Подготовка тестовых данных
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setName("Test User");
        registerRequest.setPhone("+1234567890");

        savedUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("hashedPassword")
                .name("Test User")
                .phone("+1234567890")
                .build();
    }

    @Test
    void register_Success_ReturnsAuthResponse() {
        // Arrange (Подготовка)
        // Настраиваем mock: email не существует
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        // Настраиваем mock: сохранение возвращает пользователя с ID
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        // Настраиваем mock: генерация токена
        when(jwtService.generateToken(anyLong(), anyString())).thenReturn("jwt-token-123");

        // Act (Действие)
        AuthResponse response = userService.register(registerRequest);

        // Assert (Проверка)
        assertNotNull(response);
        assertEquals("jwt-token-123", response.getToken());
        assertEquals(1L, response.getUserId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test User", response.getName());

        // Проверяем, что методы были вызваны
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(1L, "test@example.com");
    }

    @Test
    void register_EmailAlreadyExists_ThrowsException() {
        // Arrange
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.register(registerRequest);
        });

        assertEquals("Email already exists", exception.getMessage());

        // Проверяем, что save НЕ был вызван
        verify(userRepository, never()).save(any(User.class));
    }
}