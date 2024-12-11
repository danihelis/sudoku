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

import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.awt.print.*;
import java.util.*;

import com.danihelis.sudoku.*;

public class Designer implements Printable {

    public static final float DPI = 300f;

    static final int STROKE = 1;
    static final float BORDER_IN = 0.1f;
    static BasicStroke DIAG_STROKE = new BasicStroke(5f,
            BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER,
            10f, new float[] {20f}, 5f);
    static BasicStroke DIV_STROKE = new BasicStroke(5f,
            BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    static Color FAINT = Utils.alphaColor(Color.BLACK, 0.75);
    static final float BORDER = 0.05f;
    static final boolean PRINT_SOLUTION = false;

    java.util.List<Board> boards;

    public Designer(java.util.List<Board> boards) {
        this.boards = boards;
    }

    @Override
    public int print(Graphics g, PageFormat format, int index) {
        if (index > 0) return Printable.NO_SUCH_PAGE;

        int x = (int) format.getImageableX();
        int y = (int) format.getImageableY();
        int width = (int) format.getImageableWidth();
        int height = (int) format.getImageableHeight();
        float scale = 72f / DPI;
        var clip = Utils.clip((Graphics2D) g, x, y, width, height);
        clip.scale(scale, scale);
        renderPage(clip, (int) (width / scale), (int) (height / scale));
        return Printable.PAGE_EXISTS;
    }

    public void renderPage(Graphics2D g, int width, int height) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        int ROWS = 3;
        int COLS = 2;

        int border = (int) (BORDER_IN * DPI);
        int pWidth = (width - border) / 2;
        int pHeight = height / 3;
        for (int row = 0; row < ROWS; row++)
            for (int col = 0; col < COLS; col++) {
                int index = col * ROWS + row;
                int x = col == 0 ? 0 : pWidth + border;
                int y = row * pHeight;
                Graphics2D clip = Utils.clip((Graphics2D) g, x, y,
                        pWidth, pHeight);
                render(boards.get(index), clip, pWidth, pWidth);
            }
    }

    public static void render(Board board, Graphics2D g, int width,
            int height) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        /*
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, width - 1, height - 1);
        */

        int minimum = Math.min(width, height);
        int caption = (int) (minimum * BORDER);
        minimum = Math.min(Math.max(width, height) - caption, minimum);
        boolean vertical = width > height;

        int dim = (minimum - 5) / board.dimension;
        int offX = (width + (vertical ? caption : 0) - board.dimension * dim) / 2;
        int offY = (height - (vertical ? 0 : caption) - board.dimension * dim) / 2;

        // Draw caption
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN,
                Math.round(caption * 0.5f)));
        g.setColor(Color.BLACK);
        String text = String.format("%s Sudoku - %s",
                board.type, board.difficulty);
        if (!vertical) {
            Utils.center(g, text, 0, height - caption, width, caption);
        } else {
            double theta = Math.PI / 2;
            g.rotate(-theta);
            Utils.center(g, text, -height, offX - caption, height, caption);
            g.rotate(theta);
        }

        // Draw puzzle
        Font font = new Font(Font.SANS_SERIF, Font.PLAIN,
                Math.round(dim * 0.7f));
        g.setFont(font);
        Stroke baseStroke = g.getStroke();

        // Draw grids
        for (int row = 0; row <= board.dimension; row++) {
            for (int col = 0; col <= board.dimension; col++) {
                int x = offX + col * dim;
                int y = offY + row * dim;
                if (col < board.dimension) {
                    if (row == 0 || row == board.dimension ||
                            board.getRank(row, col)
                                != board.getRank(row - 1, col)) {
                        // g.fillRect(x, y - STROKE, dim, 2 * STROKE + 1);
                        g.setColor(Color.BLACK);
                        g.setStroke(DIV_STROKE);
                    } else {
                        g.setColor(FAINT);
                        g.setStroke(baseStroke);
                    }
                    g.drawLine(x, y, x + dim, y);
                }
                if (row < board.dimension) {
                    if (col == 0 || col == board.dimension ||
                            board.getRank(row, col)
                                != board.getRank(row, col - 1)) {
                        // g.fillRect(x - STROKE, y, 2 * STROKE + 1, dim);
                        g.setColor(Color.BLACK);
                        g.setStroke(DIV_STROKE);
                    } else {
                        g.setColor(FAINT);
                        g.setStroke(baseStroke);
                    }
                    g.drawLine(x, y, x, y + dim);
                }
            }
        }
        if (board.type == Type.DIAGONAL) {
            g.setStroke(DIAG_STROKE);
            g.setColor(Utils.alphaColor(Color.BLACK, 0.30));
            int length = board.dimension * dim;
            g.drawLine(offX, offY, offX + length, offY + length);
            g.drawLine(offX + length, offY, offX, offY + length);
        }

        // Draw givens
        g.setStroke(baseStroke);
        g.setColor(Color.BLACK);
        for (int row = 0; row < board.dimension; row++) {
            for (int col = 0; col < board.dimension; col++) {
                int x = offX + col * dim;
                int y = offY + row * dim;
                int pos = board.intoPosition(row, col);
                int given = board.given[pos];
                if (given > 0) {
                    Utils.center((Graphics2D) g, "" + given, x, y, dim, dim);
                } else if (PRINT_SOLUTION) {
                    g.setColor(Color.BLUE);
                    Utils.center((Graphics2D) g, "" + board.solution[pos],
                            x, y, dim, dim);
                    g.setColor(Color.BLACK);
                }
            }
        }
    }
}
