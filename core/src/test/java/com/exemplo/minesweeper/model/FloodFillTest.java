package com.exemplo.minesweeper.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FloodFillTest {
    @Test
    void revealsConnectedAreaAndStopsAtNumberBoundary() {
        Board board = new Board(5, 5, 1);
        board.setMine(4, 4, true);
        board.recalculateAdjacentMines();

        FloodFill.reveal(board, 0, 0);

        assertEquals(24, board.getRevealedCount());
        for (int y = 0; y < board.getRows(); y++) {
            for (int x = 0; x < board.getColumns(); x++) {
                CellState expected = x == 4 && y == 4 ? CellState.COVERED : CellState.REVEALED;
                assertEquals(expected, board.get(x, y).getState(), "Cell (" + x + ", " + y + ")");
            }
        }
    }

    @Test
    void revealsOnlyTheNumberWhenStartingAtBoundary() {
        Board board = new Board(3, 3, 1);
        board.setMine(1, 1, true);
        board.recalculateAdjacentMines();

        FloodFill.reveal(board, 0, 0);

        assertEquals(1, board.getRevealedCount());
        assertEquals(CellState.REVEALED, board.get(0, 0).getState());
        assertEquals(CellState.COVERED, board.get(0, 1).getState());
    }

    @Test
    void doesNotRevealFlaggedCells() {
        Board board = new Board(5, 5, 1);
        board.setMine(4, 4, true);
        board.recalculateAdjacentMines();
        board.setState(2, 2, CellState.FLAGGED);

        FloodFill.reveal(board, 0, 0);

        assertEquals(CellState.FLAGGED, board.get(2, 2).getState());
        assertEquals(23, board.getRevealedCount());
    }

    @Test
    void revealsQuestionCells() {
        Board board = new Board(5, 5, 1);
        board.setMine(4, 4, true);
        board.recalculateAdjacentMines();
        board.setState(1, 1, CellState.QUESTION);

        FloodFill.reveal(board, 0, 0);

        assertEquals(CellState.REVEALED, board.get(1, 1).getState());
    }

    @Test
    void doesNotRevealMineAtStart() {
        Board board = new Board(3, 3, 1);
        board.setMine(1, 1, true);
        board.recalculateAdjacentMines();

        FloodFill.reveal(board, 1, 1);

        assertEquals(CellState.COVERED, board.get(1, 1).getState());
        assertEquals(0, board.getRevealedCount());
    }

    @Test
    void handlesLargeBoardWithoutStackOverflow() {
        Board board = new Board(30, 24, 0);
        board.recalculateAdjacentMines();

        FloodFill.reveal(board, 0, 0);

        assertEquals(30 * 24, board.getRevealedCount());
        for (int y = 0; y < board.getRows(); y++) {
            for (int x = 0; x < board.getColumns(); x++) {
                assertEquals(CellState.REVEALED, board.get(x, y).getState(), "Cell (" + x + ", " + y + ")");
            }
        }
    }
}
