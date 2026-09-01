package dungeon;

import java.awt.Color;

public class Player {

    private int x, y;
    private int hp, maxHp;
    private int attack, defense;
    private int level, xp, xpToNext;
    private int floor;
    private int potions;

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
        this.hp = 30;
        this.maxHp = 30;
        this.attack = 5;
        this.defense = 2;
        this.level = 1;
        this.xp = 0;
        this.xpToNext = 20;
        this.floor = 1;
        this.potions = 3;
    }

    public void move(int dx, int dy, DungeonMap map) {
        int nx = x + dx;
        int ny = y + dy;
        if (map.inBounds(nx, ny) && !map.getTile(nx, ny).isBlocked()) {
            x = nx;
            y = ny;
        }
    }

    public boolean usePotion() {
        if (potions <= 0 || hp >= maxHp) return false;
        potions--;
        hp = Math.min(maxHp, hp + 12);
        return true;
    }

    public int attackDamage() {
        return attack + level;
    }

    public void takeDamage(int damage) {
        int actual = Math.max(1, damage - defense);
        hp = Math.max(0, hp - actual);
    }

    public void gainXp(int amount) {
        xp += amount;
        while (xp >= xpToNext) {
            xp -= xpToNext;
            level++;
            maxHp += 5;
            hp = Math.min(hp + 5, maxHp);
            attack += 2;
            defense += 1;
            xpToNext = (int)(xpToNext * 1.5);
        }
    }

    public boolean isDead() { return hp <= 0; }
    public int x() { return x; }
    public int y() { return y; }
    public int hp() { return hp; }
    public int maxHp() { return maxHp; }
    public int attack() { return attack + level; }
    public int defense() { return defense; }
    public int level() { return level; }
    public int xp() { return xp; }
    public int xpToNext() { return xpToNext; }
    public int floor() { return floor; }
    public int potions() { return potions; }
    public Color color() { return new Color(80, 140, 255); }
    public char symbol() { return '@'; }

    public void setFloor(int f) { this.floor = f; }
    public void setPos(int nx, int ny) { this.x = nx; this.y = ny; }
    public void addPotion() { potions++; }
}
