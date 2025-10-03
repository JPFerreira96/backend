@echo off
echo 🔍 DIAGNÓSTICO COMPLETO DO ENDPOINT /api/users
echo ===============================================

echo.
echo 🔍 1. Verificando se Gateway está rodando (porta 8080)...
curl -s --connect-timeout 5 http://localhost:8080 >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Gateway rodando
) else (
    echo ❌ Gateway NÃO está rodando
    goto :end
)

echo.
echo 🔍 2. Verificando se User Service está rodando (porta 8084)...
curl -s --connect-timeout 5 http://localhost:8084 >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ User Service rodando
) else (
    echo ❌ User Service NÃO está rodando na porta 8084
    echo 💡 EXECUTE: cd user-service && mvn spring-boot:run
    goto :end
)

echo.
echo 🧪 3. Testando DIRETO no User Service (sem Gateway)...
curl -s -X GET ^
  -H "Content-Type: application/json" ^
  -w "\nHTTP_CODE:%%{http_code}" ^
  http://localhost:8084/api/users > temp_direct_test.json

if exist temp_direct_test.json (
    findstr "HTTP_CODE:" temp_direct_test.json
    echo 📄 Resposta direta do User Service:
    type temp_direct_test.json | findstr /v "HTTP_CODE:"
    echo.
) else (
    echo ❌ Erro ao testar diretamente
)

echo.
echo 🧪 4. Testando VIA Gateway (sem autenticação)...
curl -s -X GET ^
  -H "Content-Type: application/json" ^
  -w "\nHTTP_CODE:%%{http_code}" ^
  http://localhost:8080/api/users > temp_gateway_test.json

if exist temp_gateway_test.json (
    findstr "HTTP_CODE:" temp_gateway_test.json
    echo 📄 Resposta via Gateway:
    type temp_gateway_test.json | findstr /v "HTTP_CODE:"
    echo.
) else (
    echo ❌ Erro ao testar via Gateway
)

echo.
echo 🔑 5. Fazendo login e testando COM autenticação...
curl -s -X POST ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"admin@urbanpass.com\",\"password\":\"admin123\"}" ^
  http://localhost:8080/api/auth/login > temp_login.json

if exist temp_login.json (
    findstr "token" temp_login.json >nul
    if !errorlevel! equ 0 (
        echo ✅ Login realizado
        
        REM Extrair token de forma simples
        for /f "tokens=*" %%a in ('findstr "token" temp_login.json') do (
            set "LINE=%%a"
        )
        
        REM Pegar uma parte do token para teste
        for /f "tokens=2 delims=:" %%b in ("!LINE!") do (
            set "TOKEN_RAW=%%b"
        )
        
        REM Remover aspas e vírgulas
        set "TOKEN=!TOKEN_RAW:"=!"
        set "TOKEN=!TOKEN:,=!"
        set "TOKEN=!TOKEN: =!"
        
        echo 🧪 6. Testando /api/users COM token JWT...
        curl -s -X GET ^
          -H "Content-Type: application/json" ^
          -H "Authorization: Bearer !TOKEN!" ^
          -w "\nHTTP_CODE:%%{http_code}" ^
          http://localhost:8080/api/users > temp_auth_test.json
          
        if exist temp_auth_test.json (
            findstr "HTTP_CODE:" temp_auth_test.json
            echo 📄 Resposta com autenticação:
            type temp_auth_test.json | findstr /v "HTTP_CODE:"
        )
    ) else (
        echo ❌ Falha no login
        type temp_login.json
    )
)

echo.
echo 📋 RESULTADO DO DIAGNÓSTICO:
echo ============================
if exist temp_auth_test.json (
    findstr "HTTP_CODE:200" temp_auth_test.json >nul
    if !errorlevel! equ 0 (
        echo ✅ STATUS: ENDPOINT /api/users FUNCIONANDO PERFEITAMENTE!
        echo 🎯 Acesse: http://localhost:8080/api/users
        echo 🔑 Certifique-se de incluir o header Authorization: Bearer {token}
    ) else (
        findstr "HTTP_CODE:401" temp_auth_test.json >nul
        if !errorlevel! equ 0 (
            echo ⚠️  STATUS: ENDPOINT EXISTE MAS REQUER AUTENTICAÇÃO
            echo 💡 Faça login primeiro e use o token JWT
        ) else (
            findstr "HTTP_CODE:404" temp_auth_test.json >nul
            if !errorlevel! equ 0 (
                echo ❌ STATUS: ENDPOINT RETORNA 404 - PROBLEMA NO ROTEAMENTO
                echo 💡 Verifique se o Gateway está roteando /api/users/** para porta 8084
            ) else (
                echo ❌ STATUS: ERRO DESCONHECIDO
                echo 📄 Verifique os logs acima
            )
        )
    )
) else (
    echo ❌ STATUS: FALHA CRÍTICA - SERVIÇOS NÃO ESTÃO FUNCIONANDO
)

:end
echo.
echo 🧹 Limpando arquivos temporários...
if exist temp_direct_test.json del temp_direct_test.json
if exist temp_gateway_test.json del temp_gateway_test.json
if exist temp_login.json del temp_login.json
if exist temp_auth_test.json del temp_auth_test.json

echo.
pause