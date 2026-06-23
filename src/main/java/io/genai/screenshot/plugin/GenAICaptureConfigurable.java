package io.genai.screenshot.plugin;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

/**
 * "Settings ▸ Tools ▸ GenAI Capture" — the group node. It has no settings of its
 * own; returning {@code null} from {@link #createComponent()} makes the platform
 * render the standard landing page that links to the child pages (Watermark,
 * Toolbar), which are registered with {@code parentId} in plugin.xml.
 */
public class GenAICaptureConfigurable implements Configurable {

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "GenAI Capture";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return null;   // group node → platform shows links to the child pages
    }

    @Override public boolean isModified() { return false; }
    @Override public void apply() { }
    @Override public void reset() { }
}
