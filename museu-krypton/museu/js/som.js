"use strict";

/*
 * Som da abertura: toca com o foguete carregando e voando e para (com um
 * desvanecer curto) quando as escritas aparecem. Não altera nenhum outro código:
 * só observa as classes que o app.js já coloca no <body>.
 */
(function () {
  const corpo = document.body;
  const ARQUIVO = "assets/som/foguete.mp3";
  /** Segundos, depois do fim do voo, em que a escrita FRC ROBOTICS TEAM surge (ver estilo.css). */
  const ATRASO_ESCRITAS = 2.4;
  const DURACAO_DESVANECER = 600;
  const VOLUME = 0.8;

  const som = new Audio(ARQUIVO);
  som.preload = "auto";
  som.volume = VOLUME;

  let encerrado = false;
  let agendado = false;

  function parar() {
    if (encerrado) {
      return;
    }
    encerrado = true;
    removerGatilhos();
    const inicio = performance.now();
    const passo = () => {
      const p = Math.min((performance.now() - inicio) / DURACAO_DESVANECER, 1);
      som.volume = VOLUME * (1 - p);
      if (p < 1) {
        setTimeout(passo, 30);
      } else {
        som.pause();
      }
    };
    passo();
  }

  function tocar() {
    if (encerrado || !som.paused) {
      return;
    }
    const promessa = som.play();
    if (promessa) {
      promessa.catch(() => {});
    }
  }

  // Os navegadores só liberam som depois de um toque ou tecla; se o automático
  // for bloqueado, o som entra no primeiro toque ou tecla durante a abertura.
  const gatilhos = ["pointerdown", "keydown", "touchstart"];
  function aoInteragir() {
    if (corpo.classList.contains("carregando")) {
      tocar();
    }
  }
  function removerGatilhos() {
    gatilhos.forEach((nome) => document.removeEventListener(nome, aoInteragir, true));
  }
  gatilhos.forEach((nome) => document.addEventListener(nome, aoInteragir, true));

  function atualizar() {
    if (corpo.classList.contains("intro-visto")) {
      parar();
    } else if (corpo.classList.contains("pronto") && !agendado) {
      agendado = true;
      const emInicio = corpo.dataset.pagina === "inicio";
      setTimeout(parar, emInicio ? ATRASO_ESCRITAS * 1000 : 0);
    }
  }

  new MutationObserver(atualizar).observe(corpo, { attributes: true, attributeFilter: ["class"] });
  atualizar();
  tocar();
})();