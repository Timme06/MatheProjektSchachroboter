package robot.interpreter;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import robot.model.Board;
import robot.parser.RobotParser;

/**
 * Schreibt eine Liste von Hindernissen als board-Block in einen Programmtext.
 * Der Programmtext bleibt so die einzige Quelle für das Brett.
 */
public final class BoardBlockWriter {

    /** Notlösung für Programme mit Syntaxfehlern, in denen es keinen Parse-Baum gibt. */
    private static final Pattern BOARD_BLOCK = Pattern.compile("board\\s*\\{[^}]*\\}");

    private BoardBlockWriter() {
    }

    /** Liefert den Programmtext mit neuem board-Block (ersetzt einen vorhandenen oder stellt ihn voran). */
    public static String replaceBoardBlock(String code, List<Point> obstacles) {
        String block = format(obstacles);
        ProgramParser.Result parsed = ProgramParser.parse(code);
        if (parsed.isValid()) {
            RobotParser.BoardContext board = parsed.tree.board();
            if (board == null) {
                return block + "\n\n" + code;
            }
            int start = board.getStart().getStartIndex();
            int end = board.getStop().getStopIndex() + 1;
            return code.substring(0, start) + block + code.substring(end);
        }
        Matcher m = BOARD_BLOCK.matcher(code);
        if (m.find()) {
            return code.substring(0, m.start()) + block + code.substring(m.end());
        }
        return block + "\n\n" + code;
    }

    /** Formatiert die Hindernisse als board-Block, sortiert von A1 nach H8. */
    static String format(List<Point> obstacles) {
        List<Point> sorted = new ArrayList<>(obstacles);
        sorted.sort(Comparator.comparingInt((Point p) -> p.y).thenComparingInt(p -> p.x));
        StringBuilder block = new StringBuilder("board {\n");
        for (Point p : sorted) {
            block.append("    obstacle ").append(Board.fieldName(p.x, p.y)).append('\n');
        }
        return block.append('}').toString();
    }
}
