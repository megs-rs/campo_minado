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

    private final GameLayout layout;
    private final int columns;
    private final int rows;
    private final float cellSize;
    private final float border;
    private final float gridLine;
    private final ShapeRenderer shapes;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final GlyphLayout glyphLayout;

    public BoardRenderer(GameLayout layout) {
        this.layout = layout;
        this.columns = layout.getColumns();
        this.rows = layout.getRows();
        this.cellSize = layout.getCellSize();
        this.border = 3f * layout.getScale();
        this.gridLine = Math.max(1f, layout.getScale());
        this.shapes = new ShapeRenderer();
        this.batch = new SpriteBatch();
        this.font = new BitmapFont();
        this.font.getData().setScale(1.25f * layout.getScale());
        this.glyphLayout = new GlyphLayout();
    }

    public int[] cellAt(float worldX, float worldY) {
        float boardX = layout.getBoardX();
        float boardY = layout.getBoardY();
        if (worldX < boardX || worldX >= boardX + layout.getBoardWidth()
                || worldY < boardY || worldY >= boardY + layout.getBoardHeight()) {
            return null;
        }

        int column = (int) ((worldX - boardX) / cellSize);
        int rowFromBottom = (int) ((worldY - boardY) / cellSize);
        return new int[]{column, rows - 1 - rowFromBottom};
    }

    public void render(Board board, GameState state, int pressedX, int pressedY,
                       int chordX, int chordY, Matrix4 projection) {
        shapes.setProjectionMatrix(projection);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        shapes.setColor(BACKGROUND);
        shapes.rect(0f, 0f, layout.getWorldWidth(), layout.getWorldHeight());
        shapes.setColor(OPEN_COLOR);
        shapes.rect(layout.getBoardX(), layout.getBoardY(),
                layout.getBoardWidth(), layout.getBoardHeight());

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                Cell cell = board.get(x, y);
                float cellX = cellWorldX(x);
                float cellY = cellWorldY(y);
                boolean pressed = isPressed(cell, state, x, y,
                        pressedX, pressedY, chordX, chordY);

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
                        shapes.rect(cellX + gridLine, cellY + gridLine,
                                cellSize - gridLine * 2f, cellSize - gridLine * 2f);
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
                boolean pressed = isPressed(cell, state, x, y,
                        pressedX, pressedY, chordX, chordY);

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
        return layout.getBoardX() + column * cellSize;
    }

    private float cellWorldY(int row) {
        return layout.getBoardY() + (rows - 1 - row) * cellSize;
    }

    private boolean isPressed(Cell cell, GameState state, int x, int y,
                              int pressedX, int pressedY, int chordX, int chordY) {
        if (state == GameState.WON || state == GameState.LOST) {
            return false;
        }
        if (cell.getState() != CellState.COVERED && cell.getState() != CellState.QUESTION) {
            return false;
        }
        if (chordX >= 0 && chordY >= 0) {
            return Math.abs(x - chordX) <= 1 && Math.abs(y - chordY) <= 1
                    && (x != chordX || y != chordY);
        }
        return x == pressedX && y == pressedY;
    }

    private void drawRaisedCell(float x, float y) {
        shapes.setColor(COVERED_COLOR);
        shapes.rect(x, y, cellSize, cellSize);

        shapes.setColor(LIGHT_COLOR);
        shapes.rect(x, y + cellSize - border, cellSize - border, border);
        shapes.rect(x, y, border, cellSize - border);

        shapes.setColor(DARK_COLOR);
        shapes.rect(x + border, y, cellSize - border, border);
        shapes.rect(x + cellSize - border, y + border, border, cellSize - border);
    }

    private void drawPressedCell(float x, float y) {
        shapes.setColor(PRESSED_COLOR);
        shapes.rect(x, y, cellSize, cellSize);

        shapes.setColor(DARK_COLOR);
        shapes.rect(x, y + cellSize - border, cellSize - border, border);
        shapes.rect(x, y, border, cellSize - border);

        shapes.setColor(LIGHT_COLOR);
        shapes.rect(x + border, y, cellSize - border, border);
        shapes.rect(x + cellSize - border, y + border, border, cellSize - border);
    }

    private void drawOpenCell(float x, float y) {
        shapes.setColor(OPEN_COLOR);
        shapes.rect(x, y, cellSize, cellSize);

        shapes.setColor(OPEN_BORDER_COLOR);
        shapes.rect(x, y, cellSize, gridLine);
        shapes.rect(x, y + cellSize - gridLine, cellSize, gridLine);
        shapes.rect(x, y, gridLine, cellSize);
        shapes.rect(x + cellSize - gridLine, y, gridLine, cellSize);
    }

    private void drawFlag(float x, float y) {
        float centerX = x + cellSize / 2f;
        float centerY = y + cellSize / 2f;
        float scale = layout.getScale();

        shapes.setColor(POLE_COLOR);
        shapes.rectLine(centerX - 4f * scale, centerY - 9f * scale,
                centerX - 4f * scale, centerY + 10f * scale, 2f * scale);
        shapes.rect(centerX - 9f * scale, centerY - 10f * scale, 10f * scale, 2f * scale);

        shapes.setColor(FLAG_COLOR);
        shapes.triangle(centerX - 3f * scale, centerY + 10f * scale,
                centerX + 10f * scale, centerY + 5f * scale,
                centerX - 3f * scale, centerY);
    }

    private void drawMine(float x, float y) {
        float centerX = x + cellSize / 2f;
        float centerY = y + cellSize / 2f;
        float radius = cellSize * 0.30f;
        float scale = layout.getScale();

        shapes.setColor(MINE_COLOR);
        shapes.circle(centerX, centerY, cellSize * 0.18f, 16);
        shapes.rectLine(centerX - radius, centerY, centerX + radius, centerY, 2f * scale);
        shapes.rectLine(centerX, centerY - radius, centerX, centerY + radius, 2f * scale);

        float diagonal = radius * 0.70f;
        shapes.rectLine(centerX - diagonal, centerY - diagonal,
                centerX + diagonal, centerY + diagonal, 2f * scale);
        shapes.rectLine(centerX - diagonal, centerY + diagonal,
                centerX + diagonal, centerY - diagonal, 2f * scale);
    }

    private void drawWrongMark(float x, float y) {
        float padding = 8f * layout.getScale();
        float size = cellSize - padding * 2f;

        shapes.setColor(WRONG_FLAG_COLOR);
        shapes.rectLine(x + padding, y + padding, x + padding + size, y + padding + size,
                3f * layout.getScale());
        shapes.rectLine(x + padding, y + padding + size, x + padding + size, y + padding,
                3f * layout.getScale());
    }

    private void drawCenteredText(String text, float cellX, float cellY, Color color) {
        font.setColor(color);
        glyphLayout.setText(font, text);
        font.draw(batch, glyphLayout,
                cellX + (cellSize - glyphLayout.width) / 2f,
                cellY + (cellSize + glyphLayout.height) / 2f);
    }
}
