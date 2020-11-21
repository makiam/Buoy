package buoy.internal;

import buoy.widget.*;
import java.awt.*;
import javax.swing.JPanel;

/**
 * This is a JPanel subclass, which is used internally by various
 * WidgetContainers. It contains a single Widget, and matches its minimum,
 * maximum, and preferred sizes to the Widget.
 *
 * @author Peter Eastman
 */
public class SingleWidgetPanel extends JPanel {

    protected Widget widget;

    public SingleWidgetPanel(Widget widget) {
        super(new BorderLayout());
        this.widget = widget;
        add(widget.getComponent(), BorderLayout.CENTER);
    }

    @Override
    public Dimension getMinimumSize() {
        return widget.getMinimumSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return widget.getMaximumSize();
    }

    @Override
    public Dimension getPreferredSize() {
        return widget.getPreferredSize();
    }
}
