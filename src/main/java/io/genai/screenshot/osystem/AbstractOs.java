package io.genai.screenshot.osystem;

import javax.swing.JRootPane;
import javax.swing.RootPaneContainer;
import java.nio.file.Path;

/**
 * Plugin replacement for the desktop app's {@code AbstractOs}.
 *
 * <p>The desktop version uses the Foreign Function &amp; Memory API (CoreGraphics
 * permission checks) and jnativehook (global hotkeys), neither of which loads
 * cleanly inside the IDE's JBR. The annotation overlay only needs three things
 * from the OS layer — system dark-mode detection, the macOS unified title bar,
 * and a user-data directory — so this shim provides just those, FFM-free.
 */
public class AbstractOs {

    private static final boolean MAC =
            System.getProperty("os.name", "").toLowerCase().contains("mac");

    private static AbstractOs current;

    public static AbstractOs current() {
        if (current == null) current = new AbstractOs();
        return current;
    }

    public boolean isMac() { return MAC; }

    /** Follow the OS appearance (used when the user hasn't picked a theme). */
    public boolean isSystemDark() {
        if (!MAC) return false;   // simple default off macOS
        try {
            Process p = new ProcessBuilder("defaults", "read", "-g", "AppleInterfaceStyle")
                    .redirectErrorStream(true).start();
            String out = new String(p.getInputStream().readAllBytes()).trim();
            p.waitFor();
            return out.equalsIgnoreCase("Dark");
        } catch (Exception e) {
            return false;
        }
    }

    /** macOS unified/transparent title bar (plain client properties — no native calls). */
    public void applyWindowChrome(RootPaneContainer window) {
        if (!MAC || window == null) return;
        JRootPane rp = window.getRootPane();
        if (rp == null) return;
        rp.putClientProperty("apple.awt.fullWindowContent", Boolean.TRUE);
        rp.putClientProperty("apple.awt.transparentTitleBar", Boolean.TRUE);
        rp.putClientProperty("apple.awt.windowTitleVisible", Boolean.FALSE);
    }

    /** Where the overlay keeps user themes/languages. */
    public Path userDataDir() {
        String home = System.getProperty("user.home");
        return MAC
                ? Path.of(home, "Library", "Application Support", "genai-screenshot")
                : Path.of(home, ".genai-screenshot");
    }
}
