package com.gameoflife;

public class GameOfLife {

    private Grid grid;
    private boolean running;
    private long delayMs;

    public GameOfLife(Grid grid) {
        this.grid = grid;
        this.running = false;
        this.delayMs = 100;
    }

    public Grid grid() {
        return grid;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void toggleRunning() {
        this.running = !this.running;
    }

    public long delayMs() {
        return delayMs;
    }

    public void setDelayMs(long delayMs) {
        this.delayMs = delayMs;
    }

    public void step() {
        this.grid = grid.nextGeneration();
    }

    public void setGrid(Grid grid) {
        this.grid = grid;
        this.running = false;
    }
}
