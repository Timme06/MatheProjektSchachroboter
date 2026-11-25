package robot.model;

/** Laufzeitfehler bei der Ausführung eines Roboterprogramms (z. B. Kollision). */
public class RobotException extends RuntimeException {
    public RobotException(String message) {
        super(message);
    }
}
