package buoy.widget;

import buoy.event.*;
import buoy.xml.*;
import buoy.xml.delegate.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

/**
 * A BTable is a Widget that displays a grid of objects. Typically the objects
 * are Strings, but other types of objects can be used as well. It optionally
 * can allow the user to select rows, columns, or individual cells, and to edit
 * the contents of cells. There are methods for adding and removing rows and
 * columns, and for setting the contents of cells. Alternatively, you can set a
 * TableModel to provide more complex behaviors.
 * <p>
 * BTable does not provide scrolling automatically. Normally, it is used inside
 * a BScrollPane. When a BTable is set as the content of a BScrollPane, it
 * automatically places its column headers (which are represented by a separate
 * Widget) into the BScrollPane's column header position. When using a BTable
 * outside a BScrollPane, you will need to call
 * {@link buoy.widget.BTable#getTableHeader getTableHeader()} to get the Widget
 * representing the column headers, and position it separately.
 * <p>
 * You may also want to access the column headers directly, in order to respond
 * to events in the headers. For example, suppose you want mouse clicks in a
 * column header to select the clicked column, and deselect all other columns.
 * This is done by adding an event listener to the header Widget:
 * <p>
 * <
 * pre>
 * table.getTableHeader().addEventLink(MousePressedEvent.class, new Object() {
 *   void processEvent(MousePressedEvent ev)
 *   {
 *     table.clearSelection();
 *     table.setColumnSelected(table.findColumn(ev.getPoint()), true);
 *   }
 * });
 * </pre>
 * <p>
 * A BTable can optionally allow the user to reorder the columns by dragging
 * their headers. This affects only the display of the table, not the internal
 * data representation. Whenever a method takes a column index as an argument,
 * that index refers to the internal ordering, not to the display order.
 * Therefore, any reordering of columns by the user will be completely invisible
 * to the program, and can be ignored.
 * <p>
 * A BTable is a wrapper around a JTable and its associated classes, which
 * together form a powerful but also very complex API. BTable exposes only the
 * most commonly used features of this API. To use other features, call
 * <code>getComponent()</code> to get the underlying JTable, then manipulate it
 * directly. For example, you can set a custom TableCellRenderer to customize
 * the appearance of individual cells, or set a TableCellEditor to control the
 * user interface for editing cells.
 * <p>
 * In addition to the event types generated by all Widgets, BTables generate the
 * following event types:
 * <ul>
 * <li>{@link buoy.event.CellValueChangedEvent CellValueChangedEvent}</li>
 * <li>{@link buoy.event.SelectionChangedEvent SelectionChangedEvent}</li>
 * </ul>
 *
 * @author Peter Eastman
 */
public class BTable extends Widget<JTable> {

    protected DefaultTableModel defaultModel;
    protected BTableHeader tableHeader;
    protected ArrayList<Boolean> columnEditable;

    public static final SelectionMode SELECT_NONE = new SelectionMode();
    public static final SelectionMode SELECT_ROWS = new SelectionMode();
    public static final SelectionMode SELECT_COLUMNS = new SelectionMode();
    public static final SelectionMode SELECT_CELLS = new SelectionMode();

    static {
        WidgetEncoder.setPersistenceDelegate(BTable.class, new BTableDelegate());
        WidgetEncoder.setPersistenceDelegate(BTable.BTableHeader.class, new BTableHeaderDelegate());
        WidgetEncoder.setPersistenceDelegate(SelectionMode.class, new StaticFieldDelegate(BTable.class));
    }

    /**
     * Create a BTable with no rows or columns.
     */
    public BTable() {
        defaultModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return columnEditable.get(column);
            }
        };
        component = createComponent();
        tableHeader = new BTableHeader();
        columnEditable = new ArrayList<>();
        ListSelectionListener lsl = ev -> dispatchEvent(new SelectionChangedEvent(BTable.this, ev.getValueIsAdjusting()));
        component.getSelectionModel().addListSelectionListener(lsl);
        component.getColumnModel().getSelectionModel().addListSelectionListener(lsl);
        component.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    }

    /**
     * Create an empty BTable of a specified size.
     *
     * @param rows the number of rows
     * @param cols the number of columns
     */
    public BTable(int rows, int cols) {
        this();
        defaultModel.setRowCount(rows);
        defaultModel.setColumnCount(cols);
        for (int i = 0; i < cols; i++) {
            columnEditable.add(Boolean.FALSE);
        }
    }

    /**
     * Create a BTable, and populate it with the data in an array.
     *
     * @param cellData element [i][j] is the object to display in row i, column
     * j
     * @param columnTitle the list of column titles (usually Strings)
     */
    public BTable(Object[][] cellData, Object[] columnTitle) {
        this();
        defaultModel.setDataVector(cellData, columnTitle);
        for (Object aColumnTitle : columnTitle) {
            columnEditable.add(Boolean.FALSE);
        }
    }

    /**
     * Create a BTable whose contents are determined by a TableModel.
     */
    public BTable(TableModel model) {
        this();
        component.setModel(model);
    }

    /**
     * Create the JTable which serves as this Widget's Component. This method is
     * protected so that subclasses can override it.
     */
    protected JTable createComponent() {
        return new JTable(defaultModel) {
            @Override
            public void editingStopped(ChangeEvent ev) {
                int row = getEditingRow();
                int col = getEditingColumn();
                super.editingStopped(ev);
                BTable.this.dispatchEvent(new CellValueChangedEvent(BTable.this, row, col));
            }

            @Override
            protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
                if (e.getKeyCode() == KeyEvent.VK_META || e.getKeyCode() == KeyEvent.VK_CAPS_LOCK) {
                    return false;
                }
                return super.processKeyBinding(ks, e, condition, pressed);
            }
        };
    }

    /**
     * Get the Widget that displays this table's column headers.
     */
    public BTableHeader getTableHeader() {
        return tableHeader;
    }

    /**
     * Get the TableModel which controls the contents of this BTable.
     */
    public TableModel getModel() {
        return component.getModel();
    }

    /**
     * Set the TableModel which controls the contents of this BTable.
     */
    public void setModel(TableModel model) {
        component.setModel(model);
        invalidateSize();
    }

    /**
     * Add a column to this table.
     * <p>
     * This method works by manipulating the default TableModel for this table.
     * If you have set a custom model, either by passing it to the constructor
     * or by calling setModel(), this method has no effect.
     *
     * @param columnTitle the title of the column to add (usually a String)
     */
    public void addColumn(Object columnTitle) {
        defaultModel.addColumn(columnTitle);
        columnEditable.add(Boolean.FALSE);
        invalidateSize();
    }

    /**
     * Add a column to the end of this table.
     * <p>
     * This method works by manipulating the default TableModel for this table.
     * If you have set a custom model, either by passing it to the constructor
     * or by calling setModel(), this method has no effect.
     *
     * @param columnTitle the title of the column to add (usually a String)
     * @param columnData the objects to display in the cells of the new column
     */
    public void addColumn(Object columnTitle, Object[] columnData) {
        defaultModel.addColumn(columnTitle, columnData);
        columnEditable.add(Boolean.FALSE);
        invalidateSize();
    }

    /**
     * Remove a column from this table.
     * <p>
     * This method works by manipulating the default TableModel for this table.
     * If you have set a custom model, either by passing it to the constructor
     * or by calling setModel(), this method has no effect.
     *
     * @param index the index of the column to remove
     */
    public void removeColumn(int index) {
        int columns = getColumnCount();
        Vector<Object> columnNames = new Vector<>();
        for (int i = 0; i < columns; i++) {
            if (i != index) {
                columnNames.add(getColumnHeader(i));
            }
        }
        Vector<Vector> data = defaultModel.getDataVector();
        for (Vector aData : data) {
             aData.remove(index);
        }
        defaultModel.setDataVector(data, columnNames);
        if (index < columnEditable.size()) {
            columnEditable.remove(index);
        }
        invalidateSize();
    }

    /**
     * Remove all columns from this table.
     * <p>
     * This method works by manipulating the default TableModel for this table.
     * If you have set a custom model, either by passing it to the constructor
     * or by calling setModel(), this method has no effect.
     */
    public void removeAllColumns() {
        defaultModel.setColumnCount(0);
        columnEditable.clear();
        invalidateSize();
    }

    /**
     * Add a row to the end of the table.
     * <p>
     * This method works by manipulating the default TableModel for this table.
     * If you have set a custom model, either by passing it to the constructor
     * or by calling setModel(), this method has no effect.
     *
     * @param rowData the objects to display in the cells of the new row
     */
    public void addRow(Object[] rowData) {
        defaultModel.addRow(rowData);
        invalidateSize();
    }

    /**
     * Add a row to the table.
     * <p>
     * This method works by manipulating the default TableModel for this table.
     * If you have set a custom model, either by passing it to the constructor
     * or by calling setModel(), this method has no effect.
     *
     * @param index the position at which to add the row
     * @param rowData the objects to display in the cells of the new row
     */
    public void addRow(int index, Object[] rowData) {
        defaultModel.insertRow(index, rowData);
        invalidateSize();
    }

    /**
     * Remove a row from the table.
     * <p>
     * This method works by manipulating the default TableModel for this table.
     * If you have set a custom model, either by passing it to the constructor
     * or by calling setModel(), this method has no effect.
     *
     * @param index the index of the row to remove
     */
    public void removeRow(int index) {
        defaultModel.removeRow(index);
        invalidateSize();
    }

    /**
     * Remove all rows from the table.
     * <p>
     * This method works by manipulating the default TableModel for this table.
     * If you have set a custom model, either by passing it to the constructor
     * or by calling setModel(), this method has no effect.
     */
    public void removeAllRows() {
        defaultModel.setRowCount(0);
        invalidateSize();
    }

    /**
     * Get the number of rows in the table.
     */
    public int getRowCount() {
        return component.getRowCount();
    }

    /**
     * Get the number of columns in the table.
     */
    public int getColumnCount() {
        return component.getColumnCount();
    }

    /**
     * Determine whether the cells in a particular column may be edited.
     * <p>
     * If you have set a custom model for this table, either by passing it to
     * the constructor or by calling setModel(), the value returned by this
     * method is meaningless and should be ignored.
     *
     * @param index the index of the column
     */
    public boolean isColumnEditable(int index) {
        return columnEditable.get(index);
    }

    /**
     * Set whether the cells in a particular column may be edited.
     * <p>
     * This method works by manipulating the default TableModel for this table.
     * If you have set a custom model, either by passing it to the constructor
     * or by calling setModel(), this method has no effect.
     *
     * @param index the index of the column
     * @param editable specifies whether cells in the column may be edited
     */
    public void setColumnEditable(int index, boolean editable) {
        columnEditable.set(index, editable);
    }

    /**
     * Get the value contained in a cell.
     *
     * @param row the row containing the cell
     * @param col the column containing the cell
     */
    public Object getCellValue(int row, int col) {
        return component.getModel().getValueAt(row, col);
    }

    /**
     * Set the value contained in a cell.
     *
     * @param row the row containing the cell
     * @param col the column containing the cell
     * @param value the value to place into the cell
     */
    public void setCellValue(int row, int col, Object value) {
        component.getModel().setValueAt(value, row, col);
    }

    /**
     * Get the TableColumn object representing a column.
     *
     * @param col the column index
     */
    private TableColumn getColumn(int col) {
        JTable table = component;
        col = table.convertColumnIndexToView(col);
        return table.getColumnModel().getColumn(col);
    }

    /**
     * Get the header value for a column.
     *
     * @param col the column index
     */
    public Object getColumnHeader(int col) {
        return getColumn(col).getHeaderValue();
    }

    /**
     * Set the header value for a column.
     *
     * @param col the column index
     * @param value the value to place into the column header
     */
    public void setColumnHeader(int col, Object value) {
        getColumn(col).setHeaderValue(value);
    }

    /**
     * Get the height of a row.
     *
     * @param row the row index
     */
    public int getRowHeight(int row) {
        return component.getRowHeight(row);
    }

    /**
     * Set the height of a row.
     *
     * @param row the row index
     * @param height the new height for the row
     */
    public void setRowHeight(int row, int height) {
        component.setRowHeight(row, height);
        invalidateSize();
    }

    /**
     * Get the width of a column.
     *
     * @param col the column index
     */
    public int getColumnWidth(int col) {
        return getColumn(col).getWidth();
    }

    /**
     * Set the width of a column.
     *
     * @param col the column index
     * @param width the new width for the column
     */
    public void setColumnWidth(int col, int width) {
        getColumn(col).setPreferredWidth(width);
        invalidateSize();
    }

    /**
     * Adjust the width of a column based on the size of its header.
     *
     * @param col the column index
     */
    public void sizeColumnToFit(int col) {
        TableColumn tc = getColumn(col);
        if (tc.getHeaderRenderer() != null) {
            tc.sizeWidthToFit();
        } else {
            JTableHeader th = component.getTableHeader();
            FontMetrics fm = th.getFontMetrics(th.getFont());
            tc.setPreferredWidth(fm.stringWidth(tc.getHeaderValue().toString()) + 10);
        }
        invalidateSize();
    }

    /**
     * Get whether the user is allowed to resize columns by clicking between the
     * headers and dragging.
     */
    public boolean getColumnsResizable() {
        return component.getTableHeader().getResizingAllowed();
    }

    /**
     * Set whether the user is allowed to resize columns by clicking between the
     * headers and dragging.
     */
    public void setColumnsResizable(boolean resizable) {
        component.getTableHeader().setResizingAllowed(resizable);
    }

    /**
     * Get whether the user is allowed to reorder columns by dragging their
     * headers.
     */
    public boolean getColumnsReorderable() {
        return component.getTableHeader().getReorderingAllowed();
    }

    /**
     * Set whether the user is allowed to reorder columns by dragging their
     * headers.
     */
    public void setColumnsReorderable(boolean reorderable) {
        component.getTableHeader().setReorderingAllowed(reorderable);
    }

    /**
     * Get the selection mode for this table. This will be equal to SELECT_NONE,
     * SELECT_ROWS, SELECT_COLUMNS, or SELECT_CELLS.
     */
    public SelectionMode getSelectionMode() {
        if (component.getCellSelectionEnabled()) {
            return SELECT_CELLS;
        }
        if (component.getColumnSelectionAllowed()) {
            return SELECT_COLUMNS;
        }
        if (component.getRowSelectionAllowed()) {
            return SELECT_ROWS;
        }
        return SELECT_NONE;
    }

    /**
     * Set the selection mode for this table. This should be equal to
     * SELECT_NONE, SELECT_ROWS, SELECT_COLUMNS, or SELECT_CELLS.
     */
    public void setSelectionMode(SelectionMode mode) {
        component.setColumnSelectionAllowed(mode == SELECT_COLUMNS || mode == SELECT_CELLS);
        component.setRowSelectionAllowed(mode == SELECT_ROWS || mode == SELECT_CELLS);
    }

    /**
     * Determine whether this table allows multiple cells to be selected.
     */
    public boolean isMultipleSelectionEnabled() {
        return component.getSelectionModel().getSelectionMode() != ListSelectionModel.SINGLE_SELECTION;
    }

    /**
     * Set whether this table should allow multiple cells to be selected.
     */
    public void setMultipleSelectionEnabled(boolean multiple) {
        component.setSelectionMode(multiple ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION : ListSelectionModel.SINGLE_SELECTION);
    }

    /**
     * Determine whether a row is selected.
     * <p>
     * If the selection mode is SELECT_COLUMNS, the value returned by this
     * method is meaningless and should be ignored.
     *
     * @param row the row index
     */
    public boolean isRowSelected(int row) {
        return component.isRowSelected(row);
    }

    /**
     * Set whether a row is selected.
     *
     * @param row the row index
     * @param selected specifies whether the row should be selected
     */
    public void setRowSelected(int row, boolean selected) {
        if (selected) {
            component.addRowSelectionInterval(row, row);
        } else {
            component.removeRowSelectionInterval(row, row);
        }
    }

    /**
     * Get an array which contains the indices of all selected rows. If no rows
     * are selected, this returns an empty array.
     * <p>
     * If the selection mode is SELECT_COLUMNS, the value returned by this
     * method is meaningless and should be ignored.
     */
    public int[] getSelectedRows() {
        return component.getSelectedRows();
    }

    /**
     * Determine whether a column is selected.
     * <p>
     * If the selection mode is SELECT_ROWS, the value returned by this method
     * is meaningless and should be ignored.
     *
     * @param col the column index
     */
    public boolean isColumnSelected(int col) {
        return component.isColumnSelected(col);
    }

    /**
     * Set whether a column is selected.
     *
     * @param col the column index
     * @param selected specifies whether the column should be selected
     */
    public void setColumnSelected(int col, boolean selected) {
        if (selected) {
            component.addColumnSelectionInterval(col, col);
        } else {
            component.removeColumnSelectionInterval(col, col);
        }
    }

    /**
     * Get an array which contains the indices of all selected columns. If no
     * columns are selected, this returns an empty array.
     * <p>
     * If the selection mode is SELECT_ROWS, the value returned by this method
     * is meaningless and should be ignored.
     */
    public int[] getSelectedColumns() {
        return component.getSelectedColumns();
    }

    /**
     * Determine whether a cell is selected.
     *
     * @param row the row index
     * @param col the column index
     */
    public boolean isCellSelected(int row, int col) {
        return component.isCellSelected(row, col);
    }

    /**
     * Set whether a cell is selected.
     *
     * @param row the row index
     * @param col the column index
     * @param selected specifies whether the cell should be selected
     */
    public void setCellSelected(int row, int col, boolean selected) {
        if (selected) {
            component.addRowSelectionInterval(row, row);
            component.addColumnSelectionInterval(col, col);
        } else {
            component.removeRowSelectionInterval(row, row);
            component.removeColumnSelectionInterval(col, col);
        }
    }

    /**
     * Get an array of Points which contain the indices of all selected cells.
     * For every selected cell, the array contains a Point whose x field
     * contains the column index of the cell and whose y field contains the row
     * index. If no cells are selected, this returns an empty array.
     */
    public Point[] getSelectedCells() {
        int[] rows;
        int[] cols;
        if (component.getRowSelectionAllowed()) {
            rows = component.getSelectedRows();
        } else {
            rows = new int[getRowCount()];
            for (int i = 0; i < rows.length; i++) {
                rows[i] = i;
            }
        }
        if (component.getColumnSelectionAllowed()) {
            cols = component.getSelectedColumns();
        } else {
            cols = new int[getColumnCount()];
            for (int i = 0; i < cols.length; i++) {
                cols[i] = i;
            }
        }
        Point[] cells = new Point[rows.length * cols.length];
        for (int i = 0; i < rows.length; i++) {
            for (int j = 0; j < cols.length; j++) {
                cells[i * cols.length + j] = new Point(cols[j], rows[i]);
            }
        }
        return cells;
    }

    /**
     * Deselect all rows and columns.
     */
    public void clearSelection() {
        component.clearSelection();
    }

    /**
     * Given a Point which represents a pixel location, find which row the Point
     * lies on.
     *
     * @param pos the point of interest
     * @return the row index, or -1 if the Point is outside the table
     */
    public int findRow(Point pos) {
        return component.rowAtPoint(pos);
    }

    /**
     * Given a Point which represents a pixel location, find which column the
     * Point lies on.
     *
     * @param pos the point of interest
     * @return the column index, or -1 if the Point is outside the table
     */
    public int findColumn(Point pos) {
        return component.columnAtPoint(pos);
    }

    /**
     * Programmatically begin editing a specified cell, if editing is allowed.
     *
     * @param row the row containing the cell
     * @param col the column containing the cell
     */
    public void editCellAt(int row, int col) {
        component.editCellAt(row, col);
    }

    /**
     * Get whether this table displays horizontal lines between the rows.
     */
    public boolean getShowHorizontalLines() {
        return component.getShowHorizontalLines();
    }

    /**
     * Set whether this table displays horizontal lines between the rows.
     */
    public void setShowHorizontalLines(boolean show) {
        component.setShowHorizontalLines(show);
    }

    /**
     * Get whether this table displays vertical lines between the columns.
     */
    public boolean getShowVerticalLines() {
        return component.getShowVerticalLines();
    }

    /**
     * Set whether this table displays vertical lines between the columns.
     */
    public void setShowVerticalLines(boolean show) {
        component.setShowVerticalLines(show);
    }

    /**
     * Scroll the BTable's parent BScrollPane to ensure that a particular cell
     * is visible. If the parent is not a BScrollPane, the results of calling
     * this method are undefined, but usually it will have no effect at all.
     *
     * @param row the row containing the cell
     * @param col the column containing the cell
     */
    public void scrollToCell(int row, int col) {
        Rectangle bounds = component.getCellRect(row, col, true);
        component.scrollRectToVisible(bounds);
    }

    /**
     * Set this Widget's parent in the layout hierarchy (may be null). If the
     * parent is a BScrollPane, the table header is placed into the
     * BScrollPane's column header position.
     */
    @Override
    protected void setParent(WidgetContainer container) {
        super.setParent(container);
        if (container instanceof BScrollPane) {
            BScrollPane scroll = (BScrollPane) container;
            scroll.setColHeader(tableHeader);
            getComponent().setAutoResizeMode(scroll.getHorizontalScrollbarPolicy() == BScrollPane.SCROLLBAR_NEVER ? JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS : JTable.AUTO_RESIZE_OFF);
        }
    }

    /**
     * This inner class is the Widget that draws the table's column headers.
     */
    public class BTableHeader extends Widget<JTableHeader> {

        private BTableHeader() {
            component = BTable.this.getComponent().getTableHeader();
            component.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent ev) {
                    // When the user resizes a column by hand, update the BScrollPane.

                    if (component.getResizingColumn() != null && getParent() instanceof BScrollPane) {
                        getParent().layoutChildren();
                    }
                }
            });
        }

        @Override
        public JTableHeader getComponent() {
            return component;
        }

        public BTable getTable() {
            return BTable.this;
        }
    }

    /**
     * This inner class represents a selection mode.
     */
    public static class SelectionMode {

        private SelectionMode() {
        }
    }
}
