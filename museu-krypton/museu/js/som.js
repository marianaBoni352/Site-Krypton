
"use strict";

/*
 * Som da abertura: começa sozinho quando o foguete decola, segue durante o voo e
 * continua até depois de a escrita FRC ROBOTICS TEAM aparecer, quando desvanece.
 * Também cria o botão "Pular introdução" (canto inferior direito): ao apertá-lo a
 * abertura termina na hora e o som não toca.
 * Não altera nenhum outro código: só observa as classes que o app.js e o
 * decolagem.js já colocam na página.
 */
(function () {
  const corpo = document.body;
  const carregador = document.querySelector(".carregador");
  const ARQUIVO = "assets/som/foguete.mp3";
  /** Segundos, depois do fim do voo, em que FRC ROBOTICS TEAM surge (ver estilo.css). */
  const APARECE_FRC = 2.4;
  /** Segundos extras de som depois de FRC ROBOTICS TEAM aparecer, antes de desvanecer. */
  const SEGURAR = 1.5;
  const DURACAO_DESVANECER = 800;
  const VOLUME = 0.8;
  /** O arquivo tem uns 0,5 s de silêncio no começo: pulamos esse trecho para o som entrar junto com a decolagem. */
  const INICIO_SOM = 0.45;

  const som = new Audio(ARQUIVO);
  som.preload = "auto";
  som.volume = VOLUME;

  let encerrado = false;
  let agendado = false;
  let iniciado = false;

  function tocar() {
    if (encerrado || iniciado) {
      return;
    }
    iniciado = true;
    try {
      som.currentTime = INICIO_SOM;
    } catch (erro) {
      // Se o navegador não deixar pular, o som toca desde o começo.
    }
    const promessa = som.play();
    if (promessa) {
      promessa.catch(() => {});
    }
  }

  function parar() {
    if (encerrado) {
      return;
    }
    encerrado = true;
    const comeco = performance.now();
    const passo = () => {
      const p = Math.min((performance.now() - comeco) / DURACAO_DESVANECER, 1);
      som.volume = VOLUME * (1 - p);
      if (p < 1) {
        setTimeout(passo, 30);
      } else {
        som.pause();
      }
    };
    passo();
  }

  function verCorpo() {
    if (corpo.classList.contains("intro-visto")) {
      // A pessoa pulou a abertura (ou ela terminou): o som para.
      parar();
    } else if (corpo.classList.contains("pronto") && !agendado) {
      agendado = true;
      const emInicio = corpo.dataset.pagina === "inicio";
      setTimeout(parar, emInicio ? (APARECE_FRC + SEGURAR) * 1000 : 0);
    }
  }

  function verCarregador() {
    if (carregador && carregador.classList.contains("decolando")) {
      tocar();
    }
  }

  // ---- Botão "Pular introdução" ------------------------------------------------

  const estilo = document.createElement("style");
  estilo.textContent = `
    .pular-intro {
      position: fixed;
      right: max(16px, env(safe-area-inset-right, 0px));
      bottom: max(16px, env(safe-area-inset-bottom, 0px));
      z-index: 200;
      padding: 9px 20px;
      border: 1px solid var(--borda, rgba(77, 255, 124, 0.27));
      border-radius: 4px;
      background: rgba(4, 14, 7, 0.8);
      color: var(--branco, #f4fff6);
      font-size: 15px;
      font-weight: 700;
      letter-spacing: 2px;
      text-transform: uppercase;
      cursor: pointer;
      transition: background 0.2s, color 0.2s, border-color 0.2s;
    }
    .pular-intro:hover,
    .pular-intro:focus-visible {
      background: var(--verde, #4dff7c);
      border-color: var(--verde, #4dff7c);
      color: rgb(4, 16, 8);
    }
    body.intro-visto .pular-intro {
      display: none;
    }
  `;
  document.head.appendChild(estilo);

  /** Termina a abertura na hora e deixa tudo sem som. */
  function pular() {
    encerrado = true;
    som.pause();
    corpo.classList.remove("carregando");
    corpo.classList.add("pronto", "intro-visto");
  }

  if (!corpo.classList.contains("intro-visto")) {
    const botao = document.createElement("button");
    botao.type = "button";
    botao.className = "pular-intro";
    botao.textContent = "Pular introdução";
    // No primeiro toque (o clique do site esconderia o botão antes do "click") e no teclado.
    botao.addEventListener("pointerdown", pular);
    botao.addEventListener("click", pular);
    corpo.appendChild(botao);
  }

  new MutationObserver(verCorpo).observe(corpo, { attributes: true, attributeFilter: ["class"] });
  if (carregador) {
    new MutationObserver(verCarregador).observe(carregador, { attributes: true, attributeFilter: ["class"] });
  }
  verCarregador();
  verCorpo();
})();
