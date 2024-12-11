package com.danihelis.sudoku;

import java.util.*;

public enum Type {
    CLASSIC("Classic"),
    DIAGONAL("Diagonal"),
    IRREGULAR("Irregular");

    String name;

    Type(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
