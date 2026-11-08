#!/usr/bin/env bash
# Sobe o site localmente e abre no navegador: ./iniciar-site.sh
cd "$(dirname "$0")/museu-krypton/museu" || exit 1
PORTA=${PORTA:-8000}
URL="http://localhost:$PORTA"
abrir() { (sleep 1; xdg-open "$URL" 2>/dev/null || open "$URL" 2>/dev/null) & }
echo "Site do museu Krypton em $URL  (Ctrl+C para parar)"
if command -v python3 >/dev/null; then abrir; python3 -m http.server "$PORTA"
elif command -v python >/dev/null; then abrir; python -m http.server "$PORTA"
elif command -v npx >/dev/null; then abrir; npx --yes serve -l "$PORTA" .
else echo "Instale Python ou Node.js."; exit 1; fi
