package io.genai.screenshot.plugin;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import io.genai.screenshot.Icons;
import io.genai.screenshot.ToolbarConfig;
import io.genai.screenshot.toolbar.Tool;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * "Settings ▸ Tools ▸ GenAI Capture ▸ Toolbar" — choose which annotation tools
 * appear on the capture toolbar (click to toggle) and reorder them with the
 * up/down buttons. Native IntelliJ list rendering; saves to the shared
 * {@link ToolbarConfig} preferences immediately (effective on the next capture).
 */
public class ToolbarConfigurable implements Configurable {

    private JComponent root;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Toolbar";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        if (root != null) return root;

        DefaultListModel<Tool> model = new DefaultListModel<>();
        for (Tool t : ToolbarConfig.order()) model.addElement(t);
        Set<Tool> enabled = new LinkedHashSet<>(ToolbarConfig.enabled());

        Runnable save = () -> {
            List<Tool> order = new ArrayList<>();
            for (int i = 0; i < model.size(); i++) order.add(model.get(i));
            ToolbarConfig.save(order, enabled);
        };

        JBList<Tool> list = new JBList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer((lst, t, i, sel, focus) -> {
            JBLabel l = new JBLabel((enabled.contains(t) ? "✓   " : "      ") + ToolbarConfig.label(t),
                    Icons.tool(t, 16), SwingConstants.LEFT);
            l.setIconTextGap(8);
            l.setBorder(JBUI.Borders.empty(4, 8));
            l.setOpaque(true);
            l.setBackground(sel ? UIUtil.getListSelectionBackground(focus) : UIUtil.getListBackground());
            l.setForeground(sel ? UIUtil.getListSelectionForeground(focus) : UIUtil.getListForeground());
            return l;
        });
        list.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int i = list.locationToIndex(e.getPoint());
                if (i < 0 || !list.getCellBounds(i, i).contains(e.getPoint())) return;
                Tool t = model.get(i);
                if (!enabled.add(t)) enabled.remove(t);
                list.repaint();
                save.run();
            }
        });

        AnActionButtonRunnable moveUp = b -> move(list, model, -1, save);
        AnActionButtonRunnable moveDown = b -> move(list, model, 1, save);

        JComponent listPanel = ToolbarDecorator.createDecorator(list)
                .setMoveUpAction(moveUp)
                .setMoveDownAction(moveDown)
                .disableAddAction()
                .disableRemoveAction()
                .createPanel();

        JBLabel hint = new JBLabel("<html>Click a tool to show or hide it on the capture toolbar. "
                + "Use the up/down buttons to reorder. Hidden tools stay available under the toolbar's "
                + "“more” menu.</html>");
        hint.setComponentStyle(UIUtil.ComponentStyle.SMALL);
        hint.setForeground(UIUtil.getContextHelpForeground());
        hint.setBorder(JBUI.Borders.emptyBottom(8));

        root = new JPanel(new BorderLayout());
        root.setBorder(JBUI.Borders.empty(12));
        root.add(hint, BorderLayout.NORTH);
        root.add(listPanel, BorderLayout.CENTER);
        return root;
    }

    private static void move(JBList<Tool> list, DefaultListModel<Tool> model, int delta, Runnable save) {
        int i = list.getSelectedIndex();
        int j = i + delta;
        if (i < 0 || j < 0 || j >= model.size()) return;
        Tool t = model.remove(i);
        model.add(j, t);
        list.setSelectedIndex(j);
        save.run();
    }

    @Override public boolean isModified() { return false; } // saves live
    @Override public void apply() throws ConfigurationException { }
    @Override public void reset() { }

    @Override
    public void disposeUIResources() {
        root = null;
    }
}
