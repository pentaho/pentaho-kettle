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

//import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.SWTEventObject;

/**
 * Instances of this class are sent as a result of accessibility clients
 * sending AccessibleAttribute or AccessibleEditableText messages to an
 * accessible object.
 *
 * @see AccessibleAttributeListener
 * @see AccessibleAttributeAdapter
 * @see AccessibleEditableTextListener
 * @see AccessibleEditableTextAdapter
 *
 * @since 1.4
 */
public class AccessibleTextAttributeEvent extends SWTEventObject {

	/**
	 * [in] the 0-based text offset for which to return attribute information
	 * 
	 * @see AccessibleAttributeListener#getTextAttributes
	 */
	public int offset;

	/**
	 * [in/out] the starting and ending offsets of the character range
	 * 
	 * @see AccessibleAttributeListener#getTextAttributes
	 * @see AccessibleEditableTextListener#setTextAttributes
	 */
	public int start, end;

//RAP [if] TextStyle is missing
//	/**
//	 * [in/out] the TextStyle of the character range
//	 * 
//	 * @see AccessibleAttributeListener#getTextAttributes
//	 * @see AccessibleEditableTextListener#setTextAttributes
//	 */
//	public TextStyle textStyle;
	
	/**
	 * [in/out] an array of alternating key and value Strings
	 * that represent attributes that do not correspond to TextStyle fields
	 * 
	 * @see AccessibleAttributeListener#getTextAttributes
	 * @see AccessibleEditableTextListener#setTextAttributes
	 */
	public String [] attributes;

	/**
	 * [out] Set this field to {@link ACC#OK} if the operation
	 * was completed successfully, and null otherwise.
	 * 
	 * @see AccessibleEditableTextListener#setTextAttributes
	 * 
	 */
	public String result;

	static final long serialVersionUID = 7131825608864332802L;

/**
 * Constructs a new instance of this class.
 *
 * @param source the object that fired the event
 */
public AccessibleTextAttributeEvent(Object source) {
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
		+ " offset=" + offset   //$NON-NLS-1$
		+ " start=" + start   //$NON-NLS-1$
		+ " end=" + end   //$NON-NLS-1$
//		+ " textStyle=" + textStyle   //$NON-NLS-1$
		+ " attributes=" + toAttributeString(attributes)   //$NON-NLS-1$
		+ " result=" + result   //$NON-NLS-1$
		+ "}";  //$NON-NLS-1$
}

String toAttributeString(String [] attributes) {
	if (attributes == null || attributes.length == 0) return "" + attributes;   //$NON-NLS-1$
	StringBuffer attributeString = new StringBuffer();
	for (int i = 0; i < attributes.length; i++) {
		attributeString.append(attributes[i]);
		attributeString.append((i % 2 == 0) ? ":" : ";");   //$NON-NLS-1$   //$NON-NLS-2$
	}
	return attributeString.toString();
}
}
