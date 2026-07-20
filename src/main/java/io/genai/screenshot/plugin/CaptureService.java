package io.genai.screenshot.plugin;

import com.intellij.openapi.ui.Messages;
import io.genai.screenshot.CaptureOverlay;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.AWTException;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared capture logic for the menu / status-bar actions: grab the whole
 * (multi-monitor) desktop and open the GenAI Capture annotation overlay (reused
 * from the desktop app).
 *
 * <p>On macOS the IDE needs Screen Recording permission (System Settings ▸
 * Privacy &amp; Security ▸ Screen Recording) or the capture comes back black.
 */
public final class CaptureService {

    /** Time to let the screen repaint after toggling window visibility, before the grab. */
    private static final int SETTLE_MS = 220;

    private CaptureService() {}

    /** Capture the screen now (IDE included) and open the annotation overlay. */
    public static void capture() {
        final Rectangle virtual = virtualBounds();
        final BufferedImage shot = grab(virtual);
        if (shot != null) openOverlay(shot, virtual, null);
    }

    /**
     * Hide the IDE's visible windows, capture the desktop behind them, and open
     * the overlay. The IDE stays hidden for the whole annotation session and is
     * restored only when the overlay closes (ESC / save / copy / pin / close),
     * so it never reappears over your work.
     */
    public static void captureHidingIde() {
        final List<Window> hidden = new ArrayList<>();
        for (Window w : Window.getWindows()) {
            if (w.isVisible() && w.isShowing()) {
                hidden.add(w);
                w.setVisible(false);
            }
        }
        if (hidden.isEmpty()) { capture(); return; }

        final Runnable restore = () -> restore(hidden);

        // Give the compositor a beat to redraw without the IDE, then grab.
        Timer t = new Timer(SETTLE_MS, e -> {
            final Rectangle virtual = virtualBounds();
            final BufferedImage shot;
            try {
                shot = new Robot().createScreenCapture(virtual);
            } catch (AWTException ex) {
                restore.run();
                Messages.showErrorDialog("Could not capture the screen: " + ex.getMessage(), "GenAI Capture");
                return;
            }
            // Keep the IDE hidden; bring it back when the overlay is dismissed.
            openOverlay(shot, virtual, restore);
        });
        t.setRepeats(false);
        t.start();
    }

    private static void restore(List<Window> windows) {
        for (Window w : windows) {
            if (w.isDisplayable()) w.setVisible(true);
        }
    }

    private static BufferedImage grab(Rectangle r) {
        try {
            return new Robot().createScreenCapture(r);
        } catch (AWTException ex) {
            Messages.showErrorDialog("Could not capture the screen: " + ex.getMessage(), "GenAI Capture");
            return null;
        }
    }

    /** Open the overlay on the EDT; {@code onClose} (may be null) runs when it's dismissed. */
    private static void openOverlay(BufferedImage shot, Rectangle virtual, Runnable onClose) {
        final Runnable cb = (onClose != null) ? onClose : () -> { };
        SwingUtilities.invokeLater(() -> {
            io.genai.screenshot.PluginThemeBridge.syncToIde();   // match the IDE's dark/light theme
            new CaptureOverlay(shot, virtual, cb);
        });
    }

    /** Union of every screen device's bounds (handles multiple monitors). */
    private static Rectangle virtualBounds() {
        Rectangle bounds = new Rectangle();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (GraphicsDevice gd : ge.getScreenDevices()) {
            for (GraphicsConfiguration gc : gd.getConfigurations()) {
                bounds = bounds.union(gc.getBounds());
            }
        }
        if (bounds.isEmpty()) {
            bounds = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        }
        return bounds;
    }
}
