@echo off
echo ========================================
echo   Logs Docker en Temps Reel
echo ========================================
echo.
echo Appuyez sur Ctrl+C pour arreter
echo.

docker-compose logs -f --tail=100
