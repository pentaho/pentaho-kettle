///*******************************************************************************
// * Copyright (c) 2007, 2009 IBM Corporation and others.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// *
// * Contributors:
// *     IBM Corporation - initial API and implementation
// *     Michael Krkoska - initial API and implementation (bug 188333)
// *******************************************************************************/
//package org.eclipse.jface.viewers;
//
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.custom.StyleRange;
//import org.eclipse.swt.graphics.Color;
//import org.eclipse.swt.graphics.GC;
//import org.eclipse.swt.graphics.Image;
//import org.eclipse.swt.graphics.Rectangle;
//import org.eclipse.swt.graphics.TextLayout;
//import org.eclipse.swt.widgets.Display;
//import org.eclipse.swt.widgets.Event;
//import org.eclipse.swt.widgets.Widget;
//
//import org.eclipse.core.runtime.Assert;
//
//import org.eclipse.jface.viewers.StyledString.Styler;
//
///**
// * A {@link StyledCellLabelProvider} supports styled labels by using owner
// * draw.
// * Besides the styles in labels, the label provider preserves native viewer behavior:
// * <ul>
// * <li>similar image and label positioning</li>
// * <li>native drawing of focus and selection</li>
// * </ul>
// * <p>
// * For providing the label's styles, create a subclass and overwrite
// * {@link StyledCellLabelProvider#update(ViewerCell)} to
// * return set all information needed to render a element. Use
// * {@link ViewerCell#setStyleRanges(StyleRange[])} to set style ranges
// * on the label.
// * </p>
// * 
// * @since 3.4
// */
//public abstract class StyledCellLabelProvider extends OwnerDrawLabelProvider {
//
//
//
//	/**
//	 * Style constant for indicating that the styled colors are to be applied
//	 * even it the viewer's item is selected. Default is not to apply colors.
//	 */
//	public static final int COLORS_ON_SELECTION = 1 << 0;
//
//	/**
//	 * Style constant for indicating to draw the focus if requested by the owner
//	 * draw event. Default is to draw the focus.
//	 */
//	public static final int NO_FOCUS = 1 << 1;
//	
//	/**
//	 * Private constant to indicate if owner draw is enabled for the
//	 * label provider's column.
//	 */
//	private static final int OWNER_DRAW_ENABLED = 1 << 4;
//
//	private int style;
//
//	// reused text layout
//	private TextLayout cachedTextLayout;
//	
//	private ColumnViewer viewer;
//	private ViewerColumn column;
//	
//	private Widget itemOfLastMeasure;
//	private Object elementOfLastMeasure;
//	private int deltaOfLastMeasure;
//
//	/**
//	 * Creates a new StyledCellLabelProvider. By default, owner draw is enabled, focus is drawn and no
//	 * colors are painted on selected elements.
//	 */
//	public StyledCellLabelProvider() {
//		this(0);
//	}
//
//	/**
//	 * Creates a new StyledCellLabelProvider. By default, owner draw is enabled.
//	 * 
//	 * @param style
//	 *            the style bits
//	 * @see StyledCellLabelProvider#COLORS_ON_SELECTION
//	 * @see StyledCellLabelProvider#NO_FOCUS
//	 */
//	public StyledCellLabelProvider(int style) {
//		this.style = style & (COLORS_ON_SELECTION | NO_FOCUS)
//							| OWNER_DRAW_ENABLED;
//	}
//	
//	/**
//	 * Returns <code>true</code> is the owner draw rendering is enabled for this label provider.
//	 * By default owner draw rendering is enabled. If owner draw rendering is disabled, rending is
//	 * done by the viewer and no styled ranges (see {@link ViewerCell#getStyleRanges()})
//	 * are drawn.
//	 * 
//	 * @return <code>true</code> is the rendering of styles is enabled.
//	 */
//	public boolean isOwnerDrawEnabled() {
//		return (this.style & OWNER_DRAW_ENABLED) != 0;
//	}
//	
//	/**
//	 * Specifies whether owner draw rendering is enabled for this label
//	 * provider. By default owner draw rendering is enabled. If owner draw
//	 * rendering is disabled, rendering is done by the viewer and no styled
//	 * ranges (see {@link ViewerCell#getStyleRanges()}) are drawn.
//	 * It is the caller's responsibility to also call
//	 * {@link StructuredViewer#refresh()} or similar methods to update the
//	 * underlying widget.
//	 * 
//	 * @param enabled
//	 *            specifies if owner draw rendering is enabled
//	 */
//	public void setOwnerDrawEnabled(boolean enabled) {
//		boolean isEnabled= isOwnerDrawEnabled();
//		if (isEnabled != enabled) {
//			if (enabled) {
//				this.style |= OWNER_DRAW_ENABLED;
//			} else {
//				this.style &= ~OWNER_DRAW_ENABLED;
//			}
//			if (this.viewer != null) {
//				setOwnerDrawEnabled(this.viewer, this.column, enabled);
//			}
//		}
//	}
//	
//	/**
//	 * Returns the viewer on which this label provider is installed on or <code>null</code> if the
//	 * label provider is not installed.
//	 * 
//	 * @return the viewer on which this label provider is installed on or <code>null</code> if the
//	 * label provider is not installed.
//	 */
//	protected final ColumnViewer getViewer() {
//		return this.viewer;
//	}
//	
//	/**
//	 * Returns the column on which this label provider is installed on or <code>null</code> if the
//	 * label provider is not installed.
//	 * 
//	 * @return the column on which this label provider is installed on or <code>null</code> if the
//	 * label provider is not installed.
//	 */
//	protected final ViewerColumn getColumn() {
//		return this.column;
//	}
//		
//	/* (non-Javadoc)
//	 * @see org.eclipse.jface.viewers.OwnerDrawLabelProvider#initialize(org.eclipse.jface.viewers.ColumnViewer, org.eclipse.jface.viewers.ViewerColumn)
//	 */
//	public void initialize(ColumnViewer viewer, ViewerColumn column) {
//		Assert.isTrue(this.viewer == null && this.column == null, "Label provider instance already in use"); //$NON-NLS-1$
//		
//		this.viewer= viewer;
//		this.column= column;
//		super.initialize(viewer, column, isOwnerDrawEnabled());
//	}
//	
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see org.eclipse.jface.viewers.BaseLabelProvider#dispose()
//	 */
//	public void dispose() {
//		if (this.cachedTextLayout != null) {
//			cachedTextLayout.dispose();
//			cachedTextLayout = null;
//		}
//	
//		this.viewer= null;
//		this.column= null;
//		this.itemOfLastMeasure = null;
//		this.elementOfLastMeasure = null;
//		
//		super.dispose();
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see org.eclipse.jface.viewers.OwnerDrawLabelProvider#update(org.eclipse.jface.viewers.ViewerCell)
//	 */
//	public void update(ViewerCell cell) {
//		// clients must override and configure the cell and call super
//		super.update(cell); // calls 'repaint' to trigger the paint listener
//	}
//
//	private TextLayout getSharedTextLayout(Display display) {
//		if (cachedTextLayout == null) {
//			int orientation = viewer.getControl().getStyle() & (SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT);
//			cachedTextLayout = new TextLayout(display);
//			cachedTextLayout.setOrientation(orientation);
//		}
//		return cachedTextLayout;
//	}
//
//	private boolean useColors(Event event) {
//		return (event.detail & SWT.SELECTED) == 0
//				|| (this.style & COLORS_ON_SELECTION) != 0;
//	}
//
//	private boolean drawFocus(Event event) {
//		return (event.detail & SWT.FOCUSED) != 0
//				&& (this.style & NO_FOCUS) == 0;
//	}
//	
//	/**
//	 * Prepares the given style range before it is applied to the label. This method makes sure that
//	 * no colors are drawn when the element is selected.
//	 * The current version of the {@link StyledCellLabelProvider} will also ignore all font settings on the
//	 * style range. Clients can override.
//	 * 
//	 * @param styleRange
//	 *               the style range to prepare. the style range element must not be modified
//	 * @param applyColors
//	 *               specifies if colors should be applied.
//	 * @return
//	 *               returns the style range to use on the label
//	 */
//	protected StyleRange prepareStyleRange(StyleRange styleRange, boolean applyColors) {
//		// if no colors apply or font is set, create a clone and clear the
//		// colors and font
//		if (!applyColors && (styleRange.foreground != null || styleRange.background != null)) {
//			styleRange = (StyleRange) styleRange.clone();
//			if (!applyColors) {
//				styleRange.foreground = null;
//				styleRange.background = null;
//			}
//		}
//		return styleRange;
//	}
//
//	private ViewerCell getViewerCell(Event event, Object element) {
//		ViewerRow row= viewer.getViewerRowFromItem(event.item);
//		return new ViewerCell(row, event.index, element);
//	}
//	
//	/**
//	 * Handle the erase event. The default implementation does nothing to ensure
//	 * keep native selection highlighting working.
//	 * 
//	 * @param event
//	 *            the erase event
//	 * @param element
//	 *            the model object
//	 * @see SWT#EraseItem
//	 */
//	protected void erase(Event event, Object element) {
//		// use native erase
//		if (isOwnerDrawEnabled()) {
//			// info has been set by 'update': announce that we paint ourselves
//			event.detail &= ~SWT.FOREGROUND;
//		}
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see org.eclipse.jface.viewers.OwnerDrawLabelProvider#measure(org.eclipse.swt.widgets.Event,
//	 *      java.lang.Object)
//	 */
//	protected void measure(Event event, Object element) {
//		if (!isOwnerDrawEnabled())
//			return;
//		
//		ViewerCell cell= getViewerCell(event, element);
//		boolean applyColors = useColors(event); // returns false because of bug 228376
//		
//		TextLayout layout = getSharedTextLayout(event.display);
//		
//		int textWidthDelta = deltaOfLastMeasure = updateTextLayout(layout, cell, applyColors);
//		/* remove-begin if bug 228695 fixed */
//		itemOfLastMeasure = event.item;
//		elementOfLastMeasure = event.item.getData();
//		/* remove-end if bug 228695 fixed */
//
//		event.width += textWidthDelta;
//	}
//
//	/**
//	 * @param layout
//	 * @param cell
//	 * @param applyColors
//	 * @return the text width delta (0 if the text layout contains no other font)
//	 */
//	private int updateTextLayout(TextLayout layout, ViewerCell cell,
//			boolean applyColors) {
//		layout.setText(""); //$NON-NLS-1$  //make sure all previous ranges are cleared (see bug 226090)
//		
//		layout.setText(cell.getText());
//		layout.setFont(cell.getFont()); // set also if null to clear previous usages
//		
//		int originalTextWidth = layout.getBounds().width; // text width without any styles
//		boolean containsOtherFont= false;
//		
//		StyleRange[] styleRanges = cell.getStyleRanges();
//		if (styleRanges != null) { // user didn't fill styled ranges
//			for (int i = 0; i < styleRanges.length; i++) {
//				StyleRange curr = prepareStyleRange(styleRanges[i], applyColors);
//				layout.setStyle(curr, curr.start, curr.start + curr.length - 1);
//				if (curr.font != null) {
//					containsOtherFont= true;
//				}
//			}
//		}
//		int textWidthDelta = 0;
//		if (containsOtherFont) {
//			textWidthDelta = layout.getBounds().width - originalTextWidth;
//		}
//		return textWidthDelta;
//	}
//	
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see org.eclipse.jface.viewers.OwnerDrawLabelProvider#paint(org.eclipse.swt.widgets.Event,
//	 *      java.lang.Object)
//	 */
//	protected void paint(Event event, Object element) {
//		if (!isOwnerDrawEnabled())
//			return;
//		
//		ViewerCell cell= getViewerCell(event, element);
//
//		boolean applyColors= useColors(event);
//		
//		GC gc = event.gc;
//		// remember colors to restore the GC later
//		Color oldForeground = gc.getForeground();
//		Color oldBackground = gc.getBackground();
//		
//		if (applyColors) {
//			Color foreground= cell.getForeground();
//			if (foreground != null) {
//				gc.setForeground(foreground);
//			}
//			
//			Color background= cell.getBackground();
//			if (background != null) {
//				gc.setBackground(background);
//			}
//		}
//		Image image = cell.getImage();
//		if (image != null) {
//			Rectangle imageBounds = cell.getImageBounds();
//			if (imageBounds != null) {
//				Rectangle bounds = image.getBounds();
//	
//				// center the image in the given space
//				int x = imageBounds.x
//						+ Math.max(0, (imageBounds.width - bounds.width) / 2);
//				int y = imageBounds.y
//						+ Math.max(0, (imageBounds.height - bounds.height) / 2);
//				gc.drawImage(image, x, y);
//			}
//		}
//
//		Rectangle textBounds = cell.getTextBounds();
//		if (textBounds != null) {
//			TextLayout textLayout= getSharedTextLayout(event.display);
//
//			/* remove-begin if bug 228695 fixed */
//			if (event.item != itemOfLastMeasure || event.item.getData() != elementOfLastMeasure) {
//				// fLayout has not been configured in 'measure()'
//				deltaOfLastMeasure = updateTextLayout(textLayout, cell, applyColors);
//				itemOfLastMeasure = event.item;
//				elementOfLastMeasure = event.item.getData();
//			}
//			/* remove-end if bug 228695 fixed */
//			
//			/* remove-begin if bug 228376 fixed */
//			if (!applyColors) {
//				// need to remove colors for selected elements: measure doesn't provide that information, see bug 228376
//				StyleRange[] styleRanges= cell.getStyleRanges();
//				if (styleRanges != null) {
//					for (int i= 0; i < styleRanges.length; i++) {
//						StyleRange curr = prepareStyleRange(styleRanges[i], applyColors);
//						textLayout.setStyle(curr, curr.start, curr.start + curr.length - 1);
//					}
//				}
//			}
//			/* remove-end if bug 228376 fixed */
//			
//			Rectangle layoutBounds = textLayout.getBounds();
//	
//			int x = textBounds.x;
//			int y = textBounds.y
//					+ Math.max(0, (textBounds.height - layoutBounds.height) / 2);
//	
//			textLayout.draw(gc, x, y);
//		}
//
//		if (drawFocus(event)) {
//			Rectangle focusBounds = cell.getViewerRow().getBounds();
//			gc.drawFocus(focusBounds.x, focusBounds.y, focusBounds.width + deltaOfLastMeasure,
//					focusBounds.height);
//		}
//		
//		if (applyColors) {
//			gc.setForeground(oldForeground);
//			gc.setBackground(oldBackground);
//		}
//	}
//
//	/**
//	 * Applies decoration styles to the decorated string and adds the styles of the previously
//	 * undecorated string.
//	 * <p>
//	 * If the <code>decoratedString</code> contains the <code>styledString</code>, then the result
//	 * keeps the styles of the <code>styledString</code> and styles the decorations with the
//	 * <code>decorationStyler</code>. Otherwise, the decorated string is returned without any
//	 * styles.
//	 * 
//	 * @param decoratedString the decorated string
//	 * @param decorationStyler the styler to use for the decoration or <code>null</code> for no
//	 *            styles
//	 * @param styledString the original styled string
//	 * 
//	 * @return the styled decorated string (can be the given <code>styledString</code>)
//	 * @since 3.5
//	 */
//	public static StyledString styleDecoratedString(String decoratedString, Styler decorationStyler, StyledString styledString) {
//		String label= styledString.getString();
//		int originalStart= decoratedString.indexOf(label);
//		if (originalStart == -1) {
//			return new StyledString(decoratedString); // the decorator did something wild
//		}
//
//		if (decoratedString.length() == label.length())
//			return styledString;
//
//		if (originalStart > 0) {
//			StyledString newString= new StyledString(decoratedString.substring(0, originalStart), decorationStyler);
//			newString.append(styledString);
//			styledString= newString;
//		}
//		if (decoratedString.length() > originalStart + label.length()) { // decorator appended something
//			return styledString.append(decoratedString.substring(originalStart + label.length()), decorationStyler);
//		}
//		return styledString; // no change
//	}
//
//}
