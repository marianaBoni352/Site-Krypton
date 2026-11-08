"use strict";

/*
 * Fim da tela de carregamento: o foguete acende, treme e decola antes de o menu entrar.
 * Um clique, toque ou tecla pula a decolagem.
 */
function decolar(carregador, aoTerminar) {
  const DURACAO_IGNICAO = 0.9;
  const DURACAO_SUBIDA = 1.1;
  /** O foguete já saiu de vista um pouco antes do fim da subida: o menu entra sem tela vazia. */
  const FIM_VOO = DURACAO_IGNICAO + DURACAO_SUBIDA - 0.25;
  const MAX_FAISCAS = 360;
  /** Onde o fogo nasce, abaixo do topo do foguete (a caixa dele tem 260px de altura). */
  const BOCAL = 184;

  const etapa = (t, inicio, duracao) => Math.max(0, Math.min(1, (t - inicio) / duracao));
  const suave = (v) => 1 - Math.pow(1 - v, 3);

  const foguete = carregador.querySelector(".carregador-foguete");
  const caixa = foguete.getBoundingClientRect();
  const cx = caixa.left + caixa.width / 2;
  const topo = caixa.top;
  // Distância suficiente para o foguete sumir em janelas de qualquer altura.
  const percurso = Math.max(1400, window.innerHeight * 2);

  const tela = document.createElement("canvas");
  tela.className = "carregador-fogo";
  carregador.prepend(tela);
  const densidade = window.devicePixelRatio || 1;
  tela.width = Math.round(window.innerWidth * densidade);
  tela.height = Math.round(window.innerHeight * densidade);
  const g = tela.getContext("2d");
  carregador.classList.add("decolando");

  let faiscas = [];
  let acumulado = 0;
  let comeco = null;
  let anterior = 0;
  let terminou = false;

  const terminar = () => {
    if (terminou) {
      return;
    }
    terminou = true;
    document.removeEventListener("pointerdown", terminar);
    document.removeEventListener("keydown", terminar);
    aoTerminar();
  };
  document.addEventListener("pointerdown", terminar);
  document.addEventListener("keydown", terminar);

  function soltarFaiscas(dt, bocal, fogo) {
    acumulado += dt * 260 * fogo;
    while (acumulado >= 1 && faiscas.length < MAX_FAISCAS) {
      acumulado--;
      const duracao = 0.45 + Math.random() * 0.75;
      faiscas.push({
        x: cx + (Math.random() - 0.5) * 34,
        y: bocal + Math.random() * 30,
        vx: (Math.random() - 0.5) * 240,
        vy: 260 + Math.random() * 420,
        vida: duracao,
        duracao,
        raio: 1.2 + Math.random() * 3.2,
      });
    }
    acumulado = Math.min(acumulado, 1);
  }

  function moverFaiscas(dt) {
    const atrito = Math.pow(0.14, dt);
    faiscas = faiscas.filter((f) => {
      f.vida -= dt;
      f.x += f.vx * dt;
      f.y += f.vy * dt;
      f.vx *= atrito;
      f.vy *= atrito;
      return f.vida > 0;
    });
  }

  /** Coluna de luz que o foguete deixa para trás e que se apaga antes da troca de tela. */
  function desenharRastro(bocal, voo) {
    const a = etapa(voo, DURACAO_IGNICAO, 0.25) * (1 - etapa(voo, 1.5, 0.55));
    if (a <= 0) {
      return;
    }
    const fim = Math.max(bocal + 1, topo + BOCAL + 560);
    const largura = 7 + 26 * etapa(voo, DURACAO_IGNICAO, 1);
    const gradiente = g.createLinearGradient(0, bocal, 0, fim);
    gradiente.addColorStop(0, "rgba(190,255,210,0.67)");
    gradiente.addColorStop(1, "rgba(77,255,124,0)");
    g.globalAlpha = a;
    g.fillStyle = gradiente;
    g.fillRect(cx - largura / 2, bocal, largura, fim - bocal);
  }

  function desenharLuz(bocal, fogo, t) {
    const raio = (130 + 210 * fogo) * (0.85 + 0.15 * Math.sin(t * 41));
    const cy = bocal + 40;
    const gradiente = g.createRadialGradient(cx, cy, 0, cx, cy, raio);
    gradiente.addColorStop(0, "rgba(150,255,180,0.65)");
    gradiente.addColorStop(0.35, "rgba(77,255,124,0.22)");
    gradiente.addColorStop(1, "rgba(77,255,124,0)");
    g.globalAlpha = 0.75 * fogo;
    g.fillStyle = gradiente;
    g.fillRect(cx - raio, cy - raio, raio * 2, raio * 2);
  }

  function chama(bocal, meiaLargura, comprimento, fase, t) {
    const ponta = Math.sin(t * 39 + fase) * meiaLargura * 0.22;
    g.beginPath();
    g.moveTo(cx - meiaLargura, bocal);
    g.quadraticCurveTo(cx - meiaLargura * 1.1, bocal + comprimento * 0.4, cx + ponta, bocal + comprimento);
    g.quadraticCurveTo(cx + meiaLargura * 1.1, bocal + comprimento * 0.4, cx + meiaLargura, bocal);
    g.closePath();
    g.fill();
  }

  function desenharFogo(bocal, fogo, voo, t) {
    const forca = fogo * (0.65 + 0.35 * etapa(voo, DURACAO_IGNICAO, 0.3));
    const comprimento = 250 * forca * (1 + 0.13 * Math.sin(t * 47) + 0.08 * Math.sin(t * 71));
    g.globalAlpha = 0.55;
    g.fillStyle = "#4dff7c";
    chama(bocal, 36 * forca, comprimento, 0, t);
    g.globalAlpha = 0.85;
    g.fillStyle = "rgb(170,255,195)";
    chama(bocal, 21 * forca, comprimento * 0.68, 1.7, t);
    g.globalAlpha = 1;
    g.fillStyle = "#f4fff6";
    chama(bocal, 10 * forca, comprimento * 0.4, 3.1, t);
  }

  function desenharFaiscas() {
    g.fillStyle = "rgb(170,255,195)";
    for (const f of faiscas) {
      const vida = f.vida / f.duracao;
      g.globalAlpha = vida * vida;
      g.beginPath();
      g.arc(f.x, f.y, f.raio * (0.5 + vida * 0.5), 0, Math.PI * 2);
      g.fill();
    }
  }

  function passo(agora) {
    if (terminou) {
      return;
    }
    if (comeco === null) {
      comeco = agora;
      anterior = agora;
    }
    const voo = (agora - comeco) / 1000;
    const dt = Math.min(0.1, (agora - anterior) / 1000);
    anterior = agora;

    const fogo = suave(etapa(voo, 0, DURACAO_IGNICAO));
    const s = etapa(voo, DURACAO_IGNICAO, DURACAO_SUBIDA);
    const subida = percurso * s * s * s;
    // O tremor cresce com a ignição e some quando o foguete já está longe.
    const tremor = fogo * 4.5 * (1 - etapa(voo, DURACAO_IGNICAO + 0.35, 0.5));
    const tx = Math.sin(voo * 83) * tremor;
    const ty = Math.cos(voo * 97) * tremor;
    const bocal = topo - subida + BOCAL;

    soltarFaiscas(dt, bocal, fogo);
    moverFaiscas(dt);

    foguete.style.transform = `translate(${tx}px, ${ty - subida}px)`;
    g.setTransform(densidade, 0, 0, densidade, tx * densidade, ty * densidade);
    g.clearRect(-20, -20, window.innerWidth + 40, window.innerHeight + 40);
    desenharRastro(bocal, voo);
    desenharLuz(bocal, fogo, voo);
    desenharFaiscas();
    desenharFogo(bocal, fogo, voo, voo);

    if (voo >= FIM_VOO) {
      terminar();
      return;
    }
    requestAnimationFrame(passo);
  }
  requestAnimationFrame(passo);
}
