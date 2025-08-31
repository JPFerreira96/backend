package com.acme.card.web;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.acme.card.service.CardService;
import com.acme.card.web.dto.CardDTOs.AddCardRequest;
import com.acme.card.web.dto.CardDTOs.CardResponse;
import com.acme.card.web.dto.CardDTOs.ToggleStatusRequest;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cards")
@Tag(name = "Cards")
public class CardController {
  private final CardService svc;

  public CardController(CardService svc) {
    this.svc = svc;
  }

  @GetMapping("/users/{userId}")
  @PreAuthorize("hasAnyRole('ADMIN','USER')")

  public List<CardResponse> list(@PathVariable UUID userId, Principal p) {
    var authId = UUID.fromString(p.getName());
    boolean isAdmin = false;
    return svc.list(userId, authId, isAdmin);
  }

  @PostMapping("/users/{userId}")
  @PreAuthorize("hasAnyRole('ADMIN','USER')")

  public CardResponse add(
      @PathVariable UUID userId,
      @Valid @RequestBody AddCardRequest req,
      Principal p) {
    var authId = UUID.fromString(p.getName());
    boolean isAdmin = false;
    return svc.add(userId, req, authId, isAdmin);
  }

  @DeleteMapping("/users/{userId}/{cardId}")
  @PreAuthorize("hasAnyRole('ADMIN','USER')")

  public void remove(
      @PathVariable UUID userId,
      @PathVariable UUID cardId,
      Principal p) {
    var authId = UUID.fromString(p.getName());
    boolean isAdmin = false;
    svc.remove(userId, cardId, authId, isAdmin);
  }

  @PatchMapping("/users/{userId}/{cardId}/status")
  @PreAuthorize("hasAnyRole('ADMIN','USER')")
  
  public CardResponse toggle(
      @PathVariable UUID userId,
      @PathVariable UUID cardId,
      @RequestBody ToggleStatusRequest req,
      Principal p) {
    var authId = UUID.fromString(p.getName());
    boolean isAdmin = false;
    return svc.toggle(userId, cardId, req.status, authId, isAdmin);
  }

  // nativeQuery showcase
  @GetMapping("/active/by-type/{tipo}")
  @PreAuthorize("hasAnyRole('ADMIN','USER')")
  public List<CardResponse> activeByTipo(@PathVariable String tipo) {
    return svc.listActiveByTipo(tipo);
  }
}
