package com.danihelis.sudoku.gui;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;

import com.danihelis.sudoku.*;

public class BoardPanel extends Renderer {

    Board board;

    public BoardPanel(int width, int height) {
        super(width, height);
        board = new Creator().create(Type.DIAGONAL);
    }

    public void erase() {
        board = null;
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
            Utils.center(g, "Click on Create Puzzle", 0, 0, getWidth(), getHeight());
        }
    }
}
