package krypton;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/** Ponto de entrada do museu digital da equipe Krypton FRC #9487. */
public final class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame janela = new JFrame("Krypton FRC #9487");
            janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            janela.getContentPane().setBackground(Color.BLACK);
            janela.setMinimumSize(new Dimension(960, 540));

            Palco palco = new Palco();
            janela.setContentPane(palco);
            janela.setSize(1280, 760);
            janela.setLocationRelativeTo(null);
            janela.setExtendedState(JFrame.MAXIMIZED_BOTH);
            janela.setVisible(true);
            palco.requestFocusInWindow();
            palco.iniciar();
        });
    }
}
