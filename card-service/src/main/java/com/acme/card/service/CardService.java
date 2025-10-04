package com.acme.card.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acme.card.domain.Card;
import com.acme.card.repository.CardRepository;
import com.acme.card.web.CardMapper;
import com.acme.card.web.dto.CardDTOs.AddCardRequest;
import com.acme.card.web.dto.CardDTOs.CardResponse;
import com.acme.card.web.dto.CardDTOs.CreateCardRequest;
import com.acme.card.web.dto.CardDTOs.UpdateCardRequest;

/**
 * Service responsável pelo gerenciamento de cartões
 * Implementa todas as operações CRUD e validações de negócio
 */
@Service
public class CardService {
    
    private static final Logger log = LoggerFactory.getLogger(CardService.class);
    
    private final CardRepository repository;

    public CardService(CardRepository repository) {
        this.repository = repository;
    }

    /**
     * Lista todos os cartões (apenas para ADMIN)
     */
    public List<CardResponse> getAllCards() {
        log.debug("Listando todos os cartões");
        return repository.findAll().stream()
            .map(CardMapper::toResponse)
            .toList();
    }

    /**
     * Busca cartão por ID
     */
    public CardResponse getCardById(UUID cardId) {
        log.debug("Buscando cartão por ID: {}", cardId);
        Card card = repository.findById(cardId)
            .orElseThrow(() -> new NoSuchElementException("Cartão não encontrado"));
        return CardMapper.toResponse(card);
    }

    /**
     * Lista cartões de um usuário específico
     */
    public List<CardResponse> getUserCards(UUID userId, UUID authId, boolean isAdmin) {
        log.debug("Listando cartões do usuário: {}", userId);
        
        if (!isAdmin && !userId.equals(authId)) {
            throw new AccessDeniedException("Não autorizado a visualizar cartões deste usuário");
        }
        
        return repository.findByUserId(userId).stream()
            .map(CardMapper::toResponse)
            .toList();
    }

    /**
     * Cria um novo cartão
     */
    @Transactional
    public CardResponse createCard(CreateCardRequest request) {
        log.debug("Criando novo cartão para usuário: {}", request.userId);
        
        // Validação: usuário não pode ter cartão duplicado
        repository.findByUserIdAndNumeroCartao(request.userId, request.numeroCartao)
            .ifPresent(card -> {
                throw new IllegalArgumentException("Usuário já possui cartão com este número");
            });
        
        Card card = Card.create(request.numeroCartao, request.nome, request.tipoCartao, request.userId);
        Card savedCard = repository.save(card);
        
        log.info("Cartão criado com sucesso: {} para usuário: {}", savedCard.getId(), request.userId);
        return CardMapper.toResponse(savedCard);
    }

    /**
     * Adiciona cartão a um usuário (validando autorização)
     */
    @Transactional
    public CardResponse addCardToUser(UUID userId, AddCardRequest request, UUID authId, boolean isAdmin) {
        log.debug("Adicionando cartão ao usuário: {}", userId);
        
        if (!isAdmin && !userId.equals(authId)) {
            throw new AccessDeniedException("Não autorizado a adicionar cartão para este usuário");
        }
        
        // Validação: usuário não pode ter cartão duplicado
        repository.findByUserIdAndNumeroCartao(userId, request.numeroCartao)
            .ifPresent(card -> {
                throw new IllegalArgumentException("Usuário já possui cartão com este número");
            });
        
        Card card = Card.create(request.numeroCartao, request.nome, request.tipoCartao, userId);
        Card savedCard = repository.save(card);
        
        log.info("Cartão adicionado com sucesso: {} para usuário: {}", savedCard.getId(), userId);
        return CardMapper.toResponse(savedCard);
    }

    /**
     * Atualiza um cartão existente
     */
    @Transactional
    public CardResponse updateCard(UUID cardId, UpdateCardRequest request, UUID authId, boolean isAdmin) {
        log.debug("Atualizando cartão: {}", cardId);
        
        Card card = repository.findById(cardId)
            .orElseThrow(() -> new NoSuchElementException("Cartão não encontrado"));
        
        if (!isAdmin && !card.getUserId().equals(authId)) {
            throw new AccessDeniedException("Não autorizado a alterar este cartão");
        }
        
        card.rename(request.nome);
        
        if (request.status != null) {
            if (request.status) {
                card.activate();
            } else {
                card.deactivate();
            }
        }
        
        log.info("Cartão atualizado com sucesso: {}", cardId);
        return CardMapper.toResponse(card);
    }

    /**
     * Remove um cartão
     */
    @Transactional
    public void removeCard(UUID cardId, UUID authId, boolean isAdmin) {
        log.debug("Removendo cartão: {}", cardId);
        
        Card card = repository.findById(cardId)
            .orElseThrow(() -> new NoSuchElementException("Cartão não encontrado"));
        
        if (!isAdmin && !card.getUserId().equals(authId)) {
            throw new AccessDeniedException("Não autorizado a remover este cartão");
        }
        
        repository.delete(card);
        log.info("Cartão removido com sucesso: {}", cardId);
    }

    /**
     * Remove cartão de um usuário específico (com validação adicional)
     */
    @Transactional
    public void removeCardFromUser(UUID userId, UUID cardId, UUID authId, boolean isAdmin) {
        log.debug("Removendo cartão {} do usuário: {}", cardId, userId);
        
        Card card = repository.findById(cardId)
            .orElseThrow(() -> new NoSuchElementException("Cartão não encontrado"));
        
        // Validação dupla: autorização e pertence ao usuário
        if (!isAdmin && !card.getUserId().equals(authId)) {
            throw new AccessDeniedException("Não autorizado a remover este cartão");
        }
        
        if (!card.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Cartão não pertence ao usuário especificado");
        }
        
        repository.delete(card);
        log.info("Cartão {} removido do usuário: {}", cardId, userId);
    }

    /**
     * Ativa um cartão
     */
    @Transactional
    public CardResponse activateCard(UUID cardId, UUID authId, boolean isAdmin) {
        log.debug("Ativando cartão: {}", cardId);
        return toggleCardStatus(cardId, true, authId, isAdmin);
    }

    /**
     * Desativa um cartão
     */
    @Transactional
    public CardResponse deactivateCard(UUID cardId, UUID authId, boolean isAdmin) {
        log.debug("Desativando cartão: {}", cardId);
        return toggleCardStatus(cardId, false, authId, isAdmin);
    }

    /**
     * Alterna status de um cartão
     */
    @Transactional
    public CardResponse toggleCardStatus(UUID cardId, boolean activate, UUID authId, boolean isAdmin) {
        Card card = repository.findById(cardId)
            .orElseThrow(() -> new NoSuchElementException("Cartão não encontrado"));
        
        if (!isAdmin && !card.getUserId().equals(authId)) {
            throw new AccessDeniedException("Não autorizado a alterar status deste cartão");
        }
        
        if (activate) {
            card.activate();
        } else {
            card.deactivate();
        }
        
        log.info("Cartão {} {}: {}", cardId, activate ? "ativado" : "desativado", card.getNumeroCartao());
        return CardMapper.toResponse(card);
    }

    /**
     * Lista cartões ativos por tipo (consulta pública)
     */
    public List<CardResponse> getActiveCardsByType(String tipo) {
        log.debug("Listando cartões ativos do tipo: {}", tipo);
        return repository.findActiveByTipo(tipo).stream()
            .map(CardMapper::toResponse)
            .toList();
    }

    // === MÉTODOS INTERNOS (para comunicação entre serviços) ===

    /**
     * Lista cartões de um usuário (acesso interno)
     */
    public List<CardResponse> internalGetUserCards(UUID userId) {
        log.debug("Acesso interno - listando cartões do usuário: {}", userId);
        return repository.findByUserId(userId).stream()
            .map(CardMapper::toResponse)
            .toList();
    }

    /**
     * Cria cartão via acesso interno
     */
    @Transactional
    public CardResponse internalCreateCard(CreateCardRequest request) {
        log.debug("Acesso interno - criando cartão para usuário: {}", request.userId);
        
        repository.findByUserIdAndNumeroCartao(request.userId, request.numeroCartao)
            .ifPresent(card -> {
                throw new IllegalArgumentException("Usuário já possui cartão com este número");
            });
        
        Card card = Card.create(request.numeroCartao, request.nome, request.tipoCartao, request.userId);
        Card savedCard = repository.save(card);
        
        log.info("Cartão criado via acesso interno: {} para usuário: {}", savedCard.getId(), request.userId);
        return CardMapper.toResponse(savedCard);
    }

    
}
