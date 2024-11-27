package com.danihelis.sudoku;

import javax.swing.*;
import javax.swing.UIManager.*;

public class Main {

    public static void main(String... args) {
        try {
            /*/ Layout.Model model = Layout.Model.MONOLOTUS;
            long time = System.currentTimeMillis();
            var board = new Board(Type.IRREGULAR);
            long ellapsed = System.currentTimeMillis() - time;
            board.print(System.out, false);
            System.out.printf("TIME: %d.%03ds\n",
                    ellapsed / 1000, ellapsed % 1000);

            /* /

            String puzzle =
            // ".5.6.2.8.1.3...6.4.6..7..5...59.62......4......83.19...2..3..9.9.4...5.7.8.5.7.1."; // very easy
            // ".98.....46..72.8..7..6...3..85..3....6.....4....9..78..2...7..8..1.59..74.....21."; // easy
            // ".......29..1...863.3..87......49...5..6...1..8...25......91..5.479...6..31......."; // medium
            // "..7...2......3....2..695..7..5...7...94.8.52...8...3..4..917..6....5......3...1.."; // medium plus
            // "..9.4.6..27.....498.......1...9.8...3.......5...3.7...1.......443.....78..2.1.5.."; // hard
            // "5.2.9.1.....1...8.3....6..2.4....7..6.......1..5....9.9..7....4.6...3.....7.2.5.3"; // very hard
            // ".4.37...89....6.3.6....94.2.7..9...1..34.2.6....81.....2.1.8..6.......1.........9"; // *extreme*
            // ".......1.......2...3....4.5.......................6.......7....6.2....8....34...."; // [D] *normal*
            // ".2..95...........9...764...7.2.3.6..3.9...4.7..8.1.5.2...581...8...........27..4."; // [D] easy
            // "..3...6...........6..925..7..7...2....9...7....8...1..7..216..8...........5...9.."; // [D] medium
            // "..5..1.....9...3...7..6..422..........7...2..........469..5..1...1...5.....1..9.."; // [D] hard
               "..1.6..9.....52..89...............5.86.....41.4...............62..63.....8..1.7.."; // [D] very hard
            // "..4...9...57.1.2.4...9.7.......8....1...9...2....2.......8.3...4...7.628.........";
            // "6..2845..8...6124..2..9368.1..647.9..6.938....9.152.642854.6.3..4.8....6..632.4.8";

            // var board = Board.parse(Type.CLASSIC, puzzle);
            var board = Board.parse(Type.DIAGONAL, puzzle);
            // board.print(System.out, false);
            var solver = new Solver(board, System.out);
            try {
                solver.test(true);
            } catch (Error e) {
                System.out.printf("%s\n", e);
            }

            var time = System.currentTimeMillis();
            solver = new Solver(board);
            solver.solveAndEvaluate(true);
            var ellapsed = System.currentTimeMillis() - time;
            System.out.printf("\n");
            System.out.printf("""
                TIME TO SOLVE FROM START: %d.%03ds
                DIFFICULTY: %s
                SYMMETRY (if created): %s
                TECHNIQUES: %d
                """, ellapsed / 1000, ellapsed % 1000, board.difficulty,
                board.symmetry, solver.techniques);

            /* /

            System.out.printf("Creating new puzzle....\n");
            var creator = new Creator();
            var time = System.currentTimeMillis();
            Board puzzle = null;
            try {
                puzzle = creator.create(Type.CLASSIC, Difficulty.NORMAL,
                        Symmetry.ROTATION);
            } catch (Error e) {
                e.printStackTrace();
                System.out.printf("Attempts: %d", creator.totalAttempts);
                return;
            }
            var ellapsed = System.currentTimeMillis() - time;

            puzzle.print(System.out, false);
            var solver = new Solver(puzzle);
            System.out.printf("SOLVED: %b\n", solver.solve(true, false));
            System.out.printf("""
                    TYPE: %s
                    TIME: %.3fs
                    ATTEMPTS: %d
                    DIFFICULTY: %s
                    SYMMETRY: %s
                    TECHNIQUES: %d
                    """, puzzle.type, ellapsed / 1000f, creator.totalAttempts,
                        puzzle.difficulty, puzzle.symmetry, solver.techniques);
            System.out.println(puzzle.export());

            /* /

            Generator gen = new Generator();
            for (int i = 0; i < 1000; i++) {
                Board puzzle = gen.createPuzzle(Type.DIAGONAL, Difficulty.NORMAL,
                        Symmetry.RANDOM);
                Solver solver = new Solver(puzzle);
                solver.solve2(true, true);
                if (!solver.hasUniqueSolution())
                    throw new Error("NOT UNIQUE!!!");
                System.out.printf("%03d T=%s D=%s S=%s\n", i,
                    puzzle.type, puzzle.difficulty, puzzle.symmetry);
            }

            /*/

            com.danihelis.sudoku.gui.Utils.setUIFontSize(14f);
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    // UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            new com.danihelis.sudoku.gui.App().start();

            //*/
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
