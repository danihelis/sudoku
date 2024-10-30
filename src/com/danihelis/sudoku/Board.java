package com.danihelis.sudoku;

import java.io.*;
import java.util.*;

public class Board {

    int rows;               // number of rows in the inner box
    int columns;            // number of columns in the inner box
    int dimension;          // number of different values in a box
    int positions;          // number of values in the whole board
    int regions;            // number of regions {ROW, COLUMN, GROUP, DIAGONAL?}
    int given[];            // value of a given in position (0 if none)
    int solution[];         // solution in position (0 if unknown)
    int candidate[];        // bit mask with candidates in position
    int possible[];         // number of possible candidates in position

    Symmetry symmetry;
    Difficulty difficulty;
    Type type;
    Layout layout;

    Board(Board board) {
        restore(board);
    }

    Board(Type type) {
        this(3, 3, type);
    }

    Board(int rows, int columns, Type type) {
        this.rows = rows;
        this.columns = columns;
        this.type = type;
        dimension = rows * columns;
        positions = dimension * dimension;
        given = new int[positions];
        solution = new int[positions];
        candidate = new int[positions];
        possible = new int[positions];
        regions = 3; // TODO 4 if DIAGONAL
        difficulty = null;
        symmetry = null;
        layout = Layout.createRegularLayout(this);
    }

    int intoPosition(Location location) {
        return intoPosition(Location.ROW, location);
    }

    int intoPosition(int row, int column) {
        return intoPosition(Location.ROW, row, column);
    }

    int intoPosition(int region, Location location) {
        return intoPosition(region, location.rank, location.index);
    }

    int intoPosition(int region, int rank, int index) {
        return switch (region) {
            case Location.ROW -> rank * dimension + index;
            case Location.COLUMN -> index * dimension + rank;
            case Location.GROUP -> layout.position[rank][index];
            case Location.DIAGONAL -> (rank == Location.DIAGONAL_1 ? index
                    : dimension - index - 1) * dimension + index;
            default -> throw new Error("Invalid region: " + region);
        };
    }

    Location intoLocation(int position) {
        return intoLocation(Location.ROW, position);
    }

    Location intoLocation(int region, int position) {
        if (region == Location.DIAGONAL) {
            var coord = intoLocation(position);
            var main = coord.rank == coord.index;
            var second = coord.rank == dimension - coord.index - 1;
            if (!main && !second) return null;
            return new Location(main && second ? Location.BOTH_DIAGONALS
                    : main ? Location.DIAGONAL_1 : Location.DIAGONAL_2,
                    coord.rank / dimension);
        }
        return switch (region) {
            case Location.ROW -> new Location(position / dimension,
                    position % dimension);
            case Location.COLUMN -> new Location(position % dimension,
                    position / dimension);
            case Location.GROUP -> layout.location[position];
            default -> throw new Error("Invalid region: " + region);
        };
    }

    void restore(Board board) {
        rows = board.rows;
        columns = board.columns;
        type = board.type;
        dimension = board.dimension;
        positions = board.positions;
        given = Arrays.copyOf(board.given, positions);
        solution = Arrays.copyOf(board.solution, positions);
        candidate = Arrays.copyOf(board.candidate, positions);
        possible = Arrays.copyOf(board.possible, positions);
        regions = board.regions;
        difficulty = board.difficulty;
        symmetry = board.symmetry;
        layout = board.layout;
    }

    void resetSolution() {
        Arrays.fill(solution, 0);
        Arrays.fill(candidate, (1 << dimension) - 1);
        Arrays.fill(possible, dimension);
        for (int pos = 0; pos < positions; pos++) {
            if (given[pos] != 0) markValue(pos, given[pos]);
        }
    }

    boolean isSolved() {
        for (int pos = 0; pos < positions; pos++) {
            if (solution[pos] == 0) return false;
        }
        return true;
    }

    void mark(int position) {
        markBit(position, candidate[position]);
    }

    void markValue(int position, int value) {
        solution[position] = value;
        updateCandidate(position, 1 << (value - 1));
    }

    void markBit(int position, int bit) {
        solution[position] = intoValue(bit);
        updateCandidate(position, bit);
    }

    void updateCandidate(int position, int bit) {
        for (int region = 0; region < regions; region++) {
            var loc = intoLocation(region, position);
            if (loc == null) continue;
            if (region == Location.DIAGONAL
                    && loc.rank == Location.BOTH_DIAGONALS) {
                for (int index = 0; index < dimension; index++) {
                    int pos = intoPosition(region, Location.DIAGONAL_1, index);
                    removeCandidates(pos, bit);
                }
                loc.rank = Location.DIAGONAL_2;
            }
            for (int index = 0; index < dimension; index++) {
                int pos = intoPosition(region, loc.rank, index);
                removeCandidates(pos, bit);
            }
        }
        candidate[position] = 0;
        possible[position] = 0;
    }

    void removeCandidates(int position, int mask) {
        int removed = mask & candidate[position];
        candidate[position] &= ~mask;
        for (; removed > 0; removed >>= 1) {
            if ((removed & 1) != 0) possible[position]--;
        }
    }

    static int intoValue(int mask) {
        int value = 0;
        while (mask > 0) {
            value++;
            mask >>= 1;
        }
        return value;
    }

    int[] getPositionsWithCandidate(int region, int rank, int bit) {
        int list[] = new int[dimension];
        int size = 0;
        for (int index = 0; index < dimension; index++) {
            int pos = intoPosition(region, rank, index);
            if ((candidate[pos] & bit) != 0) list[size++] = pos;
        }
        return Arrays.copyOf(list, size);
    }

    int getRank(int row, int col) {
        int pos = intoPosition(row % dimension, col % dimension);
        return layout.location[pos].rank;
    }

    void print(OutputStream stream, boolean showCandidates) {
        var output = new PrintStream(stream);
        int position = 0;
        for (int row = 0; row <= dimension; row++, position += dimension) {
            for (int col = 0; col <= dimension; col++) {
                boolean rowDivision = row == 0 || row == dimension ||
                        getRank(row, col) != getRank(row - 1, col);
                boolean colDivision = col == 0 || col == dimension ||
                        getRank(row, col) != getRank(row, col - 1);
                if (row == 0) {
                    output.print(col == 0 ? "┏" : col == dimension ? "┓"
                            : colDivision ? "┳" : "┯");
                } else if (row == dimension) {
                    output.print(col == 0 ? "┗": col == dimension ? "┛"
                            : colDivision ? "┻" : "┷");
                } else {
                    output.print(col == 0 ? (colDivision ? "┣": "┠")
                            : col == dimension ? (colDivision ? "┫" : "┨")
                            : colDivision && rowDivision ? "╋"
                            : colDivision ? "╂" : rowDivision ? "┿" : "┼");
                }
                if (col == dimension) break;
                for (int i = 0; i < 2 * columns + 1; i++) {
                    output.print(rowDivision ? "━" : i % 2 == 1 ? "─" : "─");
                }
            }
            output.println();
            if (row == dimension) break;
            for (int line = 0; line < rows; line++) {
                for (int col = 0; col <= dimension; col++) {
                    boolean div = col == 0 || col == dimension ||
                            getRank(row, col) != getRank(row, col -1);
                    System.out.printf(div ? "┃" : "│");
                    if (col == dimension) break;
                    var pos = intoPosition(row, col);
                    int value = showCandidates ? solution[pos] : given[pos];
                    for (int i = 0; i < columns; i++) {
                        output.printf(" ");
                        if (value != 0) {
                            output.printf(line == rows / 2 && i == columns / 2
                                    ? "%d".formatted(value) : " ");
                        } else {
                            int bit = 1 << (line * columns + i);
                            boolean unknown = (candidate[pos] & bit) == 0;
                            output.printf(unknown ? "." :
                                   "%d".formatted(line * columns + i + 1));
                        }
                    }
                    output.printf(" ");
                }
                output.println();
            }
        }
    }

    String export() {
        String output = "";
        for (int pos = 0; pos < positions; pos++) {
            output += given[pos] == 0 ? "." : given[pos];
        }
        return output;
    }

    static Board parse(Type type, String sequence) {
        var board = new Board(type);
        for (int pos = 0; pos < board.positions; pos++) {
            var given = sequence.charAt(pos);
            board.given[pos] = given >= '1' && given <= '9' ? given - '0' : 0;
        }
        return board;
    }

    /*
    static Board parse(Type type, String sequence, String cells) {
        Board board = parse(type, sequence);
        Vector<Integer> parityCells = new Vector<>();
        for (int index = 0; index < SIZE; index++)
            if (cells.charAt(index) == 'x')
                parityCells.add(index);
        board.setParityCells(parityCells);
        return board;
    }
    */
}
