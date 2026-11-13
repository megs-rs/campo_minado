package com.exemplo.minesweeper.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import com.exemplo.minesweeper.model.Board;
import com.exemplo.minesweeper.model.Cell;
import com.exemplo.minesweeper.model.CellState;
import com.exemplo.minesweeper.model.GameState;

public final class BoardRenderer implements Disposable {
    public static final float CELL_SIZE = 32f;
    public static final float MARGIN = 12f;

    private static final float BORDER = 3f;
    private static final float GRID_LINE = 1f;

    private static final Color BACKGROUND = new Color(0.72f, 0.72f, 0.72f, 1f);
    private static final Color COVERED_COLOR = new Color(0.78f, 0.78f, 0.78f, 1f);
    private static final Color LIGHT_COLOR = new Color(0.96f, 0.96f, 0.96f, 1f);
    private static final Color DARK_COLOR = new Color(0.42f, 0.42f, 0.42f, 1f);
    private static final Color OPEN_COLOR = new Color(0.84f, 0.84f, 0.84f, 1f);
    private static final Color OPEN_BORDER_COLOR = new Color(0.62f, 0.62f, 0.62f, 1f);
    private static final Color PRESSED_COLOR = new Color(0.80f, 0.80f, 0.80f, 1f);
    private static final Color EXPLODED_COLOR = new Color(0.92f, 0.20f, 0.20f, 1f);
    private static final Color FLAG_COLOR = new Color(0.85f, 0.10f, 0.10f, 1f);
    private static final Color QUESTION_COLOR = new Color(0.10f, 0.10f, 0.55f, 1f);
    private static final Color WRONG_FLAG_COLOR = new Color(0.90f, 0.00f, 0.00f, 1f);
    private static final Color POLE_COLOR = new Color(0.10f, 0.10f, 0.10f, 1f);
    private static final Color MINE_COLOR = new Color(0.05f, 0.05f, 0.05f, 1f);

    private static final Color[] NUMBER_COLORS = {
            Color.WHITE,
            new Color(0.00f, 0.00f, 0.90f, 1f),
            new Color(0.00f, 0.55f, 0.00f, 1f),
            new Color(0.90f, 0.00f, 0.00f, 1f),
            new Color(0.00f, 0.00f, 0.50f, 1f),
            new Color(0.55f, 0.00f, 0.00f, 1f),
            new Color(0.00f, 0.55f, 0.55f, 1f),
            new Color(0.00f, 0.00f, 0.00f, 1f),
            new Color(0.45f, 0.45f, 0.45f, 1f)
    };

    private final int columns;
    private final int rows;
    private final float worldWidth;
    private final float worldHeight;
    private final ShapeRenderer shapes;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final GlyphLayout layout;

    public BoardRenderer(int columns, int rows) {
        if (columns <= 0 || rows <= 0) {
            throw new IllegalArgumentException("Columns and rows must be positive");
        }

        this.columns = columns;
        this.rows = rows;
        this.worldWidth = columns * CELL_SIZE + MARGIN * 2f;
        this.worldHeight = rows * CELL_SIZE + MARGIN * 2f;
        this.shapes = new ShapeRenderer();
        this.batch = new SpriteBatch();
        this.font = new BitmapFont();
        this.font.getData().setScale(1.25f);
        this.layout = new GlyphLayout();
    }

    public float getWorldWidth() {
        return worldWidth;
    }

    public float getWorldHeight() {
        return worldHeight;
    }

    public int[] cellAt(float worldX, float worldY) {
        float boardWidth = columns * CELL_SIZE;
        float boardHeight = rows * CELL_SIZE;
        if (worldX < MARGIN || worldX >= MARGIN + boardWidth
                || worldY < MARGIN || worldY >= MARGIN + boardHeight) {
            return null;
        }

        int column = (int) ((worldX - MARGIN) / CELL_SIZE);
        int rowFromBottom = (int) ((worldY - MARGIN) / CELL_SIZE);
        return new int[]{column, rows - 1 - rowFromBottom};
    }

    public void render(Board board, GameState state, int pressedX, int pressedY, Matrix4 projection) {
        shapes.setProjectionMatrix(projection);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        shapes.setColor(BACKGROUND);
        shapes.rect(0f, 0f, worldWidth, worldHeight);
        shapes.setColor(OPEN_COLOR);
        shapes.rect(MARGIN, MARGIN, columns * CELL_SIZE, rows * CELL_SIZE);

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                Cell cell = board.get(x, y);
                float cellX = cellWorldX(x);
                float cellY = cellWorldY(y);
                boolean pressed = isPressed(cell, state, x, y, pressedX, pressedY);

                switch (cell.getState()) {
                    case COVERED -> {
                        if (pressed) {
                            drawPressedCell(cellX, cellY);
                        } else {
                            drawRaisedCell(cellX, cellY);
                        }
                    }
                    case FLAGGED -> {
                        drawRaisedCell(cellX, cellY);
                        drawFlag(cellX, cellY);
                    }
                    case QUESTION -> {
                        if (pressed) {
                            drawPressedCell(cellX, cellY);
                        } else {
                            drawRaisedCell(cellX, cellY);
                        }
                    }
                    case REVEALED -> drawOpenCell(cellX, cellY);
                    case EXPLODED -> {
                        drawOpenCell(cellX, cellY);
                        shapes.setColor(EXPLODED_COLOR);
                        shapes.rect(cellX + GRID_LINE, cellY + GRID_LINE,
                                CELL_SIZE - GRID_LINE * 2f, CELL_SIZE - GRID_LINE * 2f);
                        drawMine(cellX, cellY);
                    }
                    case MINE_SHOWN -> {
                        drawOpenCell(cellX, cellY);
                        drawMine(cellX, cellY);
                    }
                    case WRONG_FLAG -> {
                        drawOpenCell(cellX, cellY);
                        drawMine(cellX, cellY);
                        drawWrongMark(cellX, cellY);
                    }
                }
            }
        }

        shapes.end();

        batch.setProjectionMatrix(projection);
        batch.begin();
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                Cell cell = board.get(x, y);
                float cellX = cellWorldX(x);
                float cellY = cellWorldY(y);
                boolean pressed = isPressed(cell, state, x, y, pressedX, pressedY);

                if (cell.getState() == CellState.REVEALED && cell.getAdjacentMines() > 0) {
                    int number = cell.getAdjacentMines();
                    drawCenteredText(Integer.toString(number), cellX, cellY,
                            NUMBER_COLORS[Math.min(number, NUMBER_COLORS.length - 1)]);
                } else if (cell.getState() == CellState.QUESTION && !pressed) {
                    drawCenteredText("?", cellX, cellY, QUESTION_COLOR);
                }
            }
        }
        batch.end();
    }

    @Override
    public void dispose() {
        shapes.dispose();
        batch.dispose();
        font.dispose();
    }

    private float cellWorldX(int column) {
        return MARGIN + column * CELL_SIZE;
    }

    private float cellWorldY(int row) {
        return MARGIN + (rows - 1 - row) * CELL_SIZE;
    }

    private boolean isPressed(Cell cell, GameState state, int x, int y, int pressedX, int pressedY) {
        if (state == GameState.WON || state == GameState.LOST) {
            return false;
        }
        return x == pressedX && y == pressedY
                && (cell.getState() == CellState.COVERED || cell.getState() == CellState.QUESTION);
    }

    private void drawRaisedCell(float x, float y) {
        shapes.setColor(COVERED_COLOR);
        shapes.rect(x, y, CELL_SIZE, CELL_SIZE);

        shapes.setColor(LIGHT_COLOR);
        shapes.rect(x, y + CELL_SIZE - BORDER, CELL_SIZE - BORDER, BORDER);
        shapes.rect(x, y, BORDER, CELL_SIZE - BORDER);

        shapes.setColor(DARK_COLOR);
        shapes.rect(x + BORDER, y, CELL_SIZE - BORDER, BORDER);
        shapes.rect(x + CELL_SIZE - BORDER, y + BORDER, BORDER, CELL_SIZE - BORDER);
    }

    private void drawPressedCell(float x, float y) {
        shapes.setColor(PRESSED_COLOR);
        shapes.rect(x, y, CELL_SIZE, CELL_SIZE);

        shapes.setColor(DARK_COLOR);
        shapes.rect(x, y + CELL_SIZE - BORDER, CELL_SIZE - BORDER, BORDER);
        shapes.rect(x, y, BORDER, CELL_SIZE - BORDER);

        shapes.setColor(LIGHT_COLOR);
        shapes.rect(x + BORDER, y, CELL_SIZE - BORDER, BORDER);
        shapes.rect(x + CELL_SIZE - BORDER, y + BORDER, BORDER, CELL_SIZE - BORDER);
    }

    private void drawOpenCell(float x, float y) {
        shapes.setColor(OPEN_COLOR);
        shapes.rect(x, y, CELL_SIZE, CELL_SIZE);

        shapes.setColor(OPEN_BORDER_COLOR);
        shapes.rect(x, y, CELL_SIZE, GRID_LINE);
        shapes.rect(x, y + CELL_SIZE - GRID_LINE, CELL_SIZE, GRID_LINE);
        shapes.rect(x, y, GRID_LINE, CELL_SIZE);
        shapes.rect(x + CELL_SIZE - GRID_LINE, y, GRID_LINE, CELL_SIZE);
    }

    private void drawFlag(float x, float y) {
        float centerX = x + CELL_SIZE / 2f;
        float centerY = y + CELL_SIZE / 2f;

        shapes.setColor(POLE_COLOR);
        shapes.rectLine(centerX - 4f, centerY - 9f, centerX - 4f, centerY + 10f, 2f);
        shapes.rect(centerX - 9f, centerY - 10f, 10f, 2f);

        shapes.setColor(FLAG_COLOR);
        shapes.triangle(centerX - 3f, centerY + 10f,
                centerX + 10f, centerY + 5f,
                centerX - 3f, centerY);
    }

    private void drawMine(float x, float y) {
        float centerX = x + CELL_SIZE / 2f;
        float centerY = y + CELL_SIZE / 2f;
        float radius = CELL_SIZE * 0.30f;

        shapes.setColor(MINE_COLOR);
        shapes.circle(centerX, centerY, CELL_SIZE * 0.18f, 16);
        shapes.rectLine(centerX - radius, centerY, centerX + radius, centerY, 2f);
        shapes.rectLine(centerX, centerY - radius, centerX, centerY + radius, 2f);

        float diagonal = radius * 0.70f;
        shapes.rectLine(centerX - diagonal, centerY - diagonal,
                centerX + diagonal, centerY + diagonal, 2f);
        shapes.rectLine(centerX - diagonal, centerY + diagonal,
                centerX + diagonal, centerY - diagonal, 2f);
    }

    private void drawWrongMark(float x, float y) {
        shapes.setColor(WRONG_FLAG_COLOR);
        shapes.rectLine(x + 8f, y + 8f, x + CELL_SIZE - 8f, y + CELL_SIZE - 8f, 3f);
        shapes.rectLine(x + 8f, y + CELL_SIZE - 8f, x + CELL_SIZE - 8f, y + 8f, 3f);
    }

    private void drawCenteredText(String text, float cellX, float cellY, Color color) {
        font.setColor(color);
        layout.setText(font, text);
        font.draw(batch, layout,
                cellX + (CELL_SIZE - layout.width) / 2f,
                cellY + (CELL_SIZE + layout.height) / 2f);
    }
}
