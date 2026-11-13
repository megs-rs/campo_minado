package com.exemplo.minesweeper.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public final class GameEngine {
    public static final int MAX_ELAPSED_SECONDS = 999;

    private final Random random;
    private Board board;
    private BoardConfig config;
    private GameState state;
    private int elapsedSeconds;
    private float elapsedAccumulator;
    private boolean questionMarksEnabled;

    public GameEngine(BoardConfig config) {
        this(config, new Random());
    }

    public GameEngine(BoardConfig config, long seed) {
        this(config, new Random(seed));
    }

    private GameEngine(BoardConfig config, Random random) {
        this.random = Objects.requireNonNull(random, "random");
        newGame(config);
    }

    public GameState state() {
        return state;
    }

    public Board board() {
        return board;
    }

    public BoardConfig config() {
        return config;
    }

    public int remainingMines() {
        return board.getMines() - board.getFlagCount();
    }

    public int elapsedSeconds() {
        return elapsedSeconds;
    }

    public boolean isQuestionMarksEnabled() {
        return questionMarksEnabled;
    }

    public void setQuestionMarksEnabled(boolean questionMarksEnabled) {
        this.questionMarksEnabled = questionMarksEnabled;
    }

    public void newGame(BoardConfig config) {
        this.config = Objects.requireNonNull(config, "config");
        this.board = new Board(config);
        this.state = GameState.READY;
        this.elapsedSeconds = 0;
        this.elapsedAccumulator = 0f;
    }

    public void restart() {
        newGame(config);
    }

    public void reveal(int x, int y) {
        if (state == GameState.WON || state == GameState.LOST || !board.inBounds(x, y)) {
            return;
        }

        Cell cell = board.get(x, y);
        if (cell.getState() == CellState.FLAGGED || cell.getState() == CellState.REVEALED) {
            return;
        }

        if (state == GameState.READY) {
            MinePlacer.placeMines(board, x, y, random);
            state = GameState.PLAYING;
            elapsedSeconds = 0;
            elapsedAccumulator = 0f;
        }

        if (cell.hasMine()) {
            lose(x, y);
            return;
        }

        FloodFill.reveal(board, x, y);
        checkWin();
    }

    public void toggleFlag(int x, int y) {
        if (state == GameState.WON || state == GameState.LOST || !board.inBounds(x, y)) {
            return;
        }

        CellState next = switch (board.get(x, y).getState()) {
            case COVERED -> CellState.FLAGGED;
            case FLAGGED -> questionMarksEnabled ? CellState.QUESTION : CellState.COVERED;
            case QUESTION -> CellState.COVERED;
            default -> null;
        };

        if (next != null) {
            board.setState(x, y, next);
        }
    }

    public void chord(int x, int y) {
        if (state != GameState.PLAYING || !board.inBounds(x, y)) {
            return;
        }

        Cell cell = board.get(x, y);
        if (cell.getState() != CellState.REVEALED || cell.getAdjacentMines() == 0) {
            return;
        }
        if (board.countFlagsAround(x, y) != cell.getAdjacentMines()) {
            return;
        }

        List<int[]> neighbors = new ArrayList<>();
        board.forEachNeighbor(x, y, (neighborX, neighborY) -> {
            Cell neighbor = board.get(neighborX, neighborY);
            if (neighbor.getState() != CellState.FLAGGED && neighbor.getState() != CellState.REVEALED) {
                neighbors.add(new int[]{neighborX, neighborY});
            }
        });

        for (int[] neighbor : neighbors) {
            if (state != GameState.PLAYING) {
                break;
            }
            reveal(neighbor[0], neighbor[1]);
        }
    }

    public void update(float delta) {
        if (state != GameState.PLAYING || !(delta > 0f)) {
            return;
        }

        elapsedAccumulator += delta;
        while (elapsedAccumulator >= 1f && elapsedSeconds < MAX_ELAPSED_SECONDS) {
            elapsedAccumulator -= 1f;
            elapsedSeconds++;
        }

        if (elapsedSeconds >= MAX_ELAPSED_SECONDS) {
            elapsedAccumulator = 0f;
        }
    }

    private void lose(int x, int y) {
        state = GameState.LOST;
        board.setState(x, y, CellState.EXPLODED);

        for (int row = 0; row < board.getRows(); row++) {
            for (int column = 0; column < board.getColumns(); column++) {
                if (column == x && row == y) {
                    continue;
                }
                Cell cell = board.get(column, row);
                if (cell.hasMine()) {
                    if (cell.getState() != CellState.FLAGGED) {
                        board.setState(column, row, CellState.MINE_SHOWN);
                    }
                } else if (cell.getState() == CellState.FLAGGED) {
                    board.setState(column, row, CellState.WRONG_FLAG);
                }
            }
        }
    }

    private void checkWin() {
        int safeCells = board.getColumns() * board.getRows() - board.getMines();
        if (board.getRevealedCount() != safeCells) {
            return;
        }

        state = GameState.WON;
        for (int row = 0; row < board.getRows(); row++) {
            for (int column = 0; column < board.getColumns(); column++) {
                Cell cell = board.get(column, row);
                if (cell.hasMine() && cell.getState() != CellState.FLAGGED) {
                    board.setState(column, row, CellState.FLAGGED);
                }
            }
        }
    }
}
