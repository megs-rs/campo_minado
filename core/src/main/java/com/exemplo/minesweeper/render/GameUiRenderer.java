package com.exemplo.minesweeper.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import com.exemplo.minesweeper.model.GameEngine;
import com.exemplo.minesweeper.model.GameState;

public final class GameUiRenderer implements Disposable {
    private static final Color PANEL_COLOR = new Color(0.78f, 0.78f, 0.78f, 1f);
    private static final Color PANEL_LIGHT = new Color(0.96f, 0.96f, 0.96f, 1f);
    private static final Color PANEL_DARK = new Color(0.42f, 0.42f, 0.42f, 1f);
    private static final Color DISPLAY_BACKGROUND = new Color(0.04f, 0.02f, 0.02f, 1f);
    private static final Color DISPLAY_ON = new Color(0.95f, 0.10f, 0.10f, 1f);
    private static final Color DISPLAY_OFF = new Color(0.22f, 0.03f, 0.03f, 1f);
    private static final Color FACE_COLOR = new Color(0.98f, 0.82f, 0.20f, 1f);
    private static final Color FACE_DETAIL = new Color(0.08f, 0.08f, 0.08f, 1f);
    private static final Color OVERLAY_COLOR = new Color(0.12f, 0.12f, 0.12f, 1f);
    private static final Color TEXT_COLOR = new Color(0.08f, 0.08f, 0.08f, 1f);

    private final GameLayout layout;
    private final float scale;
    private final float border;
    private final ShapeRenderer shapes;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final GlyphLayout glyphLayout;

    private final float faceSize;
    private final float faceX;
    private final float faceY;
    private final float digitWidth;
    private final float digitHeight;
    private final float digitGap;
    private final float digitPadding;
    private final float displayWidth;
    private final float displayHeight;
    private final float displayY;
    private final float leftDisplayX;
    private final float rightDisplayX;
    private final Rectangle highScoresResetBounds = new Rectangle();
    private final Rectangle resetConfirmBounds = new Rectangle();
    private final Rectangle resetCancelBounds = new Rectangle();

    public GameUiRenderer(GameLayout layout) {
        this.layout = layout;
        this.scale = layout.getScale();
        this.border = 3f * scale;
        this.shapes = new ShapeRenderer();
        this.batch = new SpriteBatch();
        this.font = new BitmapFont();
        this.font.getData().setScale(scale);
        this.glyphLayout = new GlyphLayout();

        this.faceSize = layout.getHudHeight() * 0.66f;
        this.faceX = layout.getPanelX() + (layout.getPanelWidth() - faceSize) / 2f;
        this.faceY = layout.getPanelY() + (layout.getPanelHeight() - faceSize) / 2f;

        this.digitHeight = layout.getHudHeight() * 0.46f;
        this.digitWidth = digitHeight * 0.55f;
        this.digitGap = 2f * scale;
        this.digitPadding = 4f * scale;
        this.displayWidth = digitWidth * 3f + digitGap * 2f + digitPadding * 2f;
        this.displayHeight = digitHeight + digitPadding * 2f;
        this.displayY = layout.getPanelY() + (layout.getPanelHeight() - displayHeight) / 2f;
        this.leftDisplayX = layout.getPanelX() + 10f * scale;
        this.rightDisplayX = layout.getPanelX() + layout.getPanelWidth()
                - 10f * scale - displayWidth;
    }

    public boolean isFaceAt(float worldX, float worldY) {
        return worldX >= faceX && worldX < faceX + faceSize
                && worldY >= faceY && worldY < faceY + faceSize;
    }

    public void renderHud(GameEngine engine, boolean boardButtonDown, boolean facePressed,
                          Matrix4 projection) {
        shapes.setProjectionMatrix(projection);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        drawRaisedRect(layout.getPanelX(), layout.getPanelY(),
                layout.getPanelWidth(), layout.getPanelHeight(), PANEL_COLOR);
        SevenSegmentDisplay.draw(shapes, engine.remainingMines(), leftDisplayX, displayY,
                digitWidth, digitHeight, digitGap, digitPadding,
                DISPLAY_BACKGROUND, DISPLAY_ON, DISPLAY_OFF);
        SevenSegmentDisplay.draw(shapes, engine.elapsedSeconds(), rightDisplayX, displayY,
                digitWidth, digitHeight, digitGap, digitPadding,
                DISPLAY_BACKGROUND, DISPLAY_ON, DISPLAY_OFF);
        drawFace(engine.state(), boardButtonDown && !facePressed, facePressed);

        shapes.end();
    }

    public void renderMenu(String difficultyName, boolean questionMarks,
                           boolean chordOnNumberClick, float uiScale, Matrix4 projection) {
        String[] lines = {
                "Nível atual: " + difficultyName,
                "1 - Iniciante",
                "2 - Intermediário",
                "3 - Avançado",
                "C - Personalizado",
                "M - Interrogação: " + (questionMarks ? "ligada" : "desligada"),
                "X - Chord no número: " + (chordOnNumberClick ? "ligado" : "desligado"),
                "4/5/6 - Escala: " + (int) uiScale + "x",
                "F4 - Recordes",
                "F2 - Reiniciar   F5/Esc - Fechar"
        };
        renderOverlayPanel("Menu", lines, projection);
    }

    public void renderCustomDialog(String columns, String rows, String mines,
                                   int activeField, Matrix4 projection) {
        String[] lines = {
                fieldLine("Colunas (9-30)", columns, activeField == 0),
                fieldLine("Linhas (9-24)", rows, activeField == 1),
                fieldLine("Minas (10+)", mines, activeField == 2),
                "Tab/setas: próximo   Enter: iniciar",
                "Esc: cancelar",
                "Valores fora da faixa são ajustados"
        };
        renderOverlayPanel("Nível personalizado", lines, projection);
    }

    public void renderNewRecord(String difficultyName, int seconds, String name,
                                Matrix4 projection) {
        String shownName = name == null || name.isEmpty() ? "Anônimo" : name;
        String[] lines = {
                "Nível: " + difficultyName,
                "Tempo: " + seconds + " s",
                "Nome: " + shownName + "_",
                "Máximo de 16 caracteres",
                "Enter: salvar   Esc: cancelar"
        };
        renderOverlayPanel("Novo recorde!", lines, projection);
    }

    public void renderHighScores(String[] lines, Matrix4 projection) {
        OverlayGeometry geometry = overlayGeometry(lines.length);
        float buttonX = geometry.panelX + geometry.padding;
        float buttonWidth = geometry.panelWidth - geometry.padding * 2f;
        float lastLineY = geometry.top() - lines.length * geometry.lineStep;
        float buttonHeight = font.getLineHeight() + 4f * scale;
        float buttonY = lastLineY - font.getLineHeight() - 2f * scale;
        highScoresResetBounds.set(buttonX, buttonY, buttonWidth, buttonHeight);

        drawOverlayBase(geometry, projection);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawRaisedRect(buttonX, buttonY, buttonWidth, buttonHeight, PANEL_COLOR);
        shapes.end();

        batch.setProjectionMatrix(projection);
        batch.begin();
        drawCenteredText("Recordes", geometry.panelX + geometry.panelWidth / 2f,
                geometry.top());
        float y = geometry.top() - geometry.lineStep;
        for (int index = 0; index < lines.length - 1; index++) {
            drawText(lines[index], geometry.panelX + geometry.padding, y);
            y -= geometry.lineStep;
        }
        drawCenteredText(lines[lines.length - 1],
                geometry.panelX + geometry.panelWidth / 2f, lastLineY);
        batch.end();
    }

    public void renderResetConfirmation(Matrix4 projection) {
        OverlayGeometry geometry = overlayGeometry(2);
        float messageY = geometry.top() - geometry.lineStep;
        float buttonRowY = geometry.top() - geometry.lineStep * 2f;
        float gap = 8f * scale;
        float buttonWidth = (geometry.panelWidth - geometry.padding * 2f - gap) / 2f;
        float buttonHeight = font.getLineHeight() + 4f * scale;
        float buttonY = buttonRowY - font.getLineHeight() - 2f * scale;
        float leftX = geometry.panelX + geometry.padding;
        float rightX = leftX + buttonWidth + gap;
        resetConfirmBounds.set(leftX, buttonY, buttonWidth, buttonHeight);
        resetCancelBounds.set(rightX, buttonY, buttonWidth, buttonHeight);

        drawOverlayBase(geometry, projection);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawRaisedRect(leftX, buttonY, buttonWidth, buttonHeight, PANEL_COLOR);
        drawRaisedRect(rightX, buttonY, buttonWidth, buttonHeight, PANEL_COLOR);
        shapes.end();

        batch.setProjectionMatrix(projection);
        batch.begin();
        drawCenteredText("Zerar recordes?", geometry.panelX + geometry.panelWidth / 2f,
                geometry.top());
        drawText("Esta ação não pode ser desfeita.",
                geometry.panelX + geometry.padding, messageY);
        drawCenteredText("Sim (S)", leftX + buttonWidth / 2f, buttonRowY);
        drawCenteredText("Não (N)", rightX + buttonWidth / 2f, buttonRowY);
        batch.end();
    }

    public boolean isHighScoresResetAt(float worldX, float worldY) {
        return highScoresResetBounds.contains(worldX, worldY);
    }

    public boolean isResetConfirmAt(float worldX, float worldY) {
        return resetConfirmBounds.contains(worldX, worldY);
    }

    public boolean isResetCancelAt(float worldX, float worldY) {
        return resetCancelBounds.contains(worldX, worldY);
    }

    @Override
    public void dispose() {
        shapes.dispose();
        batch.dispose();
        font.dispose();
    }

    private String fieldLine(String label, String value, boolean active) {
        return (active ? "> " : "  ") + label + ": " + value + (active ? " <" : "");
    }

    private void renderOverlayPanel(String title, String[] bodyLines, Matrix4 projection) {
        OverlayGeometry geometry = overlayGeometry(bodyLines.length);
        drawOverlayBase(geometry, projection);

        batch.setProjectionMatrix(projection);
        batch.begin();
        drawCenteredText(title, geometry.panelX + geometry.panelWidth / 2f,
                geometry.top());
        float y = geometry.top() - geometry.lineStep;
        for (String line : bodyLines) {
            drawText(line, geometry.panelX + geometry.padding, y);
            y -= geometry.lineStep;
        }
        batch.end();
    }

    private OverlayGeometry overlayGeometry(int bodyLines) {
        float padding = 18f * scale;
        float lineStep = font.getLineHeight() * 1.35f;
        int totalLines = bodyLines + 1;
        float panelWidth = Math.min(layout.getWorldWidth() * 0.92f, 520f * scale);
        float panelHeight = padding * 2f + totalLines * lineStep;
        float panelX = (layout.getWorldWidth() - panelWidth) / 2f;
        float panelY = (layout.getWorldHeight() - panelHeight) / 2f;
        return new OverlayGeometry(panelX, panelY, panelWidth, panelHeight, padding, lineStep);
    }

    private void drawOverlayBase(OverlayGeometry geometry, Matrix4 projection) {
        shapes.setProjectionMatrix(projection);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(OVERLAY_COLOR);
        shapes.rect(0f, 0f, layout.getWorldWidth(), layout.getWorldHeight());
        drawRaisedRect(geometry.panelX, geometry.panelY, geometry.panelWidth,
                geometry.panelHeight, PANEL_COLOR);
        shapes.end();
    }

    private record OverlayGeometry(float panelX, float panelY, float panelWidth,
                                   float panelHeight, float padding, float lineStep) {
        private float top() {
            return panelY + panelHeight - padding;
        }
    }

    private void drawRaisedRect(float x, float y, float width, float height, Color color) {
        shapes.setColor(color);
        shapes.rect(x, y, width, height);

        shapes.setColor(PANEL_LIGHT);
        shapes.rect(x, y + height - border, width - border, border);
        shapes.rect(x, y, border, height - border);

        shapes.setColor(PANEL_DARK);
        shapes.rect(x + border, y, width - border, border);
        shapes.rect(x + width - border, y + border, border, height - border);
    }

    private void drawPressedRect(float x, float y, float width, float height) {
        shapes.setColor(PANEL_COLOR);
        shapes.rect(x, y, width, height);

        shapes.setColor(PANEL_DARK);
        shapes.rect(x, y + height - border, width - border, border);
        shapes.rect(x, y, border, height - border);

        shapes.setColor(PANEL_LIGHT);
        shapes.rect(x + border, y, width - border, border);
        shapes.rect(x + width - border, y + border, border, height - border);
    }

    private void drawFace(GameState state, boolean surprised, boolean pressed) {
        if (pressed) {
            drawPressedRect(faceX, faceY, faceSize, faceSize);
        } else {
            drawRaisedRect(faceX, faceY, faceSize, faceSize, PANEL_COLOR);
        }

        float radius = faceSize * 0.40f;
        float centerX = faceX + faceSize / 2f;
        float centerY = faceY + faceSize / 2f;

        shapes.setColor(FACE_COLOR);
        shapes.circle(centerX, centerY, radius, 32);

        if (pressed) {
            drawEyes(centerX, centerY, radius);
            drawSmile(centerX, centerY, radius);
        } else if (state == GameState.WON) {
            drawSunglasses(centerX, centerY, radius);
            drawSmile(centerX, centerY, radius);
        } else if (state == GameState.LOST) {
            drawLostEyes(centerX, centerY, radius);
            drawFrown(centerX, centerY, radius);
        } else if (surprised) {
            drawEyes(centerX, centerY, radius);
            drawSurprisedMouth(centerX, centerY, radius);
        } else {
            drawEyes(centerX, centerY, radius);
            drawSmile(centerX, centerY, radius);
        }
    }

    private void drawEyes(float centerX, float centerY, float radius) {
        shapes.setColor(FACE_DETAIL);
        float eyeRadius = radius * 0.10f;
        float eyeOffsetX = radius * 0.35f;
        float eyeOffsetY = radius * 0.30f;
        shapes.circle(centerX - eyeOffsetX, centerY + eyeOffsetY, eyeRadius, 12);
        shapes.circle(centerX + eyeOffsetX, centerY + eyeOffsetY, eyeRadius, 12);
    }

    private void drawLostEyes(float centerX, float centerY, float radius) {
        shapes.setColor(FACE_DETAIL);
        float offsetX = radius * 0.35f;
        float offsetY = radius * 0.30f;
        float size = radius * 0.15f;
        float thickness = 2f * scale;
        shapes.rectLine(centerX - offsetX - size, centerY + offsetY - size,
                centerX - offsetX + size, centerY + offsetY + size, thickness);
        shapes.rectLine(centerX - offsetX - size, centerY + offsetY + size,
                centerX - offsetX + size, centerY + offsetY - size, thickness);
        shapes.rectLine(centerX + offsetX - size, centerY + offsetY - size,
                centerX + offsetX + size, centerY + offsetY + size, thickness);
        shapes.rectLine(centerX + offsetX - size, centerY + offsetY + size,
                centerX + offsetX + size, centerY + offsetY - size, thickness);
    }

    private void drawSmile(float centerX, float centerY, float radius) {
        shapes.setColor(FACE_DETAIL);
        float thickness = 2f * scale;
        float mouthY = centerY - radius * 0.28f;
        shapes.rectLine(centerX - radius * 0.45f, centerY - radius * 0.05f,
                centerX - radius * 0.20f, mouthY, thickness);
        shapes.rectLine(centerX - radius * 0.20f, mouthY,
                centerX + radius * 0.20f, mouthY, thickness);
        shapes.rectLine(centerX + radius * 0.20f, mouthY,
                centerX + radius * 0.45f, centerY - radius * 0.05f, thickness);
    }

    private void drawFrown(float centerX, float centerY, float radius) {
        shapes.setColor(FACE_DETAIL);
        float thickness = 2f * scale;
        float mouthY = centerY - radius * 0.40f;
        shapes.rectLine(centerX - radius * 0.40f, mouthY,
                centerX - radius * 0.18f, centerY - radius * 0.20f, thickness);
        shapes.rectLine(centerX - radius * 0.18f, centerY - radius * 0.20f,
                centerX + radius * 0.18f, centerY - radius * 0.20f, thickness);
        shapes.rectLine(centerX + radius * 0.18f, centerY - radius * 0.20f,
                centerX + radius * 0.40f, mouthY, thickness);
    }

    private void drawSurprisedMouth(float centerX, float centerY, float radius) {
        shapes.setColor(FACE_DETAIL);
        shapes.circle(centerX, centerY - radius * 0.30f, radius * 0.15f, 16);
    }

    private void drawSunglasses(float centerX, float centerY, float radius) {
        shapes.setColor(FACE_DETAIL);
        float width = radius * 1.20f;
        float height = radius * 0.28f;
        shapes.rect(centerX - width / 2f, centerY + radius * 0.15f, width, height);
    }

    private void drawText(String text, float x, float y) {
        font.setColor(TEXT_COLOR);
        glyphLayout.setText(font, text);
        font.draw(batch, glyphLayout, x, y);
    }

    private void drawCenteredText(String text, float centerX, float y) {
        font.setColor(TEXT_COLOR);
        glyphLayout.setText(font, text);
        font.draw(batch, glyphLayout, centerX - glyphLayout.width / 2f, y);
    }
}
