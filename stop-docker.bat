@echo off
echo ========================================
echo   Arret de l'Application Docker
echo ========================================
echo.

echo Arret des conteneurs...
docker-compose down

echo.
echo [OK] Tous les conteneurs sont arretes !
echo.
echo Pour supprimer aussi les donnees (volumes) :
echo   docker-compose down -v
echo.

pause
