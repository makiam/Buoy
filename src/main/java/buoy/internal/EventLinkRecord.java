package buoy.internal;

import java.lang.reflect.*;
import java.util.*;

/**
 * This class stores a list of methods to be invoked whenever an Event of a
 * particular class is generated by a Widget.
 *
 * @author Peter Eastman
 */
public class EventLinkRecord {

    private final Class eventClass;
    private final List<Object> targetList = new ArrayList<>();
    private final List<Method> targetMethodList = new ArrayList<>();
    private final List<Boolean> argsList = new ArrayList<>();

    /**
     * Create an EventLinkRecord for storing links for a particular event class.
     */
    public EventLinkRecord(Class eventType) {
        eventClass = eventType;
    }

    /**
     * Get the event class for this record.
     */
    public Class getEventType() {
        return eventClass;
    }

    /**
     * Add a new target to be notified of events of this type.
     *
     * @param target the target object to be notified of events
     * @param method the method to be invoked on the target when events occur
     */
    public void addLink(Object target, Method method) {
        targetList.add(target);
        targetMethodList.add(method);
        argsList.add(method.getParameterTypes().length == 0 ? Boolean.FALSE : Boolean.TRUE);
    }

    /**
     * Remove an object from the list of targets to be notified of events of
     * this type.
     *
     * @param target the target object to remove
     */
    public void removeLink(Object target) {
        int index = targetList.indexOf(target);
        if (index == -1) {
            return;
        }
        targetList.remove(index);
        targetMethodList.remove(index);
        argsList.remove(index);
    }

    /**
     * Send an event to every target which has been added to this record.
     */
    public void dispatchEvent(Object event) {
        for (int i = 0; i < targetList.size(); i++) {
            try {
                if (argsList.get(i)) {
                    targetMethodList.get(i).invoke(targetList.get(i), event);
                } else {
                    targetMethodList.get(i).invoke(targetList.get(i));
                }
            } catch (InvocationTargetException ex) {
                ex.getCause().printStackTrace();
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            } catch(IllegalArgumentException iae) {
                iae.printStackTrace();
            }
        }
    }
}
