package com.example.cardservice.dto;

import com.example.cardservice.model.Card.TipoCartao;

import jakarta.validation.constraints.NotNull;

public class CardDto {
    @NotNull
    private Long userId;

    @NotNull
    private Long numeroCartao;

    @NotNull
    private String nome;

    @NotNull
    private Boolean status;

    @NotNull
    private TipoCartao tipoCartao;

    public CardDto() {}

    public CardDto(Long userId, Long numeroCartao, String nome, Boolean status, TipoCartao tipoCartao) {
        this.userId = userId;
        this.numeroCartao = numeroCartao;
        this.nome = nome;
        this.status = status;
        this.tipoCartao = tipoCartao;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getNumeroCartao() { return numeroCartao; }
    public void setNumeroCartao(Long numeroCartao) { this.numeroCartao = numeroCartao; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }

    public TipoCartao getTipoCartao() { return tipoCartao; }
    public void setTipoCartao(TipoCartao tipoCartao) { this.tipoCartao = tipoCartao; }
}