package com.jeweled;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;

/**
 * Gem types with color-blind friendly design: each gem uses a
 * hue that is easily distinguished by common color vision deficiencies
 * (protanopia/deuteranopia/tritanopia) AND a distinct shape/symbol so
 * that no two gems rely on color alone to be told apart.
 */
public enum GemType {

    RED("Red", new Color(0xE0, 0x3B, 0x3B), new Color(0x8B, 0x1A, 0x1A)),
    YELLOW("Yellow", new Color(0xF5, 0xC2, 0x3C), new Color(0x9A, 0x6B, 0x12)),
    BLUE("Blue", new Color(0x3B, 0x6E, 0xE0), new Color(0x1A, 0x33, 0x8B)),
    GREEN("Green", new Color(0x4C, 0xC0, 0x5A), new Color(0x1E, 0x6E, 0x2A)),
    PURPLE("Purple", new Color(0x9C, 0x4B, 0xD0), new Color(0x55, 0x1F, 0x7A)),
    ORANGE("Orange", new Color(0xE8, 0x8A, 0x3A), new Color(0x8E, 0x4A, 0x12));

    private final String name;
    private final Color light;
    private final Color dark;

    GemType(String name, Color light, Color dark) {
        this.name = name;
        this.light = light;
        this.dark = dark;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return light;
    }

    public Color getDarkColor() {
        return dark;
    }

    /** Draw the distinctive symbol for this gem, centered in the given cell. */
    public void drawSymbol(Graphics2D g, double cx, double cy, double size) {
        double r = size * 0.42;
        g.setColor(getDarkColor());
        g.setStroke(new BasicStroke((float) (size * 0.07), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (this) {
            case RED -> {
                // Circle with inner filled circle - looks like a "berry"
                g.fillOval((int) (cx - r * 0.45), (int) (cy - r * 0.45), (int) (r * 0.9), (int) (r * 0.9));
            }
            case YELLOW -> {
                // Star
                drawStar(g, cx, cy, r, r * 0.45);
            }
            case BLUE -> {
                // Diamond / rhombus
                GeneralPath p = new GeneralPath();
                p.moveTo(cx, cy - r);
                p.lineTo(cx + r, cy);
                p.lineTo(cx, cy + r);
                p.lineTo(cx - r, cy);
                p.closePath();
                g.fill(p);
            }
            case GREEN -> {
                // Triangle
                GeneralPath p = new GeneralPath();
                p.moveTo(cx, cy - r);
                p.lineTo(cx + r * 0.85, cy + r * 0.7);
                p.lineTo(cx - r * 0.85, cy + r * 0.7);
                p.closePath();
                g.fill(p);
            }
            case PURPLE -> {
                // Square rotated 45 (regular square orientation - distinct from diamond)
                double s = r * 0.85;
                g.fillRect((int) (cx - s), (int) (cy - s), (int) (2 * s), (int) (2 * s));
            }
            case ORANGE -> {
                // Hexagon
                GeneralPath p = new GeneralPath();
                int n = 6;
                for (int i = 0; i < n; i++) {
                    double a = Math.PI / 180 * (60 * i - 90);
                    double px = cx + r * Math.cos(a);
                    double py = cy + r * Math.sin(a);
                    if (i == 0) p.moveTo(px, py);
                    else p.lineTo(px, py);
                }
                p.closePath();
                g.fill(p);
            }
            default -> {
                g.fillOval((int) (cx - r * 0.45), (int) (cy - r * 0.45), (int) (r * 0.9), (int) (r * 0.9));
            }
        }
    }

    private void drawStar(Graphics2D g, double cx, double cy, double outer, double inner) {
        GeneralPath p = new GeneralPath();
        int points = 5;
        for (int i = 0; i < points * 2; i++) {
            double radius = (i % 2 == 0) ? outer : inner;
            double a = -Math.PI / 2 + i * Math.PI / points;
            double px = cx + radius * Math.cos(a);
            double py = cy + radius * Math.sin(a);
            if (i == 0) p.moveTo(px, py);
            else p.lineTo(px, py);
        }
        p.closePath();
        g.fill(p);
    }
}
