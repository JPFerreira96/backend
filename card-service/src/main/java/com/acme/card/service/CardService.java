package com.acme.card.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acme.card.domain.Card;
import com.acme.card.repository.CardRepository;
import com.acme.card.web.CardMapper;
import com.acme.card.web.dto.CardDTOs.AddCardRequest;
import com.acme.card.web.dto.CardDTOs.CardResponse;

@Service
public class CardService {
  private final CardRepository repo;

  public CardService(CardRepository repo) {
    this.repo = repo;
  }

  public List<CardResponse> list(UUID userId, UUID authId, boolean isAdmin) {
    if (!isAdmin && !userId.equals(authId))
      throw new AccessDeniedException("forbidden");
    return repo.findByUserId(userId).stream().map(CardMapper::toResponse).toList();
  }

  // @Transactional
  // public CardResponse add(UUID userId, AddCardRequest req, UUID authId, boolean
  // isAdmin){
  // if (!isAdmin && !userId.equals(authId)) throw new
  // AccessDeniedException("forbidden");
  // var card = Card.create(req.numeroCartao, req.nome, req.tipoCartao, userId);
  // return CardMapper.toResponse(repo.save(card));
  // }

  @Transactional
  public CardResponse add(UUID userId, AddCardRequest req, UUID authId, boolean isAdmin) {
    if (!isAdmin && !userId.equals(authId))
      throw new AccessDeniedException("forbidden");
    Card card = Card.create(req.numeroCartao, req.nome, req.tipoCartao, userId); // <-- Card explícito
    return CardMapper.toResponse(repo.save(card));
  }

  @Transactional
  public void remove(UUID userId, UUID cardId, UUID authId, boolean isAdmin) {
    var card = repo.findById(cardId).orElseThrow(() -> new NoSuchElementException("card"));
    if (!isAdmin && !card.getUserId().equals(authId))
      throw new AccessDeniedException("forbidden");
    repo.delete(card);
  }

  @Transactional
  public CardResponse toggle(UUID userId, UUID cardId, boolean status, UUID authId, boolean isAdmin) {
    var card = repo.findById(cardId).orElseThrow(() -> new NoSuchElementException("card"));
    if (!isAdmin && !card.getUserId().equals(authId))
      throw new AccessDeniedException("forbidden");
    if (status)
      card.activate();
    else
      card.deactivate();
    return CardMapper.toResponse(card);
  }

  public List<CardResponse> listActiveByTipo(String tipo) {
    return repo.findActiveByTipo(tipo).stream().map(CardMapper::toResponse).toList();
  }
}
