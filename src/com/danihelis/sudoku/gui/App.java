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
                    .addComponent(combobox)
                    .addComponent(button));
        }

        GroupLayout.Group getVerticalGroup(GroupLayout layout) {
            return layout.createSequentialGroup()
                .addComponent(label)
                .addGroup(layout.createParallelGroup()
                    .addComponent(combobox)
                    .addComponent(button));
        }
    }

    static final int WIDTH = 800;
    static final int HEIGHT = 600;

    Selector<Type> type;
    Selector<Difficulty> difficulty;
    Selector<Symmetry> symmetry;
    JButton create;
    JButton generate;
    JButton print;
    JProgressBar progress;
    JTabbedPane page;

    public App() {
        type = new Selector<>("Sudoku type", Arrays.asList(Type.values()));
        difficulty = new Selector<>("Difficulty", Arrays.asList(Difficulty.values()));
        symmetry = new Selector<>("Puzzle symmetry", Arrays.asList(Symmetry.values()));

        create = new JButton("Create", Utils.readIcon("res/puzzle.png", 40, 40));
        create.setVerticalTextPosition(SwingConstants.BOTTOM);
        create.setHorizontalTextPosition(SwingConstants.CENTER);

        generate = new JButton("Create Puzzles",
                Utils.readIcon("res/gears.png", 16, 16));
        // generate.addActionListener(this);
        print = new JButton("Print All Puzzles",
                Utils.readIcon("res/print.png", 16, 16));
        // print.addActionListener(this);

        var layout = new GroupLayout(this);
        setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createParallelGroup()
            .addGroup(GroupLayout.Alignment.CENTER, layout.createSequentialGroup()
                .addComponent(generate)
                .addComponent(print))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(type.getHorizontalGroup(layout))
                        .addGroup(difficulty.getHorizontalGroup(layout)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(symmetry.getHorizontalGroup(layout))))
                .addComponent(create)));
        layout.setVerticalGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup()
                .addComponent(generate)
                .addComponent(print))
            .addGroup(layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup()
                        .addGroup(type.getVerticalGroup(layout))
                        .addGroup(difficulty.getVerticalGroup(layout)))
                    .addGroup(layout.createParallelGroup()
                        .addGroup(symmetry.getVerticalGroup(layout))))
                .addComponent(create, GroupLayout.Alignment.CENTER))); // 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        /*
        layout.linkSize(SwingConstants.HORIZONTAL, type.combobox,
                difficulty.combobox, symmetry.combobox);
        layout.linkSize(SwingConstants.HORIZONTAL, type.button,
                difficulty.button, symmetry.button);
        */
        layout.linkSize(SwingConstants.VERTICAL, type.button, type.combobox,
                difficulty.combobox, difficulty.button,
                symmetry.combobox, symmetry.button);
    }

    public void start() {
        new Window();
    }
}
