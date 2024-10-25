package buoy.widget;

import buoy.xml.*;
import buoy.xml.delegate.*;
import java.util.*;
import javax.swing.*;

/**
 * A BMenuBar is a WidgetContainer corresponding to the menu bar of a window.
 *
 * @author Peter Eastman
 */
public class BMenuBar extends WidgetContainer<JMenuBar> {

    private final List<BMenu> menus = new ArrayList<>();

    static {
        WidgetEncoder.setPersistenceDelegate(BMenuBar.class, new IndexedContainerDelegate(new String[]{"getChild"}));
    }

    /**
     * Create a new BMenuBar.
     */
    public BMenuBar() {
        component = createComponent();
    }

    /**
     * Create the JMenu which serves as this Widget's Component. This method is
     * protected so that subclasses can override it.
     */
    protected JMenuBar createComponent() {
        return new JMenuBar();
    }

    @Override
    public JMenuBar getComponent() {
        return component;
    }

    /**
     * Add a BMenu to the end of the menu bar.
     *
     * @param menu the BMenu to add
     */
    public void add(BMenu menu) {
        add(menu, menu.getParent() == this ? getChildCount() - 1 : getChildCount());
    }

    /**
     * Add a BMenu to the menu bar.
     *
     * @param menu the BMenu to add
     * @param index the position at which to add it
     */
    public void add(BMenu menu, int index) {
        if (menu.getParent() != null) {
            menu.getParent().remove(menu);
        }
        menus.add(index, menu);
        getComponent().add(menu.getComponent(), index);
        setAsParent(menu);
        invalidateSize();
    }

    /**
     * Get the number of children in this container.
     */
    @Override
    public int getChildCount() {
        return menus.size();
    }

    /**
     * Get the i'th child of this container.
     */
    public BMenu getChild(int i) {
        return menus.get(i);
    }

    /**
     * Get a Collection containing all child Widgets of this container.
     */
    @Override
    public Collection<Widget> getChildren() {
        return new ArrayList<>(menus);
    }

    /**
     * Remove a child Widget from this container.
     */
    @Override
    public void remove(Widget widget) {
        menus.remove(widget);
        component.remove(widget.getComponent());
        removeAsParent(widget);
        invalidateSize();
    }

    /**
     * Remove all child Widgets from this container.
     */
    @Override
    public void removeAll() {
        for (BMenu menu : menus) {
            removeAsParent(menu);
        }
        component.removeAll();
        menus.clear();
        invalidateSize();
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
    }
}
