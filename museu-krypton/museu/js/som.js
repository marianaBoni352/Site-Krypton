
"use strict";

/*
 * Som da abertura: começa sozinho quando o foguete decola, segue durante o voo e
 * continua até depois de a escrita FRC ROBOTICS TEAM aparecer, quando desvanece.
 * Se a pessoa pular a abertura (clique ou tecla), o som para.
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

  new MutationObserver(verCorpo).observe(corpo, { attributes: true, attributeFilter: ["class"] });
  if (carregador) {
    new MutationObserver(verCarregador).observe(carregador, { attributes: true, attributeFilter: ["class"] });
  }
  verCarregador();
  verCorpo();
})();