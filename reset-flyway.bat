@echo off
echo ========================================
echo RESETANDO FLYWAY PARA TODOS OS SERVICOS
echo ========================================

echo.
echo 1. Limpando card-service...
cd card-service
mvn flyway:clean -q
if %ERRORLEVEL% NEQ 0 (
    echo ERRO ao limpar card-service
    pause
    exit /b 1
)
cd ..

echo.
echo 2. Limpando user-service...
cd user-service
mvn flyway:clean -q
if %ERRORLEVEL% NEQ 0 (
    echo ERRO ao limpar user-service
    pause
    exit /b 1
)
cd ..

echo.
echo ========================================
echo RESET CONCLUIDO COM SUCESSO!
echo ========================================
echo Agora voce pode executar os servicos normalmente.
echo.
pause