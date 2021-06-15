/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stefan Xenos, IBM - initial implementation, bug 178888
 *******************************************************************************/
package org.eclipse.jface.layout;
import java.io.Serializable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Control;

/**
 * This class provides a convienient shorthand for creating and initializing
 * GridData. This offers several benefits over creating GridData normal way:
 * 
 * <ul>
 * <li>The same factory can be used many times to create several GridData instances</li>
 * <li>The setters on GridDataFactory all return "this", allowing them to be chained</li> 
 * <li>GridDataFactory uses vector setters (it accepts Points), making it easy to
 *     set X and Y values together</li>
 * </ul>
 * 
 * <p>
 * GridDataFactory instances are created using one of the static methods on this class. 
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * <code><pre>
 * 
 * ////////////////////////////////////////////////////////////
 * // Example 1: Typical grid data for a non-wrapping label
 * 
 *     // GridDataFactory version
 *     GridDataFactory.fillDefaults().applyTo(myLabel);
 * 
 *     // Equivalent SWT version
 *     GridData labelData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
 *     myLabel.setLayoutData(labelData);
 * 
 * ///////////////////////////////////////////////////////////
 * // Example 2: Typical grid data for a wrapping label
 * 
 *     // GridDataFactory version
 *     GridDataFactory.fillDefaults()
 *          .align(SWT.FILL, SWT.CENTER)
 *    	    .hint(150, SWT.DEFAULT)
 *    	    .grab(true, false)
 *          .applyTo(wrappingLabel);
 *      
 *     // Equivalent SWT version
 *     GridData wrappingLabelData = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER);
 *     wrappingLabelData.minimumWidth = 1;
 *     wrappingLabelData.widthHint = 150;
 *     wrappingLabel.setLayoutData(wrappingLabelData);
 * 
 * //////////////////////////////////////////////////////////////
 * // Example 3: Typical grid data for a scrollable control (a list box, tree, table, etc.)
 * 
 *     // GridDataFactory version
 *     GridDataFactory.fillDefaults().grab(true, true).hint(150, 150).applyTo(listBox);
 * 
 *     // Equivalent SWT version
 *     GridData listBoxData = new GridData(GridData.FILL_BOTH);
 *     listBoxData.widthHint = 150;
 *     listBoxData.heightHint = 150;
 *     listBoxData.minimumWidth = 1;
 *     listBoxData.minimumHeight = 1;
 *     listBox.setLayoutData(listBoxData);
 * 
 * /////////////////////////////////////////////////////////////
 * // Example 4: Typical grid data for a button
 *
 *     // GridDataFactory version
 *     Point preferredSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
 *     Point hint = Geometry.max(LayoutConstants.getMinButtonSize(), preferredSize);
 *     GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).hint(hint).applyTo(button);
 *
 *     // Equivalent SWT version
 *     Point preferredSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
 *     Point hint = Geometry.max(LayoutConstants.getMinButtonSize(), preferredSize);
 *     GridData buttonData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
 *     buttonData.widthHint = hint.x;
 *     buttonData.heightHint = hint.y;
 *     button.setLayoutData(buttonData);
 *     
 * /////////////////////////////////////////////////////////////
 * // Example 5: Generated GridData
 *
 *     // Generates GridData a wrapping label that spans 2 columns
 *     GridDataFactory.generate(wrappingLabel, 2, 1);
 *     
 *     // Generates GridData for a listbox. and adjusts the preferred size to 300x400 pixels
 *     GridDataFactory.defaultsFor(listBox).hint(300, 400).applyTo(listBox);
 *     
 *     // Generates GridData equivalent to example 4
 *     GridDataFactory.generate(button, 1, 1);
 *          
 * </pre></code>
 * 
 * @since 1.0
 */
public final class GridDataFactory implements Serializable {
    private GridData data;
    
    /**
     * Creates a GridDataFactory that creates copes of the given GridData. 
     * 
     * @param d template GridData to copy
     */
    private GridDataFactory(GridData d) {
        this.data = d;
    }
    
    /**
     * Creates a new GridDataFactory initialized with the SWT defaults.
     * This factory will generate GridData that is equivalent to 
     * "new GridData()".
     * 
     * <p>
     * Initial values are:
     * </p>
     * 
     * <ul>
     * <li>align(SWT.BEGINNING, SWT.CENTER)</li>
     * <li>exclude(false)</li>
     * <li>grab(false, false)</li>
     * <li>hint(SWT.DEFAULT, SWT.DEFAULT)</li>
     * <li>indent(0,0)</li>
     * <li>minSize(0,0)</li>
     * <li>span(1,1)</li>
     * </ul>
     * 
     * @return a new GridDataFactory instance
     * @see #fillDefaults()
     */
    public static GridDataFactory swtDefaults() {
    	return new GridDataFactory(new GridData());
    }
    
    /**
     * Creates a new GridDataFactory that creates copies of the given GridData
     * by default.
     * 
     * @param data GridData to copy
     * @return a new GridDataFactory that creates copies of the argument by default
     */
    public static GridDataFactory createFrom(GridData data) {
    	return new GridDataFactory(copyData(data));
    }
    
    /**
     * Creates a GridDataFactory initialized with defaults that will cause
     * the control to fill its cell. The minimum size is set to the smallest possible
     * minimum size supported by SWT. Currently, the smallest supported minimum size
     * is (1,1) so this is the default. If GridLayout ever adds support for grid data
     * with no minimum size, this will be changed to 0,0 in the future. 
     * 
     * <p>
     * Initial values are:
     * </p>
     * 
     * <ul>
     * <li>align(SWT.FILL, SWT.FILL)</li>
     * <li>exclude(false)</li>
     * <li>grab(false, false)</li>
     * <li>hint(SWT.DEFAULT, SWT.DEFAULT)</li>
     * <li>indent(0,0)</li>
     * <li>minSize(1,1)</li>
     * <li>span(1,1)</li>
     * </ul>
     *  
     * @return a GridDataFactory that makes controls fill their grid by default
     * 
     * @see #swtDefaults()
     */
    public static GridDataFactory fillDefaults() {
    	GridData data = new GridData();
        data.minimumWidth = 1;
        data.minimumHeight = 1;
        data.horizontalAlignment = SWT.FILL;
        data.verticalAlignment = SWT.FILL;
        
    	return new GridDataFactory(data);
    }
    
    /**
     * Returns a GridDataFactory initialized with heuristicly generated defaults for the given control.
     * To be precise, this method picks the default values that GridLayoutFactory.generateLayout
     * would have assigned to the control. Does not attach GridData to the control. Callers must
     * additionally call applyTo(theControl) if they wish to use the generated values.
     * 
     * <p>
     * This method is intended for situations where generateLayout is generating layout data
     * for a particular control that is not quite right for the desired layout.
     * This allows callers to start with the generated values and tweak one or two settings
     * before applying the GridData to the control.
     * </p> 
     * 
     * @see GridLayoutFactory#generateLayout(org.eclipse.swt.widgets.Composite)
     * @param theControl 
     * @return a GridLayoutFactory initialized with defaults that GridLayoutFactory would have 
     */
    public static GridDataFactory defaultsFor(Control theControl) {
    	return LayoutGenerator.defaultsFor(theControl);
    }
    
    /**
     * Generates layout data to the given control, given the number of cells
     * spanned by the control. Attaches a GridData to the control. This method 
     * allows generated layout data to be used with controls that span multiple cells. 
     * <p>
     * The generated layout data is the same as what would be generated by 
     * GridLayoutFactory.generateLayout, except that the span is configurable
     * </p>  
     * 
     * @see GridLayoutFactory#generateLayout(org.eclipse.swt.widgets.Composite)
     * @param theControl
     * @param hSpan number of columns spanned by the control
     * @param vSpan number of rows spanned by the control
     */
    public static void generate(Control theControl, int hSpan, int vSpan) {
    	defaultsFor(theControl).span(hSpan, vSpan).applyTo(theControl);
    }

    /**
     * Generates layout data to the given control, given the number of cells
     * spanned by the control. Attaches GridData to the control. This method 
     * allows generated layout data to be used with controls that span multiple cells. 
     * <p>
     * The generated layout data is the same as what would be generated by 
     * GridLayoutFactory.generateLayout, except that the span is configurable
     * </p>  
     * 
     * @see GridLayoutFactory#generateLayout(org.eclipse.swt.widgets.Composite)
     * @param theControl
     * @param span The x coordinate indicates the number of
     * columns spanned, and the y coordinate indicates the number of rows.
     */
    public static void generate(Control theControl, Point span) {
    	defaultsFor(theControl).span(span).applyTo(theControl);
    }
    
    /**
     * Sets the GridData span. The span controls how many cells
     * are filled by the control. 
     * 
     * @param hSpan number of columns spanned by the control
     * @param vSpan number of rows spanned by the control
     * @return this
     */
    public GridDataFactory span(int hSpan, int vSpan) {
        data.horizontalSpan = hSpan;
        data.verticalSpan = vSpan;
        return this;
    }

    /**
     * Sets the GridData span. The span controls how many cells
     * are filled by the control. 
     * 
     * @param span the new span. The x coordinate indicates the number of
     * columns spanned, and the y coordinate indicates the number of rows.
     * @return this
     */
    public GridDataFactory span(Point span) {
        data.horizontalSpan = span.x;
        data.verticalSpan = span.y;
        return this;
    }

    /**
     * Sets the width and height hints. The width and height hints override
     * the control's preferred size. If either hint is set to SWT.DEFAULT,
     * the control's preferred size is used. 
     * 
     * @param xHint horizontal hint (pixels), or SWT.DEFAULT to use the control's preferred size
     * @param yHint vertical hint (pixels), or SWT.DEFAULT to use the control's preferred size
     * @return this
     */
    public GridDataFactory hint(int xHint, int yHint) {
        data.widthHint = xHint;
        data.heightHint = yHint;
        return this;
    }

    /**
     * Sets the width and height hints. The width and height hints override
     * the control's preferred size. If either hint is set to SWT.DEFAULT,
     * the control's preferred size is used.
     * 
     * @param hint size (pixels) to be used instead of the control's preferred size. If
     * the x or y values are set to SWT.DEFAULT, the control's computeSize() method will
     * be used to obtain that dimension of the preferred size.
     * @return this
     */
    public GridDataFactory hint(Point hint) {
        data.widthHint = hint.x;
        data.heightHint = hint.y;
        return this;
    }

    /**
     * Sets the alignment of the control within its cell.
     * 
     * @param hAlign horizontal alignment. One of SWT.BEGINNING, SWT.CENTER, SWT.END, or SWT.FILL.
     * @param vAlign vertical alignment. One of SWT.BEGINNING, SWT.CENTER, SWT.END, or SWT.FILL.
     * @return this
     */
    public GridDataFactory align(int hAlign, int vAlign) {
    	if (hAlign != SWT.BEGINNING && hAlign != SWT.CENTER && hAlign != GridData.CENTER && hAlign != SWT.END && hAlign != GridData.END && hAlign != SWT.FILL && hAlign != SWT.LEFT && hAlign != SWT.RIGHT) {
    		throw new IllegalArgumentException();
    	}
    	if (vAlign != SWT.BEGINNING && vAlign != SWT.CENTER && vAlign != GridData.CENTER && vAlign != SWT.END && vAlign != GridData.END && vAlign != SWT.FILL && vAlign != SWT.TOP && vAlign != SWT.BOTTOM) {
    		throw new IllegalArgumentException();
    	}
        data.horizontalAlignment = hAlign;
        data.verticalAlignment = vAlign;
        return this;
    }

    /**
     * Sets the indent of the control within the cell. Moves the position of the control
     * by the given number of pixels. Positive values move toward the lower-right, negative
     * values move toward the upper-left.
     * 
     * @param hIndent distance to move to the right (negative values move left)
     * @param vIndent distance to move down (negative values move up)
     * @return this
     */
    public GridDataFactory indent(int hIndent, int vIndent) {
        data.horizontalIndent = hIndent;
        data.verticalIndent = vIndent;
        return this;
    }

    /**
     * Sets the indent of the control within the cell. Moves the position of the control
     * by the given number of pixels. Positive values move toward the lower-right, negative
     * values move toward the upper-left.
     * 
     * @param indent offset to move the control
     * @return this
     */
    public GridDataFactory indent(Point indent) {
        data.horizontalIndent = indent.x;
        data.verticalIndent = indent.y;
        return this;
    }

    /**
     * Determines whether extra horizontal or vertical space should be allocated to
     * this control's column when the layout resizes. If any control in the column
     * is set to grab horizontal then the whole column will grab horizontal space.
     * If any control in the row is set to grab vertical then the whole row will grab
     * vertical space.
     * 
     * @param horizontal true if the control's column should grow horizontally
     * @param vertical true if the control's row should grow vertically
     * @return this
     */
    public GridDataFactory grab(boolean horizontal, boolean vertical) {
        data.grabExcessHorizontalSpace = horizontal;
        data.grabExcessVerticalSpace = vertical;
        return this;
    }

    /**
     * Sets the minimum size for the control. The control will not be permitted
     * to shrink below this size. Note: GridLayout treats a minimum size of 0
     * as an undocumented special value, so the smallest possible minimum size 
     * is a size of 1. A minimum size of SWT.DEFAULT indicates that the result
     * of computeSize(int, int, boolean) should be used as the control's minimum
     * size.
     * 
     * 
     * @param minX minimum a value of 1 or more is a horizontal size of the control (pixels). 
     *        SWT.DEFAULT indicates that the control's preferred size should be used. A size
     *        of 0 has special semantics defined by GridLayout. 
     * @param minY minimum a value of 1 or more is a vertical size of the control (pixels). SWT.DEFAULT
     *        indicates that the control's preferred size should be used. A size
     *        of 0 has special semantics defined by GridLayout.
     * @return this
     */
    public GridDataFactory minSize(int minX, int minY) {
        data.minimumWidth = minX;
        data.minimumHeight = minY;
        return this;
    }

    /**
     * Sets the minimum size for the control. The control will not be permitted
     * to shrink below this size. Note: GridLayout treats a minimum size of 0
     * as an undocumented special value, so the smallest possible minimum size 
     * is a size of 1. A minimum size of SWT.DEFAULT indicates that the result
     * of computeSize(int, int, boolean) should be used as the control's minimum
     * size.
     * 
     * @param min minimum size of the control
     * @return this
     */
    public GridDataFactory minSize(Point min) {
        data.minimumWidth = min.x;
        data.minimumHeight = min.y;
        return this;
    }

    /**
     * Instructs the GridLayout to ignore this control when performing layouts. 
     * 
     * @param shouldExclude true iff the control should be excluded from layouts
     * @return this
     */
    public GridDataFactory exclude(boolean shouldExclude) {
        data.exclude = shouldExclude;
        return this;
    }

    /**
     * Creates a new GridData instance. All attributes of the GridData instance
     * will be initialized by the factory.
     * 
     * @return a new GridData instance
     */
    public GridData create() {
        return copyData(data);
    }

    /**
     * Creates a copy of the receiver.
     * 
     * @return a copy of the receiver
     */
    public GridDataFactory copy() {
    	return new GridDataFactory(create());
    }
    
    /**
     * Returns a copy of the given GridData 
     * 
     * @param data GridData to copy
     * @return a copy of the argument
     */
    public static GridData copyData(GridData data) {
        GridData newData = new GridData(data.horizontalAlignment, data.verticalAlignment, data.grabExcessHorizontalSpace, data.grabExcessVerticalSpace, data.horizontalSpan,
                data.verticalSpan);
        newData.exclude = data.exclude;
        newData.heightHint = data.heightHint;
        newData.horizontalIndent = data.horizontalIndent;
        newData.minimumHeight = data.minimumHeight;
        newData.minimumWidth = data.minimumWidth;
        newData.verticalIndent = data.verticalIndent;
        newData.widthHint = data.widthHint;

        return newData;
    }

    /**
     * Sets the layout data on the given control. Creates a new GridData instance and
     * assigns it to the control by calling control.setLayoutData.
     *  
     * @param control control whose layout data will be initialized
     */
    public void applyTo(Control control) {
        control.setLayoutData(create());
    }

}
