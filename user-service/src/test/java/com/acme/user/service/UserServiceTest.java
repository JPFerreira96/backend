package com.acme.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.acme.user.domain.User;
import com.acme.user.repository.UserRepository;
import com.acme.user.web.dto.UserDTOs.AddCardToUserRequest;
import com.acme.user.web.dto.UserDTOs.CardSummary;
import com.acme.user.web.dto.UserDTOs.ChangePasswordRequest;
import com.acme.user.web.dto.UserDTOs.CreateUserRequest;
import com.acme.user.web.dto.UserDTOs.TipoCartao;
import com.acme.user.web.dto.UserDTOs.UpdateUserRequest;
import com.acme.user.web.dto.UserDTOs.UserResponse;

/**
 * Testes unitários para UserService
 * Implementa cenários de sucesso e falha para todas as operações
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private CardClient cardClient;

    @InjectMocks
    private UserService userService;

    private User user;
    private UUID userId;
    private UUID authUserId;

    @BeforeEach
    void setUp() {
        user = spy(User.create("João Silva", "joao@email.com", "hashedPassword", "ROLE_USER"));
        userId = user.getId();
        authUserId = UUID.randomUUID();
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Given
        List<User> users = List.of(user);
        when(repository.findAll()).thenReturn(users);
        when(cardClient.getUserCards(any(UUID.class))).thenReturn(List.of());

        // When
        List<UserResponse> result = userService.getAllUsers();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("João Silva", result.get(0).name);
        assertEquals("joao@email.com", result.get(0).email);
        verify(repository).findAll();
    }

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() {
        // Given
        when(repository.findById(userId)).thenReturn(Optional.of(user));
        when(cardClient.getUserCards(userId)).thenReturn(List.of());

        // When
        UserResponse result = userService.getUserById(userId);

        // Then
        assertNotNull(result);
        assertEquals("João Silva", result.name);
        assertEquals("joao@email.com", result.email);
        verify(repository).findById(userId);
    }

    @Test
    void getUserById_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(repository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> userService.getUserById(userId));
        verify(repository).findById(userId);
    }

    @Test
    void createUser_ShouldCreateUser_WhenValidRequest() {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.name = "Maria Santos";
        request.email = "maria@email.com";
        request.password = "senha123";

        when(repository.findByEmail("maria@email.com")).thenReturn(Optional.empty());
        when(repository.save(any(User.class))).thenReturn(user);
        when(cardClient.getUserCards(any(UUID.class))).thenReturn(List.of());

        // When
        UserResponse result = userService.createUser(request, false);

        // Then
        assertNotNull(result);
        verify(repository).findByEmail("maria@email.com");
        verify(repository).save(any(User.class));
    }

    @Test
    void createUser_ShouldThrowException_WhenEmailAlreadyExists() {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.name = "Maria Santos";
        request.email = "joao@email.com";
        request.password = "senha123";

        when(repository.findByEmail("joao@email.com")).thenReturn(Optional.of(user));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(request, false));
        verify(repository).findByEmail("joao@email.com");
        verify(repository, never()).save(any(User.class));
    }

    @Test
    void createUser_ShouldThrowException_WhenNonAdminTriesToSetRole() {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.name = "Maria Santos";
        request.email = "maria@email.com";
        request.password = "senha123";
        request.role = "ROLE_ADMIN";

        // When & Then
        assertThrows(AccessDeniedException.class, () -> userService.createUser(request, false));
    }

    @Test
    void updateUser_ShouldUpdateUser_WhenAuthorized() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest();
        request.name = "João Silva Atualizado";
        request.email = "joao.novo@email.com";

        when(repository.findById(userId)).thenReturn(Optional.of(user));
        when(repository.findByEmail("joao.novo@email.com")).thenReturn(Optional.empty());
        when(cardClient.getUserCards(userId)).thenReturn(List.of());

        // When
        UserResponse result = userService.updateUser(userId, request, userId, false);

        // Then
        assertNotNull(result);
        verify(repository).findById(userId);
        verify(user).rename("João Silva Atualizado");
        verify(user).changeEmail("joao.novo@email.com");
    }

    @Test
    void updateUser_ShouldThrowException_WhenNotAuthorized() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest();
        request.name = "João Silva Atualizado";

        when(repository.findById(userId)).thenReturn(Optional.of(user));

        // When & Then
        assertThrows(AccessDeniedException.class, () -> 
            userService.updateUser(userId, request, authUserId, false));
        verify(repository).findById(userId);
    }

    @Test
    void updateUser_ShouldAllowAdminToChangeOnlyEmail() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest();
        request.email = "admin.novo@email.com";

        when(repository.findById(userId)).thenReturn(Optional.of(user));
        when(repository.findByEmail("admin.novo@email.com")).thenReturn(Optional.empty());
        when(cardClient.getUserCards(userId)).thenReturn(List.of());

        // When
        UserResponse result = userService.updateUser(userId, request, authUserId, true);

        // Then
        assertNotNull(result);
        verify(repository).findById(userId);
        verify(repository).findByEmail("admin.novo@email.com");
        verify(user, never()).rename(anyString());
        verify(user).changeEmail("admin.novo@email.com");
    }

    @Test
    void updateUser_ShouldThrowException_WhenNoFieldsProvided() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest();
        when(repository.findById(userId)).thenReturn(Optional.of(user));

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
            userService.updateUser(userId, request, userId, false));
        verify(repository).findById(userId);
        verify(user, never()).rename(anyString());
        verify(user, never()).changeEmail(anyString());
    }

    @Test
    void deleteUser_ShouldDeleteUser_WhenUserExists() {
        // Given
        when(repository.existsById(userId)).thenReturn(true);

        // When
        userService.deleteUser(userId, userId, false);

        // Then
        verify(repository).existsById(userId);
        verify(repository).deleteById(userId);
    }

    @Test
    void deleteUser_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(repository.existsById(userId)).thenReturn(false);

        // When & Then
        assertThrows(NoSuchElementException.class, () -> userService.deleteUser(userId, userId, true));
        verify(repository).existsById(userId);
        verify(repository, never()).deleteById(any());
    }

    @Test
    void deleteUser_ShouldThrowException_WhenNotAuthorized() {
        // Given
        when(repository.existsById(userId)).thenReturn(true);
        UUID anotherUserId = UUID.randomUUID();

        // When & Then
        assertThrows(AccessDeniedException.class, () ->
            userService.deleteUser(userId, anotherUserId, false));
        verify(repository).existsById(userId);
        verify(repository, never()).deleteById(any());
    }

    @Test
    void changePassword_ShouldChangePassword_WhenCurrentPasswordIsCorrect() {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.currentPassword = "senhaAtual";
        request.newPassword = "novaSenha123";

        when(repository.findById(userId)).thenReturn(Optional.of(user));
        when(user.getPasswordHash()).thenReturn(new BCryptPasswordEncoder().encode("senhaAtual"));

        // When
        userService.changePassword(userId, request, userId, false);

        // Then
        verify(repository).findById(userId);
        verify(user).changePassword(anyString());
    }

    @Test
    void changePassword_ShouldThrowException_WhenCurrentPasswordIsIncorrect() {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.currentPassword = "senhaErrada";
        request.newPassword = "novaSenha123";

        when(repository.findById(userId)).thenReturn(Optional.of(user));
        when(user.getPasswordHash()).thenReturn(new BCryptPasswordEncoder().encode("senhaCorreta"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            userService.changePassword(userId, request, userId, false));
        verify(repository).findById(userId);
        verify(user, never()).changePassword(anyString());
    }

    @Test
    void addCardToUser_ShouldAddCard_WhenUserExists() {
        // Given
        AddCardToUserRequest request = new AddCardToUserRequest();
        request.numeroCartao = "1234.5678.9012.3456";
        request.nome = "Cartão Trabalho";
        request.tipoCartao = TipoCartao.TRABALHADOR;

        CardSummary cardSummary = new CardSummary();
        cardSummary.numeroCartao = request.numeroCartao;

        when(repository.existsById(userId)).thenReturn(true);
        when(cardClient.createCard(userId, request.numeroCartao, request.nome, request.tipoCartao.name()))
            .thenReturn(cardSummary);

        // When
        CardSummary result = userService.addCardToUser(userId, request);

        // Then
        assertNotNull(result);
        assertEquals(request.numeroCartao, result.numeroCartao);
        verify(repository).existsById(userId);
        verify(cardClient).createCard(userId, request.numeroCartao, request.nome, request.tipoCartao.name());
    }

    @Test
    void addCardToUser_ShouldThrowException_WhenUserNotFound() {
        // Given
        AddCardToUserRequest request = new AddCardToUserRequest();
        request.numeroCartao = "1234.5678.9012.3456";
        request.nome = "Cartão Trabalho";
        request.tipoCartao = TipoCartao.TRABALHADOR;

        when(repository.existsById(userId)).thenReturn(false);

        // When & Then
        assertThrows(NoSuchElementException.class, () -> userService.addCardToUser(userId, request));
        verify(repository).existsById(userId);
        verify(cardClient, never()).createCard(any(), any(), any(), any());
    }

    @Test
    void removeCardFromUser_ShouldRemoveCard_WhenUserExists() {
        // Given
        UUID cardId = UUID.randomUUID();
        when(repository.existsById(userId)).thenReturn(true);

        // When
        userService.removeCardFromUser(userId, cardId);

        // Then
        verify(repository).existsById(userId);
        verify(cardClient).removeCard(userId, cardId);
    }

    @Test
    void toggleCardStatus_ShouldActivateCard_WhenUserExists() {
        // Given
        UUID cardId = UUID.randomUUID();
        when(repository.existsById(userId)).thenReturn(true);

        // When
        userService.toggleCardStatus(userId, cardId, true);

        // Then
        verify(repository).existsById(userId);
        verify(cardClient).toggleCardStatus(userId, cardId, true);
    }

    @Test
    void internalFindByEmail_ShouldReturnUser_WhenUserExists() {
        // Given
        when(repository.findByEmail("joao@email.com")).thenReturn(Optional.of(user));

        // When
        User result = userService.internalFindByEmail("joao@email.com");

        // Then
        assertNotNull(result);
        assertEquals("joao@email.com", result.getEmail());
        verify(repository).findByEmail("joao@email.com");
    }

    @Test
    void internalFindByEmail_ShouldReturnNull_WhenUserNotFound() {
        // Given
        when(repository.findByEmail("inexistente@email.com")).thenReturn(Optional.empty());

        // When
        User result = userService.internalFindByEmail("inexistente@email.com");

        // Then
        assertNull(result);
        verify(repository).findByEmail("inexistente@email.com");
    }
}