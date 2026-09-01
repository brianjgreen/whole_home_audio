package dungeon;

public record Room(int x, int y, int width, int height) {

    public int centerX() { return x + width / 2; }
    public int centerY() { return y + height / 2; }

    public int x2() { return x + width; }
    public int y2() { return y + height; }

    public boolean intersects(Room other) {
        return x < other.x2() && x2() > other.x
            && y < other.y2() && y2() > other.y;
    }
}
