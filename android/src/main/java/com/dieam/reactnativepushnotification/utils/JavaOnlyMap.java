package com.dieam.reactnativepushnotification.utils;


import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class JavaOnlyMap implements ReadableMap, WritableMap {

    private final Map mBackingMap;

    public static JavaOnlyMap of(Object... keysAndValues) {
        return new JavaOnlyMap(keysAndValues);
    }

    private JavaOnlyMap(Object... keysAndValues) {
        if (keysAndValues.length % 2 != 0) {
            throw new IllegalArgumentException("You must provide the same number of keys and values");
        }
        mBackingMap = new HashMap();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            mBackingMap.put(keysAndValues[i], keysAndValues[i + 1]);
        }
    }

    public JavaOnlyMap() {
        mBackingMap = new HashMap();
    }

    @Override
    public boolean hasKey(String name) {
        return mBackingMap.containsKey(name);
    }

    @Override
    public boolean isNull(String name) {
        return mBackingMap.get(name) == null;
    }

    @Override
    public boolean getBoolean(String name) {
        return (Boolean) mBackingMap.get(name);
    }

    @Override
    public double getDouble(String name) {
        return (Double) mBackingMap.get(name);
    }

    @Override
    public int getInt(String name) {
        return (Integer) mBackingMap.get(name);
    }

    @Override
    public String getString(String name) {
        return (String) mBackingMap.get(name);
    }

    @Override
    public JavaOnlyMap getMap(String name) {
        return (JavaOnlyMap) mBackingMap.get(name);
    }

    @Override
    public JavaOnlyArray getArray(String name) {
        return (JavaOnlyArray) mBackingMap.get(name);
    }

    @Override
    public ReadableType getType(String name) {
        Object value = mBackingMap.get(name);
        if (value == null) {
            return ReadableType.Null;
        } else if (value instanceof Number) {
            return ReadableType.Number;
        } else if (value instanceof String) {
            return ReadableType.String;
        } else if (value instanceof Boolean) {
            return ReadableType.Boolean;
        } else if (value instanceof ReadableMap) {
            return ReadableType.Map;
        } else if (value instanceof ReadableArray) {
            return ReadableType.Array;
        } else {
            throw new IllegalArgumentException("Invalid value " + value.toString() + " for key " + name +
                    "contained in JavaOnlyMap");
        }
    }

    @Override
    public ReadableMapKeySetIterator keySetIterator() {
        return new ReadableMapKeySetIterator() {
            Iterator<String> mIterator = mBackingMap.keySet().iterator();

            @Override
            public boolean hasNextKey() {
                return mIterator.hasNext();
            }

            @Override
            public String nextKey() {
                return mIterator.next();
            }
        };
    }

    @Override
    public void putBoolean(String key, boolean value) {
        mBackingMap.put(key, value);
    }

    @Override
    public void putDouble(String key, double value) {
        mBackingMap.put(key, value);
    }

    @Override
    public void putInt(String key, int value) {
        mBackingMap.put(key, value);
    }

    @Override
    public void putString(String key, String value) {
        mBackingMap.put(key, value);
    }

    @Override
    public void putNull(String key) {
        mBackingMap.put(key, null);
    }

    @Override
    public void putMap(String key, WritableMap value) {
        mBackingMap.put(key, value);
    }

    @Override
    public void merge(ReadableMap source) {
        mBackingMap.putAll(((JavaOnlyMap) source).mBackingMap);
    }

    @Override
    public void putArray(String key, WritableArray value) {
        mBackingMap.put(key, value);
    }

    @Override
    public String toString() {
        return mBackingMap.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JavaOnlyMap that = (JavaOnlyMap) o;

        if (mBackingMap != null ? !mBackingMap.equals(that.mBackingMap) : that.mBackingMap != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return mBackingMap != null ? mBackingMap.hashCode() : 0;
    }
}