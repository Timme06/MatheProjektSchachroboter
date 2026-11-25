package robot.interpreter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Point;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public class BoardBlockWriterTest {

    @Test
    public void replacesExistingBlockAndKeepsProgram() {
        String code = "board {\n    obstacle C3\n}\n\nforward\n";
        String result = BoardBlockWriter.replaceBoardBlock(code, Arrays.asList(new Point(4, 5), new Point(1, 1)));
        assertEquals("board {\n    obstacle B2\n    obstacle E6\n}\n\nforward\n", result);
    }

    @Test
    public void addsBlockWhenMissing() {
        String result = BoardBlockWriter.replaceBoardBlock("forward", Collections.singletonList(new Point(2, 2)));
        assertTrue(result.startsWith("board {\n    obstacle C3\n}"));
        assertTrue(result.endsWith("forward"));
    }

    @Test
    public void ignoresBoardTextInsideComments() {
        String code = "// board { obstacle H8 }\nboard { obstacle C3 }\nforward";
        String result = BoardBlockWriter.replaceBoardBlock(code, Collections.<Point>emptyList());
        assertTrue(result.startsWith("// board { obstacle H8 }\nboard {\n}"));
    }

    @Test
    public void resultIsStillAValidProgram() {
        String result = BoardBlockWriter.replaceBoardBlock("while not atGoal { forward }",
                Arrays.asList(new Point(3, 3), new Point(5, 6)));
        assertTrue(ProgramParser.parse(result).isValid());
    }
}
