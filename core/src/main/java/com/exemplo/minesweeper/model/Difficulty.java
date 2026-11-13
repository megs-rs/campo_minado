package com.exemplo.minesweeper.model;

public enum Difficulty implements BoardConfig {
    BEGINNER("Iniciante", 9, 9, 10),
    INTERMEDIATE("Intermediário", 16, 16, 40),
    EXPERT("Avançado", 30, 16, 99);

    private final String displayName;
    private final int columns;
    private final int rows;
    private final int mines;

    Difficulty(String displayName, int columns, int rows, int mines) {
        this.displayName = displayName;
        this.columns = columns;
        this.rows = rows;
        this.mines = mines;
    }

    @Override
    public String displayName() {
        return displayName;
    }

    @Override
    public int columns() {
        return columns;
    }

    @Override
    public int rows() {
        return rows;
    }

    @Override
    public int mines() {
        return mines;
    }
}
