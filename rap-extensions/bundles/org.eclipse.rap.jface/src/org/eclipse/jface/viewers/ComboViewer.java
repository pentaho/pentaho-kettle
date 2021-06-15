/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids - bug 69254
 *******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A concrete viewer based either on an SWT <code>Combo</code> control or <code>CCombo</code>
 * control. This class is intended as an alternative to the JFace <code>ListViewer</code>, which displays
 * its content in a combo box rather than a list. Wherever possible, this class attempts to behave
 * like ListViewer. <p>
 *
 * This class is designed to be instantiated with a pre-existing SWT combo control
 * and configured with a domain-specific content provider, label provider, element
 * filter (optional), and element sorter (optional).
 * </p>
 *
 * @see org.eclipse.jface.viewers.ListViewer
 * @since 1.0 (made non-final in 3.4)
 */
public class ComboViewer extends AbstractListViewer {

    /**
     * This viewer's list control if this viewer is instantiated with a combo control; otherwise
     * <code>null</code>.
     *
     * @see #ComboViewer(Combo)
     */
    private Combo combo;

    /**
     * This viewer's list control if this viewer is instantiated with a CCombo control; otherwise
     * <code>null</code>.
     *
     * @see #ComboViewer(CCombo)
     * @since 1.0
     */
    private CCombo ccombo;

    /**
     * Creates a combo viewer on a newly-created combo control under the given parent.
     * The viewer has no input, no content provider, a default label provider,
     * no sorter, and no filters.
     *
     * @param parent the parent control
     */
    public ComboViewer(Composite parent) {
        this(parent, SWT.READ_ONLY | SWT.BORDER);
    }

    /**
     * Creates a combo viewer on a newly-created combo control under the given parent.
     * The combo control is created using the given SWT style bits.
     * The viewer has no input, no content provider, a default label provider,
     * no sorter, and no filters.
     *
     * @param parent the parent control
     * @param style the SWT style bits
     */
    public ComboViewer(Composite parent, int style) {
        this(new Combo(parent, style));
    }

    /**
     * Creates a combo viewer on the given combo control.
     * The viewer has no input, no content provider, a default label provider,
     * no sorter, and no filters.
     *
     * @param list the combo control
     */
    public ComboViewer(Combo list) {
        this.combo = list;
        hookControl(list);
    }

    /**
     * Creates a combo viewer on the given CCombo control.
     * The viewer has no input, no content provider, a default label provider,
     * no sorter, and no filters.
     *
     * @param list the CCombo control
     * @since 1.0
     */
    public ComboViewer(CCombo list) {
        this.ccombo = list;
        hookControl(list);
    }

    protected void listAdd(String string, int index) {
        if (combo == null) {
            ccombo.add(string, index);
        } else {
            combo.add(string, index);
        }
    }

    protected void listSetItem(int index, String string) {
        if (combo == null) {
            ccombo.setItem(index, string);
        } else {
            combo.setItem(index, string);
        }
    }

    protected int[] listGetSelectionIndices() {
        if (combo == null) {
            return new int[] { ccombo.getSelectionIndex() };
        } else {
            return new int[] { combo.getSelectionIndex() };
        }
    }

    protected int listGetItemCount() {
        if (combo == null) {
            return ccombo.getItemCount();
        } else {
            return combo.getItemCount();
        }
    }

    protected void listSetItems(String[] labels) {
        if (combo == null) {
            ccombo.setItems(labels);
        } else {
            combo.setItems(labels);
        }
    }

    protected void listRemoveAll() {
        if (combo == null) {
            ccombo.removeAll();
        } else {
            combo.removeAll();
        }
    }

    protected void listRemove(int index) {
        if (combo == null) {
            ccombo.remove(index);
        } else {
            combo.remove(index);
        }
    }

    /* (non-Javadoc)
     * Method declared on Viewer.
     */
    public Control getControl() {
        if (combo == null) {
            return ccombo;
        } else {
            return combo;
        }
    }

    /**
	 * Returns this list viewer's list control. If the viewer was not created on
	 * a CCombo control, some kind of unchecked exception is thrown.
	 *
	 * @return the list control
     * @since 1.0
	 */
    public CCombo getCCombo() {
        Assert.isNotNull(ccombo);
        return ccombo;
    }

    /**
     * Returns this list viewer's list control. If the viewer was not created on
	 * a Combo control, some kind of unchecked exception is thrown.
     *
     * @return the list control
     */
    public Combo getCombo() {
    	Assert.isNotNull(combo);
        return combo;
    }

    /*
     * Do nothing -- combos only display the selected element, so there is no way
     * we can ensure that the given element is visible without changing the selection.
     * Method defined on StructuredViewer.
     */
    public void reveal(Object element) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractListViewer#listSetSelection(int[])
     */
    protected void listSetSelection(int[] ixs) {
        if (combo == null) {
            for (int idx = 0; idx < ixs.length; idx++) {
                ccombo.select(ixs[idx]);
            }
        } else {
            for (int idx = 0; idx < ixs.length; idx++) {
                combo.select(ixs[idx]);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractListViewer#listDeselectAll()
     */
    protected void listDeselectAll() {
        if (combo == null) {
            ccombo.deselectAll();
            ccombo.clearSelection();
        } else {
            combo.deselectAll();
            combo.clearSelection();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractListViewer#listShowSelection()
     */
    protected void listShowSelection() {
    }
}
