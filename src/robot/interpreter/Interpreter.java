package robot.interpreter;

import java.util.List;

import robot.model.Board;
import robot.model.RobotException;
import robot.parser.RobotBaseVisitor;
import robot.parser.RobotParser;

/**
 * Führt ein Roboterprogramm aus, indem es den Parse-Baum mit der Visitor-Technik durchläuft.
 * Anweisungen behandelt dieser Visitor selbst, Bedingungen gibt er an den {@link ConditionEvaluator} ab.
 * Jede ausgeführte Bewegung wird dem {@link StepListener} gemeldet (für die Animation in der GUI).
 */
public class Interpreter extends RobotBaseVisitor<Void> {

    /** Wird nach jedem ausgeführten Befehl aufgerufen. */
    public interface StepListener {
        void afterStep(int stepNumber, String command, Board board);
    }

    /** Wird geworfen, wenn die Ausführung von außen abgebrochen wird. */
    public static class CancelledException extends RuntimeException {
        public CancelledException() {
            super("Abgebrochen");
        }
    }

    /** Interner Sprung für den Befehl stop. */
    private static class StopSignal extends RuntimeException {
        StopSignal() {
            super(null, null, false, false);
        }
    }

    /** So viele Schleifendurchläufe ohne einen einzigen Befehl gelten als Endlosschleife. */
    private static final long MAX_ITERATIONS_WITHOUT_STEP = 1_000_000L;

    private final Board board;
    private final ConditionEvaluator conditions;
    private final StepListener listener;
    private final int maxSteps;
    private int steps;
    private long iterationsWithoutStep;

    public Interpreter(Board board, StepListener listener, int maxSteps) {
        this.board = board;
        this.conditions = new ConditionEvaluator(board);
        this.listener = listener;
        this.maxSteps = maxSteps;
    }

    /** Führt das Programm aus und liefert die Anzahl ausgeführter Befehle. */
    public int run(RobotParser.ProgramContext program) {
        steps = 0;
        iterationsWithoutStep = 0;
        try {
            visitAll(program.statement());
        } catch (StopSignal ignored) {
            // stop beendet das Programm regulär
        }
        return steps;
    }

    // ---------- Anweisungen ----------

    @Override
    public Void visitCommandStatement(RobotParser.CommandStatementContext ctx) {
        checkCancelled();
        String command = ctx.command().getText();
        execute(command);
        recordStep(command);
        if (command.equals("stop")) {
            throw new StopSignal();
        }
        return null;
    }

    @Override
    public Void visitWhileStatement(RobotParser.WhileStatementContext ctx) {
        while (conditions.visit(ctx.condition())) {
            guardLoop();
            visit(ctx.block());
        }
        return null;
    }

    @Override
    public Void visitIfStatement(RobotParser.IfStatementContext ctx) {
        if (conditions.visit(ctx.condition())) {
            visit(ctx.block(0));
        } else if (ctx.block().size() > 1) {
            visit(ctx.block(1));
        }
        return null;
    }

    @Override
    public Void visitBlock(RobotParser.BlockContext ctx) {
        visitAll(ctx.statement());
        return null;
    }

    private void visitAll(List<RobotParser.StatementContext> statements) {
        for (RobotParser.StatementContext s : statements) {
            visit(s);
        }
    }

    // ---------- Befehle ----------

    /** Führt einen einzelnen Befehl auf dem Brett aus. */
    private void execute(String command) {
        switch (command) {
            case "forward":
                board.forward();
                break;
            case "turnLeft":
                board.turnLeft();
                break;
            case "turnRight":
                board.turnRight();
                break;
            case "stop":
                break;
            default:
                throw new RobotException("Unbekannter Befehl: " + command);
        }
    }

    /** Zählt den Schritt, meldet ihn und prüft das Schrittlimit. */
    private void recordStep(String command) {
        steps++;
        iterationsWithoutStep = 0;
        listener.afterStep(steps, command, board);
        if (steps >= maxSteps && !command.equals("stop")) {
            throw new RobotException("Schrittlimit von " + maxSteps + " erreicht – vermutlich eine Endlosschleife"
                    + " oder es gibt keinen Weg zur Reihe 8.");
        }
    }

    // ---------- Schutz vor Endlosschleifen ----------

    private void guardLoop() {
        checkCancelled();
        if (++iterationsWithoutStep > MAX_ITERATIONS_WITHOUT_STEP) {
            throw new RobotException("Eine Schleife läuft ohne Ende, ohne dass sich der Roboter bewegt.");
        }
    }

    private void checkCancelled() {
        if (Thread.currentThread().isInterrupted()) {
            throw new CancelledException();
        }
    }
}
