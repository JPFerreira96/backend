# Bus Card Management

## Descrição do Projeto

O projeto "Bus Card Management" é uma aplicação web que permite o cadastro e a gestão de usuários e cartões de ônibus associados a cada usuário. A aplicação é construída utilizando uma arquitetura de microserviços, onde o backend é implementado em Java 17 com Spring Boot e o frontend é desenvolvido em Angular.

## Estrutura do Projeto

O projeto é dividido em duas partes principais: **backend** e **frontend**.

### Backend

O backend é composto por três microserviços:

1. **User Service**: Gerencia as operações relacionadas a usuários.
   - CRUD completo de usuários.
   - Autenticação e autorização de usuários.
   - Implementação de segurança com JWT.

2. **Card Service**: Gerencia as operações relacionadas a cartões de ônibus.
   - CRUD completo de cartões.
   - Associação de cartões a usuários.

3. **Gateway**: Serve como um ponto de entrada para as requisições, roteando-as para os serviços apropriados.

### Frontend

O frontend é uma aplicação Angular que fornece uma interface de usuário para interagir com os microserviços do backend. As principais funcionalidades incluem:

- Tela de login para autenticação de usuários.
- Listagem e gerenciamento de usuários e cartões, dependendo do perfil do usuário (administrador ou comum).

## Tecnologias Utilizadas

- **Backend**: Java 17, Spring Boot, Maven, JPA, JWT.
- **Frontend**: Angular, TypeScript, Bootstrap.

## Como Executar o Projeto

### Backend

1. Navegue até o diretório do serviço desejado (user-service ou card-service).
2. Execute o comando Maven para compilar e iniciar o serviço:
   ```
   mvn spring-boot:run
   ```
3. O serviço estará disponível em `http://localhost:8080`.

### Frontend

1. Navegue até o diretório do frontend.
2. Instale as dependências do projeto:
   ```
   npm install
   ```
3. Inicie a aplicação Angular:
   ```
   ng serve
   ```
4. A aplicação estará disponível em `http://localhost:4200`.

## Contribuição

Contribuições são bem-vindas! Sinta-se à vontade para abrir issues ou pull requests.

## Licença

Este projeto está licenciado sob a MIT License. Veja o arquivo LICENSE para mais detalhes.