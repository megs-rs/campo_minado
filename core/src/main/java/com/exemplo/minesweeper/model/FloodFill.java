package com.exemplo.minesweeper.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

public final class FloodFill {
    private FloodFill() {
    }

    public static void reveal(Board board, int startX, int startY) {
        Objects.requireNonNull(board, "board");
        if (!board.inBounds(startX, startY)) {
            return;
        }

        Cell start = board.get(startX, startY);
        if (start.hasMine() || start.getState() == CellState.FLAGGED || start.getState() == CellState.REVEALED) {
            return;
        }

        Deque<int[]> queue = new ArrayDeque<>();
        queue.addLast(new int[]{startX, startY});

        while (!queue.isEmpty()) {
            int[] position = queue.removeFirst();
            int x = position[0];
            int y = position[1];
            Cell cell = board.get(x, y);

            if (cell.hasMine() || cell.getState() == CellState.FLAGGED || cell.getState() == CellState.REVEALED) {
                continue;
            }

            board.setState(x, y, CellState.REVEALED);

            if (cell.getAdjacentMines() == 0) {
                board.forEachNeighbor(x, y, (neighborX, neighborY) -> {
                    Cell neighbor = board.get(neighborX, neighborY);
                    if (!neighbor.hasMine()
                            && neighbor.getState() != CellState.FLAGGED
                            && neighbor.getState() != CellState.REVEALED) {
                        queue.addLast(new int[]{neighborX, neighborY});
                    }
                });
            }
        }
    }
}
