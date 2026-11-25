package robot.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Einfache Zeilennummern-Leiste für den Code-Editor. */
public class LineNumberView extends JComponent {

    private final JTextArea editor;

    public LineNumberView(JTextArea editor) {
        this.editor = editor;
        setFont(editor.getFont());
        setForeground(new Color(0x888780));
        setBackground(new Color(0xF1EFE8));
        setOpaque(true);
        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update();
            }
        });
    }

    private void update() {
        revalidate();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        int digits = Math.max(2, String.valueOf(editor.getLineCount()).length());
        int width = fm.charWidth('0') * digits + 14;
        return new Dimension(width, editor.getPreferredSize().height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setFont(getFont());
        g.setColor(getForeground());
        FontMetrics fm = g.getFontMetrics();
        Insets insets = editor.getInsets();
        int lineHeight = fm.getHeight();
        for (int i = 0; i < editor.getLineCount(); i++) {
            String text = String.valueOf(i + 1);
            int y = insets.top + i * lineHeight + fm.getAscent();
            g.drawString(text, getWidth() - fm.stringWidth(text) - 6, y);
        }
    }
}
