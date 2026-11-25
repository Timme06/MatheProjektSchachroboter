package robot.interpreter;

import robot.model.Board;
import robot.model.RobotException;
import robot.parser.RobotBaseVisitor;
import robot.parser.RobotParser;

/**
 * Zweiter Visitor: wertet Bedingungen aus und liefert true/false.
 * Getrennt vom {@link Interpreter}, weil Bedingungen einen Wert zurückgeben,
 * Anweisungen dagegen nur eine Wirkung haben.
 */
public class ConditionEvaluator extends RobotBaseVisitor<Boolean> {

    private final Board board;

    public ConditionEvaluator(Board board) {
        this.board = board;
    }

    /** not &lt;Bedingung&gt;: innere Bedingung auswerten und umkehren. */
    @Override
    public Boolean visitNotCondition(RobotParser.NotConditionContext ctx) {
        return !visit(ctx.condition());
    }

    /** Sensor: am Brett nachfragen (relativ zur Blickrichtung des Roboters). */
    @Override
    public Boolean visitSensorCondition(RobotParser.SensorConditionContext ctx) {
        String sensor = ctx.sensor().getText();
        switch (sensor) {
            case "frontFree":
                return board.frontFree();
            case "leftFree":
                return board.leftFree();
            case "rightFree":
                return board.rightFree();
            case "atGoal":
                return board.atGoal();
            default:
                throw new RobotException("Unbekannter Sensor: " + sensor);
        }
    }
}
