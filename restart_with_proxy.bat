@echo off
echo ========================================
echo  RESTART COM PROXY CONTROLLER
echo ========================================

cd /d "c:\Users\Júlio Paulo\Documents\GitHub\projeto-vem-urbana-pe\backend"

echo.
echo 1. Parando todos os serviços...
echo ========================================

:: Parar todos os processos Java que rodam os serviços
for /f "tokens=2" %%i in ('tasklist ^| findstr "java.exe"') do (
    echo Matando processo Java: %%i
    taskkill /PID %%i /F 2>nul
)

echo.
echo 2. Recompilando Gateway com ProxyController...
echo ========================================
cd gateway
call mvn clean compile -q
if %ERRORLEVEL% NEQ 0 (
    echo ❌ ERRO na compilação do Gateway!
    pause
    exit /b 1
)
echo ✅ Gateway compilado com sucesso!

echo.
echo 3. Recompilando outros serviços...
echo ========================================

cd ..\user-service
call mvn clean compile -q
if %ERRORLEVEL% NEQ 0 (
    echo ❌ ERRO na compilação do User Service!
    pause
    exit /b 1
)
echo ✅ User Service compilado!

cd ..\auth-service
call mvn clean compile -q
if %ERRORLEVEL% NEQ 0 (
    echo ❌ ERRO na compilação do Auth Service!
    pause
    exit /b 1
)
echo ✅ Auth Service compilado!

echo.
echo 4. Iniciando serviços na ordem correta...
echo ========================================

:: Iniciar Gateway (porta 8080)
cd ..\gateway
echo 🚀 Iniciando Gateway (porta 8080)...
start "Gateway-8080" cmd /k "mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8080"
timeout /t 5 /nobreak >nul

:: Iniciar Auth Service (porta 8081)
cd ..\auth-service
echo 🚀 Iniciando Auth Service (porta 8081)...
start "Auth-Service-8081" cmd /k "mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081"
timeout /t 5 /nobreak >nul

:: Iniciar User Service (porta 8084)
cd ..\user-service
echo 🚀 Iniciando User Service (porta 8084)...
start "User-Service-8084" cmd /k "mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8084"
timeout /t 5 /nobreak >nul

echo.
echo ========================================
echo ✅ TODOS OS SERVIÇOS INICIADOS!
echo ========================================
echo.
echo 🌐 Gateway:      http://localhost:8080
echo 🔐 Auth Service: http://localhost:8081
echo 👤 User Service: http://localhost:8084
echo.
echo 🎯 NOVA FUNCIONALIDADE:
echo    - UserProxyController no Gateway
echo    - Rotas diretas: /api/users/me, /api/users/me/password
echo    - Proxy transparente para User Service
echo.
echo Aguardando inicialização completa...
timeout /t 15 /nobreak >nul

echo.
echo 🧪 TESTE RÁPIDO - GET /api/users/me:
curl -X GET "http://localhost:8080/api/users/me" -H "Content-Type: application/json"

echo.
echo.
echo ========================================
echo Sistema pronto para uso!
echo ========================================
pause