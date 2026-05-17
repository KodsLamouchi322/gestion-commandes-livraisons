@echo off
echo ========================================
echo   Demarrage de l'Application Docker
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

REM Verifier si les ports sont libres
echo Verification des ports...
netstat -ano | findstr ":8080" >nul 2>&1
if %errorlevel% equ 0 (
    echo [ATTENTION] Le port 8080 est deja utilise !
    echo Voulez-vous continuer quand meme ? (O/N)
    set /p continue=
    if /i not "%continue%"=="O" exit /b 1
)

netstat -ano | findstr ":8085" >nul 2>&1
if %errorlevel% equ 0 (
    echo [ATTENTION] Le port 8085 est deja utilise !
    echo Voulez-vous continuer quand meme ? (O/N)
    set /p continue=
    if /i not "%continue%"=="O" exit /b 1
)

echo [OK] Ports disponibles
echo.

REM Demander si on veut rebuild
echo Voulez-vous reconstruire les images ? (O/N)
echo (Choisir O si c'est la premiere fois ou si vous avez modifie le code)
set /p rebuild=
echo.

if /i "%rebuild%"=="O" (
    echo Demarrage avec reconstruction des images...
    docker-compose up --build
) else (
    echo Demarrage sans reconstruction...
    docker-compose up
)

pause
