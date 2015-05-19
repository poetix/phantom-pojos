package com.codepoetics.phantompojo.impl;

import java.lang.reflect.Method;
import java.util.Map;

public final class MethodIndexLookup {

    public static MethodIndexLookup create(Map<Method, Integer> readIndices, Map<Method, Integer> writeIndices) {
        return new MethodIndexLookup(readIndices, writeIndices);
    }

    private final Map<Method, Integer> readIndices;
    private final Map<Method, Integer> writeIndices;

    private MethodIndexLookup(Map<Method, Integer> readIndices, Map<Method, Integer> writeIndices) {
        this.readIndices = readIndices;
        this.writeIndices = writeIndices;
    }

    public int getReadIndex(Method method) {
        return readIndices.get(method);
    }

    public int getWriteIndex(Method method) {
        return writeIndices.get(method);
    }
}
