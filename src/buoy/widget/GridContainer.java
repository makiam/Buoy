package buoy.widget;

import buoy.internal.*;
import buoy.xml.*;
import buoy.xml.delegate.*;
import java.awt.*;
import java.util.*;
import javax.swing.JPanel;

/**
 * GridContainer is a WidgetContainer which arranges its child Widgets in a uniform grid.  Every column is
 * the same width, and every row is the same height.
 * <p>
 * In addition to the event types generated by all Widgets, GridContainers generate the following event types:
 * <ul>
 * <li>{@link buoy.event.RepaintEvent RepaintEvent}</li>
 * </ul>
 *
 * @author Peter Eastman
 */

public class GridContainer extends WidgetContainer
{
  private Widget child[][];
  private LayoutInfo defaultLayout, childLayout[][];
  private int numRows, numCols;
  
  static
  {
    WidgetEncoder.setPersistenceDelegate(GridContainer.class, new GridContainerDelegate());
  }

  /**
   * Create a new GridContainer.
   *
   * @param cols     the number of columns in the grid
   * @param rows     the number of rows in the grid
   */
  
  public GridContainer(int cols, int rows)
  {
    component = new WidgetContainerPanel(this);
    child = new Widget [cols][rows];
    childLayout = new LayoutInfo [cols][rows];
    defaultLayout = new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.NONE, new Insets(2, 2, 2, 2), null);
    numRows = rows;
    numCols = cols;
  }

  public JPanel getComponent()
  {
    return (JPanel) component;
  }

  /**
   * Get the number of children in this container.
   */
  
  public int getChildCount()
  {
    int count = 0;
    for (int i = 0; i < child.length; i++)
      for (int j = 0; j < child[i].length; j++)
        if (child[i][j] != null)
          count++;
    return count;
  }

  /**
   * Get a Collection containing all child Widgets of this container.
   */
  
  public Collection<Widget> getChildren()
  {
    ArrayList<Widget> ls = new ArrayList<Widget>(numCols*numRows);
    for (int i = 0; i < child.length; i++)
      for (int j = 0; j < child[i].length; j++)
        if (child[i][j] != null)
          ls.add(child[i][j]);
    return ls;
  }
  
  /**
   * Get the child in a particular cell.
   *
   * @param col        the column for which to get the Widget
   * @param row        the row for which to get the Widget
   * @return the Widget in the specified cell
   */
  
  public Widget getChild(int col, int row)
  {
    return child[col][row];
  }

  /**
   * Layout the child Widgets.  This may be invoked whenever something has changed (the size of this
   * WidgetContainer, the preferred size of one of its children, etc.) that causes the layout to no
   * longer be correct.  If a child is itself a WidgetContainer, its layoutChildren() method will be
   * called in turn.
   */
  
  public void layoutChildren()
  {
    // Work out the positions of every row and column.
    
    Dimension dim = getComponent().getSize();
    int cellXBound[] = new int [numCols+1];
    int cellYBound[] = new int [numRows+1];
    double xsize = dim.width/numCols;
    double ysize = dim.height/numRows;
    for (int i = 1; i < cellXBound.length; i++)
      cellXBound[i] = (int) (i*xsize);
    for (int i = 1; i < cellYBound.length; i++)
      cellYBound[i] = (int) (i*ysize);
    
    // Now layout the children.
    
    Rectangle cell = new Rectangle();
    for (int i = 0; i < child.length; i++)
      for (int j = 0; j < child[i].length; j++)
        if (child[i][j] != null)
        {
          Widget w = child[i][j];
          LayoutInfo layout = childLayout[i][j];
          if (layout == null)
            layout = defaultLayout;
          cell.x = cellXBound[i];
          cell.y = cellYBound[j];
          cell.width = cellXBound[i+1]-cell.x;
          cell.height = cellYBound[j+1]-cell.y;
          w.getComponent().setBounds(layout.getWidgetLayout(w, cell));
          if (w instanceof WidgetContainer)
            ((WidgetContainer) w).layoutChildren();
        }
  }
  
  /**
   * Add a Widget to this container, using the default LayoutInfo to position it.  If there is already
   * a Widget in the specified cell, that one will be removed before the new one is added.
   *
   * @param widget     the Widget to add
   * @param col        the column in which to place the Widget
   * @param row        the row in which to place the Widget
   */
  
  public void add(Widget widget, int col, int row)
  {
    add(widget, col, row, null);
  }

  /**
   * Add a Widget to this container.  If there is already a Widget in the specified cell, that one will
   * be removed before the new one is added.
   *
   * @param widget     the Widget to add
   * @param col        the column in which to place the Widget
   * @param row        the row in which to place the Widget
   * @param layout     the LayoutInfo to use for this Widget.  If null, the default LayoutInfo will be used.
   */
  
  public void add(Widget widget, int col, int row, LayoutInfo layout)
  {
    if (widget.getParent() != null)
      widget.getParent().remove(widget);
    if (child[col][row] != null)
      remove(col, row);
    child[col][row] = widget;
    childLayout[col][row] = layout;
    getComponent().add(widget.getComponent());
    setAsParent(widget);
    invalidateSize();
  }
  
  /**
   * Get the LayoutInfo for the Widget in a particular cell.
   *
   * @param col        the column of the Widget for which to get the LayoutInfo
   * @param row        the row of the Widget for which to get the LayoutInfo
   * @return the LayoutInfo being used for that cell.  This may return null, which indicates that the
   *         default LayoutInfo is being used.
   */
  
  public LayoutInfo getChildLayout(int col, int row)
  {
    return childLayout[col][row];
  }

  /**
   * Set the LayoutInfo for the Widget in a particular cell.
   *
   * @param col        the column of the Widget for which to set the LayoutInfo
   * @param row        the row of the Widget for which to set the LayoutInfo
   * @param layout     the new LayoutInfo.  If null, the default LayoutInfo will be used
   */
  
  public void setChildLayout(int col, int row, LayoutInfo layout)
  {
    childLayout[col][row] = layout;
    invalidateSize();
  }
  
  /**
   * Get the LayoutInfo for a particular Widget.
   *
   * @param widget     the Widget for which to get the LayoutInfo
   * @return the LayoutInfo being used for that Widget.  This may return null, which indicates that the
   *         default LayoutInfo is being used.  It will also return null if the specified Widget is not
   *         a child of this container.
   */
  
  public LayoutInfo getChildLayout(Widget widget)
  {
    for (int i = 0; i < child.length; i++)
      for (int j = 0; j < child[i].length; j++)
        if (child[i][j] == widget)
          return childLayout[i][j];
    return null;
  }
  
  /**
   * Set the LayoutInfo for a particular Widget.
   *
   * @param widget     the Widget for which to set the LayoutInfo
   * @param layout     the new LayoutInfo.  If null, the default LayoutInfo will be used
   */
  
  public void setChildLayout(Widget widget, LayoutInfo layout)
  {
    for (int i = 0; i < child.length; i++)
      for (int j = 0; j < child[i].length; j++)
        if (child[i][j] == widget)
        {
          childLayout[i][j] = layout;
          invalidateSize();
          return;
        }
  }

  /**
   * Get the default LayoutInfo.
   */
  
  public LayoutInfo getDefaultLayout()
  {
    return defaultLayout;
  }
  
  /**
   * Set the default LayoutInfo.
   */
  
  public void setDefaultLayout(LayoutInfo layout)
  {
    defaultLayout = layout;
    invalidateSize();
  }
  
  /**
   * Get the number of rows in the grid.
   */
  
  public int getRowCount()
  {
    return numRows;
  }

  /**
   * Set the number of rows in the grid.  If this causes the grid to shrink, all Widgets which do not fit
   * into the new grid will be removed.
   */
  
  public void setRowCount(int rows)
  {
    if (rows < numRows)
      for (int i = 0; i < child.length; i++)
        for (int j = rows; j < child[i].length; j++)
          if (child[i][j] != null)
            remove(i, j);
    Widget newchild[][] = new Widget [numCols][rows];
    LayoutInfo newlayout[][] = new LayoutInfo [numCols][rows];
    int copy = Math.min(rows, numRows);
    for (int i = 0; i < child.length; i++)
      for (int j = 0; j < copy; j++)
      {
        newchild[i][j] = child[i][j];
        newlayout[i][j] = childLayout[i][j];
      }
    child = newchild;
    childLayout = newlayout;
    numRows = rows;
    invalidateSize();
  }

  /**
   * Get the number of columns in the grid.
   */
  
  public int getColumnCount()
  {
    return numCols;
  }

  /**
   * Set the number of columns in the grid.  If this causes the grid to shrink, all Widgets which do not fit
   * into the new grid will be removed.
   */
  
  public void setColumnCount(int cols)
  {
    if (cols < numCols)
      for (int i = cols; i < child.length; i++)
        for (int j = 0; j < child[i].length; j++)
          if (child[i][j] != null)
            remove(i, j);
    Widget newchild[][] = new Widget [cols][numRows];
    LayoutInfo newlayout[][] = new LayoutInfo [cols][numRows];
    int copy = Math.min(cols, numCols);
    for (int i = 0; i < copy; i++)
    {
      newchild[i] = child[i];
      newlayout[i] = childLayout[i];
    }
    child = newchild;
    childLayout = newlayout;
    numCols = cols;
    invalidateSize();
  }
  
  /**
   * Remove a child Widget from this container.
   *
   * @param widget     the Widget to remove
   */
  
  public void remove(Widget widget)
  {
    for (int i = 0; i < child.length; i++)
      for (int j = 0; j < child[i].length; j++)
        if (child[i][j] == widget)
        {
          getComponent().remove(widget.getComponent());
          removeAsParent(widget);
          child[i][j] = null;
          childLayout[i][j] = null;
          invalidateSize();
          return;
        }
  }
  
  /**
   * Remove the Widget in a particular cell from this container.
   *
   * @param col       the column from which to remove the Widget
   * @param row       the row from which to remove the Widget
   */
  
  public void remove(int col, int row)
  {
    getComponent().remove(child[col][row].getComponent());
    removeAsParent(child[col][row]);
    child[col][row] = null;
    childLayout[col][row] = null;
    invalidateSize();
  }
  
  /**
   * Remove all child Widgets from this container.
   */
  
  public void removeAll()
  {
    getComponent().removeAll();
    for (int i = 0; i < child.length; i++)
      for (int j = 0; j < child[i].length; j++)
        if (child[i][j] != null)
        {
          removeAsParent(child[i][j]);
          child[i][j] = null;
          childLayout[i][j] = null;
        }
    invalidateSize();
  }

  /**
   * Get the cell containing the specified Widget.
   *
   * @param widget      the Widget to locate
   * @return a Point containing the row and column where the Widget is located, or null if the Widget is
   *         not a child of the container
   */
  
  public Point getChildCell(Widget widget)
  {
    for (int i = 0; i < child.length; i++)
      for (int j = 0; j < child[i].length; j++)
        if (child[i][j] == widget)
          return new Point(i, j);
    return null;
  }
  
  /**
   * Get the smallest size at which this Widget can reasonably be drawn.  When a WidgetContainer lays out
   * its contents, it will attempt never to make this Widget smaller than its minimum size.
   */
  
  public Dimension getMinimumSize()
  {
    int x = 0, y = 0;
    for (int i = 0; i < child.length; i++)
      for (int j = 0; j < child[i].length; j++)
        if (child[i][j] != null)
        {
          Dimension dim = child[i][j].getMinimumSize();
          if (dim.width > x)
            x = dim.width;
          if (dim.height > y)
            y = dim.height;
        }
    return new Dimension(x*numCols, y*numRows);
  }

  /**
   * Get the preferred size at which this Widget will look best.  When a WidgetContainer lays out
   * its contents, it will attempt to make this Widget as close as possible to its preferred size.
   */
  
  public Dimension getPreferredSize()
  {
    int x = 0, y = 0;
    for (int i = 0; i < child.length; i++)
      for (int j = 0; j < child[i].length; j++)
        if (child[i][j] != null)
        {
          Widget w = child[i][j];
          LayoutInfo layout = childLayout[i][j];
          if (layout == null)
            layout = defaultLayout;
          Dimension dim = layout.getPreferredSize(w);
          if (dim.width > x)
            x = dim.width;
          if (dim.height > y)
            y = dim.height;
        }
    return new Dimension(x*numCols, y*numRows);
  }
}