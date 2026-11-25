package robot.interpreter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Point;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

import org.junit.Test;

import robot.model.Board;
import robot.model.RobotException;

public class InterpreterTest {

    static final String AUSWEICHEN =
            "while not atGoal { if frontFree { forward } else { turnRight forward turnLeft } } stop";
    static final String RECHTE_HAND =
            "while not atGoal { if rightFree { turnRight forward } else "
            + "{ if frontFree { forward } else { turnLeft } } } stop";
    static final String WAND_LUECKE_LINKS =
            "board { obstacle A3 obstacle B5 obstacle C5 obstacle D5 obstacle E5 obstacle F5 obstacle G5 obstacle H5 } ";

    /** Liest ein Programm ein, stellt das Brett auf und führt es aus. */
    private static Board run(String code, int maxSteps) {
        ProgramParser.Result r = ProgramParser.parse(code);
        assertTrue(r.problems.toString(), r.isValid());
        Board board = new Board();
        for (Point p : r.obstacles) {
            board.setObstacle(p.x, p.y, true);
        }
        new Interpreter(board, (n, cmd, b) -> { }, maxSteps).run(r.tree);
        return board;
    }

    @Test
    public void stufe1ReachesGoalOnEmptyBoard() {
        Board board = run("forward forward forward forward forward forward forward stop", 100);
        assertTrue(board.atGoal());
    }

    @Test(expected = RobotException.class)
    public void stufe1CollidesWithCrate() {
        run("board { obstacle A4 } forward forward forward forward", 100);
    }

    @Test
    public void stufe2AvoidsSingleCrates() {
        assertTrue(run("board { obstacle A4 obstacle B6 } " + AUSWEICHEN, 500).atGoal());
    }

    @Test(expected = RobotException.class)
    public void stufe2FailsWhenGapIsOnTheLeft() {
        run(WAND_LUECKE_LINKS + AUSWEICHEN, 500);
    }

    @Test
    public void stufe3SolvesTheSameBoard() {
        assertTrue(run(WAND_LUECKE_LINKS + RECHTE_HAND, 500).atGoal());
    }

    @Test
    public void stopEndsProgramImmediately() {
        Board board = run("forward stop forward forward", 100);
        assertEquals(1, board.getY());
    }

    @Test
    public void stepLimitStopsEndlessLoop() {
        try {
            run("while not atGoal { turnLeft }", 50);
            fail("Schrittlimit hätte greifen müssen");
        } catch (RobotException e) {
            assertTrue(e.getMessage().contains("Schrittlimit"));
        }
    }

    @Test
    public void loopWithoutCommandsIsStopped() {
        try {
            run("while not atGoal { }", 50);
            fail("Leere Schleife hätte abgebrochen werden müssen");
        } catch (RobotException e) {
            assertTrue(e.getMessage().contains("Schleife"));
        }
    }

    /** Die Rechte-Hand-Regel kommt genau dann an, wenn eine Breitensuche einen Weg findet. */
    @Test
    public void rightHandRuleMatchesBreadthFirstSearch() {
        Random random = new Random(42);
        for (int t = 0; t < 2000; t++) {
            boolean[][] obstacles = new boolean[Board.SIZE][Board.SIZE];
            StringBuilder code = new StringBuilder("board { ");
            for (int i = random.nextInt(30); i > 0; i--) {
                int x = random.nextInt(Board.SIZE);
                int y = random.nextInt(Board.SIZE);
                if (!Board.isStart(x, y) && !obstacles[x][y]) {
                    obstacles[x][y] = true;
                    code.append("obstacle ").append(Board.fieldName(x, y)).append(' ');
                }
            }
            code.append("} ").append(RECHTE_HAND);
            boolean reachable = pathExists(obstacles);
            boolean arrived;
            try {
                arrived = run(code.toString(), 2000).atGoal();
            } catch (RobotException e) {
                arrived = false;
            }
            assertEquals("Brett " + t + ": " + code, reachable, arrived);
        }
    }

    private static boolean pathExists(boolean[][] obstacles) {
        boolean[][] seen = new boolean[Board.SIZE][Board.SIZE];
        Deque<Point> queue = new ArrayDeque<>();
        queue.add(new Point(0, 0));
        seen[0][0] = true;
        int[][] steps = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!queue.isEmpty()) {
            Point p = queue.poll();
            if (p.y == Board.SIZE - 1) {
                return true;
            }
            for (int[] s : steps) {
                int x = p.x + s[0];
                int y = p.y + s[1];
                if (x >= 0 && x < Board.SIZE && y >= 0 && y < Board.SIZE && !obstacles[x][y] && !seen[x][y]) {
                    seen[x][y] = true;
                    queue.add(new Point(x, y));
                }
            }
        }
        return false;
    }
}
