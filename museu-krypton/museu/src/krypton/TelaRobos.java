package krypton;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;

/** A vitrine do museu: um robô por temporada, da primeira até a atual. */
final class TelaRobos extends Tela {

    private static final double VITRINE_X = 420;
    private static final double PISO_Y = 548;
    private static final double FOTO_L = 380;
    private static final double FOTO_A = 340;
    private static final int PAINEL_X = 770;
    private static final int PAINEL_L = 446;
    private static final float LINHA_Y = 640;
    private static final float LINHA_X0 = 240;
    private static final float LINHA_X1 = 1040;

    private final List<Conteudo.Robo> robos = Conteudo.ROBOS;
    private final BufferedImage[] fotos = new BufferedImage[robos.size()];
    private final Rectangle2D[] areasAnos = new Rectangle2D[robos.size()];
    private final Rectangle2D areaAnterior = new Rectangle2D.Double(68, 310, 56, 80);
    private final Rectangle2D areaProximo = new Rectangle2D.Double(668, 310, 56, 80);
    private int atual;
    private int direcao = 1;
    private double mudouEm;

    TelaRobos(Palco palco) {
        super(palco);
        for (int i = 0; i < robos.size(); i++) {
            fotos[i] = lerFoto(robos.get(i).foto());
            float x = posicaoAno(i);
            areasAnos[i] = new Rectangle2D.Float(x - 48, LINHA_Y - 22, 96, 66);
        }
    }

    private static BufferedImage lerFoto(String nome) {
        File arquivo = new File("assets/robos", nome);
        if (!arquivo.isFile()) {
            return null;
        }
        try {
            return ImageIO.read(arquivo);
        } catch (IOException erro) {
            return null;
        }
    }

    private float posicaoAno(int i) {
        if (robos.size() == 1) {
            return (LINHA_X0 + LINHA_X1) / 2;
        }
        return LINHA_X0 + (LINHA_X1 - LINHA_X0) * i / (robos.size() - 1);
    }

    private void selecionar(int i) {
        if (i != atual && i >= 0 && i < robos.size()) {
            direcao = i > atual ? 1 : -1;
            atual = i;
            mudouEm = t;
        }
    }

    @Override
    void desenharFundo(Graphics2D g, int w, int h) {
        // Mais escuro que as outras páginas: aqui o destaque é o robô sob o holofote.
        palco.recursos.desenharFundo(g, w, h, palco.relogio, 30, .84);
    }

    @Override
    void desenhar(Graphics2D g) {
        double entrada = Tema.etapa(t, 0, .5);
        desenharVitrine(g, entrada);
        Tema.alfa(g, entrada);
        desenharCabecalho(g, "VISUALIZAR ROBÔS", "Da primeira temporada até a atual");
        desenharSetas(g);
        RoundRectangle2D caixa = new RoundRectangle2D.Double(PAINEL_X - 30, 56, PAINEL_L + 60, 530,
                10, 10);
        g.setColor(Tema.PAINEL);
        g.fill(caixa);
        g.setColor(Tema.BORDA);
        g.setStroke(new BasicStroke(1));
        g.draw(caixa);
        desenharFicha(g, entrada);
        desenharLinhaDoTempo(g, entrada);
        Tema.alfa(g, 1);
    }

    private void desenharVitrine(Graphics2D g, double entrada) {
        Tema.alfa(g, entrada * (.8 + .06 * Math.sin(palco.relogio * 1.7)));
        Path2D cone = new Path2D.Double();
        cone.moveTo(VITRINE_X - 34, -10);
        cone.lineTo(VITRINE_X + 34, -10);
        cone.lineTo(VITRINE_X + 215, PISO_Y);
        cone.lineTo(VITRINE_X - 215, PISO_Y);
        cone.closePath();
        g.setPaint(new GradientPaint(0, 0, new Color(170, 255, 195, 75), 0, (float) PISO_Y,
                new Color(120, 255, 160, 8)));
        g.fill(cone);

        Tema.alfa(g, entrada);
        Rectangle2D luzNoPiso = new Rectangle2D.Double(VITRINE_X - 240, PISO_Y - 34, 480, 68);
        g.setPaint(new RadialGradientPaint(luzNoPiso, new float[] {0, 1},
                new Color[] {new Color(120, 255, 160, 80), new Color(120, 255, 160, 0)},
                MultipleGradientPaint.CycleMethod.NO_CYCLE));
        g.fill(new Ellipse2D.Double(VITRINE_X - 240, PISO_Y - 34, 480, 68));
        g.setColor(new Color(12, 32, 18));
        g.fill(new Ellipse2D.Double(VITRINE_X - 165, PISO_Y - 16, 330, 42));
        g.setColor(new Color(77, 255, 124, 130));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(new Ellipse2D.Double(VITRINE_X - 165, PISO_Y - 16, 330, 42));

        double e = Tema.suave(Tema.etapa(t, mudouEm, .5));
        double x = VITRINE_X + (1 - e) * direcao * 70;
        Tema.alfa(g, entrada * e);
        if (fotos[atual] != null) {
            desenharFoto(g, fotos[atual], x);
        } else {
            desenharRoboSemFoto(g, x);
        }
    }

    private void desenharFoto(Graphics2D g, BufferedImage foto, double cx) {
        double s = Math.min(FOTO_L / foto.getWidth(), FOTO_A / foto.getHeight());
        double l = foto.getWidth() * s;
        double a = foto.getHeight() * s;
        g.drawImage(foto, (int) (cx - l / 2), (int) (PISO_Y - a), (int) l, (int) a, null);
    }

    /** Silhueta usada enquanto a equipe não adiciona a foto da temporada. */
    private void desenharRoboSemFoto(Graphics2D g, double cx) {
        double base = PISO_Y - 2;
        Graphics2D r = (Graphics2D) g.create();
        r.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Color preenchimento = new Color(36, 78, 48, 150);
        Color linha = new Color(205, 255, 220, 215);

        for (double dx : new double[] {-105, 105}) {
            Ellipse2D roda = new Ellipse2D.Double(cx + dx - 30, base - 60, 60, 60);
            r.setColor(new Color(6, 18, 10));
            r.fill(roda);
            r.setColor(linha);
            r.draw(roda);
        }
        RoundRectangle2D chassi = new RoundRectangle2D.Double(cx - 145, base - 88, 290, 56, 10, 10);
        r.setColor(preenchimento);
        r.fill(chassi);
        r.setColor(linha);
        r.draw(chassi);

        Path2D torre = new Path2D.Double();
        torre.moveTo(cx - 62, base - 88);
        torre.lineTo(cx - 34, base - 270);
        torre.lineTo(cx + 2, base - 270);
        torre.lineTo(cx + 42, base - 88);
        torre.closePath();
        r.setColor(preenchimento);
        r.fill(torre);
        r.setColor(linha);
        r.draw(torre);
        r.draw(new Line2D.Double(cx - 42, base - 150, cx + 26, base - 150));
        r.draw(new Line2D.Double(cx - 34, base - 210, cx + 12, base - 210));

        Path2D braco = new Path2D.Double();
        braco.moveTo(cx - 16, base - 256);
        braco.lineTo(cx + 118, base - 300);
        braco.lineTo(cx + 150, base - 256);
        braco.lineTo(cx + 128, base - 236);
        r.setStroke(new BasicStroke(9, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        r.draw(braco);
        r.dispose();

        g.setFont(Tema.fonte(Font.PLAIN, 14));
        String aviso = "Ilustração: adicione a foto do robô desta temporada";
        Tema.texto(g, aviso, (float) (cx - Tema.largura(g, aviso, 0) / 2), (float) PISO_Y + 48, 0,
                Tema.APAGADO, null, 0);
    }

    private void desenharSetas(Graphics2D g) {
        desenharSeta(g, areaAnterior, -1, atual > 0);
        desenharSeta(g, areaProximo, 1, atual < robos.size() - 1);
    }

    private void desenharSeta(Graphics2D g, Rectangle2D area, int lado, boolean ativa) {
        if (!ativa) {
            return;
        }
        boolean foco = sobre(area);
        double cx = area.getCenterX();
        double cy = area.getCenterY();
        Path2D seta = new Path2D.Double();
        seta.moveTo(cx - lado * 11, cy - 26);
        seta.lineTo(cx + lado * 11, cy);
        seta.lineTo(cx - lado * 11, cy + 26);
        g.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(foco ? Tema.VERDE : Tema.TEXTO);
        g.draw(seta);
    }

    private void desenharFicha(Graphics2D g, double entrada) {
        double e = Tema.suave(Tema.etapa(t, mudouEm, .45));
        Conteudo.Robo robo = robos.get(atual);
        float x = PAINEL_X + (float) ((1 - e) * direcao * 24);
        Tema.alfa(g, entrada * e);

        g.setFont(Tema.fonte(Font.BOLD, 84));
        Tema.texto(g, String.valueOf(robo.temporada()), x, 142, 2, Tema.VERDE, Tema.VERDE, .8f);
        g.setFont(Tema.fonte(Font.BOLD, 19));
        Tema.texto(g, "TEMPORADA " + robo.jogo(), x + 2, 178, 2.5f, Tema.BRANCO, null, 0);
        g.setColor(Tema.BORDA);
        g.fill(new Rectangle2D.Float(x + 2, 198, PAINEL_L, 1));

        g.setFont(Tema.fonte(Font.BOLD, 30));
        Tema.texto(g, robo.nome(), x + 2, 246, .5f, Tema.BRANCO, null, 0);

        float y = 288;
        Font corpo = Tema.fonte(Font.PLAIN, 19);
        g.setFont(corpo);
        for (String linha : Tema.quebrar(palco.getFontMetrics(corpo), robo.descricao(), PAINEL_L)) {
            g.setColor(robo.descricao().startsWith("[") ? Tema.APAGADO : Tema.TEXTO);
            g.drawString(linha, x + 2, y);
            y += 28;
        }
        y += 14;
        for (String dado : robo.dados()) {
            g.setColor(Tema.VERDE);
            g.fill(new Rectangle2D.Float(x + 2, y - 11, 7, 7));
            g.setColor(Tema.TEXTO);
            g.drawString(dado, x + 22, y);
            y += 30;
        }
    }

    private void desenharLinhaDoTempo(Graphics2D g, double entrada) {
        Tema.alfa(g, entrada);
        g.setColor(new Color(255, 255, 255, 50));
        g.fill(new Rectangle2D.Float(LINHA_X0, LINHA_Y - .75f, LINHA_X1 - LINHA_X0, 1.5f));
        g.setFont(Tema.fonte(Font.BOLD, 19));
        for (int i = 0; i < robos.size(); i++) {
            float x = posicaoAno(i);
            boolean ativa = i == atual;
            boolean foco = sobre(areasAnos[i]);
            double raio = ativa ? 8 : 5;
            if (ativa) {
                g.setColor(new Color(77, 255, 124, 70));
                g.fill(new Ellipse2D.Double(x - 15, LINHA_Y - 15, 30, 30));
            }
            g.setColor(ativa || foco ? Tema.VERDE : Tema.APAGADO);
            g.fill(new Ellipse2D.Double(x - raio, LINHA_Y - raio, raio * 2, raio * 2));
            String ano = String.valueOf(robos.get(i).temporada());
            Tema.texto(g, ano, x - Tema.largura(g, ano, 0) / 2, LINHA_Y + 36, 0,
                    ativa ? Tema.BRANCO : foco ? Tema.VERDE : Tema.APAGADO, null, 0);
        }
    }

    @Override
    void clicou(double x, double y) {
        super.clicou(x, y);
        if (areaAnterior.contains(x, y)) {
            selecionar(atual - 1);
        } else if (areaProximo.contains(x, y)) {
            selecionar(atual + 1);
        }
        for (int i = 0; i < areasAnos.length; i++) {
            if (areasAnos[i].contains(x, y)) {
                selecionar(i);
            }
        }
    }

    @Override
    void tecla(int codigo) {
        super.tecla(codigo);
        if (codigo == KeyEvent.VK_LEFT) {
            selecionar(atual - 1);
        } else if (codigo == KeyEvent.VK_RIGHT) {
            selecionar(atual + 1);
        }
    }
}
