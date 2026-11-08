package krypton;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.net.URI;
import javax.swing.JComponent;
import javax.swing.Timer;

/**
 * Área única de desenho do programa: controla o relógio, a tela atual, as transições e a entrada.
 * As telas trabalham sempre em um espaço virtual de 1280x720, ajustado ao tamanho da janela.
 */
final class Palco extends JComponent {

    static final int L = 1280;
    static final int A = 720;
    private static final double MEIA_TRANSICAO = .35;

    final Recursos recursos = new Recursos();
    double relogio;
    /** Posição atual do mouse, em coordenadas virtuais. */
    double mouseX = -1;
    double mouseY = -1;
    /** Cada tela liga esta opção ao desenhar se o mouse estiver sobre algo clicável. */
    boolean mao;

    private Tela atual;
    private Tela proxima;
    private TelaMenu menu;
    private double transicao = -1;
    private String aviso;
    private double avisoAte;
    private long ultimoQuadro;
    private final Timer temporizador = new Timer(16, e -> quadro());

    Palco() {
        setOpaque(true);
        setBackground(Color.BLACK);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                atualizarMouse(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                atualizarMouse(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                atualizarMouse(e);
                if (e.getButton() == MouseEvent.BUTTON1 && transicao < 0) {
                    atual.clicou(mouseX, mouseY);
                }
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (transicao < 0) {
                    atual.rolou(e.getPreciseWheelRotation());
                }
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
        addMouseWheelListener(mouse);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (transicao < 0) {
                    atual.tecla(e.getKeyCode());
                }
            }
        });
    }

    void iniciar() {
        atual = new TelaCarregamento(this);
        Thread carga = new Thread(recursos::carregar, "carregar-recursos");
        carga.setDaemon(true);
        carga.start();
        ultimoQuadro = System.nanoTime();
        temporizador.start();
    }

    void abrir(Tela nova) {
        if (transicao < 0) {
            proxima = nova;
            transicao = 0;
        }
    }

    void abrirMenu() {
        if (menu == null) {
            menu = new TelaMenu(this);
        }
        abrir(menu);
    }

    void abrirLink(String link) {
        if (link == null || link.isBlank()) {
            avisar("Este link ainda não foi configurado.");
            return;
        }
        try {
            Desktop.getDesktop().browse(new URI(link));
        } catch (Exception erro) {
            avisar("Não foi possível abrir o link.");
        }
    }

    void avisar(String texto) {
        aviso = texto;
        avisoAte = relogio + 2.6;
    }

    private void quadro() {
        long agora = System.nanoTime();
        double dt = Math.min(.1, (agora - ultimoQuadro) / 1e9);
        ultimoQuadro = agora;
        relogio += dt;
        if (transicao >= 0) {
            double antes = transicao;
            transicao += dt;
            if (antes < MEIA_TRANSICAO && transicao >= MEIA_TRANSICAO) {
                atual = proxima;
                proxima = null;
            }
            if (transicao >= MEIA_TRANSICAO * 2) {
                transicao = -1;
            }
        }
        atual.t += dt;
        atual.avancar();
        repaint();
    }

    private void atualizarMouse(MouseEvent e) {
        double s = escala();
        mouseX = (e.getX() - (getWidth() - L * s) / 2) / s;
        mouseY = (e.getY() - (getHeight() - A * s) / 2) / s;
    }

    private double escala() {
        return Math.min((double) getWidth() / L, (double) getHeight() / A);
    }

    /** Borda inferior da janela em coordenadas virtuais: passa de A se a janela for mais alta que 16:9. */
    double fundoVirtual() {
        double s = escala();
        return A + (getHeight() - A * s) / 2 / s;
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        int w = getWidth();
        int h = getHeight();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, w, h);
        if (atual != null && w > 0 && h > 0) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                    RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            atual.desenharFundo(g, w, h);

            AffineTransform original = g.getTransform();
            double s = escala();
            g.translate((w - L * s) / 2, (h - A * s) / 2);
            g.scale(s, s);
            mao = false;
            atual.desenhar(g);
            desenharAviso(g);
            g.setTransform(original);
            setCursor(Cursor.getPredefinedCursor(mao ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));

            if (transicao >= 0) {
                double a = transicao < MEIA_TRANSICAO
                        ? transicao / MEIA_TRANSICAO
                        : 2 - transicao / MEIA_TRANSICAO;
                Tema.alfa(g, a);
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, w, h);
            }
        }
        g.dispose();
    }

    private void desenharAviso(Graphics2D g) {
        if (aviso == null || relogio > avisoAte) {
            return;
        }
        Tema.alfa(g, Tema.limitar((avisoAte - relogio) * 3));
        g.setFont(Tema.fonte(Font.PLAIN, 17));
        float largura = Tema.largura(g, aviso, 0);
        g.setColor(new Color(5, 20, 10, 235));
        g.fill(new RoundRectangle2D.Double(L / 2.0 - largura / 2 - 22, 618, largura + 44, 42, 8, 8));
        g.setColor(Tema.BORDA);
        g.draw(new RoundRectangle2D.Double(L / 2.0 - largura / 2 - 22, 618, largura + 44, 42, 8, 8));
        g.setColor(Tema.BRANCO);
        g.drawString(aviso, L / 2f - largura / 2, 645);
        Tema.alfa(g, 1);
    }
}
