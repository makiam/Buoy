package buoy.event;

import buoy.widget.Widget;
import java.awt.event.FocusEvent;

/**
 * This is an event corresponding to a Widget losing keyboard focus.
 *
 * @author Peter Eastman
 */
public class FocusLostEvent extends WidgetFocusEvent {

    /**
     * Create a FocusLostEvent.
     *
     * @param source the Widget which generated this event
     * @param temporary specifies whether this represents a permanent or
     * temporary change in focus state
     */

    public FocusLostEvent(Widget source, boolean temporary) {
        super(source, FocusEvent.FOCUS_LOST, temporary);
    }
}
