package com.exemplo.minesweeper.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BoardTest {
    @Test
    void storesCellsInRowMajorOrder() {
        Board board = new Board(3, 2, 0);

        assertEquals(0, board.indexOf(0, 0));
        assertEquals(2, board.indexOf(2, 0));
        assertEquals(3, board.indexOf(0, 1));
    }

    @Test
    void calculatesAdjacentMinesAtCornerEdgeAndCenter() {
        Board board = new Board(3, 3, 3);
        board.setMine(0, 0, true);
        board.setMine(2, 0, true);
        board.setMine(0, 2, true);
        board.recalculateAdjacentMines();

        assertEquals(0, board.get(2, 2).getAdjacentMines());
        assertEquals(2, board.get(1, 0).getAdjacentMines());
        assertEquals(3, board.get(1, 1).getAdjacentMines());
    }

    @Test
    void countsFlagsButNotQuestions() {
        Board board = new Board(3, 3, 1);
        board.setState(0, 0, CellState.FLAGGED);
        board.setState(1, 0, CellState.QUESTION);
        board.setState(2, 0, CellState.FLAGGED);

        assertEquals(2, board.countFlagsAround(1, 1));
        assertEquals(2, board.getFlagCount());
    }

    @Test
    void tracksRevealedAndFlagCountsAcrossTransitions() {
        Board board = new Board(3, 3, 1);

        board.setState(0, 0, CellState.FLAGGED);
        assertEquals(1, board.getFlagCount());

        board.setState(0, 0, CellState.REVEALED);
        assertEquals(0, board.getFlagCount());
        assertEquals(1, board.getRevealedCount());

        board.setState(0, 0, CellState.REVEALED);
        assertEquals(1, board.getRevealedCount());

        board.reset(3, 3, 1);
        assertEquals(0, board.getRevealedCount());
        assertEquals(0, board.getFlagCount());
        assertEquals(9, board.getColumns() * board.getRows());
    }

    @Test
    void rejectsInvalidDimensionsAndMineCounts() {
        assertThrows(IllegalArgumentException.class, () -> new Board(0, 9, 10));
        assertThrows(IllegalArgumentException.class, () -> new Board(9, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> new Board(9, 9, -1));
        assertThrows(IllegalArgumentException.class, () -> new Board(9, 9, 81));
    }
}
