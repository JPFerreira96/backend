-- Execute este SQL no MySQL Workbench para criar usuários de teste

USE db_card_user;

-- Limpar tabela se necessário
-- DELETE FROM users;

-- Criar usuários de teste
INSERT INTO users (id, name, email, password_hash, role, created_at) VALUES 
-- Admin (senha: 123456)
(UNHEX(REPLACE(UUID(), '-', '')), 'Administrador', 'admin@teste.com', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPjiuiPfu', 'ROLE_ADMIN', NOW()),

-- Usuário comum (senha: 123456) 
(UNHEX(REPLACE(UUID(), '-', '')), 'Usuário Teste', 'user@teste.com', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPjiuiPfu', 'ROLE_USER', NOW()),

-- Usuário João (senha: 123456)
(UNHEX(REPLACE(UUID(), '-', '')), 'João Silva', 'joao@teste.com', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPjiuiPfu', 'ROLE_USER', NOW());

-- Verificar usuários criados
SELECT 
    HEX(id) as id_hex,
    name,
    email,
    role,
    created_at
FROM users
ORDER BY created_at DESC;