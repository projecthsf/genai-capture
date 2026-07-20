package io.genai.screenshot;

/**
 * Aligns the capture overlay's theme with the host IDE. The desktop app calls
 * {@code ModernUi.loadTheme()} at startup; the plugin has no such startup, so the
 * overlay would otherwise stay on the first registered theme. Lives in the
 * {@code io.genai.screenshot} package so it can reach the package-private
 * {@link ModernUi}.
 */
public final class PluginThemeBridge {

    private PluginThemeBridge() {}

    /** Pick the Dark/Light theme that matches the IDE's current Look&amp;Feel. */
    public static void syncToIde() {
        ModernUi.current = ModernUi.byName(ModernUi.systemThemeName());
    }
}
