package com.gameoflife;

public class TerminalRenderer {

    private static final String ANSI_RESET = "\u001b[0m";
    private static final String ANSI_BOLD = "\u001b[1m";
    private static final String ANSI_FG_GREEN = "\u001b[32m";
    private static final String ANSI_FG_BRIGHT_GREEN = "\u001b[92m";
    private static final String ANSI_FG_RED = "\u001b[31m";
    private static final String ANSI_FG_YELLOW = "\u001b[33m";
    private static final String ANSI_FG_CYAN = "\u001b[36m";
    private static final String ANSI_FG_WHITE = "\u001b[37m";
    private static final String ANSI_FG_DIM = "\u001b[2m";
    private static final String ANSI_BG_BLACK = "\u001b[40m";
    private static final String ANSI_CLEAR = "\u001b[2J";
    private static final String ANSI_HOME = "\u001b[H";
    private static final String ANSI_HIDE_CURSOR = "\u001b[?25l";
    private static final String ANSI_SHOW_CURSOR = "\u001b[?25h";

    public void clearScreen() {
        System.out.print(ANSI_CLEAR + ANSI_HOME);
        System.out.flush();
    }

    public void hideCursor() {
        System.out.print(ANSI_HIDE_CURSOR);
        System.out.flush();
    }

    public void showCursor() {
        System.out.print(ANSI_SHOW_CURSOR);
        System.out.flush();
    }

    public void render(Grid grid, String patternName) {
        System.out.print(ANSI_HOME);

        System.out.println(ANSI_BOLD + ANSI_FG_CYAN
                + "  ╔══════════════════════════════════════════════════╗" + ANSI_RESET);
        System.out.println(ANSI_BOLD + ANSI_FG_CYAN
                + "  ║        CONWAY'S GAME OF LIFE  (Java 25)         ║" + ANSI_RESET);
        System.out.println(ANSI_BOLD + ANSI_FG_CYAN
                + "  ╚══════════════════════════════════════════════════╝" + ANSI_RESET);
        System.out.println();

        System.out.println(ANSI_FG_YELLOW + "  Pattern: " + ANSI_FG_WHITE + patternName
                + ANSI_FG_DIM + "  │  " + ANSI_RESET
                + ANSI_FG_YELLOW + "Generation: " + ANSI_FG_WHITE + grid.generation()
                + ANSI_FG_DIM + "  │  " + ANSI_RESET
                + ANSI_FG_YELLOW + "Population: " + ANSI_FG_WHITE + grid.population()
                + ANSI_RESET);
        System.out.println(ANSI_FG_DIM
                + "  ─────────────────────────────────────────────────────"
                + ANSI_RESET);

        StringBuilder sb = new StringBuilder();
        sb.append('\n');

        for (int r = 0; r < grid.rows(); r++) {
            sb.append("  ");
            for (int c = 0; c < grid.cols(); c++) {
                if (grid.isAlive(r, c)) {
                    int neighbors = grid.countNeighbors(r, c);
                    sb.append(switch (neighbors) {
                        case 0, 1 -> ANSI_FG_RED;
                        case 2 -> ANSI_FG_GREEN;
                        case 3 -> ANSI_FG_BRIGHT_GREEN;
                        case 4 -> ANSI_FG_YELLOW;
                        default -> ANSI_FG_RED;
                    });
                    sb.append("██");
                    sb.append(ANSI_RESET);
                } else {
                    sb.append(ANSI_FG_DIM);
                    sb.append("░░");
                    sb.append(ANSI_RESET);
                }
            }
            sb.append('\n');
        }

        System.out.print(sb);

        System.out.println(ANSI_FG_DIM
                + "  ─────────────────────────────────────────────────────"
                + ANSI_RESET);
        System.out.println(ANSI_FG_CYAN + "  Commands:" + ANSI_RESET
                + ANSI_FG_WHITE + " [Space] pause/resume  [→] step  [R] randomize"
                + ANSI_RESET);
        System.out.println(ANSI_FG_CYAN + "           " + ANSI_RESET
                + ANSI_FG_WHITE + "[G] patterns  [C] clear  [Q] quit" + ANSI_RESET);
        System.out.flush();
    }
}
