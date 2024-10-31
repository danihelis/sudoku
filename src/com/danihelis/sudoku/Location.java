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
}
