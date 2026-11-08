@echo off
rem Sobe o site localmente e abre no navegador (usa Java, Python ou Node, o que estiver instalado).
cd /d "%~dp0museu-krypton\museu"
set PORTA=8000

jwebserver --version >nul 2>nul
if %errorlevel%==0 (
    echo Site Krypton em http://localhost:%PORTA%  ^(Ctrl+C para parar^)
    start "" http://localhost:%PORTA%
    jwebserver -p %PORTA% -d "%CD%"
    goto :fim
)
python --version >nul 2>nul
if %errorlevel%==0 (
    start "" http://localhost:%PORTA%
    python -m http.server %PORTA%
    goto :fim
)
py --version >nul 2>nul
if %errorlevel%==0 (
    start "" http://localhost:%PORTA%
    py -m http.server %PORTA%
    goto :fim
)
call npx --version >nul 2>nul
if %errorlevel%==0 (
    start "" http://localhost:%PORTA%
    call npx --yes serve -l %PORTA% .
    goto :fim
)
echo Nenhum servidor encontrado. Abrindo o arquivo direto no navegador...
start "" index.html
:fim