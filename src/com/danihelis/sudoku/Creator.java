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
import java.util.stream.*;

public class Creator {

    static final int ATTEMPTS_BEFORE_NEW_PUZZLE = 30;
    static final int MAXIMUM_ATTEMPTS = 300;

    Board puzzle;
    Board initial;
    int totalAttempts;

    public Board create(Type type) {
        return create(type, null, null);
    }

    public Board create(Type type, Difficulty difficulty) {
        return create(type, difficulty, null);
    }

    public Board create(Type type, Difficulty difficulty, Symmetry symmetry) {
        return create(type, difficulty, symmetry, null);
    }

    public Board create(Type type, Difficulty difficulty, Symmetry symmetry,
            Symmetry layout) {
        totalAttempts = 0;
        while (totalAttempts < MAXIMUM_ATTEMPTS) {
            initial = new Board(3, 3, type, layout);
            var solver = new Solver(initial);
            solver.randomized = initial.createPositionArray(true);
            try {
                solver.solve(true, false);
            } catch (Solver.TooManyGuesses t) {
                continue;
            }
            initial.given = Arrays.copyOf(initial.solution,
                    initial.solution.length);
            initial.difficulty = Difficulty.EASY;
            initial.symmetry = symmetry == Symmetry.RANDOM
                    ? Symmetry.randomize() : symmetry;

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
                    for (var p: board.getSymmetricPositions(pos)) {
                        board.given[p] = 0;
                    }
                    solver = new Solver(board);
                    var solved = false;
                    try {
                        solved = solver.solveAndEvaluate(
                                difficulty != Difficulty.EASY &&
                                difficulty != Difficulty.NORMAL);
                    } catch (Solver.TooManyGuesses t) {
                    }
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
