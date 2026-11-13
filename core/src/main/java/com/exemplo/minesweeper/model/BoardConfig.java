package com.exemplo.minesweeper.model;

public interface BoardConfig {
    int columns();

    int rows();

    int mines();

    String displayName();

    default Board createBoard() {
        return new Board(columns(), rows(), mines());
    }
}
