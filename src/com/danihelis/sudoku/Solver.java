package com.danihelis.sudoku;

import java.io.*;
import java.util.*;

public class Solver {

    enum Result {
        SOLUTION_FOUND,
        STEP_FOUND,
        STEP_NOT_FOUND,
        SOLUTION_NOT_FOUND;
    }

    Board board;
    int[] randomized;
    int techniques;
    int guesses;
    int solutions;
    PrintStream debug;

    public Solver(Board board) {
        this(board, null);
    }

    public Solver(Board board, PrintStream debug) {
        this.board = board;
        this.debug = debug;
        board.resetSolution();
        board.difficulty = null;
        techniques = 0;
        guesses = 0;
        solutions = 0;
    }

    boolean hasUniqueSolution() {
        return solutions == 1;
    }

    boolean solveAndEvaluate(boolean mayGuess) {
        boolean result = solve(mayGuess, true);
        board.boring = board.difficulty == Difficulty.NORMAL && techniques < 5;
        return hasUniqueSolution();
    }

    boolean solve(boolean mayGuess, boolean checkUniqueness) {
        Result result;
        do {
            result = solveOneStep();
        } while (result == Result.STEP_FOUND);
        if (result == Result.SOLUTION_FOUND) {
            if (solutions == 0) {
                board.difficulty = guesses > 0 ? Difficulty.HARD
                        : techniques > 0 ? Difficulty.NORMAL : Difficulty.EASY;
            }
            solutions++;
            return true;
        }
        if (!mayGuess) return false;
        if (++guesses > 1000) throw new Error("Taking too long to solve!");

        int position = -1;
        int less = board.dimension + 1;
        for (int i = 0; i < board.positions; i++) {
            int pos = randomized == null ? i : randomized[i];
            if (board.candidate[pos] == 0 && board.solution[pos] == 0) {
                return false;
            } else if (board.candidate[pos] != 0) {
                if (board.possible[pos] < less) {
                    position = pos;
                    less = board.possible[pos];
                }
            }
        }
        Board backup = new Board(board);
        int mask = board.candidate[position];
        for (int i = 0, bit = 1; i < board.dimension; i++, bit <<= 1) {
            if ((mask & bit) != 0) {
                board.markBit(position, bit);
                boolean solved = solve(true, checkUniqueness);
                if (solved && (!checkUniqueness || solutions > 1)) {
                    return true;
                }
                board.restore(backup);
            }
        }
        return false;
    }

    Result solveOneStep() {
        if (board.isSolved()) return Result.SOLUTION_FOUND;
        if (markSingleOnCell() || markSingleOnRegion()) {
            return Result.STEP_FOUND;
        }
        if (guesses == 0) techniques++;
        if (board.type == Type.DIAGONAL
                && checkPointingPair(Location.DIAGONAL)) {
            return Result.STEP_FOUND;
        } else if (board.type == Type.IRREGULAR
                && checkPointingPair(Location.GROUP)) {
            return Result.STEP_FOUND;
        } else if (checkNakedPairs() || checkGridReduction()
                || checkHiddenPairs()) {
            return Result.STEP_FOUND;
        /*
        } else if (board.type == Type.IRREGULAR && checkDoubleReduction()) {
            return Result.STEP_FOUND;
        */
        }
        return Result.STEP_NOT_FOUND;
    }

    boolean markSingleOnCell() {
        boolean change = false;
        for (int pos = 0; pos < board.positions; pos++) {
            if (board.possible[pos] == 1) {
                board.mark(pos);
                if (debug != null) {
                    debug.printf("Marking single found on cell %s: %d\n",
                            board.intoLocation(pos), board.solution[pos]);
                }
                change = true;
            }
        }
        return change;
    }

    boolean markSingleOnRegion() {
        int[] positions = new int[board.dimension];
        for (int region = 0; region < board.regions; region++) {
            for (int rank = 0; rank < board.ranks(region); rank++) {
                int once = 0;
                int many = 0;
                for (int index = 0; index < board.dimension; index++) {
                    positions[index] = board.intoPosition(region, rank, index);
                    int mask = board.candidate[positions[index]];
                    many |= mask & once;
                    once |= mask;
                }
                int unique = once & ~many;
                if (unique == 0) continue;
                for (int pos: positions) {
                    int mask = unique & board.candidate[pos];
                    if (mask == 0) continue;
                    board.markBit(pos, mask);
                    if (debug != null) {
                        debug.printf("Marking single found on region %d:%d at %s: %d\n",
                                region, rank, board.intoLocation(pos),
                                board.solution[pos]);
                    }
                }
                return true;
            }
        }
        return false;
    }

    boolean checkNakedPairs() {
        int[] positions = new int[board.dimension];
        int[] mask = new int[board.dimension];
        for (int region = 0; region < board.regions; region++) {
            for (int rank = 0; rank < board.ranks(region); rank++) {
                int total = 0;
                for (int index = 0; index < board.dimension; index++) {
                    positions[index] = board.intoPosition(region, rank, index);
                    if (board.possible[positions[index]] == 2) {
                        mask[total++] = board.candidate[positions[index]];
                    }
                }
                for (int i = 0; i < total - 1; i++) {
                    for (int j = i + 1; j < total; j++) {
                        if (mask[i] != mask[j]) continue;
                        boolean modified = false;
                        for (int k = 0; k < board.dimension; k++) {
                            int cand = board.candidate[positions[k]];
                            if (cand != mask[i] && (cand & mask[i]) != 0) {
                                board.removeCandidates(positions[k], mask[i]);
                                modified = true;
                            }
                        }
                        if (!modified) continue;
                        if (debug != null) {
                            debug.printf("Checking nacked pairs found on region %d:%d\n",
                                    region, rank);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    boolean checkGridReduction() {
        return checkGridReduction(Location.GROUP, Location.ROW) ||
               checkGridReduction(Location.GROUP, Location.COLUMN) ||
               checkGridReduction(Location.ROW, Location.GROUP) ||
               checkGridReduction(Location.COLUMN, Location.GROUP) ||
               (board.type == Type.DIAGONAL &&
                    checkGridReduction(Location.DIAGONAL, Location.GROUP));
    }

    int[] getRanks(int[] positions, int region, int expected) {
        int[] ranks = new int[board.ranks(region)];
        int total = 0;
        for (int i = 0; total <= expected && i < positions.length; i++) {
            var loc = board.intoLocation(region, positions[i]);
            boolean different = true;
            for (int k = 0; different && k < total; k++) {
                different = ranks[k] != loc.rank;
            }
            if (different) ranks[total++] = loc.rank;
        }
        return total == expected ? Arrays.copyOf(ranks, total) : null;
    }

    boolean checkGridReduction(int region, int area) {
        for (int rank = 0; rank < board.ranks(region); rank++) {
            for (int i = 0, bit = 1; i < board.dimension; i++, bit <<= 1) {
                int ranks[] = getRanks(board.getPositionsWithCandidate(region,
                        rank, bit), area, 1);
                if (ranks == null) continue;
                int areaRank = ranks[0];
                boolean modified = false;
                for (int index = 0; index < board.dimension; index++) {
                    int pos = board.intoPosition(area, areaRank, index);
                    var loc = board.intoLocation(region, pos);
                    if (loc != null && (loc.rank == rank ||
                            (region == Location.DIAGONAL &&
                                loc.rank == Location.BOTH_DIAGONALS))) {
                        continue;
                    }
                    if ((board.candidate[pos] & bit) != 0) {
                        board.removeCandidates(pos, bit);
                        modified = true;
                    }
                }
                if (!modified) continue;
                if (debug != null) {
                    debug.printf("Checking Grid reduction from region %d:%d to region %d:%d for value: %d\n",
                        region, rank, area, areaRank, Board.intoValue(bit));
                }
                return true;
            }
        }
        return false;
    }

    boolean checkHiddenPairs() {
        int[][] positions = new int[board.dimension][];
        int[] hidden = new int[board.dimension];
        for (int region = 0; region < board.regions; region++) {
            for (int rank = 0; rank < board.ranks(region); rank++) {
                int total = 0;
                for (int i = 0, bit = 1; i < board.dimension; i++, bit <<= 1) {
                    positions[i] = board.getPositionsWithCandidate(region,
                            rank, bit);
                    if (positions[i].length == 2) hidden[total++] = i;
                }
                for (int i = 0; i < total - 1; i++) {
                    for (int j = i + 1; j < total; j++) {
                        boolean equal = true;
                        for (int k = 0; equal && k < 2; k++) {
                            equal = positions[hidden[i]][k]
                                   == positions[hidden[j]][k];
                        }
                        if (!equal) continue;
                        int mask = (1 << hidden[i]) | (1 << hidden[j]);
                        boolean modified = false;
                        for (int k = 0; k < 2; k++) {
                            int pos = positions[hidden[i]][k];
                            int cand = board.candidate[pos];
                            if (cand == mask) continue;
                            board.removeCandidates(pos,
                                    board.candidate[pos] & ~mask);
                            modified = true;
                        }
                        if (!modified) continue;
                        if (debug != null) {
                            debug.printf("Checking hidden pairs found on region %d:%d for (%d,%d)\n",
                                region, rank, hidden[i] + 1, hidden[j] + 1);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    boolean checkPointingPair(int region) {
        for (int rank = 0; rank < board.ranks(region); rank++) {
            for (int i = 0, bit = 1; i < board.dimension; i++, bit <<= 1) {
                int[] positions = board.getPositionsWithCandidate(region,
                        rank, bit);
                if (positions.length != 2) continue;
                int row[] = new int[2];
                int col[] = new int[2];
                for (int k = 0; k < 2; k++) {
                    var loc = board.intoLocation(positions[k]);
                    row[k] = loc.rank;
                    col[k] = loc.index;
                }
                if (row[0] == row[1] || col[0] == col[1]) continue;
                boolean modified = false;
                for (int k = 0; k < 2; k++) {
                    int pos = board.intoPosition(row[k], col[1 - k]);
                    if ((board.candidate[pos] & bit) != 0) {
                        board.removeCandidates(pos, bit);
                        modified = true;
                    }
                }
                if (!modified) continue;
                if (debug != null) {
                    debug.printf("Checking pointing pair on %d:%d for value: %d\n",
                            region, rank, Board.intoValue(bit));
                }
                return true;
            }
        }
        return false;
    }

    /*
    boolean checkDoubleReduction() {
        return checkDoubleReduction(Location.GROUP, Location.ROW) ||
               checkDoubleReduction(Location.GROUP, Location.COLUMN) ||
               checkDoubleReduction(Location.ROW, Location.GROUP) ||
               checkDoubleReduction(Location.COLUMN, Location.GROUP);
    }

    boolean checkDoubleReduction(Location region, Location area) {
        for (int regionIndex = 0; regionIndex < region.length; regionIndex++)
            for (int i = 0, bit = 1; i < Board.DIM; i++, bit <<= 1) {
                int indices[] = getAreaIndices(
                        board.getPositionsForBit(region, regionIndex, bit),
                        area, 2);
                for (int otherLocation = regionIndex + 1; indices != null &&
                            otherLocation < region.length; otherLocation++) {
                    int otherIdx[] = getAreaIndices(
                            board.getPositionsForBit(region, otherLocation,
                            bit), area, 2);
                    if (otherIdx != null && indices[0] == otherIdx[0] &&
                            indices[1] == otherIdx[1]) {
                        boolean modified = false;
                        for (int k = 0; k < 2; k++) {
                            int areaIndex = indices[k];
                            for (int index = 0; index < Board.DIM; index++) {
                                int pos = board.getPositionFromLocation(area,
                                        areaIndex, index);
                                int ri = board.getLocationFromPosition(region,
                                        pos);
                                if ((board.candidate[pos] & bit) != 0 &&
                                        ri != regionIndex &&
                                        ri != otherLocation) {
                                    board.removeCandidates(pos, bit);
                                    modified = true;
                                }
                            }
                        }
                        if (modified) {
                            if (LOG)
                                System.out.printf("Checking double reduction from regions %s:%d,%d to region %s:%d,%d for value: %d\n",
                                        region, regionIndex, otherLocation,
                                        area, indices[0], indices[1], Board.intoValue(bit));
                            return true;
                        }
                    }
                }
            }
        return false;
    }
    */

    void test(boolean interactive) {
        var stream = System.out;
        if (interactive) {
            var console = System.console();
            if (console == null) throw new Error("Cannot run interactively");
            stream.printf("Trying to solve this puzzle already annotated...\n");
            stream.printf("Type: %s\n\n", board.type);
            do {
                board.print(stream, true);
                console.readLine();
                stream.printf("\n");
            } while (solveOneStep() == Result.STEP_FOUND);
            stream.printf("No more steps found! Should try guessing now...");
            console.readLine();
        }
        board.print(stream, true);
        long time = System.currentTimeMillis();
        boolean solved = solve(true, true);
        long ellapsed = System.currentTimeMillis() - time;
        board.print(stream, true);
        stream.println();
        stream.printf("""
                SOLVED: %b
                UNIQUE: %b
                GUESSES: %d
                TIME TO FINISH SOLVING: %d.%03d
                """, solutions > 0, solutions == 1, guesses, ellapsed / 1000,
                ellapsed % 1000);
    }
}
