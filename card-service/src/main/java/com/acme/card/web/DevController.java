package com.acme.card.web;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.acme.card.domain.TipoCartao;
import com.acme.card.service.CardService;
import com.acme.card.web.dto.CardDTOs.CardResponse;
import com.acme.card.web.dto.CardDTOs.CreateCardRequest;

@RestController
@RequestMapping("/api/cards")
public class DevController {
    
    private final CardService cardService;

    public DevController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping("/dev/create-test-cards")
    public ResponseEntity<List<CardResponse>> createTestCards(Authentication authentication) {
        try {
            UUID userId = UUID.fromString(authentication.getName());
            
            List<CardResponse> createdCards = new ArrayList<>();
            
            // Cartão Trabalhador
            CreateCardRequest trabalhador = new CreateCardRequest();
            trabalhador.numeroCartao = "90.04.01987473-3";
            trabalhador.nome = "Cartão Trabalhador";
            trabalhador.tipoCartao = TipoCartao.TRABALHADOR;
            trabalhador.userId = userId;
            createdCards.add(cardService.createCard(trabalhador));
            
            // Cartão Estudantil
            CreateCardRequest estudantil = new CreateCardRequest();
            estudantil.numeroCartao = "90.03.01391738-7";
            estudantil.nome = "Cartão Estudantil";
            estudantil.tipoCartao = TipoCartao.ESTUDANTE;
            estudantil.userId = userId;
            createdCards.add(cardService.createCard(estudantil));
            
            // Cartão Comum
            CreateCardRequest comum = new CreateCardRequest();
            comum.numeroCartao = "90.02.01234567-8";
            comum.nome = "Cartão Comum";
            comum.tipoCartao = TipoCartao.COMUM;
            comum.userId = userId;
            createdCards.add(cardService.createCard(comum));
            
            return ResponseEntity.ok(createdCards);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}