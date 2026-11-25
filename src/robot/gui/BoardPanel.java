package robot.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.function.BiConsumer;

import javax.swing.JPanel;

import robot.model.Board;

/** Zeichnet das Schachbrett mit Hindernissen, Spur und Roboter. Klick auf ein Feld = Hindernis an/aus. */
public class BoardPanel extends JPanel {

    private static final Color LIGHT = new Color(0xEEEED2);
    private static final Color DARK = new Color(0x769656);
    private static final Color GOAL = new Color(255, 215, 0, 90);
    private static final Color TRAIL = new Color(55, 138, 221, 150);
    private static final Color LABEL = new Color(0x444441);

    private final Board board;
    private BiConsumer<Integer, Integer> fieldClickHandler;
    private boolean editable = true;

    public BoardPanel(Board board) {
        this.board = board;
        setPreferredSize(new Dimension(560, 560));
        setBackground(Color.WHITE);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!editable || fieldClickHandler == null) {
                    return;
                }
                Layout l = layoutNow();
                int fx = (int) Math.floor((e.getX() - l.left) / l.cell);
                int row = (int) Math.floor((e.getY() - l.top) / l.cell);
                int fy = Board.SIZE - 1 - row;
                if (fx >= 0 && fx < Board.SIZE && fy >= 0 && fy < Board.SIZE) {
                    fieldClickHandler.accept(fx, fy);
                }
            }
        });
    }

    public void setFieldClickHandler(BiConsumer<Integer, Integer> handler) {
        this.fieldClickHandler = handler;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        setToolTipText(editable ? "Klick auf ein Feld setzt oder entfernt eine Kiste" : null);
    }

    // ---------- Layout ----------

    private static final class Layout {
        double left;
        double top;
        double cell;
    }

    private Layout layoutNow() {
        Layout l = new Layout();
        double margin = 28;
        double available = Math.min(getWidth(), getHeight()) - 2 * margin;
        l.cell = Math.max(10, available / Board.SIZE);
        double boardSize = l.cell * Board.SIZE;
        l.left = (getWidth() - boardSize) / 2.0;
        l.top = (getHeight() - boardSize) / 2.0;
        return l;
    }

    // ---------- Zeichnen ----------

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Layout l = layoutNow();
        int n = Board.SIZE;

        // Felder
        for (int fy = 0; fy < n; fy++) {
            for (int fx = 0; fx < n; fx++) {
                double px = l.left + fx * l.cell;
                double py = l.top + (n - 1 - fy) * l.cell;
                g.setColor((fx + fy) % 2 == 0 ? DARK : LIGHT);
                g.fill(new java.awt.geom.Rectangle2D.Double(px, py, l.cell + 0.5, l.cell + 0.5));
                if (fy == n - 1) {
                    g.setColor(GOAL);
                    g.fill(new java.awt.geom.Rectangle2D.Double(px, py, l.cell + 0.5, l.cell + 0.5));
                }
            }
        }
        g.setColor(new Color(0x2C2C2A));
        g.setStroke(new BasicStroke(2f));
        g.draw(new java.awt.geom.Rectangle2D.Double(l.left, l.top, l.cell * n, l.cell * n));

        // Beschriftung A-H und 1-8
        g.setFont(getFont().deriveFont(Font.BOLD, 13f));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(LABEL);
        for (int i = 0; i < n; i++) {
            String col = String.valueOf((char) ('A' + i));
            String row = String.valueOf(i + 1);
            double cx = l.left + i * l.cell + l.cell / 2;
            double cy = l.top + (n - 1 - i) * l.cell + l.cell / 2;
            g.drawString(col, (float) (cx - fm.stringWidth(col) / 2.0), (float) (l.top + n * l.cell + 18));
            g.drawString(row, (float) (l.left - 16 - fm.stringWidth(row) / 2.0), (float) (cy + fm.getAscent() / 2.0 - 2));
        }

        // Hindernisse
        for (Point p : board.getObstacles()) {
            drawCrate(g, center(l, p.x, p.y, true), center(l, p.x, p.y, false), l.cell);
        }

        // Spur
        List<Point> trail = board.getTrail();
        if (trail.size() > 1) {
            g.setColor(TRAIL);
            g.setStroke(new BasicStroke((float) (l.cell * 0.08), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D path = new Path2D.Double();
            for (int i = 0; i < trail.size(); i++) {
                Point p = trail.get(i);
                double cx = center(l, p.x, p.y, true);
                double cy = center(l, p.x, p.y, false);
                if (i == 0) {
                    path.moveTo(cx, cy);
                } else {
                    path.lineTo(cx, cy);
                }
            }
            g.draw(path);
        }

        // Roboter
        drawRobot(g, center(l, board.getX(), board.getY(), true), center(l, board.getX(), board.getY(), false),
                l.cell, board.getDirection().angle());

        g.dispose();
    }

    private static double center(Layout l, int fx, int fy, boolean horizontal) {
        if (horizontal) {
            return l.left + fx * l.cell + l.cell / 2;
        }
        return l.top + (Board.SIZE - 1 - fy) * l.cell + l.cell / 2;
    }

    /** Roboter von oben; das rote Dreieck zeigt die Blickrichtung. Entwurf in einem 200er-Raster. */
    static void drawRobot(Graphics2D g0, double cx, double cy, double cell, int angleDeg) {
        Graphics2D g = (Graphics2D) g0.create();
        AffineTransform t = new AffineTransform();
        t.translate(cx, cy);
        t.rotate(Math.toRadians(angleDeg));
        double s = cell / 165.0;
        t.scale(s, s);
        t.translate(0, 15);
        g.transform(t);

        // Richtungsdreieck + Antenne
        g.setColor(new Color(0xE24B4A));
        g.fill(triangle(0, -82, -16, -58, 16, -58));
        g.setColor(new Color(0x2C2C2A));
        g.setStroke(new BasicStroke(3f));
        g.drawLine(0, -58, 0, -48);

        // Ketten
        g.setColor(new Color(0x5F5E5A));
        g.fill(new RoundRectangle2D.Double(-70, -30, 14, 60, 10, 10));
        g.fill(new RoundRectangle2D.Double(56, -30, 14, 60, 10, 10));

        // Körper
        RoundRectangle2D body = new RoundRectangle2D.Double(-58, -48, 116, 100, 36, 36);
        g.setColor(new Color(0x378ADD));
        g.fill(body);
        g.setColor(new Color(0x0C447C));
        g.setStroke(new BasicStroke(3f));
        g.draw(body);

        // Gesicht
        RoundRectangle2D face = new RoundRectangle2D.Double(-42, -34, 84, 40, 20, 20);
        g.setColor(new Color(0xE6F1FB));
        g.fill(face);
        g.setColor(new Color(0x0C447C));
        g.setStroke(new BasicStroke(2f));
        g.draw(face);
        g.setColor(new Color(0x2C2C2A));
        g.fillOval(-29, -23, 18, 18);
        g.fillOval(11, -23, 18, 18);
        g.setColor(Color.WHITE);
        g.fillOval(-20, -20, 6, 6);
        g.fillOval(20, -20, 6, 6);
        g.setColor(new Color(0x0C447C));
        g.fill(new RoundRectangle2D.Double(-22, 20, 44, 8, 8, 8));
        g.setColor(new Color(0xEF9F27));
        g.fillOval(-40, 34, 8, 8);
        g.setColor(new Color(0x97C459));
        g.fillOval(32, 34, 8, 8);

        g.dispose();
    }

    /** Holzkiste als Hindernis. Entwurf in einem 200er-Raster. */
    static void drawCrate(Graphics2D g0, double cx, double cy, double cell) {
        Graphics2D g = (Graphics2D) g0.create();
        g.translate(cx, cy);
        double s = cell / 160.0;
        g.scale(s, s);

        Color dark = new Color(0x5C3A14);
        g.setColor(new Color(0xC8873F));
        g.fill(new RoundRectangle2D.Double(-60, -60, 120, 120, 8, 8));
        g.setColor(new Color(0x8A5524));
        g.setStroke(new BasicStroke(2f));
        g.drawLine(-60, -20, 60, -20);
        g.drawLine(-60, 20, 60, 20);
        g.setColor(dark);
        g.setStroke(new BasicStroke(10f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(-56, -56, 56, 56);
        g.drawLine(56, -56, -56, 56);
        g.setStroke(new BasicStroke(8f));
        g.draw(new RoundRectangle2D.Double(-60, -60, 120, 120, 8, 8));
        g.setColor(new Color(0x2C2C2A));
        int[][] nails = {{-48, -48}, {48, -48}, {-48, 48}, {48, 48}};
        for (int[] nail : nails) {
            g.fillOval(nail[0] - 3, nail[1] - 3, 6, 6);
        }
        g.dispose();
    }

    private static Path2D triangle(double x1, double y1, double x2, double y2, double x3, double y3) {
        Path2D p = new Path2D.Double();
        p.moveTo(x1, y1);
        p.lineTo(x2, y2);
        p.lineTo(x3, y3);
        p.closePath();
        return p;
    }
}
