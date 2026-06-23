package io.genai.screenshot;

import java.awt.Window;

/**
 * Plugin shim for the desktop app's {@code CaptureController}. In the app this
 * hid the app's own windows during a capture; inside the IDE there are no such
 * windows to manage, so the pin window's calls become no-ops.
 */
public final class CaptureController {
    private CaptureController() { }

    public static void addManagedWindow(Window w) { /* no-op in the plugin */ }

    public static void removeManagedWindow(Window w) { /* no-op in the plugin */ }
}
