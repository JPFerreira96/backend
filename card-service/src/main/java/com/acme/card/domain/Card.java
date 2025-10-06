package com.acme.card.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "cards",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_cards_user_numero", columnNames = {"user_id", "numero_cartao"})
    },
    indexes = {
        @Index(name = "idx_cards_user", columnList = "user_id")
    }
)
public class Card {

  @Id
  private UUID id = UUID.randomUUID();

  @Column(name="numero_cartao", nullable=false, length=19)
  private String numeroCartao;

  @Column(nullable=false, length=120)
  private String nome;

  @Column(nullable=false)
  private boolean status = true;

  @Enumerated(EnumType.STRING)
  @Column(name="tipo_cartao", nullable=false)
  private TipoCartao tipoCartao;

  @Column(name="user_id", nullable=false)
  private UUID userId;

  protected Card() {}

  private Card(String numero, String nome, TipoCartao tipo, UUID userId) {
    this.numeroCartao = numero;
    this.nome = nome;
    this.tipoCartao = tipo;
    this.userId = userId;
  }

  public static Card create(String numero, String nome, TipoCartao tipo, UUID userId) {
    return new Card(numero, nome, tipo, userId);
  }

  public UUID getId() { return id; }
  public String getNumeroCartao() { return numeroCartao; }
  public String getNome() { return nome; }
  public boolean isStatus() { return status; }
  public TipoCartao getTipoCartao() { return tipoCartao; }
  public UUID getUserId() { return userId; }

  public void rename(String n){ this.nome = n; }
  public void activate(){ this.status = true; }
  public void deactivate(){ this.status = false; }
}
