package com.example.userservice.model;

public class Card {
    private String numeroCartao;
    private String nome;
    private boolean status;
    private Card tipoCartao;

    public Card() {
    }

    public Card(String numeroCartao, String nome, boolean status, Card tipoCartao) {
        this.numeroCartao = numeroCartao;
        this.nome = nome;
        this.status = status;
        this.tipoCartao = tipoCartao;
    }

    public String getNumeroCartao() {
        return numeroCartao;
    }

    public void setNumeroCartao(String numeroCartao) {
        this.numeroCartao = numeroCartao;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Card getTipoCartao() {
        return tipoCartao;
    }

    public void setTipoCartao(Card tipoCartao) {
        this.tipoCartao = tipoCartao;
    }
}