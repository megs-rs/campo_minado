package com.exemplo.minesweeper.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameEngineChordTest {
    @Test
    void chordDoesNothingWhenFlagCountDoesNotMatch() {
        GameEngine engine = new GameEngine(Difficulty.INTERMEDIATE, 100L);
        engine.reveal(0, 0);
        Board board = engine.board();

        int[] center = findSafeNumberedCell(board);
        assertNotNull(center);
        board.setState(center[0], center[1], CellState.REVEALED);
        int revealedBefore = board.getRevealedCount();

        engine.chord(center[0], center[1]);

        assertEquals(GameState.PLAYING, engine.state());
        assertEquals(revealedBefore, board.getRevealedCount());
    }

    @Test
    void chordRevealsNeighborsWhenFlagCountMatches() {
        GameEngine engine = new GameEngine(Difficulty.INTERMEDIATE, 101L);
        engine.reveal(0, 0);
        Board board = engine.board();

        int[] center = findSafeNumberedCellWithCoveredSafeNeighbor(board);
        assertNotNull(center);
        board.setState(center[0], center[1], CellState.REVEALED);

        List<int[]> mineNeighbors = neighborsMatching(board, center[0], center[1], Cell::hasMine);
        assertFalse(mineNeighbors.isEmpty());
        for (int[] mine : mineNeighbors) {
            engine.toggleFlag(mine[0], mine[1]);
        }

        List<int[]> expected = neighborsMatching(board, center[0], center[1],
                cell -> !cell.hasMine()
                        && cell.getState() != CellState.FLAGGED
                        && cell.getState() != CellState.REVEALED);
        assertFalse(expected.isEmpty());

        engine.chord(center[0], center[1]);

        assertNotEquals(GameState.LOST, engine.state());
        for (int[] cell : expected) {
            assertEquals(CellState.REVEALED, board.get(cell[0], cell[1]).getState(),
                    "Cell (" + cell[0] + ", " + cell[1] + ")");
        }
    }

    @Test
    void chordExplodesWhenFlagIsWrong() {
        GameEngine engine = new GameEngine(Difficulty.INTERMEDIATE, 102L);
        engine.reveal(0, 0);
        Board board = engine.board();

        int[] center = findSafeCellWithAdjacentMines(board, 1);
        assertNotNull(center);
        board.setState(center[0], center[1], CellState.REVEALED);

        List<int[]> safeNeighbors = neighborsMatching(board, center[0], center[1],
                cell -> !cell.hasMine() && cell.getState() != CellState.REVEALED);
        if (safeNeighbors.isEmpty()) {
            List<int[]> allSafeNeighbors = neighborsMatching(board, center[0], center[1], cell -> !cell.hasMine());
            assertFalse(allSafeNeighbors.isEmpty());
            int[] safeNeighbor = allSafeNeighbors.get(0);
            board.setState(safeNeighbor[0], safeNeighbor[1], CellState.COVERED);
            safeNeighbors = List.of(safeNeighbor);
        }

        int[] wrongFlag = safeNeighbors.get(0);
        engine.toggleFlag(wrongFlag[0], wrongFlag[1]);

        engine.chord(center[0], center[1]);

        assertEquals(GameState.LOST, engine.state());
        assertEquals(CellState.WRONG_FLAG, board.get(wrongFlag[0], wrongFlag[1]).getState());

        boolean foundExplodedMine = false;
        for (int[] mine : neighborsMatching(board, center[0], center[1], Cell::hasMine)) {
            if (board.get(mine[0], mine[1]).getState() == CellState.EXPLODED) {
                foundExplodedMine = true;
            }
        }
        assertTrue(foundExplodedMine);
    }

    private static int[] findSafeNumberedCell(Board board) {
        for (int y = 0; y < board.getRows(); y++) {
            for (int x = 0; x < board.getColumns(); x++) {
                Cell cell = board.get(x, y);
                if (!cell.hasMine() && cell.getAdjacentMines() > 0) {
                    return new int[]{x, y};
                }
            }
        }
        return null;
    }

    private static int[] findSafeNumberedCellWithCoveredSafeNeighbor(Board board) {
        for (int y = 0; y < board.getRows(); y++) {
            for (int x = 0; x < board.getColumns(); x++) {
                Cell cell = board.get(x, y);
                if (cell.hasMine() || cell.getAdjacentMines() == 0) {
                    continue;
                }
                if (hasCoveredSafeNeighbor(board, x, y)) {
                    return new int[]{x, y};
                }
            }
        }
        return null;
    }

    private static int[] findSafeCellWithAdjacentMines(Board board, int adjacentMines) {
        for (int y = 0; y < board.getRows(); y++) {
            for (int x = 0; x < board.getColumns(); x++) {
                Cell cell = board.get(x, y);
                if (!cell.hasMine() && cell.getAdjacentMines() == adjacentMines) {
                    return new int[]{x, y};
                }
            }
        }
        return null;
    }

    private static boolean hasCoveredSafeNeighbor(Board board, int x, int y) {
        boolean[] found = {false};
        board.forEachNeighbor(x, y, (neighborX, neighborY) -> {
            Cell neighbor = board.get(neighborX, neighborY);
            if (!neighbor.hasMine()
                    && neighbor.getState() != CellState.FLAGGED
                    && neighbor.getState() != CellState.REVEALED) {
                found[0] = true;
            }
        });
        return found[0];
    }

    private static List<int[]> neighborsMatching(Board board, int x, int y, Predicate<Cell> predicate) {
        List<int[]> result = new ArrayList<>();
        board.forEachNeighbor(x, y, (neighborX, neighborY) -> {
            if (predicate.test(board.get(neighborX, neighborY))) {
                result.add(new int[]{neighborX, neighborY});
            }
        });
        return result;
    }
}
