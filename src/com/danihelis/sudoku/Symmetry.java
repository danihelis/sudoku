package com.danihelis.sudoku;

import java.util.*;

public enum Symmetry {
    ROTATION,
    MIRROR,
    FLIP,
    TRANSPOSE,
    DOUBLE_MIRROR,
    DOUBLE_ROTATION,
    NONE,
    RANDOM;

    public static Symmetry random() {
        int index = (int) (Math.random() * (values().length - 2));
        return values()[index];
    }

    public static Symmetry randomIrregularLayout() {
        int index = (int) (Math.random() * 3);
        return values()[index];
    }

    public static Symmetry randomOddityLayout() {
        int index = (int) (Math.random() * 5);
        return values()[index];
    }
}
