#!/bin/bash

# Script para testar os endpoints da API de usuários do UrbanPass
# Execute este script para verificar se os endpoints estão funcionando

API_BASE="http://localhost:8081/api/users"
AUTH_BASE="http://localhost:8081/auth"

echo "🚀 Testando API do UrbanPass - Módulo de Usuários"
echo "=================================================="

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Função para fazer login e obter token
get_auth_token() {
    echo -e "${YELLOW}1. Fazendo login para obter token...${NC}"
    
    LOGIN_RESPONSE=$(curl -s -X POST \
        -H "Content-Type: application/json" \
        -d '{
            "email": "admin@urbanpass.com",
            "password": "admin123"
        }' \
        "${AUTH_BASE}/login")
    
    TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.token')
    
    if [ "$TOKEN" != "null" ] && [ "$TOKEN" != "" ]; then
        echo -e "${GREEN}✅ Login realizado com sucesso${NC}"
        echo "Token: ${TOKEN:0:20}..."
        return 0
    else
        echo -e "${RED}❌ Falha no login${NC}"
        echo "Response: $LOGIN_RESPONSE"
        return 1
    fi
}

# Função para testar endpoint /me
test_me_endpoint() {
    echo -e "\n${YELLOW}2. Testando endpoint /me...${NC}"
    
    RESPONSE=$(curl -s -X GET \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        "${API_BASE}/me")
    
    if echo $RESPONSE | jq -e '.id' > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Endpoint /me funcionando${NC}"
        echo "User ID: $(echo $RESPONSE | jq -r '.id')"
        echo "Nome: $(echo $RESPONSE | jq -r '.name')"
        echo "Email: $(echo $RESPONSE | jq -r '.email')"
        USER_ID=$(echo $RESPONSE | jq -r '.id')
        return 0
    else
        echo -e "${RED}❌ Endpoint /me com problemas${NC}"
        echo "Response: $RESPONSE"
        return 1
    fi
}

# Função para testar atualização de usuário
test_update_user() {
    echo -e "\n${YELLOW}3. Testando atualização de usuário...${NC}"
    
    RESPONSE=$(curl -s -X PUT \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d '{
            "email": "admin.updated@urbanpass.com"
        }' \
        "${API_BASE}/${USER_ID}")
    
    if echo $RESPONSE | jq -e '.email' > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Atualização de usuário funcionando${NC}"
        echo "Email atualizado: $(echo $RESPONSE | jq -r '.email')"
        
        # Reverter mudança
        curl -s -X PUT \
            -H "Authorization: Bearer $TOKEN" \
            -H "Content-Type: application/json" \
            -d '{
                "email": "admin@urbanpass.com"
            }' \
            "${API_BASE}/${USER_ID}" > /dev/null
        
        return 0
    else
        echo -e "${RED}❌ Atualização de usuário com problemas${NC}"
        echo "Response: $RESPONSE"
        return 1
    fi
}

# Função para testar alteração de senha
test_change_password() {
    echo -e "\n${YELLOW}4. Testando alteração de senha...${NC}"
    
    RESPONSE=$(curl -s -w "%{http_code}" -X PUT \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d '{
            "currentPassword": "admin123",
            "newPassword": "newPassword123"
        }' \
        "${API_BASE}/${USER_ID}/password")
    
    HTTP_CODE="${RESPONSE: -3}"
    
    if [ "$HTTP_CODE" == "204" ]; then
        echo -e "${GREEN}✅ Alteração de senha funcionando${NC}"
        
        # Reverter senha
        curl -s -X PUT \
            -H "Authorization: Bearer $TOKEN" \
            -H "Content-Type: application/json" \
            -d '{
                "currentPassword": "newPassword123",
                "newPassword": "admin123"
            }' \
            "${API_BASE}/${USER_ID}/password" > /dev/null
        
        return 0
    else
        echo -e "${RED}❌ Alteração de senha com problemas${NC}"
        echo "HTTP Code: $HTTP_CODE"
        echo "Response: ${RESPONSE%???}"
        return 1
    fi
}

# Executar testes
main() {
    if get_auth_token; then
        test_me_endpoint
        
        if [ $? -eq 0 ]; then
            test_update_user
            test_change_password
        fi
        
        echo -e "\n${GREEN}🎉 Testes concluídos!${NC}"
        echo -e "${YELLOW}💡 Agora você pode testar o frontend em http://localhost:4200/dashboard/profile${NC}"
    else
        echo -e "\n${RED}❌ Não foi possível executar os testes devido à falha na autenticação${NC}"
        echo -e "${YELLOW}💡 Verifique se o backend está rodando e se existem usuários cadastrados${NC}"
    fi
}

# Verificar se jq está instalado
if ! command -v jq &> /dev/null; then
    echo -e "${RED}❌ jq não está instalado. Instale com: sudo apt-get install jq${NC}"
    exit 1
fi

main