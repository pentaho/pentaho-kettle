/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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
 * methods described by the <code>AccessibleControlListener</code> interface.
 * <p>
 * Classes that wish to deal with <code>AccessibleControlEvent</code>s can
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
 * @see AccessibleControlListener
 * @see AccessibleControlEvent
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * 
 * @since 1.4
 */
public abstract class AccessibleControlAdapter implements AccessibleControlListener {

	/**
	 * Sent when an accessibility client requests the identifier
	 * of the control child at the specified display coordinates.
	 * The default behavior is to do nothing.
	 * <p>
	 * Return the identifier of the child at display point (x, y)
	 * in the <code>childID</code> field of the event object.
	 * Return CHILDID_SELF if point (x, y) is in the control itself
	 * and not in any child. Return CHILDID_NONE if point (x, y)
	 * is not contained in either the control or any of its children.
	 * </p>
	 *
	 * @param e an event object containing the following fields:<ul>
	 *    <li>x, y [IN] - the specified point in display coordinates</li>
	 *    <li>childID [Typical OUT] - the ID of the child at point, or CHILDID_SELF, or CHILDID_NONE</li>
	 *    <li>accessible [Optional OUT] - the accessible object for the control or child may be returned instead of the childID</li>
	 * </ul>
	 */
	public void getChildAtPoint(AccessibleControlEvent e) {
	}
	
	/**
	 * Sent when an accessibility client requests the location
	 * of the control, or the location of a child of the control.
	 * The default behavior is to do nothing.
	 * <p>
	 * Return a rectangle describing the location of the specified
	 * control or child in the <code>x, y, width, and height</code>
	 * fields of the event object.
	 * </p>
	 *
	 * @param e an event object containing the following fields:<ul>
	 *    <li>childID [IN] - an identifier specifying the control or one of its children</li>
	 *    <li>x, y, width, height [OUT] - the control or child location in display coordinates</li>
	 * </ul>
	 */
	public void getLocation(AccessibleControlEvent e) {
	}
	
	/**
	 * Sent when an accessibility client requests the accessible object
	 * for a child of the control by index or childID, or when a client
	 * requests the index of an accessible object in its parent.
	 * <p>
	 * The childID field in the event object can be one of the following:<ul>
	 *    <li>an integer child ID - return the accessible object for the specified child ID,
	 *    	or null if the specified child does not have its own accessible</li>
	 *    <li>{@link ACC#CHILDID_CHILD_AT_INDEX} - return the accessible child object at the specified index,
	 *    	or null if this object has no children</li>
	 *    <li>{@link ACC#CHILDID_CHILD_INDEX} - return the index of this accessible in its parent</li>
	 * </ul></p>
	 *
	 * @param e an event object containing the following fields:<ul>
	 *    <li>childID [IN] - an identifier specifying a child of the control, or one of the predefined CHILDID constants</li>
	 *    <li>detail [Optional IN] - the index of the child accessible to be returned when childID = CHILDID_CHILD_AT_INDEX</li>
	 *    <li>detail [Optional OUT] - the index of this accessible in its parent when childID = CHILDID_CHILD_INDEX</li>
	 *    <li>accessible [Optional OUT] - an Accessible for the specified childID or index, or null if one does not exist</li>
	 * </ul>
	 */
	public void getChild(AccessibleControlEvent e) {
	}
	
	/**
	 * Sent when an accessibility client requests the number of
	 * children in the control.
	 * The default behavior is to do nothing.
	 * <p>
	 * Return the number of child items in the <code>detail</code>
	 * field of the event object.
	 * </p>
	 *
	 * @param e an event object containing the following fields:<ul>
	 *    <li>detail [OUT] - the number of child items in this control</li>
	 * </ul>
	 */
	public void getChildCount(AccessibleControlEvent e) {
	}
	
	/**
	 * Sent when an accessibility client requests the default action
	 * of the control, or the default action of a child of the control.
	 * The default behavior is to do nothing.
	 * <p>
	 * This string is typically a verb describing what the user does to it.
	 * For example, a Push Button's default action is "Press", a Check Button's
	 * is "Check" or "UnCheck", and List items have the default action "Double Click".
	 * </p><p>
	 * Return a string describing the default action of the specified
	 * control or child in the <code>result</code> field of the event object.
	 * Returning null tells the client to use the platform default action string.
	 * </p>
	 *
	 * @param e an event object containing the following fields:<ul>
	 *    <li>childID [IN] - an identifier specifying the control or one of its children</li>
	 *    <li>result [OUT] - the requested default action string, or null</li>
	 * </ul>
	 */
	public void getDefaultAction(AccessibleControlEvent e) {
	}
	
	/**
	 * Sent when an accessibility client requests the identity of
	 * the child or control that has keyboard focus.
	 * The default behavior is to do nothing.
	 * <p>
	 * Return the identifier of the child that has focus in the
	 * <code>childID</code> field of the event object.
	 * Return CHILDID_SELF if the control itself has keyboard focus.
	 * Return CHILDID_NONE if neither the control nor any of its children has focus.
	 * </p>
	 *
	 * @param e an event object containing the following fields:<ul>
	 *    <li>childID [Typical OUT] - the ID of the child with focus, or CHILDID_SELF, or CHILDID_NONE</li>
	 *    <li>accessible [Optional OUT] - the accessible object for a child may be returned instead of its childID</li>
	 * </ul>
	 */
	public void getFocus(AccessibleControlEvent e) {
	}
	
	/**
	 * Sent when an accessibility client requests the role
	 * of the control, or the role of a child of the control.
	 * The default behavior is to do nothing.
	 * <p>
	 * Return a role constant (constant defined in ACC beginning with ROLE_)
	 * that describes the role of the specified control or child in the
	 * <code>detail</code> field of the event object.
	 * </p>
	 *
	 * @param e an event object containing the following fields:<ul>
	 *    <li>childID [IN] - an identifier specifying the control or one of its children</li>
	 *    <li>detail [OUT] - a role constant describing the role of the control or child</li>
	 * </ul>
	 */
	public void getRole(AccessibleControlEvent e) {
	}
	
	/**
	 * Sent when an accessibility client requests the identity of
	 * the child or control that is currently selected.
	 * The default behavior is to do nothing.
	 * <p>
	 * Return the identifier of the selected child in the
	 * <code>childID</code> field of the event object.
	 * Return CHILDID_SELF if the control itself is selected.
	 * Return CHILDID_MULTIPLE if multiple children are selected, and return an array of childIDs in the <code>children</code> field.
	 * Return CHILDID_NONE if neither the control nor any of its children are selected.
	 * </p>
	 *
	 * @param e an event object containing the following fields:<ul>
	 *    <li>childID [Typical OUT] - the ID of the selected child, or CHILDID_SELF, or CHILDID_MULTIPLE, or CHILDID_NONE</li>
	 *    <li>children [Optional OUT] - the array of childIDs for the selected children if CHILDID_MULTIPLE is returned</li>
	 *    <li>accessible [Optional OUT] - the accessible object for the control or child may be returned instead of the childID</li>
	 * </ul>
	 */
	public void getSelection(AccessibleControlEvent e) {
	}
	
	/**
	 * Sent when an accessibility client requests the state
	 * of the control, or the state of a child of the control.
	 * The default behavior is to do nothing.
	 * <p>
	 * Return a state mask (mask bit constants defined in ACC beginning with STATE_)
	 * that describes the current state of the specified control or child in the
	 * <code>detail</code> field of the event object.
	 * </p>
	 *
	 * @param e an event object containing the following fields:<ul>
	 *    <li>childID [IN] - an identifier specifying the control or one of its children</li>
	 *    <li>detail [OUT] - a state mask describing the current state of the control or child</li>
	 * </ul>
	 */
	public void getState(AccessibleControlEvent e) {
	}
	
	/**
	 * Sent when an accessibility client requests the value
	 * of the control, or the value of a child of the control.
	 * The default behavior is to do nothing.
	 * <p>
	 * Many controls do not return a value. Examples of controls
	 * that do are: Combo returns the text string, Text returns
	 * its contents, ProgressBar returns a string representing a
	 * percentage, and Tree items return a string representing
	 * their level in the tree.
	 * </p><p>
	 * Return a string describing the value of the specified control
	 * or child in the <code>result</code> field of the event object.
	 * Returning null tells the client to use the platform value string.
	 * </p>
	 *
	 * @param e an event object containing the following fields:<ul>
	 *    <li>childID [IN] - an identifier specifying the control or one of its children</li>
	 *    <li>result [OUT] - the requested value string, or null</li>
	 * </ul>
	 */
	public void getValue(AccessibleControlEvent e) {
	}
	
	/**
	 * Sent when an accessibility client requests the children, or visible children,
	 * of the control. The default behavior is to do nothing.
	 * <p>
	 * Return the children as an array of childIDs or accessibles in the
	 * <code>children</code> field of the event object.
	 * </p>
	 *
	 * @param e an event object containing the following fields:<ul>
	 *    <li>detail [IN] - a flag that may have one of the following values:<ul>
	 *    	<li>0 (default) - return all children</li>
	 *    	<li>VISIBLE - return all visible children</li>
	 *    </ul>
	 *    <li>children [Typical OUT] - an array of childIDs</li>
	 *    <li>children [Optional OUT] - an array of accessible objects for the children may be returned instead of the childIDs</li>
	 * </ul>
	 */
	public void getChildren(AccessibleControlEvent e) {
	}
}
