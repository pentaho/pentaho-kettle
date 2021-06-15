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
 * sending AccessibleValue messages to an accessible object.
 *
 * @see AccessibleValueListener
 * @see AccessibleValueAdapter
 *
 * @since 1.4
 */
public class AccessibleValueEvent extends SWTEventObject {

	public Number value;

	static final long serialVersionUID = -465979079760740668L;

/**
 * Constructs a new instance of this class.
 *
 * @param source the object that fired the event
 */
public AccessibleValueEvent(Object source) {
	super(source);
}

/**
 * Returns a string containing a concise, human-readable
 * description of the receiver.
 *
 * @return a string representation of the event
 */
public String toString () {
	return "AccessibleValueEvent {" //$NON-NLS-1$
		+ "value=" + value   //$NON-NLS-1$
		+ "}";  //$NON-NLS-1$
}
}
