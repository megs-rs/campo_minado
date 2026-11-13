package com.exemplo.minesweeper.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public final class MinePlacer {
    private MinePlacer() {
    }

    public static void placeMines(Board board, int safeX, int safeY) {
        placeMines(board, safeX, safeY, new Random());
    }

    public static void placeMines(Board board, int safeX, int safeY, long seed) {
        placeMines(board, safeX, safeY, new Random(seed));
    }

    public static void placeMines(Board board, int safeX, int safeY, Random random) {
        Objects.requireNonNull(board, "board");
        Objects.requireNonNull(random, "random");
        if (!board.inBounds(safeX, safeY)) {
            throw new IllegalArgumentException("Safe cell is outside the board");
        }

        int totalCells = board.getColumns() * board.getRows();
        int mines = board.getMines();
        boolean protectNeighbors = totalCells - 9 >= mines;

        List<Cell> candidates = new ArrayList<>(totalCells);
        for (int y = 0; y < board.getRows(); y++) {
            for (int x = 0; x < board.getColumns(); x++) {
                if (x == safeX && y == safeY) {
                    continue;
                }
                if (protectNeighbors && isInSafeZone(safeX, safeY, x, y)) {
                    continue;
                }
                candidates.add(board.get(x, y));
            }
        }

        for (int i = candidates.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Collections.swap(candidates, i, j);
        }

        for (int i = 0; i < mines; i++) {
            candidates.get(i).setMine(true);
        }

        board.recalculateAdjacentMines();
    }

    private static boolean isInSafeZone(int safeX, int safeY, int x, int y) {
        return Math.abs(safeX - x) <= 1 && Math.abs(safeY - y) <= 1;
    }
}
