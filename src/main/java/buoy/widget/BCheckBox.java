package buoy.widget;

import buoy.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * A BCheckBox is a Widget for making simple boolean selections. Clicking it
 * with the mouse toggles it on and off.
 * <p>
 * In addition to the event types generated by all Widgets, BCheckBoxes generate
 * the following event types:
 * <ul>
 * <li>{@link buoy.event.ValueChangedEvent ValueChangedEvent}</li>
 * </ul>
 *
 * @author Peter Eastman
 */
public class BCheckBox extends Widget<JCheckBox> {

    /**
     * Create a new BCheckBox with no label, which is initially deselected.
     */

    public BCheckBox() {
        this(null, false);
    }

    /**
     * Create a new BCheckBox.
     *
     * @param text the text to display on the BCheckBox
     * @param state the initial selection state of the BCheckBox
     */
    public BCheckBox(String text, boolean state) {
        component = createComponent(text, state);
        component.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ev) {
                dispatchEvent(new ValueChangedEvent(BCheckBox.this));
            }
        });
    }

    /**
     * Create the JCheckBox which serves as this Widget's Component. This method
     * is protected so that subclasses can override it.
     *
     * @param text the text to display on the BCheckBox
     * @param state the initial selection state of the BCheckBox
     */
    protected JCheckBox createComponent(String text, boolean state) {
        return new JCheckBox(text, state);
    }

    @Override
    public JCheckBox getComponent() {
        return component;
    }

    /**
     * Get the selection state of this check box.
     */
    public boolean getState() {
        return component.isSelected();
    }

    /**
     * Set the selection state of this check box.
     */
    public void setState(boolean selected) {
        component.setSelected(selected);
    }

    /**
     * Get the text which appears on this check box.
     */
    public String getText() {
        return component.getText();
    }

    /**
     * Set the text which appears on this check box.
     */
    public void setText(String text) {
        component.setText(text);
        invalidateSize();
    }

    /**
     * Get the largest size at which this Widget can reasonably be drawn. When a
     * WidgetContainer lays out its contents, it will attempt never to make this
     * Widget larger than its maximum size.
     */
    @Override
    public Dimension getMaximumSize() {
        return new Dimension(32767, 32767);
    }
}
