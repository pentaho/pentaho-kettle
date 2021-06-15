/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
 * methods described by the <code>AccessibleTextListener</code> interface.
 * <p>
 * Classes that wish to deal with <code>AccessibleTextEvent</code>s can
 * extend this class and override only the methods that they are
 * interested in.
 * </p><p>
 * Note: Accessibility clients use child identifiers to specify
 * whether they want information about a control or one of its children.
 * Child identifiers are increasing integers beginning with 0.
 * The identifier CHILDID_SELF represents the control itself.
 * When returning a child identifier to a client, you may use CHILDID_NONE
 * to indicate that no child or control has the required information.
 * </p><p>
 * Note: This adapter is typically used by implementors of
 * a custom control to provide very detailed information about
 * the control instance to accessibility clients.
 * </p>
 *
 * @see AccessibleTextListener
 * @see AccessibleTextEvent
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * 
 * @since 1.4
 */
public abstract class AccessibleTextAdapter implements AccessibleTextListener {
	
	/**
	 * Sent when an accessibility client requests the current character offset
	 * of the text caret.
	 * The default behavior is to do nothing.
	 * <p>
	 * Return the caret offset in the <code>offset</code>
	 * field of the event object.
	 * </p>
	 *
	 * @param e an event object containing the following fields:<ul>
	 *    <li>childID [IN] - an identifier specifying a child of the control</li>
	 *    <li>offset [OUT] - the current offset of the text caret</li>
	 * </ul>
	 */
	public void getCaretOffset (AccessibleTextEvent e) {
	}

	/**
	 * Sent when an accessibility client requests the range of the current
	 * text selection.
	 * The default behavior is to do nothing.
	 * <p>
	 * Return the selection start offset and non-negative length in the
	 * <code>offset</code> and <code>length</code> fields of the event object.
	 * </p>
	 *
	 * @param e an event object containing the following fields:<ul>
	 *    <li>childID [IN] - an identifier specifying a child of the control</li>
	 *    <li>offset [OUT] - the offset of the current text selection</li>
	 *    <li>length [OUT] - the length of the current text selection</li>
	 * </ul>
	 */
	public void getSelectionRange (AccessibleTextEvent e) {
	}
}
