package com.danihelis.sudoku;

import java.util.*;
import java.util.stream.*;

public class Creator {

    static final int ATTEMPTS_BEFORE_NEW_PUZZLE = 30;
    static final int MAXIMUM_ATTEMPTS = 300;

    Board puzzle;
    Board initial;
    int totalAttempts;

    Board create(Type type) {
        return create(type, null, null);
    }

    Board create(Type type, Difficulty difficulty) {
        return create(type, difficulty, null);
    }

    Board create(Type type, Difficulty difficulty, Symmetry symmetry) {
        totalAttempts = 0;
        while (totalAttempts < MAXIMUM_ATTEMPTS) {
            initial = new Board(type);
            var solver = new Solver(initial);
            solver.randomized = initial.createPositionArray(true);
            solver.solve(true, false);
            initial.given = Arrays.copyOf(initial.solution, initial.solution.length);
            initial.difficulty = Difficulty.EASY;
            initial.symmetry = symmetry;

            boolean created = false;
            int attempts = 0;
            while (!created && attempts < ATTEMPTS_BEFORE_NEW_PUZZLE) {
                int[] positions = initial.createPositionArray(true);
                var board = new Board(initial);
                puzzle = new Board(board);
                attempts++;
                totalAttempts++;

                for (var pos: positions) {
                    if (board.given[pos] == 0) continue;
                    board.given[pos] = 0;
                    if (symmetry != null) {
                        for (var p: board.getSymmetricPositions(pos, symmetry)) {
                            board.given[p] = 0;
                        }
                    }
                    solver = new Solver(board);
                    var solved = solver.solveAndEvaluate(
                            difficulty != Difficulty.EASY &&
                            difficulty != Difficulty.NORMAL);
                    /*
                            puzzle.difficulty == Difficulty.UNKNOWN)
                        System.out.printf("BUG! G=%d T=%d S=%d\n",
                                solver.guesses,
                                solver.technique,
                                solver.solutions);
                    if (solver.evaluation == Difficulty.HARD)
                        System.out.printf("HARD: %d! P=%d -- %s\n",
                                solver.solutions,
                                puzzle.hashCode(),
                                puzzle.difficulty);
                    */
                    if (solved && solver.hasUniqueSolution() && (
                                difficulty == null ||
                                board.difficulty.level <= difficulty.level)) {
                        created = difficulty == null ||
                                board.difficulty.level == difficulty.level;
                        puzzle = new Board(board); // store board
                    } else {
                        board = new Board(puzzle); // restore previous
                    }
                }
            }
            if (created) return puzzle;
        }
        throw new Error("Could not create puzzle");
    }
}
