package com.danihelis.sudoku;

import javax.swing.*;
import javax.swing.UIManager.*;

public class Main {

    public static void main(String... args) {
        try {
            /*/ Layout.Model model = Layout.Model.MONOLOTUS;
            long time = System.currentTimeMillis();
            Layout layout = Layout.createRandomLayout();
            System.out.printf("Random layout:\n");
            long ellapsed = System.currentTimeMillis() - time;
            layout.print();
            System.out.printf("TIME: %d.%03ds\n",
                    ellapsed / 1000, ellapsed % 1000);

            /*/

            String puzzle =
            // ".5.6.2.8.1.3...6.4.6..7..5...59.62......4......83.19...2..3..9.9.4...5.7.8.5.7.1."; // very easy
            // ".98.....46..72.8..7..6...3..85..3....6.....4....9..78..2...7..8..1.59..74.....21."; // easy
            // ".......29..1...863.3..87......49...5..6...1..8...25......91..5.479...6..31......."; // medium
            // "..7...2......3....2..695..7..5...7...94.8.52...8...3..4..917..6....5......3...1.."; // medium plus
            // "..9.4.6..27.....498.......1...9.8...3.......5...3.7...1.......443.....78..2.1.5.."; // hard
            // "5.2.9.1.....1...8.3....6..2.4....7..6.......1..5....9.9..7....4.6...3.....7.2.5.3"; // very hard
             ".4.37...89....6.3.6....94.2.7..9...1..34.2.6....81.....2.1.8..6.......1.........9"; // extreme
            // ".......1.......2...3....4.5.......................6.......7....6.2....8....34...."; // multiple
            // "..4...9...57.1.2.4...9.7.......8....1...9...2....2.......8.3...4...7.628.........";
            // "6..2845..8...6124..2..9368.1..647.9..6.938....9.152.642854.6.3..4.8....6..632.4.8";

            String cells =
            // "..:..:..|..:..:..|..:..:..|..:..:..|..:..:..|..:..:..|..:..:..|..:..:..|..:..:..|";
               ".x.............................xx..x..x...x..x..xx.............................x.";

            // Board board = Board.parse(Type.ODD_EVEN, puzzle, cells);
            Board board = Board.parse(Type.CLASSIC, puzzle);
            // board.print(System.out, false);
            Solver solver = new Solver(board, System.out);
            solver.test(true);
            /*

            long time = System.currentTimeMillis();
            solver.solve2(true, false);
            long ellapsed = System.currentTimeMillis() - time;
            board.print(true);
            System.out.printf("TIME: %d.%03ds\n" +
                    "DIFFICULTY: %s\nSYMMETRY: %s\nTECHNIQUES: %d\n\n",
                    ellapsed / 1000, ellapsed % 1000,
                    board.difficulty.toString(),
                    board.symmetry.toString(),
                    solver.technique);

            /* /
            Generator gen = new Generator();

            System.out.printf("Creating new puzzle....\n");

            long time = System.currentTimeMillis();
            Board puzzle = gen.createPuzzle(Type.ODD_EVEN, Difficulty.EXTREME, Symmetry.RANDOM);
            long ellapsed = System.currentTimeMillis() - time;

            // Layout.createRandomParityCells(puzzle);
            puzzle.print(false);

            Solver solver = new Solver(puzzle);
            solver.testSolve(true);
            solver.solve2(true, false);
            System.out.printf("TIME: %d.%03ds\nATTEMPTS: %d\n" +
                    "DIFFICULTY: %s\nSYMMETRY: %s\nTECHNIQUES: %d\n\n",
                    ellapsed / 1000, ellapsed % 1000,
                    gen.totalAttempts,
                    puzzle.difficulty.toString(),
                    puzzle.symmetry.toString(),
                    solver.technique);
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

            /* /
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            new Window();
            //*/
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
