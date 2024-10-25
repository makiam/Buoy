package buoy.widget;

import buoy.event.*;
import buoy.xml.*;
import buoy.xml.delegate.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * A BScrollBar is a Widget that allows the user to select a single value by
 * dragging a "thumb" along a bar. The size of the thumb can be variable, so as
 * to represent a selected region of the range. The size and position of the
 * thumb are determined by four properties:
 * <p>
 * minimum: the lower end of the range represented by the scrollbar<br>
 * value: the lower end of the "selected region" represented by the thumb<br>
 * extent: the size of the "selected region" represented by the thumb<br>
 * maximum: the upper end of the range represented by the scrollbar
 * <p>
 * Thus, the "value" of the scrollbar can vary between minimum and
 * maximum-extent, inclusive.
 * <p>
 * In addition to the event types generated by all Widgets, BScrollBars generate
 * the following event types:
 * <ul>
 * <li>{@link buoy.event.ValueChangedEvent ValueChangedEvent}</li>
 * </ul>
 *
 * @author Peter Eastman
 */
public class BScrollBar extends Widget<JScrollBar> {

    private DefaultBoundedRangeModel model;
    private int suppressEvents;

    public static final Orientation HORIZONTAL = new Orientation(Adjustable.HORIZONTAL);
    public static final Orientation VERTICAL = new Orientation(Adjustable.VERTICAL);

    static {
        WidgetEncoder.setPersistenceDelegate(Orientation.class, new StaticFieldDelegate(BScrollBar.class));
    }

    /**
     * Create a new BScrollBar.
     */
    public BScrollBar() {
        this(0, 10, 0, 100, VERTICAL);
    }

    /**
     * Create a new BScrollBar.
     *
     * @param value the BScrollBar's initial value
     * @param extent the size of the region represented by the thumb
     * @param minimum the minimum value for the range represented by the
     * BScrollBar
     * @param maximum the maximum value for the range represented by the
     * BScrollBar
     * @param orientation defines how the BScrollBar should be drawn and
     * positioned. This should be HORIZONTAL or VERTICAL.
     */
    public BScrollBar(int value, int extent, int minimum, int maximum, Orientation orientation) {
        component = createComponent(orientation);
        getComponent().setModel(model = new DefaultBoundedRangeModel(value, extent, minimum, maximum));
    }

    /**
     * Create the JScrollBar which serves as this Widget's Component. This
     * method is protected so that subclasses can override it.
     *
     * @param orientation defines how the BScrollBar should be drawn and
     * positioned. This should be HORIZONTAL or VERTICAL.
     */
    protected JScrollBar createComponent(Orientation orientation) {
        return new BScrollBarComponent(orientation);
    }

    /**
     * Get the minimum value of the range represented by this BScrollBar.
     */
    public int getMinimum() {
        return model.getMinimum();
    }

    /**
     * Set the minimum value of the range represented by this BScrollBar.
     */
    public void setMinimum(int value) {
        model.setMinimum(value);
    }

    /**
     * Get the maximum value of the range represented by this BScrollBar.
     */
    public int getMaximum() {
        return model.getMaximum();
    }

    /**
     * Set the maximum value of the range represented by this BScrollBar.
     */
    public void setMaximum(int value) {
        model.setMaximum(value);
    }

    /**
     * Get the current value of this BScrollBar.
     */
    public int getValue() {
        return model.getValue();
    }

    /**
     * Set the current value of this BScrollBar.
     */
    public void setValue(int value) {
        try {
            suppressEvents++;
            model.setValue(value);
        } finally {
            suppressEvents--;
        }
    }

    /**
     * Get the extent of this BScrollBar's thumb.
     */
    public int getExtent() {
        return model.getExtent();
    }

    /**
     * Set the extent of this BScrollBar's thumb.
     */
    public void setExtent(int value) {
        model.setExtent(value);
    }

    /**
     * Get the orientation (HORIZONTAL or VERTICAL) of this BScrollBar.
     */
    public Orientation getOrientation() {
        return component.getOrientation() == HORIZONTAL.value ? HORIZONTAL : VERTICAL;
    }

    /**
     * Set the orientation (HORIZONTAL or VERTICAL) of this BScrollBar.
     */
    public void setOrientation(Orientation orientation) {
        component.setOrientation(orientation.value);
        invalidateSize();
    }

    /**
     * Get the amount by which this BScrollBar will scroll when the user clicks
     * on one of the arrows at its end.
     */
    public int getUnitIncrement() {
        return component.getUnitIncrement();
    }

    /**
     * Get the amount by which this BScrollBar will scroll when the user clicks
     * on one of the arrows at its end, to scroll in a particular direction.
     * Normally, this method simply returns the same value as the public
     * getUnitIncrement() method. Subclasses may override it to provide more
     * complex behavior, such as scrolling through a set of variable size
     * objects.
     *
     * @param direction the direction in which the user has asked to scroll (-1
     * for up, 1 for down)
     */
    protected int getUnitIncrement(int direction) {
        return getUnitIncrement();
    }

    /**
     * Set the amount by which this BScrollBar will scroll when the user clicks
     * on one of the arrows at its end.
     */
    public void setUnitIncrement(int increment) {
        component.setUnitIncrement(increment);
    }

    /**
     * Get the amount by which this BScrollBar will scroll when the user clicks
     * in the body of the scrollbar.
     */
    public int getBlockIncrement() {
        return component.getBlockIncrement();
    }

    /**
     * Get the amount by which this BScrollBar will scroll when the user clicks
     * in the body of the scrollbar, to scroll in a particular direction.
     * Normally, this method simply returns the same value as the public
     * getBlockIncrement() method. Subclasses may override it to provide more
     * complex behavior, such as scrolling through a set of variable size
     * objects.
     *
     * @param direction the direction in which the user has asked to scroll (-1
     * for up, 1 for down)
     */
    protected int getBlockIncrement(int direction) {
        return getBlockIncrement();
    }

    /**
     * Set the amount by which this BScrollBar will scroll when the user clicks
     * in the body of the scrollbar.
     */
    public void setBlockIncrement(int increment) {
        component.setBlockIncrement(increment);
    }

    /**
     * This is the JScrollBar subclass which is used as the Component for
     * BScrollbar.
     */
    private class BScrollBarComponent extends JScrollBar implements AdjustmentListener {

        public BScrollBarComponent(Orientation orientation) {
            super(orientation.value);
            addAdjustmentListener(this);
        }

        @Override
        public void adjustmentValueChanged(AdjustmentEvent ev) {
            if (suppressEvents == 0) {
                BScrollBar.this.dispatchEvent(new ValueChangedEvent(BScrollBar.this, BScrollBar.this.getComponent().getValueIsAdjusting()));
            }
        }

        @Override
        public int getUnitIncrement(int direction) {
            return BScrollBar.this.getUnitIncrement(direction);
        }

        @Override
        public int getBlockIncrement(int direction) {
            return BScrollBar.this.getBlockIncrement(direction);
        }
    }

    /**
     * This inner class represents an orientation for the scrollbar.
     */
    public static class Orientation {

        protected int value;

        private Orientation(int value) {
            this.value = value;
        }
    }
}
