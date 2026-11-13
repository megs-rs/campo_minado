package com.exemplo.minesweeper.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.exemplo.minesweeper.model.GameEngine;
import com.exemplo.minesweeper.model.GameState;
import com.exemplo.minesweeper.render.BoardRenderer;

import java.util.Objects;

public final class GameScreen extends ScreenAdapter {
    private final GameEngine engine;
    private final BoardRenderer renderer;
    private final FitViewport viewport;
    private final Vector2 worldPoint = new Vector2();
    private final GameInput inputProcessor = new GameInput();

    private boolean leftDown;
    private boolean rightDown;

    public GameScreen(GameEngine engine) {
        this.engine = Objects.requireNonNull(engine, "engine");
        this.renderer = new BoardRenderer(engine.board().getColumns(), engine.board().getRows());
        this.viewport = new FitViewport(renderer.getWorldWidth(), renderer.getWorldHeight());
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputProcessor);
    }

    @Override
    public void hide() {
        if (Gdx.input.getInputProcessor() == inputProcessor) {
            Gdx.input.setInputProcessor(null);
        }
    }

    @Override
    public void render(float delta) {
        engine.update(delta);

        int pressedX = -1;
        int pressedY = -1;
        if (leftDown && engine.state() != GameState.WON && engine.state() != GameState.LOST) {
            int[] cell = cellAtScreen(Gdx.input.getX(), Gdx.input.getY());
            if (cell != null) {
                pressedX = cell[0];
                pressedY = cell[1];
            }
        }

        viewport.apply();
        renderer.render(engine.board(), engine.state(), pressedX, pressedY,
                viewport.getCamera().combined);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        renderer.dispose();
    }

    private int[] cellAtScreen(int screenX, int screenY) {
        worldPoint.set(screenX, screenY);
        viewport.unproject(worldPoint);
        return renderer.cellAt(worldPoint.x, worldPoint.y);
    }

    private void revealAt(int screenX, int screenY) {
        int[] cell = cellAtScreen(screenX, screenY);
        if (cell != null) {
            engine.reveal(cell[0], cell[1]);
        }
    }

    private void toggleFlagAt(int screenX, int screenY) {
        int[] cell = cellAtScreen(screenX, screenY);
        if (cell != null) {
            engine.toggleFlag(cell[0], cell[1]);
        }
    }

    private final class GameInput extends InputAdapter {
        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            if (pointer != 0) {
                return false;
            }

            if (button == Input.Buttons.LEFT) {
                leftDown = true;
                return true;
            }
            if (button == Input.Buttons.RIGHT) {
                rightDown = true;
                if (!leftDown) {
                    toggleFlagAt(screenX, screenY);
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            if (pointer != 0) {
                return false;
            }

            if (button == Input.Buttons.LEFT) {
                leftDown = false;
                revealAt(screenX, screenY);
                return true;
            }
            if (button == Input.Buttons.RIGHT) {
                rightDown = false;
                return true;
            }
            return false;
        }

        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.F2) {
                engine.restart();
                leftDown = false;
                rightDown = false;
                return true;
            }
            return false;
        }
    }
}
