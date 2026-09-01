package com.gameoflife;

public record Cell(int row, int col) {

    public Cell {
        if (row < 0 || col < 0) {
            throw new IllegalArgumentException("Cell coordinates must be non-negative");
        }
    }

    public Cell neighbors(int maxRow, int maxCol) {
        return new Cell(row, col);
    }

    public Iterable<Cell> neighborPositions(int maxRow, int maxCol) {
        return () -> new java.util.Iterator<>() {
            private int dr = -1;
            private int dc = -1;
            private int nextRow = row + dr;
            private int nextCol = col + dc;

            @Override
            public boolean hasNext() {
                while (dr <= 1) {
                    while (dc <= 1) {
                        if (dr == 0 && dc == 0) {
                            dc++;
                            continue;
                        }
                        int nr = row + dr;
                        int nc = col + dc;
                        if (nr >= 0 && nr < maxRow && nc >= 0 && nc < maxCol) {
                            return true;
                        }
                        dc++;
                    }
                    dr++;
                    dc = -1;
                }
                return false;
            }

            @Override
            public Cell next() {
                while (dr <= 1) {
                    while (dc <= 1) {
                        if (dr == 0 && dc == 0) {
                            dc++;
                            continue;
                        }
                        int nr = row + dr;
                        int nc = col + dc;
                        if (nr >= 0 && nr < maxRow && nc >= 0 && nc < maxCol) {
                            dc++;
                            if (dc > 1) {
                                dc = -1;
                                dr++;
                            }
                            return new Cell(nr, nc);
                        }
                        dc++;
                    }
                    dr++;
                    dc = -1;
                }
                throw new java.util.NoSuchElementException();
            }
        };
    }
}
