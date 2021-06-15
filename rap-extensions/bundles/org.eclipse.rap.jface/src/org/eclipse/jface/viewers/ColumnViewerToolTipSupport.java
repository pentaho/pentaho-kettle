/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *                                                 bugfix in: 195137, 198089
 *     Fredy Dobler <fredy@dobler.net> - bug 159600
 *     Brock Janiczak <brockj@tpg.com.au> - bug 182443
 *******************************************************************************/

package org.eclipse.jface.viewers;

//import org.eclipse.jface.util.Policy;
//import org.eclipse.jface.window.DefaultToolTip;
//import org.eclipse.jface.window.ToolTip;
//import org.eclipse.swt.graphics.Image;
//import org.eclipse.swt.graphics.Point;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.internal.widgets.ICellToolTipProvider;

/**
 * The ColumnViewerTooltipSupport is the class that provides tool tips for
 * ColumnViewers.
 *
 * @since 1.3
 *
 */
// RAP [rh] RAP provides tooltip support for the table and tree widgets
//     In addition RAP cannot reuse the DefaultToolTip and ToolTip as these  
//     classes make heavy use of mouse events 
public class ColumnViewerToolTipSupport 
//  extends DefaultToolTip 
{
// RAP [rh] unused code  
//	private ColumnViewer viewer;
//
//	private static final String VIEWER_CELL_KEY = Policy.JFACE
//			+ "_VIEWER_CELL_KEY"; //$NON-NLS-1$
//
//	private static final int DEFAULT_SHIFT_X = 10;
//
//	private static final int DEFAULT_SHIFT_Y = 0;

// RAP [rh] unnecessary code
//	/**
//	 * Enable ToolTip support for the viewer by creating an instance from this
//	 * class. To get all necessary informations this support class consults the
//	 * {@link CellLabelProvider}.
//	 *
//	 * @param viewer
//	 *            the viewer the support is attached to
//	 * @param style
//	 *            style passed to control tool tip behavior
//	 *
//	 * @param manualActivation
//	 *            <code>true</code> if the activation is done manually using
//	 *            {@link #show(Point)}
//	 */
//	protected ColumnViewerToolTipSupport(ColumnViewer viewer, int style,
//			boolean manualActivation) {
//		super(viewer.getControl(), style, manualActivation);
//		this.viewer = viewer;
//	}

// RAP [rh] RAP uses custom tooltips	  
	/**
	 * Enable ToolTip support for the viewer by creating an instance from this
	 * class. To get all necessary informations this support class consults the
	 * {@link CellLabelProvider}.
	 *
	 * @param viewer
	 *            the viewer the support is attached to
	 */
    public static void enableFor(ColumnViewer viewer) {
//      new ColumnViewerToolTipSupport(viewer, dis, false);
        viewer.getControl().setData( ICellToolTipProvider.ENABLE_CELL_TOOLTIP,
                                     Boolean.TRUE );
    }

// RAP [rh] ToolTip style not supported 
//	/**
//	 * Enable ToolTip support for the viewer by creating an instance from this
//	 * class. To get all necessary informations this support class consults the
//	 * {@link CellLabelProvider}.
//	 *
//	 * @param viewer
//	 *            the viewer the support is attached to
//	 * @param style
//	 *            style passed to control tool tip behavior
//	 *
//	 * @see ToolTip#RECREATE
//	 * @see ToolTip#NO_RECREATE
//	 */
//	public static void enableFor(ColumnViewer viewer, int style) {
//		new ColumnViewerToolTipSupport(viewer, style, false);
//	}

// RAP [rh] unused code	
//	protected Object getToolTipArea(Event event) {
//		return viewer.getCell(new Point(event.x, event.y));
//	}
//
//	/**
//	 * Instead of overwriting this method subclasses should overwrite
//	 * {@link #createViewerToolTipContentArea(Event, ViewerCell, Composite)}
//	 */
//	protected Composite createToolTipContentArea(Event event, Composite parent) {
//		ViewerCell cell = (ViewerCell) getData(VIEWER_CELL_KEY);
//		setData(VIEWER_CELL_KEY, null);
//
//		return createViewerToolTipContentArea(event, cell, parent);
//	}
//
//	/**
//	 * Creates the content area of the tool tip giving access to the cell the
//	 * tip is shown for. Subclasses can overload this method to implement their
//	 * own tool tip design.
//	 *
//	 * <p>
//	 * This method is called from
//	 * {@link #createToolTipContentArea(Event, Composite)} and by default calls
//	 * the {@link DefaultToolTip#createToolTipContentArea(Event, Composite)}.
//	 * </p>
//	 *
//	 * @param event
//	 *            the event that which
//	 * @param cell
//	 *            the cell the tool tip is shown for
//	 * @param parent
//	 *            the parent of the control to create
//	 * @return the control to be displayed in the tool tip area
//	 */
//	protected Composite createViewerToolTipContentArea(Event event,
//			ViewerCell cell, Composite parent) {
//		return super.createToolTipContentArea(event, parent);
//	}
//
//	protected boolean shouldCreateToolTip(Event event) {
//		if (!super.shouldCreateToolTip(event)) {
//			return false;
//		}
//
//		boolean rv = false;
//
//		ViewerRow row = viewer.getViewerRow(new Point(event.x, event.y));
//
//		viewer.getControl().setToolTipText(""); //$NON-NLS-1$
//		Point point = new Point(event.x, event.y);
//
//		if (row != null) {
//			Object element = row.getItem().getData();
//
//			ViewerCell cell = row.getCell(point);
//			ViewerColumn viewPart = viewer.getViewerColumn(cell
//					.getColumnIndex());
//
//			if (viewPart == null) {
//				return false;
//			}
//
//			CellLabelProvider labelProvider = viewPart.getLabelProvider();
//			boolean useNative = labelProvider.useNativeToolTip(element);
//
//			String text = labelProvider.getToolTipText(element);
//			Image img = null;
//
//			if (!useNative) {
//				img = labelProvider.getToolTipImage(element);
//			}
//
//			if (useNative || (text == null && img == null)) {
//				viewer.getControl().setToolTipText(text);
//				rv = false;
//			} else {
//				setPopupDelay(labelProvider.getToolTipDisplayDelayTime(element));
//				setHideDelay(labelProvider.getToolTipTimeDisplayed(element));
//
//				Point shift = labelProvider.getToolTipShift(element);
//
//				if (shift == null) {
//					setShift(new Point(DEFAULT_SHIFT_X, DEFAULT_SHIFT_Y));
//				} else {
//					setShift(new Point(shift.x, shift.y));
//				}
//
//				setData(VIEWER_CELL_KEY, cell);
//
//				setText(text);
//				setImage(img);
//				setStyle(labelProvider.getToolTipStyle(element));
//				setForegroundColor(labelProvider
//						.getToolTipForegroundColor(element));
//				setBackgroundColor(labelProvider
//						.getToolTipBackgroundColor(element));
//				setFont(labelProvider.getToolTipFont(element));
//
//				// Check if at least one of the values is set
//				rv = getText(event) != null || getImage(event) != null;
//			}
//		}
//
//		return rv;
//	}
//
//	protected void afterHideToolTip(Event event) {
//		super.afterHideToolTip(event);
//		// Clear the restored value else this could be a source of a leak
//		setData(VIEWER_CELL_KEY, null);
//		if (event != null && event.widget != viewer.getControl()) {
//			viewer.getControl().setFocus();
//		}
//	}
}
