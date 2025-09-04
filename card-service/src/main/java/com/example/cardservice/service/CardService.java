package com.example.cardservice.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.cardservice.dto.CardDto;
import com.example.cardservice.model.Card;
import com.example.cardservice.repository.CardRepository;

@Service
public class CardService {

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public List<Card> getAllCards() {
        return cardRepository.findAll();
    }

    public Optional<Card> getCardById(Long id) {
        return cardRepository.findById(id);
    }

    public Card createCard(CardDto cardDto) {
        Card card = new Card();
        card.setUserId(cardDto.getUserId()); // faltava
        card.setNumeroCartao(cardDto.getNumeroCartao());
        card.setNome(cardDto.getNome());
        card.setStatus(Boolean.TRUE.equals(cardDto.getStatus())); // evita NPE
        card.setTipoCartao(cardDto.getTipoCartao());
        return cardRepository.save(card);
    }

    public Card updateCard(Long id, CardDto cardDto) {
        Card card = cardRepository.findById(id).orElseThrow(() -> new RuntimeException("Card not found"));
        card.setUserId(cardDto.getUserId());
        card.setNumeroCartao(cardDto.getNumeroCartao());
        card.setNome(cardDto.getNome());
        card.setStatus(Boolean.TRUE.equals(cardDto.getStatus()));
        card.setTipoCartao(cardDto.getTipoCartao());
        return cardRepository.save(card);
    }

    public void deleteCard(Long id) {
        cardRepository.deleteById(id);
    }

    public void activateCard(Long id) {
        Card card = cardRepository.findById(id).orElseThrow(() -> new RuntimeException("Card not found"));
        card.setStatus(true);
        cardRepository.save(card);
    }

    public void deactivateCard(Long id) {
        Card card = cardRepository.findById(id).orElseThrow(() -> new RuntimeException("Card not found"));
        card.setStatus(false);
        cardRepository.save(card);
    }
}