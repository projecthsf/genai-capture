package io.genai.screenshot.plugin;

import com.intellij.util.ui.UIUtil;
import io.genai.screenshot.Icons;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Icons for the capture actions:
 * <ul>
 *   <li>{@link #CAMERA} — the app's own brand camera ({@code app_camera.png}),
 *       so it matches the desktop app and dock icon.</li>
 *   <li>{@link #DESKTOP} — a theme-adaptive monitor glyph for "capture the
 *       desktop behind the IDE".</li>
 * </ul>
 */
public final class PluginIcons {

    public static final Icon CAMERA = new CameraIcon();
    public static final Icon DESKTOP = new MonitorIcon();

    private PluginIcons() {}

    /** A simple monitor outline, drawn in the component's foreground colour. */
    private static final class MonitorIcon implements Icon {
        private static final int SIZE = 16;

        @Override public int getIconWidth() { return SIZE; }
        @Override public int getIconHeight() { return SIZE; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fg = (c != null && c.getForeground() != null) ? c.getForeground() : UIUtil.getLabelForeground();
                g2.setColor(fg);
                g2.setStroke(new BasicStroke(1.3f));
                g2.translate(x, y);
                g2.drawRoundRect(1, 2, 14, 9, 2, 2);   // screen
                g2.drawLine(8, 11, 8, 13);             // neck
                g2.drawLine(5, 13, 11, 13);            // base
            } finally {
                g2.dispose();
            }
        }
    }

    private static final class CameraIcon implements Icon {
        private static final int SIZE = 16;
        // Loaded at 2× and drawn down to 16 logical px so it stays crisp on HiDPI displays.
        private static final BufferedImage IMG = Icons.camera(SIZE * 2);

        @Override public int getIconWidth() { return SIZE; }
        @Override public int getIconHeight() { return SIZE; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY);
                g2.drawImage(IMG, x, y, SIZE, SIZE, null);
            } finally {
                g2.dispose();
            }
        }
    }
}
