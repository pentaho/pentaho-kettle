/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
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
import org.eclipse.swt.widgets.Control;

/**
 * This class provides default implementations to display a drag source
 * effect during a drag and drop operation. The current implementation
 * does not provide any visual feedback. 
 * 
 * <p>The drag source effect has the same API as the 
 * <code>DragSourceAdapter</code> so that it can provide custom visual 
 * feedback when a <code>DragSourceEvent</code> occurs. 
 * </p>
 * 
 * <p>Classes that wish to provide their own drag source effect such as
 * displaying a default source image during a drag can extend the <code>DragSourceEffect</code> 
 * class, override the <code>DragSourceAdapter.dragStart</code> method and set 
 * the field <code>DragSourceEvent.image</code> with their own image.
 * The image should be disposed when <code>DragSourceAdapter.dragFinished</code> is called.
 * </p> 
 *
 * @see DragSourceAdapter
 * @see DragSourceEvent
 * @since 1.3
 */
public class DragSourceEffect extends DragSourceAdapter {
	Control control = null;

	/**
	 * Creates a new <code>DragSourceEffect</code> to handle drag effect from the specified <code>Control</code>.
	 *
	 * @param control the <code>Control</code> that the user clicks on to initiate the drag
	 * 
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the control is null</li>
	 * </ul>
	 */
	public DragSourceEffect(Control control) {
		if (control == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
		this.control = control;
	}

	/**
	 * Returns the Control which is registered for this DragSourceEffect.  This is the control that the 
	 * user clicks in to initiate dragging.
	 *
	 * @return the Control which is registered for this DragSourceEffect
	 */
	public Control getControl() {
		return control;
	}
}
