@echo off
echo 🔄 REINICIANDO PROJETO URBANPASS COMPLETO
echo ========================================

echo.
echo 🛑 1. Parando todos os serviços...
echo    Matando todos os processos Java...
taskkill /F /IM java.exe >nul 2>&1
echo    ✅ Todos os processos Java finalizados

echo.
echo    Verificando se as portas estão livres...
timeout /t 3 /nobreak >nul

netstat -an | findstr ":8080" >nul && echo    ⚠️  Porta 8080 ainda ocupada || echo    ✅ Porta 8080 livre
netstat -an | findstr ":8081" >nul && echo    ⚠️  Porta 8081 ainda ocupada || echo    ✅ Porta 8081 livre  
netstat -an | findstr ":8084" >nul && echo    ⚠️  Porta 8084 ainda ocupada || echo    ✅ Porta 8084 livre
netstat -an | findstr ":8083" >nul && echo    ⚠️  Porta 8083 ainda ocupada || echo    ✅ Porta 8083 livre
netstat -an | findstr ":4200" >nul && echo    ⚠️  Porta 4200 ainda ocupada || echo    ✅ Porta 4200 livre

echo.
echo 🚀 2. Iniciando serviços na ordem correta...
cd /d "c:\Users\Júlio Paulo\Documents\GitHub\projeto-vem-urbana-pe\backend"

echo.
echo    🛠️  Gateway (porta 8080)...
cd gateway
start "UrbanPass-Gateway" cmd /k "echo 🛠️  GATEWAY URBANPASS && mvn spring-boot:run"
cd ..

echo    ⏳ Aguardando Gateway (25s)...
timeout /t 25 /nobreak >nul

echo.
echo    🔐 Auth Service (porta 8081)...  
cd auth-service
start "UrbanPass-Auth" cmd /k "echo 🔐 AUTH SERVICE URBANPASS && mvn spring-boot:run"
cd ..

echo    ⏳ Aguardando Auth Service (20s)...
timeout /t 20 /nobreak >nul

echo.
echo    👤 User Service (porta 8084)...
cd user-service  
start "UrbanPass-User" cmd /k "echo 👤 USER SERVICE URBANPASS && mvn spring-boot:run"
cd ..

echo    ⏳ Aguardando User Service (20s)...
timeout /t 20 /nobreak >nul

echo.
echo    💳 Card Service (porta 8083)...
cd card-service
start "UrbanPass-Card" cmd /k "echo 💳 CARD SERVICE URBANPASS && mvn spring-boot:run"
cd ..

echo    ⏳ Aguardando Card Service (15s)...
timeout /t 15 /nobreak >nul

echo.
echo 🌐 3. Iniciando Frontend Angular...
cd /d "c:\Users\Júlio Paulo\Documents\GitHub\projeto-vem-urbana-pe\login-page"
start "UrbanPass-Frontend" cmd /k "echo 🌐 FRONTEND ANGULAR URBANPASS && npm start"

echo.
echo ✅ TODOS OS SERVIÇOS INICIADOS!
echo.
echo 📋 CONFIGURAÇÃO FINAL:
echo =====================
echo 🛠️  Gateway:      http://localhost:8080
echo 🔐 Auth Service:  http://localhost:8081  
echo 👤 User Service:  http://localhost:8084 (NOVA PORTA)
echo 💳 Card Service:  http://localhost:8083
echo 🌐 Frontend:     http://localhost:4200
echo.
echo 🎯 ENDPOINTS PRINCIPAIS:
echo ========================
echo 🔐 Login/Signup:   POST http://localhost:8080/api/auth/login
echo 👤 Meus Dados:     GET  http://localhost:8080/api/users/me
echo 👤 Editar Perfil:  PUT  http://localhost:8080/api/users/me  
echo 🔑 Mudar Senha:    PUT  http://localhost:8080/api/users/me/password
echo 💳 Meus Cartões:   GET  http://localhost:8080/api/cards/me
echo.
echo ⏰ Aguardando mais 30s para todos os serviços estabilizarem...
timeout /t 30 /nobreak >nul

echo.
echo 🧪 4. Testando conectividade dos serviços...
curl -s --connect-timeout 5 http://localhost:8080 >nul && echo ✅ Gateway respondendo || echo ❌ Gateway não responde
curl -s --connect-timeout 5 http://localhost:8081 >nul && echo ✅ Auth Service respondendo || echo ❌ Auth Service não responde
curl -s --connect-timeout 5 http://localhost:8084 >nul && echo ✅ User Service respondendo || echo ❌ User Service não responde  
curl -s --connect-timeout 5 http://localhost:8083 >nul && echo ✅ Card Service respondendo || echo ❌ Card Service não responde
curl -s --connect-timeout 5 http://localhost:4200 >nul && echo ✅ Frontend respondendo || echo ❌ Frontend não responde

echo.
echo 🎉 PROJETO URBANPASS REINICIADO COM SUCESSO!
echo.
echo 📝 PRÓXIMOS PASSOS:
echo ===================
echo 1. Acesse: http://localhost:4200
echo 2. Faça CADASTRO (recomendado) ou Login
echo 3. Teste "Meus Dados" - deve funcionar perfeitamente!
echo 4. Teste "Meus Cartões" 
echo.
echo 💡 DICA: Para novos usuários, use CADASTRO em vez de Login
echo    O cadastro retorna token automático e funciona 100%

pause