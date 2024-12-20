package buoy.widget;

import buoy.event.*;
import buoy.internal.*;
import buoy.xml.*;
import buoy.xml.delegate.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/**
 * BTabbedPane is a WidgetContainer which arranges its child Widgets in a row.
 * <p>
 * In addition to the event types generated by all Widgets, BTabbedPanes
 * generate the following event types:
 * <ul>
 * <li>{@link buoy.event.SelectionChangedEvent SelectionChangedEvent}</li>
 * </ul>
 *
 * @author Peter Eastman
 */
public class BTabbedPane extends WidgetContainer<JTabbedPane> {

    private final List<Widget<?>> child;
    private int suppressEvents;

    public static final TabPosition TOP = new TabPosition(SwingConstants.TOP);
    public static final TabPosition LEFT = new TabPosition(SwingConstants.LEFT);
    public static final TabPosition BOTTOM = new TabPosition(SwingConstants.BOTTOM);
    public static final TabPosition RIGHT = new TabPosition(SwingConstants.RIGHT);

    static {
        WidgetEncoder.setPersistenceDelegate(TabPosition.class, new StaticFieldDelegate(BTabbedPane.class));
        WidgetEncoder.setPersistenceDelegate(BTabbedPane.class, new IndexedContainerDelegate(new String[]{"getChild", "getTabName", "getTabImage"}));
    }

    /**
     * Create a new BTabbedPane with the tabs along the top.
     */
    public BTabbedPane() {
        this(TOP);
    }

    /**
     * Create a new TabbedContainer.
     *
     * @param pos the position for the tabs (TOP, LEFT, BOTTOM, or RIGHT)
     */
    public BTabbedPane(TabPosition pos) {
        component = createComponent(pos);
        component.addChangeListener(ev -> {
            if (suppressEvents == 0) {
                dispatchEvent(new SelectionChangedEvent(BTabbedPane.this));
            }
        });
        component.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent ev) {
                SwingUtilities.invokeLater(() -> layoutChildren());
            }
        });
        child = new ArrayList<>();
    }

    /**
     * Create the JTabbedPane which serves as this Widget's Component. This
     * method is protected so that subclasses can override it.
     *
     * @param pos the position for the tabs (TOP, LEFT, BOTTOM, or RIGHT)
     *
     */
    protected JTabbedPane createComponent(TabPosition pos) {
        return new JTabbedPane(pos.value);
    }

    /**
     * Get the number of children in this container.
     */
    @Override
    public int getChildCount() {
        return child.size();
    }

    /**
     * Get the i'th child of this container.
     */
    public Widget getChild(int i) {
        return child.get(i);
    }

    /**
     * Get a Collection containing all child Widgets of this container.
     */
    @Override
    public Collection<Widget<?>> getChildren() {
        return new ArrayList<>(child);
    }

    /**
     * Layout the child Widgets. This may be invoked whenever something has
     * changed (the size of this WidgetContainer, the preferred size of one of
     * its children, etc.) that causes the layout to no longer be correct. If a
     * child is itself a WidgetContainer, its layoutChildren() method will be
     * called in turn.
     */
    @Override
    public void layoutChildren() {
        component.validate();
        for (Widget w : child) {
            if (w instanceof WidgetContainer) {
                ((WidgetContainer) w).layoutChildren();
            }
        }
    }

    /**
     * Add a Widget to this container.
     *
     * @param widget the Widget to add
     * @param tabName the name to display on the tab
     */
    public void add(Widget widget, String tabName) {
        add(widget, tabName, null, child.size());
    }

    /**
     * Add a Widget to this container.
     *
     * @param widget the Widget to add
     * @param tabName the name to display on the tab
     * @param image the image to display on the tab
     */
    public void add(Widget widget, String tabName, Icon image) {
        add(widget, tabName, image, child.size());
    }

    /**
     * Add a Widget to this container.
     *
     * @param widget the Widget to add
     * @param tabName the name to display on the tab
     * @param image the image to display on the tab
     * @param index the position at which to add this tab
     */
    public void add(Widget widget, String tabName, Icon image, int index) {
        if (widget.getParent() != null) {
            widget.getParent().remove(widget);
        }
        child.add(index, widget);
        component.insertTab(tabName, image, new SingleWidgetPanel(widget), null, index);
        setAsParent(widget);
        invalidateSize();
    }

    /**
     * Remove a child Widget from this container.
     *
     * @param widget the Widget to remove
     */
    @Override
    public void remove(Widget widget) {
        int index = child.indexOf(widget);
        if (index > -1) {
            remove(index);
        }
    }

    /**
     * Remove a child Widget from this container.
     *
     * @param index the index of the Widget to remove
     */
    public void remove(int index) {
        Widget w = child.get(index);
        component.remove(index);
        child.remove(index);
        removeAsParent(w);
        invalidateSize();
    }

    /**
     * Remove all child Widgets from this container.
     */
    @Override
    public void removeAll() {
        component.removeAll();
        for (Widget aChild : child) {
            removeAsParent(aChild);
        }
        child.clear();
        invalidateSize();
    }

    /**
     * Get the index of a particular Widget.
     *
     * @param widget the Widget to locate
     * @return the position of the Widget within this container
     */
    public int getChildIndex(Widget widget) {
        return child.indexOf(widget);
    }

    /**
     * Get the position of the tabs (TOP, LEFT, BOTTOM, or RIGHT).
     */
    public TabPosition getTabPosition() {
        switch (component.getTabPlacement()) {
            case SwingConstants.TOP:
                return TOP;
            case SwingConstants.LEFT:
                return LEFT;
            case SwingConstants.BOTTOM:
                return BOTTOM;
            default:
                return RIGHT;
        }
    }

    /**
     * Set the position of the tabs (TOP, LEFT, BOTTOM, or RIGHT).
     */
    public void setTabPosition(TabPosition pos) {
        component.setTabPlacement(pos.value);
    }

    /**
     * Get the name displayed on a particular tab.
     *
     * @param index the index of the tab
     */
    public String getTabName(int index) {
        return component.getTitleAt(index);
    }

    /**
     * Set the name displayed on a particular tab.
     *
     * @param index the index of the tab
     * @param name the name to display
     */
    public void setTabName(int index, String name) {
        component.setTitleAt(index, name);
    }

    /**
     * Get the image displayed on a particular tab.
     *
     * @param index the index of the tab
     */
    public Icon getTabImage(int index) {
        return component.getIconAt(index);
    }

    /**
     * Set the image displayed on a particular tab.
     *
     * @param index the index of the tab
     * @param image the image to display
     */
    public void setTabImage(int index, Icon image) {
        component.setIconAt(index, image);
    }

    /**
     * Get the index of the tab which is currently selected.
     */
    public int getSelectedTab() {
        return component.getSelectedIndex();
    }

    /**
     * Set which tab is selected.
     *
     * @param index the index of the tab
     */
    public void setSelectedTab(int index) {
        try {
            suppressEvents++;
            component.setSelectedIndex(index);
        } finally {
            suppressEvents--;
        }
    }

    /**
     * This inner class represents a position for the tabs.
     */
    public static class TabPosition {

        protected int value;

        private TabPosition(int value) {
            this.value = value;
        }
    }
}
