package robot.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;

import robot.interpreter.BoardBlockWriter;
import robot.interpreter.Interpreter;
import robot.interpreter.ProgramParser;
import robot.model.Board;
import robot.model.RobotException;

/** Hauptfenster: links der DSL-Code, rechts das Schachbrett mit Animation und Protokoll. */
public class RobotGui extends JFrame {

    private static final String DEFAULT_PROGRAM = String.join("\n",
            "// Roboter-DSL (Aufgabe 1d)",
            "// Der Roboter startet auf A1 mit Blick nach Norden.",
            "// Ziel: irgendein Feld der Reihe 8 erreichen.",
            "// Tipp: Klick auf ein Feld setzt/entfernt eine Kiste.",
            "",
            "board {",
            "    obstacle A3",
            "    obstacle B3",
            "    obstacle C3",
            "    obstacle E5",
            "    obstacle F5",
            "    obstacle G5",
            "    obstacle H5",
            "}",
            "",
            "// Strategie: Rechte-Hand-Regel (Wall Follower)",
            "while not atGoal {",
            "    if rightFree {",
            "        turnRight",
            "        forward",
            "    } else {",
            "        if frontFree {",
            "            forward",
            "        } else {",
            "            turnLeft",
            "        }",
            "    }",
            "}",
            "stop",
            "");

    private static final Color OK_COLOR = new Color(0x3B6D11);
    private static final Color ERROR_COLOR = new Color(0xA32D2D);
    private static final Color INFO_COLOR = new Color(0x444441);

    private final Board board = new Board();
    private final JTextArea editor = new JTextArea(DEFAULT_PROGRAM, 30, 42);
    private final BoardPanel boardPanel = new BoardPanel(board);
    private final JTextArea log = new JTextArea(8, 30);
    private final JLabel status = new JLabel(" ");
    private final JLabel positionLabel = new JLabel(" ");
    private final JSlider speed = new JSlider(0, 100, 60);
    private final JSpinner maxSteps = new JSpinner(new SpinnerNumberModel(2000, 10, 1_000_000, 100));

    private final JButton startButton = new JButton("Start");
    private final JButton stepButton = new JButton("Schritt");
    private final JButton pauseButton = new JButton("Pause");
    private final JButton stopButton = new JButton("Stopp");
    private final JButton resetButton = new JButton("Zurücksetzen");
    private final JButton randomButton = new JButton("Zufallsbrett");
    private final JButton clearButton = new JButton("Brett leeren");
    private final JButton openButton = new JButton("Öffnen…");
    private final JButton saveButton = new JButton("Speichern…");

    private final Timer liveCheck;
    private ExecutionControl control;
    private Thread runner;
    private boolean running;
    private boolean resetAfterRun;
    private File currentFile;

    public RobotGui() {
        super("Roboter-DSL – Schachbrett-Simulator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // ----- Editor -----
        editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
        editor.setTabSize(4);
        editor.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        JScrollPane editorScroll = new JScrollPane(editor);
        editorScroll.setRowHeaderView(new LineNumberView(editor));
        JPanel editorPanel = new JPanel(new BorderLayout());
        editorPanel.add(title("Programm"), BorderLayout.NORTH);
        editorPanel.add(editorScroll, BorderLayout.CENTER);

        // ----- Brett + Protokoll -----
        log.setEditable(false);
        log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JPanel boardSide = new JPanel(new BorderLayout());
        JPanel boardHeader = new JPanel(new BorderLayout());
        boardHeader.add(title("Schachbrett"), BorderLayout.WEST);
        positionLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        boardHeader.add(positionLabel, BorderLayout.EAST);
        boardSide.add(boardHeader, BorderLayout.NORTH);
        boardSide.add(boardPanel, BorderLayout.CENTER);
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.add(title("Protokoll"), BorderLayout.NORTH);
        logPanel.add(new JScrollPane(log), BorderLayout.CENTER);
        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, boardSide, logPanel);
        rightSplit.setResizeWeight(0.8);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editorPanel, rightSplit);
        mainSplit.setResizeWeight(0.4);

        // ----- Werkzeugleiste -----
        JPanel toolbar = new JPanel();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
        toolbar.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        toolbar.add(startButton);
        toolbar.add(stepButton);
        toolbar.add(pauseButton);
        toolbar.add(stopButton);
        toolbar.add(resetButton);
        toolbar.add(Box.createHorizontalStrut(16));
        toolbar.add(new JLabel("Tempo "));
        speed.setMaximumSize(new Dimension(140, 30));
        toolbar.add(speed);
        toolbar.add(Box.createHorizontalStrut(12));
        toolbar.add(new JLabel("Max. Schritte "));
        maxSteps.setMaximumSize(new Dimension(90, 30));
        toolbar.add(maxSteps);
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(randomButton);
        toolbar.add(clearButton);
        toolbar.add(Box.createHorizontalStrut(12));
        toolbar.add(openButton);
        toolbar.add(saveButton);

        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.add(status);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(toolbar, BorderLayout.NORTH);
        getContentPane().add(mainSplit, BorderLayout.CENTER);
        getContentPane().add(statusBar, BorderLayout.SOUTH);

        // ----- Aktionen -----
        startButton.addActionListener(e -> onStart());
        stepButton.addActionListener(e -> onStep());
        pauseButton.addActionListener(e -> onPause());
        stopButton.addActionListener(e -> onStop());
        resetButton.addActionListener(e -> onReset());
        randomButton.addActionListener(e -> randomBoard());
        clearButton.addActionListener(e -> writeObstaclesToCode(new ArrayList<Point>()));
        openButton.addActionListener(e -> openFile());
        saveButton.addActionListener(e -> saveFile());
        speed.addChangeListener(e -> {
            if (control != null) {
                control.setDelay(currentDelay());
            }
        });
        boardPanel.setFieldClickHandler(this::toggleObstacle);

        liveCheck = new Timer(300, e -> checkProgram());
        liveCheck.setRepeats(false);
        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                liveCheck.restart();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                liveCheck.restart();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                liveCheck.restart();
            }
        });

        checkProgram();
        updateControls();
        pack();
        setSize(1250, 800);
        setLocationRelativeTo(null);
    }

    private static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        label.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        return label;
    }

    // ---------- Programm prüfen ----------

    /** Prüft den Code, markiert Fehler und übernimmt die Hindernisse auf das Brett. */
    private ProgramParser.Result checkProgram() {
        ProgramParser.Result result = ProgramParser.parse(editor.getText());
        editor.getHighlighter().removeAllHighlights();
        if (result.isValid()) {
            if (!running) {
                applyObstacles(result.obstacles);
                board.resetRobot();
                boardPanel.repaint();
                updatePosition();
            }
            setStatus("Programm ist gültig. " + result.obstacles.size() + " Hindernis(se) auf dem Brett.", OK_COLOR);
        } else {
            ProgramParser.Problem first = result.problems.get(0);
            highlightLine(first.line);
            String more = result.problems.size() > 1 ? "  (+" + (result.problems.size() - 1) + " weitere)" : "";
            setStatus("Fehler in " + first + more, ERROR_COLOR);
        }
        return result;
    }

    private void highlightLine(int line) {
        try {
            int index = Math.max(0, Math.min(line - 1, editor.getLineCount() - 1));
            int start = editor.getLineStartOffset(index);
            int end = editor.getLineEndOffset(index);
            editor.getHighlighter().addHighlight(start, end,
                    new DefaultHighlighter.DefaultHighlightPainter(new Color(255, 200, 200)));
        } catch (BadLocationException ignored) {
            // Zeile existiert nicht (mehr)
        }
    }

    private void applyObstacles(List<Point> obstacles) {
        board.clearObstacles();
        for (Point p : obstacles) {
            board.setObstacle(p.x, p.y, true);
        }
    }

    // ---------- Ausführung ----------

    private void onStart() {
        if (running) {
            control.resume();
            updateControls();
            return;
        }
        startRun(false);
    }

    private void onStep() {
        if (running) {
            control.step();
            updateControls();
            return;
        }
        startRun(true);
    }

    private void onPause() {
        if (running) {
            control.pause();
            updateControls();
            setStatus("Pausiert – mit „Start“ weiter oder mit „Schritt“ einzeln fortfahren.", INFO_COLOR);
        }
    }

    private void onStop() {
        if (running) {
            control.cancel();
            runner.interrupt();
        }
    }

    private void onReset() {
        log.setText("");
        if (running) {
            resetAfterRun = true;
            onStop();
            return;
        }
        checkProgram();
    }

    private void startRun(boolean paused) {
        liveCheck.stop();
        ProgramParser.Result result = checkProgram();
        if (!result.isValid()) {
            StringBuilder sb = new StringBuilder("Das Programm enthält Fehler:\n\n");
            for (ProgramParser.Problem p : result.problems) {
                sb.append("• ").append(p).append('\n');
            }
            JOptionPane.showMessageDialog(this, sb.toString(), "Syntaxfehler", JOptionPane.ERROR_MESSAGE);
            return;
        }

        applyObstacles(result.obstacles);
        board.resetRobot();
        log.setText("Start auf " + board.position() + "\n");
        boardPanel.repaint();
        updatePosition();

        control = new ExecutionControl(this::onRobotStep, currentDelay(), paused);
        final int limit = (Integer) maxSteps.getValue();
        final ProgramParser.Result program = result;
        runner = new Thread(() -> {
            String message;
            Color color;
            try {
                int steps = new Interpreter(board, control, limit).run(program.tree);
                if (board.atGoal()) {
                    message = "Ziel erreicht! Reihe 8 nach " + steps + " Befehlen (" + board.position() + ").";
                    color = OK_COLOR;
                } else {
                    message = "Programm beendet nach " + steps + " Befehlen – Reihe 8 wurde nicht erreicht.";
                    color = ERROR_COLOR;
                }
            } catch (Interpreter.CancelledException ex) {
                message = "Ausführung abgebrochen.";
                color = INFO_COLOR;
            } catch (RobotException ex) {
                message = "Laufzeitfehler: " + ex.getMessage();
                color = ERROR_COLOR;
            } catch (RuntimeException ex) {
                message = "Unerwarteter Fehler: " + ex;
                color = ERROR_COLOR;
            }
            final String finalMessage = message;
            final Color finalColor = color;
            SwingUtilities.invokeLater(() -> finishRun(finalMessage, finalColor));
        }, "robot-interpreter");
        runner.setDaemon(true);
        running = true;
        updateControls();
        setStatus(paused ? "Einzelschritt-Modus – „Schritt“ führt den nächsten Befehl aus." : "Läuft …", INFO_COLOR);
        runner.start();
    }

    /** Wird nach jedem Befehl im Interpreter-Thread aufgerufen. */
    private void onRobotStep(int stepNumber, String command, Board b) {
        final String line = String.format("%4d  %-10s → %s%n", stepNumber, command, b.position());
        SwingUtilities.invokeLater(() -> {
            log.append(line);
            log.setCaretPosition(log.getDocument().getLength());
            boardPanel.repaint();
            updatePosition();
        });
    }

    private void finishRun(String message, Color color) {
        running = false;
        if (resetAfterRun) {
            resetAfterRun = false;
            log.setText("");
            updateControls();
            checkProgram();
            return;
        }
        log.append(message + "\n");
        setStatus(message, color);
        boardPanel.repaint();
        updatePosition();
        updateControls();
    }

    private int currentDelay() {
        return (100 - speed.getValue()) * 10;
    }

    // ---------- Brett bearbeiten ----------

    private void toggleObstacle(int fx, int fy) {
        if (running) {
            return;
        }
        if (Board.isStart(fx, fy)) {
            setStatus("Auf A1 startet der Roboter – dort kann keine Kiste stehen.", ERROR_COLOR);
            return;
        }
        List<Point> obstacles = board.getObstacles();
        Point p = new Point(fx, fy);
        if (obstacles.contains(p)) {
            obstacles.remove(p);
        } else {
            obstacles.add(p);
        }
        writeObstaclesToCode(obstacles);
    }

    private void randomBoard() {
        if (running) {
            return;
        }
        Random random = new Random();
        List<Point> obstacles = new ArrayList<>();
        int count = 10 + random.nextInt(8);
        while (obstacles.size() < count) {
            Point p = new Point(random.nextInt(Board.SIZE), random.nextInt(Board.SIZE));
            if (!Board.isStart(p.x, p.y) && !obstacles.contains(p)) {
                obstacles.add(p);
            }
        }
        writeObstaclesToCode(obstacles);
    }

    /** Schreibt die Hindernisse als board-Block in den Code (der Code bleibt die einzige Quelle). */
    private void writeObstaclesToCode(List<Point> obstacles) {
        if (running) {
            return;
        }
        String newCode = BoardBlockWriter.replaceBoardBlock(editor.getText(), obstacles);
        int caret = Math.min(editor.getCaretPosition(), newCode.length());
        editor.setText(newCode);
        editor.setCaretPosition(caret);
        liveCheck.stop();
        checkProgram();
    }

    // ---------- Dateien ----------

    private JFileChooser chooser() {
        File dir = currentFile != null ? currentFile.getParentFile() : new File("beispiele");
        JFileChooser chooser = new JFileChooser(dir.isDirectory() ? dir : new File("."));
        chooser.setFileFilter(new FileNameExtensionFilter("Roboter-Programme (*.robot)", "robot"));
        return chooser;
    }

    private void openFile() {
        if (running) {
            return;
        }
        JFileChooser chooser = chooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                editor.setText(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
                editor.setCaretPosition(0);
                currentFile = file;
                setTitle("Roboter-DSL – " + file.getName());
                log.setText("");
                liveCheck.stop();
                checkProgram();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Datei konnte nicht gelesen werden:\n" + ex.getMessage(),
                        "Fehler", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveFile() {
        JFileChooser chooser = chooser();
        if (currentFile != null) {
            chooser.setSelectedFile(currentFile);
        }
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().contains(".")) {
                file = new File(file.getParentFile(), file.getName() + ".robot");
            }
            try {
                Files.write(file.toPath(), editor.getText().getBytes(StandardCharsets.UTF_8));
                currentFile = file;
                setTitle("Roboter-DSL – " + file.getName());
                setStatus("Gespeichert: " + file.getName(), OK_COLOR);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Datei konnte nicht gespeichert werden:\n" + ex.getMessage(),
                        "Fehler", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ---------- Anzeige ----------

    private void updateControls() {
        boolean paused = running && control.isPaused();
        startButton.setText(paused ? "Weiter" : "Start");
        startButton.setEnabled(!running || paused);
        stepButton.setEnabled(!running || paused);
        pauseButton.setEnabled(running && !paused);
        stopButton.setEnabled(running);
        editor.setEditable(!running);
        boardPanel.setEditable(!running);
        randomButton.setEnabled(!running);
        clearButton.setEnabled(!running);
        openButton.setEnabled(!running);
    }

    private void updatePosition() {
        positionLabel.setText("Roboter: " + board.position());
    }

    private void setStatus(String text, Color color) {
        status.setText(text);
        status.setForeground(color);
    }

    // ---------- Start ----------

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Standard-Look verwenden
            }
            new RobotGui().setVisible(true);
        });
    }

    public static void main(String[] args) {
        launch();
    }
}
