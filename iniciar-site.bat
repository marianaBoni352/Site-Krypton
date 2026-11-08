@echo off
rem Sobe o site localmente e abre no navegador.
cd /d "%~dp0museu-krypton\museu"
set PORTA=8000
where python >nul 2>nul
if %errorlevel%==0 ( start "" http://localhost:%PORTA% & python -m http.server %PORTA% & goto :fim )
where py >nul 2>nul
if %errorlevel%==0 ( start "" http://localhost:%PORTA% & py -m http.server %PORTA% & goto :fim )
where npx >nul 2>nul
if %errorlevel%==0 ( start "" http://localhost:%PORTA% & npx --yes serve -l %PORTA% . & goto :fim )
echo Python/Node nao encontrados. Abrindo o arquivo direto no navegador...
start "" index.html
:fim
