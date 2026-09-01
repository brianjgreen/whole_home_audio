package dungeon;

import java.awt.Color;
import java.util.Random;

public class Enemy {

    public enum Type {
        RAT("Rat", 8, 3, 1, 4, new Color(160, 120, 80), 'r'),
        SLIME("Slime", 12, 4, 1, 6, new Color(80, 200, 80), 's'),
        SKELETON("Skeleton", 18, 6, 2, 10, new Color(210, 210, 210), 'S'),
        ORC("Orc", 28, 9, 3, 18, new Color(120, 160, 60), 'O'),
        TROLL("Troll", 40, 12, 4, 28, new Color(140, 100, 70), 'T'),
        DRAGON("Dragon", 60, 18, 6, 50, new Color(220, 60, 40), 'D');

        private final String name;
        private final int baseHp, baseAtk, baseDef, xpValue;
        private final Color color;
        private final char symbol;

        Type(String name, int baseHp, int baseAtk, int baseDef, int xpValue, Color color, char symbol) {
            this.name = name;
            this.baseHp = baseHp;
            this.baseAtk = baseAtk;
            this.baseDef = baseDef;
            this.xpValue = xpValue;
            this.color = color;
            this.symbol = symbol;
        }

        public String typeName() { return name; }
        public int baseHp() { return baseHp; }
        public int baseAtk() { return baseAtk; }
        public int baseDef() { return baseDef; }
        public int xpValue() { return xpValue; }
        public Color color() { return color; }
        public char symbol() { return symbol; }
    }

    private int x, y;
    private int hp, maxHp;
    private int attack, defense;
    private final Type type;
    private boolean alerted;
    private final Random rng = new Random();

    public Enemy(int x, int y, Type type, int floorScaling) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.maxHp = type.baseHp() + floorScaling * 3;
        this.hp = maxHp;
        this.attack = type.baseAtk() + floorScaling;
        this.defense = type.baseDef() + floorScaling / 2;
        this.alerted = false;
    }

    public void takeTurn(Player player, DungeonMap map, java.util.List<Enemy> allEnemies) {
        int dist = Math.abs(x - player.x()) + Math.abs(y - player.y());
        if (dist <= 6) alerted = true;
        if (!alerted) return;

        int dx = Integer.signum(player.x() - x);
        int dy = Integer.signum(player.y() - y);

        // Try to move toward player
        if (dist <= 1) return; // adjacent, will attack via combat

        int[] attempts;
        if (Math.abs(player.x() - x) >= Math.abs(player.y() - y)) {
            attempts = new int[]{dx, 0, 0, dy};
        } else {
            attempts = new int[]{0, dy, dx, 0};
        }

        for (int i = 0; i < 4; i += 2) {
            int mx = x + attempts[i];
            int my = y + attempts[i + 1];
            if (!map.inBounds(mx, my) || map.getTile(mx, my).isBlocked()) continue;
            if (mx == player.x() && my == player.y()) continue;
            boolean blocked = false;
            for (Enemy e : allEnemies) {
                if (e != this && e.x == mx && e.y == my) { blocked = true; break; }
            }
            if (!blocked) {
                x = mx;
                y = my;
                return;
            }
        }
    }

    public boolean isAdjacentTo(Player player) {
        return Math.abs(x - player.x()) + Math.abs(y - player.y()) == 1;
    }

    public void takeDamage(int damage) {
        int actual = Math.max(1, damage - defense);
        hp = Math.max(0, hp - actual);
    }

    public boolean isDead() { return hp <= 0; }
    public int x() { return x; }
    public int y() { return y; }
    public int hp() { return hp; }
    public int maxHp() { return maxHp; }
    public int attack() { return attack; }
    public Type type() { return type; }
    public Color color() { return type.color(); }
    public char symbol() { return type.symbol(); }
    public String name() { return type.typeName(); }
    public int xpValue() { return type.xpValue(); }
}
