/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.accessibility;

import org.eclipse.swt.internal.SWTEventObject;

/**
 * Instances of this class are sent as a result of accessibility clients
 * sending AccessibleTableCell messages to an accessible object.
 *
 * @see AccessibleTableCellListener
 * @see AccessibleTableCellAdapter
 *
 * @since 1.4
 */
public class AccessibleTableCellEvent extends SWTEventObject {

	public Accessible accessible;
	public Accessible[] accessibles;
	public boolean isSelected;
	public int count;
	public int index;

	static final long serialVersionUID = 7231059449172889781L;

/**
 * Constructs a new instance of this class.
 *
 * @param source the object that fired the event
 */
public AccessibleTableCellEvent(Object source) {
	super(source);
}

/**
 * Returns a string containing a concise, human-readable
 * description of the receiver.
 *
 * @return a string representation of the event
 */
public String toString () {
	return "AccessibleTableCellEvent {" //$NON-NLS-1$
		+ " accessibles=" + accessibles   //$NON-NLS-1$
		+ " isSelected=" + isSelected   //$NON-NLS-1$
		+ " count=" + count   //$NON-NLS-1$
		+ " index=" + index   //$NON-NLS-1$
		+ "}";  //$NON-NLS-1$
}
}
