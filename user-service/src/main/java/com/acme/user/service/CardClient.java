package com.acme.user.service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.acme.user.web.dto.UserDTOs.CardSummary;

/**
 * Client para comunicação com o Card Service
 */
@Service
public class CardClient {
    
    private static final Logger log = LoggerFactory.getLogger(CardClient.class);
    
    private final RestClient restClient;
    private final String internalSecret;
    
    public CardClient(
            @Value("${services.card.base-url:http://localhost:8083}") String baseUrl,
            @Value("${internal.secret:change-me}") String internalSecret) {
        this.restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
        this.internalSecret = internalSecret;
    }
    
    /**
     * Busca todos os cartões de um usuário
     */
    public List<CardSummary> getUserCards(UUID userId) {
        try {
            log.debug("Buscando cartões do usuário: {}", userId);
            
            CardSummary[] cards = restClient.get()
                .uri("/internal/cards/user/{userId}", userId)
                .header("X-Internal-Secret", internalSecret)
                .retrieve()
                .body(CardSummary[].class);
                
            return cards != null ? List.of(cards) : Collections.emptyList();
            
        } catch (RestClientException e) {
            log.error("Erro ao buscar cartões do usuário {}: {}", userId, e.getMessage());
            return Collections.emptyList();
        }
    }
    
    /**
     * Cria um novo cartão para o usuário
     */
    public CardSummary createCard(UUID userId, String numeroCartao, String nome, String tipoCartao) {
        try {
            log.debug("Criando cartão para usuário: {}", userId);
            
            var request = new CreateCardRequest(userId, numeroCartao, nome, tipoCartao);
            
            return restClient.post()
                .uri("/internal/cards")
                .header("X-Internal-Secret", internalSecret)
                .body(request)
                .retrieve()
                .body(CardSummary.class);
                
        } catch (RestClientException e) {
            log.error("Erro ao criar cartão para usuário {}: {}", userId, e.getMessage());
            throw new RuntimeException("Erro ao criar cartão: " + e.getMessage());
        }
    }
    
    /**
     * Remove um cartão do usuário
     */
    public void removeCard(UUID userId, UUID cardId) {
        try {
            log.debug("Removendo cartão {} do usuário: {}", cardId, userId);
            
            restClient.delete()
                .uri("/internal/cards/{cardId}/user/{userId}", cardId, userId)
                .header("X-Internal-Secret", internalSecret)
                .retrieve()
                .toBodilessEntity();
                
        } catch (RestClientException e) {
            log.error("Erro ao remover cartão {} do usuário {}: {}", cardId, userId, e.getMessage());
            throw new RuntimeException("Erro ao remover cartão: " + e.getMessage());
        }
    }
    
    /**
     * Ativa/Desativa um cartão
     */
    public void toggleCardStatus(UUID userId, UUID cardId, boolean activate) {
        try {
            log.debug("{} cartão {} do usuário: {}", activate ? "Ativando" : "Desativando", cardId, userId);
            
            String endpoint = activate ? "/internal/cards/{cardId}/activate" : "/internal/cards/{cardId}/deactivate";
            
            restClient.put()
                .uri(endpoint, cardId)
                .header("X-Internal-Secret", internalSecret)
                .header("X-User-Id", userId.toString())
                .retrieve()
                .toBodilessEntity();
                
        } catch (RestClientException e) {
            log.error("Erro ao {} cartão {} do usuário {}: {}", 
                activate ? "ativar" : "desativar", cardId, userId, e.getMessage());
            throw new RuntimeException("Erro ao alterar status do cartão: " + e.getMessage());
        }
    }

   public CardSummary updateCard(UUID userId, UUID cardId, String nome, Boolean status) {
    try {
        var req = new UpdateCardRequest(nome, status);

        return restClient.put()
            .uri("/internal/cards/{cardId}", cardId)
            .header("X-Internal-Secret", internalSecret)
            .header("X-User-Id", userId.toString())
            .body(req)
            .retrieve()
            .body(CardSummary.class);
    } catch (RestClientException e) {
        log.error("Erro ao atualizar cartão {} do usuário {}: {}", cardId, userId, e.getMessage());
        throw new RuntimeException("Erro ao atualizar cartão: " + e.getMessage());
    }
}

static class UpdateCardRequest {
    public String nome;
    public Boolean status;
    UpdateCardRequest() {}
    UpdateCardRequest(String nome, Boolean status) {
        this.nome = nome;
        this.status = status;
    }
}
    
    /**
     * DTO para criação de cartão
     */
    public static class CreateCardRequest {
        public UUID userId;
        public String numeroCartao;
        public String nome;
        public String tipoCartao;
        
        public CreateCardRequest() {}
        
        public CreateCardRequest(UUID userId, String numeroCartao, String nome, String tipoCartao) {
            this.userId = userId;
            this.numeroCartao = numeroCartao;
            this.nome = nome;
            this.tipoCartao = tipoCartao;
        }
    }
}