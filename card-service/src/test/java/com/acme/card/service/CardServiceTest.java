package com.acme.card.service;

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

import com.acme.card.domain.Card;
import com.acme.card.domain.TipoCartao;
import com.acme.card.repository.CardRepository;
import com.acme.card.web.dto.CardDTOs.CardResponse;
import com.acme.card.web.dto.CardDTOs.CreateCardRequest;

/**
 * Testes unitários para CardService
 * Testa apenas os métodos que existem na implementação atual
 */
@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository repository;

    @InjectMocks
    private CardService cardService;

    private Card card;
    private UUID cardId;
    private UUID userId;
    private UUID authUserId;

    @BeforeEach
    void setUp() {
        authUserId = UUID.randomUUID();

        card = Card.create("1234.5678.9012.3456", "Cartão Trabalho", TipoCartao.TRABALHADOR, UUID.randomUUID());
        cardId = card.getId();
        userId = card.getUserId();
    }

    @Test
    void getAllCards_ShouldReturnAllCards() {
        // Given
        List<Card> cards = List.of(card);
        when(repository.findAll()).thenReturn(cards);

        // When
        List<CardResponse> result = cardService.getAllCards();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("1234.5678.9012.3456", result.get(0).numeroCartao);
        assertEquals("Cartão Trabalho", result.get(0).nome);
        verify(repository).findAll();
    }

    @Test
    void getCardById_ShouldReturnCard_WhenCardExists() {
        // Given
        when(repository.findById(cardId)).thenReturn(Optional.of(card));

        // When
        CardResponse result = cardService.getCardById(cardId);

        // Then
        assertNotNull(result);
        assertEquals("1234.5678.9012.3456", result.numeroCartao);
        verify(repository).findById(cardId);
    }

    @Test
    void getCardById_ShouldThrowException_WhenCardNotFound() {
        // Given
        when(repository.findById(cardId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> 
            cardService.getCardById(cardId));
        verify(repository).findById(cardId);
    }

    @Test
    void getUserCards_ShouldReturnUserCards_WhenUserIsOwner() {
        // Given
        List<Card> cards = List.of(card);
        when(repository.findByUserId(userId)).thenReturn(cards);

        // When
        List<CardResponse> result = cardService.getUserCards(userId, userId, false);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repository).findByUserId(userId);
    }

    @Test
    void getUserCards_ShouldThrowException_WhenUserNotAuthorized() {
        // When & Then
        assertThrows(AccessDeniedException.class, () -> 
            cardService.getUserCards(userId, authUserId, false));
        verify(repository, never()).findByUserId(any());
    }

    @Test
    void getUserCards_ShouldReturnCards_WhenUserIsAdmin() {
        // Given
        List<Card> cards = List.of(card);
        when(repository.findByUserId(userId)).thenReturn(cards);

        // When
        List<CardResponse> result = cardService.getUserCards(userId, authUserId, true);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repository).findByUserId(userId);
    }

    @Test
    void createCard_ShouldCreateCard_WhenValidRequest() {
        // Given
        CreateCardRequest request = new CreateCardRequest();
        request.numeroCartao = "1234.5678.9012.3456";
        request.nome = "Cartão Trabalho";
        request.tipoCartao = TipoCartao.TRABALHADOR;
        request.userId = userId;

        when(repository.findByUserIdAndNumeroCartao(userId, "1234.5678.9012.3456"))
            .thenReturn(Optional.empty());
        when(repository.save(any(Card.class))).thenReturn(card);

        // When
        CardResponse result = cardService.createCard(request);

        // Then
        assertNotNull(result);
        assertEquals("1234.5678.9012.3456", result.numeroCartao);
        verify(repository).findByUserIdAndNumeroCartao(userId, "1234.5678.9012.3456");
        verify(repository).save(any(Card.class));
    }

    @Test
    void createCard_ShouldThrowException_WhenCardAlreadyExists() {
        // Given
        CreateCardRequest request = new CreateCardRequest();
        request.numeroCartao = "1234.5678.9012.3456";
        request.nome = "Cartão Trabalho";
        request.tipoCartao = TipoCartao.TRABALHADOR;
        request.userId = userId;

        when(repository.findByUserIdAndNumeroCartao(userId, "1234.5678.9012.3456"))
            .thenReturn(Optional.of(card));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            cardService.createCard(request));
        verify(repository).findByUserIdAndNumeroCartao(userId, "1234.5678.9012.3456");
        verify(repository, never()).save(any(Card.class));
    }
}