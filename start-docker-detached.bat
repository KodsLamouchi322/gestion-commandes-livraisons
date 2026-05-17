@echo off
echo ========================================
echo   Demarrage Docker en Arriere-Plan
echo ========================================
echo.

REM Verifier si Docker est en cours d'execution
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERREUR] Docker n'est pas en cours d'execution !
    echo Veuillez demarrer Docker Desktop et reessayer.
    pause
    exit /b 1
)

echo [OK] Docker est en cours d'execution
echo.

echo Demarrage des conteneurs en arriere-plan...
docker-compose up -d --build

echo.
echo Attente du demarrage des services...
timeout /t 5 /nobreak >nul

echo.
echo ========================================
echo   Status des Conteneurs
echo ========================================
docker-compose ps

echo.
echo ========================================
echo   Application Prete !
echo ========================================
echo.
echo Frontend : http://localhost:8080
echo Backend  : http://localhost:8085
echo Swagger  : http://localhost:8085/swagger-ui/index.html
echo.
echo Compte admin : admin@gestion.com / admin123
echo.
echo Pour voir les logs : docker-compose logs -f
echo Pour arreter       : docker-compose down
echo.

pause
