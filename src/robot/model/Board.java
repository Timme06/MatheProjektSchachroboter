package robot.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Das Schachbrett mit Hindernissen und dem Roboter.
 * Koordinaten: x = Spalte (0 = A ... 7 = H), y = Reihe (0 = 1 ... 7 = 8).
 * Der Brettrand zählt als Hindernis.
 */
public final class Board {

    public static final int SIZE = 8;

    /** Startfeld A1: dort beginnt der Roboter, dort darf kein Hindernis stehen. */
    public static boolean isStart(int fx, int fy) {
        return fx == 0 && fy == 0;
    }

    private final boolean[][] obstacles = new boolean[SIZE][SIZE];
    private final List<Point> trail = new ArrayList<>();
    private int x;
    private int y;
    private Direction direction;

    public Board() {
        resetRobot();
    }

    // ---------- Aufbau ----------

    /** Setzt den Roboter auf A1 mit Blick nach Norden zurück. */
    public synchronized void resetRobot() {
        x = 0;
        y = 0;
        direction = Direction.NORTH;
        trail.clear();
        trail.add(new Point(x, y));
    }

    public synchronized void clearObstacles() {
        for (boolean[] column : obstacles) {
            Arrays.fill(column, false);
        }
    }

    /** Setzt oder entfernt ein Hindernis. Das Startfeld A1 bleibt immer frei. */
    public synchronized void setObstacle(int fx, int fy, boolean value) {
        if (inside(fx, fy) && !isStart(fx, fy)) {
            obstacles[fx][fy] = value;
        }
    }

    // ---------- Sensoren ----------

    public synchronized boolean frontFree() {
        return isFreeIn(direction);
    }

    public synchronized boolean leftFree() {
        return isFreeIn(direction.left());
    }

    public synchronized boolean rightFree() {
        return isFreeIn(direction.right());
    }

    /** Ziel erreicht = Roboter steht auf Reihe 8. */
    public synchronized boolean atGoal() {
        return y == SIZE - 1;
    }

    // ---------- Bewegungen ----------

    public synchronized void forward() {
        int nx = x + direction.dx;
        int ny = y + direction.dy;
        if (!inside(nx, ny)) {
            throw new RobotException("Kollision mit dem Brettrand: forward auf " + fieldName(x, y)
                    + " mit Blick nach " + direction.label + ".");
        }
        if (obstacles[nx][ny]) {
            throw new RobotException("Kollision mit dem Hindernis auf " + fieldName(nx, ny) + ".");
        }
        x = nx;
        y = ny;
        trail.add(new Point(x, y));
    }

    public synchronized void turnLeft() {
        direction = direction.left();
    }

    public synchronized void turnRight() {
        direction = direction.right();
    }

    // ---------- Abfragen ----------

    public synchronized int getX() {
        return x;
    }

    public synchronized int getY() {
        return y;
    }

    public synchronized Direction getDirection() {
        return direction;
    }

    public synchronized List<Point> getTrail() {
        return new ArrayList<>(trail);
    }

    public synchronized List<Point> getObstacles() {
        List<Point> list = new ArrayList<>();
        for (int fy = 0; fy < SIZE; fy++) {
            for (int fx = 0; fx < SIZE; fx++) {
                if (obstacles[fx][fy]) {
                    list.add(new Point(fx, fy));
                }
            }
        }
        return list;
    }

    public synchronized String position() {
        return fieldName(x, y) + ", Blick nach " + direction.label;
    }

    /** Ist das Nachbarfeld in Richtung d frei? Der Brettrand zählt als Wand. */
    private boolean isFreeIn(Direction d) {
        return isFree(x + d.dx, y + d.dy);
    }

    private boolean isFree(int fx, int fy) {
        return inside(fx, fy) && !obstacles[fx][fy];
    }

    private static boolean inside(int fx, int fy) {
        return fx >= 0 && fx < SIZE && fy >= 0 && fy < SIZE;
    }

    /** Feldname in Schachnotation, z. B. (2, 3) -> "C4". */
    public static String fieldName(int fx, int fy) {
        return "" + (char) ('A' + fx) + (fy + 1);
    }

    /** Gegenstück zu {@link #fieldName}: "C4" -> (2, 3). Erwartet ein gültiges Feld A1 bis H8. */
    public static Point parseField(String field) {
        String f = field.toUpperCase();
        return new Point(f.charAt(0) - 'A', f.charAt(1) - '1');
    }
}
