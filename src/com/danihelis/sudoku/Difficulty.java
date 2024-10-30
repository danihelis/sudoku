package com.danihelis.sudoku;

import java.util.*;

public enum Difficulty {
    EASY(0),
    NORMAL(1),
    HARD(2);

    int level;

    Difficulty(int level) {
        this.level = level;
    }
}
