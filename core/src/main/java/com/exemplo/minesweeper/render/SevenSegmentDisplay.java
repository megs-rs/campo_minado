package com.exemplo.minesweeper.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public final class SevenSegmentDisplay {
    private static final int SEGMENTS = 7;

    private SevenSegmentDisplay() {
    }

    public static void draw(ShapeRenderer shapes, int value, float x, float y,
                            float digitWidth, float digitHeight, float gap, float padding,
                            Color background, Color on, Color off) {
        int clamped = Math.max(-99, Math.min(999, value));
        String text = clamped < 0
                ? String.format("-%02d", -clamped)
                : String.format("%03d", clamped);

        float totalWidth = digitWidth * 3f + gap * 2f + padding * 2f;
        float totalHeight = digitHeight + padding * 2f;

        shapes.setColor(background);
        shapes.rect(x, y, totalWidth, totalHeight);

        float digitX = x + padding;
        float digitY = y + padding;
        for (int i = 0; i < 3; i++) {
            drawDigit(shapes, text.charAt(i), digitX, digitY, digitWidth, digitHeight, on, off);
            digitX += digitWidth + gap;
        }
    }

    private static void drawDigit(ShapeRenderer shapes, char digit, float x, float y,
                                  float width, float height, Color on, Color off) {
        boolean[] segments = new boolean[SEGMENTS];
        switch (digit) {
            case '0' -> activate(segments, 0, 1, 2, 4, 5, 6);
            case '1' -> activate(segments, 1, 2);
            case '2' -> activate(segments, 0, 1, 3, 4, 6);
            case '3' -> activate(segments, 0, 1, 2, 3, 6);
            case '4' -> activate(segments, 1, 2, 3, 5);
            case '5' -> activate(segments, 0, 2, 3, 5, 6);
            case '6' -> activate(segments, 0, 2, 3, 4, 5, 6);
            case '7' -> activate(segments, 0, 1, 2);
            case '8' -> activate(segments, 0, 1, 2, 3, 4, 5, 6);
            case '9' -> activate(segments, 0, 1, 2, 3, 5, 6);
            case '-' -> activate(segments, 3);
            default -> {
            }
        }

        float thickness = Math.max(1.5f, width * 0.18f);
        drawSegment(shapes, segments[0], x + thickness, y + height - thickness,
                width - thickness * 2f, thickness, on, off);
        drawSegment(shapes, segments[1], x + width - thickness, y + height / 2f,
                thickness, height / 2f - thickness, on, off);
        drawSegment(shapes, segments[2], x + width - thickness, y + thickness,
                thickness, height / 2f - thickness, on, off);
        drawSegment(shapes, segments[3], x + thickness, y + height / 2f - thickness / 2f,
                width - thickness * 2f, thickness, on, off);
        drawSegment(shapes, segments[4], x, y + thickness,
                thickness, height / 2f - thickness, on, off);
        drawSegment(shapes, segments[5], x, y + height / 2f,
                thickness, height / 2f - thickness, on, off);
        drawSegment(shapes, segments[6], x + thickness, y,
                width - thickness * 2f, thickness, on, off);
    }

    private static void drawSegment(ShapeRenderer shapes, boolean active, float x, float y,
                                    float width, float height, Color on, Color off) {
        shapes.setColor(active ? on : off);
        shapes.rect(x, y, width, height);
    }

    private static void activate(boolean[] segments, int... indices) {
        for (int index : indices) {
            segments[index] = true;
        }
    }
}
