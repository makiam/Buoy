package buoy.widget;

import buoy.event.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.*;
import javax.swing.*;

/**
 * A BDialog is a WidgetContainer corresponding to a dialog window.  It may contain up to two child Widgets:
 * a BMenuBar, and a single other Widget (usually a WidgetContainer of some sort) which fills the rest
 * of the window.
 * <p>
 * In addition to the event types generated by all Widgets, BDialogs generate the following event types:
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

public class BDialog extends WindowWidget
{
  private BMenuBar menubar;
  
  /**
   * Create a non-modal BDialog with no title or parent window.
   */
  
  public BDialog()
  {
    component = createComponent(null, null, false);
    initInternal();
  }
  
  /**
   * Create a non-modal BDialog with no parent window.
   *
   * @param title      the title of the dialog
   */
  
  public BDialog(String title)
  {
    component = createComponent(null, null, false);
    initInternal();
    ((JDialog) component).setTitle(title);
  }

  /**
   * Create a new BDialog with no title.
   *
   * @param parent     the parent window (a BFrame or BDialog) for this dialog
   * @param modal      specifies whether this is a modal dialog
   */
  
  public BDialog(WindowWidget parent, boolean modal)
  {
    component = createComponent((Window) parent.getComponent(), null, modal);
    setParent(parent);
    initInternal();
  }

  /**
   * Create a new BDialog.
   *
   * @param parent     the parent window (a BFrame or BDialog) for this dialog
   * @param title      the title of the dialog
   * @param modal      specifies whether this is a modal dialog
   */
  
  public BDialog(WindowWidget parent, String title, boolean modal)
  {
    component = createComponent((Window) parent.getComponent(), title, modal);
    setParent(parent);
    initInternal();
  }
 
  /**
   * Create the JDialog which serves as this Widget's Component.  This method is protected so that
   * subclasses can override it.
   *
   * @param parent     the parent JFrame or JDialog (may be null)
   * @param title      the title of the dialog (may be null)
   * @param modal      specifies whether this is a modal dialog
   */
  
  protected JDialog createComponent(Window parent, String title, boolean modal)
  {
    if (parent instanceof Dialog)
      return new BDialogComponent((Dialog) parent, title, modal);
    else if (parent instanceof Frame)
      return new BDialogComponent((Frame) parent, title, modal);
    else if (parent == null)
      return new BDialogComponent();
    else
      throw new IllegalArgumentException("parent must be a BFrame or BDialog");
  }
  
  /**
   * Perform internal initialization.
   */
  
  private void initInternal()
  {
    ((JDialog) component).getContentPane().setLayout(null);
    component.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent ev)
      {
        if (lastSetSize == null || !lastSetSize.equals(component.getSize()))
        {
          lastSetSize = null;
          layoutChildren();
          BDialog.this.dispatchEvent(new WindowResizedEvent(BDialog.this));
        }
        else
          lastSetSize = null;
      }
    });
    ((JDialog) component).setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
  }

  /**
   * Get the number of children in this container.
   */
  
  public int getChildCount()
  {
    return ((menubar == null ? 0 : 1) + (content == null ? 0 : 1));
  }
  
  /**
   * Get an Iterator listing all child Widgets.
   */
  
  public Collection getChildren()
  {
    ArrayList ls = new ArrayList(3);
    if (menubar != null)
      ls.add(menubar);
    if (content != null)
      ls.add(content);
    return ls;
  }
  
  /**
   * Get the BMenuBar for this window.
   */

  public BMenuBar getMenuBar()
  {
    return menubar;
  }
  
  /**
   * Set the BMenuBar for this window.
   */
  
  public void setMenuBar(BMenuBar menus)
  {
    if (menubar != null)
      remove(menubar);
    if (menus == null)
      return;
    if (menus.getParent() != null)
      menus.getParent().remove(menus);
    menubar = menus;
    ((JDialog) component).setJMenuBar((JMenuBar) menubar.component);
    setAsParent(menubar);
  }

  /**
   * Remove a child Widget from this container.
   */
  
  public void remove(Widget widget)
  {
    if (menubar == widget)
    {
      ((JDialog) component).setJMenuBar(null);
      removeAsParent(menubar);
      menubar = null;
    }
    else if (content == widget)
    {
      ((JDialog) component).getContentPane().remove(widget.component);
      removeAsParent(content);
      content = null;
    }
  }
  
  /**
   * Remove all child Widgets from this container.
   */
  
  public void removeAll()
  {
    if (menubar != null)
      remove(menubar);
    if (content != null)
      remove(content);
  }
  
  /**
   * Get the title of the dialog.
   */
  
  public String getTitle()
  {
    return ((JDialog) component).getTitle();
  }
  
  /**
   * Set the title of the dialog.
   */
  
  public void setTitle(String title)
  {
    ((JDialog) component).setTitle(title);
  }
  
  /**
   * Set whether this dialog is modal.  This must be called before the dialog is made visible.
   * It is not possible to change whether a currently visible dialog is modal.
   */
  
  public void setModal(boolean modal)
  {
    ((JDialog) component).setModal(modal);
  }
  
  /**
   * Determine whether this dialog is modal.
   */
  
  public boolean isModal()
  {
    return ((JDialog) component).isModal();
  }
  
  /**
   * Determine whether this dialog may be resized by the user.
   */
  
  public boolean isResizable()
  {
    return ((JDialog) component).isResizable();
  }
  
  /**
   * Set whether this dialog may be resized by the user.
   */
  
  public void setResizable(boolean resizable)
  {
    ((JDialog) component).setResizable(resizable);
  }

  /**
   * Select an appropriate size for the dialog, based on the preferred size of its contents, then re-layout
   * all of its contents.  If this is being called for the first time before the dialog has yet been shown,
   * it also centers the dialog relative to its parent (or the screen if it does not have a parent).
   */
  
  public void pack()
  {
    boolean center = !getComponent().isDisplayable();
    super.pack();
    if (!center)
      return;
    Rectangle bounds = getBounds();
    Widget parent = getParent();
    if (parent == null)
    {
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      bounds.x = (screenSize.width-bounds.width)/2;
      bounds.y = (screenSize.height-bounds.height)/2;
    }
    else
    {
      Rectangle parentBounds = parent.getBounds();
      bounds.x = parentBounds.x+(parentBounds.width-bounds.width)/2;
      bounds.y = parentBounds.y+(parentBounds.height-bounds.height)/2;
    }
    setBounds(new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height));
  }

  /**
   * Get the JRootPane for this Widget's component.
   */

  protected JRootPane getRootPane()
  {
    return ((JDialog) getComponent()).getRootPane();
  }

  /**
   * This is the JDialog subclass which is used as the Component for a BDialog.
   */
  
  private class BDialogComponent extends JDialog
  {
    public BDialogComponent()
    {
      super();
    }

    public BDialogComponent(Frame parent, String title, boolean modal)
    {
      super(parent, title, modal);
    }

    public BDialogComponent(Dialog parent, String title, boolean modal)
    {
      super(parent, title, modal);
    }

    public void paintComponent(Graphics g)
    {
      BDialog.this.dispatchEvent(new RepaintEvent(BDialog.this, (Graphics2D) g));
    }
  }
}
