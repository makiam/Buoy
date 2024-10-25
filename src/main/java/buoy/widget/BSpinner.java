package buoy.widget;

import buoy.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;

/**
 * A BSpinner is a Widget that allows the user to select a value from an ordered
 * sequence. It allows the user to enter a value, and also provides a pair of
 * arrows for stepping through the values in the sequence.
 * <p>
 * The list of allowed values is determined by a
 * <code>javax.swing.SpinnerModel</code>. BSpinner provides constructors for
 * handling the most common cases: a range of numbers, a date, or a fixed list
 * of objects. For other cases, you can explicitly set the model by calling
 * <code>setModel()</code>, or by using the constructor which takes a
 * <code>SpinnerModel</code>.
 * <p>
 * In addition to the event types generated by all Widgets, BSpinners generate
 * the following event types:
 * <ul>
 * <li>{@link buoy.event.ValueChangedEvent ValueChangedEvent}</li>
 * </ul>
 *
 * @author Peter Eastman
 */
public class BSpinner extends Widget<JSpinner> {

    private int suppressEvents;

    /**
     * Create a new BSpinner which allows the user to select an arbitrary
     * integer. The initial value is 0.
     */
    public BSpinner() {
        component = createComponent();
        component.addChangeListener(ev -> {
            if (suppressEvents == 0) {
                dispatchEvent(new ValueChangedEvent(BSpinner.this));
            }
        });
    }

    /**
     * Create a new BSpinner.
     *
     * @param model the model which specifies the values for the spinner.
     */
    public BSpinner(SpinnerModel model) {
        this();
        component.setModel(model);
    }

    /**
     * Create a new BSpinner which allows the user to select integers in a fixed
     * range.
     *
     * @param value the initial value
     * @param min the minimum allowed value
     * @param max the maximum allowed value
     * @param step the amount by which the value changes when the user clicks
     * the arrows
     */
    public BSpinner(int value, int min, int max, int step) {
        this(new SpinnerNumberModel(value, min, max, step));
    }

    /**
     * Create a new BSpinner which allows the user to select floating point
     * numbers in a fixed range.
     *
     * @param value the initial value
     * @param min the minimum allowed value
     * @param max the maximum allowed value
     * @param step the amount by which the value changes when the user clicks
     * the arrows
     */
    public BSpinner(double value, double min, double max, double step) {
        this(new SpinnerNumberModel(value, min, max, step));
    }

    /**
     * Create a new BSpinner which allows the user to select a date.
     *
     * @param date the initial value
     */
    public BSpinner(Date date) {
        this(new SpinnerDateModel());
        getModel().setValue(date);
    }

    /**
     * Create a new BSpinner which allows the user to select from a fixed list
     * of objects. The initial value is the first element in the list.
     *
     * @param values the list of allowed values
     */
    public BSpinner(Object[] values) {
        this(new SpinnerListModel(values));
    }

    /**
     * Create the JSpinner which serves as this Widget's Component. This method
     * is protected so that subclasses can override it.
     */
    protected JSpinner createComponent() {
        return new JSpinner();
    }

    /**
     * Get the current value of the spinner.
     */
    public Object getValue() {
        return component.getValue();
    }

    /**
     * Set the current value of the spinner.
     */
    public void setValue(Object value) {
        try {
            suppressEvents++;
            component.setValue(value);
        } finally {
            suppressEvents--;
        }
    }

    /**
     * If the user adjusts the spinner by typing a value (rather than clicking
     * the arrows), the new value is not actually parsed and "committed" until
     * they press return. Call this method to immediately commit an edited
     * value.
     * <p>
     * Note: calling commitEdit() will <i>not</i> generate a ValueChangedEvent.
     */
    public void commitEdit() throws ParseException {
        component.commitEdit();
    }

    /**
     * Get the model for this spinner.
     */
    public SpinnerModel getModel() {
        return component.getModel();
    }

    /**
     * Set the model for this spinner.
     */
    public void setModel(SpinnerModel model) {
        component.setModel(model);
    }
}
