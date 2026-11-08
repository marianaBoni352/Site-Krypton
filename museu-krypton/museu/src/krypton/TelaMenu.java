package krypton;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.function.Supplier;

/** Tela de entrada: título e opções à esquerda, logo ao fundo e rodapé com os contatos. */
final class TelaMenu extends Tela {

    /** A partir deste instante, a animação de entrada terminou. */
    private static final double FIM_INTRO = 5.6;
    private static final double INICIO_TITULO = 2.7;
    private static final double INICIO_OPCOES = 3.4;
    private static final double INICIO_RODAPE = 4.4;
    private static final float MENU_X = 64;
    private static final float MENU_Y = 250;
    private static final float MENU_PASSO = 50;
    /** Distância da linha do rodapé até a borda inferior da janela. */
    private static final float RODAPE_MARGEM = 18;

    private record Opcao(String rotulo, Supplier<Tela> destino) {
    }

    private final List<Opcao> opcoes;
    private final Rectangle2D[] areasOpcoes;
    private final double[] destaque;
    private final Rectangle2D[] areasContatos = new Rectangle2D[Conteudo.CONTATOS.size()];
    private final Rectangle2D areaFirst = new Rectangle2D.Double();
    private int selecionada = -1;
    private double ultimoMouseX = -1;
    private double ultimoMouseY = -1;

    TelaMenu(Palco palco) {
        super(palco);
        opcoes = List.of(
                new Opcao("CONHECER A HISTÓRIA", () -> new TelaCodex(palco, "A HISTÓRIA",
                        "A jornada da equipe", Conteudo.HISTORIA)),
                new Opcao("VISUALIZAR ROBÔS", () -> new TelaRobos(palco)),
                new Opcao("PREMIAÇÕES E CONQUISTAS", () -> new TelaCodex(palco, "PREMIAÇÕES",
                        "Conquistas por temporada", Conteudo.PREMIACOES)),
                new Opcao("CONHECER AS ÁREAS", () -> new TelaCodex(palco, "AS ÁREAS",
                        "Áreas, mentores e integrantes", Conteudo.AREAS)),
                new Opcao("SOBRE A FIRST", () -> new TelaCodex(palco, "A FIRST",
                        "O programa por trás da equipe", Conteudo.FIRST)));
        areasOpcoes = new Rectangle2D[opcoes.size()];
        destaque = new double[opcoes.size()];
        for (int i = 0; i < areasOpcoes.length; i++) {
            areasOpcoes[i] = new Rectangle2D.Double(MENU_X - 16, MENU_Y + i * MENU_PASSO - 31,
                    350, MENU_PASSO - 4);
        }
    }

    private boolean introTerminou() {
        return t >= FIM_INTRO;
    }

    @Override
    void avancar() {
        if (palco.mouseX != ultimoMouseX || palco.mouseY != ultimoMouseY) {
            ultimoMouseX = palco.mouseX;
            ultimoMouseY = palco.mouseY;
            if (t >= INICIO_OPCOES + .3) {
                selecionada = -1;
                for (int i = 0; i < areasOpcoes.length; i++) {
                    if (areasOpcoes[i].contains(palco.mouseX, palco.mouseY)) {
                        selecionada = i;
                    }
                }
            }
        }
        for (int i = 0; i < destaque.length; i++) {
            destaque[i] += ((i == selecionada ? 1 : 0) - destaque[i]) * .2;
        }
    }

    @Override
    void desenharFundo(Graphics2D g, int w, int h) {
        palco.recursos.desenharFundo(g, w, h, palco.relogio, t, 0);
    }

    @Override
    void desenhar(Graphics2D g) {
        desenharTitulo(g);
        desenharOpcoes(g);
        desenharRodape(g);
        Tema.alfa(g, 1);
    }

    private void desenharTitulo(Graphics2D g) {
        double p = Tema.etapa(t, INICIO_TITULO, 1.1);
        if (p <= 0) {
            return;
        }
        double e = Tema.suave(p);
        Tema.alfa(g, e);
        g.setFont(Tema.fonte(Font.BOLD, 72));
        // As letras começam afastadas e se fecham enquanto o título aparece.
        float espaco = (float) (3 + (1 - e) * 24);
        float forca = (float) (1.1 + (1 - p) * 2.4);
        if (p < 1) {
            Tema.textoAnimado(g, "KRYPTON", MENU_X, 122, espaco, Tema.BRANCO, Tema.VERDE, forca);
        } else {
            Tema.texto(g, "KRYPTON", MENU_X, 122, 3, Tema.BRANCO, Tema.VERDE, 1.1f);
        }

        Tema.alfa(g, Tema.etapa(t, INICIO_TITULO + .4, .7));
        g.setFont(Tema.fonte(Font.BOLD, 18));
        float largura = Tema.texto(g, "FRC", MENU_X + 3, 158, 9, Tema.VERDE, null, 0);
        Tema.texto(g, "#9487", MENU_X + 3 + largura + 26, 158, 9, Tema.BRANCO, null, 0);
    }

    private void desenharOpcoes(Graphics2D g) {
        g.setFont(Tema.fonte(Font.BOLD, 23));
        for (int i = 0; i < opcoes.size(); i++) {
            double e = Tema.suave(Tema.etapa(t, INICIO_OPCOES + i * .13, .5));
            if (e <= 0) {
                continue;
            }
            if (t >= INICIO_OPCOES) {
                sobre(areasOpcoes[i]);
            }
            double d = destaque[i];
            float x = (float) (MENU_X - (1 - e) * 26 + d * 22);
            float y = MENU_Y + i * MENU_PASSO;

            if (d > .02) {
                Tema.alfa(g, e * d);
                Path2D losango = new Path2D.Float();
                losango.moveTo(MENU_X + 5, y - 15);
                losango.lineTo(MENU_X + 11, y - 9);
                losango.lineTo(MENU_X + 5, y - 3);
                losango.lineTo(MENU_X - 1, y - 9);
                losango.closePath();
                g.setColor(Tema.VERDE);
                g.fill(losango);
            }
            Tema.alfa(g, e);
            Tema.texto(g, opcoes.get(i).rotulo(), x + 1, y + 2, 1, new Color(0, 0, 0, 170), null, 0);
            Tema.texto(g, opcoes.get(i).rotulo(), x, y, 1, Tema.TEXTO, Tema.VERDE, .25f);
            if (d > .02) {
                Tema.alfa(g, e * d);
                Tema.texto(g, opcoes.get(i).rotulo(), x, y, 1, Tema.BRANCO, Tema.VERDE, 1.35f);
            }
        }
    }

    private void desenharRodape(Graphics2D g) {
        double e = Tema.suave(Tema.etapa(t, INICIO_RODAPE, .8));
        if (e <= 0) {
            return;
        }
        Tema.alfa(g, e);
        float y = (float) (palco.fundoVirtual() - RODAPE_MARGEM + (1 - e) * 14);
        float vao = 30;
        float larguraBotao = 88;
        g.setFont(Tema.fonte(Font.PLAIN, 17));
        float total = larguraBotao;
        for (Conteudo.Contato c : Conteudo.CONTATOS) {
            total += Tema.largura(g, c.rotulo(), .3f) + vao;
        }
        float x = (Palco.L - total) / 2;
        for (int i = 0; i < Conteudo.CONTATOS.size(); i++) {
            Conteudo.Contato c = Conteudo.CONTATOS.get(i);
            float largura = Tema.largura(g, c.rotulo(), .3f);
            areasContatos[i] = new Rectangle2D.Float(x - 6, y - 22, largura + 12, 32);
            boolean foco = c.link() != null && sobre(areasContatos[i]);
            Tema.texto(g, c.rotulo(), x, y, .3f, foco ? Tema.VERDE : Tema.TEXTO, null, 0);
            x += largura + vao;
        }

        areaFirst.setRect(x, y - 27, larguraBotao, 38);
        boolean focoFirst = sobre(areaFirst);
        g.setColor(focoFirst ? Tema.VERDE : Tema.BRANCO);
        g.fill(new RoundRectangle2D.Double(x, y - 27, larguraBotao, 38, 8, 8));
        g.setFont(Tema.fonte(Font.BOLD, 17));
        Tema.texto(g, "FIRST", x + 18, y - 2, 2.5f, new Color(4, 16, 8), null, 0);
    }

    @Override
    void clicou(double x, double y) {
        if (!introTerminou()) {
            t = FIM_INTRO;
            return;
        }
        for (int i = 0; i < areasOpcoes.length; i++) {
            if (areasOpcoes[i].contains(x, y)) {
                palco.abrir(opcoes.get(i).destino().get());
                return;
            }
        }
        for (int i = 0; i < areasContatos.length; i++) {
            String link = Conteudo.CONTATOS.get(i).link();
            if (link != null && areasContatos[i] != null && areasContatos[i].contains(x, y)) {
                palco.abrirLink(link);
                return;
            }
        }
        if (areaFirst.contains(x, y)) {
            palco.abrirLink(Conteudo.LINK_FIRST);
        }
    }

    @Override
    void tecla(int codigo) {
        if (!introTerminou()) {
            t = FIM_INTRO;
            return;
        }
        int n = opcoes.size();
        if (codigo == KeyEvent.VK_DOWN) {
            selecionada = (selecionada + 1) % n;
        } else if (codigo == KeyEvent.VK_UP) {
            selecionada = selecionada <= 0 ? n - 1 : selecionada - 1;
        } else if ((codigo == KeyEvent.VK_ENTER || codigo == KeyEvent.VK_SPACE) && selecionada >= 0) {
            palco.abrir(opcoes.get(selecionada).destino().get());
        }
    }
}
