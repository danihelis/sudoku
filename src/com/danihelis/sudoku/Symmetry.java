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

    private static Symmetry randomize(int length) {
        int index = 2 + (int) (Math.random() * length);
        return values()[index];
    }

    public static Symmetry randomize() {
        return randomize(values().length - 2);
    }

    public static Symmetry randomizeForLayout() {
        return randomize(4);
    }
}
