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
            // ".4.37...89....6.3.6....94.2.7..9...1..34.2.6....81.....2.1.8..6.......1.........9";
            // ".......1.......2...3....4.5.......................6.......7....6.2....8....34....";
            // "004000900057010204000907000000080000100090002000020000000803000400070628000000000";
            // "..:..:..|..:..:..|..:..:..|..:..:..|..:..:..|..:..:..|..:..:..|..:..:..|..:..:..|";
               "6..2845..8...6124..2..9368.1..647.9..6.938....9.152.642854.6.3..4.8....6..632.4.8";

            String cells =
            // "..:..:..|..:..:..|..:..:..|..:..:..|..:..:..|..:..:..|..:..:..|..:..:..|..:..:..|";
               ".x.............................xx..x..x...x..x..xx.............................x.";

            // Board board = Board.parse(Type.ODD_EVEN, puzzle, cells);
            Board board = Board.parse(Type.CLASSIC, puzzle);
            board.print(System.out, false);
            /*

            long time = System.currentTimeMillis();
            Solver solver = new Solver(board);
            solver.testSolve(true);
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
