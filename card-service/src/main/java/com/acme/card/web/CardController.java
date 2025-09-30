package com.acme.card.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.acme.card.service.CardService;
import com.acme.card.web.dto.CardDTOs.AddCardRequest;
import com.acme.card.web.dto.CardDTOs.CardResponse;
import com.acme.card.web.dto.CardDTOs.CreateCardRequest;
import com.acme.card.web.dto.CardDTOs.ToggleStatusRequest;
import com.acme.card.web.dto.CardDTOs.UpdateCardRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controller REST para gerenciamento de cartões
 * Implementa todas as operações CRUD e funcionalidades específicas
 */
@RestController
@RequestMapping("/api/cards")
@Tag(name = "Cards", description = "API para gerenciamento de cartões de ônibus")
public class CardController {
    
    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    // === OPERAÇÕES GERAIS DE CARTÕES ===

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista todos os cartões", description = "Retorna todos os cartões do sistema (apenas ADMIN)")
    @ApiResponse(responseCode = "200", description = "Lista de cartões retornada com sucesso")
    public List<CardResponse> getAllCards() {
        return cardService.getAllCards();
    }

    @GetMapping("/{cardId}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Busca cartão por ID", description = "Retorna os dados de um cartão específico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cartão encontrado"),
        @ApiResponse(responseCode = "404", description = "Cartão não encontrado")
    })
    public CardResponse getCardById(
            @Parameter(description = "ID do cartão") @PathVariable UUID cardId) {
        return cardService.getCardById(cardId);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cria um novo cartão", description = "Cria um novo cartão no sistema (apenas ADMIN)")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Cartão criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "409", description = "Cartão já existe para este usuário")
    })
    public ResponseEntity<CardResponse> createCard(
            @Parameter(description = "Dados do cartão a ser criado") 
            @Valid @RequestBody CreateCardRequest request) {
        
        CardResponse card = cardService.createCard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(card);
    }

    @PutMapping("/{cardId}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Atualiza um cartão", description = "Atualiza os dados de um cartão existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cartão atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "403", description = "Não autorizado"),
        @ApiResponse(responseCode = "404", description = "Cartão não encontrado")
    })
    public CardResponse updateCard(
            @Parameter(description = "ID do cartão") @PathVariable UUID cardId,
            @Parameter(description = "Dados a serem atualizados") @Valid @RequestBody UpdateCardRequest request,
            Authentication authentication) {
        
        UUID authUserId = UUID.fromString(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(role -> role.equals("ROLE_ADMIN"));
            
        return cardService.updateCard(cardId, request, authUserId, isAdmin);
    }

    @DeleteMapping("/{cardId}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Remove um cartão", description = "Remove um cartão do sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Cartão removido com sucesso"),
        @ApiResponse(responseCode = "403", description = "Não autorizado"),
        @ApiResponse(responseCode = "404", description = "Cartão não encontrado")
    })
    public ResponseEntity<Void> deleteCard(
            @Parameter(description = "ID do cartão") @PathVariable UUID cardId,
            Authentication authentication) {
        
        UUID authUserId = UUID.fromString(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(role -> role.equals("ROLE_ADMIN"));
            
        cardService.removeCard(cardId, authUserId, isAdmin);
        return ResponseEntity.noContent().build();
    }

    // === OPERAÇÕES POR USUÁRIO ===

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Lista cartões de um usuário", description = "Retorna todos os cartões de um usuário específico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de cartões retornada"),
        @ApiResponse(responseCode = "403", description = "Não autorizado")
    })
    public List<CardResponse> getUserCards(
            @Parameter(description = "ID do usuário") @PathVariable UUID userId,
            Authentication authentication) {
        
        UUID authUserId = UUID.fromString(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(role -> role.equals("ROLE_ADMIN"));
            
        return cardService.getUserCards(userId, authUserId, isAdmin);
    }

    @PostMapping("/users/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Adiciona cartão a um usuário", description = "Adiciona um novo cartão a um usuário específico")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Cartão adicionado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "403", description = "Não autorizado"),
        @ApiResponse(responseCode = "409", description = "Cartão já existe para este usuário")
    })
    public ResponseEntity<CardResponse> addCardToUser(
            @Parameter(description = "ID do usuário") @PathVariable UUID userId,
            @Parameter(description = "Dados do cartão") @Valid @RequestBody AddCardRequest request,
            Authentication authentication) {
        
        UUID authUserId = UUID.fromString(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(role -> role.equals("ROLE_ADMIN"));
            
        CardResponse card = cardService.addCardToUser(userId, request, authUserId, isAdmin);
        return ResponseEntity.status(HttpStatus.CREATED).body(card);
    }

    @DeleteMapping("/users/{userId}/{cardId}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Remove cartão de um usuário", description = "Remove um cartão específico de um usuário")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Cartão removido com sucesso"),
        @ApiResponse(responseCode = "403", description = "Não autorizado"),
        @ApiResponse(responseCode = "404", description = "Cartão não encontrado")
    })
    public ResponseEntity<Void> removeCardFromUser(
            @Parameter(description = "ID do usuário") @PathVariable UUID userId,
            @Parameter(description = "ID do cartão") @PathVariable UUID cardId,
            Authentication authentication) {
        
        UUID authUserId = UUID.fromString(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(role -> role.equals("ROLE_ADMIN"));
            
        cardService.removeCardFromUser(userId, cardId, authUserId, isAdmin);
        return ResponseEntity.noContent().build();
    }

    // === OPERAÇÕES DE STATUS ===

    @PutMapping("/{cardId}/activate")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Ativa um cartão", description = "Ativa um cartão específico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cartão ativado com sucesso"),
        @ApiResponse(responseCode = "403", description = "Não autorizado"),
        @ApiResponse(responseCode = "404", description = "Cartão não encontrado")
    })
    public CardResponse activateCard(
            @Parameter(description = "ID do cartão") @PathVariable UUID cardId,
            Authentication authentication) {
        
        UUID authUserId = UUID.fromString(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(role -> role.equals("ROLE_ADMIN"));
            
        return cardService.activateCard(cardId, authUserId, isAdmin);
    }

    @PutMapping("/{cardId}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Desativa um cartão", description = "Desativa um cartão específico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cartão desativado com sucesso"),
        @ApiResponse(responseCode = "403", description = "Não autorizado"),
        @ApiResponse(responseCode = "404", description = "Cartão não encontrado")
    })
    public CardResponse deactivateCard(
            @Parameter(description = "ID do cartão") @PathVariable UUID cardId,
            Authentication authentication) {
        
        UUID authUserId = UUID.fromString(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(role -> role.equals("ROLE_ADMIN"));
            
        return cardService.deactivateCard(cardId, authUserId, isAdmin);
    }

    @PatchMapping("/{cardId}/status")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Alterna status do cartão", description = "Ativa ou desativa um cartão")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status alterado com sucesso"),
        @ApiResponse(responseCode = "403", description = "Não autorizado"),
        @ApiResponse(responseCode = "404", description = "Cartão não encontrado")
    })
    public CardResponse toggleCardStatus(
            @Parameter(description = "ID do cartão") @PathVariable UUID cardId,
            @Parameter(description = "Novo status") @RequestBody ToggleStatusRequest request,
            Authentication authentication) {
        
        UUID authUserId = UUID.fromString(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(role -> role.equals("ROLE_ADMIN"));
            
        return cardService.toggleCardStatus(cardId, request.status, authUserId, isAdmin);
    }

    // === CONSULTAS PÚBLICAS (com nativeQuery) ===

    @GetMapping("/active/by-type/{tipo}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Lista cartões ativos por tipo", description = "Retorna cartões ativos filtrados por tipo (usa nativeQuery)")
    @ApiResponse(responseCode = "200", description = "Lista de cartões ativos retornada")
    public List<CardResponse> getActiveCardsByType(
            @Parameter(description = "Tipo do cartão") @PathVariable String tipo) {
        return cardService.getActiveCardsByType(tipo);
    }

    @GetMapping("/by-type/{tipo}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Lista cartões por tipo", description = "Retorna todos os cartões filtrados por tipo (usa nativeQuery)")
    @ApiResponse(responseCode = "200", description = "Lista de cartões retornada")
    public List<CardResponse> getCardsByType(
            @Parameter(description = "Tipo do cartão") @PathVariable String tipo) {
        return cardService.getActiveCardsByType(tipo); // Reutiliza o método por enquanto
    }
}
