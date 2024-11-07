package com.danihelis.sudoku.gui;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;

public class Utils {

    private static final BufferedImage GENERIC_IMAGE;
    public static final Graphics2D GENERIC_GRAPHICS;

    static {
        GENERIC_IMAGE = createImage(1, 1);
        GENERIC_GRAPHICS = createGraphics(GENERIC_IMAGE);
    }

    public static BufferedImage readImage(String url) throws Exception {
        return ImageIO.read(ClassLoader.getSystemResource(url));
    }

    public static ImageIcon readIcon(String icon) {
        return new ImageIcon(ClassLoader.getSystemResource(icon));
    }

    public static ImageIcon readIcon(String icon, int width, int height) {
        return new ImageIcon(readIcon(icon).getImage().getScaledInstance(
                    width, height, Image.SCALE_SMOOTH));
    }

    public static BufferedImage createImage(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    public static Graphics2D createGraphics(BufferedImage image) {
        Graphics2D g = image.createGraphics();
        Utils.setupGraphics(g);
        return g;
    }

    public static BufferedImage resizeImage(Image image, int width,
            int height) {
        var resized = createImage(width, height);
        var g = createGraphics(resized);
        g.drawImage(image.getScaledInstance(width, height, Image.SCALE_SMOOTH),
               null, null);
        g.dispose();
        return resized;
    }

    public static Graphics2D clip(Graphics2D g, int x, int y, int width,
            int height) {
        Graphics2D out = (Graphics2D) g.create(x, y, width, height);
        setupGraphics(out);
        return out;
    }

    public static void setupGraphics(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_DITHERING,
                RenderingHints.VALUE_DITHER_ENABLE);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        /*
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        */
    }

    public static RenderingHints getRenderingHints() {
        return GENERIC_GRAPHICS.getRenderingHints();
    }

    public static Color alphaColor(Color c, double alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(),
                (int) (255 * alpha));
    }

    public static Color alphaBlend(Color opaque, Color over, double alpha) {
        int result = 0;
        for (int i = 0, mask = 0xff; i < 3; i++) {
            int t0 = (opaque.getRGB() >> (i * 8)) & mask;
            int t1 = (over.getRGB() >> (i * 8)) & mask;
            int comb = (int) (t0 * (1 - alpha) + t1 * alpha);
            result += comb << (i * 8);
        }
        return new Color(result);
    }

    public static Color grayColor(Color c) {
        int average = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
        return new Color(average, average, average);
    }

    public static Color colorVariation(Color color) {
        return colorVariation(color.getRGB() & 0xFFFFFF);
    }

    public static Color colorVariation(int rgb) {
        var tone = new int[3];
        double noise = (Math.random() - 0.5) * 0x17;
        for (int i = 0; i < tone.length; i++, rgb >>= 8) {
            int color = rgb % 0x100;
            tone[i] = Math.min(0xFF, Math.max(0,
                    (int) (color + noise * color / 0xFF)));
        }
        return new Color(tone[2], tone[1], tone[0]);
    }

    public static Rectangle2D getBounds(Graphics2D g, String text) {
        return g.getFont().getStringBounds(text, g.getFontRenderContext());
    }

    public static Rectangle2D getFontBounds(Font font, String text) {
        GENERIC_GRAPHICS.setFont(font);
        return getBounds(GENERIC_GRAPHICS, text);
    }

    private static int centerX(Rectangle2D bounds, int width) {
        return (int) Math.round((width - bounds.getWidth() - 1) / 2);
    }

    private static int centerY(Rectangle2D bounds, int height) {
        return (int) Math.round(bounds.getHeight() * 0.31 + height / 2);
    }

    public static Rectangle2D left(Graphics2D g, String text,
            Rectangle area) {
        return left(g, text, area.x, area.y, area.height);
    }

    public static Rectangle2D left(Graphics2D g, String text, int x,
            int y, int height) {
        Rectangle2D bounds = getBounds(g, text);
        g.drawString(text, x, y + centerY(bounds, height));
        return bounds;
    }

    public static Rectangle2D right(Graphics2D g, String text,
            Rectangle area) {
        return right(g, text, area.x, area.y, area.height);
    }

    public static Rectangle2D right(Graphics2D g, String text, int x, int y,
            int height) {
        Rectangle2D bounds = getBounds(g, text);
        g.drawString(text, x - (int) bounds.getWidth(),
                y + centerY(bounds, height));
        return bounds;
    }

    public static Rectangle center(Graphics2D g, String text,
            Rectangle area) {
        return center(g, text, area.x, area.y, area.width, area.height);
    }

    public static Rectangle center(Graphics2D g, String text, int x, int y,
            int width, int height) {
        Rectangle2D bounds = getBounds(g, text);
        var frame = new Rectangle(x + centerX(bounds, width),
                y + centerY(bounds, height),
                (int) Math.round(bounds.getWidth()),
                (int) Math.round(bounds.getHeight()));
        /*
        g.drawRect(frame.x, frame.y - frame.height, frame.width, frame.height);
        */
        g.drawString(text, frame.x, frame.y);
        return frame;
    }

    public static void setUIFontSize(float size) {
        var keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            var key = keys.nextElement();
            var value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource font) {
                UIManager.put(key, font.deriveFont(size));
            }
        }
    }
}
