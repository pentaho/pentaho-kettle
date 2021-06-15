/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.util;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * Utilities for configuring columns of trees and tables in a
 * keyboard-accessible way.
 * 
 * @since 1.3
 * 
 */
public class ConfigureColumns {

	/**
	 * Configure the columns of the given tree in a keyboard-accessible way,
	 * using the given shell provider to parent dialogs.
	 * 
	 * @param tree the tree
	 * @param shellProvider a shell provider
	 * @return <code>false</code> if the user canceled, <code>true</code>
	 *         otherwise
	 */
	public static boolean forTree(Tree tree, IShellProvider shellProvider) {
		return new ConfigureColumnsDialog(shellProvider, tree).open() == Window.OK;
	}

	/**
	 * Configure the columns of the given tree in a keyboard-accessible way,
	 * using the given shell provider to parent dialogs.
	 * 
	 * @param table the table
	 * @param shellProvider a shell provider
	 * @return <code>false</code> if the user canceled, <code>true</code>
	 *         otherwise
	 */
	public static boolean forTable(Table table, IShellProvider shellProvider) {
		return new ConfigureColumnsDialog(shellProvider, table).open() == Window.OK;
	}

	/**
	 * NON-API - This class is internal.
	 * 
	 */
	static class ConfigureColumnsDialog extends Dialog {

		private Control targetControl;
		private ColumnObject[] columnObjects;
		private Table table;
		private Button upButton;
		private Button downButton;
		private Text text;
		private boolean moveableColumnsFound;

		class ColumnObject {
			Item column;
			int index;
			String name;
			Image image;
			boolean visible;
			int width;
			boolean moveable;
			boolean resizable;

			ColumnObject(Item column, int index, String text, Image image, int width,
					boolean moveable, boolean resizable, boolean visible) {
				this.column = column;
				this.index = index;
				this.name = text;
				this.image = image;
				this.width = width;
				this.moveable = moveable;
				this.resizable = resizable;
				this.visible = visible;
			}
		}

		/**
		 * NON-API - This class is internal and will be moved to another package
		 * in 3.5. Creates a new dialog for configuring columns of the given
		 * column viewer. The column viewer must have an underlying {@link Tree}
		 * or {@link Table}, other controls are not supported.
		 * 
		 * @param shellProvider
		 * @param table
		 */
		public ConfigureColumnsDialog(IShellProvider shellProvider, Table table) {
			this(shellProvider, (Control) table);
		}

		/**
		 * NON-API - This class is internal and will be moved to another package
		 * in 3.5. Creates a new dialog for configuring columns of the given
		 * column viewer. The column viewer must have an underlying {@link Tree}
		 * or {@link Table}, other controls are not supported.
		 * 
		 * @param shellProvider
		 * @param tree
		 */
		public ConfigureColumnsDialog(IShellProvider shellProvider, Tree tree) {
			this(shellProvider, (Control) tree);
		}

		/**
		 * @param shellProvider
		 * @param control
		 */
		private ConfigureColumnsDialog(IShellProvider shellProvider, Control control) {
			super(shellProvider);
			this.targetControl = control;
			this.moveableColumnsFound = createColumnObjects();
		}

		protected boolean isResizable() {
			return true;
		}

		public void create() {
			super.create();
			getShell().setText(JFaceResources.getString("ConfigureColumnsDialog_Title")); //$NON-NLS-1$
		}

		protected void initializeBounds() {
			super.initializeBounds();
			table.setSelection(0);
			handleSelectionChanged(0);
		}

		/**
		 * Returns true if any of the columns is moveable (can be reordered).
		 */
		private boolean createColumnObjects() {
			boolean result = true;
			Item[] columns = getViewerColumns();
			ColumnObject[] cObjects = new ColumnObject[columns.length];
			for (int i = 0; i < columns.length; i++) {
				Item c = columns[i];
				boolean moveable = getMoveable(c);
				result = result && moveable;
				cObjects[i] = new ColumnObject(c, i, getColumnName(c), getColumnImage(c),
						getColumnWidth(c), moveable, getResizable(c), true);
			}
			int[] columnOrder = getColumnOrder();
			columnObjects = new ColumnObject[columns.length];
			for (int i = 0; i < columnOrder.length; i++) {
				columnObjects[i] = cObjects[columnOrder[i]];
			}
			return result;
		}

		/**
		 * @param c
		 * @return
		 */
		private Image getColumnImage(Item item) {
			if (item instanceof TableColumn) {
				return ((TableColumn) item).getImage();
			} else if (item instanceof TreeColumn) {
				return ((TreeColumn) item).getImage();
			}
			return null;
		}

		/**
		 * @return
		 */
		private int[] getColumnOrder() {
			if (targetControl instanceof Table) {
				return ((Table) targetControl).getColumnOrder();
			} else if (targetControl instanceof Tree) {
				return ((Tree) targetControl).getColumnOrder();
			}
			return new int[0];
		}

		/**
		 * @param c
		 * @return
		 */
		private boolean getMoveable(Item item) {
			if (item instanceof TableColumn) {
				return ((TableColumn) item).getMoveable();
			} else if (item instanceof TreeColumn) {
				return ((TreeColumn) item).getMoveable();
			}
			return false;
		}

		/**
		 * @param c
		 * @return
		 */
		private boolean getResizable(Item item) {
			if (item instanceof TableColumn) {
				return ((TableColumn) item).getResizable();
			} else if (item instanceof TreeColumn) {
				return ((TreeColumn) item).getResizable();
			}
			return false;
		}

		protected Control createDialogArea(Composite parent) {
			Composite composite = (Composite) super.createDialogArea(parent);

			table = new Table(composite, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL
					| SWT.H_SCROLL /*
									 * | SWT.CHECK
									 */);
			for (int i = 0; i < columnObjects.length; i++) {
				TableItem tableItem = new TableItem(table, SWT.NONE);
				tableItem.setText(columnObjects[i].name);
				tableItem.setImage(columnObjects[i].image);
				tableItem.setData(columnObjects[i]);
			}

			GridDataFactory.defaultsFor(table).span(1, moveableColumnsFound ? 3 : 1)
					.applyTo(table);

			if (moveableColumnsFound) {
				upButton = new Button(composite, SWT.PUSH);
				upButton.setText(JFaceResources.getString("ConfigureColumnsDialog_up")); //$NON-NLS-1$
				upButton.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						handleMove(table, true);
					}
				});
				setButtonLayoutData(upButton);
				downButton = new Button(composite, SWT.PUSH);
				downButton.setText(JFaceResources
						.getString("ConfigureColumnsDialog_down")); //$NON-NLS-1$
				downButton.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						handleMove(table, false);
					}
				});
				setButtonLayoutData(downButton);

				// filler label
				createLabel(composite, ""); //$NON-NLS-1$
			}

			Composite widthComposite = new Composite(composite, SWT.NONE);
			createLabel(widthComposite, JFaceResources
					.getString("ConfigureColumnsDialog_WidthOfSelectedColumn")); //$NON-NLS-1$

			text = new Text(widthComposite, SWT.SINGLE | SWT.BORDER);
			// see #initializeBounds
			text.setText(Integer.toString(1000));

			GridLayoutFactory.fillDefaults().numColumns(2).applyTo(widthComposite);

			int numColumns = moveableColumnsFound ? 2 : 1;

			GridDataFactory.defaultsFor(widthComposite).grab(false, false).span(
					numColumns, 1).applyTo(widthComposite);

			GridLayoutFactory.swtDefaults().numColumns(numColumns).applyTo(composite);

			table.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					handleSelectionChanged(table.indexOf((TableItem) event.item));
				}
			});
			text.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event event) {
					ColumnObject columnObject = columnObjects[table.getSelectionIndex()];
					if (!columnObject.resizable) {
						return;
					}
					try {
						int width = Integer.parseInt(text.getText());
						columnObject.width = width;
					} catch (NumberFormatException ex) {
						// ignore for now
					}
				}
			});

			Dialog.applyDialogFont(composite);

			return composite;
		}

		/**
		 * @param table
		 * @param up
		 */
		protected void handleMove(Table table, boolean up) {
			int index = table.getSelectionIndex();
			int newIndex = index + (up ? -1 : 1);
			if (index < 0 || index >= table.getItemCount()) {
				return;
			}
			ColumnObject columnObject = columnObjects[index];
			columnObjects[index] = columnObjects[newIndex];
			columnObjects[newIndex] = columnObject;
			table.getItem(index).dispose();
			TableItem newItem = new TableItem(table, SWT.NONE, newIndex);
			newItem.setText(columnObject.name);
			newItem.setImage(columnObject.image);
			newItem.setData(columnObject);
			table.setSelection(newIndex);
			handleSelectionChanged(newIndex);
		}

		private void createLabel(final Composite composite, String string) {
			Label label = new Label(composite, SWT.NONE);
			label.setText(string);
		}

		/**
		 * @param item
		 * @return
		 */
		private String getColumnName(Item item) {
			String result = ""; //$NON-NLS-1$
			if (item instanceof TableColumn) {
				result = ((TableColumn) item).getText();
				if (result.trim().equals("")) { //$NON-NLS-1$
					result = ((TableColumn) item).getToolTipText();
				}
			} else if (item instanceof TreeColumn) {
				result = ((TreeColumn) item).getText();
				if (result.trim().equals("")) { //$NON-NLS-1$
					result = ((TreeColumn) item).getToolTipText();
				}
			}
			return result;
		}

		/**
		 * @param item
		 * @return
		 */
		private int getColumnWidth(Item item) {
			if (item instanceof TableColumn) {
				return ((TableColumn) item).getWidth();
			} else if (item instanceof TreeColumn) {
				return ((TreeColumn) item).getWidth();
			}
			return 0;
		}

		/**
		 * @return
		 */
		private Item[] getViewerColumns() {
			if (targetControl instanceof Table) {
				return ((Table) targetControl).getColumns();
			} else if (targetControl instanceof Tree) {
				return ((Tree) targetControl).getColumns();
			}
			return new Item[0];
		}

		private void handleSelectionChanged(int index) {
			ColumnObject c = columnObjects[index];
			text.setText(Integer.toString(c.width));
			text.setEnabled(c.resizable);
			if (moveableColumnsFound) {
				upButton.setEnabled(c.moveable && index > 0);
				downButton.setEnabled(c.moveable && index + 1 < table.getItemCount());
			}
		}

		protected void okPressed() {
			int[] columnOrder = new int[columnObjects.length];
			for (int i = 0; i < columnObjects.length; i++) {
				ColumnObject columnObject = columnObjects[i];
				columnOrder[i] = columnObject.index;
				setColumnWidth(columnObject.column, columnObject.width);
			}
			setColumnOrder(columnOrder);
			super.okPressed();
		}

		/**
		 * @param column
		 * @param width
		 */
		private void setColumnWidth(Item item, int width) {
			if (item instanceof TableColumn) {
				((TableColumn) item).setWidth(width);
			} else if (item instanceof TreeColumn) {
				((TreeColumn) item).setWidth(width);
			}
		}

		/**
		 * @param columnOrder
		 */
		private void setColumnOrder(int[] order) {
			if (targetControl instanceof Table) {
				((Table) targetControl).setColumnOrder(order);
			} else if (targetControl instanceof Tree) {
				((Tree) targetControl).setColumnOrder(order);
			}
		}
	}
}