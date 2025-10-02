# Backend - User Card CRUD Application

Aplicação backend para gerenciamento de usuários e cartões, desenvolvida em Java com Spring Boot.

## 🏗️ Arquitetura

Microserviços usando Spring Boot:
- **auth-service** (8081) - Autenticação e autorização
- **user-service** (8084) - Gerenciamento de usuários  
- **card-service** (8083) - Gerenciamento de cartões
- **gateway** (8080) - API Gateway

## 🚀 Como executar

### Pré-requisitos
- Java 21+
- Maven 3.6+
- MySQL 5.7+ rodando na porta 3306

### Configuração do Banco de Dados
```sql
CREATE DATABASE db_card_user;
CREATE DATABASE db_cards;
```

### Executando os serviços

1. **Inicie cada serviço individualmente:**
```bash
# Auth Service
cd auth-service && mvn spring-boot:run

# User Service  
cd user-service && mvn spring-boot:run

# Card Service
cd card-service && mvn spring-boot:run

# Gateway
cd gateway && mvn spring-boot:run
```

### 🔧 Solução de Problemas

#### Erro de Migração Flyway
Se encontrar erros do tipo "Migrations have failed validation", execute:

```bash
# Para card-service
cd card-service
mvn flyway:clean

# Para user-service  
cd user-service
mvn flyway:clean
```

#### Erro de Conexão com Banco
Verifique se:
- MySQL está rodando na porta 3306
- Usuário `root` sem senha está configurado
- Databases `db_card_user` e `db_cards` existem

## 🧪 Testes

```bash
# Executar testes de um serviço específico
cd user-service && mvn test

# Executar todos os testes
mvn test
```

## 📝 Endpoints Principais

- **Gateway**: http://localhost:8080
- **Auth**: http://localhost:8081/swagger
- **Users**: http://localhost:8084/swagger  
- **Cards**: http://localhost:8083/swagger

## 🔐 Variáveis de Ambiente

Crie um arquivo `.env` na raiz se necessário:
```env
JWT_SECRET_BASE64=your_jwt_secret_here
INTERNAL_API_SECRET=your_internal_secret_here
```