// Generated from src/robot/Robot.g4 by ANTLR 4.13.2
package robot.parser;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link RobotParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface RobotVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link RobotParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(RobotParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link RobotParser#board}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBoard(RobotParser.BoardContext ctx);
	/**
	 * Visit a parse tree produced by {@link RobotParser#obstacle}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObstacle(RobotParser.ObstacleContext ctx);
	/**
	 * Visit a parse tree produced by the {@code CommandStatement}
	 * labeled alternative in {@link RobotParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommandStatement(RobotParser.CommandStatementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code WhileStatement}
	 * labeled alternative in {@link RobotParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhileStatement(RobotParser.WhileStatementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code IfStatement}
	 * labeled alternative in {@link RobotParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfStatement(RobotParser.IfStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link RobotParser#block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock(RobotParser.BlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link RobotParser#command}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommand(RobotParser.CommandContext ctx);
	/**
	 * Visit a parse tree produced by the {@code NotCondition}
	 * labeled alternative in {@link RobotParser#condition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNotCondition(RobotParser.NotConditionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code SensorCondition}
	 * labeled alternative in {@link RobotParser#condition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSensorCondition(RobotParser.SensorConditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link RobotParser#sensor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSensor(RobotParser.SensorContext ctx);
}