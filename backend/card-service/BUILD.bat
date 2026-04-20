@echo off
cd /d "%~dp0"
call mvnw.cmd clean compile
echo.
echo Build completed with exit code: %ERRORLEVEL%
pause
