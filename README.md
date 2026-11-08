# Site Krypton — FRC #9487

Museu digital da equipe Krypton. O site fica em `museu-krypton/museu/` (HTML, CSS e JS puros, sem build).

## Ver o site no seu computador

Pelo terminal, na pasta do projeto:

```bash
./iniciar-site.sh      # Linux / macOS
iniciar-site.bat       # Windows (duplo clique ou pelo terminal)
npm start              # alternativa, se tiver Node.js
```

O navegador abre em http://localhost:8000.

## Publicar no GitHub (link público)

1. Envie o projeto para um repositório no GitHub (branch `main`):
   ```bash
   git init && git add . && git commit -m "Site Krypton"
   git branch -M main
   git remote add origin https://github.com/SEU-USUARIO/SEU-REPO.git
   git push -u origin main
   ```
2. No GitHub: **Settings → Pages → Build and deployment → Source: GitHub Actions**.
3. Pronto: a cada `git push` o workflow `.github/workflows/pages.yml` publica o site em
   `https://SEU-USUARIO.github.io/SEU-REPO/`.

> Alternativa sem Actions: Source = "Deploy from a branch" (`main`, pasta `/root`). O `index.html` da raiz redireciona para o museu.
