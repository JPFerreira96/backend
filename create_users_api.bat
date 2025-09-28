# Comandos para inserir usuários via API REST
# Execute estes comandos no seu terminal quando o user-service estiver rodando

# 1. Criar usuário administrador via signup
curl -X POST http://localhost:8084/auth/signup ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"João Silva\",\"email\":\"joao.silva@email.com\",\"password\":\"123456\",\"role\":\"ADMIN\"}"

# 2. Criar usuário comum 1
curl -X POST http://localhost:8084/auth/signup ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Maria Santos\",\"email\":\"maria.santos@email.com\",\"password\":\"123456\",\"role\":\"USER\"}"

# 3. Criar usuário comum 2
curl -X POST http://localhost:8084/auth/signup ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Pedro Oliveira\",\"email\":\"pedro.oliveira@email.com\",\"password\":\"123456\"}"

# 4. Criar usuário comum 3
curl -X POST http://localhost:8084/auth/signup ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Ana Costa\",\"email\":\"ana.costa@email.com\",\"password\":\"123456\"}"

# 5. Criar usuário comum 4
curl -X POST http://localhost:8084/auth/signup ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Carlos Ferreira\",\"email\":\"carlos.ferreira@email.com\",\"password\":\"123456\"}"

# 6. Listar usuários criados
curl -X GET http://localhost:8084/users ^
  -H "Content-Type: application/json"

# 7. Fazer login como administrador
curl -X POST http://localhost:8084/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"joao.silva@email.com\",\"password\":\"123456\"}"