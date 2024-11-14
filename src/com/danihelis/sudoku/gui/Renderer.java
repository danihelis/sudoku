package com.danihelis.sudoku.gui;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;

abstract class Renderer extends JPanel {

    BufferedImage image;

    Renderer(int width, int height) {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resetImage();
            }
        });
        setMinimumSize(new Dimension(width, height));
    }

    void resetImage() {
        image = null;
        updateImage();
    }

    abstract void drawCanvas(Graphics2D g);

    synchronized void updateImage() {
        if (image == null && getWidth() > 0 && getHeight() > 0) {
            image = Utils.createImage(getWidth(), getHeight());
        }
        if (image == null) return;
        var g = Utils.createGraphics(image);
        drawCanvas(g);
        g.dispose();
        repaint();
    }

    synchronized void render(Graphics2D g) {
        if (image != null) {
            Utils.setupGraphics(g);
            g.drawImage(image, 0, 0, null);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        render((Graphics2D) g);
    }
}
