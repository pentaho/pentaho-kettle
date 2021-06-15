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
 * sending AccessibleAttribute messages to an accessible object.
 *
 * @see AccessibleAttributeListener
 * @see AccessibleAttributeAdapter
 *
 * @since 1.4
 */
public class AccessibleAttributeEvent extends SWTEventObject {

	public int topMargin;
	public int bottomMargin;
	public int leftMargin;
	public int rightMargin;
	public int[] tabStops;
	public boolean justify;
	public int alignment;
	public int indent;
	public String [] attributes;

	static final long serialVersionUID = 2237016128901566049L;

/**
 * Constructs a new instance of this class.
 *
 * @param source the object that fired the event
 */
public AccessibleAttributeEvent(Object source) {
	super(source);
}

/**
 * Returns a string containing a concise, human-readable
 * description of the receiver.
 *
 * @return a string representation of the event
 */
public String toString () {
	return "AccessibleAttributeEvent {" //$NON-NLS-1$
		+ " topMargin=" + topMargin   //$NON-NLS-1$
		+ " bottomMargin=" + bottomMargin   //$NON-NLS-1$
		+ " leftMargin=" + leftMargin   //$NON-NLS-1$
		+ " rightMargin=" + rightMargin   //$NON-NLS-1$
		+ " tabStops=" + tabStops   //$NON-NLS-1$
		+ " justify=" + justify   //$NON-NLS-1$
		+ " alignment=" + alignment   //$NON-NLS-1$
		+ " indent=" + indent   //$NON-NLS-1$
		+ "}";  //$NON-NLS-1$
}
}
