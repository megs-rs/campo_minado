package com.exemplo.minesweeper.persistence;

import com.badlogic.gdx.Preferences;
import com.exemplo.minesweeper.model.BoardConfig;
import com.exemplo.minesweeper.model.Difficulty;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class HighScores {
    public static final int DEFAULT_TIME = 999;
    public static final String DEFAULT_NAME = "Anônimo";
    public static final int MAX_NAME_LENGTH = 16;

    private final Preferences preferences;
    private final Map<Difficulty, Score> scores = new EnumMap<>(Difficulty.class);

    public HighScores(Preferences preferences) {
        this.preferences = Objects.requireNonNull(preferences, "preferences");
        load();
    }

    public Score best(Difficulty difficulty) {
        return scores.getOrDefault(difficulty, defaultScore());
    }

    public boolean isBetter(Difficulty difficulty, int seconds) {
        return seconds < best(difficulty).seconds();
    }

    public boolean isBetter(BoardConfig config, int seconds) {
        return config instanceof Difficulty difficulty && isBetter(difficulty, seconds);
    }

    public boolean submit(Difficulty difficulty, int seconds, String name) {
        Objects.requireNonNull(difficulty, "difficulty");
        if (!isBetter(difficulty, seconds)) {
            return false;
        }

        scores.put(difficulty, new Score(seconds, normalizeName(name)));
        save();
        return true;
    }

    public boolean submit(BoardConfig config, int seconds, String name) {
        return config instanceof Difficulty difficulty && submit(difficulty, seconds, name);
    }

    public void reset() {
        scores.clear();
        for (Difficulty difficulty : Difficulty.values()) {
            scores.put(difficulty, defaultScore());
        }
        save();
    }

    public static String normalizeName(String name) {
        if (name == null) {
            return DEFAULT_NAME;
        }
        String trimmed = name.strip();
        if (trimmed.isEmpty()) {
            return DEFAULT_NAME;
        }
        if (trimmed.length() <= MAX_NAME_LENGTH) {
            return trimmed;
        }
        return trimmed.substring(0, MAX_NAME_LENGTH);
    }

    private void load() {
        scores.clear();
        for (Difficulty difficulty : Difficulty.values()) {
            int seconds = preferences.getInteger(timeKey(difficulty), DEFAULT_TIME);
            if (seconds < 0 || seconds > DEFAULT_TIME) {
                seconds = DEFAULT_TIME;
            }
            String name = normalizeName(
                    preferences.getString(nameKey(difficulty), DEFAULT_NAME));
            scores.put(difficulty, new Score(seconds, name));
        }
    }

    private void save() {
        for (Difficulty difficulty : Difficulty.values()) {
            Score score = best(difficulty);
            preferences.putInteger(timeKey(difficulty), score.seconds());
            preferences.putString(nameKey(difficulty), score.name());
        }
        preferences.flush();
    }

    private static Score defaultScore() {
        return new Score(DEFAULT_TIME, DEFAULT_NAME);
    }

    private static String timeKey(Difficulty difficulty) {
        return "best." + difficulty.name().toLowerCase(Locale.ROOT) + ".time";
    }

    private static String nameKey(Difficulty difficulty) {
        return "best." + difficulty.name().toLowerCase(Locale.ROOT) + ".name";
    }

    public record Score(int seconds, String name) {
    }
}
