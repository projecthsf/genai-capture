package io.genai.screenshot;

import javax.swing.JMenuBar;

/**
 * Plugin shim for the desktop app's {@code ScreenshotApp}. The pin window asks
 * for a menu bar (so the app could trigger another capture from a pin); inside
 * the IDE the File ▸ Screenshot action covers that, so the pin window gets no
 * menu bar.
 */
public final class ScreenshotApp {
    private ScreenshotApp() { }

    /** No menu bar on pinned windows inside the IDE. */
    public static JMenuBar newMenuBar() { return null; }
}
