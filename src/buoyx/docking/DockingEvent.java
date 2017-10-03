package buoyx.docking;

import java.util.*;

/**
 * A DockingEvent is generated by a {@link DockingContainer} whenever a
 * {@link DockableWidget} is dragged into it, out of it, or to a new location
 * inside it.
 *
 * @author Peter Eastman
 */
public class DockingEvent {

    private DockingContainer source, target;
    private DockableWidget widget[];

    /**
     * Create a new DockingEvent.
     *
     * @param sourceContainer the container from which one or more
     * DockableWidgets were dragged
     * @param targetContainer the container into which the DockableWidgets were
     * dragged
     * @param widgets the list of DockableWidgets which were moved
     */
    public DockingEvent(DockingContainer sourceContainer, DockingContainer targetContainer, DockableWidget widgets[]) {
        source = sourceContainer;
        target = targetContainer;
        widget = widgets;
    }

    /**
     * Get the DockingContainer from which one or more DockableWidgets were
     * dragged.
     */
    public DockingContainer getSourceContainer() {
        return source;
    }

    /**
     * Get the DockingContainer into which the DockableWidgets were dragged.
     * This may be the same as the source container, if the widgets were simply
     * moved to a new location within the same container.
     */
    public DockingContainer getTargetContainer() {
        return target;
    }

    /**
     * Get the list of DockableWidgets which were moved.
     */
    public List getMovedWidgets() {
        return Collections.unmodifiableList(Arrays.asList(widget));
    }
}
