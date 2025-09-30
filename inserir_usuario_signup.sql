-- Script para inserir o usuário do signup no banco de dados
-- Execute no MySQL Workbench ou phpMyAdmin

USE db_card_user;

-- Inserir o usuário "juliojpf21@gmail.com" que fez signup
INSERT INTO users (id, name, email, password_hash, role, created_at) VALUES 
(UNHEX(REPLACE(UUID(), '-', '')), 'Julio Paulo', 'juliojpf21@gmail.com', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPjiuiPfu', 'ROLE_USER', NOW());

-- Verificar se foi inserido
SELECT 
    HEX(id) as id_hex,
    name,
    email,
    role,
    created_at
FROM users
WHERE email = 'juliojpf21@gmail.com';