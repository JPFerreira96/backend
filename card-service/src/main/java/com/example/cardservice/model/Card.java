package com.example.cardservice.model;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "numero_cartao", nullable = false, unique = true)
    private Long numeroCartao;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Column(name = "status", nullable = false)
    private boolean status;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cartao", nullable = false)
    private TipoCartao tipoCartao;

    // Construtor sem args requerido pelo JPA
    public Card() {}

    public Card(Long userId, Long numeroCartao, String nome, boolean status, TipoCartao tipoCartao) {
        this.userId = userId;
        this.numeroCartao = numeroCartao;
        this.nome = nome;
        this.status = status;
        this.tipoCartao = tipoCartao;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getNumeroCartao() { return numeroCartao; }
    public void setNumeroCartao(Long numeroCartao) { this.numeroCartao = numeroCartao; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public boolean isStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }

    public TipoCartao getTipoCartao() { return tipoCartao; }
    public void setTipoCartao(TipoCartao tipoCartao) { this.tipoCartao = tipoCartao; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card)) return false;
        Card card = (Card) o;
        return status == card.status &&
               Objects.equals(id, card.id) &&
               Objects.equals(userId, card.userId) &&
               Objects.equals(numeroCartao, card.numeroCartao) &&
               Objects.equals(nome, card.nome) &&
               tipoCartao == card.tipoCartao;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, numeroCartao, nome, status, tipoCartao);
    }

    public enum TipoCartao { COMUM, ESTUDANTE, TRABALHADOR }
}