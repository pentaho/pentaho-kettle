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
 * sending AccessibleHyperlink messages to an accessible object.
 *
 * @see AccessibleHyperlinkListener
 * @see AccessibleHyperlinkAdapter
 *
 * @since 1.4
 */
public class AccessibleHyperlinkEvent extends SWTEventObject {

	public Accessible accessible;

	/**
	 * The value of this field must be set in the accessible hyperlink listener method
	 * before returning. What to set it to depends on the listener method called.
	 */
	public String result;

	public int index;

	static final long serialVersionUID = 6253098373844074544L;

/**
 * Constructs a new instance of this class.
 *
 * @param source the object that fired the event
 */
public AccessibleHyperlinkEvent(Object source) {
	super(source);
}

/**
 * Returns a string containing a concise, human-readable
 * description of the receiver.
 *
 * @return a string representation of the event
 */
public String toString () {
	return "AccessibleHyperlinkEvent {" //$NON-NLS-1$
		+ "accessible=" + accessible   //$NON-NLS-1$
		+ " string=" + result   //$NON-NLS-1$
		+ " index=" + index   //$NON-NLS-1$
		+ "}";  //$NON-NLS-1$
}
}
