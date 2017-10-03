package buoy.event;

import buoy.widget.*;
import java.awt.event.*;

/**
 * This class defines an event caused by the mouse interacting with a Widget. It
 * is an abstract class, with subclasses for specific types of events.
 *
 * @author Peter Eastman
 */
public abstract class WidgetMouseEvent extends MouseEvent implements WidgetEvent {

    private Widget widget;

    /**
     * Create a WidgetMouseEvent.
     *
     * @param source the Widget which generated this event
     * @param id the event ID
     * @param when the time at which the event occurred
     * @param modifiers describes the state of various keys and buttons at the
     * time when the event occurred (a sum of the constants defined by
     * InputEvent)
     * @param x the x coordinate at which the event occurred
     * @param y the y coordinate at which the event occurred
     * @param clickCount the number of successive times the mouse has been
     * clicked
     * @param popupTrigger true if this event corresponds to the
     * platform-specific trigger for displaying popup menus
     * @param button the flag for the button which has just changed state, or
     * NOBUTTON if this event was was not generated by a button changing state
     */
    protected WidgetMouseEvent(Widget source, int id, long when, int modifiers, int x, int y, int clickCount, boolean popupTrigger, int button) {
        super(source.getComponent(), id, when, modifiers, x, y, clickCount, popupTrigger, button);
        widget = source;
    }

    /**
     * Get the object which generated this event.
     */
    public Object getSource() {
        // The superclass requires the source to be a Component.  This is overridden so getSource()
        // will still return the Widget itself.

        return widget;
    }

    /**
     * Get the Widget which generated this event.
     */
    public Widget getWidget() {
        return widget;
    }
}
