package com.dieam.reactnativepushnotification.utils;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JavaOnlyArray implements ReadableArray, WritableArray {

    private final List mBackingList;

    public static JavaOnlyArray from(List list) {
        return new JavaOnlyArray(list);
    }

    public static JavaOnlyArray of(Object... values) {
        return new JavaOnlyArray(values);
    }

    private JavaOnlyArray(Object... values) {
        mBackingList = Arrays.asList(values);
    }

    private JavaOnlyArray(List list) {
        mBackingList = new ArrayList(list);
    }

    public JavaOnlyArray() {
        mBackingList = new ArrayList();
    }

    @Override
    public int size() {
        return mBackingList.size();
    }

    @Override
    public boolean isNull(int index) {
        return mBackingList.get(index) == null;
    }

    @Override
    public double getDouble(int index) {
        return (Double) mBackingList.get(index);
    }

    @Override
    public int getInt(int index) {
        return (Integer) mBackingList.get(index);
    }

    @Override
    public String getString(int index) {
        return (String) mBackingList.get(index);
    }

    @Override
    public JavaOnlyArray getArray(int index) {
        return (JavaOnlyArray) mBackingList.get(index);
    }

    @Override
    public boolean getBoolean(int index) {
        return (Boolean) mBackingList.get(index);
    }

    @Override
    public JavaOnlyMap getMap(int index) {
        return (JavaOnlyMap) mBackingList.get(index);
    }

    @Override
    public ReadableType getType(int index) {
        Object object = mBackingList.get(index);

        if (object == null) {
            return ReadableType.Null;
        } else if (object instanceof Boolean) {
            return ReadableType.Boolean;
        } else if (object instanceof Double ||
                object instanceof Float ||
                object instanceof Integer) {
            return ReadableType.Number;
        } else if (object instanceof String) {
            return ReadableType.String;
        } else if (object instanceof ReadableArray) {
            return ReadableType.Array;
        } else if (object instanceof ReadableMap) {
            return ReadableType.Map;
        }
        return null;
    }

    @Override
    public void pushBoolean(boolean value) {
        mBackingList.add(value);
    }

    @Override
    public void pushDouble(double value) {
        mBackingList.add(value);
    }

    @Override
    public void pushInt(int value) {
        mBackingList.add(value);
    }

    @Override
    public void pushString(String value) {
        mBackingList.add(value);
    }

    @Override
    public void pushArray(WritableArray array) {
        mBackingList.add(array);
    }

    @Override
    public void pushMap(WritableMap map) {
        mBackingList.add(map);
    }

    @Override
    public void pushNull() {
        mBackingList.add(null);
    }

    @Override
    public String toString() {
        return mBackingList.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JavaOnlyArray that = (JavaOnlyArray) o;

        if (mBackingList != null ? !mBackingList.equals(that.mBackingList) : that.mBackingList != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return mBackingList != null ? mBackingList.hashCode() : 0;
    }
}