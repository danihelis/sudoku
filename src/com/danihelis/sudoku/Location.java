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

class Location {

    static final int ROW = 0;
    static final int COLUMN = 1;
    static final int GROUP = 2;
    static final int DIAGONAL = 3;

    static final int NO_DIAGONAL = -1;
    static final int DIAGONAL_1 = 0;
    static final int DIAGONAL_2 = 1;
    static final int BOTH_DIAGONALS = 2;

    int rank;
    int index;

    Location(int rank, int index) {
        this.rank = rank;
        this.index = index;
    }

    @Override
    public int hashCode() {
        return rank << 6 + index;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Location l) {
            return rank == l.rank && index == l.index;
        }
        return false;
    }

    @Override
    public String toString() {
        return "(r=%d,i=%d)".formatted(rank, index);
    }

    boolean isValid(Board board) {
        return rank >= 0 && rank < board.dimension && index >= 0
                && index < board.dimension;
    }
}
