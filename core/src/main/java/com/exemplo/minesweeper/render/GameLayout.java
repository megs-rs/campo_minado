package com.exemplo.minesweeper.render;

public final class GameLayout {
    public static final float BASE_CELL_SIZE = 32f;
    public static final float BASE_MARGIN = 12f;
    public static final float BASE_HUD_HEIGHT = 64f;

    private final int columns;
    private final int rows;
    private final float scale;
    private final float cellSize;
    private final float margin;
    private final float hudHeight;
    private final float boardX;
    private final float boardY;
    private final float boardWidth;
    private final float boardHeight;
    private final float panelX;
    private final float panelY;
    private final float panelWidth;
    private final float panelHeight;
    private final float worldWidth;
    private final float worldHeight;

    public GameLayout(int columns, int rows, float scale) {
        if (columns <= 0 || rows <= 0) {
            throw new IllegalArgumentException("Columns and rows must be positive");
        }
        if (!(scale > 0f)) {
            throw new IllegalArgumentException("Scale must be positive");
        }

        this.columns = columns;
        this.rows = rows;
        this.scale = scale;
        this.cellSize = BASE_CELL_SIZE * scale;
        this.margin = BASE_MARGIN * scale;
        this.hudHeight = BASE_HUD_HEIGHT * scale;
        this.boardWidth = columns * cellSize;
        this.boardHeight = rows * cellSize;
        this.boardX = margin;
        this.boardY = margin;
        this.panelWidth = boardWidth;
        this.panelHeight = hudHeight;
        this.panelX = boardX;
        this.panelY = boardY + boardHeight + margin;
        this.worldWidth = boardWidth + margin * 2f;
        this.worldHeight = panelY + panelHeight + margin;
    }

    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }

    public float getScale() {
        return scale;
    }

    public float getCellSize() {
        return cellSize;
    }

    public float getMargin() {
        return margin;
    }

    public float getHudHeight() {
        return hudHeight;
    }

    public float getBoardX() {
        return boardX;
    }

    public float getBoardY() {
        return boardY;
    }

    public float getBoardWidth() {
        return boardWidth;
    }

    public float getBoardHeight() {
        return boardHeight;
    }

    public float getPanelX() {
        return panelX;
    }

    public float getPanelY() {
        return panelY;
    }

    public float getPanelWidth() {
        return panelWidth;
    }

    public float getPanelHeight() {
        return panelHeight;
    }

    public float getWorldWidth() {
        return worldWidth;
    }

    public float getWorldHeight() {
        return worldHeight;
    }
}
