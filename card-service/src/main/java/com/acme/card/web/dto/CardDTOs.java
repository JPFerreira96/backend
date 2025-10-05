// com.acme.card.web.dto.CardDTOs

package com.acme.card.web.dto;

import java.util.UUID;

import com.acme.card.domain.TipoCartao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CardDTOs {

  public static class CreateCardRequest {
    @Size(min=13, max=30)
    public String numeroCartao;

    @Size(max=120)
    public String nome;

    @NotNull
    public TipoCartao tipoCartao;

    @NotNull
    public UUID userId;
  }

  public static class UpdateCardRequest {
    @NotBlank @Size(max=120)
    public String nome;

    public Boolean status; // opcional
  }

  public static class CardResponse {
    public UUID id;
    public String numeroCartao;   // <-- STRING AQUI
    public String nome;
    public boolean status;
    public TipoCartao tipoCartao;
  }

  public static class AddCardRequest {
    public String numeroCartao;   // <-- STRING
    public String nome;
    public TipoCartao tipoCartao;
  }

  public static class ToggleStatusRequest {
    public boolean status; // <- o campo que o controller usa
  }
}
