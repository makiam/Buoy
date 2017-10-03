package buoy.widget;

import buoy.event.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * A BMenuItem is a Widget corresponding to an item in a pulldown menu. Each
 * menu item typically represents a different command, which the user can issue
 * by selecting it with the mouse.
 * <p>
 * It also is possible to select a menu item with the keyboard. There are, in
 * fact, two different ways of doing this. First, a menu item may have a
 * {@link Shortcut Shortcut} associated with it. This is a particular
 * combination of keys, usually including a platform specific modifier key such
 * as Control or Meta. Whenever that combination of keys is pressed, the
 * corresponding menu item is immediately selected.
 * <p>
 * Second, many platforms allow keyboard navigation of menus and menu items.
 * Typically, a user enters keyboard navigation mode by pressing a particular
 * key. They can then use arrow keys to select a desired menu item, and Enter or
 * Space to activate it. Keyboard navigation can be accelerated by assigning a
 * <i>mnemonic</i> to each menu and menu item. When the user is in keyboard
 * navigation mode, pressing the mnemonic key for a particular menu item will
 * immediately select that item.
 * <p>
 * In addition to the event types generated by all Widgets, BMenuItems generate
 * the following event types:
 * <ul>
 * <li>{@link buoy.event.CommandEvent CommandEvent}</li>
 * </ul>
 *
 * @author Peter Eastman
 */
public class BMenuItem extends Widget implements MenuWidget {

    private Shortcut shortcut;

    /**
     * Create a new BMenuItem with no label.
     */
    public BMenuItem() {
        this(null, null, null);
    }

    /**
     * Create a new BMenuItem.
     *
     * @param text the text to display on the BMenuItem
     */
    public BMenuItem(String text) {
        this(text, null, null);
    }

    /**
     * Create a new BMenuItem.
     *
     * @param text the text to display on the BMenuItem
     * @param image the image to display next to the menu item
     */
    public BMenuItem(String text, Icon image) {
        this(text, null, image);
    }

    /**
     * Create a new BMenuItem.
     *
     * @param text the text to display on the BMenuItem
     * @param shortcut a keyboard shortcut which will activate this menu item
     */
    public BMenuItem(String text, Shortcut shortcut) {
        this(text, shortcut, null);
    }

    /**
     * Create a new BMenuItem.
     *
     * @param text the text to display on the BMenuItem
     * @param shortcut a keyboard shortcut which will activate this menu item
     * @param image the image to display next to the menu item
     */
    public BMenuItem(String text, Shortcut shortcut, Icon image) {
        component = createComponent();
        getComponent().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                dispatchEvent(new CommandEvent(BMenuItem.this, ev.getWhen(), ev.getModifiers(), getComponent().getActionCommand()));
            }
        });
        getComponent().setText(text);
        getComponent().setIcon(image);
        this.shortcut = shortcut;
        if (shortcut != null) {
            getComponent().setAccelerator(shortcut.getKeyStroke());
        }
    }

    /**
     * Create the JMenuItem which serves as this Widget's Component. This method
     * is protected so that subclasses can override it.
     */
    protected JMenuItem createComponent() {
        return new JMenuItem();
    }

    public JMenuItem getComponent() {
        return (JMenuItem) component;
    }

    /**
     * Get the text which appears on this menu item.
     */
    public String getText() {
        return getComponent().getText();
    }

    /**
     * Set the text which appears on this menu item.
     */
    public void setText(String title) {
        getComponent().setText(title);
        invalidateSize();
    }

    /**
     * Get the "action command" which will be sent in a CommandEvent when this
     * menu item is selected.
     */
    public String getActionCommand() {
        return getComponent().getActionCommand();
    }

    /**
     * Set the "action command" which will be sent in a CommandEvent when this
     * menu item is selected.
     */
    public void setActionCommand(String command) {
        getComponent().setActionCommand(command);
    }

    /**
     * Get the keyboard shortcut for this menu item.
     */
    public Shortcut getShortcut() {
        return shortcut;
    }

    /**
     * Set the keyboard shortcut for this menu item.
     */
    public void setShortcut(Shortcut shortcut) {
        this.shortcut = shortcut;
        if (shortcut == null) {
            getComponent().setAccelerator(null);
        } else {
            getComponent().setAccelerator(shortcut.getKeyStroke());
        }
    }

    /**
     * Get the mnemonic which can be used to activate this menu item in keyboard
     * navigation mode.
     *
     * @return the key code (defined by the KeyEvent class) which activates this
     * menu item
     */
    public int getMnemonic() {
        return getComponent().getMnemonic();
    }

    /**
     * Set the mnemonic which can be used to activate this menu item in keyboard
     * navigation mode.
     *
     * @param key the key code (defined by the KeyEvent class) which activates
     * this menu item
     */
    public void setMnemonic(int key) {
        getComponent().setMnemonic(key);
    }

    /**
     * Get the image which appears next to this menu item.
     */
    public Icon getIcon() {
        return getComponent().getIcon();
    }

    /**
     * Set the image which appears next to this menu item.
     */
    public void setIcon(Icon image) {
        getComponent().setIcon(image);
        invalidateSize();
    }
}
