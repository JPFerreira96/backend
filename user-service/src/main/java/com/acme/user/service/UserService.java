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

/**
 * Service responsável pelo gerenciamento de usuários
 * Implementa todas as operações CRUD e integração com cartões
 */
@Service
public class UserService {
    
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository repository;
    private final CardClient cardClient;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    
    public UserService(UserRepository repository, CardClient cardClient) {
        this.repository = repository;
        this.cardClient = cardClient;
    }

    /**
     * Lista todos os usuários
     */
    public List<UserResponse> getAllUsers() {
        log.debug("Listando todos os usuários");
        return repository.findAll().stream()
            .map(this::mapToResponseWithCards)
            .toList();
    }

    /**
     * Busca usuário por ID
     */
    public UserResponse getUserById(UUID id) {
        log.debug("Buscando usuário por ID: {}", id);
        User user = repository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));
        return mapToResponseWithCards(user);
    }

    /**
     * Cria um novo usuário
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest request, boolean isAdmin) {
        log.debug("Criando novo usuário: {}", request.email);
        
        // Validação de role
        if (!isAdmin && request.role != null) {
            throw new AccessDeniedException("Role só pode ser definido por ADMIN");
        }
        
        // Validação de email único
        repository.findByEmail(request.email).ifPresent(u -> {
            throw new IllegalArgumentException("Email já está em uso");
        });
        
        // Criação do usuário
        User user = User.create(
            request.name,
            request.email,
            encoder.encode(request.password),
            isAdmin && request.role != null ? request.role : "ROLE_USER"
        );
        
        User savedUser = repository.save(user);
        log.info("Usuário criado com sucesso: {} (ID: {})", savedUser.getEmail(), savedUser.getId());
        
        return mapToResponseWithCards(savedUser);
    }

    /**
     * Atualiza um usuário existente
     */
    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request, UUID authUserId, boolean isAdmin) {
        log.debug("Atualizando usuário: {}", id);
        
        User user = repository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));
        
        // Verificação de autorização
        if (!isAdmin && !user.getId().equals(authUserId)) {
            throw new AccessDeniedException("Não autorizado a alterar este usuário");
        }
        
        // Validação de email único (se alterado)
        if (request.email != null && !request.email.equals(user.getEmail())) {
            repository.findByEmail(request.email).ifPresent(u -> {
                throw new IllegalArgumentException("Email já está em uso");
            });
        }
        
        // Atualização dos campos
        user.rename(request.name);
        if (request.email != null) {
            user.changeEmail(request.email);
        }
        
        log.info("Usuário atualizado com sucesso: {}", id);
        return mapToResponseWithCards(user);
    }

    /**
     * Remove um usuário
     */
    @Transactional
    public void deleteUser(UUID id) {
        log.debug("Removendo usuário: {}", id);
        
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("Usuário não encontrado");
        }
        
        repository.deleteById(id);
        log.info("Usuário removido com sucesso: {}", id);
    }

    /**
     * Altera senha do usuário
     */
    @Transactional
    public void changePassword(UUID id, ChangePasswordRequest request, UUID authUserId, boolean isAdmin) {
        log.debug("Alterando senha do usuário: {}", id);
        
        User user = repository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));
        
        // Verificação de autorização
        if (!isAdmin && !user.getId().equals(authUserId)) {
            throw new AccessDeniedException("Não autorizado a alterar senha deste usuário");
        }
        
        // Verificação da senha atual (apenas para o próprio usuário)
        if (!isAdmin && !encoder.matches(request.currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Senha atual incorreta");
        }
        
        user.changePassword(encoder.encode(request.newPassword));
        log.info("Senha alterada com sucesso para usuário: {}", id);
    }

    // === OPERAÇÕES COM CARTÕES ===

    /**
     * Adiciona um cartão ao usuário
     */
    @Transactional
    public CardSummary addCardToUser(UUID userId, AddCardToUserRequest request) {
        log.debug("Adicionando cartão ao usuário: {}", userId);
        
        // Verifica se o usuário existe
        if (!repository.existsById(userId)) {
            throw new NoSuchElementException("Usuário não encontrado");
        }
        
        // Cria o cartão via Card Service
        CardSummary card = cardClient.createCard(
            userId,
            request.numeroCartao,
            request.nome,
            request.tipoCartao.name()
        );
        
        log.info("Cartão adicionado ao usuário {}: {}", userId, card.numeroCartao);
        return card;
    }

    /**
     * Remove um cartão do usuário
     */
    @Transactional
    public void removeCardFromUser(UUID userId, UUID cardId) {
        log.debug("Removendo cartão {} do usuário: {}", cardId, userId);
        
        // Verifica se o usuário existe
        if (!repository.existsById(userId)) {
            throw new NoSuchElementException("Usuário não encontrado");
        }
        
        cardClient.removeCard(userId, cardId);
        log.info("Cartão {} removido do usuário: {}", cardId, userId);
    }

    /**
     * Ativa/Desativa um cartão do usuário
     */
    @Transactional
    public void toggleCardStatus(UUID userId, UUID cardId, boolean activate) {
        log.debug("{} cartão {} do usuário: {}", activate ? "Ativando" : "Desativando", cardId, userId);
        
        // Verifica se o usuário existe
        if (!repository.existsById(userId)) {
            throw new NoSuchElementException("Usuário não encontrado");
        }
        
        cardClient.toggleCardStatus(userId, cardId, activate);
        log.info("Cartão {} {} para usuário: {}", cardId, activate ? "ativado" : "desativado", userId);
    }

    // === MÉTODOS INTERNOS ===

    /**
     * Busca usuário por email (para autenticação)
     */
    public User internalFindByEmail(String email) {
        return repository.findByEmail(email).orElse(null);
    }

    /**
     * Mapeia User para UserResponse incluindo cartões
     */
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
