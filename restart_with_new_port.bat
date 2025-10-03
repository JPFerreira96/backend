@echo off
echo 🚀 REINICIANDO GATEWAY E USER SERVICE COM NOVA PORTA
echo ==================================================

cd /d "c:\Users\Júlio Paulo\Documents\GitHub\projeto-vem-urbana-pe\backend"

echo.
echo 🛠️  1. Iniciando Gateway (porta 8080)...
cd gateway
start "Gateway-8080" cmd /k "mvn spring-boot:run"

echo ⏳ Aguardando Gateway inicializar (20s)...
timeout /t 20 /nobreak >nul

echo.
echo 👤 2. Iniciando User Service (NOVA PORTA 8084)...
cd ..\user-service
start "User-Service-8084" cmd /k "mvn spring-boot:run"

echo ⏳ Aguardando User Service inicializar (20s)...
timeout /t 20 /nobreak >nul

echo.
echo 🧪 3. Testando nova configuração...
echo    Gateway: http://localhost:8080
echo    User Service: http://localhost:8084

echo.
echo 🔍 Verificando se estão rodando...
curl -s --connect-timeout 5 http://localhost:8080 >nul && echo ✅ Gateway OK || echo ❌ Gateway FALHOU
curl -s --connect-timeout 5 http://localhost:8084 >nul && echo ✅ User Service OK || echo ❌ User Service FALHOU

echo.
echo 🧪 4. Testando /api/users via Gateway...
curl -s -X GET ^
  -H "Content-Type: application/json" ^
  -w "\nHTTP_CODE:%%{http_code}" ^
  http://localhost:8080/api/users > temp_test.json

if exist temp_test.json (
    findstr "HTTP_CODE:403" temp_test.json >nul
    if !errorlevel! equ 0 (
        echo ✅ SUCESSO! Endpoint retorna 403 (precisa autenticação)
        echo 🎉 PROBLEMA RESOLVIDO - User Service agora na porta 8084
    ) else (
        findstr "HTTP_CODE:404" temp_test.json >nul
        if !errorlevel! equ 0 (
            echo ❌ Ainda retorna 404 - precisa investigar mais
        ) else (
            echo ⚠️  Resposta inesperada:
            type temp_test.json
        )
    )
) else (
    echo ❌ Erro no teste
)

echo.
echo 🎯 AGORA TESTE:
echo    http://localhost:8080/api/users (deve dar 403, não 404)
echo    
echo 💡 Para fazer login e testar com autenticação:
echo    Execute: diagnose_users_endpoint.bat

if exist temp_test.json del temp_test.json

pause