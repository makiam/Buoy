package buoy.widget;

import buoy.event.*;
import buoy.xml.*;
import buoy.xml.delegate.*;
import java.beans.*;
import java.io.File;
import java.awt.*;
import javax.swing.*;
import javax.swing.filechooser.*;

/**
 * A BFileChooser is a Widget that allows the user to select files or
 * directories from the file system. It has modes for loading files, saving
 * files, and choosing directories. It supports both single and multiple
 * selection modes, and you can optionally restrict the list of files shown in
 * the dialog by setting a FileFilter.
 * <p>
 * BFileChooser can be used in two different ways. First, it can be added to a
 * container like any other Widget. This is useful when you want it to appear
 * inside of a window along with other Widgets.
 * <p>
 * Most often, however, BFileChooser is used in a modal dialog as a self
 * contained user interface element. To use it this way, you simply instantiate
 * a BFileChooser, set any properties, and then call
 * {@link buoy.widget.BFileChooser#showDialog showDialog()} to display it.
 *  <code>showDialog()</code> will automatically create a dialog, add the
 * BFileChooser, display it, and block until the user dismisses the dialog. You
 * can reuse a single BFileChooser by repeatedly calling
 * <code>showDialog()</code>.
 * <p>
 * In addition to the event types generated by all Widgets, BFileChoosers
 * generate the following event types:
 * <ul>
 * <li>{@link buoy.event.SelectionChangedEvent SelectionChangedEvent}</li>
 * </ul>
 *
 * @author Peter Eastman
 */
public class BFileChooser extends Widget<JFileChooser> {

    private SelectionMode selectMode;
    private static File lastDirectory = FileSystemView.getFileSystemView().getDefaultDirectory();

    public static final SelectionMode OPEN_FILE = new SelectionMode();
    public static final SelectionMode SAVE_FILE = new SelectionMode();
    public static final SelectionMode SELECT_FOLDER = new SelectionMode();

    static {
        WidgetEncoder.setPersistenceDelegate(SelectionMode.class, new StaticFieldDelegate(SelectionMode.class));
    }

    /**
     * Create a new BFileChooser in OPEN_FILE mode.
     */
    public BFileChooser() {
        this(OPEN_FILE, null, lastDirectory);
    }

    /**
     * Create a new BFileChooser
     *
     * @param mode the selection mode (OPEN_FILE, SAVE_FILE, or SELECT_FOLDER)
     * @param title the title displayed on the dialog
     */
    public BFileChooser(SelectionMode mode, String title) {
        this(mode, title, lastDirectory);
    }

    /**
     * Create a new BFileChooser.
     *
     * @param mode the selection mode (OPEN_FILE, SAVE_FILE, or SELECT_FOLDER)
     * @param title the title displayed on the dialog
     * @param directory the directory which the file chooser should initially
     * display
     */
    public BFileChooser(SelectionMode mode, String title, File directory) {
        component = createComponent();
        setMode(mode);
        setTitle(title);
        setDirectory(directory);
        getComponent().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent ev) {
                String name = ev.getPropertyName();
                if (JFileChooser.SELECTED_FILES_CHANGED_PROPERTY.equals(name)
                        || (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(name) && !isMultipleSelectionEnabled())) {
                    dispatchEvent(new SelectionChangedEvent(BFileChooser.this));
                }
            }
        });
    }

    /**
     * Create the JFileChooser which serves as this Widget's Component. This
     * method is protected so that subclasses can override it.
     */
    protected JFileChooser createComponent() {
        return new JFileChooser();
    }

    /**
     * Get the title displayed on the dialog.
     */
    public String getTitle() {
        return component.getDialogTitle();
    }

    /**
     * Set the title displayed on the dialog.
     */
    public void setTitle(String title) {
        component.setDialogTitle(title);
    }

    /**
     * Get the select mode for this file chooser (OPEN_FILE, SAVE_FILE, or
     * SELECT_FOLDER).
     */
    public SelectionMode getMode() {
        return selectMode;
    }

    /**
     * Set the select mode for this file chooser (OPEN_FILE, SAVE_FILE, or
     * SELECT_FOLDER).
     */
    public void setMode(SelectionMode mode) {
        selectMode = mode;
        component.setFileSelectionMode(mode == SELECT_FOLDER ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY);
    }

    /**
     * Get whether the user is allowed to select multiple files.
     */
    public boolean isMultipleSelectionEnabled() {
        return component.isMultiSelectionEnabled();
    }

    /**
     * Set whether the user is allowed to select multiple files.
     */
    public void setMultipleSelectionEnabled(boolean multiple) {
        component.setMultiSelectionEnabled(multiple);
    }

    /**
     * Get the FileFilter which restricts the list of files shown in the dialog.
     */
    public FileFilter getFileFilter() {
        return component.getFileFilter();
    }

    /**
     * Set the FileFilter which restricts the list of files shown in the dialog.
     */
    public void setFileFilter(FileFilter filter) {
        component.setFileFilter(filter);
    }

    /**
     * Get the directory displayed in this file chooser.
     */
    public File getDirectory() {
        return component.getCurrentDirectory();
    }

    /**
     * Set the directory displayed in this file chooser.
     */
    public void setDirectory(File directory) {
        component.setCurrentDirectory(directory);
        lastDirectory = directory;
    }

    /**
     * Get the file selected in the file chooser. If multiple files are
     * selected, no guarantees are made about which one will be returned, so you
     * should use <code>getSelectedFiles()</code> instead of this method
     * whenever multiple selection is enabled.
     * <p>
     * If no file is selected, this returns null.
     */
    public File getSelectedFile() {
        return component.getSelectedFile();
    }

    /**
     * Set the file selected in the file chooser.
     */
    public void setSelectedFile(File file) {
        component.setSelectedFile(file);
    }

    /**
     * Get the list of selected files. This should be used instead of
     * <code>getSelectedFile()</code> when multiple selection is enabled.
     */
    public File[] getSelectedFiles() {
        return component.getSelectedFiles();
    }

    /**
     * Set the list of selected files.
     *
     * @throws IllegalArgumentException if multiple selection is not enabled
     */
    public void setSelectedFiles(File[] files) throws IllegalArgumentException {
        if (!isMultipleSelectionEnabled()) {
            throw new IllegalArgumentException();
        }
        component.setSelectedFiles(files);
    }

    /**
     * Show a dialog containing this BFileChooser and block until the user
     * closes it. After this method returns, you can call
     * <code>getSelectedFile()</code> or <code>getSelectedFiles()</code> to
     * determine which file or files were selected.
     *
     * @param parent the dialog's parent Widget (usually a WindowWidget). This
     * may be null.
     * @return true if the user clicked the accept button (usually labelled
     * "Open" or "Save"), false if they clicked the cancel button
     */
    public boolean showDialog(Widget parent) {
        JFileChooser jfc = component;
        Component parentComponent = (parent == null ? null : parent.getComponent());
        int result;
        if (selectMode == SAVE_FILE) {
            result = jfc.showSaveDialog(parentComponent);
        } else {
            result = jfc.showOpenDialog(parentComponent);
        }
        lastDirectory = jfc.getCurrentDirectory();
        return (result == JFileChooser.APPROVE_OPTION);
    }

    /**
     * This inner class represents a mode for the file chooser.
     */
    public static class SelectionMode {

        private SelectionMode() {
        }
    }
}
