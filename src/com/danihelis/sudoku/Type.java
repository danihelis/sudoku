package com.danihelis.sudoku;

import java.util.*;

public enum Type {
    CLASSIC("CLASSIC"),
    DIAGONAL("DIAGONAL"),
    IRREGULAR("IRREGULAR"),
    ODD_EVEN("ODD-EVEN");

    String name;

    Type(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
