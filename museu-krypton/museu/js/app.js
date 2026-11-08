"use strict";

/*
 * Museu digital da equipe Krypton FRC #9487.
 * Aqui ficam a navegação pelo endereço (#historia, #robos/2…),
 * a montagem das páginas internas e a tela de carregamento.
 */

const TITULO_SITE = "Krypton FRC #9487 — Museu";
const PASTA_FOTOS = "assets/robos/";
const PASTA_PATROCINADORES = "assets/patrocinadores/";
/** Tempo mínimo da tela de carregamento e duração da animação de entrada, em milissegundos. */
const DURACAO_CARREGAMENTO = 2600;
const FIM_INTRO = 5600;
const DURACAO_TROCA = 250;

const PAGINAS = {
  historia: { titulo: "A história", subtitulo: "A jornada da equipe", secoes: Conteudo.HISTORIA },
  premiacoes: { titulo: "Premiações", subtitulo: "Conquistas por temporada", secoes: Conteudo.PREMIACOES },
  areas: { titulo: "As áreas", subtitulo: "Áreas, mentores e integrantes", secoes: Conteudo.AREAS },
  first: { titulo: "A FIRST", subtitulo: "O programa por trás da equipe", secoes: Conteudo.FIRST },
};

const corpo = document.body;
const inicio = document.getElementById("inicio");
const pagina = document.getElementById("pagina");
const rodape = document.getElementById("rodape");
const movimentoReduzido = matchMedia("(prefers-reduced-motion: reduce)").matches;

/** Rota exibida no momento e o tratamento de teclas próprio dela. */
let rotaAtual = null;
let teclaPagina = null;

// ---- Utilidades ---------------------------------------------------------------

function esc(texto) {
  return String(texto).replace(/[&<>"']/g, (c) =>
    ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[c]);
}

/** Textos que começam com "[" ainda precisam ser preenchidos pela equipe. */
const classePendente = (texto) => (texto.startsWith("[") ? ' class="pendente"' : "");

/** Reinicia uma animação CSS ligada a uma classe. */
function reanimar(elemento, classe) {
  elemento.classList.remove(classe);
  void elemento.offsetWidth;
  elemento.classList.add(classe);
}

let avisoTempo;
function avisar(texto) {
  const aviso = document.getElementById("aviso");
  aviso.textContent = texto;
  aviso.classList.add("visivel");
  clearTimeout(avisoTempo);
  avisoTempo = setTimeout(() => aviso.classList.remove("visivel"), 2600);
}

document.addEventListener("click", (e) => {
  const alvo = e.target.closest("[data-aviso]");
  if (alvo) {
    avisar(alvo.dataset.aviso);
  }
});

// ---- Rodapé -------------------------------------------------------------------

function montarRodape() {
  rodape.querySelector(".contatos").innerHTML = Conteudo.CONTATOS.map((c) => {
    if (c.link == null) {
      return `<li><span>${esc(c.rotulo)}</span></li>`;
    }
    if (!c.link.trim()) {
      return `<li><button type="button" data-aviso="Este link ainda não foi configurado.">${esc(c.rotulo)}</button></li>`;
    }
    const externo = /^https?:/i.test(c.link) ? ' target="_blank" rel="noopener"' : "";
    return `<li><a href="${esc(c.link)}"${externo}>${esc(c.rotulo)}</a></li>`;
  }).join("");
  rodape.querySelector(".botao-first").href = Conteudo.LINK_FIRST;
}

// ---- Partes comuns das páginas internas -----------------------------------------

function cabecalhoHtml(titulo, subtitulo) {
  return `
    <header class="cabecalho">
      <a class="voltar" href="#">‹&nbsp; Voltar</a>
      <h1 tabindex="-1">${esc(titulo)}</h1>
      <p>${esc(subtitulo)}</p>
    </header>`;
}

/** Parágrafos com "• " viram lista; com "## ", subtítulo. */
function textoHtml(paragrafos) {
  let html = "";
  let listaAberta = false;
  for (const paragrafo of paragrafos) {
    const marcador = paragrafo.startsWith("• ");
    if (listaAberta && !marcador) {
      html += "</ul>";
      listaAberta = false;
    }
    if (marcador) {
      if (!listaAberta) {
        html += '<ul class="lista">';
        listaAberta = true;
      }
      const texto = paragrafo.slice(2);
      html += `<li${classePendente(texto)}>${esc(texto)}</li>`;
    } else if (paragrafo.startsWith("## ")) {
      html += `<h3>${esc(paragrafo.slice(3))}</h3>`;
    } else {
      html += `<p${classePendente(paragrafo)}>${esc(paragrafo)}</p>`;
    }
  }
  return listaAberta ? html + "</ul>" : html;
}

// ---- Páginas de leitura (história, premiações, áreas, FIRST) ---------------------

function mostrarLeitura(nome, indice) {
  const { titulo, subtitulo, secoes } = PAGINAS[nome];
  pagina.className = "pagina";
  pagina.innerHTML = cabecalhoHtml(titulo, subtitulo) + `
    <div class="abas" role="tablist" aria-label="Seções">
      ${secoes.map((s, i) => `
        <button type="button" class="aba" role="tab" id="aba-${i}" aria-controls="leitura"
                style="--i: ${i}">${esc(s.rotulo)}</button>`).join("")}
    </div>
    <article class="leitura painel" id="leitura" role="tabpanel" tabindex="0"></article>`;

  const abas = [...pagina.querySelectorAll(".aba")];
  const leitura = pagina.querySelector(".leitura");
  let atual = -1;

  const selecionar = (i, focar = false) => {
    if (i < 0 || i >= secoes.length || i === atual) {
      return;
    }
    atual = i;
    abas.forEach((aba, j) => {
      aba.setAttribute("aria-selected", String(j === i));
      aba.tabIndex = j === i ? 0 : -1;
    });
    leitura.setAttribute("aria-labelledby", abas[i].id);
    leitura.innerHTML = `
      <div class="leitura-conteudo">
        <h2>${esc(secoes[i].titulo)}</h2>
        ${textoHtml(secoes[i].paragrafos)}
      </div>`;
    leitura.scrollTop = 0;
    if (focar) {
      abas[i].focus();
      abas[i].scrollIntoView({ block: "nearest", inline: "nearest" });
    }
    history.replaceState(null, "", i ? `#${nome}/${i}` : `#${nome}`);
  };

  abas.forEach((aba, i) => aba.addEventListener("click", () => selecionar(i)));
  teclaPagina = (e) => {
    const nasAbas = e.target.closest?.(".abas");
    const proxima = { ArrowDown: 1, ArrowRight: 1, ArrowUp: -1, ArrowLeft: -1 }[e.key];
    // Fora das abas, só as setas para cima e para baixo trocam de seção.
    if (proxima && (nasAbas || e.key === "ArrowUp" || e.key === "ArrowDown")
        && !e.target.closest?.(".leitura")) {
      e.preventDefault();
      selecionar(atual + proxima, nasAbas);
    } else if (nasAbas && (e.key === "Home" || e.key === "End")) {
      e.preventDefault();
      selecionar(e.key === "Home" ? 0 : secoes.length - 1, true);
    }
  };

  selecionar(Math.min(Math.max(indice, 0), secoes.length - 1));
  document.title = `${titulo} — ${TITULO_SITE}`;
}

// ---- Robôs ---------------------------------------------------------------------

/** Se a foto de cada robô existe (true/false) ou ainda está sendo verificada (Promise). */
const fotos = new Map();

function verificarFoto(nome) {
  if (!fotos.has(nome)) {
    fotos.set(nome, new Promise((pronto) => {
      const img = new Image();
      img.onload = () => pronto(true);
      img.onerror = () => pronto(false);
      img.src = PASTA_FOTOS + nome;
    }).then((existe) => {
      fotos.set(nome, existe);
      return existe;
    }));
  }
  return fotos.get(nome);
}

/** Silhueta usada enquanto a equipe não adiciona a foto da temporada. */
const SILHUETA = `
  <svg viewBox="-160 -312 320 316" width="320" height="316" role="img" aria-label="Ilustração de um robô">
    <g fill="rgba(36,78,48,.59)" stroke="rgba(205,255,220,.84)" stroke-width="3"
       stroke-linecap="round" stroke-linejoin="round">
      <circle cx="-105" cy="-30" r="30" fill="rgb(6,18,10)"/>
      <circle cx="105" cy="-30" r="30" fill="rgb(6,18,10)"/>
      <rect x="-145" y="-88" width="290" height="56" rx="5"/>
      <path d="M-62 -88 L-34 -270 L2 -270 L42 -88 Z"/>
      <path d="M-42 -150 H26 M-34 -210 H12" fill="none"/>
      <path d="M-16 -256 L118 -300 L150 -256 L128 -236" fill="none" stroke-width="9"/>
    </g>
  </svg>
  <figcaption>Ilustração: adicione a foto do robô desta temporada</figcaption>`;

function fotoHtml(robo) {
  return `<img src="${esc(PASTA_FOTOS + robo.foto)}" alt="${esc(`Robô ${robo.nome}, temporada ${robo.temporada}`)}">`;
}

function mostrarRobos(indice) {
  const robos = Conteudo.ROBOS;
  pagina.className = "pagina robos";
  pagina.innerHTML = cabecalhoHtml("Visualizar robôs", "Da primeira temporada até a atual") + `
    <div class="vitrine">
      <div class="holofote" aria-hidden="true"></div>
      <div class="piso" aria-hidden="true"></div>
      <figure class="robo"></figure>
      <button type="button" class="seta seta-anterior" aria-label="Robô anterior">
        <svg viewBox="0 0 30 60" aria-hidden="true"><path d="M26 4 L4 30 L26 56"/></svg>
      </button>
      <button type="button" class="seta seta-proxima" aria-label="Próximo robô">
        <svg viewBox="0 0 30 60" aria-hidden="true"><path d="M4 4 L26 30 L4 56"/></svg>
      </button>
    </div>
    <article class="ficha painel" aria-live="polite">
      <div class="ficha-conteudo"></div>
    </article>
    <nav class="linha-tempo" aria-label="Temporadas">
      ${robos.map((r, i) => `
        <button type="button" class="ano" data-i="${i}">
          <span class="ano-ponto" aria-hidden="true"></span>${esc(r.temporada)}
        </button>`).join("")}
    </nav>`;

  const figura = pagina.querySelector(".robo");
  const ficha = pagina.querySelector(".ficha-conteudo");
  const anterior = pagina.querySelector(".seta-anterior");
  const proxima = pagina.querySelector(".seta-proxima");
  const anos = [...pagina.querySelectorAll(".ano")];
  let atual = -1;

  const selecionar = (i) => {
    if (i < 0 || i >= robos.length || i === atual) {
      return;
    }
    const direcao = i > atual ? 1 : -1;
    const primeira = atual < 0;
    atual = i;
    const robo = robos[i];

    const existe = robo.foto ? fotos.get(robo.foto) : false;
    figura.innerHTML = existe === true ? fotoHtml(robo) : SILHUETA;
    if (existe instanceof Promise) {
      existe.then((ok) => {
        if (ok && atual === i) {
          figura.innerHTML = fotoHtml(robo);
        }
      });
    }

    ficha.innerHTML = `
      <p class="ficha-ano">${esc(robo.temporada)}</p>
      <p class="ficha-temporada">Temporada ${esc(robo.jogo)}</p>
      <h2>${esc(robo.nome)}</h2>
      <p${classePendente(robo.descricao)}>${esc(robo.descricao)}</p>
      <ul class="lista">${robo.dados.map((d) => `<li>${esc(d)}</li>`).join("")}</ul>`;

    if (!primeira) {
      for (const elemento of [figura, ficha]) {
        elemento.style.setProperty("--direcao", direcao);
        reanimar(elemento, "trocou");
      }
    }
    anterior.hidden = i === 0;
    proxima.hidden = i === robos.length - 1;
    anos.forEach((ano, j) => ano.setAttribute("aria-current", String(j === i)));
    history.replaceState(null, "", i ? `#robos/${i}` : "#robos");
  };

  anterior.addEventListener("click", () => selecionar(atual - 1));
  proxima.addEventListener("click", () => selecionar(atual + 1));
  anos.forEach((ano, i) => ano.addEventListener("click", () => selecionar(i)));
  teclaPagina = (e) => {
    if (e.key === "ArrowLeft") {
      selecionar(atual - 1);
    } else if (e.key === "ArrowRight") {
      selecionar(atual + 1);
    }
  };

  // Deslizar o dedo para os lados também troca de robô.
  let toqueX = null;
  const vitrine = pagina.querySelector(".vitrine");
  vitrine.addEventListener("touchstart", (e) => { toqueX = e.touches[0].clientX; }, { passive: true });
  vitrine.addEventListener("touchend", (e) => {
    const dx = e.changedTouches[0].clientX - toqueX;
    if (toqueX !== null && Math.abs(dx) > 50) {
      selecionar(atual + (dx < 0 ? 1 : -1));
    }
    toqueX = null;
  });

  selecionar(Math.min(Math.max(indice, 0), robos.length - 1));
  document.title = `Robôs — ${TITULO_SITE}`;
}

// ---- Patrocinadores -------------------------------------------------------------

function mostrarPatrocinadores() {
  const separador = `<div class="patro-separador" aria-hidden="true"><img src="${PASTA_PATROCINADORES}nave.png" alt=""></div>`;
  const cartoes = Conteudo.PATROCINADORES.map((p) => `
    <section class="patro" aria-label="${esc(p.nome)}">
      <div class="patro-logo">
        <img src="${esc(PASTA_PATROCINADORES + p.logo)}" alt="Logo ${esc(p.nome)}" style="height: ${p.tamanho * 0.7}px">
      </div>
      <div class="patro-texto">
        <h3>Sobre a empresa</h3>
        <p${classePendente(p.sobre)}>${esc(p.sobre)}</p>
        <h3>Como ela nos ajuda</h3>
        <p${classePendente(p.ajuda)}>${esc(p.ajuda)}</p>
      </div>
    </section>`).join(separador);

  pagina.className = "pagina";
  pagina.innerHTML = `
    <header class="cabecalho">
      <a class="voltar" href="#">‹&nbsp; Voltar</a>
      <h1 class="faixa" tabindex="-1">Patrocinadores</h1>
      <p class="faixa faixa-sub">${esc(Conteudo.TEMPORADA_PATROCINIO)}</p>
    </header>
    <article class="leitura painel patrocinadores" tabindex="0" aria-label="Lista de patrocinadores">
      <div class="leitura-conteudo">
        ${cartoes}
        <div class="patro-apoio">
          <h2>Seja um apoiador</h2>
          <p>Quer ajudar a Krypton a chegar mais longe? Fale com a equipe pelos contatos da página inicial.</p>
          <a class="patro-botao" href="#">Ver contatos</a>
        </div>
      </div>
    </article>`;
  document.title = `Patrocinadores — ${TITULO_SITE}`;
}

// ---- Navegação -------------------------------------------------------------------

function lerRota() {
  const [nome = "", extra] = decodeURIComponent(location.hash.slice(1)).split("/");
  const valida = nome === "robos" || nome === "patrocinadores" || Object.hasOwn(PAGINAS, nome);
  return { nome: valida ? nome : "inicio", indice: parseInt(extra, 10) || 0 };
}

function aplicarRota() {
  const { nome, indice } = lerRota();
  teclaPagina = null;
  rotaAtual = nome;
  if (nome === "inicio") {
    pagina.hidden = true;
    pagina.innerHTML = "";
    inicio.hidden = false;
    corpo.dataset.pagina = "inicio";
    document.title = TITULO_SITE;
  } else {
    inicio.hidden = true;
    pagina.hidden = false;
    corpo.dataset.pagina = nome === "robos" ? "robos" : "codex";
    if (nome === "robos") {
      mostrarRobos(indice);
    } else if (nome === "patrocinadores") {
      mostrarPatrocinadores();
    } else {
      mostrarLeitura(nome, indice);
    }
  }
  window.scrollTo(0, 0);
}

function trocarPagina() {
  if (lerRota().nome === rotaAtual) {
    // Só mudou a seção dentro da mesma página (ex.: um link para #historia/2).
    aplicarRota();
    return;
  }
  pularIntro();
  corpo.classList.add("trocando");
  setTimeout(() => {
    aplicarRota();
    corpo.classList.remove("trocando");
    pagina.querySelector("h1")?.focus({ preventScroll: true });
  }, movimentoReduzido ? 0 : DURACAO_TROCA);
}

window.addEventListener("hashchange", trocarPagina);

document.addEventListener("keydown", (e) => {
  if (e.ctrlKey || e.altKey || e.metaKey || !corpo.classList.contains("pronto")) {
    return;
  }
  if (!corpo.classList.contains("intro-visto")) {
    pularIntro();
  }
  if (e.key === "Escape" && rotaAtual !== "inicio") {
    location.hash = "";
    return;
  }
  teclaPagina?.(e);
});

// ---- Carregamento e animação de entrada -----------------------------------------------

/** Encerra a animação de entrada da página inicial (um clique ou tecla também a encerra). */
function pularIntro() {
  corpo.classList.add("intro-visto");
}

document.addEventListener("pointerdown", () => {
  if (corpo.classList.contains("pronto")) {
    pularIntro();
  }
});

function carregar() {
  const carregador = document.querySelector(".carregador");
  const cheio = carregador.querySelector(".cheio");
  const barra = carregador.querySelector(".carregador-barra span");
  const mensagem = carregador.querySelector(".carregador-mensagem");
  const porcentagem = carregador.querySelector(".carregador-porcentagem");
  const MENSAGENS = [
    "Abrindo as portas do museu", "Polindo as vitrines", "Posicionando os robôs",
    "Acendendo as luzes",
  ];

  let fontesProntas = false;
  Promise.race([
    document.fonts ? document.fonts.ready : Promise.resolve(),
    new Promise((pronto) => setTimeout(pronto, 3000)),
  ]).then(() => { fontesProntas = true; });
  Conteudo.ROBOS.forEach((robo) => robo.foto && verificarFoto(robo.foto));

  const duracao = movimentoReduzido ? 300 : DURACAO_CARREGAMENTO;
  const comeco = performance.now();
  const passo = (agora) => {
    const p = Math.min((agora - comeco) / duracao, fontesProntas ? 1 : 0.9);
    cheio.style.clipPath = `inset(${(1 - p) * 100}% 0 0 0)`;
    barra.style.transform = `scaleX(${p})`;
    mensagem.textContent = MENSAGENS[Math.min(MENSAGENS.length - 1, Math.floor(p * MENSAGENS.length))];
    porcentagem.textContent = `${Math.round(p * 100)}%`;
    if (p < 1) {
      requestAnimationFrame(passo);
      return;
    }
    if (movimentoReduzido) {
      abrirPagina();
    } else {
      decolar(carregador, abrirPagina);
    }
  };
  const abrirPagina = () => {
    corpo.classList.remove("carregando");
    corpo.classList.add("pronto");
    if (rotaAtual !== "inicio" || movimentoReduzido) {
      pularIntro();
    } else {
      setTimeout(pularIntro, FIM_INTRO);
    }
  };
  requestAnimationFrame(passo);
}

montarRodape();
aplicarRota();
carregar();
