package buoy.widget;

import buoy.event.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.lang.ref.*;
import javax.swing.*;

/**
 * A BFrame is a WidgetContainer corresponding to a main window. It may contain
 * up to two child Widgets: a BMenuBar, and a single other Widget (usually a
 * WidgetContainer of some sort) which fills the rest of the window.
 * <p>
 * In addition to the event types generated by all Widgets, BFrames generate the
 * following event types:
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
public class BFrame extends WindowWidget<JFrame> {

    private BMenuBar menubar;
    private ImageIcon icon;
    private static WeakHashMap<Frame, WeakReference<BFrame>> frameMap = new WeakHashMap<Frame, WeakReference<BFrame>>();

    /**
     * Create a new BFrame.
     */
    public BFrame() {
        component = createComponent();
        component.getContentPane().setLayout(null);
        component.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frameMap.put(component, new WeakReference<>(this));
    }

    /**
     * Create a new BFrame.
     */
    public BFrame(String title) {
        this();
        component.setTitle(title);
    }

    /**
     * Create the JFrame which serves as this Widget's Component. This method is
     * protected so that subclasses can override it.
     */
    protected JFrame createComponent() {
        return new BFrameComponent();
    }

    /**
     * Get the number of children in this container.
     */
    @Override
    public int getChildCount() {
        return ((menubar == null ? 0 : 1) + (content == null ? 0 : 1));
    }

    /**
     * Get a Collection containing all child Widgets of this container.
     */
    @Override
    public Collection<Widget<?>> getChildren() {
        List<Widget<?>> ls = new ArrayList<>(3);
        if (menubar != null) {
            ls.add(menubar);
        }
        if (content != null) {
            ls.add(content);
        }
        return ls;
    }

    /**
     * Get the BMenuBar for this window.
     */
    public BMenuBar getMenuBar() {
        return menubar;
    }

    /**
     * Set the BMenuBar for this window.
     */
    public void setMenuBar(BMenuBar menus) {
        if (menubar != null) {
            remove(menubar);
        }
        if (menus == null) {
            return;
        }
        if (menus.getParent() != null) {
            menus.getParent().remove(menus);
        }
        menubar = menus;
        component.setJMenuBar(menubar.getComponent());
        setAsParent(menubar);
    }

    /**
     * Remove a child Widget from this container.
     */
    @Override
    public void remove(Widget widget) {
        if (menubar == widget) {
            component.setJMenuBar(null);
            removeAsParent(menubar);
            menubar = null;
        } else if (content == widget) {
            component.getContentPane().remove(widget.getComponent());
            removeAsParent(content);
            content = null;
        }
    }

    /**
     * Remove all child Widgets from this container.
     */
    @Override
    public void removeAll() {
        if (menubar != null) {
            remove(menubar);
        }
        if (content != null) {
            remove(content);
        }
    }

    /**
     * Get the title of the window.
     */
    public String getTitle() {
        return component.getTitle();
    }

    /**
     * Set the title of the window.
     */
    public void setTitle(String title) {
        component.setTitle(title);
    }

    /**
     * Determine whether this window may be resized by the user.
     */
    public boolean isResizable() {
        return component.isResizable();
    }

    /**
     * Set whether this window may be resized by the user.
     */
    public void setResizable(boolean resizable) {
        component.setResizable(resizable);
    }

    /**
     * Determine whether this window has been iconified. The precise behavior of
     * an iconified window is platform specific, but it generally causes the
     * window to be hidden and replaced by an icon on the desktop or elsewhere
     * on the screen.
     */
    public boolean isIconified() {
        return (component.getExtendedState() & Frame.ICONIFIED) != 0;
    }

    /**
     * Set whether this window is iconified. The precise behavior of an
     * iconified window is platform specific, but it generally causes the window
     * to be hidden and replaced by an icon on the desktop or elsewhere on the
     * screen.
     */
    public void setIconified(boolean iconified) {
        JFrame jf = component;
        int state = jf.getExtendedState();
        if (iconified) {
            jf.setExtendedState(state | Frame.ICONIFIED);
        } else {
            jf.setExtendedState(state - state & Frame.ICONIFIED);
        }
    }

    /**
     * Get the image which should be used to represent this window when it is
     * iconified. Note that the behavior of iconified windows is platform
     * specific, and some platforms may ignore the image you set. Also, some
     * platforms may display this image in the title bar of the window.
     * <p>
     * This may be null, in which case the platform-specific default image will
     * be used.
     */
    public ImageIcon getIcon() {
        return icon;
    }

    /**
     * Set the image which should be used to represent this window when it is
     * iconified. Note that the behavior of iconified windows is platform
     * specific, and some platforms may ignore the image you set. Also, some
     * platforms may display this image in the title bar of the window.
     * <p>
     * This may be null, in which case the platform-specific default image will
     * be used.
     */
    public void setIcon(ImageIcon icon) {
        this.icon = icon;
        component.setIconImage(icon.getImage());
    }

    /**
     * Determine whether this window has been maximized. The precise behavior of
     * a maximized window is platform specific, but it generally causes the
     * window to expand to fill the entire screen.
     */
    public boolean isMaximized() {
        return (component.getExtendedState() & Frame.MAXIMIZED_BOTH) != 0;
    }

    /**
     * Set whether this window is maximized. The precise behavior of a maximized
     * window is platform specific, but it generally causes the window to expand
     * to fill the entire screen.
     */
    public void setMaximized(boolean maximized) {
        JFrame jf = component;
        int state = jf.getExtendedState();
        if (maximized) {
            jf.setExtendedState(state | Frame.MAXIMIZED_BOTH);
        } else {
            jf.setExtendedState(state - state & Frame.MAXIMIZED_BOTH);
        }
        lastSize = jf.getSize();
    }

    /**
     * Get the JRootPane for this Widget's component.
     */
    @Override
    protected JRootPane getRootPane() {
        return component.getRootPane();
    }

    /**
     * Get a list of all BFrames that currently exist.
     */
    public static List<BFrame> getFrames() {
        ArrayList<BFrame> list = new ArrayList<>();
        for (Frame frame : Frame.getFrames()) {
            WeakReference<BFrame> ref = frameMap.get(frame);
            if (ref != null) {
                BFrame bf = ref.get();
                if (bf != null) {
                    list.add(bf);
                }
            }
        }
        return list;
    }

    /**
     * This is the JFrame subclass which is used as the Component for a BFrame.
     */
    private class BFrameComponent extends JFrame {

        public BFrameComponent() {
            super();
        }

        public void paintComponent(Graphics g) {
            BFrame.this.dispatchEvent(new RepaintEvent(BFrame.this, (Graphics2D) g));
        }

        @Override
        public void validate() {
            super.validate();
            layoutChildren();
            if (!BFrame.this.getComponent().getSize().equals(lastSize)) {
                lastSize = BFrame.this.getComponent().getSize();
                EventQueue.invokeLater(() -> BFrame.this.dispatchEvent(new WindowResizedEvent(BFrame.this)));
            }
        }
    }
}
