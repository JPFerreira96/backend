@echo off
echo ==================================================
echo    Iniciando todos os servicos do backend
echo ==================================================

echo.
echo Iniciando Gateway (porta 8080)...
start "Gateway" cmd /c "cd gateway && mvn spring-boot:run"

timeout /t 5 /nobreak > nul

echo.
echo Iniciando Auth Service (porta 8081)...
start "Auth Service" cmd /c "cd auth-service && mvn spring-boot:run"

timeout /t 5 /nobreak > nul

echo.
echo Iniciando User Service (porta 8084)...
start "User Service" cmd /c "cd user-service && mvn spring-boot:run"

timeout /t 5 /nobreak > nul

echo.
echo Iniciando Card Service (porta 8083)...
start "Card Service" cmd /c "cd card-service && mvn spring-boot:run"

echo.
echo ==================================================
echo    Todos os servicos foram iniciados!
echo    
echo    Gateway:      http://localhost:8080
echo    Auth Service: http://localhost:8081
echo    User Service: http://localhost:8084
echo    Card Service: http://localhost:8083
echo    
echo    Swagger UIs disponiveis em:
echo    - http://localhost:8081/swagger
echo    - http://localhost:8084/swagger
echo    - http://localhost:8083/swagger
echo ==================================================

pause