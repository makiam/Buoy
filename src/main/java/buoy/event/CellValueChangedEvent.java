package buoy.event;

import buoy.widget.Widget;
import java.util.EventObject;

/**
 * A CellValueChangedEvent is generated when the user edits the value in a cell
 * of a BTable.
 *
 * @author Peter Eastman
 */
public class CellValueChangedEvent extends EventObject implements WidgetEvent {

    private final Widget widget;
    private final int row;
    private final int col;

    /**
     * Create a CellValueChangedEvent.
     *
     * @param widget the Widget whose value has changed
     * @param row the row containing the cell whose value was edited
     * @param col the column containing the cell whose value was edited
     */
    public CellValueChangedEvent(Widget widget, int row, int col) {
        super(widget);
        this.widget = widget;
        this.row = row;
        this.col = col;
    }

    /**
     * Get the Widget which generated this event.
     */
    @Override
    public Widget getWidget() {
        return widget;
    }

    /**
     * Get the row containing the cell whose value was edited.
     */
    public int getRow() {
        return row;
    }

    /**
     * Get the column containing the cell whose value was edited.
     */
    public int getColumn() {
        return col;
    }
}
