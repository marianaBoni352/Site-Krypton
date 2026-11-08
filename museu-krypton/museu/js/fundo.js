"use strict";

/*
 * Partículas verdes que sobem devagar no fundo. É só decoração: o resto do fundo
 * (luz, névoa e logo) é feito em CSS e SVG.
 */
(() => {
  const tela = document.querySelector(".particulas");
  if (!tela) {
    return;
  }
  const g = tela.getContext("2d");
  const reduzido = matchMedia("(prefers-reduced-motion: reduce)");

  // As partículas vivem em um quadro virtual de 1280x720 que cobre a janela, como o logo.
  const L = 1280;
  const A = 720;

  let semente = 9487;
  const aleatorio = () => {
    semente = (semente + 0x6d2b79f5) >>> 0;
    let z = semente;
    z = Math.imul(z ^ (z >>> 15), z | 1);
    z ^= z + Math.imul(z ^ (z >>> 7), z | 61);
    return ((z ^ (z >>> 14)) >>> 0) / 4294967296;
  };

  const particulas = Array.from({ length: 80 }, () => ({
    x: aleatorio() * L,
    y: aleatorio() * A,
    raio: 0.8 + aleatorio() * 2.2,
    velocidade: 3 + aleatorio() * 10,
    fase: aleatorio() * Math.PI * 2,
  }));

  let largura = 0;
  let altura = 0;
  let escala = 1;

  function ajustar() {
    const densidade = window.devicePixelRatio || 1;
    largura = window.innerWidth;
    altura = window.innerHeight;
    tela.width = Math.round(largura * densidade);
    tela.height = Math.round(altura * densidade);
    g.setTransform(densidade, 0, 0, densidade, 0, 0);
    escala = Math.max(largura / L, altura / A);
  }

  function desenhar(ms) {
    const t = ms / 1000;
    g.clearRect(0, 0, largura, altura);
    g.fillStyle = "#4dff7c";
    for (const p of particulas) {
      let y = (p.y - t * p.velocidade) % (A + 20);
      if (y < -10) {
        y += A + 20;
      }
      const x = p.x + Math.sin(t * 0.3 + p.fase) * 14;
      const cintila = 0.35 + 0.4 * Math.sin(t * 0.9 + p.fase);
      const sx = largura / 2 + (x - L / 2) * escala;
      const sy = altura / 2 + (y - A / 2) * escala;
      const r = p.raio * escala;

      g.globalAlpha = Math.max(0.08, cintila) * 0.35;
      g.beginPath();
      g.arc(sx, sy, r * 2, 0, Math.PI * 2);
      g.fill();
      g.globalAlpha = Math.max(0.1, cintila);
      g.beginPath();
      g.arc(sx, sy, r / 2, 0, Math.PI * 2);
      g.fill();
    }
    g.globalAlpha = 1;
    if (!reduzido.matches) {
      requestAnimationFrame(desenhar);
    }
  }

  window.addEventListener("resize", () => {
    ajustar();
    if (reduzido.matches) {
      desenhar(0);
    }
  });
  ajustar();
  requestAnimationFrame(desenhar);
})();
