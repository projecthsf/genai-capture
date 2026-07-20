package io.genai.screenshot.plugin;

import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.awt.event.MouseEvent;

/**
 * A clickable icon in the IDE status bar that triggers a capture. Reused for both
 * the immediate screenshot and the "hide IDE, capture desktop" variants — each
 * factory supplies its own id / icon / tooltip / action.
 */
public class CaptureStatusBarWidget implements StatusBarWidget, StatusBarWidget.IconPresentation {

    static final String SCREENSHOT_ID = "io.genai.screenshot.CaptureWidget";
    static final String DESKTOP_ID = "io.genai.screenshot.CaptureDesktopWidget";

    private final String id;
    private final Icon icon;
    private final String tooltip;
    private final Runnable action;

    public CaptureStatusBarWidget(String id, Icon icon, String tooltip, Runnable action) {
        this.id = id;
        this.icon = icon;
        this.tooltip = tooltip;
        this.action = action;
    }

    @NotNull
    @Override
    public String ID() {
        return id;
    }

    @Nullable
    @Override
    public WidgetPresentation getPresentation() {
        return this;
    }

    @Override
    public void install(@NotNull StatusBar statusBar) { }

    @Override
    public void dispose() { }

    // --- IconPresentation -----------------------------------------------------

    @Nullable
    @Override
    public Icon getIcon() {
        return icon;
    }

    @NotNull
    @Override
    public String getTooltipText() {
        return tooltip;
    }

    @Nullable
    @Override
    public Consumer<MouseEvent> getClickConsumer() {
        return e -> action.run();
    }
}
