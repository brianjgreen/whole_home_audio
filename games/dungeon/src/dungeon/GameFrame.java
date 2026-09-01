package dungeon;

import javax.swing.*;

public class GameFrame extends JFrame {

    public GameFrame() {
        setTitle("Dungeon Crawler");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(true);

        GamePanel panel = new GamePanel();
        add(panel);
        pack();

        setLocationRelativeTo(null);
        setMinimumSize(getSize());
    }
}
