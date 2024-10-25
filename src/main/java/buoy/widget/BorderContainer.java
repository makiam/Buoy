package buoy.widget;

import buoy.internal.*;
import buoy.xml.*;
import buoy.xml.delegate.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.JPanel;

/**
 * BorderContainer is a WidgetContainer which may have up to five children: one
 * along each edge, and a fifth one in the center. When this container is
 * resized, the center component grows to take up as much space as possible.
 * <p>
 * In addition to the event types generated by all Widgets, BorderContainers
 * generate the following event types:
 * <ul>
 * <li>{@link buoy.event.RepaintEvent RepaintEvent}</li>
 * </ul>
 *
 * @author Peter Eastman
 */
public class BorderContainer extends WidgetContainer<JPanel> {

    private final Widget[] child;
    private final LayoutInfo[] childLayout;
    private LayoutInfo defaultLayout;
    private boolean cornersVertical;

    public static final Position CENTER = new Position(0);
    public static final Position NORTH = new Position(1);
    public static final Position EAST = new Position(2);
    public static final Position SOUTH = new Position(3);
    public static final Position WEST = new Position(4);

    private static final Position ALL_POSITIONS[] = new Position[]{CENTER, NORTH, EAST, SOUTH, WEST};

    static {
        WidgetEncoder.setPersistenceDelegate(BorderContainer.class, new BorderContainerDelegate());
        WidgetEncoder.setPersistenceDelegate(Position.class, new StaticFieldDelegate(BorderContainer.class));
    }

    /**
     * Create a new BorderContainer.
     */
    public BorderContainer() {
        component = new WidgetContainerPanel(this);
        child = new Widget[5];
        childLayout = new LayoutInfo[5];
        defaultLayout = new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.BOTH, null, null);
    }

    @Override
    public JPanel getComponent() {
        return component;
    }

    /**
     * Get the number of children in this container.
     */
    @Override
    public int getChildCount() {
        int count = 0;
        for (Widget child1 : child) {
            if(child1 == null) continue;
            count++;
        }
        return count;
    }

    /**
     * Get a Collection containing all child Widgets of this container.
     */
    @Override
    public Collection<Widget<?>> getChildren() {
        List<Widget<?>> ls = new ArrayList<>(5);
        for (Widget child1 : child) {
            if(child1 == null) continue;
            ls.add(child1);
        }
        return ls;
    }

    /**
     * Get the child in a particular location.
     *
     * @param where the location of the Widget to get (CENTER, NORTH, EAST,
     * SOUTH, or WEST)
     */
    public Widget getChild(Position where) {
        return child[where.value];
    }

    /**
     * Get the Position of a particular Widget.
     *
     * @param widget the Widget for which to get the Position
     * @return the Position at which that Widget is located, or null if the
     * specified Widget is not a child of this container.
     */
    public Position getChildPosition(Widget widget) {
        for (Position ALL_POSITIONS1 : ALL_POSITIONS) {
            if (child[ALL_POSITIONS1.value] == widget) {
                return ALL_POSITIONS1;
            }
        }
        return null;
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
        Dimension size = getComponent().getSize();
        Rectangle bounds[] = new Rectangle[5];
        Dimension prefSize[] = new Dimension[5];
        for (int i = 0; i < child.length; i++) {
            bounds[i] = new Rectangle();
            if (child[i] == null || i == CENTER.value) {
                prefSize[i] = new Dimension();
            } else {
                prefSize[i] = (childLayout[i] == null ? defaultLayout.getPreferredSize(child[i]) : childLayout[i].getPreferredSize(child[i]));
            }
        }
        bounds[WEST.value].width = prefSize[WEST.value].width;
        bounds[EAST.value].width = prefSize[EAST.value].width;
        bounds[EAST.value].x = size.width - bounds[EAST.value].width;
        bounds[NORTH.value].height = prefSize[NORTH.value].height;
        bounds[SOUTH.value].height = prefSize[SOUTH.value].height;
        bounds[SOUTH.value].y = size.height - bounds[SOUTH.value].height;
        if (cornersVertical) {
            bounds[WEST.value].height = bounds[EAST.value].height = size.height;
            bounds[NORTH.value].x = bounds[SOUTH.value].x = bounds[WEST.value].width;
            bounds[NORTH.value].width = bounds[SOUTH.value].width = bounds[EAST.value].x - bounds[NORTH.value].x;
        } else {
            bounds[NORTH.value].width = bounds[SOUTH.value].width = size.width;
            bounds[WEST.value].y = bounds[EAST.value].y = bounds[NORTH.value].height;
            bounds[WEST.value].height = bounds[EAST.value].height = bounds[SOUTH.value].y - bounds[WEST.value].y;
        }
        bounds[CENTER.value].x = bounds[WEST.value].width;
        bounds[CENTER.value].y = bounds[NORTH.value].height;
        bounds[CENTER.value].width = bounds[EAST.value].x - bounds[CENTER.value].x;
        bounds[CENTER.value].height = bounds[SOUTH.value].y - bounds[CENTER.value].y;
        for (int i = 0; i < 5; i++) {
            if (child[i] == null) {
                continue;
            }
            LayoutInfo layout = (childLayout[i] == null ? defaultLayout : childLayout[i]);
            child[i].getComponent().setBounds(layout.getWidgetLayout(child[i], bounds[i]));
            if (child[i] instanceof WidgetContainer) {
                ((WidgetContainer) child[i]).layoutChildren();
            }
        }
    }

    /**
     * Add a Widget to this container, using the default LayoutInfo to position
     * it.
     *
     * @param widget the Widget to add
     * @param where the location to add it (CENTER, NORTH, EAST, SOUTH, or WEST)
     */
    public void add(Widget widget, Position where) {
        add(widget, where, null);
    }

    /**
     * Add a Widget to this container.
     *
     * @param widget the Widget to add
     * @param where the location to add it (CENTER, NORTH, EAST, SOUTH, or WEST)
     * @param layout the LayoutInfo to use for this Widget. If null, the default
     * LayoutInfo will be used.
     */
    public void add(Widget widget, Position where, LayoutInfo layout) {
        if (child[where.value] != null) {
            remove(where);
        }
        if (widget.getParent() != null) {
            widget.getParent().remove(widget);
        }
        child[where.value] = widget;
        childLayout[where.value] = layout;
        getComponent().add(widget.getComponent());
        setAsParent(widget);
        invalidateSize();
    }

    /**
     * Get the LayoutInfo for the Widget in a particular location.
     *
     * @param where the location of the Widget (CENTER, NORTH, EAST, SOUTH, or
     * WEST)
     * @return the LayoutInfo being used for that Widget. This may return null,
     * which indicates that the default LayoutInfo is being used.
     */
    public LayoutInfo getChildLayout(Position where) {
        return childLayout[where.value];
    }

    /**
     * Set the LayoutInfo for the Widget in a particular location.
     *
     * @param where the location of the Widget (CENTER, NORTH, EAST, SOUTH, or
     * WEST)
     * @param layout the new LayoutInfo. If null, the default LayoutInfo will be
     * used.
     */
    public void setChildLayout(Position where, LayoutInfo layout) {
        childLayout[where.value] = layout;
        invalidateSize();
    }

    /**
     * Get the LayoutInfo for a particular Widget.
     *
     * @param widget the Widget for which to get the LayoutInfo
     * @return the LayoutInfo being used for that Widget. This may return null,
     * which indicates that the default LayoutInfo is being used. It will also
     * return null if the specified Widget is not a child of this container.
     */
    public LayoutInfo getChildLayout(Widget widget) {
        for (int i = 0; i < child.length; i++) {
            if (child[i] == widget) {
                return childLayout[i];
            }
        }
        return null;
    }

    /**
     * Set the LayoutInfo for a particular Widget.
     *
     * @param widget the Widget for which to set the LayoutInfo
     * @param layout the new LayoutInfo. If null, the default LayoutInfo will be
     * used.
     */
    public void setChildLayout(Widget widget, LayoutInfo layout) {
        for (int i = 0; i < child.length; i++) {
            if (child[i] == widget) {
                childLayout[i] = layout;
                return;
            }
        }
        invalidateSize();
    }

    /**
     * Get the default LayoutInfo.
     */
    public LayoutInfo getDefaultLayout() {
        return defaultLayout;
    }

    /**
     * Set the default LayoutInfo.
     */
    public void setDefaultLayout(LayoutInfo layout) {
        defaultLayout = layout;
        invalidateSize();
    }

    /**
     * Remove a child Widget from this container.
     *
     * @param widget the Widget to remove
     */
    @Override
    public void remove(Widget widget) {
        for (int i = 0; i < child.length; i++) {
            if (child[i] == widget) {
                remove(i);
                return;
            }
        }
    }

    /**
     * Remove a child Widget from this container.
     *
     * @param where the location of the Widget to remove (CENTER, NORTH, EAST,
     * SOUTH, or WEST)
     */
    public void remove(Position where) {
        remove(where.value);
    }

    /**
     * Remove a child Widget from this container.
     *
     * @param where the index of the Widget to remove
     */
    private void remove(int where) {
        if (child[where] == null) {
            return;
        }
        getComponent().remove(child[where].getComponent());
        removeAsParent(child[where]);
        child[where] = null;
        childLayout[where] = null;
        invalidateSize();
    }

    /**
     * Remove all child Widgets from this container.
     */
    @Override
    public void removeAll() {
        for (int i = 0; i < child.length; i++) {
            remove(i);
        }
    }

    /**
     * Get whether the vertical Widgets (EAST and WEST) or the horizontal
     * Widgets (NORTH and SOUTH) extend all the way to the corners.
     */
    public boolean getCornersAreVertical() {
        return cornersVertical;
    }

    /**
     * Set whether the vertical Widgets (EAST and WEST) or the horizontal
     * Widgets (NORTH and SOUTH) extend all the way to the corners.
     */
    public void setCornersAreVertical(boolean vertical) {
        cornersVertical = vertical;
        invalidateSize();
    }

    /**
     * Get the smallest size at which this Widget can reasonably be drawn. When
     * a WidgetContainer lays out its contents, it will attempt never to make
     * this Widget smaller than its minimum size.
     */
    @Override
    public Dimension getMinimumSize() {
        Dimension size = (child[CENTER.value] == null ? new Dimension() : child[CENTER.value].getMinimumSize());
        if (cornersVertical) {
            if (child[NORTH.value] != null) {
                Dimension dim = child[NORTH.value].getMinimumSize();
                size.height += dim.height;
                if (size.width < dim.width) {
                    size.width = dim.width;
                }
            }
            if (child[SOUTH.value] != null) {
                Dimension dim = child[SOUTH.value].getMinimumSize();
                size.height += dim.height;
                if (size.width < dim.width) {
                    size.width = dim.width;
                }
            }
            if (child[EAST.value] != null) {
                Dimension dim = child[EAST.value].getMinimumSize();
                size.width += dim.width;
                if (size.height < dim.height) {
                    size.height = dim.height;
                }
            }
            if (child[WEST.value] != null) {
                Dimension dim = child[WEST.value].getMinimumSize();
                size.width += dim.width;
                if (size.height < dim.height) {
                    size.height = dim.height;
                }
            }
        } else {
            if (child[EAST.value] != null) {
                Dimension dim = child[EAST.value].getMinimumSize();
                size.width += dim.width;
                if (size.height < dim.height) {
                    size.height = dim.height;
                }
            }
            if (child[WEST.value] != null) {
                Dimension dim = child[WEST.value].getMinimumSize();
                size.width += dim.width;
                if (size.height < dim.height) {
                    size.height = dim.height;
                }
            }
            if (child[NORTH.value] != null) {
                Dimension dim = child[NORTH.value].getMinimumSize();
                size.height += dim.height;
                if (size.width < dim.width) {
                    size.width = dim.width;
                }
            }
            if (child[SOUTH.value] != null) {
                Dimension dim = child[SOUTH.value].getMinimumSize();
                size.height += dim.height;
                if (size.width < dim.width) {
                    size.width = dim.width;
                }
            }
        }
        return size;
    }

    /**
     * Get the preferred size at which this Widget will look best. When a
     * WidgetContainer lays out its contents, it will attempt to make this
     * Widget as close as possible to its preferred size.
     */
    @Override
    public Dimension getPreferredSize() {
        Dimension size;
        if (child[CENTER.value] == null) {
            size = new Dimension();
        } else if (childLayout[CENTER.value] == null) {
            size = defaultLayout.getPreferredSize(child[CENTER.value]);
        } else {
            size = childLayout[CENTER.value].getPreferredSize(child[CENTER.value]);
        }
        if (cornersVertical) {
            if (child[NORTH.value] != null) {
                Dimension dim = (childLayout[NORTH.value] == null ? defaultLayout.getPreferredSize(child[NORTH.value]) : childLayout[NORTH.value].getPreferredSize(child[NORTH.value]));
                size.height += dim.height;
                if (size.width < dim.width) {
                    size.width = dim.width;
                }
            }
            if (child[SOUTH.value] != null) {
                Dimension dim = (childLayout[SOUTH.value] == null ? defaultLayout.getPreferredSize(child[SOUTH.value]) : childLayout[SOUTH.value].getPreferredSize(child[SOUTH.value]));
                size.height += dim.height;
                if (size.width < dim.width) {
                    size.width = dim.width;
                }
            }
            if (child[EAST.value] != null) {
                Dimension dim = (childLayout[EAST.value] == null ? defaultLayout.getPreferredSize(child[EAST.value]) : childLayout[EAST.value].getPreferredSize(child[EAST.value]));
                size.width += dim.width;
                if (size.height < dim.height) {
                    size.height = dim.height;
                }
            }
            if (child[WEST.value] != null) {
                Dimension dim = (childLayout[WEST.value] == null ? defaultLayout.getPreferredSize(child[WEST.value]) : childLayout[WEST.value].getPreferredSize(child[WEST.value]));
                size.width += dim.width;
                if (size.height < dim.height) {
                    size.height = dim.height;
                }
            }
        } else {
            if (child[EAST.value] != null) {
                Dimension dim = (childLayout[EAST.value] == null ? defaultLayout.getPreferredSize(child[EAST.value]) : childLayout[EAST.value].getPreferredSize(child[EAST.value]));
                size.width += dim.width;
                if (size.height < dim.height) {
                    size.height = dim.height;
                }
            }
            if (child[WEST.value] != null) {
                Dimension dim = (childLayout[WEST.value] == null ? defaultLayout.getPreferredSize(child[WEST.value]) : childLayout[WEST.value].getPreferredSize(child[WEST.value]));
                size.width += dim.width;
                if (size.height < dim.height) {
                    size.height = dim.height;
                }
            }
            if (child[NORTH.value] != null) {
                Dimension dim = (childLayout[NORTH.value] == null ? defaultLayout.getPreferredSize(child[NORTH.value]) : childLayout[NORTH.value].getPreferredSize(child[NORTH.value]));
                size.height += dim.height;
                if (size.width < dim.width) {
                    size.width = dim.width;
                }
            }
            if (child[SOUTH.value] != null) {
                Dimension dim = (childLayout[SOUTH.value] == null ? defaultLayout.getPreferredSize(child[SOUTH.value]) : childLayout[SOUTH.value].getPreferredSize(child[SOUTH.value]));
                size.height += dim.height;
                if (size.width < dim.width) {
                    size.width = dim.width;
                }
            }
        }
        return size;
    }

    /**
     * This inner class represents a position within the container.
     */
    public static class Position {

        protected int value;

        private Position(int value) {
            this.value = value;
        }
    }
}
