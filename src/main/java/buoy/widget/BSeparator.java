package buoy.widget;

import buoy.xml.*;
import buoy.xml.delegate.*;

import javax.swing.*;

/**
 * A BSeparator is a Widget corresponding to a divider line between parts of a
 * container. They are most often used in BMenus and BToolBars to divide menu
 * items or toolbar icons into related groups. They can also be used anywhere
 * you want to create a horizontal or vertical divider between Widgets.
 *
 * @author Peter Eastman
 */
public class BSeparator extends Widget<JSeparator> implements MenuWidget {

    public static final Orientation HORIZONTAL = new Orientation(SwingConstants.HORIZONTAL);
    public static final Orientation VERTICAL = new Orientation(SwingConstants.VERTICAL);

    static {
        WidgetEncoder.setPersistenceDelegate(Orientation.class, new StaticFieldDelegate(BSeparator.class));
    }

    /**
     * Create a new BSeparator whose orientation is set to HORIZONTAL.
     */
    public BSeparator() {
        this(HORIZONTAL);
    }

    /**
     * Create a new BSeparator.
     */
    public BSeparator(Orientation orientation) {
        component = createComponent();
        setOrientation(orientation);
    }

    /**
     * Create the JSeparator which serves as this Widget's Component. This
     * method is protected so that subclasses can override it.
     */
    protected JSeparator createComponent() {
        return new JSeparator();
    }

    @Override
    public JSeparator getComponent() {
        return component;
    }

    /**
     * Get the orientation of this separator.
     */
    public Orientation getOrientation() {
        return component.getOrientation() == SwingConstants.HORIZONTAL ? HORIZONTAL : VERTICAL;
    }

    /**
     * Set the orientation of this separator.
     */
    public void setOrientation(Orientation orientation) {
        component.setOrientation(orientation.value);
    }

    /**
     * This inner class represents an orientation for the separator.
     */
    public static class Orientation {

        private int value;

        private Orientation(int value) {
            this.value = value;
        }
    }
}
