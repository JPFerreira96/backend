package com.acme.card.web;

import com.acme.card.domain.Card;
import com.acme.card.web.dto.CardDTOs.CardResponse;

public class CardMapper {
  public static CardResponse toResponse(Card c){
    var dto = new CardResponse();
    dto.id=c.getId(); 
    dto.numeroCartao=c.getNumeroCartao(); 
    dto.nome=c.getNome(); dto.status=c.isStatus(); 
    dto.tipoCartao=c.getTipoCartao();
    return dto;
  }
}
