package com.exemplo.minesweeper.model;

import java.util.Objects;

public final class Cell {
    private boolean mine;
    private int adjacentMines;
    private CellState state = CellState.COVERED;
    private boolean exploded;

    public boolean hasMine() {
        return mine;
    }

    public int getAdjacentMines() {
        return adjacentMines;
    }

    public CellState getState() {
        return state;
    }

    public boolean isExploded() {
        return exploded;
    }

    void setMine(boolean mine) {
        this.mine = mine;
    }

    void setAdjacentMines(int adjacentMines) {
        if (adjacentMines < 0 || adjacentMines > 8) {
            throw new IllegalArgumentException("Adjacent mine count must be between 0 and 8");
        }
        this.adjacentMines = adjacentMines;
    }

    void setState(CellState state) {
        this.state = Objects.requireNonNull(state, "state");
    }

    void setExploded(boolean exploded) {
        this.exploded = exploded;
    }
}
