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
package org.eclipse.swt.dnd;

 
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.SWTException;

/**
 * Class DND contains all the constants used in defining a 
 * DragSource or a DropTarget.
 *
 * @since 1.3
 */
public class DND {
	
	/**
	 * The transfer mechanism for data that is being cut
	 * and then pasted or copied and then pasted (value is 1).
	 */
//  * @see Clipboard
	public final static int CLIPBOARD = 1 << 0;
	
	/**
	 * The transfer mechanism for clients that use the selection 
	 * mechanism (value is 2).
	 */
//	* @see Clipboard
	public final static int SELECTION_CLIPBOARD = 1 << 1;

	/**
	 * Drag and Drop Operation: no drag/drop operation performed
	 * (value is 0).
	 */
	public final static int DROP_NONE = 0;
	
	/**
	 * Drag and Drop Operation: a copy of the data in the drag source is 
	 * added to the drop target (value is 1 &lt;&lt; 0).
	 */
	public final static int DROP_COPY = 1 << 0;
	
	/**
	 * Drag and Drop Operation: a copy of the data is added to the drop target and 
	 * the original data is removed from the drag source (value is 1 &lt;&lt; 1).
	 */
	public final static int DROP_MOVE = 1 << 1;
	
	/**
	 * Drag and Drop Operation: the drop target makes a link to the data in 
	 * the drag source (value is 1 &lt;&lt; 2).
	 */
	public final static int DROP_LINK = 1 << 2;
	
	/**
	 * Drag and Drop Operation: the drop target moves the data and the drag source removes 
	 * any references to the data and updates its display.  This is not available on all platforms
	 * and is only used when a non-SWT application is the drop target.  In this case, the SWT 
	 * drag source is informed in the dragFinished event that the drop target has moved the data.
	 * (value is 1 &lt;&lt; 3).
	 * 
	 * @see DragSourceListener#dragFinished
	 */
	public final static int DROP_TARGET_MOVE = 1 << 3;
	
	/**
	 * Drag and Drop Operation: During a dragEnter event or a dragOperationChanged, if no modifier keys
	 * are pressed, the operation is set to DROP_DEFAULT.  The application can choose what the default 
	 * operation should be by setting a new value in the operation field.  If no value is choosen, the
	 * default operation for the platform will be selected (value is 1 &lt;&lt; 4).
	 * 
	 * @see DropTargetListener#dragEnter
	 * @see DropTargetListener#dragOperationChanged
	 */
	public final static int DROP_DEFAULT = 1 << 4;
	
	/**
	 * DragSource Event: the drop has successfully completed or has been terminated (such as hitting 
	 * the ESC key); perform cleanup such as removing data on a move operation (value is 2000).
	 */
	public static final int DragEnd		= 2000;
	
	/**
	 * DragSource Event: the data to be dropped is required from the drag source (value is 2001).
	 */
	public static final int DragSetData = 2001;
	
	/**
	 * DropTarget Event: the cursor has entered the drop target boundaries (value is 2002).
	 */
	public static final int DragEnter	= 2002;
	
	/**
	 * DropTarget Event: the cursor has left the drop target boundaries OR the drop
	 * operation has been cancelled (such as by hitting ECS) OR the drop is about to 
	 * happen (user has released the mouse button over this target) (value is 2003).
	 */
	public static final int DragLeave	= 2003;
	
	/**
	 * DropTarget Event: the cursor is over the drop target (value is 2004).
	 */
	public static final int	DragOver	= 2004;
	
	/**
	 * DropTarget Event: the operation being performed has changed usually due to the user 
	 * changing the selected modifier keys while dragging (value is 2005).
	 */
	public static final int DragOperationChanged = 2005;
	
	/**
	 * DropTarget Event: the data has been dropped (value is 2006).
	 */
	public static final int	Drop = 2006;
	
	/**
	 * DropTarget Event: the drop target is given a last chance to modify the drop (value is 2007).
	 */
	public static final int	DropAccept	= 2007;
	
	/**
	 * DragSource Event: a drag is about to begin (value is 2008).
	 */
	public static final int	DragStart = 2008;

	/**
	 * DropTarget drag under effect: No effect is shown (value is 0).
	 */
	public static final int FEEDBACK_NONE = 0;
	
	/**
	 * DropTarget drag under effect: The item under the cursor is selected; applies to tables
	 * and trees (value is 1).
	 */
	public static final int FEEDBACK_SELECT = 1;
	
	/**
	 * DropTarget drag under effect: An insertion mark is shown before the item under the cursor; applies to 
	 * tables and trees (value is 2).
	 */
	public static final int FEEDBACK_INSERT_BEFORE = 2;
	
	/**
	 * DropTarget drag under effect:An insertion mark is shown after the item under the cursor; applies to
	 * tables and trees (value is 4).
	 */	
	public static final int FEEDBACK_INSERT_AFTER = 4;
	
	/**
	 * DropTarget drag under effect: The widget is scrolled up or down to allow the user to drop on items that 
	 * are not currently visible;  applies to tables and trees (value is 8).
	 */	
	public static final int FEEDBACK_SCROLL = 8;
	
	/**
	 * DropTarget drag under effect: The item currently under the cursor is expanded to allow the user to 
	 * select a drop target from a sub item; applies to trees (value is 16).
	 */	
	public static final int FEEDBACK_EXPAND = 16;

	/**
	 * Error code: drag source can not be initialized (value is 2000).
	 */
	public static final int ERROR_CANNOT_INIT_DRAG = 2000;
	
	/**
	 * Error code: drop target cannot be initialized (value is 2001).
	 */
	public static final int ERROR_CANNOT_INIT_DROP = 2001;
	
	/**
	 * Error code: Data can not be set on system clipboard (value is 2002).
	 */
	public static final int ERROR_CANNOT_SET_CLIPBOARD = 2002;
	
	/**
	 * Error code: Data does not have correct format for type (value is 2003).
	 */
	public static final int ERROR_INVALID_DATA = 2003;
	
	/**
	 * DropTarget Key: The string constant for looking up the drop target 
	 * for a control using <code>getData(String)</code>. When a drop target 
	 * is created for a control, it is stored as a property in the control 
	 * using <code>setData(String, Object)</code>.
	 */
	public static final String DROP_TARGET_KEY = "DropTarget"; //$NON-NLS-1$
	
	/**
	 * DragSource Key: The string constant for looking up the drag source 
	 * for a control using <code>getData(String)</code>. When a drag source 
	 * is created for a control, it is stored as a property in the control 
	 * using <code>setData(String, Object)</code>.
	 */
	public static final String DRAG_SOURCE_KEY = "DragSource"; //$NON-NLS-1$

	static final String INIT_DRAG_MESSAGE = "Cannot initialize Drag"; //$NON-NLS-1$
	static final String INIT_DROP_MESSAGE = "Cannot initialize Drop"; //$NON-NLS-1$
	static final String CANNOT_SET_CLIPBOARD_MESSAGE = "Cannot set data in clipboard"; //$NON-NLS-1$
	static final String INVALID_DATA_MESSAGE = "Data does not have correct format for type"; //$NON-NLS-1$

/**
 * Throws an appropriate exception based on the passed in error code.
 *
 * @param code the DND error code
 */
public static void error (int code) {
	error (code, 0);
}

/**
 * Throws an appropriate exception based on the passed in error code.
 * The <code>hresult</code> argument should be either 0, or the
 * platform specific error code.
 * <p>
 * In DND, errors are reported by throwing one of three exceptions:
 * <dl>
 * <dd>java.lang.IllegalArgumentException</dd>
 * <dt>thrown whenever one of the API methods is invoked with an illegal argument</dt>
 * <dd>org.eclipse.swt.SWTException (extends java.lang.RuntimeException)</dd>
 * <dt>thrown whenever a recoverable error happens internally in SWT</dt>
 * <dd>org.eclipse.swt.SWTError (extends java.lang.Error)</dd>
 * <dt>thrown whenever a <b>non-recoverable</b> error happens internally in SWT</dt>
 * </dl>
 * This method provides the logic which maps between error codes
 * and one of the above exceptions.
 * </p>
 *
 * @param code the DND error code.
 * @param hresult the platform specific error code.
 *
 * @see SWTError
 * @see SWTException
 * @see IllegalArgumentException
 */
public static void error (int code, int hresult) {		
	switch (code) {		
		/* OS Failure/Limit (fatal, may occur only on some platforms) */
		case DND.ERROR_CANNOT_INIT_DRAG:{
			String msg = DND.INIT_DRAG_MESSAGE;
			if (hresult != 0) msg += " result = "+hresult; //$NON-NLS-1$
			throw new SWTError (code, msg);
		}
		case DND.ERROR_CANNOT_INIT_DROP:{
			String msg = DND.INIT_DROP_MESSAGE;
			if (hresult != 0) msg += " result = "+hresult; //$NON-NLS-1$
			throw new SWTError (code, msg);
		}
		case DND.ERROR_CANNOT_SET_CLIPBOARD:{
			String msg = DND.CANNOT_SET_CLIPBOARD_MESSAGE;
			if (hresult != 0) msg += " result = "+hresult; //$NON-NLS-1$
			throw new SWTError (code, msg);
		}
		case DND.ERROR_INVALID_DATA:{
			String msg = DND.INVALID_DATA_MESSAGE;
			if (hresult != 0) msg += " result = "+hresult; //$NON-NLS-1$
			throw new SWTException (code, msg);
		}
	}
			
	/* Unknown/Undefined Error */
	SWT.error(code);
}

}
