package com.exemplo.minesweeper.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinePlacerTest {
    @Test
    void placesConfiguredMineCountForEachDifficulty() {
        for (Difficulty difficulty : Difficulty.values()) {
            Board board = difficulty.createBoard();
            MinePlacer.placeMines(board, 0, 0, 12_345L);

            assertEquals(difficulty.mines(), countMines(board), difficulty.name());
        }
    }

    @Test
    void firstClickAndNeighborsAreSafeAcrossSeeds() {
        for (Difficulty difficulty : Difficulty.values()) {
            int columns = difficulty.columns();
            int rows = difficulty.rows();

            for (int seed = 0; seed < 10_000; seed++) {
                int safeX = seed % columns;
                int safeY = (seed / columns) % rows;
                Board board = difficulty.createBoard();

                MinePlacer.placeMines(board, safeX, safeY, seed);

                assertFalse(hasMineInNeighborhood(board, safeX, safeY),
                        "Mine in safe zone for " + difficulty + " seed " + seed);
            }
        }
    }

    @Test
    void protectsOnlyClickedCellWhenSafeZoneDoesNotFitAllMines() {
        Board board = new Board(3, 3, 8);

        MinePlacer.placeMines(board, 1, 1, 42L);

        assertFalse(board.get(1, 1).hasMine());
        assertEquals(8, countMines(board));
    }

    @Test
    void sameSeedProducesSameLayout() {
        Board first = new Board(9, 9, 10);
        Board second = new Board(9, 9, 10);

        MinePlacer.placeMines(first, 4, 4, 777L);
        MinePlacer.placeMines(second, 4, 4, 777L);

        for (int y = 0; y < first.getRows(); y++) {
            for (int x = 0; x < first.getColumns(); x++) {
                assertEquals(first.get(x, y).hasMine(), second.get(x, y).hasMine(),
                        "Mine mismatch at (" + x + ", " + y + ")");
            }
        }
    }

    private static int countMines(Board board) {
        int count = 0;
        for (int y = 0; y < board.getRows(); y++) {
            for (int x = 0; x < board.getColumns(); x++) {
                if (board.get(x, y).hasMine()) {
                    count++;
                }
            }
        }
        return count;
    }

    private static boolean hasMineInNeighborhood(Board board, int x, int y) {
        if (board.get(x, y).hasMine()) {
            return true;
        }

        boolean[] found = {false};
        board.forEachNeighbor(x, y, (neighborX, neighborY) -> {
            if (board.get(neighborX, neighborY).hasMine()) {
                found[0] = true;
            }
        });
        return found[0];
    }
}
