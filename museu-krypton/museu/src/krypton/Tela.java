package krypton;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;

/** Base de todas as telas. As coordenadas de desenho e de mouse são virtuais (1280x720). */
abstract class Tela {

    protected final Palco palco;
    /** Segundos desde que a tela foi aberta. */
    double t;

    private final Rectangle2D areaVoltar = new Rectangle2D.Double(52, 28, 130, 40);

    Tela(Palco palco) {
        this.palco = palco;
    }

    /** Páginas internas: fundo já revelado e escurecido. */
    void desenharFundo(Graphics2D g, int w, int h) {
        palco.recursos.desenharFundo(g, w, h, palco.relogio, 30, .58);
    }

    abstract void desenhar(Graphics2D g);

    void avancar() {
    }

    void clicou(double x, double y) {
        if (areaVoltar.contains(x, y)) {
            palco.abrirMenu();
        }
    }

    void rolou(double quanto) {
    }

    void tecla(int codigo) {
        if (codigo == KeyEvent.VK_ESCAPE || codigo == KeyEvent.VK_BACK_SPACE) {
            palco.abrirMenu();
        }
    }

    protected boolean sobre(Rectangle2D area) {
        boolean dentro = area.contains(palco.mouseX, palco.mouseY);
        palco.mao |= dentro;
        return dentro;
    }

    protected void desenharCabecalho(Graphics2D g, String titulo, String subtitulo) {
        boolean foco = sobre(areaVoltar);
        g.setFont(Tema.fonte(Font.BOLD, 17));
        Tema.texto(g, "‹  VOLTAR", 64, 54, 2, foco ? Tema.VERDE : Tema.APAGADO, null, 0);

        g.setFont(Tema.fonte(Font.BOLD, 34));
        Tema.texto(g, titulo, 64, 118, 1.5f, Tema.BRANCO, Tema.VERDE, .9f);
        g.setFont(Tema.fonte(Font.PLAIN, 16));
        Tema.texto(g, subtitulo, 64, 146, .5f, Tema.VERDE, null, 0);
        g.setColor(new Color(77, 255, 124, 90));
        g.fillRect(64, 162, 56, 2);
    }
}
