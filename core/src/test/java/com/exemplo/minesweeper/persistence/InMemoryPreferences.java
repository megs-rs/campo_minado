package com.exemplo.minesweeper.persistence;

import com.badlogic.gdx.Preferences;

import java.util.HashMap;
import java.util.Map;

final class InMemoryPreferences implements Preferences {
    private final Map<String, Object> values = new HashMap<>();

    @Override
    public Preferences putBoolean(String key, boolean value) {
        values.put(key, value);
        return this;
    }

    @Override
    public Preferences putInteger(String key, int value) {
        values.put(key, value);
        return this;
    }

    @Override
    public Preferences putLong(String key, long value) {
        values.put(key, value);
        return this;
    }

    @Override
    public Preferences putFloat(String key, float value) {
        values.put(key, value);
        return this;
    }

    @Override
    public Preferences putString(String key, String value) {
        values.put(key, value);
        return this;
    }

    @Override
    public Preferences put(Map<String, ?> values) {
        this.values.putAll(values);
        return this;
    }

    @Override
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    @Override
    public int getInteger(String key) {
        return getInteger(key, 0);
    }

    @Override
    public long getLong(String key) {
        return getLong(key, 0L);
    }

    @Override
    public float getFloat(String key) {
        return getFloat(key, 0f);
    }

    @Override
    public String getString(String key) {
        return getString(key, "");
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = values.get(key);
        return value instanceof Boolean booleanValue ? booleanValue : defaultValue;
    }

    @Override
    public int getInteger(String key, int defaultValue) {
        Object value = values.get(key);
        return value instanceof Integer integerValue ? integerValue : defaultValue;
    }

    @Override
    public long getLong(String key, long defaultValue) {
        Object value = values.get(key);
        return value instanceof Long longValue ? longValue : defaultValue;
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        Object value = values.get(key);
        return value instanceof Float floatValue ? floatValue : defaultValue;
    }

    @Override
    public String getString(String key, String defaultValue) {
        Object value = values.get(key);
        return value instanceof String stringValue ? stringValue : defaultValue;
    }

    @Override
    public Map<String, ?> get() {
        return Map.copyOf(values);
    }

    @Override
    public boolean contains(String key) {
        return values.containsKey(key);
    }

    @Override
    public void clear() {
        values.clear();
    }

    @Override
    public void remove(String key) {
        values.remove(key);
    }

    @Override
    public void flush() {
    }
}
