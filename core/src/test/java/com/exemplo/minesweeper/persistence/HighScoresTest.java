package com.exemplo.minesweeper.persistence;

import com.exemplo.minesweeper.model.CustomDifficulty;
import com.exemplo.minesweeper.model.Difficulty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HighScoresTest {
    @Test
    void startsWithDefaultScoresForFixedDifficulties() {
        HighScores scores = new HighScores(new InMemoryPreferences());

        for (Difficulty difficulty : Difficulty.values()) {
            assertEquals(HighScores.DEFAULT_TIME, scores.best(difficulty).seconds());
            assertEquals(HighScores.DEFAULT_NAME, scores.best(difficulty).name());
        }
    }

    @Test
    void savesOnlyWhenTimeIsBetter() {
        HighScores scores = new HighScores(new InMemoryPreferences());

        assertTrue(scores.submit(Difficulty.BEGINNER, 42, "Ana"));
        assertFalse(scores.submit(Difficulty.BEGINNER, 42, "Bia"));
        assertFalse(scores.submit(Difficulty.BEGINNER, 50, "Caio"));
        assertTrue(scores.submit(Difficulty.BEGINNER, 30, "Duda"));

        assertEquals(30, scores.best(Difficulty.BEGINNER).seconds());
        assertEquals("Duda", scores.best(Difficulty.BEGINNER).name());
    }

    @Test
    void normalizesEmptyAndLongNames() {
        HighScores scores = new HighScores(new InMemoryPreferences());

        assertTrue(scores.submit(Difficulty.INTERMEDIATE, 80, "   "));
        assertEquals(HighScores.DEFAULT_NAME, scores.best(Difficulty.INTERMEDIATE).name());

        assertTrue(scores.submit(Difficulty.INTERMEDIATE, 70, "  12345678901234567890  "));
        assertEquals("1234567890123456", scores.best(Difficulty.INTERMEDIATE).name());
    }

    @Test
    void ignoresCustomDifficultyScores() {
        HighScores scores = new HighScores(new InMemoryPreferences());
        CustomDifficulty custom = new CustomDifficulty(9, 9, 10);

        assertFalse(scores.isBetter(custom, 1));
        assertFalse(scores.submit(custom, 1, "Ana"));
        assertEquals(HighScores.DEFAULT_TIME, scores.best(Difficulty.BEGINNER).seconds());
    }

    @Test
    void persistsAndResetsScores() {
        InMemoryPreferences preferences = new InMemoryPreferences();
        HighScores scores = new HighScores(preferences);
        assertTrue(scores.submit(Difficulty.EXPERT, 120, "Eva"));

        HighScores reloaded = new HighScores(preferences);
        assertEquals(120, reloaded.best(Difficulty.EXPERT).seconds());
        assertEquals("Eva", reloaded.best(Difficulty.EXPERT).name());

        reloaded.reset();

        for (Difficulty difficulty : Difficulty.values()) {
            assertEquals(HighScores.DEFAULT_TIME, reloaded.best(difficulty).seconds());
            assertEquals(HighScores.DEFAULT_NAME, reloaded.best(difficulty).name());
        }
        HighScores afterReset = new HighScores(preferences);
        assertEquals(HighScores.DEFAULT_TIME, afterReset.best(Difficulty.EXPERT).seconds());
    }

    @Test
    void ignoresCorruptedStoredTimes() {
        InMemoryPreferences preferences = new InMemoryPreferences();
        preferences.putInteger("best.beginner.time", -5);
        preferences.putInteger("best.intermediate.time", 2000);

        HighScores scores = new HighScores(preferences);

        assertEquals(HighScores.DEFAULT_TIME, scores.best(Difficulty.BEGINNER).seconds());
        assertEquals(HighScores.DEFAULT_TIME, scores.best(Difficulty.INTERMEDIATE).seconds());
    }
}
