/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.dialogs;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;

/**
 * ControlAnimator provides a simple implementation to display or hide a control
 * at the bottom of the parent composite. Other animations will be written as
 * subclasses of this class. <p>
 * Instances of this class can be created using an AnimatorFactory.
 * 
 * @since 1.0
 */


public class ControlAnimator {
	/** the control that will be displayed or hidden */
	protected Control control;	
	
	/**
	 * Constructs a new ControlAnimator instance and passes along the
	 * control that will be displayed or hidden.
	 * 
	 * @param control the control that will be displayed or hidden.
	 */
	public ControlAnimator(Control control) {
		this.control = control;
	}

	/**
	 * Displays or hides a control at the bottom of the parent composite
	 * and makes use of the control's SWT visible flag.<p>
	 * Subclasses should override this method.</p>
	 * 
	 * @param visible <code>true</code> if the control should be shown, 
	 * 		  and <code>false</code> otherwise.
	 */
	public void setVisible(boolean visible){
		// Using the SWT visible flag to determine if the control has
		// already been displayed or hidden. Return if already displayed
		// and visible is true, or if already hidden and visible is false.
		if (!(control.isVisible() ^ visible))
			return;
		control.setVisible(visible);
		Rectangle parentBounds = control.getParent().getBounds();
		int bottom = parentBounds.height;		
		final int endY = visible ? bottom - control.getBounds().height
				: bottom;
		Point loc = control.getLocation();
		control.setLocation(loc.x,endY);
	}
	
}
