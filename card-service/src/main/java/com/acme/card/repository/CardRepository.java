package com.acme.card.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.acme.card.domain.Card;


public interface CardRepository extends JpaRepository<Card, UUID> {
   
    List<Card> findByUserId(UUID userId);
   
    @Query(value = "SELECT * FROM cards c WHERE c.user_id = :userId AND c.numero_cartao = :numeroCartao", nativeQuery = true)
    Optional<Card> findByUserIdAndNumeroCartao(@Param("userId") UUID userId, @Param("numeroCartao") String numeroCartao);
    
    @Query(value = "SELECT * FROM cards c WHERE c.status = true AND c.tipo_cartao = :tipo ORDER BY c.nome", nativeQuery = true)
    List<Card> findActiveByTipo(@Param("tipo") String tipo);
    
    @Query(value = "SELECT * FROM cards c WHERE c.user_id = :userId AND c.status = true ORDER BY c.nome", nativeQuery = true)
    List<Card> findActiveByUserId(@Param("userId") UUID userId);
    
    @Query(value = "SELECT * FROM cards c WHERE c.user_id = :userId AND c.status = false ORDER BY c.nome", nativeQuery = true)
    List<Card> findInactiveByUserId(@Param("userId") UUID userId);
    
    @Query(value = "SELECT COUNT(*) FROM cards c WHERE c.user_id = :userId", nativeQuery = true)
    int countCardsByUserId(@Param("userId") UUID userId);
    
    @Query(value = "SELECT COUNT(*) FROM cards c WHERE c.user_id = :userId AND c.status = true", nativeQuery = true)
    int countActiveCardsByUserId(@Param("userId") UUID userId);
    
    @Query(value = "SELECT * FROM cards c WHERE c.tipo_cartao = :tipo ORDER BY c.nome", nativeQuery = true)
    List<Card> findByTipoCartao(@Param("tipo") String tipo);
}
