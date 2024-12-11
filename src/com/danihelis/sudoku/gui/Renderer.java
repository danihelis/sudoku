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
