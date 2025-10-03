@echo off
echo 🚨 RESTAURANDO FUNCIONAMENTO DO SISTEMA
echo ======================================

echo.
echo 🔄 1. Parando processos Java conflitantes...
taskkill /F /IM java.exe >nul 2>&1

echo.
echo 🚀 2. Iniciando serviços em ordem correta...

echo    🛠️  Gateway (porta 8080)...
cd /d "c:\Users\Júlio Paulo\Documents\GitHub\projeto-vem-urbana-pe\backend\gateway"
start "Gateway" cmd /k "mvn spring-boot:run"

echo    ⏳ Aguardando Gateway (20s)...
timeout /t 20 /nobreak >nul

echo    🔐 Auth Service (porta 8081)...
cd /d "c:\Users\Júlio Paulo\Documents\GitHub\projeto-vem-urbana-pe\backend\auth-service"
start "Auth" cmd /k "mvn spring-boot:run"

echo    ⏳ Aguardando Auth (15s)...
timeout /t 15 /nobreak >nul

echo    👤 User Service (porta 8084)...
cd /d "c:\Users\Júlio Paulo\Documents\GitHub\projeto-vem-urbana-pe\backend\user-service"
start "User" cmd /k "mvn spring-boot:run"

echo    ⏳ Aguardando User (15s)...
timeout /t 15 /nobreak >nul

echo    💳 Card Service (porta 8083)...
cd /d "c:\Users\Júlio Paulo\Documents\GitHub\projeto-vem-urbana-pe\backend\card-service"  
start "Card" cmd /k "mvn spring-boot:run"

echo.
echo ✅ TODOS OS SERVIÇOS INICIADOS!
echo.
echo 🎯 TESTE AGORA:
echo    1. http://localhost:4200
echo    2. Login: admin@urbanpass.com / admin123
echo    3. Cards devem aparecer
echo    4. Meus Dados devem funcionar

pause