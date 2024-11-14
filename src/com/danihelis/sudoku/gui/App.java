package com.danihelis.sudoku.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;

import com.danihelis.sudoku.*;

public class App extends JPanel /*implements ActionListener*/ {

    static final int NUMBER_PUZZLES = 6;
    static final int BOARD_WIDTH = 500;
    static final int BOARD_HEIGHT = 500;

    class Window extends JFrame {

        Window() {
            super("Danihelis's Sudoku");
            getContentPane().add(App.this);
            pack();
            setMinimumSize(new Dimension(getWidth(), getHeight()));
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            // setResizable(false);
            setVisible(true);
        }
    }

    class Selector<T> {

        JComboBox<T> combobox;
        JLabel label;
        JButton button;

        Selector(String label, Collection<T> data) {
            combobox = new JComboBox<>(new Vector<T>(data));
            this.label = new JLabel(label);
            var icon = Utils.readIcon("res/shuffle.png", 16, 16);
            button = new JButton(icon);
        }

        GroupLayout.Group getHorizontalGroup(GroupLayout layout) {
            return layout.createParallelGroup()
                .addComponent(label)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(combobox));
                    //.addComponent(button));
        }

        GroupLayout.Group getVerticalGroup(GroupLayout layout) {
            return layout.createSequentialGroup()
                .addComponent(label)
                .addGroup(layout.createParallelGroup()
                    .addComponent(combobox));
                    // .addComponent(button));
        }
    }

    class Puzzle extends JPanel {

        Selector<Type> type;
        Selector<Difficulty> difficulty;
        Selector<Symmetry> symmetry;
        Selector<Symmetry> board;
        BoardPanel panel;
        JButton create;
        JButton randomize;

        Puzzle() {
            type = new Selector<>("Sudoku type",
                    Arrays.asList(Type.values()));
            difficulty = new Selector<>("Difficulty",
                    Arrays.asList(Difficulty.values()));
            symmetry = new Selector<>("Puzzle symmetry",
                    Arrays.asList(Symmetry.values()));
            board = new Selector<>("Board symmetry",
                    Arrays.asList(Symmetry.values()));
            create = new JButton("Create",
                    Utils.readIcon("res/puzzle.png", 40, 40));
            randomize = new JButton("Randomize",
                    Utils.readIcon("res/shuffle.png", 16, 16));
            panel = new BoardPanel(BOARD_WIDTH, BOARD_HEIGHT);
            // panel.setBorder(BorderFactory.createLineBorder(new Color(0x7a8a99)));
            create.setVerticalTextPosition(SwingConstants.BOTTOM);
            create.setHorizontalTextPosition(SwingConstants.CENTER);

            var layout = new GroupLayout(this);
            setLayout(layout);
            layout.setAutoCreateGaps(true);
            layout.setAutoCreateContainerGaps(true);
            layout.setHorizontalGroup(layout.createParallelGroup()
                .addGroup(GroupLayout.Alignment.CENTER, layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup()
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(type.getHorizontalGroup(layout))
                            .addGroup(difficulty.getHorizontalGroup(layout)))
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(symmetry.getHorizontalGroup(layout))
                            .addGroup(board.getHorizontalGroup(layout))))
                    .addGap(20)
                    .addComponent(create, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE))
                .addComponent(panel, BOARD_WIDTH, BOARD_WIDTH, Short.MAX_VALUE));
            layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup()
                            .addGroup(type.getVerticalGroup(layout))
                            .addGroup(difficulty.getVerticalGroup(layout)))
                        .addGroup(layout.createParallelGroup()
                            .addGroup(symmetry.getVerticalGroup(layout))
                            .addGroup(board.getVerticalGroup(layout))))
                    .addGroup(GroupLayout.Alignment.CENTER, layout.createSequentialGroup()
                        // .addComponent(randomize)
                        .addComponent(create, 0, 80, 80)))
                .addComponent(panel, BOARD_HEIGHT, BOARD_HEIGHT, Short.MAX_VALUE));
            /*
            layout.linkSize(SwingConstants.VERTICAL, type.button, type.combobox,
                    difficulty.combobox, difficulty.button,
                    symmetry.combobox, symmetry.button, board.combobox,
                    board.button);
            */
            layout.linkSize(SwingConstants.HORIZONTAL, type.combobox,
                    difficulty.combobox, symmetry.combobox, board.combobox);
            // layout.linkSize(create, randomize);
        }
    }

    static final int WIDTH = 800;
    static final int HEIGHT = 600;

    JButton randomize;
    JButton generate;
    JButton print;
    JTabbedPane tab;
    Vector<Puzzle> puzzles;

    public App() {
        randomize = new JButton("Randomize All",
                Utils.readIcon("res/shuffle.png", 16, 16));
        generate = new JButton("Create Puzzles",
                Utils.readIcon("res/gears.png", 16, 16));
        // generate.addActionListener(this);
        print = new JButton("Print All Puzzles",
                Utils.readIcon("res/print.png", 16, 16));
        // print.addActionListener(this);
        var buttons = new JPanel();
        var layout = new GroupLayout(buttons);
        setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        layout.setHorizontalGroup(layout.createSequentialGroup()
            .addComponent(randomize)
            .addComponent(generate)
            .addComponent(print));
        layout.setVerticalGroup(layout.createParallelGroup()
            .addComponent(randomize)
            .addComponent(generate)
            .addComponent(print));
        layout.linkSize(randomize, generate, print);

        tab = new JTabbedPane();
        puzzles = new Vector<>();
        for (int i = 0; i < NUMBER_PUZZLES; i++) {
            var puzzle = new Puzzle();
            puzzles.add(puzzle);
            tab.add("Sudoku %d".formatted(i + 1), puzzle);
        }

        layout = new GroupLayout(this);
        setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setHorizontalGroup(layout.createParallelGroup()
            .addComponent(buttons)
            .addComponent(tab));
        layout.setVerticalGroup(layout.createSequentialGroup()
            .addComponent(buttons, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
            .addComponent(tab, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
    }

    public void start() {
        new Window();
    }
}
