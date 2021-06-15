/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.internal.util.SerializableListenerList;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;

/**
 * A concrete viewer based on an SWT <code>Table</code>
 * control with checkboxes on each node.
 * <p>This class supports setting an {@link ICheckStateProvider} to 
 * set the checkbox states. To see standard SWT behavior, view
 * SWT Snippet274.</p>
 * <p>
 * This class is not intended to be subclassed outside the viewer framework. 
 * It is designed to be instantiated with a pre-existing SWT table control and configured
 * with a domain-specific content provider, label provider, element filter (optional),
 * and element sorter (optional).
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CheckboxTableViewer extends TableViewer implements ICheckable {

    /**
     * List of check state listeners (element type: <code>ICheckStateListener</code>).
     */
    private ListenerList checkStateListeners = new SerializableListenerList();
    
    /**
     * Provides the desired state of the check boxes.
     */
    private ICheckStateProvider checkStateProvider;

    /**
     * Creates a table viewer on a newly-created table control under the given parent.
     * The table control is created using the SWT style bits: 
     * <code>SWT.CHECK</code> and <code>SWT.BORDER</code>.
     * The table has one column.
     * The viewer has no input, no content provider, a default label provider, 
     * no sorter, and no filters.
     * <p>
     * This is equivalent to calling <code>new CheckboxTableViewer(parent, SWT.BORDER)</code>.
     * See that constructor for more details.
     * </p>
     *
     * @param parent the parent control
     * 
     * @deprecated use newCheckList(Composite, int) or new CheckboxTableViewer(Table)
     *   instead (see below for details)
     */
    public CheckboxTableViewer(Composite parent) {
        this(parent, SWT.BORDER);
    }

    /**
     * Creates a table viewer on a newly-created table control under the given parent.
     * The table control is created using the given SWT style bits, plus the 
     * <code>SWT.CHECK</code> style bit.
     * The table has one column. 
     * The viewer has no input, no content provider, a default label provider, 
     * no sorter, and no filters.
     * <p>
     * This also adds a <code>TableColumn</code> for the single column, 
     * and sets a <code>TableLayout</code> on the table which sizes the column to fill 
     * the table for its initial sizing, but does nothing on subsequent resizes.
     * </p>
     * <p>
     * If the caller just needs to show a single column with no header,
     * it is preferable to use the <code>newCheckList</code> factory method instead,
     * since SWT properly handles the initial sizing and subsequent resizes in this case.
     * </p>
     * <p>
     * If the caller adds its own columns, uses <code>Table.setHeadersVisible(true)</code>, 
     * or needs to handle dynamic resizing of the table, it is recommended to  
     * create the <code>Table</code> itself, specifying the <code>SWT.CHECK</code> style bit 
     * (along with any other style bits needed), and use <code>new CheckboxTableViewer(Table)</code> 
     * rather than this constructor.
     * </p>
     * 
     * @param parent the parent control
     * @param style SWT style bits
     * 
     * @deprecated use newCheckList(Composite, int) or new CheckboxTableViewer(Table) 
     *   instead (see above for details)
     */
    public CheckboxTableViewer(Composite parent, int style) {
        this(createTable(parent, style));
    }

    /**
     * Creates a table viewer on a newly-created table control under the given parent.
     * The table control is created using the given SWT style bits, plus the 
     * <code>SWT.CHECK</code> style bit.
     * The table shows its contents in a single column, with no header.
     * The viewer has no input, no content provider, a default label provider, 
     * no sorter, and no filters.
     * <p>
     * No <code>TableColumn</code> is added. SWT does not require a 
     * <code>TableColumn</code> if showing only a single column with no header.
     * SWT correctly handles the initial sizing and subsequent resizes in this case.
     *
     * @param parent the parent control
     * @param style SWT style bits
     * 
     * @since 1.0
     * @return CheckboxTableViewer
     */
    public static CheckboxTableViewer newCheckList(Composite parent, int style) {
        Table table = new Table(parent, SWT.CHECK | style);
        return new CheckboxTableViewer(table);
    }

    /**
     * Creates a table viewer on the given table control.
     * The <code>SWT.CHECK</code> style bit must be set on the given table control.
     * The viewer has no input, no content provider, a default label provider, 
     * no sorter, and no filters.
     *
     * @param table the table control
     */
    public CheckboxTableViewer(Table table) {
        super(table);
    }

    /* (non-Javadoc)
     * Method declared on ICheckable.
     */
    public void addCheckStateListener(ICheckStateListener listener) {
        checkStateListeners.add(listener);
    }
    
    /**
     * Sets the {@link ICheckStateProvider} for this {@link CheckboxTreeViewer}.
     * The check state provider will supply the logic for deciding whether the
     * check box associated with each item should be checked, grayed or 
     * unchecked. 
     * @param checkStateProvider	The provider.
     * @since 1.3
     */
    public void setCheckStateProvider(ICheckStateProvider checkStateProvider) {
    	this.checkStateProvider = checkStateProvider;
    	refresh();
    }
    
    /*
     * Extends this method to update check box states.
     */
    protected void doUpdateItem(Widget widget, Object element, boolean fullMap) {
    	super.doUpdateItem(widget, element, fullMap);
    	if(!widget.isDisposed()) {
    		if(checkStateProvider != null) {
				setChecked(element, checkStateProvider.isChecked(element));
				setGrayed(element, checkStateProvider.isGrayed(element));
			}
    	}
	}

	/**
     * Creates a new table control with one column.
     *
     * @param parent the parent control
     * @param style style bits
     * @return a new table control
     */
    protected static Table createTable(Composite parent, int style) {
        Table table = new Table(parent, SWT.CHECK | style);

        // Although this table column is not needed, and can cause resize problems,
        // it can't be removed since this would be a breaking change against R1.0.
        // See bug 6643 for more details.
        new TableColumn(table, SWT.NONE);
        TableLayout layout = new TableLayout();
        layout.addColumnData(new ColumnWeightData(100));
        table.setLayout(layout);

        return table;
    }

    /**
     * Notifies any check state listeners that a check state changed  has been received.
     * Only listeners registered at the time this method is called are notified.
     *
     * @param event a check state changed event
     *
     * @see ICheckStateListener#checkStateChanged
     */
    private void fireCheckStateChanged(final CheckStateChangedEvent event) {
        Object[] array = checkStateListeners.getListeners();
        for (int i = 0; i < array.length; i++) {
            final ICheckStateListener l = (ICheckStateListener) array[i];
            SafeRunnable.run(new SafeRunnable() {
                public void run() {
                    l.checkStateChanged(event);
                }
            });
        }
    }

    /* (non-Javadoc)
     * Method declared on ICheckable.
     */
    public boolean getChecked(Object element) {
        Widget widget = findItem(element);
        if (widget instanceof TableItem) {
            return ((TableItem) widget).getChecked();
        }
        return false;
    }

    /**
     * Returns a list of elements corresponding to checked table items in this
     * viewer.
     * <p>
     * This method is typically used when preserving the interesting
     * state of a viewer; <code>setCheckedElements</code> is used during the restore.
     * </p>
     *
     * @return the array of checked elements
     * @see #setCheckedElements
     */
    public Object[] getCheckedElements() {
        TableItem[] children = getTable().getItems();
        ArrayList v = new ArrayList(children.length);
        for (int i = 0; i < children.length; i++) {
            TableItem item = children[i];
            Object data = item.getData();
			if (data != null) {
				if (item.getChecked()) {
					v.add(data);
				}
			}
        }
        return v.toArray();
    }

    /**
     * Returns the grayed state of the given element.
     *
     * @param element the element
     * @return <code>true</code> if the element is grayed,
     *   and <code>false</code> if not grayed
     */
    public boolean getGrayed(Object element) {
        Widget widget = findItem(element);
        if (widget instanceof TableItem) {
            return ((TableItem) widget).getGrayed();
        }
        return false;
    }

    /**
     * Returns a list of elements corresponding to grayed nodes in this
     * viewer.
     * <p>
     * This method is typically used when preserving the interesting
     * state of a viewer; <code>setGrayedElements</code> is used during the restore.
     * </p>
     *
     * @return the array of grayed elements
     * @see #setGrayedElements
     */
    public Object[] getGrayedElements() {
        TableItem[] children = getTable().getItems();
        List v = new ArrayList(children.length);
        for (int i = 0; i < children.length; i++) {
            TableItem item = children[i];
            Object data = item.getData();
			if (data != null) {
				if (item.getGrayed()) {
					v.add(data);
				}
			}
        }
        return v.toArray();
    }

    /* (non-Javadoc)
     * Method declared on StructuredViewer.
     */
    public void handleSelect(SelectionEvent event) {
        if (event.detail == SWT.CHECK) {
            super.handleSelect(event); // this will change the current selection

            TableItem item = (TableItem) event.item;
            Object data = item.getData();
            if (data != null) {
                fireCheckStateChanged(new CheckStateChangedEvent(this, data,
                        item.getChecked()));
            }
        } else {
			super.handleSelect(event);
		}
    }

    /* (non-Javadoc)
     * Method declared on Viewer.
     */
    protected void preservingSelection(Runnable updateCode) {
		if (!getPreserveSelection()) {
			return;
		}
    	//If a check provider is present, it determines the state across input
    	//changes.
    	if(checkStateProvider != null) {
    		//Try to preserve the selection, let the ICheckProvider manage 
    		//the check states
    		super.preservingSelection(updateCode);
    		return;
    	}
    	
    	//Preserve checked items
        TableItem[] children = getTable().getItems();
        CustomHashtable checked = newHashtable(children.length * 2 + 1);
        CustomHashtable grayed = newHashtable(children.length * 2 + 1);

        for (int i = 0; i < children.length; i++) {
            TableItem item = children[i];
            Object data = item.getData();
            if (data != null) {
                if (item.getChecked()) {
					checked.put(data, data);
				}
                if (item.getGrayed()) {
					grayed.put(data, data);
				}
            }
        }

        super.preservingSelection(updateCode);

        children = getTable().getItems();
        for (int i = 0; i < children.length; i++) {
            TableItem item = children[i];
            Object data = item.getData();
            if (data != null) {
                item.setChecked(checked.containsKey(data));
                item.setGrayed(grayed.containsKey(data));
            }
        }
    }

    /* (non-Javadoc)
     * Method declared on ICheckable.
     */
    public void removeCheckStateListener(ICheckStateListener listener) {
        checkStateListeners.remove(listener);
    }

    /**
     * Sets to the given value the checked state for all elements in this viewer.
     * Does not fire events to check state listeners.
     *
     * @param state <code>true</code> if the element should be checked,
     *  and <code>false</code> if it should be unchecked
     */
    public void setAllChecked(boolean state) {
        TableItem[] children = getTable().getItems();
        for (int i = 0; i < children.length; i++) {
            TableItem item = children[i];
            if (item.getData() != null) {
				if (item.getChecked() != state)
					item.setChecked(state);
			}
        }
    }

    /**
     * Sets to the given value the grayed state for all elements in this viewer.
     *
     * @param state <code>true</code> if the element should be grayed,
     *  and <code>false</code> if it should be ungrayed
     */
    public void setAllGrayed(boolean state) {
        TableItem[] children = getTable().getItems();
        for (int i = 0; i < children.length; i++) {
            TableItem item = children[i];
            if (item.getData() != null) {
				if (item.getGrayed() != state)
					item.setGrayed(state);
			}
        }
    }

    /* (non-Javadoc)
     * Method declared on ICheckable.
     */
    public boolean setChecked(Object element, boolean state) {
        Assert.isNotNull(element);
        Widget widget = findItem(element);
        if (widget instanceof TableItem) {
            TableItem item = (TableItem) widget;
			if (item.getChecked() != state)
				item.setChecked(state);
            return true;
        }
        return false;
    }

    /**
     * Sets which nodes are checked in this viewer.
     * The given list contains the elements that are to be checked;
     * all other nodes are to be unchecked.
     * Does not fire events to check state listeners.
     * <p>
     * This method is typically used when restoring the interesting
     * state of a viewer captured by an earlier call to <code>getCheckedElements</code>.
     * </p>
     *
     * @param elements the list of checked elements (element type: <code>Object</code>)
     * @see #getCheckedElements
     */
    public void setCheckedElements(Object[] elements) {
        assertElementsNotNull(elements);
        CustomHashtable set = newHashtable(elements.length * 2 + 1);
        for (int i = 0; i < elements.length; ++i) {
            set.put(elements[i], elements[i]);
        }
        TableItem[] items = getTable().getItems();
        for (int i = 0; i < items.length; ++i) {
            TableItem item = items[i];
            Object element = item.getData();
            if (element != null) {
                boolean check = set.containsKey(element);
                // only set if different, to avoid flicker
                if (item.getChecked() != check) {
                    item.setChecked(check);
                }
            }
        }
    }

    /**
     * Sets the grayed state for the given element in this viewer.
     *
     * @param element the element
     * @param state <code>true</code> if the item should be grayed,
     *  and <code>false</code> if it should be ungrayed
     * @return <code>true</code> if the element is visible and the gray
     *  state could be set, and <code>false</code> otherwise
     */
    public boolean setGrayed(Object element, boolean state) {
        Assert.isNotNull(element);
        Widget widget = findItem(element);
        if (widget instanceof TableItem) {
            TableItem item = (TableItem) widget;
			if (item.getGrayed() != state)
				item.setGrayed(state);
            return true;
        }
        return false;
    }

    /**
     * Sets which nodes are grayed in this viewer.
     * The given list contains the elements that are to be grayed;
     * all other nodes are to be ungrayed.
     * <p>
     * This method is typically used when restoring the interesting
     * state of a viewer captured by an earlier call to <code>getGrayedElements</code>.
     * </p>
     *
     * @param elements the array of grayed elements
     *
     * @see #getGrayedElements
     */
    public void setGrayedElements(Object[] elements) {
        assertElementsNotNull(elements);
        CustomHashtable set = newHashtable(elements.length * 2 + 1);
        for (int i = 0; i < elements.length; ++i) {
            set.put(elements[i], elements[i]);
        }
        TableItem[] items = getTable().getItems();
        for (int i = 0; i < items.length; ++i) {
            TableItem item = items[i];
            Object element = item.getData();
            if (element != null) {
                boolean gray = set.containsKey(element);
                // only set if different, to avoid flicker
                if (item.getGrayed() != gray) {
                    item.setGrayed(gray);
                }
            }
        }
    }
}
