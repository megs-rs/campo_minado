package com.exemplo.minesweeper.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DifficultyTest {
    @Test
    void fixedDifficultiesMatchSpec() {
        assertAll(
                () -> assertEquals("Iniciante", Difficulty.BEGINNER.displayName()),
                () -> assertEquals(9, Difficulty.BEGINNER.columns()),
                () -> assertEquals(9, Difficulty.BEGINNER.rows()),
                () -> assertEquals(10, Difficulty.BEGINNER.mines()),
                () -> assertEquals(16, Difficulty.INTERMEDIATE.columns()),
                () -> assertEquals(16, Difficulty.INTERMEDIATE.rows()),
                () -> assertEquals(40, Difficulty.INTERMEDIATE.mines()),
                () -> assertEquals(30, Difficulty.EXPERT.columns()),
                () -> assertEquals(16, Difficulty.EXPERT.rows()),
                () -> assertEquals(99, Difficulty.EXPERT.mines())
        );
    }

    @Test
    void customDifficultyClampsAllValues() {
        CustomDifficulty low = new CustomDifficulty(1, 1, 1);
        assertEquals(9, low.columns());
        assertEquals(9, low.rows());
        assertEquals(10, low.mines());

        CustomDifficulty high = new CustomDifficulty(99, 99, 10_000);
        assertEquals(30, high.columns());
        assertEquals(24, high.rows());
        assertEquals(29 * 23, high.mines());

        assertEquals(10, new CustomDifficulty(16, 16, 1).mines());
        assertEquals(15 * 15, new CustomDifficulty(16, 16, 10_000).mines());
    }

    @Test
    void customDifficultyKeepsValidValues() {
        CustomDifficulty custom = new CustomDifficulty(12, 10, 20);

        assertEquals(12, custom.columns());
        assertEquals(10, custom.rows());
        assertEquals(20, custom.mines());
    }

    @Test
    void createsBoardFromConfig() {
        Board board = new CustomDifficulty(16, 16, 40).createBoard();

        assertEquals(16, board.getColumns());
        assertEquals(16, board.getRows());
        assertEquals(40, board.getMines());
    }
}
