package com.jeweled;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;

/**
 * Renders the board and handles mouse interaction + animation.
 *
 * The board is drawn in screen coordinates where y increases downward,
 * matching the model where row 0 is the top row.
 */
public class GamePanel extends JPanel implements Runnable {

    private static final int CELL = 54;
    private static final int PAD = 10;
    private static final int TOP_BAR = 60;
    private static final int ROWS = Board.ROWS;
    private static final int COLS = Board.COLS;

    private final Board board = new Board();

    private int selRow = -1, selCol = -1;
    private boolean mouseDown;
    private int pressRow = -1, pressCol = -1;
    private boolean gameOver;
    private volatile boolean running = true;

    private String status = "Click two adjacent gems to swap them.";

    public GamePanel() {
        setPreferredSize(new Dimension(
                PAD * 2 + COLS * CELL,
                TOP_BAR + PAD * 2 + ROWS * CELL));

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseDown = true;
                int[] cell = toCell(e.getX(), e.getY());
                if (cell == null) { pressRow = -1; pressCol = -1; return; }
                pressRow = cell[0];
                pressCol = cell[1];
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!mouseDown) { pressRow = -1; pressCol = -1; return; }
                mouseDown = false;
                if (gameOver) {
                    if (pressRow >= 0) {
                        board.reshuffle();
                        board.cleanNewMatches();
                        gameOver = false;
                        selRow = -1; selCol = -1;
                        setStatus("Board reshuffled. Good luck!");
                    }
                    pressRow = -1; pressCol = -1;
                    return;
                }
                int[] cell = toCell(e.getX(), e.getY());
                int relRow = pressRow;
                int relCol = pressCol;
                pressRow = -1; pressCol = -1;
                if (cell == null || relRow < 0) return;
                handleClick(relRow, relCol, cell[0], cell[1]);
            }
        };
        addMouseListener(ma);

        Thread th = new Thread(this, "game-loop");
        th.setDaemon(true);
        th.start();
    }

    private void setStatus(String s) {
        status = s;
    }

    private int[] toCell(int px, int py) {
        int x = px - PAD;
        int y = py - TOP_BAR - PAD;
        if (x < 0 || y < 0) return null;
        int c = x / CELL;
        int r = y / CELL;
        if (r >= 0 && r < ROWS && c >= 0 && c < COLS) return new int[]{r, c};
        return null;
    }

    private void handleClick(int r1, int c1, int r2, int c2) {
        if (r1 == r2 && c1 == c2) {
            // toggle selection
            if (selRow == r1 && selCol == c1) {
                selRow = -1; selCol = -1;
            } else {
                selRow = r1; selCol = c1;
                setStatus("Now click an adjacent gem to swap.");
            }
            return;
        }
        // If a gem is selected and the click is on its neighbor, swap.
        if (selRow == r1 && selCol == c1 && board.isAdjacent(r1, c1, r2, c2)) {
            attemptMove(r1, c1, r2, c2);
            selRow = -1; selCol = -1;
            return;
        }
        // treat as fresh selection of the second cell
        selRow = r2; selCol = c2;
        if (board.isAdjacent(r1, c1, r2, c2)) {
            attemptMove(r1, c1, r2, c2);
            selRow = -1; selCol = -1;
        } else {
            setStatus("Tap the first gem, then its neighbor. (Or just tap two adjacent gems.)");
        }
    }

    private void attemptMove(int r1, int c1, int r2, int c2) {
        if (board.swapAndMatch(r1, c1, r2, c2)) {
            setStatus("Nice! Score: " + board.getScore());
        } else {
            setStatus("That swap doesn't make a match.");
        }
        checkGameOver();
    }

    private void checkGameOver() {
        if (!gameOver && !board.hasAnyMove()) {
            gameOver = true;
            setStatus("No more moves! Score: " + board.getScore() + "  (click board to reshuffle)");
        }
    }

    @Override
    public void run() {
        while (running) {
            repaint();
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(0x14141A));
        g2.fillRect(0, 0, getWidth(), getHeight());

        drawBoardFrame(g2);
        drawGems(g2);
        drawSelection(g2);
        drawStatusBar(g2);

        g2.dispose();
    }

    private void drawBoardFrame(Graphics2D g2) {
        int x = PAD;
        int y = TOP_BAR + PAD;
        g2.setColor(new Color(0x2A2A33));
        g2.fillRoundRect(x - 4, y - 4, COLS * CELL + 8, ROWS * CELL + 8, 12, 12);
        g2.setColor(new Color(0x464650));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(x - 4, y - 4, COLS * CELL + 8, ROWS * CELL + 8, 12, 12);
    }

    private void drawGems(Graphics2D g2) {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                GemType t = board.get(r, c);
                if (t == null) continue;
                int px = PAD + c * CELL;
                int py = TOP_BAR + PAD + r * CELL;
                drawGemShape(g2, t, px, py, CELL);
            }
        }
    }

    private void drawGemShape(Graphics2D g2, GemType t, int px, int py, int size) {
        double cx = px + size / 2.0;
        double cy = py + size / 2.0;
        double d = size * 0.84;

        Ellipse2D bg = new Ellipse2D.Double(cx - d / 2, cy - d / 2, d, d);
        GradientPaint grad = new GradientPaint((float) cx, (float) (cy - d / 2),
                t.getColor().brighter(), (float) cx, (float) (cy + d / 2), t.getColor().darker());
        g2.setPaint(grad);
        g2.fill(bg);

        g2.setColor(t.getDarkColor());
        g2.draw(bg);

        g2.setStroke(new BasicStroke((float) (size * 0.09), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        t.drawSymbol(g2, cx, cy, size);
    }

    private void drawSelection(Graphics2D g2) {
        if (selRow < 0 || selCol < 0) return;
        int px = PAD + selCol * CELL;
        int py = TOP_BAR + PAD + selRow * CELL;
        g2.setColor(new Color(255, 255, 255, 220));
        g2.setStroke(new BasicStroke(3f));
        g2.drawRoundRect(px + 2, py + 2, CELL - 4, CELL - 4, 10, 10);
    }

    private void drawStatusBar(Graphics2D g2) {
        int y = 30;
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 17));
        g2.drawString("Score: " + board.getScore(), PAD, y);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.drawString(status, PAD, y + 22);

        // legend
        int lx = PAD;
        GemType[] vals = GemType.values();
        for (GemType t : vals) {
            drawGemShape(g2, t, lx, 42, 16);
            g2.setColor(new Color(200, 200, 205));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.drawString(t.getName(), lx + 14, 50);
            lx += 74;
        }
    }
}
