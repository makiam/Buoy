package buoy.widget;

import buoy.event.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;

/**
 * A BWindow is a WidgetContainer corresponding to an undecorated window. It has
 * no title bar or pulldown menus. It may contain a single Widget (usually a
 * WidgetContainer of some sort) which fills the window.
 * <p>
 * In addition to the event types generated by all Widgets, BWindows generate
 * the following event types:
 * <ul>
 * <li>{@link buoy.event.RepaintEvent RepaintEvent}</li>
 * <li>{@link buoy.event.WindowActivatedEvent WindowActivatedEvent}</li>
 * <li>{@link buoy.event.WindowClosingEvent WindowClosingEvent}</li>
 * <li>{@link buoy.event.WindowDeactivatedEvent WindowDeactivatedEvent}</li>
 * <li>{@link buoy.event.WindowDeiconifiedEvent WindowDeiconifiedEvent}</li>
 * <li>{@link buoy.event.WindowIconifiedEvent WindowIconifiedEvent}</li>
 * <li>{@link buoy.event.WindowResizedEvent WindowResizedEvent}</li>
 * </ul>
 *
 * @author Peter Eastman
 */
public class BWindow extends WindowWidget {

    /**
     * Create a new BWindow.
     */

    public BWindow() {
        component = createComponent();
        getComponent().getContentPane().setLayout(null);
    }

    /**
     * Create the JWindow which serves as this Widget's Component. This method
     * is protected so that subclasses can override it.
     */
    protected JWindow createComponent() {
        return new BWindowComponent();
    }

    @Override
    public JWindow getComponent() {
        return (JWindow) component;
    }

    /**
     * Get the number of children in this container.
     */
    @Override
    public int getChildCount() {
        return (content == null ? 0 : 1);
    }

    /**
     * Get a Collection containing all child Widgets of this container.
     */
    @Override
    public Collection<Widget> getChildren() {
        ArrayList<Widget> ls = new ArrayList<Widget>(1);
        if (content != null) {
            ls.add(content);
        }
        return ls;
    }

    /**
     * Remove a child Widget from this container.
     */
    @Override
    public void remove(Widget widget) {
        if (content == widget) {
            getComponent().getContentPane().remove(widget.getComponent());
            removeAsParent(content);
            content = null;
        }
    }

    /**
     * Remove all child Widgets from this container.
     */
    @Override
    public void removeAll() {
        if (content != null) {
            remove(content);
        }
    }

    /**
     * Get the JRootPane for this Widget's component.
     */
    @Override
    protected JRootPane getRootPane() {
        return getComponent().getRootPane();
    }

    /**
     * This is the JWindow subclass which is used as the Component for a
     * BWindow.
     */
    private class BWindowComponent extends JWindow {

        public BWindowComponent() {
            super();
        }

        public void paintComponent(Graphics g) {
            BWindow.this.dispatchEvent(new RepaintEvent(BWindow.this, (Graphics2D) g));
        }

        @Override
        public void validate() {
            super.validate();
            layoutChildren();
            if (!BWindow.this.getComponent().getSize().equals(lastSize)) {
                lastSize = BWindow.this.getComponent().getSize();
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        BWindow.this.dispatchEvent(new WindowResizedEvent(BWindow.this));
                    }
                });
            }
        }
    }
}
