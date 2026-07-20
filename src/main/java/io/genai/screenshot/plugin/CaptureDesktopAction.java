package io.genai.screenshot.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * "Tools ▸ Capture Desktop (Hide IDE)" — temporarily hide the IDE's windows,
 * grab the desktop behind them, then restore the IDE and open the overlay.
 */
public class CaptureDesktopAction extends AnAction {

    public CaptureDesktopAction() {
        getTemplatePresentation().setIcon(PluginIcons.DESKTOP);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        CaptureService.captureHidingIde();
    }
}
