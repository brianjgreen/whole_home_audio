package com.gameoflife;

import java.io.Console;
import java.util.concurrent.atomic.AtomicReference;

public class Main {

    public static void main(String[] args) throws Exception {
        Console console = System.console();
        if (console == null) {
            System.err.println("A console is required to run this application.");
            System.err.println("Please run from a terminal.");
            System.exit(1);
        }

        var catalog = new PatternCatalog();
        var renderer = new TerminalRenderer();
        var game = new GameOfLife(Grid.glider());
        var currentPattern = new AtomicReference<>("Glider");

        renderer.hideCursor();
        renderer.clearScreen();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            renderer.showCursor();
            System.out.print("\u001b[?25h\u001b[0m\u001b[2J\u001b[H");
            System.out.flush();
        }));

        Thread gameLoop = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                if (game.isRunning()) {
                    game.step();
                }
                renderer.render(game.grid(), currentPattern.get());
                try {
                    Thread.sleep(game.delayMs());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "game-loop");
        gameLoop.setDaemon(true);
        gameLoop.start();

        while (true) {
            var reader = console.reader();
            int ch = reader.read();
            if (ch == -1) break;

            char c = Character.toLowerCase((char) ch);
            switch (c) {
                case 'q' -> {
                    gameLoop.interrupt();
                    renderer.showCursor();
                    renderer.clearScreen();
                    System.out.println("\u001b[92mThanks for playing Conway's Game of Life!\u001b[0m\n");
                    System.exit(0);
                }
                case ' ' -> game.toggleRunning();
                case 'r' -> {
                    Grid g = new Grid(20, 40);
                    g.randomize(0.30);
                    game.setGrid(g);
                    currentPattern.set("Random (30%)");
                }
                case 'c' -> {
                    game.setGrid(new Grid(20, 40));
                    currentPattern.set("Empty");
                }
                case 's' -> game.setDelayMs(Math.max(20, game.delayMs() - 20));
                case 'f' -> game.setDelayMs(Math.min(500, game.delayMs() + 20));
                case 'g' -> {
                    String[] names = catalog.names();
                    int idx = 0;
                    for (int i = 0; i < names.length; i++) {
                        if (names[i].equals(currentPattern.get())) {
                            idx = (i + 1) % names.length;
                            break;
                        }
                    }
                    currentPattern.set(names[idx]);
                    game.setGrid(catalog.create(names[idx]));
                }
                default -> {
                    if (c >= '1' && c <= '5') {
                        int idx = c - '1';
                        String[] names = catalog.names();
                        if (idx < names.length) {
                            currentPattern.set(names[idx]);
                            game.setGrid(catalog.create(names[idx]));
                        }
                    }
                }
            }
        }
    }
}
