/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.accessibility;

/**
 * This adapter class provides default implementations for the
 * methods in the <code>AccessibleTableListener</code> interface.
 * <p>
 * Classes that wish to deal with <code>AccessibleTable</code> events can
 * extend this class and override only the methods that they are
 * interested in.
 * </p><p>
 * Many methods in this adapter return cell accessible objects,
 * which should implement <code>AccessibleTableCellListener</code>.
 * </p>
 *
 * @see AccessibleTableAdapter
 * @see AccessibleTableEvent
 * @see AccessibleTableCellListener
 * @see AccessibleTableCellEvent
 *
 * @since 1.4
 */
public class AccessibleTableAdapter implements AccessibleTableListener {
	/**
	 * Deselects one column, leaving other selected columns selected (if any).
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[in] column - 0 based index of the column to be unselected.</li>
	 * <li>[out] result - set to {@link ACC#OK} if the column was deselected.</li>
	 * </ul>
	 */
	public void deselectColumn(AccessibleTableEvent e) {}

	/**
	 * Deselects one row, leaving other selected rows selected (if any).
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[in] row - 0 based index of the row to be unselected</li>
	 * <li>[out] result - set to {@link ACC#OK} if the row was deselected.</li>
	 * </ul>
	 */
	public void deselectRow(AccessibleTableEvent e) {}

	/**
	 * Returns the caption for the table.
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[out] accessible - the caption for the table, or null if the table does not have a caption</li>
	 * </ul>
	 */
	public void getCaption(AccessibleTableEvent e) {}

	/**
	 * Returns the accessible object at the specified row and column in the table.
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[in] row - the 0 based row index for which to retrieve the accessible cell</li>
	 * <li>[in] column - the 0 based column index for which to retrieve the accessible cell</li>
	 * <li>[out] accessible - the table cell at the specified row and column index,
	 * 		or null if the row or column index are not valid</li>
	 * </ul>
	 */
	public void getCell(AccessibleTableEvent e) {}

	/**
	 * Returns the accessible object for the specified column in the table.
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[in] column - the 0 based column index for which to retrieve the accessible column</li>
	 * <li>[out] accessible - the table column at the specified column index,
	 * 		or null if the column index is not valid</li>
	 * </ul>
	 */
	public void getColumn(AccessibleTableEvent e) {}

	/**
	 * Returns the total number of columns in the table.
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[out] count - the number of columns in the table</li>
	 * </ul>
	 */
	public void getColumnCount(AccessibleTableEvent e) {}

	/**
	 * Returns the description text of the specified column in the table.
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[in] column - the 0 based index of the column for which to retrieve the description</li>
	 * <li>[out] result - the description text of the specified column in the table,
	 * 		or null if the column does not have a description</li>
	 * </ul>
	 */
	public void getColumnDescription(AccessibleTableEvent e) {}

	/**
	 * Returns the accessible object for the column header.
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[out] accessible - an accessible object representing the column header,
	 * 		or null if there is no column header</li>
	 * </ul>
	 */
	public void getColumnHeader(AccessibleTableEvent e) {}

	/**
	 * Returns the column header cells as an array of accessible objects.
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[out] accessibles - an array of accessible objects representing column header cells,
	 * 		or null if there are no column header cells</li>
	 * </ul>
	 */
	public void getColumnHeaderCells(AccessibleTableEvent e) {}

	/**
	 * Returns the columns as an array of accessible objects.
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[out] accessibles - an array of accessible objects representing columns,
	 * 		or null if there are no columns</li>
	 * </ul>
	 */
	public void getColumns(AccessibleTableEvent e) {}

	/**
	 * Returns the accessible object for the specified row in the table.
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[in] row - the 0 based row index for which to retrieve the accessible row</li>
	 * <li>[out] accessible - the table row at the specified row index,
	 * 		or null if the row index is not valid</li>
	 * </ul>
	 */
	public void getRow(AccessibleTableEvent e) {}

	/**
	 * Returns the total number of rows in the table.
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[out] count - the number of rows in the table</li>
	 * </ul>
	 */
	public void getRowCount(AccessibleTableEvent e) {}

	/**
	 * Returns the description text of the specified row in the table.
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[in] row - the 0 based index of the row for which to retrieve the description</li>
	 * <li>[out] result - the description text of the specified row in the table,
	 * 		or null if the row does not have a description</li>
	 * </ul>
	 */
	public void getRowDescription(AccessibleTableEvent e) {}

	/**
	 * Returns the accessible object for the row header.
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[out] accessible - an accessible object representing the row header,
	 * 		or null if there is no row header</li>
	 * </ul>
	 */
	public void getRowHeader(AccessibleTableEvent e) {}

	/**
	 * Returns the row header cells as an array of accessible objects.
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[out] accessibles - an array of accessible objects representing row header cells,
	 * 		or null if there are no row header cells</li>
	 * </ul>
	 */
	public void getRowHeaderCells(AccessibleTableEvent e) {}

	/**
	 * Returns the rows as an array of accessible objects.
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[out] accessibles - an array of accessible objects representing rows,
	 * 		or null if there are no rows</li>
	 * </ul>
	 */
	public void getRows(AccessibleTableEvent e) {}

	/**
	 * Returns the number of selected cells.
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[out] count -  the number of cells currently selected</li>
	 * </ul>
	 */
	public void getSelectedCellCount(AccessibleTableEvent e) {}

	/**
	 * Returns the currently selected cells.
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[out] accessibles - array containing the selected accessible cells</li>
	 * </ul>
	 */
	public void getSelectedCells(AccessibleTableEvent e) {}

	/**
	 * Returns the number of selected columns.
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[out] count - the number of columns currently selected</li>
	 * </ul>
	 */
	public void getSelectedColumnCount(AccessibleTableEvent e) {}

	/**
	 * Returns the column indexes that are currently selected.
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[out] selected - an array of 0 based column indexes of selected columns</li>
	 * </ul>
	 */
	public void getSelectedColumns(AccessibleTableEvent e) {}

	/**
	 * Returns the number of selected rows.
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[out] count - the number of rows currently selected</li>
	 * </ul>
	 */
	public void getSelectedRowCount(AccessibleTableEvent e) {}

	/**
	 * Returns the row indexes that are currently selected.
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[out] selected - an array of 0 based row indexes of selected rows</li>
	 * </ul>
	 */
	public void getSelectedRows(AccessibleTableEvent e) {}

	/**
	 * Returns the summary description of the table.
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[out] accessible - the summary for the table,
	 * 		or null if the table does not have a summary</li>
	 * </ul>
	 */
	public void getSummary(AccessibleTableEvent e) {}

	/**
	 * Returns the visible columns as an array of accessible objects.
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[out] accessibles - an array of accessible objects representing visible columns,
	 * 		or null if there are no visible columns</li>
	 * </ul>
	 */
	public void getVisibleColumns(AccessibleTableEvent e) {}

	/**
	 * Returns the visible rows as an array of accessible objects.
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[out] accessibles - an array of accessible objects representing visible rows,
	 * 		or null if there are no visible rows</li>
	 * </ul>
	 */
	public void getVisibleRows(AccessibleTableEvent e) {}

	/**
	 * Returns a boolean value indicating whether the specified column is
	 * completely selected.
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[in] column - 0 based index of the column for which to determine whether it is selected</li>
	 * <li>[out] isSelected - true if the specified column is selected completely, and false otherwise</li>
	 * </ul>
	 */
	public void isColumnSelected(AccessibleTableEvent e) {}

	/**
	 * Returns a boolean value indicating whether the specified row is
	 * completely selected.
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[in] row - 0 based index of the row for which to determine whether it is selected</li>
	 * <li>[out] isSelected - true if the specified row is selected completely, and false otherwise</li>
	 * </ul>
	 */
	public void isRowSelected(AccessibleTableEvent e) {}

	/**
	 * Selects a column.
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[in] column - 0 based index of the column to be selected</li>
	 * <li>[out] result - set to {@link ACC#OK} if the column was selected.</li>
	 * </ul>
	 */
	public void selectColumn(AccessibleTableEvent e) {}

	/**
	 * Selects a row.
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[in] row - 0 based index of the row to be selected</li>
	 * <li>[out] result - set to {@link ACC#OK} if the row was selected.</li>
	 * </ul>
	 */
	public void selectRow(AccessibleTableEvent e) {}

	/**
	 * Selects a column and deselects all previously selected columns.
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[in] column - 0 based index of the column to be selected</li>
	 * <li>[out] result - set to {@link ACC#OK} if the column was selected.</li>
	 * </ul>
	 */
	public void setSelectedColumn(AccessibleTableEvent e) {}

	/**
	 * Selects a row and deselects all previously selected rows.
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[in] row - 0 based index of the row to be selected</li>
	 * <li>[out] result - set to {@link ACC#OK} if the row was selected.</li>
	 * </ul>
	 */
	public void setSelectedRow(AccessibleTableEvent e) {}
}
