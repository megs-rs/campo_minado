package com.exemplo.minesweeper.model;

public record CustomDifficulty(int columns, int rows, int mines) implements BoardConfig {
    public static final int MIN_COLUMNS = 9;
    public static final int MAX_COLUMNS = 30;
    public static final int MIN_ROWS = 9;
    public static final int MAX_ROWS = 24;
    public static final int MIN_MINES = 10;

    public CustomDifficulty {
        columns = clamp(columns, MIN_COLUMNS, MAX_COLUMNS);
        rows = clamp(rows, MIN_ROWS, MAX_ROWS);
        mines = clamp(mines, MIN_MINES, (columns - 1) * (rows - 1));
    }

    @Override
    public String displayName() {
        return "Personalizado";
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }
}
