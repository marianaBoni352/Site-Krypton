package krypton;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/** Tela preta de carregamento: o foguete "enche" enquanto o fundo é preparado e, pronto, decola. */
final class TelaCarregamento extends Tela {

    private static final double DURACAO_MINIMA = 2.6;
    private static final double ESCALA_FOGUETE = .62;
    private static final double FOGUETE_Y = 170;
    /** Onde o fogo nasce, abaixo da ponta do foguete. */
    private static final double BOCAL = 290 * ESCALA_FOGUETE;
    private static final double DURACAO_IGNICAO = .9;
    private static final double DURACAO_SUBIDA = 1.1;
    /** O foguete já saiu de vista um pouco antes do fim da subida: o menu entra sem tela vazia. */
    private static final double FIM_VOO = DURACAO_IGNICAO + DURACAO_SUBIDA - .25;
    /** Distância percorrida: o foguete some mesmo em janelas mais altas que 16:9. */
    private static final double PERCURSO = 1400;
    private static final int MAX_FAISCAS = 360;
    private static final String[] MENSAGENS = {
        "Abrindo as portas do museu", "Polindo as vitrines", "Posicionando os robôs",
        "Acendendo as luzes"
    };

    private final List<double[]> faiscas = new ArrayList<>();
    private final Random sorteio = new Random(9487);
    private boolean concluido;
    private double decolagem = -1;
    private double ultimoT;
    private double acumulado;

    TelaCarregamento(Palco palco) {
        super(palco);
    }

    private double progresso() {
        return Math.min(palco.recursos.progresso, t / DURACAO_MINIMA);
    }

    /** Segundos desde que a ignição começou; negativo enquanto ainda está carregando. */
    private double voo() {
        return decolagem < 0 ? -1 : t - decolagem;
    }

    /** Altura da ponta do foguete: parado na ignição, acelerando na subida. */
    private double alturaFoguete() {
        double s = Tema.etapa(voo(), DURACAO_IGNICAO, DURACAO_SUBIDA);
        return FOGUETE_Y - PERCURSO * s * s * s;
    }

    /** Força do fogo, de 0 a 1: cresce na ignição e continua forte durante a subida. */
    private double forcaFogo() {
        return decolagem < 0 ? 0 : Tema.suave(Tema.etapa(voo(), 0, DURACAO_IGNICAO));
    }

    @Override
    void avancar() {
        double dt = t - ultimoT;
        ultimoT = t;
        if (decolagem < 0 && progresso() >= 1) {
            decolagem = t;
        }
        if (decolagem >= 0) {
            soltarFaiscas(dt);
            if (!concluido && voo() >= FIM_VOO) {
                irParaMenu();
            }
        }
        moverFaiscas(dt);
    }

    private void irParaMenu() {
        concluido = true;
        palco.abrirMenu();
    }

    private void soltarFaiscas(double dt) {
        acumulado += dt * 260 * forcaFogo();
        double bocal = alturaFoguete() + BOCAL;
        while (acumulado >= 1 && faiscas.size() < MAX_FAISCAS) {
            acumulado--;
            double duracao = .45 + sorteio.nextDouble() * .75;
            faiscas.add(new double[] {
                Palco.L / 2.0 + (sorteio.nextDouble() - .5) * 34, bocal + sorteio.nextDouble() * 30,
                (sorteio.nextDouble() - .5) * 240, 260 + sorteio.nextDouble() * 420,
                duracao, duracao, 1.2 + sorteio.nextDouble() * 3.2
            });
        }
        acumulado = Math.min(acumulado, 1);
    }

    private void moverFaiscas(double dt) {
        double atrito = Math.pow(.14, dt);
        for (Iterator<double[]> it = faiscas.iterator(); it.hasNext();) {
            double[] f = it.next();
            f[4] -= dt;
            if (f[4] <= 0) {
                it.remove();
                continue;
            }
            f[0] += f[2] * dt;
            f[1] += f[3] * dt;
            f[2] *= atrito;
            f[3] *= atrito;
        }
    }

    @Override
    void desenharFundo(Graphics2D g, int w, int h) {
        // O fundo continua preto até a revelação no menu.
    }

    @Override
    void desenhar(Graphics2D g0) {
        Graphics2D g = (Graphics2D) g0.create();
        double fogo = forcaFogo();
        double voo = voo();
        // O tremor cresce com a ignição e some quando o foguete já está longe.
        double tremor = fogo * 4.5 * (1 - Tema.etapa(voo, DURACAO_IGNICAO + .35, .5));
        g.translate(Math.sin(t * 83) * tremor, Math.cos(t * 97) * tremor);

        double y = decolagem < 0 ? FOGUETE_Y : alturaFoguete();
        Tema.alfa(g, Tema.etapa(t, 0, .5));
        if (fogo > 0) {
            desenharRastro(g, y + BOCAL, voo);
            desenharLuz(g, y + BOCAL, fogo);
            desenharFaiscas(g);
            desenharFogo(g, y + BOCAL, fogo, voo);
        }
        desenharFoguete(g, y);
        desenharProgresso(g, 1 - Tema.etapa(voo, 0, .35));
        g.dispose();
        Tema.alfa(g0, 1);
    }

    private void desenharFoguete(Graphics2D g, double y) {
        double p = progresso();
        Graphics2D foguete = (Graphics2D) g.create();
        foguete.translate(Palco.L / 2.0, y);
        foguete.scale(ESCALA_FOGUETE, ESCALA_FOGUETE);
        if (decolagem < 0) {
            foguete.setColor(new Color(77, 255, 124, 38));
            Recursos.desenharFoguete(foguete);
        }
        double altura = 420 * p;
        foguete.clip(new Rectangle2D.Double(-150, 410 - altura, 300, altura));
        foguete.setColor(Tema.BRANCO);
        Recursos.desenharFoguete(foguete);
        foguete.dispose();
    }

    private void desenharProgresso(Graphics2D g, double a) {
        if (a <= 0) {
            return;
        }
        double p = progresso();
        Tema.alfa(g, Tema.etapa(t, 0, .5) * a);
        float x = Palco.L / 2f - 200;
        float y = 500;
        g.setColor(new Color(255, 255, 255, 28));
        g.fill(new Rectangle2D.Float(x, y, 400, 2));
        g.setColor(Tema.VERDE);
        g.fill(new Rectangle2D.Double(x, y, 400 * p, 2));

        g.setFont(Tema.fonte(Font.PLAIN, 16));
        String mensagem = MENSAGENS[Math.min(MENSAGENS.length - 1, (int) (p * MENSAGENS.length))];
        Tema.texto(g, mensagem, x, y + 30, .5f, Tema.APAGADO, null, 0);
        String porcentagem = Math.round(p * 100) + "%";
        Tema.texto(g, porcentagem, x + 400 - Tema.largura(g, porcentagem, 0), y + 30, 0,
                Tema.TEXTO, null, 0);
    }

    /** Coluna de luz que o foguete deixa para trás e que se apaga antes da troca de tela. */
    private void desenharRastro(Graphics2D g, double bocal, double voo) {
        double a = Tema.etapa(voo, DURACAO_IGNICAO, .25) * (1 - Tema.etapa(voo, 1.5, .55));
        if (a <= 0) {
            return;
        }
        double fim = Math.max(bocal + 1, FOGUETE_Y + BOCAL + 560);
        double largura = 7 + 26 * Tema.etapa(voo, DURACAO_IGNICAO, 1);
        Tema.alfa(g, a);
        g.setPaint(new GradientPaint(0, (float) bocal, new Color(190, 255, 210, 170), 0, (float) fim,
                new Color(77, 255, 124, 0)));
        g.fill(new Rectangle2D.Double(Palco.L / 2.0 - largura / 2, bocal, largura, fim - bocal));
    }

    private void desenharLuz(Graphics2D g, double bocal, double fogo) {
        double pulso = .85 + .15 * Math.sin(t * 41);
        float raio = (float) ((130 + 210 * fogo) * pulso);
        Point2D centro = new Point2D.Double(Palco.L / 2.0, bocal + 40);
        Tema.alfa(g, .75 * fogo);
        g.setPaint(new RadialGradientPaint(centro, raio, new float[] {0, .35f, 1},
                new Color[] {new Color(150, 255, 180, 165), new Color(77, 255, 124, 55),
                    new Color(77, 255, 124, 0)}));
        g.fill(new Ellipse2D.Double(centro.getX() - raio, centro.getY() - raio, raio * 2, raio * 2));
    }

    private void desenharFogo(Graphics2D g, double bocal, double fogo, double voo) {
        double forca = fogo * (.65 + .35 * Tema.etapa(voo, DURACAO_IGNICAO, .3));
        double oscila = 1 + .13 * Math.sin(t * 47) + .08 * Math.sin(t * 71);
        double comprimento = 250 * forca * oscila;
        Tema.alfa(g, .55);
        g.setColor(Tema.VERDE);
        g.fill(chama(bocal, 36 * forca, comprimento, 0));
        Tema.alfa(g, .85);
        g.setColor(new Color(170, 255, 195));
        g.fill(chama(bocal, 21 * forca, comprimento * .68, 1.7));
        Tema.alfa(g, 1);
        g.setColor(Tema.BRANCO);
        g.fill(chama(bocal, 10 * forca, comprimento * .4, 3.1));
    }

    private Path2D chama(double bocal, double meiaLargura, double comprimento, double fase) {
        double cx = Palco.L / 2.0;
        double ponta = Math.sin(t * 39 + fase) * meiaLargura * .22;
        Path2D forma = new Path2D.Double();
        forma.moveTo(cx - meiaLargura, bocal);
        forma.quadTo(cx - meiaLargura * 1.1, bocal + comprimento * .4, cx + ponta, bocal + comprimento);
        forma.quadTo(cx + meiaLargura * 1.1, bocal + comprimento * .4, cx + meiaLargura, bocal);
        forma.closePath();
        return forma;
    }

    private void desenharFaiscas(Graphics2D g) {
        g.setColor(new Color(170, 255, 195));
        for (double[] f : faiscas) {
            double vida = f[4] / f[5];
            double raio = f[6] * (.5 + vida * .5);
            Tema.alfa(g, vida * vida);
            g.fill(new Ellipse2D.Double(f[0] - raio, f[1] - raio, raio * 2, raio * 2));
        }
    }

    /** Depois de carregado, um clique ou tecla pula a decolagem; antes disso, nada acontece. */
    @Override
    void clicou(double x, double y) {
        pular();
    }

    @Override
    void tecla(int codigo) {
        pular();
    }

    private void pular() {
        if (decolagem >= 0 && !concluido) {
            irParaMenu();
        }
    }
}
