package robot.model;

/** Blickrichtung des Roboters. Norden zeigt Richtung Reihe 8. */
public enum Direction {
    NORTH(0, 1, "Norden"),
    EAST(1, 0, "Osten"),
    SOUTH(0, -1, "Süden"),
    WEST(-1, 0, "Westen");

    public final int dx;
    public final int dy;
    public final String label;

    Direction(int dx, int dy, String label) {
        this.dx = dx;
        this.dy = dy;
        this.label = label;
    }

    /** Richtung nach einer 90°-Drehung gegen den Uhrzeigersinn. */
    public Direction left() {
        return values()[(ordinal() + 3) % 4];
    }

    /** Richtung nach einer 90°-Drehung im Uhrzeigersinn. */
    public Direction right() {
        return values()[(ordinal() + 1) % 4];
    }

    /** Drehwinkel in Grad (Norden = 0, im Uhrzeigersinn) – für die Darstellung. */
    public int angle() {
        return ordinal() * 90;
    }
}
