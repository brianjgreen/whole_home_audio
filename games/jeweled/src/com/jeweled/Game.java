package com.jeweled;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Game {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Game::createAndShow);
    }

    private static void createAndShow() {
        JFrame frame = new JFrame("Jeweled - Color Blind Friendly");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        GamePanel panel = new GamePanel();
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
