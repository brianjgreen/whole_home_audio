package com.gameoflife;

import java.util.Arrays;
import java.util.random.RandomGenerator;

public class Grid {

    private final int rows;
    private final int cols;
    private boolean[][] cells;
    private long generation;

    public Grid(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.cells = new boolean[rows][cols];
        this.generation = 0;
    }

    public Grid(boolean[][] initial) {
        this.rows = initial.length;
        this.cols = initial[0].length;
        this.cells = deepCopy(initial);
        this.generation = 0;
    }

    public int rows() {
        return rows;
    }

    public int cols() {
        return cols;
    }

    public long generation() {
        return generation;
    }

    public boolean isAlive(int row, int col) {
        return cells[row][col];
    }

    public void setAlive(int row, int col, boolean alive) {
        cells[row][col] = alive;
    }

    public int countNeighbors(int row, int col) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int nr = (row + dr + rows) % rows;
                int nc = (col + dc + cols) % cols;
                if (cells[nr][nc]) count++;
            }
        }
        return count;
    }

    public Grid nextGeneration() {
        boolean[][] next = new boolean[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int neighbors = countNeighbors(r, c);
                next[r][c] = cells[r][c] ? (neighbors == 2 || neighbors == 3) : (neighbors == 3);
            }
        }
        Grid result = new Grid(next);
        result.generation = this.generation + 1;
        return result;
    }

    public void clear() {
        for (boolean[] row : cells) {
            Arrays.fill(row, false);
        }
        generation = 0;
    }

    public void randomize(double density) {
        var rng = RandomGenerator.of("L64X128MixRandom");
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                cells[r][c] = rng.nextDouble() < density;
            }
        }
        generation = 0;
    }

    public int population() {
        int count = 0;
        for (boolean[] row : cells) {
            for (boolean cell : row) {
                if (cell) count++;
            }
        }
        return count;
    }

    public Grid copy() {
        return new Grid(deepCopy(cells));
    }

    public void copyFrom(Grid other) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                cells[r][c] = other.isAlive(r, c);
            }
        }
        this.generation = 0;
    }

    private static boolean[][] deepCopy(boolean[][] array) {
        return Arrays.stream(array)
                .map(boolean[]::clone)
                .toArray(boolean[][]::new);
    }

    public static Grid glider() {
        Grid g = new Grid(20, 40);
        g.setAlive(0, 2, true);
        g.setAlive(1, 0, true);
        g.setAlive(1, 2, true);
        g.setAlive(2, 1, true);
        g.setAlive(2, 2, true);
        return g;
    }

    public static Grid pulsar() {
        Grid g = new Grid(20, 40);
        int[] pattern = {2, 3, 4, 8, 9, 10};
        int offsetR = 5;
        int offsetC = 10;
        for (int c : pattern) {
            g.setAlive(offsetR, offsetC + c, true);
            g.setAlive(offsetR + 5, offsetC + c, true);
        }
        for (int r : new int[]{3, 4, 5, 7, 8, 9}) {
            for (int c : new int[]{2, 7, 10, 15}) {
                g.setAlive(offsetR + r, offsetC + c, true);
            }
        }
        return g;
    }

    public static Grid gosperGliderGun() {
        Grid g = new Grid(40, 60);
        var coords = new int[][]{
                {4, 0}, {4, 1}, {5, 0}, {5, 1},
                {4, 10}, {5, 10}, {6, 10},
                {3, 11}, {7, 11},
                {2, 12}, {8, 12},
                {2, 13}, {8, 13},
                {5, 14},
                {3, 15}, {7, 15},
                {4, 16}, {5, 16}, {6, 16},
                {5, 17},
                {2, 20}, {3, 20}, {4, 20},
                {2, 21}, {3, 21}, {4, 21},
                {1, 22}, {5, 22},
                {0, 24}, {1, 24}, {5, 24}, {6, 24},
                {2, 34}, {3, 34}, {2, 35}, {3, 35}
        };
        for (var coord : coords) {
            g.setAlive(coord[0], coord[1], true);
        }
        return g;
    }
}
