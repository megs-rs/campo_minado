package com.exemplo.minesweeper.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.exemplo.minesweeper.model.BoardConfig;
import com.exemplo.minesweeper.model.Cell;
import com.exemplo.minesweeper.model.CellState;
import com.exemplo.minesweeper.model.CustomDifficulty;
import com.exemplo.minesweeper.model.Difficulty;
import com.exemplo.minesweeper.model.GameEngine;
import com.exemplo.minesweeper.model.GameState;
import com.exemplo.minesweeper.persistence.HighScores;
import com.exemplo.minesweeper.persistence.Settings;
import com.exemplo.minesweeper.render.BoardRenderer;
import com.exemplo.minesweeper.render.GameLayout;
import com.exemplo.minesweeper.render.GameUiRenderer;

import java.util.Objects;

public final class GameScreen extends ScreenAdapter {
    private static final int CUSTOM_COLUMNS = 0;
    private static final int CUSTOM_ROWS = 1;
    private static final int CUSTOM_MINES = 2;

    private final GameEngine engine;
    private final Settings settings;
    private final HighScores highScores;
    private final FitViewport viewport = new FitViewport(1f, 1f);
    private final Vector2 worldPoint = new Vector2();
    private final GameInput inputProcessor = new GameInput();
    private final boolean[] customFieldEdited = new boolean[3];

    private BoardRenderer boardRenderer;
    private GameUiRenderer uiRenderer;
    private GameLayout layout;
    private BoardConfig currentConfig;
    private float uiScale = 1f;
    private boolean questionMarks;
    private boolean chordOnNumberClick;

    private boolean leftDown;
    private boolean rightDown;
    private boolean middleDown;
    private boolean chordGesture;
    private boolean suppressNextRelease;
    private boolean facePressed;

    private boolean menuOpen;
    private boolean customDialogOpen;
    private int customField;
    private String customColumns;
    private String customRows;
    private String customMines;

    private boolean highScoresOpen;
    private boolean resetConfirmationOpen;
    private boolean newRecordDialogOpen;
    private boolean recordHandled;
    private String newRecordName = "";

    public GameScreen(GameEngine engine, Settings settings, HighScores highScores) {
        this.engine = Objects.requireNonNull(engine, "engine");
        this.settings = Objects.requireNonNull(settings, "settings");
        this.highScores = Objects.requireNonNull(highScores, "highScores");
        this.currentConfig = engine.config();
        this.uiScale = settings.getUiScale();
        this.questionMarks = settings.isQuestionMarks();
        this.chordOnNumberClick = settings.isChordOnNumberClick();
        engine.setQuestionMarksEnabled(questionMarks);
        rebuildLayout();
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
        handleNewRecordOnWin();

        int[] currentCell = cellAtScreen(Gdx.input.getX(), Gdx.input.getY());
        boolean boardButtonDown = !isModalOpen()
                && (leftDown || rightDown || middleDown)
                && currentCell != null;

        int pressedX = -1;
        int pressedY = -1;
        int chordX = -1;
        int chordY = -1;
        if (!isModalOpen() && currentCell != null) {
            if (chordGesture && leftDown && rightDown) {
                Cell cell = engine.board().get(currentCell[0], currentCell[1]);
                if (cell.getState() == CellState.REVEALED && cell.getAdjacentMines() > 0) {
                    chordX = currentCell[0];
                    chordY = currentCell[1];
                }
            } else if (leftDown) {
                pressedX = currentCell[0];
                pressedY = currentCell[1];
            }
        }

        viewport.apply();
        boardRenderer.render(engine.board(), engine.state(), pressedX, pressedY,
                chordX, chordY, viewport.getCamera().combined);
        uiRenderer.renderHud(engine, boardButtonDown, facePressed,
                viewport.getCamera().combined);

        if (newRecordDialogOpen) {
            uiRenderer.renderNewRecord(currentConfig.displayName(),
                    engine.elapsedSeconds(), newRecordName,
                    viewport.getCamera().combined);
        } else if (resetConfirmationOpen) {
            uiRenderer.renderResetConfirmation(viewport.getCamera().combined);
        } else if (highScoresOpen) {
            uiRenderer.renderHighScores(highScoreLines(),
                    viewport.getCamera().combined);
        } else if (customDialogOpen) {
            uiRenderer.renderCustomDialog(customColumns, customRows, customMines,
                    customField, viewport.getCamera().combined);
        } else if (menuOpen) {
            uiRenderer.renderMenu(currentConfig.displayName(), questionMarks,
                    chordOnNumberClick, uiScale, viewport.getCamera().combined);
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        if (boardRenderer != null) {
            boardRenderer.dispose();
        }
        if (uiRenderer != null) {
            uiRenderer.dispose();
        }
    }

    private void rebuildLayout() {
        GameLayout nextLayout = new GameLayout(
                engine.board().getColumns(), engine.board().getRows(), uiScale);

        if (boardRenderer != null) {
            boardRenderer.dispose();
        }
        if (uiRenderer != null) {
            uiRenderer.dispose();
        }

        layout = nextLayout;
        boardRenderer = new BoardRenderer(layout);
        uiRenderer = new GameUiRenderer(layout);
        viewport.setWorldSize(layout.getWorldWidth(), layout.getWorldHeight());
        resizeWindowToLayout();
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }

    private void resizeWindowToLayout() {
        int desiredWidth = Math.max(1, Math.round(layout.getWorldWidth()));
        int desiredHeight = Math.max(1, Math.round(layout.getWorldHeight()));
        Graphics.DisplayMode displayMode = Gdx.graphics.getDisplayMode();
        if (displayMode != null) {
            int maxWidth = Math.max(320, Math.round(displayMode.width * 0.92f));
            int maxHeight = Math.max(240, Math.round(displayMode.height * 0.88f));
            desiredWidth = Math.min(desiredWidth, maxWidth);
            desiredHeight = Math.min(desiredHeight, maxHeight);
        }
        Gdx.graphics.setWindowedMode(desiredWidth, desiredHeight);
    }

    private int[] cellAtScreen(int screenX, int screenY) {
        updateWorldPoint(screenX, screenY);
        return boardRenderer.cellAt(worldPoint.x, worldPoint.y);
    }

    private void updateWorldPoint(int screenX, int screenY) {
        worldPoint.set(screenX, screenY);
        viewport.unproject(worldPoint);
    }

    private boolean isModalOpen() {
        return menuOpen || customDialogOpen || highScoresOpen
                || resetConfirmationOpen || newRecordDialogOpen;
    }

    private void handleNewRecordOnWin() {
        if (recordHandled || engine.state() != GameState.WON) {
            return;
        }
        recordHandled = true;
        if (!highScores.isBetter(engine.config(), engine.elapsedSeconds())) {
            return;
        }

        newRecordName = "";
        newRecordDialogOpen = true;
        menuOpen = false;
        customDialogOpen = false;
        highScoresOpen = false;
        resetConfirmationOpen = false;
        resetInputState();
    }

    private String[] highScoreLines() {
        Difficulty[] difficulties = Difficulty.values();
        String[] lines = new String[difficulties.length + 2];
        for (int index = 0; index < difficulties.length; index++) {
            Difficulty difficulty = difficulties[index];
            HighScores.Score score = highScores.best(difficulty);
            lines[index] = difficulty.displayName() + ": "
                    + score.seconds() + " s - " + score.name();
        }
        lines[difficulties.length] = "F4/Esc: fechar";
        lines[difficulties.length + 1] = "Zerar recordes (R)";
        return lines;
    }

    private void openHighScores() {
        highScoresOpen = true;
        resetConfirmationOpen = false;
        menuOpen = false;
        customDialogOpen = false;
        resetInputState();
    }

    private void closeHighScores() {
        highScoresOpen = false;
        resetConfirmationOpen = false;
    }

    private void submitNewRecord() {
        if (engine.config() instanceof Difficulty difficulty) {
            highScores.submit(difficulty, engine.elapsedSeconds(), newRecordName);
        }
        newRecordDialogOpen = false;
        newRecordName = "";
    }

    private void restart() {
        engine.restart();
        menuOpen = false;
        customDialogOpen = false;
        highScoresOpen = false;
        resetConfirmationOpen = false;
        newRecordDialogOpen = false;
        recordHandled = false;
        newRecordName = "";
        resetInputState();
    }

    private void changeDifficulty(BoardConfig config) {
        currentConfig = config;
        engine.newGame(config);
        engine.setQuestionMarksEnabled(questionMarks);
        settings.setLastDifficulty(config);
        menuOpen = false;
        customDialogOpen = false;
        highScoresOpen = false;
        resetConfirmationOpen = false;
        newRecordDialogOpen = false;
        recordHandled = false;
        newRecordName = "";
        resetInputState();
        rebuildLayout();
    }

    private void applyScale(float scale) {
        if (uiScale == scale) {
            return;
        }
        uiScale = scale;
        settings.setUiScale(Math.round(scale));
        resetInputState();
        rebuildLayout();
    }

    private void toggleQuestionMarks() {
        questionMarks = !questionMarks;
        engine.setQuestionMarksEnabled(questionMarks);
        settings.setQuestionMarks(questionMarks);
    }

    private void toggleChordOnNumberClick() {
        chordOnNumberClick = !chordOnNumberClick;
        settings.setChordOnNumberClick(chordOnNumberClick);
    }

    private void openCustomDialog() {
        customColumns = Integer.toString(engine.board().getColumns());
        customRows = Integer.toString(engine.board().getRows());
        customMines = Integer.toString(engine.board().getMines());
        customField = CUSTOM_COLUMNS;
        customFieldEdited[CUSTOM_COLUMNS] = false;
        customFieldEdited[CUSTOM_ROWS] = false;
        customFieldEdited[CUSTOM_MINES] = false;
        menuOpen = false;
        customDialogOpen = true;
        resetInputState();
    }

    private void submitCustomDialog() {
        int fallbackColumns = engine.board().getColumns();
        int fallbackRows = engine.board().getRows();
        int fallbackMines = engine.board().getMines();
        CustomDifficulty custom = new CustomDifficulty(
                parseField(customColumns, fallbackColumns),
                parseField(customRows, fallbackRows),
                parseField(customMines, fallbackMines));
        changeDifficulty(custom);
    }

    private int parseField(String value, int fallback) {
        if (value == null || value.isEmpty()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private void resetInputState() {
        leftDown = false;
        rightDown = false;
        middleDown = false;
        chordGesture = false;
        suppressNextRelease = false;
        facePressed = false;
    }

    private void reviseCustomField(int digit, boolean backspace) {
        String value = activeCustomValue();
        if (backspace) {
            if (value.isEmpty()) {
                return;
            }
            setActiveCustomValue(value.substring(0, value.length() - 1));
            customFieldEdited[customField] = true;
            return;
        }

        if (digit < 0) {
            return;
        }
        if (!customFieldEdited[customField]) {
            setActiveCustomValue(Integer.toString(digit));
            customFieldEdited[customField] = true;
            return;
        }
        if (value.length() >= 3) {
            return;
        }
        setActiveCustomValue(value + digit);
    }

    private String activeCustomValue() {
        return switch (customField) {
            case CUSTOM_COLUMNS -> customColumns;
            case CUSTOM_ROWS -> customRows;
            default -> customMines;
        };
    }

    private void setActiveCustomValue(String value) {
        switch (customField) {
            case CUSTOM_COLUMNS -> customColumns = value;
            case CUSTOM_ROWS -> customRows = value;
            default -> customMines = value;
        }
    }

    private void revealOrChordAt(int screenX, int screenY) {
        int[] cell = cellAtScreen(screenX, screenY);
        if (cell == null) {
            return;
        }
        if (chordOnNumberClick) {
            Cell modelCell = engine.board().get(cell[0], cell[1]);
            if (modelCell.getState() == CellState.REVEALED && modelCell.getAdjacentMines() > 0) {
                engine.chord(cell[0], cell[1]);
                return;
            }
        }
        engine.reveal(cell[0], cell[1]);
    }

    private void chordAt(int screenX, int screenY) {
        int[] cell = cellAtScreen(screenX, screenY);
        if (cell != null) {
            engine.chord(cell[0], cell[1]);
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
            if (isModalOpen()) {
                return true;
            }

            updateWorldPoint(screenX, screenY);
            if (uiRenderer.isFaceAt(worldPoint.x, worldPoint.y)) {
                if (button == Input.Buttons.LEFT) {
                    facePressed = true;
                }
                return true;
            }

            if (button == Input.Buttons.LEFT) {
                leftDown = true;
                updateChordGesture();
                return true;
            }
            if (button == Input.Buttons.RIGHT) {
                rightDown = true;
                updateChordGesture();
                return true;
            }
            if (button == Input.Buttons.MIDDLE) {
                middleDown = true;
                chordAt(screenX, screenY);
                return true;
            }
            return false;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            if (pointer != 0) {
                return false;
            }
            if (newRecordDialogOpen) {
                return true;
            }
            if (resetConfirmationOpen) {
                if (button == Input.Buttons.LEFT) {
                    updateWorldPoint(screenX, screenY);
                    if (uiRenderer.isResetConfirmAt(worldPoint.x, worldPoint.y)) {
                        highScores.reset();
                        resetConfirmationOpen = false;
                    } else if (uiRenderer.isResetCancelAt(worldPoint.x, worldPoint.y)) {
                        resetConfirmationOpen = false;
                    }
                }
                return true;
            }
            if (highScoresOpen) {
                if (button == Input.Buttons.LEFT) {
                    updateWorldPoint(screenX, screenY);
                    if (uiRenderer.isHighScoresResetAt(worldPoint.x, worldPoint.y)) {
                        resetConfirmationOpen = true;
                    }
                }
                return true;
            }
            if (menuOpen || customDialogOpen) {
                return true;
            }

            if (facePressed) {
                if (button == Input.Buttons.LEFT) {
                    updateWorldPoint(screenX, screenY);
                    if (uiRenderer.isFaceAt(worldPoint.x, worldPoint.y)) {
                        restart();
                    }
                    facePressed = false;
                }
                return true;
            }

            if (button == Input.Buttons.LEFT) {
                leftDown = false;
                if (chordGesture) {
                    chordAt(screenX, screenY);
                    chordGesture = false;
                    suppressNextRelease = true;
                } else if (!suppressNextRelease) {
                    revealOrChordAt(screenX, screenY);
                }
                finishRelease();
                return true;
            }
            if (button == Input.Buttons.RIGHT) {
                rightDown = false;
                if (chordGesture) {
                    chordAt(screenX, screenY);
                    chordGesture = false;
                    suppressNextRelease = true;
                } else if (!suppressNextRelease) {
                    toggleFlagAt(screenX, screenY);
                }
                finishRelease();
                return true;
            }
            if (button == Input.Buttons.MIDDLE) {
                middleDown = false;
                finishRelease();
                return true;
            }
            return false;
        }

        @Override
        public boolean keyDown(int keycode) {
            if (newRecordDialogOpen) {
                return handleNewRecordKey(keycode);
            }
            if (resetConfirmationOpen) {
                return handleResetConfirmationKey(keycode);
            }
            if (highScoresOpen) {
                return handleHighScoresKey(keycode);
            }
            if (customDialogOpen) {
                return handleCustomDialogKey(keycode);
            }

            if (keycode == Input.Keys.F4) {
                openHighScores();
                return true;
            }
            if (keycode == Input.Keys.F5 || keycode == Input.Keys.ESCAPE) {
                menuOpen = !menuOpen;
                resetInputState();
                return true;
            }
            if (keycode == Input.Keys.F2) {
                restart();
                return true;
            }
            if (keycode == Input.Keys.NUM_1) {
                changeDifficulty(Difficulty.BEGINNER);
                return true;
            }
            if (keycode == Input.Keys.NUM_2) {
                changeDifficulty(Difficulty.INTERMEDIATE);
                return true;
            }
            if (keycode == Input.Keys.NUM_3) {
                changeDifficulty(Difficulty.EXPERT);
                return true;
            }
            if (keycode == Input.Keys.NUM_4) {
                applyScale(1f);
                return true;
            }
            if (keycode == Input.Keys.NUM_5) {
                applyScale(2f);
                return true;
            }
            if (keycode == Input.Keys.NUM_6) {
                applyScale(3f);
                return true;
            }
            if (keycode == Input.Keys.C) {
                openCustomDialog();
                return true;
            }
            if (keycode == Input.Keys.M) {
                toggleQuestionMarks();
                return true;
            }
            if (keycode == Input.Keys.X) {
                toggleChordOnNumberClick();
                return true;
            }
            return false;
        }

        private boolean handleNewRecordKey(int keycode) {
            if (keycode == Input.Keys.ENTER) {
                submitNewRecord();
                return true;
            }
            if (keycode == Input.Keys.ESCAPE) {
                newRecordDialogOpen = false;
                newRecordName = "";
                return true;
            }
            if (keycode == Input.Keys.BACKSPACE) {
                if (!newRecordName.isEmpty()) {
                    newRecordName = newRecordName.substring(0, newRecordName.length() - 1);
                }
                return true;
            }

            if (newRecordName.length() >= HighScores.MAX_NAME_LENGTH) {
                return true;
            }
            char character = nameCharacterForKey(keycode);
            if (character != 0) {
                newRecordName += character;
            }
            return true;
        }

        private boolean handleHighScoresKey(int keycode) {
            if (keycode == Input.Keys.F4 || keycode == Input.Keys.ESCAPE) {
                closeHighScores();
                return true;
            }
            if (keycode == Input.Keys.R) {
                resetConfirmationOpen = true;
            }
            return true;
        }

        private boolean handleResetConfirmationKey(int keycode) {
            if (keycode == Input.Keys.S || keycode == Input.Keys.Y
                    || keycode == Input.Keys.ENTER) {
                highScores.reset();
                resetConfirmationOpen = false;
                return true;
            }
            if (keycode == Input.Keys.N || keycode == Input.Keys.ESCAPE) {
                resetConfirmationOpen = false;
            }
            return true;
        }

        private boolean handleCustomDialogKey(int keycode) {
            if (keycode == Input.Keys.ESCAPE) {
                customDialogOpen = false;
                resetInputState();
                return true;
            }
            if (keycode == Input.Keys.TAB || keycode == Input.Keys.DOWN) {
                customField = (customField + 1) % 3;
                return true;
            }
            if (keycode == Input.Keys.UP) {
                customField = (customField + 2) % 3;
                return true;
            }
            if (keycode == Input.Keys.ENTER) {
                submitCustomDialog();
                return true;
            }
            if (keycode == Input.Keys.BACKSPACE) {
                reviseCustomField(-1, true);
                return true;
            }

            int digit = digitForKey(keycode);
            if (digit >= 0) {
                reviseCustomField(digit, false);
            }
            return true;
        }
    }

    private void updateChordGesture() {
        if (leftDown && rightDown) {
            chordGesture = true;
        }
    }

    private void finishRelease() {
        if (!leftDown && !rightDown) {
            chordGesture = false;
            suppressNextRelease = false;
        }
    }

    private static int digitForKey(int keycode) {
        if (keycode >= Input.Keys.NUM_0 && keycode <= Input.Keys.NUM_9) {
            return keycode - Input.Keys.NUM_0;
        }
        return -1;
    }

    private static char nameCharacterForKey(int keycode) {
        if (keycode >= Input.Keys.A && keycode <= Input.Keys.Z) {
            return (char) ('A' + keycode - Input.Keys.A);
        }
        int digit = digitForKey(keycode);
        if (digit >= 0) {
            return (char) ('0' + digit);
        }
        if (keycode == Input.Keys.SPACE) {
            return ' ';
        }
        if (keycode == Input.Keys.MINUS) {
            return '-';
        }
        if (keycode == Input.Keys.PERIOD) {
            return '.';
        }
        return 0;
    }
}
