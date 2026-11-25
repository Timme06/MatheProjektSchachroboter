package robot.interpreter;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import robot.model.Board;
import robot.parser.RobotLexer;
import robot.parser.RobotParser;

/** Liest einen DSL-Text ein und sammelt Syntaxfehler sowie die Hindernisse. */
public final class ProgramParser {

    /** Ein gefundener Fehler mit Zeilennummer (1-basiert). */
    public static final class Problem {
        public final int line;
        public final String message;

        Problem(int line, String message) {
            this.line = line;
            this.message = message;
        }

        @Override
        public String toString() {
            return "Zeile " + line + ": " + message;
        }
    }

    /** Ergebnis des Einlesens. */
    public static final class Result {
        public final RobotParser.ProgramContext tree;
        public final List<Problem> problems;
        public final List<Point> obstacles;

        Result(RobotParser.ProgramContext tree, List<Problem> problems, List<Point> obstacles) {
            this.tree = tree;
            this.problems = Collections.unmodifiableList(problems);
            this.obstacles = Collections.unmodifiableList(obstacles);
        }

        public boolean isValid() {
            return problems.isEmpty();
        }
    }

    private ProgramParser() {
    }

    public static Result parse(String code) {
        final List<Problem> problems = new ArrayList<>();
        BaseErrorListener collector = new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
                                    int charPositionInLine, String msg, RecognitionException e) {
                problems.add(new Problem(line, "Spalte " + (charPositionInLine + 1) + ": " + translate(msg)));
            }
        };

        RobotLexer lexer = new RobotLexer(CharStreams.fromString(code));
        lexer.removeErrorListeners();
        lexer.addErrorListener(collector);

        RobotParser parser = new RobotParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(collector);

        RobotParser.ProgramContext tree = parser.program();

        List<Point> obstacles = problems.isEmpty() ? readObstacles(tree, problems) : new ArrayList<>();
        return new Result(tree, problems, obstacles);
    }

    /** Liest die Hindernisse aus dem optionalen board-Block. */
    private static List<Point> readObstacles(RobotParser.ProgramContext tree, List<Problem> problems) {
        List<Point> obstacles = new ArrayList<>();
        if (tree.board() == null) {
            return obstacles;
        }
        for (RobotParser.ObstacleContext o : tree.board().obstacle()) {
            Point field = Board.parseField(o.FIELD().getText());
            if (Board.isStart(field.x, field.y)) {
                problems.add(new Problem(o.getStart().getLine(),
                        "Auf A1 startet der Roboter – dort darf kein Hindernis stehen."));
            } else {
                obstacles.add(field);
            }
        }
        return obstacles;
    }

    /** Übersetzt die häufigsten ANTLR-Meldungen ins Deutsche. */
    private static String translate(String msg) {
        return msg
                .replace("mismatched input", "unerwartete Eingabe")
                .replace("extraneous input", "überflüssige Eingabe")
                .replace("no viable alternative at input", "unbekannte Anweisung bei")
                .replace("token recognition error at:", "unbekanntes Zeichen:")
                .replace("missing", "es fehlt")
                .replace(" expecting", ", erwartet wurde")
                .replace("at '<EOF>'", "am Ende des Programms")
                .replace("<EOF>", "Programmende")
                .replace("FIELD", "Feld (A1 bis H8)")
                .replace("WORD", "Wort")
                .replace(" at ", " bei ");
    }
}
