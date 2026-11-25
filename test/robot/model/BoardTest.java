package robot.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Point;

import org.junit.Test;

public class BoardTest {

    @Test
    public void robotStartsOnA1FacingNorth() {
        Board board = new Board();
        assertEquals(0, board.getX());
        assertEquals(0, board.getY());
        assertEquals(Direction.NORTH, board.getDirection());
    }

    @Test
    public void boardEdgeCountsAsWall() {
        Board board = new Board();
        assertFalse("links von A1 ist der Rand", board.leftFree());
        assertTrue(board.frontFree());
        assertTrue(board.rightFree());
    }

    @Test
    public void sensorsAreRelativeToDirection() {
        Board board = new Board();
        board.setObstacle(1, 0, true);   // B1, also rechts vom Roboter
        assertFalse(board.rightFree());
        board.turnRight();               // jetzt Blick nach Osten
        assertFalse(board.frontFree());
        assertFalse("rechts = Süden = Rand", board.rightFree());
    }

    @Test(expected = RobotException.class)
    public void forwardIntoObstacleIsCollision() {
        Board board = new Board();
        board.setObstacle(0, 1, true);   // A2
        board.forward();
    }

    @Test
    public void startFieldCannotHoldObstacle() {
        Board board = new Board();
        board.setObstacle(0, 0, true);
        assertTrue(board.getObstacles().isEmpty());
    }

    @Test
    public void fieldNotationRoundTrip() {
        assertEquals("C4", Board.fieldName(2, 3));
        assertEquals(new Point(2, 3), Board.parseField("c4"));
    }

    @Test
    public void fourRightTurnsReturnToStartDirection() {
        Direction d = Direction.NORTH;
        for (int i = 0; i < 4; i++) {
            d = d.right();
        }
        assertEquals(Direction.NORTH, d);
        assertEquals(Direction.WEST, Direction.NORTH.left());
    }
}
