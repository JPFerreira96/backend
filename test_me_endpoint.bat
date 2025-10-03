@echo off
echo 🧪 TESTANDO ENDPOINTS /me DO USER SERVICE
echo ========================================

echo.
echo 🔍 1. Verificando se User Service está rodando (porta 8084)...
curl -s --connect-timeout 5 http://localhost:8084 >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ User Service rodando na porta 8084
) else (
    echo ❌ User Service NÃO está rodando na porta 8084
    echo 💡 Inicie o User Service primeiro: cd user-service && mvn spring-boot:run
    pause
    exit /b 1
)

echo.
echo 🧪 2. Testando /api/users/me sem token (deve retornar 401)...
curl -s -X GET ^
  -H "Content-Type: application/json" ^
  -w "\nHTTP_CODE:%%{http_code}" ^
  http://localhost:8080/api/users/me > temp_me_test.json

if exist temp_me_test.json (
    findstr "HTTP_CODE:401" temp_me_test.json >nul
    if !errorlevel! equ 0 (
        echo ✅ Endpoint protegido corretamente (401 Unauthorized)
    ) else (
        echo ⚠️  Resposta inesperada:
        type temp_me_test.json
    )
) else (
    echo ❌ Erro ao testar endpoint
)

echo.
echo 🔑 3. Fazendo login para obter token...
curl -s -X POST ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"admin@urbanpass.com\",\"password\":\"admin123\"}" ^
  http://localhost:8080/api/auth/login > temp_login.json

if exist temp_login.json (
    findstr "token" temp_login.json >nul
    if !errorlevel! equ 0 (
        echo ✅ Login realizado com sucesso
        
        REM Extrair token (método simples para Windows)
        for /f "tokens=2 delims=:," %%a in ('findstr "token" temp_login.json') do (
            set "TOKEN=%%a"
            set "TOKEN=!TOKEN:"=!"
            set "TOKEN=!TOKEN: =!"
        )
        
        echo 🧪 4. Testando /api/users/me com token...
        curl -s -X GET ^
          -H "Content-Type: application/json" ^
          -H "Authorization: Bearer !TOKEN!" ^
          -w "\nHTTP_CODE:%%{http_code}" ^
          http://localhost:8080/api/users/me > temp_me_auth.json
          
        if exist temp_me_auth.json (
            findstr "HTTP_CODE:200" temp_me_auth.json >nul
            if !errorlevel! equ 0 (
                echo ✅ Endpoint /me funcionando perfeitamente!
                echo 📄 Dados do usuário:
                type temp_me_auth.json | findstr /v "HTTP_CODE:"
            ) else (
                echo ❌ Erro no endpoint /me:
                type temp_me_auth.json
            )
        )
        
    ) else (
        echo ❌ Falha no login:
        type temp_login.json
    )
) else (
    echo ❌ Erro na requisição de login
)

echo.
echo 🧹 Limpando arquivos temporários...
if exist temp_me_test.json del temp_me_test.json
if exist temp_login.json del temp_login.json  
if exist temp_me_auth.json del temp_me_auth.json

echo.
echo 🎯 Se tudo funcionou, acesse:
echo    http://localhost:4200/dashboard/profile
echo    E teste a edição do email!

pause