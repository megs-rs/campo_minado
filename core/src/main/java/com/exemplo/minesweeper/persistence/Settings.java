package com.exemplo.minesweeper.persistence;

import com.badlogic.gdx.Preferences;
import com.exemplo.minesweeper.model.BoardConfig;
import com.exemplo.minesweeper.model.CustomDifficulty;
import com.exemplo.minesweeper.model.Difficulty;

import java.util.Objects;

public final class Settings {
    public static final String PREFERENCES_NAME = "minesweeper";
    public static final int MIN_SCALE = 1;
    public static final int MAX_SCALE = 3;

    private static final String KEY_LAST_TYPE = "last.type";
    private static final String KEY_LAST_COLUMNS = "last.columns";
    private static final String KEY_LAST_ROWS = "last.rows";
    private static final String KEY_LAST_MINES = "last.mines";
    private static final String KEY_QUESTION_MARKS = "settings.questionMarks";
    private static final String KEY_CHORD_ON_NUMBER = "settings.chordOnNumber";
    private static final String KEY_SCALE = "settings.scale";
    private static final String KEY_SOUND = "settings.sound";
    private static final String KEY_DARK_THEME = "settings.darkTheme";

    private final Preferences preferences;
    private BoardConfig lastDifficulty;
    private boolean questionMarks;
    private boolean chordOnNumberClick;
    private int uiScale;
    private boolean soundEnabled;
    private boolean darkTheme;

    public Settings(Preferences preferences) {
        this.preferences = Objects.requireNonNull(preferences, "preferences");
        load();
    }

    public BoardConfig getLastDifficulty() {
        return lastDifficulty;
    }

    public void setLastDifficulty(BoardConfig config) {
        Objects.requireNonNull(config, "config");
        lastDifficulty = config;
        preferences.putString(KEY_LAST_TYPE, typeOf(config));
        if (config instanceof CustomDifficulty custom) {
            preferences.putInteger(KEY_LAST_COLUMNS, custom.columns());
            preferences.putInteger(KEY_LAST_ROWS, custom.rows());
            preferences.putInteger(KEY_LAST_MINES, custom.mines());
        }
        preferences.flush();
    }

    public boolean isQuestionMarks() {
        return questionMarks;
    }

    public void setQuestionMarks(boolean questionMarks) {
        this.questionMarks = questionMarks;
        preferences.putBoolean(KEY_QUESTION_MARKS, questionMarks);
        preferences.flush();
    }

    public boolean isChordOnNumberClick() {
        return chordOnNumberClick;
    }

    public void setChordOnNumberClick(boolean chordOnNumberClick) {
        this.chordOnNumberClick = chordOnNumberClick;
        preferences.putBoolean(KEY_CHORD_ON_NUMBER, chordOnNumberClick);
        preferences.flush();
    }

    public int getUiScale() {
        return uiScale;
    }

    public void setUiScale(int uiScale) {
        this.uiScale = clampScale(uiScale);
        preferences.putInteger(KEY_SCALE, this.uiScale);
        preferences.flush();
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
        preferences.putBoolean(KEY_SOUND, soundEnabled);
        preferences.flush();
    }

    public boolean isDarkTheme() {
        return darkTheme;
    }

    public void setDarkTheme(boolean darkTheme) {
        this.darkTheme = darkTheme;
        preferences.putBoolean(KEY_DARK_THEME, darkTheme);
        preferences.flush();
    }

    public void save() {
        preferences.flush();
    }

    private void load() {
        lastDifficulty = loadLastDifficulty();
        questionMarks = preferences.getBoolean(KEY_QUESTION_MARKS, false);
        chordOnNumberClick = preferences.getBoolean(KEY_CHORD_ON_NUMBER, false);
        uiScale = clampScale(preferences.getInteger(KEY_SCALE, MIN_SCALE));
        soundEnabled = preferences.getBoolean(KEY_SOUND, true);
        darkTheme = preferences.getBoolean(KEY_DARK_THEME, false);
    }

    private BoardConfig loadLastDifficulty() {
        String type = preferences.getString(KEY_LAST_TYPE, Difficulty.BEGINNER.name());
        if (typeOfCustom().equals(type)) {
            int columns = preferences.getInteger(KEY_LAST_COLUMNS,
                    CustomDifficulty.MIN_COLUMNS);
            int rows = preferences.getInteger(KEY_LAST_ROWS,
                    CustomDifficulty.MIN_ROWS);
            int mines = preferences.getInteger(KEY_LAST_MINES,
                    CustomDifficulty.MIN_MINES);
            return new CustomDifficulty(columns, rows, mines);
        }

        try {
            return Difficulty.valueOf(type);
        } catch (IllegalArgumentException exception) {
            return Difficulty.BEGINNER;
        }
    }

    private static String typeOf(BoardConfig config) {
        if (config instanceof CustomDifficulty) {
            return typeOfCustom();
        }
        if (config instanceof Difficulty difficulty) {
            return difficulty.name();
        }
        throw new IllegalArgumentException("Unsupported board config: " + config);
    }

    private static String typeOfCustom() {
        return "CUSTOM";
    }

    private static int clampScale(int scale) {
        return Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale));
    }
}
