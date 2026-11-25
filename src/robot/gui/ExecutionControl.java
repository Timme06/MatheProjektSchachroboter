package robot.gui;

import robot.interpreter.Interpreter;
import robot.model.Board;

/**
 * Steuert die Geschwindigkeit der Ausführung: Pause, Einzelschritt, Abbruch.
 * Der Interpreter läuft in einem eigenen Thread und wartet hier nach jedem Befehl.
 */
public class ExecutionControl implements Interpreter.StepListener {

    /** Wird nach jedem Schritt aufgerufen (im Interpreter-Thread). */
    public interface StepCallback {
        void onStep(int stepNumber, String command, Board board);
    }

    private final StepCallback callback;
    private volatile int delayMs;
    private boolean paused;
    private int stepPermits;
    private boolean cancelled;

    public ExecutionControl(StepCallback callback, int delayMs, boolean startPaused) {
        this.callback = callback;
        this.delayMs = delayMs;
        this.paused = startPaused;
    }

    @Override
    public void afterStep(int stepNumber, String command, Board board) {
        callback.onStep(stepNumber, command, board);
        synchronized (this) {
            long end = System.currentTimeMillis() + delayMs;
            try {
                while (true) {
                    if (cancelled) {
                        throw new Interpreter.CancelledException();
                    }
                    if (paused) {
                        if (stepPermits > 0) {
                            stepPermits--;
                            return;
                        }
                        wait();
                    } else {
                        long remaining = end - System.currentTimeMillis();
                        if (remaining <= 0) {
                            return;
                        }
                        wait(remaining);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new Interpreter.CancelledException();
            }
        }
    }

    public void setDelay(int delayMs) {
        this.delayMs = delayMs;
    }

    public synchronized void pause() {
        paused = true;
        notifyAll();
    }

    public synchronized void resume() {
        paused = false;
        notifyAll();
    }

    public synchronized void step() {
        paused = true;
        stepPermits++;
        notifyAll();
    }

    public synchronized void cancel() {
        cancelled = true;
        notifyAll();
    }

    public synchronized boolean isPaused() {
        return paused;
    }
}
