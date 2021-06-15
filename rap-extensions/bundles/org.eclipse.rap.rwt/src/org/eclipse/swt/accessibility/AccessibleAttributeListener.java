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

import org.eclipse.swt.internal.SWTEventListener;

/**
 * Classes which implement this interface provide methods
 * that handle AccessibleAttribute events.
 * <p>
 * After creating an instance of a class that implements
 * this interface it can be added to an accessible using the
 * <code>addAccessibleAttributeListener</code> method and removed using
 * the <code>removeAccessibleAttributeListener</code> method.
 * </p>
 *
 * @see AccessibleAttributeAdapter
 * @see AccessibleAttributeEvent
 * @see AccessibleTextAttributeEvent
 *
 * @since 1.4
 */
public interface AccessibleAttributeListener extends SWTEventListener {
	/**
	 * Returns attributes specific to this Accessible object.
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[out] topMargin - the top margin in pixels</li>
	 * <li>[out] bottomMargin - the bottom margin in pixels</li>
	 * <li>[out] leftMargin - the left margin in pixels</li>
	 * <li>[out] rightMargin - the right margin in pixels</li>
	 * <li>[out] tabStops - an array of pixel locations</li>
	 * <li>[out] justify - whether or not to justify the text</li>
	 * <li>[out] alignment - one of <code>SWT#LEFT</code>, <code>SWT#RIGHT</code> or <code>SWT#CENTER</code></li>
	 * <li>[out] indent - the indent in pixels</li>
	 * <li>[out] attributes - an array of alternating key and value Strings
	 * 		which represent additional (i.e. non predefined) attributes</li>
	 * </ul>
	 */
	public void getAttributes(AccessibleAttributeEvent e);

	/**
	 * Returns text attributes specific to this Accessible object.
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[in] offset - the 0 based text offset for which to return attribute information</li>
	 * <li>[out] start - the 0 based starting offset of the character range
	 * 		over which all text attributes match those of offset</li>
	 * <li>[out] end - the 0 based offset after the last character of the character range
	 * 		over which all text attributes match those of offset</li>
	 * <li>[out] textStyle - the TextStyle of the character range</li>
	 * <li>[out] attributes - an array of alternating key and value Strings
	 * 		that represent additional attributes that do not correspond to TextStyle fields</li>
	 * </ul>
	 */
	public void getTextAttributes(AccessibleTextAttributeEvent e);
}
