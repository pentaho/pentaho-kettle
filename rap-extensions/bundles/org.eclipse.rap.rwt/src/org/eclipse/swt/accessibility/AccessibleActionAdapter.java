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
 * methods in the <code>AccessibleActionListener</code> interface.
 * <p>
 * Classes that wish to deal with <code>AccessibleAction</code> events can
 * extend this class and override only the methods that they are
 * interested in.
 * </p>
 *
 * @see AccessibleActionListener
 * @see AccessibleActionEvent
 *
 * @since 1.4
 */
public class AccessibleActionAdapter implements AccessibleActionListener {
	/**
	 * Returns the number of accessible actions available in this object.
	 * <p>
	 * If there are more than one, the first one (index 0) is considered the
	 * "default" action of the object.
	 * </p>
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[out] count - the number of actions, or zero if there are no actions</li>
	 * </ul>
	 */
	public void getActionCount(AccessibleActionEvent e) {}

	/**
	 * Performs the specified action on the object.
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[in] index - a 0 based index specifying the action to perform.
	 * 		If the index lies outside the valid range no action is performed.</li>
	 * <li>[out] result - set to {@link ACC#OK} if the action was performed.</li>
	 * </ul>
	 */
	public void doAction(AccessibleActionEvent e) {}

	/**
	 * Returns a description of the specified action.
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[in] index - a 0 based index specifying which action's description to return</li>
	 * <li>[out] result - a localized string describing the specified action,
	 * 		or null if the index lies outside the valid range</li>
	 * </ul>
	 */
	public void getDescription(AccessibleActionEvent e) {}

	/**
	 * Returns a string representing one or more key bindings, if there
	 * are any, associated with the specified action.
	 * <p>
	 * The returned string is of the following form: mnemonic;accelerator
	 * for example: "C;CTRL+C" for the Copy item in a typical Edit menu.
	 * </p>
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[in] index - a 0 based index specifying which action's key bindings to return</li>
	 * <li>[out] result - a semicolon-delimited string of localized key bindings
	 * 		(example: "C;CTRL+C"), or null if the index lies outside the valid range</li>
	 * </ul>
	 */
	public void getKeyBinding(AccessibleActionEvent e) {}

	/**
	 * Returns the name of the specified action.
	 * <p>
	 * There is no need to implement this method for single action controls
	 * since that would be redundant with AccessibleControlListener.getDefaultAction.
	 * </p>
	 * 
	 * @param e an event object containing the following fields:<ul>
	 * <li>[in] index - a 0 based index specifying which action's name to return</li>
	 * <li>[in] localized - a boolean indicating whether or not to return a localized name</li>
	 * <li>[out] result - the name of the specified action,
	 * 		or null if the index lies outside the valid range</li>
	 * </ul>
	 */
	public void getName(AccessibleActionEvent e) {}
}
