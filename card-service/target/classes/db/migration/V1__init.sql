-- SQL script to initialize the database for the card service

SET NAMES utf8mb4;
SET time_zone = '+00:00';

CREATE TABLE IF NOT EXISTS cards (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  numero_cartao BIGINT NOT NULL,
  nome VARCHAR(120) NOT NULL,
  status TINYINT(1) NOT NULL DEFAULT 1,
  tipo_cartao ENUM('COMUM','ESTUDANTE','TRABALHADOR') NOT NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_cards_numero (numero_cartao),
  KEY idx_cards_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;