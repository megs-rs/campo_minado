package com.exemplo.minesweeper;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

public class MinesweeperGame extends Game {
    @Override
    public void create() {
        Gdx.gl.glClearColor(0.12f, 0.12f, 0.12f, 1f);
    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        super.render();
    }
}
