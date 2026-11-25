package robot.interpreter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ProgramParserTest {

    @Test
    public void validProgramWithNestingParses() {
        ProgramParser.Result r = ProgramParser.parse(
                "board { obstacle C3 }\n"
                + "while not atGoal { if frontFree { forward } else { turnRight forward turnLeft } }\nstop");
        assertTrue(r.problems.toString(), r.isValid());
        assertEquals(1, r.obstacles.size());
    }

    @Test
    public void emptyProgramIsValid() {
        assertTrue(ProgramParser.parse("").isValid());
    }

    @Test
    public void missingClosingBraceIsReported() {
        ProgramParser.Result r = ProgramParser.parse("while atGoal { forward");
        assertFalse(r.isValid());
    }

    @Test
    public void typoIsReportedWithLine() {
        ProgramParser.Result r = ProgramParser.parse("forward\nforwrd");
        assertFalse(r.isValid());
        assertEquals(2, r.problems.get(0).line);
    }

    @Test
    public void fieldOutsideBoardIsRejected() {
        assertFalse(ProgramParser.parse("board { obstacle A9 }").isValid());
    }

    @Test
    public void obstacleOnStartFieldIsRejected() {
        ProgramParser.Result r = ProgramParser.parse("board { obstacle A1 }");
        assertFalse(r.isValid());
        assertTrue(r.problems.get(0).message.contains("A1"));
    }

    @Test
    public void elseWithoutIfIsRejected() {
        assertFalse(ProgramParser.parse("else { forward }").isValid());
    }

    @Test
    public void commentsAndWhitespaceAreIgnored() {
        assertTrue(ProgramParser.parse("// nur ein Kommentar\n\n   forward   // vor\n").isValid());
    }
}
