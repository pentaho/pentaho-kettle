/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.dnd;

import org.eclipse.swt.internal.SWTEventListener;

/**
 * The <code>DropTargetListener</code> class provides event notification to the application 
 * for DropTarget events.
 *
 * <p>As the user moves the cursor into, over and out of a Control that has been designated 
 * as a DropTarget, events indicate what operation can be performed and what data can be 
 * transferred if a drop where to occur at that point.
 * The application can respond to these events and change the type of data that will 
 * be dropped by modifying event.currentDataType, or change the operation that will be performed 
 * by modifying the event.detail field or stop any drop from happening on the current target
 * by setting the event.detail field to DND_DROP_NONE.</p>
 *
 * <p>When the user causes a drop to happen by releasing the mouse over a valid drop target, 
 * the application has one last chance to change the data type of the drop through the 
 * DropAccept event.  If the drop is still allowed, the DropAccept event is immediately 
 * followed by the Drop event.  In the Drop event, the application can still change the
 * operation that is performed but the data type is fixed.</p>
 * 
 * @see DropTargetEvent
 * @since 1.3
 */
public interface DropTargetListener extends SWTEventListener {
	
/**
 * The cursor has entered the drop target boundaries.
 *
 * <p>The following fields in the DropTargetEvent apply:
 * <ul>
 * <li>(in)widget
 * <li>(in)time
 * <li>(in)x
 * <li>(in)y
 * <li>(in)dataTypes
 * <li>(in,out)currentDataType
 * <li>(in)operations
 * <li>(in,out)detail
 * <li>(in,out)feedback
 * </ul></p>
 *
 * <p>The <code>operations</code> value is determined by the modifier keys pressed by the user.  
 * If no keys are pressed the <code>event.detail</code> field is set to DND.DROP_DEFAULT.  
 * If the application does not set the <code>event.detail</code> to something other 
 * than <code>DND.DROP_DEFAULT</code> the operation will be set to the platform defined standard 
 * default.</p>
 * 
 * <p>The <code>currentDataType</code> is determined by the first transfer agent specified in 
 * setTransfer() that matches a data type provided by the drag source.</p>
 * 
 * <p>It is possible to get a DragEnter event when the drag source does not provide any matching data.
 * In this case, the default operation is DND.DROP_NONE and the currentDataType is null.</p>
 * 
 * <p>The application can change the operation that will be performed by modifying the 
 * <code>detail</code> field but the choice must be one of the values in the <code>operations</code> 
 * field or DND.DROP_NONE.</p>
 * 
 * <p>The application can also change the type of data being requested by 
 * modifying the <code>currentDataTypes</code> field  but the value must be one of the values 
 * in the <code>dataTypes</code> list.</p>
 *
 * @param event  the information associated with the drag enter event
 * 
 * @see DropTargetEvent
 */
public void dragEnter(DropTargetEvent event);

/**
 * The cursor has left the drop target boundaries OR the drop has been cancelled OR the data 
 * is about to be dropped.
 *
 * <p>The following fields in the DropTargetEvent apply:
 * <ul>
 * <li>(in)widget
 * <li>(in)time
 * <li>(in)x
 * <li>(in)y
 * <li>(in)dataTypes
 * <li>(in)currentDataType
 * <li>(in)operations
 * <li>(in)detail
 * </ul></p>
 *
 * @param event  the information associated with the drag leave event
 *
 * @see DropTargetEvent
 */
public void dragLeave(DropTargetEvent event);

/**
 * The operation being performed has changed (usually due to the user changing the selected modifier key(s)
 * while dragging).
 *
 * <p>The following fields in the DropTargetEvent apply:
 * <ul>
 * <li>(in)widget
 * <li>(in)time
 * <li>(in)x
 * <li>(in)y
 * <li>(in)dataTypes
 * <li>(in,out)currentDataType
 * <li>(in)operations
 * <li>(in,out)detail
 * <li>(in,out)feedback
 * </ul></p>
 *
 * <p>The <code>operations</code> value is determined by the modifier keys pressed by the user.  
 * If no keys are pressed the <code>event.detail</code> field is set to DND.DROP_DEFAULT.  
 * If the application does not set the <code>event.detail</code> to something other than 
 * <code>DND.DROP_DEFAULT</code> the operation will be set to the platform defined standard default.</p>
 * 
 * <p>The <code>currentDataType</code> value is determined by the value assigned to 
 * <code>currentDataType</code> in previous dragEnter and dragOver calls.</p>
 * 
 * <p>The application can change the operation that will be performed by modifying the 
 * <code>detail</code> field but the choice must be one of the values in the <code>operations</code> 
 * field.</p>
 * 
 * <p>The application can also change the type of data being requested by modifying 
 * the <code>currentDataTypes</code> field  but the value must be one of the values in the 
 * <code>dataTypes</code> list.</p>
 *
 * @param event  the information associated with the drag operation changed event
 * 
 * @see DropTargetEvent
 */
public void dragOperationChanged(DropTargetEvent event);

/**
 * The cursor is moving over the drop target.
 *
 * <p>The following fields in the DropTargetEvent apply:
 * <ul>
 * <li>(in)widget
 * <li>(in)time
 * <li>(in)x
 * <li>(in)y
 * <li>(in)dataTypes
 * <li>(in,out)currentDataType
 * <li>(in)operations
 * <li>(in,out)detail
 * <li>(in,out)feedback
 * </ul></p>
 *
 * <p>The <code>operations</code> value is determined by the value assigned to 
 * <code>currentDataType</code> in previous dragEnter and dragOver calls.</p>
 * 
 * <p>The <code>currentDataType</code> value is determined by the value assigned to 
 * <code>currentDataType</code> in previous dragEnter and dragOver calls.</p>
 * 
 * <p>The application can change the operation that will be performed by modifying the 
 * <code>detail</code> field but the choice must be one of the values in the <code>operations</code> 
 * field.</p>
 * 
 * <p>The application can also change the type of data being requested by modifying the 
 * <code>currentDataTypes</code> field  but the value must be one of the values in the 
 * <code>dataTypes</code> list.</p>
 * 
 * <p>NOTE: At this point the <code>data</code> field is null.  On some platforms, it is possible 
 * to obtain the data being transferred before the transfer occurs but in most platforms this is 
 * not possible.  On those platforms where the data is available, the application can access the 
 * data as follows:</p>
 * 
 * <pre><code>
 * public void dragOver(DropTargetEvent event) {
 *       TextTransfer textTransfer = TextTransfer.getInstance();
 *       String data = (String)textTransfer.nativeToJava(event.currentDataType);
 *       if (data != null) {
 *           System.out.println("Data to be dropped is (Text)"+data);
 *       }
 * };
 * </code></pre>
 *
 * @param event  the information associated with the drag over event
 * 
 * @see DropTargetEvent
 */
public void dragOver(DropTargetEvent event);

/**
 * The data is being dropped.  The data field contains java format of the data being dropped.  
 * To determine the type of the data object, refer to the documentation for the Transfer subclass 
 * specified in event.currentDataType.
 *
 * <p>The following fields in DropTargetEvent apply:
 * <ul>
 * <li>(in)widget
 * <li>(in)time
 * <li>(in)x 
 * <li>(in)y
 * <li>(in,out)detail
 * <li>(in)currentDataType
 * <li>(in)data
 * </ul></p>
 *
 * <p>The application can refuse to perform the drop operation by setting the detail 
 * field to DND.DROP_NONE.</p>
 *
 * @param event the information associated with the drop event
 * 
 * @see DropTargetEvent
 */
public void drop(DropTargetEvent event);

/**
 * The drop is about to be performed.  
 * The drop target is given a last chance to change the nature of the drop.
 * 
 * <p>The following fields in the DropTargetEvent apply:
 * <ul>
 * <li>(in)widget
 * <li>(in)time
 * <li>(in)x
 * <li>(in)y
 * <li>(in)dataTypes
 * <li>(in,out)currentDataType
 * <li>(in)operations
 * <li>(in,out)detail
 * </ul></p>
 *
 * <p>The application can veto the drop by setting the <code>event.detail</code> field to 
 * <code>DND.DROP_NONE</code>.</p>
 *
 * <p>The application can change the operation that will be performed by modifying the 
 * <code>detail</code> field but the choice must be one of the values in the 
 * <code>operations</code> field.</p>
 * 
 * <p>The application can also change the type of data being requested by modifying the 
 * <code>currentDataTypes</code> field  but the value must be one of the values in the <
 * code>dataTypes</code> list.</p>
 *
 * @param event  the information associated with the drop accept event
 * 
 * @see DropTargetEvent
 */
public void dropAccept(DropTargetEvent event);

}
