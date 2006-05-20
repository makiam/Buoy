package buoy.widget;

import buoy.xml.*;
import buoy.xml.delegate.*;

import java.awt.event.*;
import javax.swing.*;

/**
 * A BTextField is a simple text entry box.  It allows the user to enter a single line of text.
 * <p>
 * In addition to the event types generated by all Widgets, BTextFields generate the following event types:
 * <ul>
 * <li>{@link buoy.event.SelectionChangedEvent SelectionChangedEvent}</li>
 * <li>{@link buoy.event.ValueChangedEvent ValueChangedEvent}</li>
 * </ul>
 *
 * @author Peter Eastman
 */

public class BTextField extends TextWidget
{
  private boolean keepSelection;
  
  static
  {
    WidgetEncoder.setPersistenceDelegate(BTextField.class, new EventSourceDelegate(new String [] {"text"}));
  }

  /**
   * Create a new BTextField.
   */
  
  public BTextField()
  {
    this(null, 0);
  }
  
  /**
   * Create a new BTextField.
   *
   * @param text      the initial text contained in the text field (may be null)
   */
  
  public BTextField(String text)
  {
    this(text, 0);
  }

  /**
   * Create a new BTextField.
   *
   * @param columns   the number of columns this text field should be wide enough to display
   */
  
  public BTextField(int columns)
  {
    this(null, columns);
  }

  /**
   * Create a new BTextField.
   *
   * @param text      the initial text contained in the text field (may be null)
   * @param columns   the number of columns this text field should be wide enough to display
   */
  
  public BTextField(String text, int columns)
  {
    component = createComponent();
    JTextField tf = (JTextField) component;
    tf.setText(text);
    tf.setColumns(columns);
    tf.addCaretListener(caretListener);
    tf.getDocument().addDocumentListener(documentListener);
    tf.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent ev)
      {
        keepSelection = true;
      }
    });
    tf.addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent ev)
      {
        // Automatically select the entire contents of the field when it gains focus through a means
        // other than a mouse click.
        
        if (!keepSelection)
        {
          setSelectionStart(0);
          setSelectionEnd(getText().length());
        }
      }
      public void focusLost(FocusEvent ev)
      {
        // If the contents were automatically selected when focus was gained, deselect them now.
        
        if (!keepSelection)
        {
          int length = getText().length();
          setSelectionStart(length);
          setSelectionEnd(length);
        }
        keepSelection = false;
      }
    });
  }
  
  /**
   * Create the JTextField which serves as this Widget's Component.  This method is protected so that
   * subclasses can override it.
   */
  
  protected JTextField createComponent()
  {
    return new JTextField();
  }

  /**
   * Get the number of columns this text field should be wide enough to display.
   */
  
  public int getColumns()
  {
    return ((JTextField) component).getColumns();
  }
  
  /**
   * Set the number of columns this text field should be wide enough to display.
   */
  
  public void setColumns(int columns)
  {
    ((JTextField) component).setColumns(columns);
    invalidateSize();
  }
}
