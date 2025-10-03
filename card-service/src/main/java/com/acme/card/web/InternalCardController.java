// package com.acme.card.web;

// import java.util.List;
// import java.util.UUID;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.DeleteMapping;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.PutMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestHeader;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import com.acme.card.service.CardService;
// import com.acme.card.web.dto.CardDTOs.CardResponse;
// import com.acme.card.web.dto.CardDTOs.CreateCardRequest;

// import jakarta.validation.Valid;

// /**
//  * Controller interno para comunicação entre microserviços
//  * Usado pelo User Service para gerenciar cartões
//  */
// @RestController
// @RequestMapping("/internal/cards")
// public class InternalCardController {
    
//     private final CardService cardService;
//     private final String internalSecret;
    
//     public InternalCardController(
//             CardService cardService,
//             @Value("${internal.secret:change-me}") String internalSecret) {
//         this.cardService = cardService;
//         this.internalSecret = internalSecret;
//     }
    
//     /**
//      * Valida se a requisição tem o secret interno
//      */
//     private boolean isValidInternalRequest(String secret) {
//         return internalSecret.equals(secret);
//     }
    
//     /**
//      * Lista cartões de um usuário (acesso interno)
//      */
//     @GetMapping("/user/{userId}")
//     public ResponseEntity<List<CardResponse>> getUserCards(
//             @PathVariable UUID userId,
//             @RequestHeader("X-Internal-Secret") String secret) {
        
//         if (!isValidInternalRequest(secret)) {
//             return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//         }
        
//         List<CardResponse> cards = cardService.internalGetUserCards(userId);
//         return ResponseEntity.ok(cards);
//     }
    
//     /**
//      * Cria um novo cartão (acesso interno)
//      */
//     @PostMapping
//     public ResponseEntity<CardResponse> createCard(
//             @Valid @RequestBody CreateCardRequest request,
//             @RequestHeader("X-Internal-Secret") String secret) {
        
//         if (!isValidInternalRequest(secret)) {
//             return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//         }
        
//         CardResponse card = cardService.internalCreateCard(request);
//         return ResponseEntity.status(HttpStatus.CREATED).body(card);
//     }
    
//     /**
//      * Remove um cartão de um usuário (acesso interno)
//      */
//     @DeleteMapping("/{cardId}/user/{userId}")
//     public ResponseEntity<Void> removeCardFromUser(
//             @PathVariable UUID cardId,
//             @PathVariable UUID userId,
//             @RequestHeader("X-Internal-Secret") String secret) {
        
//         if (!isValidInternalRequest(secret)) {
//             return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//         }
        
//         // Para acesso interno, usamos o próprio userId como authId
//         cardService.removeCardFromUser(userId, cardId, userId, true);
//         return ResponseEntity.noContent().build();
//     }
    
//     /**
//      * Ativa um cartão (acesso interno)
//      */
//     @PutMapping("/{cardId}/activate")
//     public ResponseEntity<Void> activateCard(
//             @PathVariable UUID cardId,
//             @RequestHeader("X-Internal-Secret") String secret,
//             @RequestHeader("X-User-Id") String userIdHeader) {
        
//         if (!isValidInternalRequest(secret)) {
//             return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//         }
        
//         UUID userId = UUID.fromString(userIdHeader);
//         cardService.activateCard(cardId, userId, true);
//         return ResponseEntity.noContent().build();
//     }
    
//     /**
//      * Desativa um cartão (acesso interno)
//      */
//     @PutMapping("/{cardId}/deactivate")
//     public ResponseEntity<Void> deactivateCard(
//             @PathVariable UUID cardId,
//             @RequestHeader("X-Internal-Secret") String secret,
//             @RequestHeader("X-User-Id") String userIdHeader) {
        
//         if (!isValidInternalRequest(secret)) {
//             return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//         }
        
//         UUID userId = UUID.fromString(userIdHeader);
//         cardService.deactivateCard(cardId, userId, true);
//         return ResponseEntity.noContent().build();
//     }
// }

// card-service
 package com.acme.card.web;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.acme.card.service.CardService;
import com.acme.card.web.dto.CardDTOs.CardResponse;

@RestController
@RequestMapping("/internal/cards")
public class InternalCardController {

  private final CardService cards;
  private final String expectedSecret;

  public InternalCardController(CardService cards,
      @Value("${internal.secret:change-me}") String expectedSecret) {
    this.cards = cards;
    this.expectedSecret = expectedSecret;
  }

  private void assertSecret(String provided) {
    if (provided == null || !provided.equals(expectedSecret)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid internal secret");
    }
  }

  @GetMapping("/user/{userId}")
  public List<CardResponse> getUserCardsInternal(
      @RequestHeader("X-Internal-Secret") String secret,
      @PathVariable UUID userId) {
    assertSecret(secret);
    return cards.internalGetUserCards(userId);
  }

  @PostMapping
  public CardResponse createCardInternal(
      @RequestHeader("X-Internal-Secret") String secret,
      @RequestBody com.acme.card.web.dto.CardDTOs.CreateCardRequest req) {
    assertSecret(secret);
    return cards.internalCreateCard(req);
  }

  @DeleteMapping("/{cardId}/user/{userId}")
  public void removeCardInternal(
      @RequestHeader("X-Internal-Secret") String secret,
      @PathVariable UUID cardId,
      @PathVariable UUID userId) {
    assertSecret(secret);
    cards.removeCardFromUser(userId, cardId, userId, true);
  }

  @PutMapping("/{cardId}/activate")
  public void activateInternal(
      @RequestHeader("X-Internal-Secret") String secret,
      @RequestHeader("X-User-Id") UUID userId,
      @PathVariable UUID cardId) {
    assertSecret(secret);
    cards.activateCard(cardId, userId, true);
  }

  @PutMapping("/{cardId}")
public CardResponse updateCardInternal(
    @RequestHeader("X-Internal-Secret") String secret,
    @RequestHeader("X-User-Id") UUID userId,  // quem está operando
    @PathVariable UUID cardId,
    @RequestBody com.acme.card.web.dto.CardDTOs.UpdateCardRequest req) {
  assertSecret(secret);
  // bypass de autorização: flag isAdmin = true porque é canal interno
  return cards.updateCard(cardId, req, userId, true);
}


  @PutMapping("/{cardId}/deactivate")
  public void deactivateInternal(
      @RequestHeader("X-Internal-Secret") String secret,
      @RequestHeader("X-User-Id") UUID userId,
      @PathVariable UUID cardId) {
    assertSecret(secret);
    cards.deactivateCard(cardId, userId, true);
  }
}
