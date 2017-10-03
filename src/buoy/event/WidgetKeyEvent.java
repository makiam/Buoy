package buoy.event;

import buoy.widget.*;
import java.awt.event.*;

/**
 * This class defines an event caused by a keyboard action. It is sent to the
 * Widget that has keyboard focus. This is an abstract class, with subclasses
 * for particular types of events.
 *
 * @author Peter Eastman
 */
public abstract class WidgetKeyEvent extends KeyEvent implements WidgetEvent {

    private final Widget widget;

    /**
     * Create a WidgetMouseEvent.
     *
     * @param source the Widget which generated this event
     * @param id the event ID
     * @param when the time at which the event occurred
     * @param modifiers describes the state of various keys and buttons at the
     * time when the event occurred (a sum of the constants defined by
     * InputEvent)
     * @param keyCode specifies which key on the keyboard generated the event.
     * This should be one of the VK_ constants.
     * @param keyChar the Unicode character generated by this event, or
     * CHAR_UNDEFINED
     */
    protected WidgetKeyEvent(Widget source, int id, long when, int modifiers, int keyCode, char keyChar) {
        super(source.getComponent(), id, when, modifiers, keyCode, keyChar);
        widget = source;
    }

    /**
     * Get the object which generated this event.
     */
    @Override
    public Object getSource() {
        // The superclass requires the source to be a Component.  This is overridden so getSource()
        // will still return the Widget itself.

        return widget;
    }

    /**
     * Get the Widget which generated this event.
     */
    @Override
    public Widget getWidget() {
        return widget;
    }
}
