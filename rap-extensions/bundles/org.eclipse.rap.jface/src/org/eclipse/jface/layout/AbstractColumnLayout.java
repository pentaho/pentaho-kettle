/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation (original file org.eclipse.ui.texteditor.templates.ColumnLayout)
 *     Tom Schindl <tom.schindl@bestsolution.at> - refactored to be widget independent (bug 171824)
 *                                               - fix for bug 178280, 184342, 184045, 208014, 214532
 *     Micah Hainline <micah_hainline@yahoo.com> - fix in bug: 208335
 *******************************************************************************/
package org.eclipse.jface.layout;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.rap.rwt.theme.BoxDimensions;
import org.eclipse.rap.rwt.theme.ControlThemeAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Widget;

/**
 * The AbstractColumnLayout is a {@link Layout} used to set the size of a table
 * in a consistent way even during a resize unlike a {@link TableLayout} which
 * only sets initial sizes.
 *
 * <p>
 * <b>You can only add the layout to a container whose only child is the
 * table/tree control you want the layouts applied to.</b>
 * </p>
 *
 * @since 1.1
 */
public abstract class AbstractColumnLayout extends Layout {
    private static int COLUMN_TRIM;
    static {
        if (Util.isWindows()) {
            COLUMN_TRIM = 4;
        } else if (Util.isMac()) {
            COLUMN_TRIM = 24;
        } else {
            COLUMN_TRIM = 3;
        }
    }

    static final boolean IS_GTK = Util.isGtk();

    /**
     * Key used to restore the layout data in the columns data-slot
     * @since 1.3
     */
    protected static final String LAYOUT_DATA = Policy.JFACE + ".LAYOUT_DATA"; //$NON-NLS-1$

    private boolean inupdateMode = false;

    private boolean relayout = true;

    private Listener resizeListener = new Listener() {

        public void handleEvent(Event event) {
            if (!inupdateMode) {
                updateColumnData(event.widget);
            }
        }

    };

    /**
     * Adds a new column of data to this table layout.
     *
     * @param column
     *            the column
     *
     * @param data
     *            the column layout data
     */
    public void setColumnData(Widget column, ColumnLayoutData data) {

        if (column.getData(LAYOUT_DATA) == null) {
            column.addListener(SWT.Resize, resizeListener);
        }

        column.setData(LAYOUT_DATA, data);
    }

    /**
     * Compute the size of the table or tree based on the ColumnLayoutData and
     * the width and height hint.
     *
     * @param scrollable
     *            the widget to compute
     * @param wHint
     *            the width hint
     * @param hHint
     *            the height hint
     * @return Point where x is the width and y is the height
     */
    private Point computeTableTreeSize(Scrollable scrollable, int wHint,
            int hHint) {
        Point result = scrollable.computeSize(wHint, hHint);

        int width = 0;
        int size = getColumnCount(scrollable);
        for (int i = 0; i < size; ++i) {
            ColumnLayoutData layoutData = getLayoutData(scrollable, i);
            if (layoutData instanceof ColumnPixelData) {
                ColumnPixelData col = (ColumnPixelData) layoutData;
                width += col.width;
                if (col.addTrim) {
                    width += getColumnTrim();
                }
            } else if (layoutData instanceof ColumnWeightData) {
                ColumnWeightData col = (ColumnWeightData) layoutData;
                width += col.minimumWidth;
            } else {
                Assert.isTrue(false, "Unknown column layout data"); //$NON-NLS-1$
            }
        }
        if (width > result.x)
            result.x = width;

        return result;
    }

    /**
     * Layout the scrollable based on the supplied width and area. Only increase
     * the size of the scrollable if increase is <code>true</code>.
     *
     * @param scrollable
     * @param width
     * @param area
     * @param increase
     */
    private void layoutTableTree(final Scrollable scrollable, final int width,
            final Rectangle area, final boolean increase) {
        final int numberOfColumns = getColumnCount(scrollable);
        final int[] widths = new int[numberOfColumns];

        final int[] weightColumnIndices = new int[numberOfColumns];
        int numberOfWeightColumns = 0;

        int fixedWidth = 0;
        int totalWeight = 0;

        // First calc space occupied by fixed columns
        for (int i = 0; i < numberOfColumns; i++) {
            ColumnLayoutData col = getLayoutData(scrollable, i);
            if (col instanceof ColumnPixelData) {
                ColumnPixelData cpd = (ColumnPixelData) col;
                int pixels = cpd.width;
                if (cpd.addTrim) {
                    pixels += getColumnTrim();
                }
                widths[i] = pixels;
                fixedWidth += pixels;
            } else if (col instanceof ColumnWeightData) {
                ColumnWeightData cw = (ColumnWeightData) col;
                weightColumnIndices[numberOfWeightColumns] = i;
                numberOfWeightColumns++;
                totalWeight += cw.weight;
            } else {
                Assert.isTrue(false, "Unknown column layout data"); //$NON-NLS-1$
            }
        }

        boolean recalculate;
        do {
            recalculate = false;
            for (int i = 0; i < numberOfWeightColumns; i++) {
                int colIndex = weightColumnIndices[i];
                ColumnWeightData cw = (ColumnWeightData) getLayoutData(
                        scrollable, colIndex);
                final int minWidth = cw.minimumWidth;
                final int allowedWidth = totalWeight == 0 ? 0
                        : (width - fixedWidth) * cw.weight / totalWeight;
                if (allowedWidth < minWidth) {
                    /*
                     * if the width assigned by weight is less than the minimum,
                     * then treat this column as fixed, remove it from weight
                     * calculations, and recalculate other weights.
                     */
                    numberOfWeightColumns--;
                    totalWeight -= cw.weight;
                    fixedWidth += minWidth;
                    widths[colIndex] = minWidth;
                    System.arraycopy(weightColumnIndices, i + 1,
                            weightColumnIndices, i, numberOfWeightColumns - i);
                    recalculate = true;
                    break;
                }
                widths[colIndex] = allowedWidth;
            }
        } while (recalculate);

        if (increase) {
            scrollable.setSize(area.width, area.height);
        }

        inupdateMode = true;
        setColumnWidths(scrollable, widths);
        scrollable.update();
        inupdateMode = false;

        if (!increase) {
            scrollable.setSize(area.width, area.height);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite
     * , int, int, boolean)
     */
    protected Point computeSize(Composite composite, int wHint, int hHint,
            boolean flushCache) {
        return computeTableTreeSize(getControl(composite), wHint, hHint);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite,
     *      boolean)
     */
    protected void layout(Composite composite, boolean flushCache) {
        Rectangle area = composite.getClientArea();
        Scrollable table = getControl(composite);
        int tableWidth = table.getSize().x;
        int trim = computeTrim(area, table, tableWidth);
        int width = Math.max(0, area.width - trim);

        if (width > 1)
            layoutTableTree(table, width, area, tableWidth < area.width);

        // For the first time we need to relayout because Scrollbars are not
        // calculate appropriately
        if (relayout) {
            relayout = false;
            composite.layout();
        }
    }

    /**
     * Compute the area required for trim.
     *
     * @param area
     * @param scrollable
     * @param currentWidth
     * @return int
     */
    private int computeTrim(Rectangle area, Scrollable scrollable,
            int currentWidth) {
        int trim;

        if (currentWidth > 1) {
            trim = currentWidth - scrollable.getClientArea().width;
        } else {
            // initially, the table has no extend and no client area - use the
            // border with
            // plus some padding as educated guess
            ControlThemeAdapter themeAdapter = scrollable.getAdapter( ControlThemeAdapter.class );
            BoxDimensions border = themeAdapter.getBorder( scrollable );
            trim = border.left + border.right + 1;
        }

        return trim;
    }

    /**
     * Get the control being laid out.
     *
     * @param composite
     *            the composite with the layout
     * @return {@link Scrollable}
     */
    Scrollable getControl(Composite composite) {
        return (Scrollable) composite.getChildren()[0];
    }

    /**
     * Get the number of columns for the receiver.
     *
     * @param tableTree
     *            the control
     * 
     * @return the number of columns
     */
    protected abstract int getColumnCount(Scrollable tableTree);

    /**
     * Set the widths of the columns.
     *
     * @param tableTree
     *            the control
     * 
     * @param widths
     *            the widths of the column
     */
    protected abstract void setColumnWidths(Scrollable tableTree, int[] widths);

    /**
     * Get the layout data for a column
     * 
     * @param tableTree
     *            the control
     * @param columnIndex
     *            the column index
     * @return the layout data, might <b>not</b> null
     * @since 1.3
     */
    protected abstract ColumnLayoutData getLayoutData(Scrollable tableTree,
            int columnIndex);

    /**
     * Update the layout data for a column
     * 
     * @param column
     *            the column
     * @since 1.3
     */
    protected abstract void updateColumnData(Widget column);

    /**
     * The number of extra pixels taken as horizontal trim by the table column.
     * To ensure there are N pixels available for the content of the column,
     * assign N+COLUMN_TRIM for the column width.
     *
     * @return the trim used by the columns
     * @since 1.1
     */
    protected int getColumnTrim() {
        return COLUMN_TRIM;
    }
}
