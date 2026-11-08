package krypton;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Cores, fontes e utilitários de desenho e animação usados em todas as telas. */
final class Tema {

    static final Color VERDE = new Color(0x4DFF7C);
    static final Color VERDE_MEDIO = new Color(0x2FBF55);
    static final Color BRANCO = new Color(0xF4FFF6);
    static final Color TEXTO = new Color(0xDCE8DF);
    static final Color APAGADO = new Color(0x93A99A);
    static final Color PAINEL = new Color(4, 14, 7, 205);
    static final Color BORDA = new Color(77, 255, 124, 70);

    private static final String FAMILIA = escolherFamilia();
    private static final Map<String, Rotulo> ROTULOS = new HashMap<>();

    private Tema() {
    }

    private static String escolherFamilia() {
        Set<String> instaladas = Set.of(GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames());
        for (String nome : new String[] {"Bahnschrift", "Rajdhani", "Segoe UI"}) {
            if (instaladas.contains(nome)) {
                return nome;
            }
        }
        return Font.SANS_SERIF;
    }

    static Font fonte(int estilo, float tamanho) {
        return new Font(FAMILIA, estilo, 12).deriveFont(estilo, tamanho);
    }

    // ---- animação ----------------------------------------------------------

    static double limitar(double v) {
        return Math.max(0, Math.min(1, v));
    }

    /** Progresso de 0 a 1 de uma etapa que começa em {@code inicio} e dura {@code duracao}. */
    static double etapa(double t, double inicio, double duracao) {
        return limitar((t - inicio) / duracao);
    }

    static double suave(double v) {
        double x = 1 - limitar(v);
        return 1 - x * x * x;
    }

    static void alfa(Graphics2D g, double a) {
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) limitar(a)));
    }

    // ---- texto -------------------------------------------------------------

    private record Rotulo(BufferedImage imagem, int dx, int dy, float largura) {
    }

    private static Path2D contornar(Font f, String s, float espaco, float[] largura) {
        FontRenderContext frc = new FontRenderContext(null, true, true);
        Path2D contorno = new Path2D.Float();
        float px = 0;
        for (int i = 0; i < s.length(); i++) {
            GlyphVector gv = f.createGlyphVector(frc, s.substring(i, i + 1));
            contorno.append(gv.getOutline(px, 0), false);
            px += (float) gv.getLogicalBounds().getWidth() + espaco;
        }
        largura[0] = px - espaco;
        return contorno;
    }

    /**
     * Texto com espaçamento entre letras e brilho opcional. Devolve a largura desenhada.
     * Cada combinação é desenhada uma vez e reaproveitada; para valores que mudam a cada quadro,
     * use {@link #textoAnimado}.
     */
    static float texto(Graphics2D g, String s, float x, float y, float espaco, Color cor,
            Color brilho, float forca) {
        AffineTransform at = g.getTransform();
        double escala = at.getScaleX();
        Font f = g.getFont();
        String chave = f.getFontName() + '|' + f.getSize2D() + '|' + s + '|' + espaco + '|'
                + cor.getRGB() + '|' + (brilho == null ? 0 : brilho.getRGB()) + '|' + forca + '|'
                + escala;
        Rotulo r = ROTULOS.get(chave);
        if (r == null) {
            if (ROTULOS.size() > 800) {
                ROTULOS.clear();
            }
            r = criarRotulo(f, s, espaco, cor, brilho, forca, escala);
            ROTULOS.put(chave, r);
        }
        g.setTransform(AffineTransform.getTranslateInstance(
                Math.round(at.getTranslateX() + x * escala) + r.dx(),
                Math.round(at.getTranslateY() + y * at.getScaleY()) + r.dy()));
        g.drawImage(r.imagem(), 0, 0, null);
        g.setTransform(at);
        return r.largura();
    }

    private static Rotulo criarRotulo(Font f, String s, float espaco, Color cor, Color brilho,
            float forca, double escala) {
        float[] largura = new float[1];
        Path2D contorno = contornar(f, s, espaco, largura);
        Rectangle2D limites = contorno.getBounds2D();
        double folga = 12;
        int dx = (int) Math.floor((limites.getX() - folga) * escala);
        int dy = (int) Math.floor((limites.getY() - folga) * escala);
        BufferedImage img = new BufferedImage(
                Math.max(1, (int) Math.ceil((limites.getWidth() + folga * 2) * escala) + 1),
                Math.max(1, (int) Math.ceil((limites.getHeight() + folga * 2) * escala) + 1),
                BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.translate(-dx, -dy);
        g.scale(escala, escala);
        pintarContorno(g, contorno, cor, brilho, forca);
        g.dispose();
        return new Rotulo(img, dx, dy, largura[0]);
    }

    /** Desenha o texto diretamente, para animações em que espaçamento ou brilho mudam. */
    static float textoAnimado(Graphics2D g, String s, float x, float y, float espaco, Color cor,
            Color brilho, float forca) {
        float[] largura = new float[1];
        Path2D contorno = contornar(g.getFont(), s, espaco, largura);
        contorno.transform(AffineTransform.getTranslateInstance(x, y));
        pintarContorno(g, contorno, cor, brilho, forca);
        return largura[0];
    }

    private static void pintarContorno(Graphics2D g, Path2D contorno, Color cor, Color brilho,
            float forca) {
        if (brilho != null && forca > 0) {
            for (float[] camada : new float[][] {{12, .05f}, {7, .09f}, {3, .16f}}) {
                g.setColor(new Color(brilho.getRed(), brilho.getGreen(), brilho.getBlue(),
                        Math.round(255 * Math.min(1, camada[1] * forca))));
                g.setStroke(new BasicStroke(camada[0], BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND));
                g.draw(contorno);
            }
        }
        g.setColor(cor);
        g.fill(contorno);
    }

    static float largura(Graphics2D g, String s, float espaco) {
        return (float) g.getFont().getStringBounds(s, g.getFontRenderContext()).getWidth()
                + espaco * Math.max(0, s.length() - 1);
    }

    static List<String> quebrar(FontMetrics fm, String texto, int larguraMax) {
        List<String> linhas = new ArrayList<>();
        StringBuilder atual = new StringBuilder();
        for (String palavra : texto.split(" ")) {
            String tentativa = atual.isEmpty() ? palavra : atual + " " + palavra;
            if (fm.stringWidth(tentativa) > larguraMax && !atual.isEmpty()) {
                linhas.add(atual.toString());
                atual = new StringBuilder(palavra);
            } else {
                atual = new StringBuilder(tentativa);
            }
        }
        if (!atual.isEmpty()) {
            linhas.add(atual.toString());
        }
        return linhas;
    }
}
