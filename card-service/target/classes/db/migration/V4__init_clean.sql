-- CREATE TABLE IF NOT EXISTS cards (
--   id BINARY(16) NOT NULL,
--   numero_cartao VARCHAR(19) NOT NULL,        -- não use BIGINT (perde zeros à esquerda e pode estourar)
--   nome VARCHAR(120) NOT NULL,
--   status TINYINT(1) NOT NULL DEFAULT 1,      -- BOOLEAN em MySQL = TINYINT(1)
--   tipo_cartao VARCHAR(20) NOT NULL,
--   user_id BINARY(16) NOT NULL,

--   PRIMARY KEY (id),

--   -- 1 usuário não pode ter o mesmo número de cartão repetido
--   UNIQUE KEY uk_cards_user_numero (user_id, numero_cartao),

--   -- índice para buscas por usuário
--   KEY idx_cards_user (user_id),

--   -- FK para tabela users
--   CONSTRAINT fk_cards_user
--     FOREIGN KEY (user_id) REFERENCES users(id)
--     ON UPDATE CASCADE ON DELETE CASCADE
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS cards (
  id BINARY(16) NOT NULL,
  numero_cartao VARCHAR(19) NOT NULL,
  nome VARCHAR(120) NOT NULL,
  status TINYINT(1) NOT NULL DEFAULT 1,
  tipo_cartao VARCHAR(20) NOT NULL,
  user_id BINARY(16) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_cards_user_numero (user_id, numero_cartao),
  KEY idx_cards_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
