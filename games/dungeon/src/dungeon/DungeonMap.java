package dungeon;

import java.util.ArrayList;
// import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DungeonMap {

    private final int width, height;
    private final Tile[][] tiles;
    private final boolean[][] visible;
    private final boolean[][] explored;
    private final List<Room> rooms;
    private final Random rng;
    private int stairsDownX, stairsDownY;
    private int stairsUpX, stairsUpY;

    public DungeonMap(int width, int height, int floor) {
        this.width = width;
        this.height = height;
        this.tiles = new Tile[width][height];
        this.visible = new boolean[width][height];
        this.explored = new boolean[width][height];
        this.rooms = new ArrayList<>();
        this.rng = new Random();
        generate(floor);
    }

    private void generate(int floor) {
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                tiles[x][y] = Tile.WALL;

        int numRooms = 8 + rng.nextInt(4) + floor;
        int minSize = 4, maxSize = 10;
        int attempts = 0;

        while (rooms.size() < numRooms && attempts < 500) {
            int w = rng.nextInt(maxSize - minSize + 1) + minSize;
            int h = rng.nextInt(maxSize - minSize + 1) + minSize;
            int rx = rng.nextInt(width - w - 2) + 1;
            int ry = rng.nextInt(height - h - 2) + 1;
            Room room = new Room(rx, ry, w, h);

            boolean overlap = false;
            for (Room existing : rooms) {
                if (room.intersects(existing)) { overlap = true; break; }
            }
            if (!overlap) {
                carveRoom(room);
                rooms.add(room);
            }
            attempts++;
        }

        for (int i = 1; i < rooms.size(); i++) {
            carveCorridor(rooms.get(i - 1), rooms.get(i));
        }

        Room first = rooms.getFirst();
        Room last = rooms.getLast();
        stairsUpX = first.centerX();
        stairsUpY = first.centerY();
        stairsDownX = last.centerX();
        stairsDownY = last.centerY();
        tiles[stairsDownX][stairsDownY] = Tile.STAIRS_DOWN;
    }

    private void carveRoom(Room room) {
        for (int x = room.x(); x < room.x2(); x++)
            for (int y = room.y(); y < room.y2(); y++)
                tiles[x][y] = Tile.FLOOR;
    }

    private void carveCorridor(Room a, Room b) {
        int x = a.centerX(), y = a.centerY();
        int tx = b.centerX(), ty = b.centerY();

        while (x != tx) {
            tiles[x][y] = (tiles[x][y] == Tile.WALL) ? Tile.CORRIDOR : tiles[x][y];
            x += Integer.signum(tx - x);
        }
        while (y != ty) {
            tiles[x][y] = (tiles[x][y] == Tile.WALL) ? Tile.CORRIDOR : tiles[x][y];
            y += Integer.signum(ty - y);
        }
    }

    public void computeFOV(int px, int py, int radius) {
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                visible[x][y] = false;

        for (int angle = 0; angle < 360; angle++) {
            double rad = Math.toRadians(angle);
            double dx = Math.cos(rad) * 0.5;
            double dy = Math.sin(rad) * 0.5;
            double cx = px + 0.5, cy = py + 0.5;

            for (int i = 0; i < radius * 2; i++) {
                int ix = (int) cx;
                int iy = (int) cy;
                if (!inBounds(ix, iy)) break;

                visible[ix][iy] = true;
                explored[ix][iy] = true;

                if (tiles[ix][iy] == Tile.WALL) break;
                cx += dx;
                cy += dy;
            }
        }
    }

    public void spawnEnemies(List<Enemy> enemies, int floor) {
        for (int i = 1; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            int count = 1 + rng.nextInt(2 + floor / 2);
            for (int j = 0; j < count; j++) {
                int ex = room.x() + rng.nextInt(room.width());
                int ey = room.y() + rng.nextInt(room.height());
                if (tiles[ex][ey] == Tile.FLOOR) {
                    Enemy.Type type = pickEnemyType(floor, rng);
                    enemies.add(new Enemy(ex, ey, type, floor));
                }
            }
        }
    }

    public void spawnItems(List<int[]> items, int floor) {
        for (Room room : rooms) {
            if (rng.nextInt(3) == 0) {
                int ix = room.x() + rng.nextInt(room.width());
                int iy = room.y() + rng.nextInt(room.height());
                if (tiles[ix][iy] == Tile.FLOOR) {
                    items.add(new int[]{ix, iy, rng.nextInt(3)}); // 0=potion, 1=potion, 2=xp
                }
            }
        }
    }

    private Enemy.Type pickEnemyType(int floor, Random rng) {
        Enemy.Type[] types = Enemy.Type.values();
        int maxIndex = Math.min(types.length, 1 + floor);
        // Weight toward lower-level enemies
        int index = (int)(Math.pow(rng.nextDouble(), 0.7) * maxIndex);
        return types[Math.min(index, maxIndex - 1)];
    }

    public boolean inBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public Tile getTile(int x, int y) {
        return inBounds(x, y) ? tiles[x][y] : Tile.VOID;
    }

    public boolean isVisible(int x, int y) {
        return inBounds(x, y) && visible[x][y];
    }

    public boolean isExplored(int x, int y) {
        return inBounds(x, y) && explored[x][y];
    }

    public List<Room> rooms() { return rooms; }
    public int width() { return width; }
    public int height() { return height; }
    public int stairsDownX() { return stairsDownX; }
    public int stairsDownY() { return stairsDownY; }
    public int stairsUpX() { return stairsUpX; }
    public int stairsUpY() { return stairsUpY; }
}
