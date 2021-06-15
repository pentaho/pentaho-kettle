/*******************************************************************************
 * Copyright (c) 2008 Micah Hainline and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Micah Hainline - initial API and implementation
 *     Stefan Xenos, IBM Corporation - review, javadoc, and extendedMargins(Rectangle)
 *******************************************************************************/
package org.eclipse.jface.layout;

import java.io.Serializable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * RowLayoutFactory creates and initializes row layouts. There are two ways to
 * use RowLayoutFactory. Normally, it is used as a shorthand for writing
 * "new RowLayout()" and initializing a bunch of fields. In this case the main
 * benefit is a more concise syntax and the ability to create more than one
 * identical RowLayout from the same factory. Changing a property of the factory
 * will affect future layouts created by the factory, but has no effect on
 * layouts that have already been created.
 * 
 * @since 1.3
 */
public final class RowLayoutFactory implements Serializable {
	/**
	 * Template layout. The factory will create copies of this layout.
	 */
	private RowLayout layout;

	/**
	 * Creates a new RowLayoutFactory that will create copies of the given
	 * layout.
	 * 
	 * @param layout
	 *            layout to copy
	 */
	private RowLayoutFactory(RowLayout layout) {
		this.layout = layout;
	}

	/**
	 * Creates a factory that creates copies of the given layout.
	 * 
	 * @param layout
	 *            layout to copy
	 * @return a new RowLayoutFactory instance that creates copies of the given
	 *         layout
	 */
	public static RowLayoutFactory createFrom(RowLayout layout) {
		return new RowLayoutFactory(copyLayout(layout));
	}

	/**
	 * Creates a copy of the receiver.
	 * 
	 * @return a copy of the receiver
	 */
	public RowLayoutFactory copy() {
		return new RowLayoutFactory(create());
	}

	/**
	 * Creates a RowLayoutFactory that creates RowLayouts with the default SWT
	 * values.
	 * 
	 * <p>
	 * Initial values are:
	 * </p>
	 * 
	 * <ul>
	 * <li>margins(0,0)</li>
	 * <li>extendedMargins(3,3,3,3)</li>
	 * <li>wrap(true)</li>
	 * <li>pack(true)</li>
	 * <li>fill(false)</li>
	 * <li>justify(false)</li>
	 * <li>spacing(3)</li>
	 * </ul>
	 * 
	 * @return a RowLayoutFactory that creates RowLayouts as though created with
	 *         their default constructor
	 * @see #fillDefaults
	 */
	public static RowLayoutFactory swtDefaults() {
		return new RowLayoutFactory(new RowLayout());
	}

	/**
	 * Creates a RowLayoutFactory that creates RowLayouts with no margins, fill
	 * behavior, and default dialog spacing.
	 * 
	 * <p>
	 * Initial values are:
	 * </p>
	 * 
	 * <ul>
	 * <li>margins(0,0)</li>
	 * <li>extendedMargins(0,0,0,0)</li>
	 * <li>wrap(true)</li>
	 * <li>pack(true)</li>
	 * <li>fill(false)</li>
	 * <li>justify(false)</li>
	 * <li>spacing(LayoutConstants.getSpacing().x</li>
	 * </ul>
	 * 
	 * @return a RowLayoutFactory that creates RowLayouts with no margins
	 * @see #swtDefaults
	 */
	public static RowLayoutFactory fillDefaults() {
		RowLayout layout = new RowLayout();
		layout.marginTop = 0;
		layout.marginBottom = 0;
		layout.marginLeft = 0;
		layout.marginRight = 0;
		layout.spacing = LayoutConstants.getSpacing().x;
		return new RowLayoutFactory(layout);
	}

	/**
	 * Sets the spacing for layouts created with this factory. The spacing is
	 * the distance between items within the layout.
	 * 
	 * @param spacing
	 *            spacing (pixels)
	 * @return this
	 * @see #margins(Point)
	 * @see #margins(int, int)
	 */
	public RowLayoutFactory spacing(int spacing) {
		layout.spacing = spacing;
		return this;
	}

	/**
	 * Sets the margins for layouts created with this factory. The margins are
	 * the distance between the outer cells and the edge of the layout.
	 * 
	 * @param margins
	 *            margin size (pixels)
	 * @return this
	 * @see #spacing(int)
	 */
	public RowLayoutFactory margins(Point margins) {
		layout.marginWidth = margins.x;
		layout.marginHeight = margins.y;
		return this;
	}

	/**
	 * Sets the margins for layouts created with this factory. The margins
	 * specify the number of pixels of horizontal and vertical margin that will
	 * be placed along the left/right and top/bottom edges of the layout. Note
	 * that these margins will be added to the ones specified by
	 * {@link #extendedMargins(int, int, int, int)}.
	 * 
	 * @param width
	 *            margin width (pixels)
	 * @param height
	 *            margin height (pixels)
	 * @return this
	 * @see #spacing(int)
	 */
	public RowLayoutFactory margins(int width, int height) {
		layout.marginWidth = width;
		layout.marginHeight = height;
		return this;
	}

	/**
	 * Sets the margins for layouts created with this factory. The margins
	 * specify the number of pixels of horizontal and vertical margin that will
	 * be placed along the left, right, top, and bottom edges of the layout.
	 * Note that these margins will be added to the ones specified by
	 * {@link #margins(int, int)}.
	 * 
	 * @param left
	 *            left margin size (pixels)
	 * @param right
	 *            right margin size (pixels)
	 * @param top
	 *            top margin size (pixels)
	 * @param bottom
	 *            bottom margin size (pixels)
	 * @return this
	 * @see #spacing(int)
	 * 
	 */
	public RowLayoutFactory extendedMargins(int left, int right, int top,
			int bottom) {
		layout.marginLeft = left;
		layout.marginRight = right;
		layout.marginTop = top;
		layout.marginBottom = bottom;
		return this;
	}

	/**
	 * Fill specifies whether the controls in a row should be all the same
	 * height for horizontal layouts, or the same width for vertical layouts.
	 * 
	 * @param fill
	 *            the fill status
	 * @return this
	 */
	public RowLayoutFactory fill(boolean fill) {
		layout.fill = fill;
		return this;
	}

	/**
	 * Justify specifies whether the controls in a row should be fully
	 * justified, with any extra space placed between the controls.
	 * 
	 * @param justify
	 *            the justify status
	 * @return this
	 */
	public RowLayoutFactory justify(boolean justify) {
		layout.justify = justify;
		return this;
	}

	/**
	 * Pack specifies whether all controls in the layout take their preferred
	 * size. If pack is false, all controls will have the same size which is the
	 * size required to accommodate the largest preferred height and the largest
	 * preferred width of all the controls in the layout.
	 * 
	 * @param pack
	 *            the pack status
	 * @return this
	 */
	public RowLayoutFactory pack(boolean pack) {
		layout.pack = pack;
		return this;
	}

	/**
	 * Wrap specifies whether a control will be wrapped to the next row if there
	 * is insufficient space on the current row.
	 * 
	 * @param wrap
	 *            the wrap status
	 * @return this
	 */
	public RowLayoutFactory wrap(boolean wrap) {
		layout.wrap = wrap;
		return this;
	}

	/**
	 * type specifies whether the layout places controls in rows or columns.
	 * 
	 * Possible values are:
	 * <ul>
	 * <li>HORIZONTAL: Position the controls horizontally from left to right</li>
	 * <li>VERTICAL: Position the controls vertically from top to bottom</li>
	 * </ul>
	 * 
	 * @param type
	 *            One of SWT.HORIZONTAL or SWT.VERTICAL
	 * @return this
	 * 
	 * @throws IllegalArgumentException
	 *             if type is not one of HORIZONTAL or VERTICAL
	 */
	public RowLayoutFactory type(int type) {
		if (type != SWT.HORIZONTAL && type != SWT.VERTICAL) {
			throw new IllegalArgumentException();
		}
		layout.type = type;
		return this;
	}

	/**
	 * Creates a new RowLayout, and initializes it with values from the factory.
	 * 
	 * @return a new initialized RowLayout.
	 * @see #applyTo
	 */
	public RowLayout create() {
		return copyLayout(layout);
	}

	/**
	 * Creates a new RowLayout and attaches it to the given composite. Does not
	 * create the rowData of any of the controls in the composite.
	 * 
	 * @param c
	 *            composite whose layout will be set
	 * @see #create
	 * @see RowLayoutFactory
	 */
	public void applyTo(Composite c) {
		c.setLayout(copyLayout(layout));
	}

	/**
	 * Copies the given RowLayout instance
	 * 
	 * @param layout
	 *            layout to copy
	 * @return a new RowLayout
	 */
	public static RowLayout copyLayout(RowLayout layout) {
		RowLayout result = new RowLayout(layout.type);
		result.marginBottom = layout.marginBottom;
		result.marginTop = layout.marginTop;
		result.marginLeft = layout.marginLeft;
		result.marginRight = layout.marginRight;
		result.marginHeight = layout.marginHeight;
		result.marginWidth = layout.marginWidth;

		result.fill = layout.fill;
		result.justify = layout.justify;
		result.pack = layout.pack;
		result.spacing = layout.spacing;
		result.wrap = layout.wrap;

		result.type = layout.type;

		return result;
	}
}
