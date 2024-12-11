package com.danihelis.sudoku;

import java.util.*;

public enum Symmetry {
    NONE("None"),
    RANDOM("Random"),
    ROTATION("Rotation"),
    MIRROR("Mirror"),
    FLIP("Flip"),
    TRANSPOSE("Transpose"),
    DOUBLE_MIRROR("Double mirror"),
    DOUBLE_ROTATION("Double rotation");

    String name;

    Symmetry(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    private static Symmetry randomize(Symmetry[] values) {
        int index = 2 + (int) (Math.random() * (values.length - 2));
        return values[index];
    }

    public static Symmetry randomize() {
        return randomize(values());
    }

    public static Symmetry[] valuesForLayout() {
        return new Symmetry[] {NONE, RANDOM, ROTATION, MIRROR, FLIP, TRANSPOSE};
    }

    public static Symmetry randomizeForLayout() {
        return randomize(valuesForLayout());
    }
}
