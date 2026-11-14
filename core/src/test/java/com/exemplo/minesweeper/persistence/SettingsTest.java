package com.exemplo.minesweeper.persistence;

import com.exemplo.minesweeper.model.CustomDifficulty;
import com.exemplo.minesweeper.model.Difficulty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettingsTest {
    @Test
    void loadsDefaultsWhenPreferencesAreEmpty() {
        Settings settings = new Settings(new InMemoryPreferences());

        assertEquals(Difficulty.BEGINNER, settings.getLastDifficulty());
        assertFalse(settings.isQuestionMarks());
        assertFalse(settings.isChordOnNumberClick());
        assertEquals(1, settings.getUiScale());
        assertTrue(settings.isSoundEnabled());
        assertFalse(settings.isDarkTheme());
    }

    @Test
    void persistsOptionsAcrossInstances() {
        InMemoryPreferences preferences = new InMemoryPreferences();
        Settings settings = new Settings(preferences);
        settings.setLastDifficulty(Difficulty.EXPERT);
        settings.setQuestionMarks(true);
        settings.setChordOnNumberClick(true);
        settings.setUiScale(2);
        settings.setSoundEnabled(false);
        settings.setDarkTheme(true);

        Settings reloaded = new Settings(preferences);

        assertEquals(Difficulty.EXPERT, reloaded.getLastDifficulty());
        assertTrue(reloaded.isQuestionMarks());
        assertTrue(reloaded.isChordOnNumberClick());
        assertEquals(2, reloaded.getUiScale());
        assertFalse(reloaded.isSoundEnabled());
        assertTrue(reloaded.isDarkTheme());
    }

    @Test
    void persistsCustomDifficultyValues() {
        InMemoryPreferences preferences = new InMemoryPreferences();
        Settings settings = new Settings(preferences);
        settings.setLastDifficulty(new CustomDifficulty(12, 20, 50));

        Settings reloaded = new Settings(preferences);

        assertEquals(new CustomDifficulty(12, 20, 50), reloaded.getLastDifficulty());
    }

    @Test
    void clampsScaleAndCustomDifficulty() {
        InMemoryPreferences preferences = new InMemoryPreferences();
        Settings settings = new Settings(preferences);
        settings.setUiScale(9);
        settings.setLastDifficulty(new CustomDifficulty(2, 99, 5000));

        Settings reloaded = new Settings(preferences);

        assertEquals(Settings.MAX_SCALE, reloaded.getUiScale());
        assertEquals(new CustomDifficulty(9, 24, 184), reloaded.getLastDifficulty());
    }

    @Test
    void fallsBackToBeginnerForUnknownDifficultyType() {
        InMemoryPreferences preferences = new InMemoryPreferences();
        preferences.putString("last.type", "UNKNOWN");

        Settings settings = new Settings(preferences);

        assertEquals(Difficulty.BEGINNER, settings.getLastDifficulty());
    }
}
