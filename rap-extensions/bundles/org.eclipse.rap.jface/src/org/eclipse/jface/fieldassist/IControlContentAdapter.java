/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.fieldassist;

import java.io.Serializable;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;

/**
 * This interface is used to set and retrieve text content from an arbitrary
 * control. Clients are expected to implement this interface when defining a
 * {@link ContentProposalAdapter}, in order to specify how to retrieve and set
 * the contents of the control being adapted.
 * 
 * @since 1.0
 */
public interface IControlContentAdapter extends Serializable {
	/**
	 * Set the contents of the specified control to the specified text. Must not
	 * be <code>null</code>.
	 * 
	 * @param control
	 *            the control whose contents are to be set (replaced).
	 * @param contents
	 *            the String specifying the new control content.
	 * @param cursorPosition
	 *            the zero-based index representing the desired cursor position
	 *            in the control's contents after the contents are set.
	 */
	public void setControlContents(Control control, String contents,
			int cursorPosition);

	/**
	 * Insert the specified contents into the control's current contents. Must
	 * not be <code>null</code>.
	 * 
	 * @param control
	 *            the control whose contents are to be altered.
	 * @param contents
	 *            the String to be inserted into the control contents.
	 * @param cursorPosition
	 *            the zero-based index representing the desired cursor position
	 *            within the inserted contents after the insertion is made.
	 */
	public void insertControlContents(Control control, String contents,
			int cursorPosition);

	/**
	 * Get the text contents of the control.
	 * 
	 * @param control
	 *            the control whose contents are to be retrieved.
	 * @return the String contents of the control.
	 */
	public String getControlContents(Control control);

	/**
	 * Get the current cursor position in the control. The position is specified
	 * as a zero-based index into the string. Valid ranges are from 0 to N,
	 * where N is the size of the contents string. A value of N indicates that
	 * the cursor is at the end of the contents.
	 * 
	 * @param control
	 *            the control whose position is to be retrieved.
	 * @return the zero-based index representing the cursor position in the
	 *         control's contents.
	 */
	public int getCursorPosition(Control control);

	/**
	 * Get the bounds (in pixels) of the insertion point for the control
	 * content. This is a rectangle, in coordinates relative to the control,
	 * where the insertion point is displayed. If the implementer does not have
	 * an insertion point, or cannot determine the location of the insertion
	 * point, it is appropriate to return the bounds of the entire control. This
	 * value may be used to position a content proposal popup.
	 * 
	 * @param control
	 *            the control whose offset is to be retrieved.
	 * @return the pixel width representing the distance between the edge of the
	 *         control and the insertion point.
	 */
	public Rectangle getInsertionBounds(Control control);

	/**
	 * Set the current cursor position in the control. The position is specified
	 * as a zero-based index into the string. Valid ranges are from 0 to N,
	 * where N is the size of the contents string. A value of N indicates that
	 * the cursor is at the end of the contents.
	 * 
	 * @param control
	 *            the control whose cursor position is to be set.
	 * @param index
	 *            the zero-based index representing the cursor position in the
	 *            control's contents.
	 */
	public void setCursorPosition(Control control, int index);
}
