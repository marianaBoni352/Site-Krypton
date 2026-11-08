@echo off
rem Compila e abre o museu da equipe Krypton FRC #9487.
cd /d "%~dp0"
if not exist out mkdir out
javac -encoding UTF-8 --release 17 -d out src\krypton\*.java
if errorlevel 1 (
    echo.
    echo Nao foi possivel compilar. Verifique se o JDK 17 ou mais novo esta instalado.
    pause
    exit /b 1
)
java -cp out krypton.Main
