package buoy.widget;

import buoy.event.*;
import buoy.xml.*;
import buoy.xml.delegate.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * A BList is a Widget that displays a list of objects for the user to select.
 * Typically the objects are Strings, but other types of objects can be used as
 * well. It supports both single and multiple selection modes. There are methods
 * for adding and removing objects in the list. Alternatively, you can set a
 * ListModel to provide more complex behaviors.
 * <p>
 * BList does not provide scrolling automatically. Normally, it is used inside a
 * BScrollPane.
 * <p>
 * In addition to the event types generated by all Widgets, BLists generate the
 * following event types:
 * <ul>
 * <li>{@link buoy.event.SelectionChangedEvent SelectionChangedEvent}</li>
 * </ul>
 *
 * @author Peter Eastman
 */
public class BList extends Widget {

    protected DefaultListModel defaultModel;

    static {
        WidgetEncoder.setPersistenceDelegate(BList.class, new BListDelegate());
    }

    /**
     * Create a new BList containing no objects.
     */
    public BList() {
        defaultModel = new DefaultListModel();
        component = createComponent();
        getComponent().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent ev) {
                dispatchEvent(new SelectionChangedEvent(BList.this, ev.getValueIsAdjusting()));
            }
        });
    }

    /**
     * Create a new BList containing the objects in an array.
     */
    public BList(Object contents[]) {
        this();
        setContents(contents);
    }

    /**
     * Create a new BList containing the objects in a Collection. The objects
     * will be added in the order they are returned by the Collection's
     * Iterator.
     */
    public BList(Collection contents) {
        this();
        setContents(contents);
    }

    /**
     * Create a new BList whose contents are determined by a ListModel.
     */
    public BList(ListModel model) {
        this();
        getComponent().setModel(model);
    }

    /**
     * Create the JList which serves as this Widget's Component. This method is
     * protected so that subclasses can override it.
     */
    protected JList createComponent() {
        return new JList(defaultModel);
    }

    @Override
    public JList getComponent() {
        return (JList) component;
    }

    /**
     * Set the contents of the list to the objects in an array. This completely
     * replaces the contents of the list, removing any objects that were
     * previously in it.
     * <p>
     * If you have set a custom model for this list, either by passing it to the
     * constructor or by calling setModel(), this method has no effect.
     *
     * @param o the objects to put in the list
     */
    public void setContents(Object o[]) {
        defaultModel.clear();
        for (int i = 0; i < o.length; i++) {
            defaultModel.addElement(o[i]);
        }
        updateScrollPane();
    }

    /**
     * Set the contents of the list to the objects in a Collection. This
     * completely replaces the contents of the list, removing any objects that
     * were previously in it. The objects will be added in the order they are
     * returned by the Collection's Iterator.
     * <p>
     * If you have set a custom model for this list, either by passing it to the
     * constructor or by calling setModel(), this method has no effect.
     *
     * @param c the objects to put in the list
     */
    public void setContents(Collection c) {
        defaultModel.clear();
        for (Object obj : c) {
            defaultModel.addElement(obj);
        }
        updateScrollPane();
    }

    /**
     * Add an object to the end of the list.
     * <p>
     * If you have set a custom model for this list, either by passing it to the
     * constructor or by calling setModel(), this method has no effect.
     *
     * @param o the object to add
     */
    public void add(Object o) {
        defaultModel.addElement(o);
        updateScrollPane();
    }

    /**
     * Add an object at specified position in the middle of the list.
     * <p>
     * If you have set a custom model for this list, either by passing it to the
     * constructor or by calling setModel(), this method has no effect.
     *
     * @param index the position at which to add the object
     * @param o the object to add
     */
    public void add(int index, Object o) {
        defaultModel.add(index, o);
        updateScrollPane();
    }

    /**
     * Replace the object at a specified position in the list with a new one.
     * <p>
     * If you have set a custom model for this list, either by passing it to the
     * constructor or by calling setModel(), this method has no effect.
     *
     * @param index the position at which to set the object
     * @param o the new object to add
     */
    public void replace(int index, Object o) {
        defaultModel.set(index, o);
        updateScrollPane();
    }

    /**
     * Remove an object from the list.
     * <p>
     * If you have set a custom model for this list, either by passing it to the
     * constructor or by calling setModel(), this method has no effect.
     *
     * @param index the position from which to remove the object
     */
    public void remove(int index) {
        defaultModel.remove(index);
        updateScrollPane();
    }

    /**
     * Remove all objects from the list.
     * <p>
     * If you have set a custom model for this list, either by passing it to the
     * constructor or by calling setModel(), this method has no effect.
     */
    public void removeAll() {
        defaultModel.clear();
        updateScrollPane();
    }

    /**
     * Get the ListModel which controls the contents of this BList.
     */
    public ListModel getModel() {
        return getComponent().getModel();
    }

    /**
     * Set the ListModel which controls the contents of this BList.
     */
    public void setModel(ListModel model) {
        getComponent().setModel(model);
    }

    /**
     * If this BList is a child of a BScrollPane, update the parent whenever the
     * contents of the list changes.
     */
    private void updateScrollPane() {
        invalidateSize();
        if (!(getParent() instanceof BScrollPane)) {
            return;
        }
        BScrollPane sp = (BScrollPane) getParent();
        BScrollBar sb = sp.getVerticalScrollBar();
        if (sb != null) {
            sb.setMaximum(getComponent().getHeight());
        }
        sb = sp.getHorizontalScrollBar();
        if (sb != null) {
            sb.setMaximum(getComponent().getWidth());
        }
    }

    /**
     * Get the number of items in the list.
     */
    public int getItemCount() {
        return getComponent().getModel().getSize();
    }

    /**
     * Get the item at a specific position in the list.
     */
    public Object getItem(int index) {
        return getComponent().getModel().getElementAt(index);
    }

    /**
     * Determine whether this list allows multiple objects to be selected.
     */
    public boolean isMultipleSelectionEnabled() {
        return (getComponent().getSelectionMode() != ListSelectionModel.SINGLE_SELECTION);
    }

    /**
     * Set whether this list should allow multiple objects to be selected.
     */
    public void setMultipleSelectionEnabled(boolean multiple) {
        getComponent().setSelectionMode(multiple ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION : ListSelectionModel.SINGLE_SELECTION);
    }

    /**
     * Get the preferred number of rows which should be visible without using a
     * scrollbar.
     */
    public int getPreferredVisibleRows() {
        return getComponent().getVisibleRowCount();
    }

    /**
     * Set the preferred number of rows which should be visible without using a
     * scrollbar.
     */
    public void setPreferredVisibleRows(int rows) {
        getComponent().setVisibleRowCount(rows);
        invalidateSize();
    }

    /**
     * Determine whether a particular object in the list is selected.
     */
    public boolean isSelected(int index) {
        return getComponent().isSelectedIndex(index);
    }

    /**
     * Get the index of the first selected object, or -1 if nothing is selected.
     */
    public int getSelectedIndex() {
        return getComponent().getSelectedIndex();
    }

    /**
     * Get the indices of all selected objects, in increasing order.
     */
    public int[] getSelectedIndices() {
        return getComponent().getSelectedIndices();
    }

    /**
     * Get the first selected object, or null if nothing is selected.
     */
    public Object getSelectedValue() {
        return getComponent().getSelectedValue();
    }

    /**
     * Get an array of all selected objects, in order of increasing index.
     */
    public Object[] getSelectedValues() {
        return getComponent().getSelectedValues();
    }

    /**
     * Deselect all objects in the list.
     */
    public void clearSelection() {
        getComponent().clearSelection();
    }

    /**
     * Set whether a particular object should be selected.
     */
    public void setSelected(int index, boolean selected) {
        if (selected) {
            getComponent().addSelectionInterval(index, index);
        } else {
            getComponent().removeSelectionInterval(index, index);
        }
    }

    /**
     * Scroll the BList's parent BScrollPane to ensure that a particular list
     * entry is visible. If the parent is not a BScrollPane, the results of
     * calling this method are undefined, but usually it will have no effect at
     * all.
     */
    public void scrollToItem(int index) {
        getComponent().ensureIndexIsVisible(index);
    }

    /**
     * Get the largest size at which this Widget can reasonably be drawn. When a
     * WidgetContainer lays out its contents, it will attempt never to make this
     * Widget larger than its maximum size.
     */
    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }
}
