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

        void setSelected(T object) {
            combobox.setSelectedItem(object);
        }

        void setEnabled(boolean enabled) {
            combobox.setEnabled(enabled);
            label.setEnabled(enabled);
        }

        T getValue() {
            return combobox.getItemAt(combobox.getSelectedIndex());
        }
    }

    class Puzzle extends JPanel implements ActionListener {

        Selector<Type> type;
        Selector<Difficulty> difficulty;
        Selector<Symmetry> symmetry;
        Selector<Symmetry> layout;
        BoardPanel board;
        JButton create;
        JButton randomize;

        Puzzle() {
            type = new Selector<>("Sudoku type",
                    Arrays.asList(Type.values()));
            difficulty = new Selector<>("Difficulty",
                    Arrays.asList(Difficulty.values()));
            difficulty.setSelected(Difficulty.NORMAL);
            symmetry = new Selector<>("Puzzle symmetry",
                    Arrays.asList(Symmetry.values()));
            symmetry.setSelected(Symmetry.RANDOM);
            layout = new Selector<>("Board symmetry",
                    Arrays.asList(Symmetry.values()));
            layout.setSelected(Symmetry.NONE);
            layout.setEnabled(false);
            create = new JButton("Create",
                    Utils.readIcon("res/puzzle.png", 40, 40));
            create.addActionListener(this);
            board = new BoardPanel(BOARD_WIDTH, BOARD_HEIGHT);
            board.addBoardListener(b -> setEnabled(true));
            // panel.setBorder(BorderFactory.createLineBorder(new Color(0x7a8a99)));
            create.setVerticalTextPosition(SwingConstants.BOTTOM);
            create.setHorizontalTextPosition(SwingConstants.CENTER);

            var panelLayout = new GroupLayout(this);
            setLayout(panelLayout);
            panelLayout.setAutoCreateGaps(true);
            panelLayout.setAutoCreateContainerGaps(true);
            panelLayout.setHorizontalGroup(panelLayout.createParallelGroup()
                .addGroup(GroupLayout.Alignment.CENTER, panelLayout.createSequentialGroup()
                    .addGroup(panelLayout.createParallelGroup()
                        .addGroup(panelLayout.createSequentialGroup()
                            .addGroup(type.getHorizontalGroup(panelLayout))
                            .addGroup(difficulty.getHorizontalGroup(panelLayout)))
                        .addGroup(panelLayout.createSequentialGroup()
                            .addGroup(symmetry.getHorizontalGroup(panelLayout))
                            .addGroup(layout.getHorizontalGroup(panelLayout))))
                    .addGap(20)
                    .addComponent(create, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE))
                .addComponent(board, BOARD_WIDTH, BOARD_WIDTH, Short.MAX_VALUE));
            panelLayout.setVerticalGroup(panelLayout.createSequentialGroup()
                .addGroup(panelLayout.createParallelGroup(GroupLayout.Alignment.CENTER, false)
                    .addGroup(panelLayout.createSequentialGroup()
                        .addGroup(panelLayout.createParallelGroup()
                            .addGroup(type.getVerticalGroup(panelLayout))
                            .addGroup(difficulty.getVerticalGroup(panelLayout)))
                        .addGroup(panelLayout.createParallelGroup()
                            .addGroup(symmetry.getVerticalGroup(panelLayout))
                            .addGroup(layout.getVerticalGroup(panelLayout))))
                    .addGroup(GroupLayout.Alignment.CENTER, panelLayout.createSequentialGroup()
                        .addComponent(create, 0, 80, 80)))
                .addComponent(board, BOARD_HEIGHT, BOARD_HEIGHT, Short.MAX_VALUE));
            panelLayout.linkSize(SwingConstants.HORIZONTAL, type.combobox,
                    difficulty.combobox, symmetry.combobox, layout.combobox);
        }

        @Override
        public void setEnabled(boolean enabled) {
            type.setEnabled(enabled);
            difficulty.setEnabled(enabled);
            symmetry.setEnabled(enabled);
            layout.setEnabled(false);
            create.setEnabled(enabled);
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            board.create(type.getValue(), difficulty.getValue(),
                    symmetry.getValue(), layout.getValue());
            setEnabled(false);
        }
    }

    static final int WIDTH = 800;
    static final int HEIGHT = 600;

    JButton generate;
    JButton print;
    JTabbedPane tab;
    Vector<Puzzle> puzzles;

    public App() {
        generate = new JButton("Create All Puzzles",
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
            .addComponent(generate)
            .addComponent(print));
        layout.setVerticalGroup(layout.createParallelGroup()
            .addComponent(generate)
            .addComponent(print));
        layout.linkSize(generate, print);

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
