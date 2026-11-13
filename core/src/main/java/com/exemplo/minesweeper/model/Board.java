package com.exemplo.minesweeper.model;

import java.util.Objects;

public final class Board {
    private int columns;
    private int rows;
    private int mines;
    private Cell[] cells;
    private int revealedCount;
    private int flagCount;

    public Board(int columns, int rows, int mines) {
        reset(columns, rows, mines);
    }

    public Board(BoardConfig config) {
        this(config.columns(), config.rows(), config.mines());
    }

    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }

    public int getMines() {
        return mines;
    }

    public int getRevealedCount() {
        return revealedCount;
    }

    public int getFlagCount() {
        return flagCount;
    }

    public boolean inBounds(int x, int y) {
        return x >= 0 && x < columns && y >= 0 && y < rows;
    }

    public int indexOf(int x, int y) {
        return y * columns + x;
    }

    public Cell get(int x, int y) {
        if (!inBounds(x, y)) {
            throw new IndexOutOfBoundsException("Cell outside board: (" + x + ", " + y + ")");
        }
        return cells[indexOf(x, y)];
    }

    public void forEachNeighbor(int x, int y, IntBiConsumer action) {
        Objects.requireNonNull(action, "action");
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                int neighborX = x + dx;
                int neighborY = y + dy;
                if (inBounds(neighborX, neighborY)) {
                    action.accept(neighborX, neighborY);
                }
            }
        }
    }

    public int countFlagsAround(int x, int y) {
        int[] count = {0};
        forEachNeighbor(x, y, (neighborX, neighborY) -> {
            if (get(neighborX, neighborY).getState() == CellState.FLAGGED) {
                count[0]++;
            }
        });
        return count[0];
    }

    public void setState(int x, int y, CellState state) {
        Objects.requireNonNull(state, "state");
        Cell cell = get(x, y);
        CellState previous = cell.getState();

        if (previous == CellState.REVEALED && state != CellState.REVEALED) {
            revealedCount--;
        } else if (previous != CellState.REVEALED && state == CellState.REVEALED) {
            revealedCount++;
        }

        if (isFlagState(previous) && !isFlagState(state)) {
            flagCount--;
        } else if (!isFlagState(previous) && isFlagState(state)) {
            flagCount++;
        }

        cell.setState(state);
    }

    public void setMine(int x, int y, boolean mine) {
        get(x, y).setMine(mine);
    }

    public void clearMines() {
        for (Cell cell : cells) {
            cell.setMine(false);
            cell.setAdjacentMines(0);
        }
    }

    public void recalculateAdjacentMines() {
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                cells[indexOf(x, y)].setAdjacentMines(countMinesAround(x, y));
            }
        }
    }

    public void reset(int columns, int rows, int mines) {
        validateDimensions(columns, rows, mines);
        this.columns = columns;
        this.rows = rows;
        this.mines = mines;
        this.cells = new Cell[columns * rows];
        for (int i = 0; i < cells.length; i++) {
            cells[i] = new Cell();
        }
        this.revealedCount = 0;
        this.flagCount = 0;
    }

    private int countMinesAround(int x, int y) {
        int[] count = {0};
        forEachNeighbor(x, y, (neighborX, neighborY) -> {
            if (get(neighborX, neighborY).hasMine()) {
                count[0]++;
            }
        });
        return count[0];
    }

    private static boolean isFlagState(CellState state) {
        return state == CellState.FLAGGED || state == CellState.WRONG_FLAG;
    }

    private static void validateDimensions(int columns, int rows, int mines) {
        if (columns <= 0 || rows <= 0) {
            throw new IllegalArgumentException("Columns and rows must be positive");
        }
        long totalCells = (long) columns * rows;
        if (mines < 0 || mines >= totalCells) {
            throw new IllegalArgumentException("Mines must be between 0 and total cells - 1");
        }
    }
}
