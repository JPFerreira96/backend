package com.acme.card.repository;

import com.acme.card.domain.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {
  List<Card> findByUserId(UUID userId);

  @Query(value = "SELECT * FROM cards.cards c WHERE c.status = true AND c.tipo_cartao = :tipo", nativeQuery = true)
  List<Card> findActiveByTipo(@Param("tipo") String tipo);
}
