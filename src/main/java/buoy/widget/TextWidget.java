package buoy.widget;

import buoy.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

/**
 * A TextWidget is a Widget that allows the user to view and edit text. This is
 * an abstract class. Subclasses implement specific types of text editing
 * Widgets
 *
 * @author Peter Eastman
 */
public abstract class TextWidget<T extends JTextComponent> extends Widget<T> {

    protected CaretListener caretListener;
    protected DocumentListener documentListener;
    protected int suppressEvents;

    /**
     * The constructor creates a CaretListener and a DocumentListener which
     * dispatch appropriate events in response to user actions. Subclasses of
     * TextWidget need to attach these listeners to their components.
     */
    protected TextWidget() {
        caretListener = e -> caretMoved();
        documentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                textChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                textChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                textChanged();
            }
        };
    }

    @Override
    public T getComponent() {
        return component;
    }

    /**
     * Get the text contained in the Widget.
     */
    public String getText() {
        return component.getText();
    }

    /**
     * Set the text contained in the Widget.
     * <p>
     * This method can be safely called from any thread, not just the event
     * dispatch thread.
     */
    public void setText(String text) {
        try {
            suppressEvents++;
            component.setText(text);
        } finally {
            suppressEvents--;
        }
    }

    /**
     * Get the number of characters in the text contained in the Widget.
     */
    public int getLength() {
        return component.getDocument().getLength();
    }

    /**
     * Get the current position of the caret.
     */
    public int getCaretPosition() {
        return component.getCaretPosition();
    }

    /**
     * Set the current position of the caret.
     */
    public void setCaretPosition(int pos) {
        try {
            suppressEvents++;
            component.setCaretPosition(pos);
        } finally {
            suppressEvents--;
        }
    }

    /**
     * Get the start of the selected range. This is the index of the first
     * selected character.
     */
    public int getSelectionStart() {
        return component.getSelectionStart();
    }

    /**
     * Set the start of the selected range. This is the index of the first
     * selected character.
     */
    public void setSelectionStart(int pos) {
        try {
            suppressEvents++;
            component.setSelectionStart(pos);
        } finally {
            suppressEvents--;
        }
    }

    /**
     * Get the end of the selected range. This is the index of the first
     * character after the end of the selection.
     */
    public int getSelectionEnd() {
        return component.getSelectionEnd();
    }

    /**
     * Set the end of the selected range. This is the index of the first
     * character after the end of the selection.
     */
    public void setSelectionEnd(int pos) {
        try {
            suppressEvents++;
            component.setSelectionEnd(pos);
        } finally {
            suppressEvents--;
        }
    }

    /**
     * Get the selected text. This returns null if no text is currently
     * selected.
     */
    public String getSelectedText() {
        return component.getSelectedText();
    }

    /**
     * Determine whether the user can edit the text contained in this text
     * field.
     */
    public boolean isEditable() {
        return component.isEditable();
    }

    /**
     * Set whether the user can edit the text contained in this text field.
     */
    public void setEditable(boolean editable) {
        component.setEditable(editable);
    }

    /**
     * This method is called whenever the selection changes or the caret moves.
     * It is protected so that subclasses can override it. This allows
     * subclasses to be notified of any selection change, whether or not it is
     * the result of a user action.
     */
    protected void caretMoved() {
        if (suppressEvents == 0) {
            SwingUtilities.invokeLater(() -> dispatchEvent(new SelectionChangedEvent(TextWidget.this)));
        }
    }

    /**
     * This method is called whenever the content of the Widget changes. It is
     * protected so that subclasses can override it. This allows subclasses to
     * be notified of any content change, whether or not it is the result of a
     * user action.
     */
    protected void textChanged() {
        invalidateSize();
        if (suppressEvents == 0) {
            SwingUtilities.invokeLater(() -> dispatchEvent(new ValueChangedEvent(TextWidget.this)));
        }
    }
}
