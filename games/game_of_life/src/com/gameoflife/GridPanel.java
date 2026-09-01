package com.gameoflife;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;

public class GridPanel extends JPanel {

    private static final Color BACKGROUND = new Color(20, 20, 26);
    private static final Color GRID_LINE = new Color(45, 45, 55);
    private static final Color ALIVE = new Color(0, 255, 120);
    private static final Color ALIVE_CORE = new Color(180, 255, 200);

    private Grid grid;
    private boolean drawGridLines = true;

    public GridPanel(Grid grid) {
        this.grid = grid;
        setBackground(BACKGROUND);
        setPreferredSize(new Dimension(1200, 600));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                toggleCellAt(e);
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                toggleCellAt(e);
            }
        });
    }

    public void setGrid(Grid grid) {
        this.grid = grid;
        repaint();
    }

    public void setDrawGridLines(boolean drawGridLines) {
        this.drawGridLines = drawGridLines;
        repaint();
    }

    private void toggleCellAt(MouseEvent e) {
        double cellW = (double) getWidth() / grid.cols();
        double cellH = (double) getHeight() / grid.rows();
        int col = (int) (e.getX() / cellW);
        int row = (int) (e.getY() / cellH);
        if (row >= 0 && row < grid.rows() && col >= 0 && col < grid.cols()) {
            grid.setAlive(row, col, !grid.isAlive(row, col));
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int rows = grid.rows();
        int cols = grid.cols();
        double cellW = (double) getWidth() / cols;
        double cellH = (double) getHeight() / rows;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid.isAlive(r, c)) {
                    double x = c * cellW;
                    double y = r * cellH;
                    g2.setColor(ALIVE);
                    g2.fillRect((int) x, (int) y, (int) Math.ceil(cellW), (int) Math.ceil(cellH));
                    g2.setColor(ALIVE_CORE);
                    g2.fillOval((int) (x + cellW * 0.2), (int) (y + cellH * 0.2),
                            (int) (cellW * 0.6), (int) (cellH * 0.6));
                }
            }
        }

        if (drawGridLines && cols < 100 && rows < 60) {
            g2.setColor(GRID_LINE);
            for (int c = 1; c < cols; c++) {
                int x = (int) (c * cellW);
                g2.drawLine(x, 0, x, getHeight());
            }
            for (int r = 1; r < rows; r++) {
                int y = (int) (r * cellH);
                g2.drawLine(0, y, getWidth(), y);
            }
        }
    }
}
