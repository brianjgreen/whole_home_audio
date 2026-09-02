package dungeon;

import java.awt.*;
import java.util.List;

public class Renderer {

    private static final int TILE_SIZE = 24;
    // private static final Color FOG_EXPLORED = new Color(40, 40, 50);
    private static final Color FOG_UNSEEN = Color.BLACK;
    private static final Color HP_BAR_GREEN = new Color(40, 200, 60);
    private static final Color HP_BAR_RED = new Color(200, 40, 40);
    private static final Color HP_BAR_BG = new Color(30, 30, 30);
    private static final Color UI_BG = new Color(20, 18, 25, 200);
    private static final Color UI_BORDER = new Color(80, 70, 100);
    private static final Color UI_TEXT = new Color(210, 200, 220);
    private static final Color UI_DIM = new Color(140, 130, 150);
    private static final Color MINIMAP_BG = new Color(10, 10, 15, 220);

    public void render(Graphics2D g, int panelW, int panelH,
                       DungeonMap map, Player player,
                       List<Enemy> enemies, List<int[]> items,
                       MessageLog log) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, panelW, panelH);

        int viewW = panelW / TILE_SIZE;
        int viewH = (panelH - 80) / TILE_SIZE; // reserve bottom for messages

        int camX = player.x() - viewW / 2;
        int camY = player.y() - viewH / 2;

        map.computeFOV(player.x(), player.y(), 10);

        // Draw tiles
        for (int vy = 0; vy < viewH + 1; vy++) {
            for (int vx = 0; vx < viewW + 1; vx++) {
                int mx = camX + vx;
                int my = camY + vy;
                int sx = vx * TILE_SIZE;
                int sy = vy * TILE_SIZE;

                if (!map.inBounds(mx, my)) {
                    g.setColor(FOG_UNSEEN);
                    g.fillRect(sx, sy, TILE_SIZE, TILE_SIZE);
                    continue;
                }

                if (map.isVisible(mx, my)) {
                    drawTile(g, map.getTile(mx, my), sx, sy);
                } else if (map.isExplored(mx, my)) {
                    drawTileDim(g, map.getTile(mx, my), sx, sy);
                } else {
                    g.setColor(FOG_UNSEEN);
                    g.fillRect(sx, sy, TILE_SIZE, TILE_SIZE);
                }
            }
        }

        // Draw items
        for (int[] item : items) {
            int ix = item[0], iy = item[1];
            if (!map.isVisible(ix, iy)) continue;
            int sx = (ix - camX) * TILE_SIZE;
            int sy = (iy - camY) * TILE_SIZE;
            drawItem(g, item[2], sx, sy);
        }

        // Draw enemies
        for (Enemy enemy : enemies) {
            if (!map.isVisible(enemy.x(), enemy.y())) continue;
            int sx = (enemy.x() - camX) * TILE_SIZE;
            int sy = (enemy.y() - camY) * TILE_SIZE;
            drawEnemy(g, enemy, sx, sy);
        }

        // Draw player
        int psx = (player.x() - camX) * TILE_SIZE;
        int psy = (player.y() - camY) * TILE_SIZE;
        drawPlayer(g, player, psx, psy);

        // Draw UI panels
        drawTopBar(g, panelW, player);
        drawMinimap(g, panelW, panelH, map, player, enemies);
        drawMessageLog(g, panelW, panelH, log);
    }

    private void drawTile(Graphics2D g, Tile tile, int sx, int sy) {
        g.setColor(tile.getColor());
        g.fillRect(sx, sy, TILE_SIZE, TILE_SIZE);

        // Add subtle shading for walls
        if (tile == Tile.WALL) {
            g.setColor(new Color(0, 0, 0, 40));
            g.fillRect(sx, sy + TILE_SIZE - 3, TILE_SIZE, 3);
            g.setColor(new Color(255, 255, 255, 20));
            g.fillRect(sx, sy, TILE_SIZE, 2);
        }

        // Floor texture hint
        if (tile == Tile.FLOOR || tile == Tile.CORRIDOR) {
            g.setColor(new Color(0, 0, 0, 15));
            g.fillRect(sx + 2, sy + 2, 1, 1);
            g.fillRect(sx + TILE_SIZE - 4, sy + TILE_SIZE - 5, 1, 1);
        }

        // Stairs glow
        if (tile == Tile.STAIRS_DOWN || tile == Tile.STAIRS_UP) {
            g.setColor(new Color(255, 255, 100, 30));
            g.fillOval(sx - 2, sy - 2, TILE_SIZE + 4, TILE_SIZE + 4);
        }
    }

    private void drawTileDim(Graphics2D g, Tile tile, int sx, int sy) {
        Color c = tile.getColor();
        Color dim = new Color(
            c.getRed() / 4,
            c.getGreen() / 4,
            c.getBlue() / 4
        );
        g.setColor(dim);
        g.fillRect(sx, sy, TILE_SIZE, TILE_SIZE);
    }

    private void drawPlayer(Graphics2D g, Player p, int sx, int sy) {
        // Glow
        g.setColor(new Color(80, 140, 255, 40));
        g.fillOval(sx - 3, sy - 3, TILE_SIZE + 6, TILE_SIZE + 6);

        // Body
        g.setColor(p.color());
        g.fillRoundRect(sx + 3, sy + 3, TILE_SIZE - 6, TILE_SIZE - 6, 6, 6);

        // Symbol
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, TILE_SIZE - 6));
        FontMetrics fm = g.getFontMetrics();
        String s = String.valueOf(p.symbol());
        g.drawString(s, sx + (TILE_SIZE - fm.stringWidth(s)) / 2,
                         sy + (TILE_SIZE + fm.getAscent() - fm.getDescent()) / 2 - 1);
    }

    private void drawEnemy(Graphics2D g, Enemy e, int sx, int sy) {
        // Body
        g.setColor(e.color());
        g.fillRoundRect(sx + 4, sy + 4, TILE_SIZE - 8, TILE_SIZE - 8, 4, 4);

        // Symbol
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, TILE_SIZE - 8));
        FontMetrics fm = g.getFontMetrics();
        String s = String.valueOf(e.symbol());
        g.drawString(s, sx + (TILE_SIZE - fm.stringWidth(s)) / 2,
                         sy + (TILE_SIZE + fm.getAscent() - fm.getDescent()) / 2 - 1);

        // Health bar (only if damaged)
        if (e.hp() < e.maxHp()) {
            int barW = TILE_SIZE - 4;
            int barH = 3;
            int barX = sx + 2;
            int barY = sy - 2;
            float ratio = (float) e.hp() / e.maxHp();
            g.setColor(HP_BAR_BG);
            g.fillRect(barX, barY, barW, barH);
            g.setColor(ratio > 0.5f ? HP_BAR_GREEN : HP_BAR_RED);
            g.fillRect(barX, barY, (int)(barW * ratio), barH);
        }
    }

    private void drawItem(Graphics2D g, int type, int sx, int sy) {
        int cx = sx + TILE_SIZE / 2;
        int cy = sy + TILE_SIZE / 2;
        g.setColor(new Color(255, 255, 100, 50));
        g.fillOval(sx, sy, TILE_SIZE, TILE_SIZE);

        if (type <= 1) {
            // Potion
            g.setColor(new Color(220, 50, 50));
            g.fillRoundRect(cx - 4, cy - 5, 8, 10, 3, 3);
            g.setColor(new Color(180, 40, 40));
            g.fillRect(cx - 2, cy - 7, 4, 3);
        } else {
            // XP gem
            g.setColor(new Color(100, 200, 255));
            int[] xPoints = {cx, cx + 5, cx, cx - 5};
            int[] yPoints = {cy - 5, cy, cy + 5, cy};
            g.fillPolygon(xPoints, yPoints, 4);
        }
    }

    private void drawTopBar(Graphics2D g, int panelW, Player player) {
        int barH = 50;
        g.setColor(UI_BG);
        g.fillRect(0, 0, panelW, barH);
        g.setColor(UI_BORDER);
        g.drawLine(0, barH, panelW, barH);

        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        FontMetrics fm = g.getFontMetrics();

        // HP bar
        int hpBarX = 12, hpBarY = 8, hpBarW = 200, hpBarH = 14;
        g.setColor(HP_BAR_BG);
        g.fillRoundRect(hpBarX, hpBarY, hpBarW, hpBarH, 4, 4);
        float hpRatio = (float) player.hp() / player.maxHp();
        g.setColor(hpRatio > 0.5f ? HP_BAR_GREEN : hpRatio > 0.25f ? new Color(220, 180, 40) : HP_BAR_RED);
        g.fillRoundRect(hpBarX, hpBarY, (int)(hpBarW * hpRatio), hpBarH, 4, 4);
        g.setColor(Color.WHITE);
        String hpText = "HP " + player.hp() + "/" + player.maxHp();
        g.drawString(hpText, hpBarX + 4, hpBarY + 12);

        // Level and XP
        g.setColor(UI_TEXT);
        String lvlText = "Lv." + player.level();
        g.drawString(lvlText, hpBarX + hpBarW + 15, hpBarY + 12);

        // XP bar
        int xpBarX = hpBarX + hpBarW + 80, xpBarW = 120;
        g.setColor(new Color(30, 30, 40));
        g.fillRoundRect(xpBarX, hpBarY, xpBarW, hpBarH, 4, 4);
        float xpRatio = (float) player.xp() / player.xpToNext();
        g.setColor(new Color(100, 150, 255));
        g.fillRoundRect(xpBarX, hpBarY, (int)(xpBarW * xpRatio), hpBarH, 4, 4);
        g.setColor(UI_DIM);
        String xpText = "XP " + player.xp() + "/" + player.xpToNext();
        g.drawString(xpText, xpBarX + 4, hpBarY + 12);

        // Stats
        g.setColor(UI_TEXT);
        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        String stats = "ATK:" + player.attack() + "  DEF:" + player.defense()
                     + "  Potions:" + player.potions();
        g.drawString(stats, 12, barH - 6);

        // Floor
        g.setColor(new Color(180, 200, 140));
        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        String floorText = "Floor " + player.floor();
        g.drawString(floorText, panelW - fm.stringWidth(floorText) - 15, hpBarY + 12);
    }

    private void drawMinimap(Graphics2D g, int panelW, int panelH,
                             DungeonMap map, Player player, List<Enemy> enemies) {
        int mmSize = 140;
        int mmX = panelW - mmSize - 10;
        int mmY = 58;
        int mmScale = 2;

        g.setColor(MINIMAP_BG);
        g.fillRoundRect(mmX - 4, mmY - 4, mmSize + 8, mmSize + 8, 6, 6);
        g.setColor(UI_BORDER);
        g.drawRoundRect(mmX - 4, mmY - 4, mmSize + 8, mmSize + 8, 6, 6);

        int offsetX = player.x() - mmSize / (2 * mmScale);
        int offsetY = player.y() - mmSize / (2 * mmScale);

        for (int my = 0; my < mmSize; my += mmScale) {
            for (int mx = 0; mx < mmSize; mx += mmScale) {
                int mapX = offsetX + mx / mmScale;
                int mapY = offsetY + my / mmScale;
                if (!map.inBounds(mapX, mapY)) continue;
                if (!map.isExplored(mapX, mapY)) continue;

                Tile t = map.getTile(mapX, mapY);
                if (t == Tile.WALL) {
                    g.setColor(new Color(70, 65, 85));
                } else if (t == Tile.STAIRS_DOWN) {
                    g.setColor(new Color(50, 220, 50));
                } else {
                    g.setColor(new Color(80, 75, 70));
                }
                g.fillRect(mmX + mx, mmY + my, mmScale, mmScale);
            }
        }

        // Player dot
        g.setColor(Color.CYAN);
        g.fillRect(mmX + mmSize / 2 - 1, mmY + mmSize / 2 - 1, 3, 3);

        // Enemy dots
        for (Enemy e : enemies) {
            if (!map.isVisible(e.x(), e.y())) continue;
            int ex = mmX + (e.x() - offsetX) * mmScale;
            int ey = mmY + (e.y() - offsetY) * mmScale;
            if (ex >= mmX && ex < mmX + mmSize && ey >= mmY && ey < mmY + mmSize) {
                g.setColor(Color.RED);
                g.fillRect(ex, ey, 2, 2);
            }
        }
    }

    private void drawMessageLog(Graphics2D g, int panelW, int panelH, MessageLog log) {
        int logH = 75;
        int logY = panelH - logH;

        g.setColor(UI_BG);
        g.fillRect(0, logY, panelW, logH);
        g.setColor(UI_BORDER);
        g.drawLine(0, logY, panelW, logY);

        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        // FontMetrics fm = g.getFontMetrics();
        List<String> messages = log.recent(5);

        for (int i = 0; i < messages.size(); i++) {
            float alpha = 0.5f + 0.5f * ((float) (i + 1) / messages.size());
            g.setColor(new Color(UI_TEXT.getRed(), UI_TEXT.getGreen(), UI_TEXT.getBlue(), (int)(alpha * 255)));
            g.drawString(messages.get(i), 12, logY + 16 + i * 14);
        }
    }

    public static int tileSize() { return TILE_SIZE; }
}
