package com.danihelis.sudoku.gui;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;

import com.danihelis.sudoku.*;

public class BoardPanel extends Renderer {

    public interface Listener {

        void boardCreated(Board board);
    }

    class BoardCreator implements Runnable {
        Type type;
        Difficulty difficulty;
        Symmetry symmetry;
        Symmetry layout;

        BoardCreator(Type type, Difficulty difficulty, Symmetry symmetry,
                Symmetry layout) {
            this.type = type;
            this.difficulty = difficulty;
            this.symmetry = symmetry;
            this.layout = layout;
        }

        @Override
        public void run() {
            Board board = null;
            while (board == null) {
                try {
                    board = new Creator().create(type, difficulty, symmetry);
                } catch (Error e) {
                    e.printStackTrace();
                }
            }
            BoardPanel.this.boardCreated(board);
        }
    }

    Board board;
    Vector<Listener> listeners;
    BoardCreator creator;

    public BoardPanel(int width, int height) {
        super(width, height);
        listeners = new Vector<>();
    }

    public void addBoardListener(Listener listener) {
        listeners.add(listener);
    }

    public synchronized void create(Type type, Difficulty difficulty,
            Symmetry symmetry, Symmetry layout) {
        if (creator != null) return;
        creator = new BoardCreator(type, difficulty, symmetry, layout);
        new Thread(creator).start();
        updateImage();
    }

    private synchronized void boardCreated(Board board) {
        this.board = board;
        listeners.forEach(l -> l.boardCreated(board));
        creator = null;
        updateImage();
    }

    @Override
    void drawCanvas(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        if (board != null) {
            Designer.render(board, g, getWidth(), getHeight());
        } else {
            g.setColor(Color.BLACK);
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            var text = creator != null ? "Creating puzzle..."
                    : "Click on Create to generate a puzzle";
            Utils.center(g, text, 0, 0, getWidth(), getHeight());
        }
    }
}