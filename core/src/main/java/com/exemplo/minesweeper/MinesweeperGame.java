package com.exemplo.minesweeper;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.exemplo.minesweeper.model.Difficulty;
import com.exemplo.minesweeper.model.GameEngine;
import com.exemplo.minesweeper.screen.GameScreen;

public class MinesweeperGame extends Game {
    @Override
    public void create() {
        Gdx.gl.glClearColor(0.12f, 0.12f, 0.12f, 1f);
        setScreen(new GameScreen(new GameEngine(Difficulty.BEGINNER)));
    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        super.render();
    }

    @Override
    public void dispose() {
        Screen screen = getScreen();
        if (screen != null) {
            screen.dispose();
        }
        super.dispose();
    }
}
