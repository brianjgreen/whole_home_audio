package dungeon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel {

    private static final int MAP_WIDTH = 60;
    private static final int MAP_HEIGHT = 60;

    private DungeonMap map;
    private Player player;
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<int[]> items = new ArrayList<>();
    private final MessageLog log = new MessageLog();
    private final Renderer renderer = new Renderer();
    private final Random rng = new Random();
    private boolean gameOver;
    private boolean gameWon;
    private String deathMessage;

    public GamePanel() {
        setPreferredSize(new Dimension(1024, 768));
        setBackground(Color.BLACK);
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (gameOver || gameWon) {
                    if (e.getKeyCode() == KeyEvent.VK_R) {
                        startNewGame();
                    }
                    repaint();
                    return;
                }
                handleInput(e.getKeyCode());
                repaint();
            }
        });

        startNewGame();
    }

    private void startNewGame() {
        enemies.clear();
        items.clear();
        log.clear();
        gameOver = false;
        gameWon = false;
        deathMessage = null;

        map = new DungeonMap(MAP_WIDTH, MAP_HEIGHT, 1);
        Room firstRoom = map.rooms().getFirst();
        player = new Player(firstRoom.centerX(), firstRoom.centerY());

        map.spawnEnemies(enemies, 1);
        map.spawnItems(items, 1);

        log.add("You awaken in a dark dungeon...");
        log.add("Find the stairs (>) to descend deeper.");
        log.add("[WASD/Arrows] Move  [E] Potion  [R] Restart");
    }

    private void descendFloor() {
        int nextFloor = player.floor() + 1;
        if (nextFloor > 6) {
            gameWon = true;
            log.add("You escaped the dungeon! YOU WIN!");
            return;
        }

        enemies.clear();
        items.clear();

        map = new DungeonMap(MAP_WIDTH, MAP_HEIGHT, nextFloor);
        player.setFloor(nextFloor);
        player.setPos(map.stairsUpX(), map.stairsUpY());

        map.spawnEnemies(enemies, nextFloor);
        map.spawnItems(items, nextFloor);

        log.add("--- Floor " + nextFloor + " ---");
        if (nextFloor >= 4) log.add("The air grows thick with danger...");
    }

    private void handleInput(int keyCode) {
        int dx = 0, dy = 0;

        switch (keyCode) {
            case KeyEvent.VK_W, KeyEvent.VK_UP -> dy = -1;
            case KeyEvent.VK_S, KeyEvent.VK_DOWN -> dy = 1;
            case KeyEvent.VK_A, KeyEvent.VK_LEFT -> dx = -1;
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> dx = 1;
            case KeyEvent.VK_E -> {
                if (player.usePotion()) {
                    log.add("You drink a potion. HP: " + player.hp() + "/" + player.maxHp());
                } else if (player.potions() <= 0) {
                    log.add("No potions left!");
                } else {
                    log.add("HP already full!");
                }
                processEnemyTurns();
                return;
            }
            case KeyEvent.VK_PERIOD -> {
                // Check for stairs
                if (player.x() == map.stairsDownX() && player.y() == map.stairsDownY()) {
                    descendFloor();
                    return;
                }
            }
            default -> { return; }
        }

        if (dx == 0 && dy == 0) return;

        // Check for enemy at target
        int targetX = player.x() + dx;
        int targetY = player.y() + dy;

        Enemy target = null;
        for (Enemy e : enemies) {
            if (e.x() == targetX && e.y() == targetY) {
                target = e;
                break;
            }
        }

        if (target != null) {
            // Attack enemy
            int dmg = player.attack();
            target.takeDamage(dmg);
            log.add("You hit " + target.name() + " for " + dmg + " damage!");

            if (target.isDead()) {
                log.add(target.name() + " defeated! +" + target.xpValue() + " XP");
                player.gainXp(target.xpValue());
                enemies.remove(target);
            }
        } else {
            // Move
            player.move(dx, dy, map);

            // Check stairs prompt
            if (player.x() == map.stairsDownX() && player.y() == map.stairsDownY()) {
                log.add("Press [.] or [ENTER] to descend the stairs.");
            }
        }

        // Check for items
        Iterator<int[]> itemIter = items.iterator();
        while (itemIter.hasNext()) {
            int[] item = itemIter.next();
            if (item[0] == player.x() && item[1] == player.y()) {
                if (item[2] <= 1) {
                    player.addPotion();
                    log.add("Found a health potion! (Potions: " + player.potions() + ")");
                } else {
                    player.gainXp(8);
                    log.add("Found an XP crystal! +8 XP");
                }
                itemIter.remove();
            }
        }

        // Enemy turns
        processEnemyTurns();

        // Check death
        if (player.isDead()) {
            gameOver = true;
            deathMessage = "Killed by " + (enemies.isEmpty() ? "the dungeon" : enemies.getLast().name());
            log.add("You have died! " + deathMessage);
            log.add("Press [R] to restart.");
        }
    }

    private void processEnemyTurns() {
        for (Enemy enemy : enemies) {
            if (enemy.isAdjacentTo(player)) {
                int dmg = enemy.attack();
                player.takeDamage(dmg);
                log.add(enemy.name() + " hits you for " + dmg + " damage!");
                if (player.isDead()) return;
            } else {
                enemy.takeTurn(player, map, enemies);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        renderer.render(g2, getWidth(), getHeight(), map, player, enemies, items, log);

        if (gameOver) {
            drawOverlay(g2, "YOU DIED", deathMessage, new Color(180, 30, 30));
        } else if (gameWon) {
            drawOverlay(g2, "VICTORY!", "The dungeon is conquered!", new Color(30, 180, 60));
        }
    }

    private void drawOverlay(Graphics2D g, String title, String subtitle, Color color) {
        int w = getWidth(), h = getHeight();
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(0, 0, w, h);

        g.setFont(new Font("Monospaced", Font.BOLD, 48));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(color);
        String t = title;
        g.drawString(t, (w - fm.stringWidth(t)) / 2, h / 2 - 30);

        g.setFont(new Font("Monospaced", Font.PLAIN, 18));
        fm = g.getFontMetrics();
        g.setColor(Color.WHITE);
        String s = subtitle;
        g.drawString(s, (w - fm.stringWidth(s)) / 2, h / 2 + 10);

        g.setFont(new Font("Monospaced", Font.PLAIN, 14));
        fm = g.getFontMetrics();
        g.setColor(new Color(180, 180, 180));
        String r = "Press [R] to restart";
        g.drawString(r, (w - fm.stringWidth(r)) / 2, h / 2 + 50);
    }
}
