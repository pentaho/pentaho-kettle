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

/**
 * This adapter class provides default implementations for the
 * methods in the <code>AccessibleValueListener</code> interface.
 * <p>
 * Classes that wish to deal with <code>AccessibleValue</code> events can
 * extend this class and override only the methods that they are
 * interested in.
 * </p>
 *
 * @see AccessibleValueListener
 * @see AccessibleValueEvent
 *
 * @since 1.4
 */
public class AccessibleValueAdapter implements AccessibleValueListener {
	/**
	 * Returns the value of this object as a number.
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[out] value - the number that is the current value of this object</li>
	 * </ul>
	 */
	public void getCurrentValue(AccessibleValueEvent e) {}

	/**
	 * Sets the value of this object to the given number.
	 * 
	 * The argument is clipped to the valid interval whose upper and lower
	 * bounds are returned by getMaximumValue and getMinimumValue,
	 * i.e. if it is lower than the minimum value the new value will be the minimum,
	 * and if it is greater than the maximum then the new value will be the maximum.
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[in/out] value - on input, the number that will be the new value of this object
	 * 		<br>- on output, set to null if the value cannot be set</li>
	 * </ul>
	 */
	public void setCurrentValue(AccessibleValueEvent e) {}

	/**
	 * Returns the maximum value that can be represented by this object.
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[out] value - the number that is the maximum value that this object can represent.
	 * 		If this object has no upper bound then null is returned.</li>
	 * </ul>
	 */
	public void getMaximumValue(AccessibleValueEvent e) {}

	/**
	 * Returns the minimum value that can be represented by this object.
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[out] value - the number that is the minimum value that this object can represent.
	 * 		If this object has no lower bound then null is returned.</li>
	 * </ul>
	 */
	public void getMinimumValue(AccessibleValueEvent e) {}
}
