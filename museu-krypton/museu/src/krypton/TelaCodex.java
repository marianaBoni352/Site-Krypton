package krypton;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/** Página de leitura: seções à esquerda e texto com rolagem à direita. */
final class TelaCodex extends Tela {

    private static final int PAINEL_X = 400;
    private static final int PAINEL_Y = 56;
    private static final int PAINEL_L = 816;
    private static final int PAINEL_A = 610;
    private static final int MARGEM = 40;
    private static final int TEXTO_TOPO = PAINEL_Y + 112;
    private static final int TEXTO_FIM = PAINEL_Y + PAINEL_A - 28;
    private static final float LISTA_Y = 216;
    private static final float LISTA_PASSO = 44;

    private record Linha(String texto, Font fonte, Color cor, int x, int y, boolean marcador) {
    }

    private final String titulo;
    private final String subtitulo;
    private final List<Conteudo.Secao> secoes;
    private final Rectangle2D[] areas;
    private int atual;
    private double mudouEm;
    private List<Linha> linhas = List.of();
    private int alturaTexto;
    private double rolagem;
    private double rolagemAlvo;

    TelaCodex(Palco palco, String titulo, String subtitulo, List<Conteudo.Secao> secoes) {
        super(palco);
        this.titulo = titulo;
        this.subtitulo = subtitulo;
        this.secoes = secoes;
        areas = new Rectangle2D[secoes.size()];
        for (int i = 0; i < areas.length; i++) {
            areas[i] = new Rectangle2D.Double(48, LISTA_Y + i * LISTA_PASSO - 28, 320, LISTA_PASSO - 4);
        }
        montar();
    }

    private void selecionar(int i) {
        if (i != atual && i >= 0 && i < secoes.size()) {
            atual = i;
            mudouEm = t;
            rolagem = 0;
            rolagemAlvo = 0;
            montar();
        }
    }

    private void montar() {
        List<Linha> saida = new ArrayList<>();
        Font corpo = Tema.fonte(Font.PLAIN, 20);
        Font pendente = Tema.fonte(Font.ITALIC, 19);
        Font subtituloFonte = Tema.fonte(Font.BOLD, 19);
        int larguraMax = PAINEL_L - MARGEM * 2 - 10;
        int x = PAINEL_X + MARGEM;
        int y = 24;
        for (String paragrafo : secoes.get(atual).paragrafos()) {
            if (paragrafo.startsWith("## ")) {
                y += 8;
                saida.add(new Linha(paragrafo.substring(3).toUpperCase(), subtituloFonte, Tema.VERDE,
                        x, y, false));
                y += 34;
                continue;
            }
            boolean marcador = paragrafo.startsWith("• ");
            String texto = marcador ? paragrafo.substring(2) : paragrafo;
            boolean falta = texto.startsWith("[");
            Font fonte = falta ? pendente : corpo;
            FontMetrics fm = palco.getFontMetrics(fonte);
            int recuo = marcador ? 24 : 0;
            boolean primeira = true;
            for (String linha : Tema.quebrar(fm, texto, larguraMax - recuo)) {
                saida.add(new Linha(linha, fonte, falta ? Tema.APAGADO : Tema.TEXTO, x + recuo, y,
                        marcador && primeira));
                primeira = false;
                y += 30;
            }
            y += marcador ? 4 : 14;
        }
        linhas = saida;
        alturaTexto = y;
    }

    private double rolagemMaxima() {
        return Math.max(0, alturaTexto - (TEXTO_FIM - TEXTO_TOPO));
    }

    @Override
    void avancar() {
        rolagem += (rolagemAlvo - rolagem) * .2;
    }

    @Override
    void desenhar(Graphics2D g) {
        Tema.alfa(g, Tema.etapa(t, 0, .4));
        desenharCabecalho(g, titulo, subtitulo);

        g.setFont(Tema.fonte(Font.BOLD, 20));
        for (int i = 0; i < secoes.size(); i++) {
            double e = Tema.suave(Tema.etapa(t, .1 + i * .06, .4));
            boolean foco = sobre(areas[i]);
            boolean ativa = i == atual;
            float x = (float) (64 - (1 - e) * 18);
            float y = LISTA_Y + i * LISTA_PASSO;
            Tema.alfa(g, e);
            if (ativa) {
                g.setColor(Tema.VERDE);
                g.fill(new Rectangle2D.Float(x - 16, y - 19, 3, 24));
            }
            Color cor = ativa ? Tema.BRANCO : foco ? Tema.VERDE : Tema.APAGADO;
            Tema.texto(g, secoes.get(i).rotulo(), x, y, .5f, cor, Tema.VERDE, ativa ? .8f : 0);
        }

        double painel = Tema.suave(Tema.etapa(t, .15, .5));
        Tema.alfa(g, painel);
        double dy = (1 - painel) * 16;
        RoundRectangle2D caixa = new RoundRectangle2D.Double(PAINEL_X, PAINEL_Y + dy, PAINEL_L,
                PAINEL_A, 10, 10);
        g.setColor(Tema.PAINEL);
        g.fill(caixa);
        g.setColor(Tema.BORDA);
        g.draw(caixa);

        double conteudo = painel * Tema.suave(Tema.etapa(t, mudouEm, .35));
        Tema.alfa(g, conteudo);
        g.setFont(Tema.fonte(Font.BOLD, 32));
        Tema.texto(g, secoes.get(atual).titulo(), PAINEL_X + MARGEM, PAINEL_Y + 62, .5f, Tema.BRANCO,
                Tema.VERDE, .6f);
        g.setColor(Tema.BORDA);
        g.fillRect(PAINEL_X + MARGEM, PAINEL_Y + 84, PAINEL_L - MARGEM * 2, 1);

        Shape recorte = g.getClip();
        g.clip(new Rectangle2D.Double(PAINEL_X, TEXTO_TOPO, PAINEL_L, TEXTO_FIM - TEXTO_TOPO));
        for (Linha l : linhas) {
            float y = (float) (TEXTO_TOPO + l.y() - rolagem);
            g.setFont(l.fonte());
            g.setColor(l.cor());
            g.drawString(l.texto(), l.x(), y);
            if (l.marcador()) {
                g.setColor(Tema.VERDE);
                g.fill(new Rectangle2D.Float(l.x() - 20, y - 11, 7, 7));
            }
        }
        g.setClip(recorte);

        double maximo = rolagemMaxima();
        if (maximo > 0) {
            double area = TEXTO_FIM - TEXTO_TOPO;
            double altura = area * area / (area + maximo);
            double pos = TEXTO_TOPO + (area - altura) * (rolagem / maximo);
            g.setColor(Tema.VERDE_MEDIO);
            g.fill(new Rectangle2D.Double(PAINEL_X + PAINEL_L - 12, pos, 3, altura));
        }
        Tema.alfa(g, 1);
    }

    @Override
    void clicou(double x, double y) {
        super.clicou(x, y);
        for (int i = 0; i < areas.length; i++) {
            if (areas[i].contains(x, y)) {
                selecionar(i);
            }
        }
    }

    @Override
    void rolou(double quanto) {
        rolagemAlvo = Math.max(0, Math.min(rolagemMaxima(), rolagemAlvo + quanto * 48));
    }

    @Override
    void tecla(int codigo) {
        super.tecla(codigo);
        if (codigo == KeyEvent.VK_UP) {
            selecionar(atual - 1);
        } else if (codigo == KeyEvent.VK_DOWN) {
            selecionar(atual + 1);
        } else if (codigo == KeyEvent.VK_PAGE_DOWN) {
            rolou(8);
        } else if (codigo == KeyEvent.VK_PAGE_UP) {
            rolou(-8);
        }
    }
}
