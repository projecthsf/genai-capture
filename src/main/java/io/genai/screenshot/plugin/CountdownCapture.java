package io.genai.screenshot.plugin;

import javax.swing.JComponent;
import javax.swing.JWindow;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Shows a big "3 … 2 … 1" countdown in a floating bubble, then captures the
 * screen. The bubble is disposed (and given a beat to clear) before the grab, so
 * it never appears in the screenshot. Click the bubble to cancel.
 */
public final class CountdownCapture {

    private CountdownCapture() {}

    public static void start(int seconds) {
        if (seconds <= 0) { CaptureService.capture(); return; }

        GraphicsConfiguration gc = deviceAtMouse();
        Rectangle b = gc.getBounds();

        final JWindow w = new JWindow(gc);
        w.setAlwaysOnTop(true);
        try { w.setBackground(new Color(0, 0, 0, 0)); } catch (Throwable ignore) { /* no per-pixel alpha */ }

        final Bubble bubble = new Bubble();
        bubble.n = seconds;
        w.setContentPane(bubble);
        int size = 150;
        w.setSize(size, size);
        w.setLocation(b.x + (b.width - size) / 2, b.y + (b.height - size) / 2);

        final Timer[] ticker = new Timer[1];
        final int[] remaining = { seconds };

        bubble.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {   // cancel
                if (ticker[0] != null) ticker[0].stop();
                w.dispose();
            }
        });

        Timer t = new Timer(1000, null);
        ticker[0] = t;
        t.addActionListener(e -> {
            remaining[0]--;
            if (remaining[0] <= 0) {
                t.stop();
                w.dispose();
                // let the screen repaint without the bubble, then grab
                Timer shot = new Timer(160, ev -> CaptureService.capture());
                shot.setRepeats(false);
                shot.start();
            } else {
                bubble.n = remaining[0];
                bubble.repaint();
            }
        });

        w.setVisible(true);
        t.start();
    }

    private static GraphicsConfiguration deviceAtMouse() {
        try {
            Point p = GraphicsEnvironment.isHeadless() ? null
                    : java.awt.MouseInfo.getPointerInfo().getLocation();
            if (p != null) {
                for (GraphicsDevice d : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
                    GraphicsConfiguration c = d.getDefaultConfiguration();
                    if (c.getBounds().contains(p)) return c;
                }
            }
        } catch (Throwable ignore) { /* fall through */ }
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    }

    /** Translucent rounded bubble that paints the current count, centered. */
    private static final class Bubble extends JComponent {
        int n;

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(new Color(0, 0, 0, 180));
                g2.fillRoundRect(0, 0, w, h, 32, 32);
                g2.setColor(Color.WHITE);
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 76f));
                String s = String.valueOf(n);
                FontMetrics fm = g2.getFontMetrics();
                int tx = (w - fm.stringWidth(s)) / 2;
                int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(s, tx, ty);
            } finally {
                g2.dispose();
            }
        }
    }
}
