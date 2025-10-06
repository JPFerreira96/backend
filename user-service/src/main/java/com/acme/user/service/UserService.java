package com.acme.user.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acme.user.domain.User;
import com.acme.user.repository.UserRepository;
import com.acme.user.web.UserMapper;
import com.acme.user.web.dto.UserDTOs.AddCardToUserRequest;
import com.acme.user.web.dto.UserDTOs.CardSummary;
import com.acme.user.web.dto.UserDTOs.ChangePasswordRequest;
import com.acme.user.web.dto.UserDTOs.CreateUserRequest;
import com.acme.user.web.dto.UserDTOs.UpdateUserRequest;
import com.acme.user.web.dto.UserDTOs.UserResponse;


@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository repository;
    private final CardClient cardClient;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private String normalizeRole(String role) {
        if (role == null || role.isBlank())
            return "ROLE_USER";
        return role.startsWith("ROLE_") ? role : "ROLE_" + role.toUpperCase();
    }

    public UserService(UserRepository repository, CardClient cardClient) {
        this.repository = repository;
        this.cardClient = cardClient;
    }

    public List<UserResponse> getAllUsers() {
        log.debug("Listando todos os usuários");
        return repository.findAll().stream()
                .map(this::mapToResponseWithCards)
                .toList();
    }

    public UserResponse getUserById(UUID id) {
        log.debug("Buscando usuário por ID: {}", id);
        User user = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));
        return mapToResponseWithCards(user);
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request, boolean isAdmin) {
        log.debug("Criando novo usuário: {}", request.email);

        if (!isAdmin && request.role != null) {
            throw new AccessDeniedException("Role só pode ser definido por ADMIN");
        }

        repository.findByEmail(request.email).ifPresent(u -> {
            throw new IllegalArgumentException("Email já está em uso");
        });

        User user = User.create(
                request.name,
                request.email,
                encoder.encode(request.password),
                isAdmin && request.role != null ? normalizeRole(request.role) : "ROLE_USER");

        User savedUser = repository.save(user);
        log.info("Usuário criado com sucesso: {} (ID: {})", savedUser.getEmail(), savedUser.getId());

        return mapToResponseWithCards(savedUser);
    }

    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request, UUID authUserId, boolean isAdmin) {
        log.debug("Atualizando usuário: {}", id);

        User user = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));

        if (!isAdmin && !user.getId().equals(authUserId)) {
            throw new AccessDeniedException("Não autorizado a alterar este usuário");
        }

        boolean hasName = request.name != null && !request.name.isBlank();
        boolean hasEmail = request.email != null && !request.email.isBlank();

        if (!hasName && !hasEmail) {
            throw new IllegalArgumentException("Informe ao menos nome ou email para atualizar");
        }

        if (hasEmail) {
            String newEmail = request.email.trim();
            if (!newEmail.equals(user.getEmail())) {
                repository.findByEmail(newEmail).ifPresent(u -> {
                    throw new IllegalArgumentException("Email já está em uso");
                });
            }
            user.changeEmail(newEmail);
        }

        if (hasName) {
            user.rename(request.name.trim());
        }

        log.info("Usuário atualizado com sucesso: {}", id);
        return mapToResponseWithCards(user);
    }

    @Transactional
    public void deleteUser(UUID id) {
        deleteUser(id, id, true);
    }

    @Transactional
    public void deleteUser(UUID id, UUID authUserId, boolean isAdmin) {
        log.debug("Removendo usuário: {}", id);

        if (!repository.existsById(id)) {
            throw new NoSuchElementException("Usuário não encontrado");
        }

        if (!isAdmin && !id.equals(authUserId)) {
            throw new AccessDeniedException("Não autorizado a remover este usuário");
        }

        repository.deleteById(id);
        log.info("Usuário removido com sucesso: {}", id);
    }

    @Transactional
    public void changePassword(UUID id, ChangePasswordRequest request, UUID authUserId, boolean isAdmin) {
        log.debug("Alterando senha do usuário: {}", id);

        User user = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));

        if (!isAdmin && !user.getId().equals(authUserId)) {
            throw new AccessDeniedException("Não autorizado a alterar senha deste usuário");
        }

        if (!isAdmin && !encoder.matches(request.currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Senha atual incorreta");
        }

        user.changePassword(encoder.encode(request.newPassword));
        log.info("Senha alterada com sucesso para usuário: {}", id);
    }

    @Transactional
    public CardSummary addCardToUser(UUID userId, AddCardToUserRequest request) {
        log.debug("Adicionando cartão ao usuário: {}", userId);

        if (!repository.existsById(userId)) {
            throw new NoSuchElementException("Usuário não encontrado");
        }

        CardSummary card = cardClient.createCard(
            userId,
            request.numeroCartao,
            request.nome,
            request.tipoCartao.name());

        log.info("Cartão adicionado ao usuário {}: {}", userId, card.numeroCartao);
        return card;
    }

    @Transactional
    public void removeCardFromUser(UUID userId, UUID cardId) {
        log.debug("Removendo cartão {} do usuário: {}", cardId, userId);

        if (!repository.existsById(userId)) {
            throw new NoSuchElementException("Usuário não encontrado");
        }

        cardClient.removeCard(userId, cardId);
        log.info("Cartão {} removido do usuário: {}", cardId, userId);
    }

    @Transactional
    public void toggleCardStatus(UUID userId, UUID cardId, boolean activate) {
        log.debug("{} cartão {} do usuário: {}", activate ? "Ativando" : "Desativando", cardId, userId);

        if (!repository.existsById(userId)) {
            throw new NoSuchElementException("Usuário não encontrado");
        }

        cardClient.toggleCardStatus(userId, cardId, activate);
        log.info("Cartão {} {} para usuário: {}", cardId, activate ? "ativado" : "desativado", userId);
    }

    public User internalFindByEmail(String email) {
        return repository.findByEmail(email).orElse(null);
    }

    @Transactional
    public User internalCreateUser(CreateUserRequest request) {
        log.debug("Criando usuário interno: {}", request.email);

        repository.findByEmail(request.email).ifPresent(u -> {
            throw new IllegalArgumentException("Email já está em uso");
        });

        User user = User.create(
                request.name,
                request.email,
                encoder.encode(request.password),
                normalizeRole(request.role));

        User savedUser = repository.save(user);
        log.info("Usuário interno criado: {} (ID: {})", savedUser.getEmail(), savedUser.getId());

        return savedUser;
    }

    public User internalVerifyCredentials(String email, String rawPassword) {
        var user = repository.findByEmail(email).orElse(null);
        if (user == null) return null;
        if (!encoder.matches(rawPassword, user.getPasswordHash())) return null;
        return user;
    }

    private UserResponse mapToResponseWithCards(User user) {
        UserResponse response = UserMapper.toResponse(user);

        // Busca cartões do usuário
        try {
            response.cards = cardClient.getUserCards(user.getId());
        } catch (Exception e) {
            log.warn("Erro ao buscar cartões do usuário {}: {}", user.getId(), e.getMessage());
            response.cards = List.of(); // Lista vazia em caso de erro
        }

        return response;
    }
}
