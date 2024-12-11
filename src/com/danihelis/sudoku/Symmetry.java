/*
 * Danihelis's Sudoku
 * Copyright (C) 2024  Daniel Donadon
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
