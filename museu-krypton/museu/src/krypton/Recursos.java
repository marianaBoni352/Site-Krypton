package krypton;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Prepara e desenha o plano de fundo: gradiente, névoa, partículas, foguete e o arco KRYPTON.
 * As imagens são geradas uma vez, em segundo plano, durante a tela de carregamento.
 *
 * <p>Para a animação ficar fluida, o ambiente (gradiente e névoa) é composto em baixa resolução,
 * e o logo vira uma única imagem pronta depois que a revelação termina.
 */
final class Recursos {

    private static final double ESCALA = 1.5;
    private static final double LOGO_X = 810;
    private static final double FOGUETE_Y = 24;
    private static final double FOGUETE_ESCALA = .95;
    private static final double ARCO_BASE_Y = 508;
    private static final double ARCO_RAIO = 900;
    private static final float ARCO_ESPACO = 24;
    private static final int AMBIENTE_L = 640;
    private static final int AMBIENTE_A = 360;
    /** A partir deste instante da revelação, o logo está completo e parado. */
    private static final double FIM_REVELACAO = 5.5;
    private static final double BRILHO_FIXO = .8;
    private static final double BRILHO_SUBTITULO = .55;

    private record Sprite(BufferedImage nucleo, double nx, double ny, double nl, double na,
            BufferedImage brilho, double bx, double by, double bl, double ba) {
    }

    volatile double progresso;

    private BufferedImage base;
    private BufferedImage nevoaA;
    private BufferedImage nevoaB;
    private BufferedImage ambiente;
    private Sprite foguete;
    private Sprite[] letras;
    private Sprite subtitulo;
    private final double[][] particulas = new double[80][5];

    private BufferedImage camadaLogo;
    private int camadaW;
    private int camadaH;
    private double camadaEscala;
    private int camadaX;
    private int camadaY;

    void carregar() {
        Random r = new Random(9487);
        ambiente = new BufferedImage(AMBIENTE_L, AMBIENTE_A, BufferedImage.TYPE_INT_RGB);
        base = gerarBase(r);
        progresso = .2;
        nevoaA = gerarNevoa(r, 4);
        progresso = .35;
        nevoaB = gerarNevoa(r, 7);
        progresso = .5;
        for (double[] p : particulas) {
            p[0] = r.nextDouble() * Palco.L;
            p[1] = r.nextDouble() * Palco.A;
            p[2] = .8 + r.nextDouble() * 2.2;
            p[3] = 3 + r.nextDouble() * 10;
            p[4] = r.nextDouble() * Math.PI * 2;
        }
        foguete = criarSprite(new Rectangle2D.Double(LOGO_X - 135, FOGUETE_Y - 8, 270, 405), g -> {
            g.translate(LOGO_X, FOGUETE_Y);
            g.scale(FOGUETE_ESCALA, FOGUETE_ESCALA);
            desenharFoguete(g);
        });
        progresso = .65;
        letras = criarLetras();
        progresso = .9;
        Font fonte = Tema.fonte(Font.BOLD, 33);
        Shape forma = centralizado(fonte, "FRC ROBOTICS TEAM", LOGO_X, 596);
        subtitulo = criarSprite(forma.getBounds2D(), g -> g.fill(forma));
        progresso = 1;
    }

    private static Shape centralizado(Font fonte, String s, double cx, double y) {
        FontRenderContext frc = new FontRenderContext(null, true, true);
        GlyphVector gv = fonte.createGlyphVector(frc, s);
        return gv.getOutline((float) (cx - gv.getLogicalBounds().getWidth() / 2), (float) y);
    }

    /** O foguete da equipe. A ponta fica em (0,0), com altura de 405 e largura de cerca de 260. */
    static void desenharFoguete(Graphics2D g) {
        Path2D meia = new Path2D.Double();
        meia.moveTo(-5, 6);
        meia.lineTo(-126, 238);
        meia.lineTo(-108, 250);
        meia.lineTo(-40, 188);
        meia.moveTo(-40, 168);
        meia.lineTo(-2, 134);
        meia.moveTo(-40, 168);
        meia.lineTo(-40, 212);
        meia.lineTo(-56, 224);

        BasicStroke traco = new BasicStroke(13, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        Shape esquerda = traco.createStrokedShape(meia);
        g.fill(esquerda);
        g.fill(AffineTransform.getScaleInstance(-1, 1).createTransformedShape(esquerda));

        Path2D centro = new Path2D.Double();
        centro.moveTo(-5, 8);
        centro.lineTo(5, 8);
        centro.lineTo(1.5, 205);
        centro.lineTo(-1.5, 205);
        centro.closePath();
        g.fill(centro);

        Path2D chama = new Path2D.Double();
        chama.moveTo(-100, 335);
        chama.lineTo(-50, 242);
        chama.lineTo(-28, 264);
        chama.lineTo(0, 218);
        chama.lineTo(28, 264);
        chama.lineTo(50, 242);
        chama.lineTo(100, 335);
        chama.lineTo(32, 286);
        chama.lineTo(0, 405);
        chama.lineTo(-32, 286);
        chama.closePath();
        g.fill(chama);
    }

    private Sprite[] criarLetras() {
        String nome = "KRYPTON";
        Font fonte = Tema.fonte(Font.BOLD, 128);
        FontRenderContext frc = new FontRenderContext(null, true, true);
        GlyphVector[] glifos = new GlyphVector[nome.length()];
        double total = ARCO_ESPACO * (nome.length() - 1);
        for (int i = 0; i < nome.length(); i++) {
            glifos[i] = fonte.createGlyphVector(frc, nome.substring(i, i + 1));
            total += glifos[i].getLogicalBounds().getWidth();
        }
        Sprite[] saida = new Sprite[nome.length()];
        double pos = -total / 2;
        BasicStroke arredondar = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        for (int i = 0; i < nome.length(); i++) {
            double avanco = glifos[i].getLogicalBounds().getWidth();
            AffineTransform at = new AffineTransform();
            at.translate(LOGO_X, ARCO_BASE_Y + ARCO_RAIO);
            at.rotate((pos + avanco / 2) / ARCO_RAIO);
            at.translate(-avanco / 2, -ARCO_RAIO);
            Shape forma = at.createTransformedShape(glifos[i].getOutline());
            saida[i] = criarSprite(forma.getBounds2D(), g -> {
                g.fill(forma);
                g.fill(arredondar.createStrokedShape(forma));
            });
            pos += avanco + ARCO_ESPACO;
            progresso = .65 + .25 * (i + 1) / nome.length();
        }
        return saida;
    }

    private static Sprite criarSprite(Rectangle2D limites, Consumer<Graphics2D> desenho) {
        double folgaNucleo = 4;
        double nx = limites.getX() - folgaNucleo;
        double ny = limites.getY() - folgaNucleo;
        double nl = limites.getWidth() + folgaNucleo * 2;
        double na = limites.getHeight() + folgaNucleo * 2;
        BufferedImage nucleo = rasterizar(nx, ny, nl, na, ESCALA, desenho);

        double folgaBrilho = 60;
        double bx = limites.getX() - folgaBrilho;
        double by = limites.getY() - folgaBrilho;
        double bl = limites.getWidth() + folgaBrilho * 2;
        double ba = limites.getHeight() + folgaBrilho * 2;
        BufferedImage reduzida = rasterizar(bx, by, bl, ba, ESCALA / 3, desenho);

        int bw = reduzida.getWidth();
        int bh = reduzida.getHeight();
        int[] px = reduzida.getRGB(0, 0, bw, bh, null, 0, bw);
        float[] curto = new float[px.length];
        for (int i = 0; i < px.length; i++) {
            curto[i] = (px[i] >>> 24) / 255f;
        }
        float[] longo = curto.clone();
        desfocar(curto, bw, bh, 3);
        desfocar(longo, bw, bh, 11);
        int cor = Tema.VERDE.getRGB() & 0xFFFFFF;
        for (int i = 0; i < px.length; i++) {
            float v = Math.min(1, curto[i] * .85f + longo[i] * 1.5f);
            px[i] = (Math.round(v * 255) << 24) | cor;
        }
        BufferedImage bruto = new BufferedImage(bw, bh, BufferedImage.TYPE_INT_ARGB);
        bruto.setRGB(0, 0, bw, bh, px, 0, bw);
        return new Sprite(nucleo, nx, ny, nl, na, premultiplicar(bruto), bx, by, bl, ba);
    }

    private static BufferedImage rasterizar(double x, double y, double l, double a, double escala,
            Consumer<Graphics2D> desenho) {
        BufferedImage img = new BufferedImage((int) Math.ceil(l * escala),
                (int) Math.ceil(a * escala), BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.scale(escala, escala);
        g.translate(-x, -y);
        g.setColor(Tema.BRANCO);
        desenho.accept(g);
        g.dispose();
        return img;
    }

    private static BufferedImage premultiplicar(BufferedImage origem) {
        BufferedImage saida = new BufferedImage(origem.getWidth(), origem.getHeight(),
                BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g = saida.createGraphics();
        g.drawImage(origem, 0, 0, null);
        g.dispose();
        return saida;
    }

    /** Três passadas de desfoque em caixa, uma boa aproximação de um desfoque gaussiano. */
    private static void desfocar(float[] v, int w, int h, int raio) {
        float[] aux = new float[v.length];
        for (int passada = 0; passada < 3; passada++) {
            for (int y = 0; y < h; y++) {
                caixa(v, aux, y * w, 1, w, raio);
            }
            for (int x = 0; x < w; x++) {
                caixa(aux, v, x, w, h, raio);
            }
        }
    }

    private static void caixa(float[] de, float[] para, int inicio, int passo, int n, int raio) {
        float soma = 0;
        float fator = 1f / (raio * 2 + 1);
        for (int i = -raio; i <= raio; i++) {
            soma += de[inicio + Math.max(0, Math.min(n - 1, i)) * passo];
        }
        for (int i = 0; i < n; i++) {
            para[inicio + i * passo] = soma * fator;
            soma += de[inicio + Math.min(n - 1, i + raio + 1) * passo]
                    - de[inicio + Math.max(0, i - raio) * passo];
        }
    }

    /** Gradiente verde atrás do logo, já escurecido à esquerda e embaixo para o texto se destacar. */
    private static BufferedImage gerarBase(Random r) {
        BufferedImage img = new BufferedImage(AMBIENTE_L, AMBIENTE_A, BufferedImage.TYPE_INT_RGB);
        double fatorX = (double) Palco.L / AMBIENTE_L;
        double fatorY = (double) Palco.A / AMBIENTE_A;
        double cx = LOGO_X / fatorX;
        double cy = .42 * AMBIENTE_A;
        double raio = .62 * AMBIENTE_L;
        for (int y = 0; y < AMBIENTE_A; y++) {
            double sombraBaixo = 1 - .78 * Tema.etapa(y * fatorY, 590, 130);
            for (int x = 0; x < AMBIENTE_L; x++) {
                double d = Math.min(1, Math.hypot(x - cx, (y - cy) * 1.15) / raio);
                double v = Math.pow(1 - d, 2.1);
                double sombra = sombraBaixo * (1 - .84 * (1 - Tema.etapa(x * fatorX, 60, 370)));
                double ruido = r.nextDouble() * 1.6 - .8;
                int red = (int) Math.max(0, (3 + 15 * v) * sombra + ruido);
                int green = (int) Math.max(0, (7 + 58 * v) * sombra + ruido);
                int blue = (int) Math.max(0, (5 + 25 * v) * sombra + ruido);
                img.setRGB(x, y, (red << 16) | (green << 8) | blue);
            }
        }
        return img;
    }

    /** Névoa contínua nas bordas, para poder deslizar sem emenda visível. */
    private static BufferedImage gerarNevoa(Random r, int celulas) {
        int w = 320;
        int h = 180;
        float[] campo = new float[w * h];
        double amplitude = 1;
        double soma = 0;
        for (int oitava = 0; oitava < 4; oitava++) {
            int cw = celulas << oitava;
            int ch = Math.max(2, cw * 9 / 16);
            double[] grade = new double[cw * ch];
            for (int i = 0; i < grade.length; i++) {
                grade[i] = r.nextDouble();
            }
            for (int y = 0; y < h; y++) {
                double gy = (double) y / h * ch;
                int y0 = (int) gy;
                double fy = gy - y0;
                fy = fy * fy * (3 - 2 * fy);
                for (int x = 0; x < w; x++) {
                    double gx = (double) x / w * cw;
                    int x0 = (int) gx;
                    double fx = gx - x0;
                    fx = fx * fx * (3 - 2 * fx);
                    double a = grade[y0 * cw + x0];
                    double b = grade[y0 * cw + (x0 + 1) % cw];
                    double c = grade[(y0 + 1) % ch * cw + x0];
                    double d = grade[(y0 + 1) % ch * cw + (x0 + 1) % cw];
                    double topo = a + (b - a) * fx;
                    double fundo = c + (d - c) * fx;
                    campo[y * w + x] += (float) (amplitude * (topo + (fundo - topo) * fy));
                }
            }
            soma += amplitude;
            amplitude *= .5;
        }
        int[] px = new int[w * h];
        for (int i = 0; i < px.length; i++) {
            double n = Tema.limitar(campo[i] / soma * 1.5 - .42);
            int a = (int) (n * n * 62);
            px[i] = (a << 24) | (120 << 16) | (225 << 8) | 150;
        }
        BufferedImage bruto = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        bruto.setRGB(0, 0, w, h, px, 0, w);
        return premultiplicar(bruto);
    }

    /**
     * Desenha o fundo inteiro.
     *
     * @param relogio tempo total, usado para os movimentos contínuos
     * @param r segundos desde o começo da revelação; define o que já apareceu
     * @param escuro de 0 a 1, quanto escurecer o fundo (páginas internas)
     */
    void desenharFundo(Graphics2D g0, int w, int h, double relogio, double r, double escuro) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        comporAmbiente(relogio, r, escuro);

        double cobrir = Math.max((double) w / Palco.L, (double) h / Palco.A);
        double zoom = 1 + .07 * (1 - Tema.suave(r / FIM_REVELACAO));
        AffineTransform dispositivo = g.getTransform();
        g.translate(w / 2.0, h / 2.0);
        g.scale(cobrir * zoom, cobrir * zoom);
        g.translate(-Palco.L / 2.0, -Palco.A / 2.0);

        double claro = 1 - escuro;
        Tema.alfa(g, 1);
        g.drawImage(ambiente, new AffineTransform((double) Palco.L / AMBIENTE_L, 0, 0,
                (double) Palco.A / AMBIENTE_A, 0, 0), null);

        if (foguete != null) {
            if (r >= FIM_REVELACAO) {
                AffineTransform mundo = g.getTransform();
                prepararCamadaLogo(w, h, dispositivo.getScaleX());
                g.setTransform(AffineTransform.getTranslateInstance(
                        dispositivo.getTranslateX() + camadaX,
                        dispositivo.getTranslateY() + camadaY));
                Tema.alfa(g, claro);
                g.drawImage(camadaLogo, 0, 0, null);
                g.setTransform(mundo);
                // A luz do foguete pulsa devagar sobre a camada pronta.
                double pulso = .5 + .5 * Math.sin(relogio * 1.3);
                desenharImagem(g, foguete.brilho, foguete.bx, foguete.by, foguete.bl, foguete.ba,
                        .28 * pulso * claro);
            } else {
                double e = Tema.suave(Tema.etapa(r, .7, 1.5));
                desenharSprite(g, foguete, (1 - e) * 60, e * claro, BRILHO_FIXO + (1 - e) * .5);
                for (int i = 0; i < letras.length; i++) {
                    double el = Tema.suave(Tema.etapa(r, 1.7 + i * .09, .55));
                    desenharSprite(g, letras[i], (1 - el) * -22, el * claro, BRILHO_FIXO);
                }
                double es = Tema.suave(Tema.etapa(r, 2.4, .7));
                desenharSprite(g, subtitulo, (1 - es) * 12, es * claro, BRILHO_SUBTITULO);
            }
        }
        desenharParticulas(g, relogio, Tema.etapa(r, 1, 2) * (.35 + .65 * claro));
        g.dispose();
    }

    /** Compõe gradiente e névoa em baixa resolução: como são suaves, ampliar não prejudica. */
    private void comporAmbiente(double relogio, double r, double escuro) {
        Graphics2D a = ambiente.createGraphics();
        a.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        a.drawImage(base, 0, 0, null);
        double surgir = Tema.etapa(r, .3, 2.2);
        desenharNevoa(a, nevoaA, relogio * 6, surgir);
        desenharNevoa(a, nevoaB, -relogio * 9, .8 * surgir);
        double preto = 1 - Tema.etapa(r, 0, 1.6) * (1 - escuro);
        if (preto > 0) {
            Tema.alfa(a, preto);
            a.setColor(Color.BLACK);
            a.fillRect(0, 0, AMBIENTE_L, AMBIENTE_A);
        }
        a.dispose();
    }

    private static void desenharNevoa(Graphics2D g, BufferedImage img, double deslocamento,
            double a) {
        if (a <= 0) {
            return;
        }
        Tema.alfa(g, a);
        double x = deslocamento % AMBIENTE_L;
        if (x > 0) {
            x -= AMBIENTE_L;
        }
        double ex = (double) AMBIENTE_L / img.getWidth();
        double ey = (double) AMBIENTE_A / img.getHeight();
        g.drawImage(img, new AffineTransform(ex, 0, 0, ey, x, 0), null);
        g.drawImage(img, new AffineTransform(ex, 0, 0, ey, x + AMBIENTE_L, 0), null);
    }

    /** Junta o logo completo em uma só imagem, na resolução real da janela. */
    private void prepararCamadaLogo(int w, int h, double escalaTela) {
        if (camadaLogo != null && camadaW == w && camadaH == h && camadaEscala == escalaTela) {
            return;
        }
        camadaW = w;
        camadaH = h;
        camadaEscala = escalaTela;

        double cobrir = Math.max((double) w / Palco.L, (double) h / Palco.A);
        AffineTransform mundo = AffineTransform.getScaleInstance(escalaTela, escalaTela);
        mundo.translate(w / 2.0, h / 2.0);
        mundo.scale(cobrir, cobrir);
        mundo.translate(-Palco.L / 2.0, -Palco.A / 2.0);

        Rectangle2D area = new Rectangle2D.Double(foguete.bx, foguete.by, foguete.bl, foguete.ba);
        area = area.createUnion(new Rectangle2D.Double(subtitulo.bx, subtitulo.by, subtitulo.bl,
                subtitulo.ba));
        for (Sprite s : letras) {
            area = area.createUnion(new Rectangle2D.Double(s.bx, s.by, s.bl, s.ba));
        }
        Rectangle2D naTela = mundo.createTransformedShape(area).getBounds2D();
        camadaX = (int) Math.floor(naTela.getX());
        camadaY = (int) Math.floor(naTela.getY());
        camadaLogo = new BufferedImage((int) Math.ceil(naTela.getWidth()) + 2,
                (int) Math.ceil(naTela.getHeight()) + 2, BufferedImage.TYPE_INT_ARGB_PRE);

        Graphics2D g = camadaLogo.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.translate(-camadaX, -camadaY);
        g.transform(mundo);
        desenharSprite(g, foguete, 0, 1, BRILHO_FIXO);
        for (Sprite letra : letras) {
            desenharSprite(g, letra, 0, 1, BRILHO_FIXO);
        }
        desenharSprite(g, subtitulo, 0, 1, BRILHO_SUBTITULO);
        g.dispose();
    }

    private void desenharParticulas(Graphics2D g, double relogio, double a) {
        if (a <= 0) {
            return;
        }
        g.setColor(Tema.VERDE);
        for (double[] p : particulas) {
            double y = (p[1] - relogio * p[3]) % (Palco.A + 20);
            if (y < -10) {
                y += Palco.A + 20;
            }
            double x = p[0] + Math.sin(relogio * .3 + p[4]) * 14;
            double cintila = .35 + .4 * Math.sin(relogio * .9 + p[4]);
            Tema.alfa(g, a * Math.max(.08, cintila) * .35);
            g.fill(new Ellipse2D.Double(x - p[2] * 2, y - p[2] * 2, p[2] * 4, p[2] * 4));
            Tema.alfa(g, a * Math.max(.1, cintila));
            g.fill(new Ellipse2D.Double(x - p[2] / 2, y - p[2] / 2, p[2], p[2]));
        }
    }

    private static void desenharSprite(Graphics2D g, Sprite s, double dy, double a, double brilho) {
        if (a <= 0) {
            return;
        }
        desenharImagem(g, s.brilho, s.bx, s.by + dy, s.bl, s.ba, a * Math.min(1, brilho));
        desenharImagem(g, s.nucleo, s.nx, s.ny + dy, s.nl, s.na, a);
    }

    private static void desenharImagem(Graphics2D g, BufferedImage img, double x, double y, double l,
            double a, double opacidade) {
        Tema.alfa(g, opacidade);
        g.drawImage(img, new AffineTransform(l / img.getWidth(), 0, 0, a / img.getHeight(), x, y),
                null);
    }
}
