-- Script para inserir usuários de exemplo no banco db_card_user
-- Execute este script no seu cliente MySQL (como MySQL Workbench, phpMyAdmin, etc.)

USE db_card_user;

-- Inserir usuários de exemplo
-- Nota: As senhas estão com hash BCrypt para "123456"
-- Hash gerado: $2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPjiuiPfu

-- Usuário Administrador
INSERT INTO users (id, name, email, password_hash, role, created_at) VALUES 
(UNHEX(REPLACE(UUID(), '-', '')), 'João Silva', 'joao.silva@email.com', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPjiuiPfu', 'ROLE_ADMIN', NOW());

-- Usuário comum 1
INSERT INTO users (id, name, email, password_hash, role, created_at) VALUES 
(UNHEX(REPLACE(UUID(), '-', '')), 'Maria Santos', 'maria.santos@email.com', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPjiuiPfu', 'ROLE_USER', NOW());

-- Usuário comum 2
INSERT INTO users (id, name, email, password_hash, role, created_at) VALUES 
(UNHEX(REPLACE(UUID(), '-', '')), 'Pedro Oliveira', 'pedro.oliveira@email.com', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPjiuiPfu', 'ROLE_USER', NOW());

-- Usuário comum 3
INSERT INTO users (id, name, email, password_hash, role, created_at) VALUES 
(UNHEX(REPLACE(UUID(), '-', '')), 'Ana Costa', 'ana.costa@email.com', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPjiuiPfu', 'ROLE_USER', NOW());

-- Usuário comum 4
INSERT INTO users (id, name, email, password_hash, role, created_at) VALUES 
(UNHEX(REPLACE(UUID(), '-', '')), 'Carlos Ferreira', 'carlos.ferreira@email.com', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPjiuiPfu', 'ROLE_USER', NOW());

-- Verificar os usuários inseridos
SELECT 
    HEX(id) as id_hex,
    name,
    email,
    role,
    created_at
FROM users
ORDER BY created_at DESC;