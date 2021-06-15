/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
 * Classes that implement this interface provide methods
 * that deal with the events that are generated when an
 * accessibility client sends a message to a control.
 * <p>
 * After creating an instance of a class that implements
 * this interface it can be added to a control using the
 * <code>addAccessibleListener</code> method and removed
 * using the <code>removeAccessibleListener</code> method.
 * When a client requests information, the appropriate method
 * will be invoked.
 * </p><p>
 * Note: Accessibility clients use child identifiers to specify
 * whether they want information about a control or one of its children.
 * Child identifiers are increasing integers beginning with 0.
 * The identifier CHILDID_SELF represents the control itself.
 * </p>
 *
 * @see AccessibleAdapter
 * @see AccessibleEvent
 * 
 * @since 1.4
 */
public interface AccessibleListener extends SWTEventListener {

	/**
	 * Sent when an accessibility client requests the name
	 * of the control, or the name of a child of the control.
	 * <p>
	 * Return the name of the control or specified child in the
	 * <code>result</code> field of the event object. Returning
	 * an empty string tells the client that the control or child
	 * does not have a name, and returning null tells the client
	 * to use the platform name.
	 * </p>
	 *
	 * @param e an event object containing the following fields:<ul>
	 *    <li>childID [IN] - an identifier specifying the control or one of its children</li>
	 *    <li>result [OUT] - the requested name string, or null</li>
	 * </ul>
	 */
	public void getName(AccessibleEvent e);

	/**
	 * Sent when an accessibility client requests the help string
	 * of the control, or the help string of a child of the control.
	 * <p>
	 * The information in this property should be similar to the help
	 * provided by toolTipText. It describes what the control or child
	 * does or how to use it, as opposed to getDescription, which
	 * describes appearance.
	 * </p><p>
	 * Return the help string of the control or specified child in
	 * the <code>result</code> field of the event object. Returning
	 * an empty string tells the client that the control or child
	 * does not have a help string, and returning null tells the
	 * client to use the platform help string.
	 * </p>
	 *
	 * @param e an event object containing the following fields:<ul>
	 *    <li>childID [IN] - an identifier specifying the control or one of its children</li>
	 *    <li>result [OUT] - the requested help string, or null</li>
	 * </ul>
	 */
	public void getHelp(AccessibleEvent e);

	/**
	 * Sent when an accessibility client requests the keyboard shortcut
	 * of the control, or the keyboard shortcut of a child of the control.
	 * <p>
	 * A keyboard shortcut can either be a mnemonic, or an accelerator.
	 * As a general rule, if the control or child can receive keyboard focus,
	 * then you should expose its mnemonic, and if it cannot receive keyboard
	 * focus, then you should expose its accelerator.
	 * </p><p>
	 * Return the keyboard shortcut string of the control or specified child
	 * in the <code>result</code> field of the event object. Returning an
	 * empty string tells the client that the control or child does not
	 * have a keyboard shortcut string, and returning null tells the client
	 * to use the platform keyboard shortcut string.
	 * </p>
	 *
	 * @param e an event object containing the following fields:<ul>
	 *    <li>childID [IN] - an identifier specifying the control or one of its children</li>
	 *    <li>result [OUT] - the requested keyboard shortcut string (example: "ALT+N"), or null</li>
	 * </ul>
	 */
	public void getKeyboardShortcut(AccessibleEvent e);

	/**
	 * Sent when an accessibility client requests a description
	 * of the control, or a description of a child of the control.
	 * <p>
	 * This is a textual description of the control or child's visual
	 * appearance, which is typically only necessary if it cannot be
	 * determined from other properties such as role.
	 * </p><p>
	 * Return the description of the control or specified child in
	 * the <code>result</code> field of the event object. Returning
	 * an empty string tells the client that the control or child
	 * does not have a description, and returning null tells the
	 * client to use the platform description.
	 * </p>
	 *
	 * @param e an event object containing the following fields:<ul>
	 *    <li>childID [IN] - an identifier specifying the control or one of its children</li>
	 *    <li>result [OUT] - the requested description string, or null</li>
	 * </ul>
	 */
	public void getDescription(AccessibleEvent e);
}
