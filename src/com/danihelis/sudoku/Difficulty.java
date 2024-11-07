package com.danihelis.sudoku;

import java.util.*;

public enum Difficulty {
    EASY("Easy", 0),
    NORMAL("Normal", 1),
    HARD("Hard", 2);

    String name;
    int level;

    @Override
    public String toString() {
        return name;
    }

    Difficulty(String name, int level) {
        this.name = name;
        this.level = level;
    }
}
