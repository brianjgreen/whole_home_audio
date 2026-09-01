package com.jeweled;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The game model: an 8x8 grid of gems, match detection, cascading
 * refills, scoring, and move legality checks. The board is stored with
 * row 0 at the TOP and column 0 at the LEFT.
 */
public class Board {

    public static final int ROWS = 8;
    public static final int COLS = 8;

    private final GemType[][] grid;
    private final Random random = new Random();
    private int score;
    private int combo;

    public Board() {
        grid = new GemType[ROWS][COLS];
        fillBoard();
    }

    /** Fill the board so that no initial matches exist. */
    private void fillBoard() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                GemType cell;
                do {
                    cell = randomType();
                } while (wouldMatch(r, c, cell));
                grid[r][c] = cell;
            }
        }
    }

    private GemType randomType() {
        GemType[] vals = GemType.values();
        return vals[random.nextInt(vals.length)];
    }

    /** Would placing {@code type} at (r,c) create a match with neighbors? */
    private boolean wouldMatch(int r, int c, GemType type) {
        if (c >= 2 && grid[r][c - 1] == type && grid[r][c - 2] == type) return true;
        if (r >= 2 && grid[r - 1][c] == type && grid[r - 2][c] == type) return true;
        return false;
    }

    public GemType get(int r, int c) {
        return grid[r][c];
    }

    public boolean isEmpty(int r, int c) {
        return grid[r][c] == null;
    }

    public void set(int r, int c, GemType type) {
        grid[r][c] = type;
    }

    public int getScore() {
        return score;
    }

    /** Attempt a swap. Returns true if a match is made (legal move). */
    public boolean swapAndMatch(int r1, int c1, int r2, int c2) {
        swap(r1, c1, r2, c2);
        List<Match> matches = findAllMatches();
        if (matches.isEmpty()) {
            swap(r1, c1, r2, c2);
            return false;
        }
        combo = 0;
        resolveMatches();
        return true;
    }

    public boolean isAdjacent(int r1, int c1, int r2, int c2) {
        return Math.abs(r1 - r2) + Math.abs(c1 - c2) == 1;
    }

    private void swap(int r1, int c1, int r2, int c2) {
        GemType t = grid[r1][c1];
        grid[r1][c1] = grid[r2][c2];
        grid[r2][c2] = t;
    }

    public static class Match {
        public final int row;
        public final int col;
        public Match(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    public List<Match> findAllMatches() {
        List<Match> matches = new ArrayList<>();
        boolean[][] marked = new boolean[ROWS][COLS];

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                GemType t = grid[r][c];
                if (t == null) continue;
                // horizontal run
                int cRun = 1;
                while (c + cRun < COLS && grid[r][c + cRun] == t) cRun++;
                if (cRun >= 3) {
                    for (int k = 0; k < cRun; k++) marked[r][c + k] = true;
                }
                // vertical run
                int rRun = 1;
                while (r + rRun < ROWS && grid[r + rRun][c] == t) rRun++;
                if (rRun >= 3) {
                    for (int k = 0; k < rRun; k++) marked[r + k][c] = true;
                }
            }
        }

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (marked[r][c]) matches.add(new Match(r, c));
            }
        }
        return matches;
    }

    /**
     * Resolve matches and cascade until the board is stable.
     * Returns the total number of cleared gem-cells (for scoring).
     */
    public int resolveMatches() {
        int totalCleared = 0;
        List<Match> matches = findAllMatches();
        while (!matches.isEmpty()) {
            combo++;
            int cleared = matches.size();
            totalCleared += cleared;
            score += cleared * 10 * combo;
            for (Match m : matches) {
                grid[m.row][m.col] = null;
            }
            applyGravity();
            refillEmpty();
            matches = findAllMatches();
            if (!matches.isEmpty()) {
                // pause a moment so the cascade is visible; handled by animation layer
            }
        }
        return totalCleared;
    }

    /** Shift gems downward into empty cells (row 0 is top). */
    public void applyGravity() {
        for (int c = 0; c < COLS; c++) {
            int writeRow = ROWS - 1;
            for (int r = ROWS - 1; r >= 0; r--) {
                if (grid[r][c] != null) {
                    grid[writeRow][c] = grid[r][c];
                    if (writeRow != r) grid[r][c] = null;
                    writeRow--;
                }
            }
            for (int r = writeRow; r >= 0; r--) {
                grid[r][c] = null;
            }
        }
    }

    /** Fill empty cells from the top with fresh random gems. */
    public void refillEmpty() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c] == null) {
                    grid[r][c] = randomType();
                }
            }
        }
    }

    /** After refill, remove any accidental new matches (top rows) - keeps board clean. */
    public void cleanNewMatches() {
        List<Match> matches = findAllMatches();
        int guard = 0;
        while (!matches.isEmpty() && guard++ < ROWS * COLS) {
            for (Match m : matches) {
                grid[m.row][m.col] = randomType();
            }
            matches = findAllMatches();
        }
    }

    /** Is there at least one legal move on the current board? */
    public boolean hasAnyMove() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (trySwap(r, c, r, c + 1) || trySwap(r, c, r + 1, c)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean trySwap(int r1, int c1, int r2, int c2) {
        if (r2 < 0 || r2 >= ROWS || c2 < 0 || c2 >= COLS) return false;
        swap(r1, c1, r2, c2);
        boolean matched = !findAllMatches().isEmpty();
        swap(r1, c1, r2, c2);
        return matched;
    }

    public void reshuffle() {
        // Simplest robust approach: refill the whole board with no initial matches.
        fillBoard();
    }
}
