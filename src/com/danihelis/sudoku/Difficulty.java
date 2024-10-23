package com.danihelis.sudoku;

import java.util.*;

public enum Difficulty implements Comparable<Difficulty> {
    EASY(0),
    NORMAL(1),
    HARD(2),
    EXTREME(3),
    UNKNOWN(4);

    int level;

    Difficulty(int level) {
        this.level = level;
    }

    public boolean harderThan(Difficulty difficulty) {
        if (this.level == UNKNOWN.level || difficulty.level == UNKNOWN.level) {
            return false;
        }
        return this.level > difficulty.level;
    }
}
