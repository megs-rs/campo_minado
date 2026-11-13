package com.exemplo.minesweeper.model;

import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameEngineTest {
    @Test
    void startsReadyWithoutPlacingMines() {
        GameEngine engine = new GameEngine(Difficulty.BEGINNER, 1L);

        assertEquals(GameState.READY, engine.state());
        assertEquals(0, engine.elapsedSeconds());
        assertEquals(10, engine.remainingMines());
        assertEquals(0, countMines(engine.board()));
        assertEquals(0, engine.board().getFlagCount());
    }

    @Test
    void firstRevealPlacesMinesAndStartsTimer() {
        GameEngine engine = new GameEngine(Difficulty.BEGINNER, 2L);

        engine.reveal(0, 0);

        assertEquals(GameState.PLAYING, engine.state());
        assertEquals(10, countMines(engine.board()));
        assertFalse(hasMineInNeighborhood(engine.board(), 0, 0));
        assertEquals(0, engine.elapsedSeconds());

        engine.update(0.99f);
        assertEquals(0, engine.elapsedSeconds());

        engine.update(0.02f);
        assertEquals(1, engine.elapsedSeconds());

        engine.update(1f);
        assertEquals(2, engine.elapsedSeconds());
    }

    @Test
    void flaggingBeforeFirstRevealDoesNotStartGame() {
        GameEngine engine = new GameEngine(Difficulty.BEGINNER, 3L);

        engine.toggleFlag(0, 0);

        assertEquals(GameState.READY, engine.state());
        assertEquals(CellState.FLAGGED, engine.board().get(0, 0).getState());
        assertEquals(9, engine.remainingMines());
        assertEquals(0, engine.elapsedSeconds());
        assertEquals(0, countMines(engine.board()));

        engine.reveal(0, 0);
        assertEquals(GameState.READY, engine.state());
        assertEquals(0, countMines(engine.board()));

        engine.toggleFlag(0, 0);
        assertEquals(CellState.COVERED, engine.board().get(0, 0).getState());

        engine.reveal(0, 0);
        assertEquals(GameState.PLAYING, engine.state());
        assertEquals(10, countMines(engine.board()));
    }

    @Test
    void questionMarksCycleOnlyWhenEnabled() {
        GameEngine engine = new GameEngine(Difficulty.BEGINNER, 4L);

        engine.toggleFlag(1, 1);
        assertEquals(CellState.FLAGGED, engine.board().get(1, 1).getState());
        assertEquals(9, engine.remainingMines());

        engine.toggleFlag(1, 1);
        assertEquals(CellState.COVERED, engine.board().get(1, 1).getState());
        assertEquals(10, engine.remainingMines());

        engine.setQuestionMarksEnabled(true);
        engine.toggleFlag(1, 1);
        assertEquals(CellState.FLAGGED, engine.board().get(1, 1).getState());
        assertEquals(9, engine.remainingMines());

        engine.toggleFlag(1, 1);
        assertEquals(CellState.QUESTION, engine.board().get(1, 1).getState());
        assertEquals(10, engine.remainingMines());

        engine.toggleFlag(1, 1);
        assertEquals(CellState.COVERED, engine.board().get(1, 1).getState());
        assertEquals(10, engine.remainingMines());
    }

    @Test
    void mineCounterCanGoNegativeAndQuestionDoesNotCount() {
        GameEngine engine = new GameEngine(Difficulty.BEGINNER, 9L);
        engine.setQuestionMarksEnabled(true);

        for (int i = 0; i < 12; i++) {
            engine.toggleFlag(i % 9, i / 9);
        }

        assertEquals(-2, engine.remainingMines());

        engine.toggleFlag(0, 0);

        assertEquals(CellState.QUESTION, engine.board().get(0, 0).getState());
        assertEquals(-1, engine.remainingMines());
    }

    @Test
    void timerStopsAtLimitWithoutEndingGame() {
        GameEngine engine = new GameEngine(Difficulty.BEGINNER, 5L);
        engine.reveal(0, 0);

        engine.update(1_000f);

        assertEquals(999, engine.elapsedSeconds());
        assertEquals(GameState.PLAYING, engine.state());

        engine.update(10f);
        assertEquals(999, engine.elapsedSeconds());
    }

    @Test
    void winIsDetectedWhenLastSafeCellIsRevealed() {
        GameEngine engine = new GameEngine(Difficulty.INTERMEDIATE, 6L);
        engine.reveal(0, 0);
        Board board = engine.board();
        engine.update(1.2f);
        assertEquals(1, engine.elapsedSeconds());

        int[] lastSafeCell = findCell(board, cell -> !cell.hasMine() && cell.getState() != CellState.REVEALED);
        assertNotNull(lastSafeCell);

        for (int y = 0; y < board.getRows(); y++) {
            for (int x = 0; x < board.getColumns(); x++) {
                if (x == lastSafeCell[0] && y == lastSafeCell[1]) {
                    continue;
                }
                if (!board.get(x, y).hasMine()) {
                    board.setState(x, y, CellState.REVEALED);
                }
            }
        }

        assertEquals(GameState.PLAYING, engine.state());

        engine.reveal(lastSafeCell[0], lastSafeCell[1]);

        assertEquals(GameState.WON, engine.state());
        assertEquals(0, engine.remainingMines());
        for (int y = 0; y < board.getRows(); y++) {
            for (int x = 0; x < board.getColumns(); x++) {
                if (board.get(x, y).hasMine()) {
                    assertEquals(CellState.FLAGGED, board.get(x, y).getState(), "Mine at (" + x + ", " + y + ")");
                }
            }
        }

        engine.update(5f);
        assertEquals(1, engine.elapsedSeconds());
    }

    @Test
    void lossRevealsMinesAndMarksWrongFlags() {
        GameEngine engine = new GameEngine(Difficulty.INTERMEDIATE, 7L);
        engine.reveal(0, 0);
        Board board = engine.board();

        int[] wrongFlag = findCell(board, cell -> !cell.hasMine() && cell.getState() == CellState.COVERED);
        assertNotNull(wrongFlag);

        int[] correctMine = findMine(board, -1, -1);
        assertNotNull(correctMine);

        int[] explodedMine = findMine(board, correctMine[0], correctMine[1]);
        assertNotNull(explodedMine);

        engine.toggleFlag(wrongFlag[0], wrongFlag[1]);
        engine.toggleFlag(correctMine[0], correctMine[1]);
        int remainingBeforeLoss = engine.remainingMines();

        engine.update(1.2f);
        assertEquals(1, engine.elapsedSeconds());

        engine.reveal(explodedMine[0], explodedMine[1]);

        assertEquals(GameState.LOST, engine.state());
        assertEquals(CellState.EXPLODED, board.get(explodedMine[0], explodedMine[1]).getState());
        assertEquals(CellState.FLAGGED, board.get(correctMine[0], correctMine[1]).getState());
        assertEquals(CellState.WRONG_FLAG, board.get(wrongFlag[0], wrongFlag[1]).getState());
        assertEquals(remainingBeforeLoss, engine.remainingMines());

        for (int y = 0; y < board.getRows(); y++) {
            for (int x = 0; x < board.getColumns(); x++) {
                if (!board.get(x, y).hasMine()) {
                    continue;
                }
                if ((x == explodedMine[0] && y == explodedMine[1])
                        || (x == correctMine[0] && y == correctMine[1])) {
                    continue;
                }
                assertEquals(CellState.MINE_SHOWN, board.get(x, y).getState(), "Mine at (" + x + ", " + y + ")");
            }
        }

        int revealedAfterLoss = board.getRevealedCount();
        engine.reveal(wrongFlag[0], wrongFlag[1]);
        engine.toggleFlag(wrongFlag[0], wrongFlag[1]);
        engine.chord(0, 0);
        engine.update(5f);

        assertEquals(GameState.LOST, engine.state());
        assertEquals(revealedAfterLoss, board.getRevealedCount());
        assertEquals(1, engine.elapsedSeconds());
    }

    @Test
    void restartAndNewGameResetState() {
        GameEngine engine = new GameEngine(Difficulty.BEGINNER, 8L);
        engine.reveal(0, 0);
        engine.toggleFlag(0, 1);
        engine.update(2.5f);
        assertTrue(engine.elapsedSeconds() >= 2);

        engine.restart();

        assertEquals(GameState.READY, engine.state());
        assertEquals(0, engine.elapsedSeconds());
        assertEquals(10, engine.remainingMines());
        assertEquals(0, countMines(engine.board()));
        assertEquals(0, engine.board().getFlagCount());
        assertEquals(9, engine.board().getColumns());
        assertEquals(9, engine.board().getRows());

        engine.newGame(Difficulty.EXPERT);

        assertEquals(GameState.READY, engine.state());
        assertEquals(0, engine.elapsedSeconds());
        assertEquals(99, engine.remainingMines());
        assertEquals(0, countMines(engine.board()));
        assertEquals(30, engine.board().getColumns());
        assertEquals(16, engine.board().getRows());
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

    private static int[] findCell(Board board, Predicate<Cell> predicate) {
        for (int y = 0; y < board.getRows(); y++) {
            for (int x = 0; x < board.getColumns(); x++) {
                if (predicate.test(board.get(x, y))) {
                    return new int[]{x, y};
                }
            }
        }
        return null;
    }

    private static int[] findMine(Board board, int excludeX, int excludeY) {
        for (int y = 0; y < board.getRows(); y++) {
            for (int x = 0; x < board.getColumns(); x++) {
                if ((x == excludeX && y == excludeY) || !board.get(x, y).hasMine()) {
                    continue;
                }
                return new int[]{x, y};
            }
        }
        return null;
    }
}
