package dungeon;

import java.awt.Color;

public enum Tile {
    VOID(true, new Color(0, 0, 0), ' '),
    WALL(true, new Color(55, 50, 65), '#'),
    FLOOR(false, new Color(140, 130, 110), '.'),
    CORRIDOR(false, new Color(115, 110, 95), ','),
    DOOR(false, new Color(160, 100, 50), '+'),
    STAIRS_DOWN(false, new Color(50, 220, 50), '>'),
    STAIRS_UP(false, new Color(50, 170, 50), '<');

    private final boolean blocked;
    private final Color color;
    private final char symbol;

    Tile(boolean blocked, Color color, char symbol) {
        this.blocked = blocked;
        this.color = color;
        this.symbol = symbol;
    }

    public boolean isBlocked() { return blocked; }
    public Color getColor() { return color; }
    public char getSymbol() { return symbol; }
}
