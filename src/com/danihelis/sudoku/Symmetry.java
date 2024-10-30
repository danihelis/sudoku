package com.danihelis.sudoku;

import java.util.*;

public enum Symmetry {
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

    public static Symmetry random() {
        int index = (int) (Math.random() * (values().length - 2));
        return values()[index];
    }
}
