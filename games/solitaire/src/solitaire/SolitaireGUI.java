package solitaire;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class SolitaireGUI extends JPanel implements MouseListener, MouseMotionListener {
    private final SolitaireGame game = new SolitaireGame();
    private static final int CARD_W = 80, CARD_H = 115, GAP = 12, TABLEAU_Y = 160;
    private static final int FOUNDATION_Y = 20, STOCK_X = GAP;
    private static final int CORNER_R = 8;

    private int dragX, dragY, mouseX, mouseY;
    private boolean dragging;
    private int dragSourceTableau = -1, dragCardIndex = -1;
    private List<Card> dragCards = List.of();
    private boolean dragFromWaste;

    private int winAnimPhase = 0;
    private final Timer winTimer;

    public SolitaireGUI() {
        setPreferredSize(new Dimension(690, 520));
        setBackground(new Color(0, 56, 28));
        addMouseListener(this);
        addMouseMotionListener(this);

        winTimer = new Timer(80, e -> {
            winAnimPhase++;
            repaint();
            if (winAnimPhase > 200) stopWin();
        });
    }

    private void stopWin() {
        winTimer.stop();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        var g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawStock(g2);
        drawWaste(g2);
        drawFoundations(g2);
        drawTableau(g2);
        drawScore(g2);

        if (game.isWon()) drawWinScreen(g2);
    }

    private void drawCard(Graphics2D g, Card card, int x, int y, boolean shadow) {
        if (shadow) {
            g.setColor(new Color(0, 0, 0, 40));
            g.fill(new RoundRectangle2D.Double(x + 3, y + 3, CARD_W, CARD_H, CORNER_R, CORNER_R));
        }

        if (card.faceUp()) {
            g.setColor(Color.WHITE);
            g.fill(new RoundRectangle2D.Double(x, y, CARD_W, CARD_H, CORNER_R, CORNER_R));
            g.setColor(Color.GRAY);
            g.setStroke(new BasicStroke(1.5f));
            g.draw(new RoundRectangle2D.Double(x, y, CARD_W, CARD_H, CORNER_R, CORNER_R));

            g.setColor(card.suit().color);
            g.setFont(new Font("SansSerif", Font.BOLD, 22));
            g.drawString(card.rank().symbol, x + 6, y + 22);
            g.setFont(new Font("SansSerif", Font.PLAIN, 18));
            g.drawString(card.suit().symbol, x + 6, y + 40);

            g.setFont(new Font("Serif", Font.BOLD, 36));
            g.drawString(card.suit().symbol, x + CARD_W / 2 - 12, y + CARD_H / 2 + 14);
        } else {
            g.setColor(new Color(30, 80, 160));
            g.fill(new RoundRectangle2D.Double(x, y, CARD_W, CARD_H, CORNER_R, CORNER_R));
            g.setColor(new Color(50, 100, 180));
            g.setStroke(new BasicStroke(2f));
            g.draw(new RoundRectangle2D.Double(x + 4, y + 4, CARD_W - 8, CARD_H - 8, CORNER_R, CORNER_R));
            g.setColor(new Color(255, 255, 255, 40));
            g.setFont(new Font("SansSerif", Font.BOLD, 30));
            g.drawString("\u2660", x + CARD_W / 2 - 12, y + CARD_H / 2 + 12);
        }
    }

    private void drawEmptySlot(Graphics2D g, int x, int y) {
        g.setColor(new Color(255, 255, 255, 50));
        g.fill(new RoundRectangle2D.Double(x, y, CARD_W, CARD_H, CORNER_R, CORNER_R));
        g.setColor(new Color(255, 255, 255, 80));
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{4}, 0));
        g.draw(new RoundRectangle2D.Double(x, y, CARD_W, CARD_H, CORNER_R, CORNER_R));
    }

    private void drawStock(Graphics2D g) {
        int x = STOCK_X, y = FOUNDATION_Y;
        if (game.getStock().isEmpty()) {
            drawEmptySlot(g, x, y);
            g.setColor(new Color(255, 255, 255, 120));
            g.setFont(new Font("SansSerif", Font.BOLD, 28));
            g.drawString("\u21BA", x + CARD_W / 2 - 10, y + CARD_H / 2 + 10);
        } else {
            for (int i = 0; i < Math.min(3, game.getStock().size()); i++) {
                drawCard(g, new Card(Card.Suit.SPADES, Card.Rank.ACE, false), x + i * 2, y, true);
            }
            var top = game.getStock().peek();
            drawCard(g, top, x, y, true);
        }
    }

    private void drawWaste(Graphics2D g) {
        int x = GAP + CARD_W + GAP, y = FOUNDATION_Y;
        if (game.getWaste().isEmpty()) {
            drawEmptySlot(g, x, y);
        } else {
            var waste = game.getWaste();
            int start = Math.max(0, waste.size() - 3);
            for (int i = start; i < waste.size(); i++) {
                if (dragging && dragFromWaste && i == waste.size() - 1) continue;
                drawCard(g, waste.get(i), x + (i - start) * 15, y, true);
            }
        }
    }

    private void drawFoundations(Graphics2D g) {
        for (int i = 0; i < 4; i++) {
            int x = GAP + (CARD_W + GAP) * (i + 3);
            int y = FOUNDATION_Y;
            var pile = game.getFoundations().get(i);
            if (pile.isEmpty()) {
                drawEmptySlot(g, x, y);
                g.setColor(new Color(255, 255, 255, 80));
                g.setFont(new Font("SansSerif", Font.BOLD, 24));
                String[] suits = {"\u2665", "\u2666", "\u2663", "\u2660"};
                g.drawString(suits[i], x + CARD_W / 2 - 8, y + CARD_H / 2 + 10);
            } else {
                drawCard(g, pile.getLast(), x, y, true);
            }
        }
    }

    private void drawTableau(Graphics2D g) {
        for (int col = 0; col < SolitaireGame.TABLEAU_SIZE; col++) {
            int x = getTableauX(col);
            var pile = game.getTableau().get(col);
            if (pile.isEmpty()) {
                drawEmptySlot(g, x, TABLEAU_Y);
                continue;
            }
            for (int row = 0; row < pile.size(); row++) {
                int y = TABLEAU_Y + row * 25;
                if (dragging && col == dragSourceTableau && row >= dragCardIndex) continue;
                drawCard(g, pile.get(row), x, y, true);
            }
        }

        if (dragging && !dragCards.isEmpty()) {
            for (int i = 0; i < dragCards.size(); i++) {
                drawCard(g, dragCards.get(i), dragX, dragY + i * 25, true);
            }
        }
    }

    private void drawScore(Graphics2D g) {
        g.setColor(new Color(255, 255, 255, 180));
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.drawString("Score: " + game.getScore() + "  Moves: " + game.getMoves(), GAP, getHeight() - 10);
        g.drawString("New Game [N]", getWidth() - 110, getHeight() - 10);
    }

    private void drawWinScreen(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());

        for (int i = 0; i < 30; i++) {
            double angle = winAnimPhase * 0.05 + i * 0.21;
            int fx = (int) (getWidth() / 2 + Math.cos(angle) * (100 + i * 8));
            int fy = (int) (getHeight() / 2 + Math.sin(angle) * (60 + i * 5));
            g.setFont(new Font("SansSerif", Font.BOLD, 20 + i % 10));
            g.setColor(new Color(
                (int) (Math.sin(angle) * 127 + 128),
                (int) (Math.sin(angle + 2) * 127 + 128),
                (int) (Math.sin(angle + 4) * 127 + 128)));
            g.drawString("\u2665", fx, fy);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 48));
        var fm = g.getFontMetrics();
        String msg = "YOU WIN!";
        g.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2 - 10);
        g.setFont(new Font("SansSerif", Font.PLAIN, 20));
        fm = g.getFontMetrics();
        String score = "Score: " + game.getScore() + "  Moves: " + game.getMoves();
        g.drawString(score, (getWidth() - fm.stringWidth(score)) / 2, getHeight() / 2 + 30);
    }

    private int getTableauX(int col) {
        return GAP + col * (CARD_W + GAP);
    }

    private int getWasteX() { return GAP + CARD_W + GAP; }

    private int hitTest(int mx, int my) {
        if (my >= FOUNDATION_Y && my <= FOUNDATION_Y + CARD_H) {
            if (mx >= STOCK_X && mx <= STOCK_X + CARD_W) return 100;
            if (mx >= getWasteX() && mx <= getWasteX() + CARD_W) return 101;
            for (int i = 0; i < 4; i++) {
                int fx = GAP + (CARD_W + GAP) * (i + 3);
                if (mx >= fx && mx <= fx + CARD_W) return 200 + i;
            }
        }
        if (my >= TABLEAU_Y) {
            for (int col = 0; col < SolitaireGame.TABLEAU_SIZE; col++) {
                int tx = getTableauX(col);
                if (mx >= tx && mx <= tx + CARD_W) {
                    var pile = game.getTableau().get(col);
                    for (int row = pile.size() - 1; row >= 0; row--) {
                        int cardBottom = TABLEAU_Y + row * 25 + CARD_H;
                        if (my <= cardBottom || row == pile.size() - 1) {
                            if (my >= TABLEAU_Y + row * 25) return 300 + col * 100 + row;
                        }
                    }
                    return 300 + col * 100;
                }
            }
        }
        return -1;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int hit = hitTest(e.getX(), e.getY());
        if (hit == -1) return;

        if (hit == 100) {
            game.dealFromStock();
            repaint();
            return;
        }

        if (hit >= 300 && hit < 4000) {
            int col = (hit - 300) / 100;
            int row = (hit - 300) % 100;
            var pile = game.getTableau().get(col);
            if (!pile.isEmpty() && row == pile.size() - 1 && pile.getLast().faceUp()) {
                if (game.moveToFoundationFromTableau(col)) {
                    if (game.isWon()) winTimer.start();
                    repaint();
                    return;
                }
            }
        }

        if (hit == 101) {
            if (game.wasteToFoundation()) {
                if (game.isWon()) winTimer.start();
                repaint();
                return;
            }
        }

        if (hit >= 300 && hit < 4000) {
            int col = (hit - 300) / 100;
            int row = (hit - 300) % 100;
            var pile = game.getTableau().get(col);
            if (!pile.isEmpty() && row == pile.size() - 1 && pile.getLast().faceUp()) {
                for (int f = 0; f < SolitaireGame.TABLEAU_SIZE; f++) {
                    if (f != col && game.moveToTableau(col, row, f)) {
                        repaint();
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int hit = hitTest(e.getX(), e.getY());
        if (hit == -1) return;

        if (hit == 101 && !game.getWaste().isEmpty()) {
            dragging = true;
            dragFromWaste = true;
            dragCards = List.of(game.getWaste().getLast());
            dragX = e.getX() - CARD_W / 2;
            dragY = e.getY() - 20;
            repaint();
            return;
        }

        if (hit < 300 || hit >= 4000) return;

        int col = (hit - 300) / 100;
        int row = (hit - 300) % 100;
        var pile = game.getTableau().get(col);
        if (pile.isEmpty() || row < 0 || row >= pile.size()) return;
        if (!pile.get(row).faceUp()) return;

        dragging = true;
        dragSourceTableau = col;
        dragCardIndex = row;
        dragCards = new ArrayList<>(pile.subList(row, pile.size()));
        dragX = e.getX() - CARD_W / 2;
        dragY = e.getY() - 20;
        mouseX = e.getX();
        mouseY = e.getY();
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (!dragging) return;
        dragging = false;

        int dropCol = -1;
        for (int col = 0; col < SolitaireGame.TABLEAU_SIZE; col++) {
            int tx = getTableauX(col);
            if (e.getX() >= tx && e.getX() <= tx + CARD_W && e.getY() >= TABLEAU_Y - 30) {
                dropCol = col;
                break;
            }
        }

        boolean handled = false;

        if (e.getY() >= FOUNDATION_Y && e.getY() <= FOUNDATION_Y + CARD_H + 20
                && dragCards.size() == 1) {
            for (int f = 0; f < 4; f++) {
                int fx = GAP + (CARD_W + GAP) * (f + 3);
                if (e.getX() >= fx && e.getX() <= fx + CARD_W) {
                    if (dragFromWaste) {
                        if (game.wasteToFoundation()) handled = true;
                    } else if (game.moveToFoundation(dragSourceTableau, dragCardIndex)) {
                        handled = true;
                    }
                    if (handled) break;
                }
            }
        }

        if (!handled && dropCol >= 0) {
            if (dragFromWaste) {
                handled = game.wasteToTableau(dropCol);
            } else {
                handled = game.moveToTableau(dragSourceTableau, dragCardIndex, dropCol);
            }
        }

        if (handled && game.isWon()) winTimer.start();

        dragFromWaste = false;
        dragCards = List.of();
        dragSourceTableau = -1;
        dragCardIndex = -1;
        repaint();
    }

    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseDragged(MouseEvent e) {
        if (dragging) {
            dragX = e.getX() - CARD_W / 2;
            dragY = e.getY() - 20;
            mouseX = e.getX();
            mouseY = e.getY();
            repaint();
        }
    }
    @Override public void mouseMoved(MouseEvent e) {}

    public void handleKey(char key) {
        if (key == 'n' || key == 'N') {
            game.newGame();
            winTimer.stop();
            winAnimPhase = 0;
            repaint();
        }
        if (key == 'a' || key == 'A') {
            game.autoMoveToFoundation();
            if (game.isWon()) winTimer.start();
            repaint();
        }
        if (key == 'd' || key == 'D') {
            game.dealFromStock();
            repaint();
        }
    }
}
