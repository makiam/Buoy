package buoy.widget;

import buoy.internal.*;
import buoy.xml.*;
import buoy.xml.delegate.*;
import java.awt.*;
import java.util.*;
import javax.swing.JPanel;

/**
 * OverlayContainer is a WidgetContainer which overlays its children on top of each other.  Every child
 * Widget is sized to fill the entire container, and the preferred size of the container is equal to the
 * largest preferred size of any of its children.
 * <p>
 * OverlayContainers are generally used to switch between several different Widgets which all appear in the
 * same position.  When used this way, only one of the Widgets will be visible at any time.  The
 * {@link buoy.widget.OverlayContainer#setVisibleChild setVisibleChild()} method provides an easy way to
 * switch between the child Widgets by showing one and hiding all the others.
 * <p>
 * Another use of OverlayContainer is to place a partially transparent Widget in front of
 * another one.  This allows an "overlay" to be displayed at a particular position, independent of the
 * positions of other Widgets in the window.  An OverlayContainer can also be used to block mouse events
 * from reaching a Widget.  Mouse events are always delivered to the topmost visible Widget that has requested
 * to receive them.  Suppose that <code>mainWidget</code> is a Widget (possibly a WidgetContainer with
 * many children).  The following code will block all mouse events from reaching it and its children:
 * <p>
 * <pre>
 * CustomWidget blocker = new CustomWidget();
 * blocker.setOpaque(false);
 * blocker.addEventLink(WidgetMouseEvent.class, new Object() {
 *   void processEvent()
 *   {
 *     // Ignore the event.
 *   }
 * });
 * OverlayContainer overlay = new OverlayContainer();
 * overlay.add(mainWidget);
 * overlay.add(blocker);
 * </pre>
 * <p>
 * You can then turn event blocking on or off by calling <code>blocker.setVisible(true)</code> or
 * <code>blocker.setVisible(false)</code>.  Note that we have added an event link to <code>blocker</code>
 * which listens for mouse events.  If you do not do this, it will ignore mouse events and let them pass
 * through to the Widget underneath.
 * <p>
 * In addition to the event types generated by all Widgets, OverlayContainers generate the following
 * event types:
 * <ul>
 * <li>{@link buoy.event.RepaintEvent RepaintEvent}</li>
 * </ul>
 *
 * @author Peter Eastman
 */

public class OverlayContainer extends WidgetContainer
{
  private ArrayList children;
  private Dimension minSize, prefSize;

  static
  {
    WidgetEncoder.setPersistenceDelegate(OverlayContainer.class, new OverlayContainerDelegate());
  }
  
  /**
   * Create a new OverlayContainer.
   */
  
  public OverlayContainer()
  {
    component = new WidgetContainerPanel(this);
    children = new ArrayList();
  }
  
  /**
   * Get the number of children in this container.
   */
  
  public int getChildCount()
  {
    return children.size();
  }
  
  /**
   * Get the i'th child of this container.
   */
  
  public Widget getChild(int i)
  {
    return (Widget) children.get(i);
  }
  
  /**
   * Get a Collection containing all child Widgets of this container.
   */
  
  public Collection getChildren()
  {
    return new ArrayList(children);
  }

  /**
   * Get the index of a particular Widget.
   *
   * @param widget      the Widget to locate
   * @return the index of the Widget within this container
   */
  
  public int getChildIndex(Widget widget)
  {
    return children.indexOf(widget);
  }
  
  /**
   * Layout the child Widgets.  This may be invoked whenever something has changed (the size of this
   * WidgetContainer, the preferred size of one of its children, etc.) that causes the layout to no
   * longer be correct.  If a child is itself a WidgetContainer, its layoutChildren() method will be
   * called in turn.
   */
  
  public void layoutChildren()
  {
    Rectangle bounds = getBounds();
    for (int i = 0; i < children.size(); i++)
    {
      Widget child = (Widget) children.get(i);
      Dimension max = child.getMaximumSize();
      child.getComponent().setBounds(new Rectangle(0, 0, Math.min(bounds.width, max.width), Math.min(bounds.height, max.height)));
      if (child instanceof WidgetContainer)
        ((WidgetContainer) child).layoutChildren();
    }
  }
  
  /**
   * Add a Widget to this container.
   *
   * @param widget    the Widget to add
   * @param index     the index of the Widget within the container.  Widgets with lower indices are displayed
   *                  in front of ones with higher indices.
   */
  
  public void add(Widget widget, int index)
  {
    if (widget.getParent() != null)
      widget.getParent().remove(widget);
    children.add(index, widget);
    ((JPanel) component).add(widget.component, index);
    setAsParent(widget);
    invalidateSize();
  }
  
  /**
   * Add a Widget to this container.  The new Widget will be placed in front of all other Widgets in the
   * container (at index 0).
   *
   * @param widget    the Widget to add
   */
  
  public void add(Widget widget)
  {
    add(widget, 0);
  }
  
  /**
   * Remove a child Widget from this container.
   *
   * @param widget     the Widget to remove
   */
  
  public void remove(Widget widget)
  {
    int index = children.indexOf(widget);
    if (index > -1)
    {
      ((JPanel) component).remove(widget.component);
      children.remove(index);
      removeAsParent(widget);
      invalidateSize();
    }
  }
  
  /**
   * Remove all child Widgets from this container.
   */
  
  public void removeAll()
  {
    ((JPanel) component).removeAll();
    for (int i = 0; i < children.size(); i++)
      removeAsParent((Widget) children.get(i));
    children.clear();
    invalidateSize();
  }
  
  /**
   * Set the i'th child Widget to be visible, and all others to be not visible.
   */
  
  public void setVisibleChild(int i)
  {
    for (int j = 0; j < children.size(); j++)
      ((Widget) children.get(j)).setVisible(i == j);
  }
  
  /**
   * Set a particular child Widget to be visible, and all others to be not visible.
   */
  
  public void setVisibleChild(Widget child)
  {
    for (int j = 0; j < children.size(); j++)
    {
      Widget w = (Widget) children.get(j);
      w.setVisible(w == child);
    }
  }
  
  /**
   * Get the smallest size at which this Widget can reasonably be drawn.  When a WidgetContainer lays out
   * its contents, it will attempt never to make this Widget smaller than its minimum size.
   */
  
  public Dimension getMinimumSize()
  {
    if (minSize == null)
    {
      minSize = new Dimension();
      for (int i = 0; i < children.size(); i++)
      {
        Dimension size = ((Widget) children.get(i)).getMinimumSize();
        if (size.width > minSize.width)
          minSize.width = size.width;
        if (size.height > minSize.height)
          minSize.height = size.height;
      }
    }
    return minSize;
  }

  /**
   * Get the preferred size at which this Widget will look best.  When a WidgetContainer lays out
   * its contents, it will attempt to make this Widget as close as possible to its preferred size.
   */
  
  public Dimension getPreferredSize()
  {
    if (prefSize == null)
    {
      prefSize = new Dimension();
      for (int i = 0; i < children.size(); i++)
      {
        Dimension size = ((Widget) children.get(i)).getPreferredSize();
        if (size.width > prefSize.width)
          prefSize.width = size.width;
        if (size.height > prefSize.height)
          prefSize.height = size.height;
      }
    }
    return prefSize;
  }
  
  /**
   * Discard the cached sizes when any child's size changes.
   */
  
  protected void invalidateSize()
  {
    minSize = prefSize = null;
    super.invalidateSize();
  }
}