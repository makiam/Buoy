package buoy.widget;

import buoy.xml.*;
import buoy.xml.delegate.*;
import java.awt.*;
import javax.swing.*;

/**
 * A BTextArea is a multi-line text entry box. You can specify a preferred size
 * for the text area in terms of rows and columns, but it automatically expands
 * to be large enough to show all text contained in it. Usually, a BTextArea is
 * used inside a BScrollPane. The settings for numbers of rows and columns are
 * then treated as the preferred size of the visible area, and the scroll bars
 * can be used to scroll through the text.
 * <p>
 * In addition to the event types generated by all Widgets, BTextAreas generate
 * the following event types:
 * <ul>
 * <li>{@link buoy.event.SelectionChangedEvent SelectionChangedEvent}</li>
 * <li>{@link buoy.event.ValueChangedEvent ValueChangedEvent}</li>
 * </ul>
 *
 * @author Peter Eastman
 */
public class BTextArea extends TextWidget<JTextArea> {

    public static final WrapStyle WRAP_NONE = new WrapStyle();
    public static final WrapStyle WRAP_CHARACTER = new WrapStyle();
    public static final WrapStyle WRAP_WORD = new WrapStyle();

    static {
        WidgetEncoder.setPersistenceDelegate(BTextArea.class, new EventSourceDelegate(new String[]{"text"}));
        WidgetEncoder.setPersistenceDelegate(WrapStyle.class, new StaticFieldDelegate(BTextArea.class));
    }

    /**
     * Create an empty BTextArea whose preferred numbers of rows and columns are
     * 0.
     */
    public BTextArea() {
        this(null, 0, 0);
    }

    /**
     * Create a new BTextArea whose preferred numbers of rows and columns are 0.
     *
     * @param text the initial text contained in the text area (may be null)
     */
    public BTextArea(String text) {
        this(text, 0, 0);
    }

    /**
     * Create an empty BTextArea.
     *
     * @param rows the number of rows this text area should be tall enough to
     * display
     * @param columns the number of columns this text area should be wide enough
     * to display
     */
    public BTextArea(int rows, int columns) {
        this(null, rows, columns);
    }

    /**
     * Create a new BTextArea.
     *
     * @param text the initial text contained in the text area (may be null)
     * @param rows the number of rows this text area should be tall enough to
     * display
     * @param columns the number of columns this text area should be wide enough
     * to display
     */
    public BTextArea(String text, int rows, int columns) {
        JTextArea ta = component = createComponent();
        ta.setText(text);
        ta.setRows(rows);
        ta.setColumns(columns);
        ta.addCaretListener(caretListener);
        ta.getDocument().addDocumentListener(documentListener);
    }

    /**
     * Create the JTextArea which serves as this Widget's Component. This method
     * is protected so that subclasses can override it.
     */
    protected JTextArea createComponent() {
        return new JTextArea();
    }

    /**
     * Get the number of rows this text area should be tall enough to display.
     */
    public int getRows() {
        return component.getRows();
    }

    /**
     * Set the number of rows this text area should be tall enough to display.
     */
    public void setRows(int rows) {
        component.setRows(rows);
        invalidateSize();
    }

    /**
     * Get the number of columns this text area should be wide enough to
     * display.
     */
    public int getColumns() {
        return component.getColumns();
    }

    /**
     * Set the number of columns this text area should be wide enough to
     * display.
     */
    public void setColumns(int columns) {
        component.setColumns(columns);
        invalidateSize();
    }

    /**
     * Determine the number of lines of text contained in this text area.
     */
    public int getLineCount() {
        return component.getLineCount();
    }

    /**
     * Get the line wrap style for this text area. This will be equal to
     * WRAP_NONE, WRAP_CHARACTER, or WRAP_WORD.
     */
    public WrapStyle getWrapStyle() {
        if (!component.getLineWrap()) {
            return WRAP_NONE;
        }
        if (component.getWrapStyleWord()) {
            return WRAP_WORD;
        }
        return WRAP_CHARACTER;
    }

    /**
     * Set the line wrap style for this text area. This should be equal to
     * WRAP_NONE, WRAP_CHARACTER, or WRAP_WORD.
     */
    public void setWrapStyle(WrapStyle style) {
        JTextArea ta = component;
        if (style == WRAP_NONE) {
            ta.setLineWrap(false);
        } else if (style == WRAP_CHARACTER) {
            ta.setLineWrap(true);
            ta.setWrapStyleWord(false);
        } else if (style == WRAP_WORD) {
            ta.setLineWrap(true);
            ta.setWrapStyleWord(true);
        }
        invalidateSize();
    }

    /**
     * Get the number of character widths to use for a tab character.
     */
    public int getTabSize() {
        return component.getTabSize();
    }

    /**
     * Set the number of character widths to use for a tab character.
     */
    public void setTabSize(int size) {
        component.setTabSize(size);
    }

    /**
     * Append a String to the text contained in this Widget.
     * <p>
     * This method can be safely called from any thread, not just the event
     * dispatch thread.
     *
     * @param text the text to append
     */
    public void append(String text) {
        try {
            suppressEvents++;
            component.append(text);
        } finally {
            suppressEvents--;
        }
    }

    /**
     * Insert a String into the middle of the text contained in this Widget.
     * <p>
     * This method can be safely called from any thread, not just the event
     * dispatch thread.
     *
     * @param text the text to insert
     * @param pos the position at which to insert it
     */
    public void insert(String text, int pos) {
        try {
            suppressEvents++;
            component.insert(text, pos);
        } finally {
            suppressEvents--;
        }
    }

    /**
     * Replace a part of the text contained in this Widget with a new String.
     * <p>
     * This method can be safely called from any thread, not just the event
     * dispatch thread.
     *
     * @param text the new text with which to replace the specified range
     * @param start the beginning of the range to replace
     * @param end the end of the range to replace
     */
    public void replaceRange(String text, int start, int end) {
        try {
            suppressEvents++;
            component.replaceRange(text, start, end);
        } finally {
            suppressEvents--;
        }
    }

    /**
     * Get the smallest size at which this Widget can reasonably be drawn. When
     * a WidgetContainer lays out its contents, it will attempt never to make
     * this Widget smaller than its minimum size.
     */
    @Override
    public Dimension getMinimumSize() {
        // Workaround for a Swing bug which prevents text areas from being made smaller if wrapping
        // is enabled.

        Dimension min = super.getMinimumSize();
        if (getWrapStyle() != WRAP_NONE) {
            min.width = 0;
        }
        return min;
    }

    /**
     * This method is called whenever the content of the Widget changes. If this
     * text area is contained inside a BScrollPane, we need to update its
     * layout.
     */
    @Override
    protected void textChanged() {
        super.textChanged();
        if (getParent() instanceof BScrollPane) {
            SwingUtilities.invokeLater(() -> getParent().layoutChildren());
        }
    }

    /**
     * This inner class represents a wrapping style.
     */
    public static class WrapStyle {

        private WrapStyle() {
        }
    }
}
