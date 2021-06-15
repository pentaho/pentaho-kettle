/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.accessibility;


import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Control;

/**
 * Instances of this class provide a bridge between application
 * code and assistive technology clients. Many platforms provide
 * default accessible behavior for most widgets, and this class
 * allows that default behavior to be overridden. Applications
 * can get the default Accessible object for a control by sending
 * it <code>getAccessible</code>, and then add an accessible listener
 * to override simple items like the name and help string, or they
 * can add an accessible control listener to override complex items.
 * As a rule of thumb, an application would only want to use the
 * accessible control listener to implement accessibility for a
 * custom control.
 * 
 * @see Control#getAccessible
 * @see AccessibleListener
 * @see AccessibleEvent
 * @see AccessibleControlListener
 * @see AccessibleControlEvent
 * @see <a href="http://www.eclipse.org/swt/snippets/#accessibility">Accessibility snippets</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 * 
 * @since 1.4
 */
public class Accessible {
	Control control;

	/**
	 * Constructs a new instance of this class given its parent.
	 * 
	 * @param parent the Accessible parent, which must not be null
	 * 
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 * </ul>
	 * 
	 * @see Control#getAccessible
	 * 
	 */
	public Accessible(Accessible parent) {
	}

	Accessible(Control control) {
		this.control = control;
	}
	
	/**
	 * Invokes platform specific functionality to allocate a new accessible object.
	 * <p>
	 * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
	 * API for <code>Accessible</code>. It is marked public only so that it
	 * can be shared within the packages provided by SWT. It is not
	 * available on all platforms, and should never be called from
	 * application code.
	 * </p>
	 *
	 * @param control the control to get the accessible object for
	 * @return the platform specific accessible object
	 * 
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static Accessible internal_new_Accessible(Control control) {
		return new Accessible(control);
	}

	/**
	 * Adds the listener to the collection of listeners who will
	 * be notified when an accessible client asks for certain strings,
	 * such as name, description, help, or keyboard shortcut. The
	 * listener is notified by sending it one of the messages defined
	 * in the <code>AccessibleListener</code> interface.
	 *
	 * @param listener the listener that should be notified when the receiver
	 * is asked for a name, description, help, or keyboard shortcut string
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
	 * </ul>
	 *
	 * @see AccessibleListener
	 * @see #removeAccessibleListener
	 */
	public void addAccessibleListener(AccessibleListener listener) {
	}
	
	/**
	 * Removes the listener from the collection of listeners who will
	 * be notified when an accessible client asks for certain strings,
	 * such as name, description, help, or keyboard shortcut.
	 *
	 * @param listener the listener that should no longer be notified when the receiver
	 * is asked for a name, description, help, or keyboard shortcut string
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
	 * </ul>
	 *
	 * @see AccessibleListener
	 * @see #addAccessibleListener
	 */
	public void removeAccessibleListener(AccessibleListener listener) {
	}
	
	/**
	 * Adds the listener to the collection of listeners who will
	 * be notified when an accessible client asks for custom control
	 * specific information. The listener is notified by sending it
	 * one of the messages defined in the <code>AccessibleControlListener</code>
	 * interface.
	 *
	 * @param listener the listener that should be notified when the receiver
	 * is asked for custom control specific information
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
	 * </ul>
	 *
	 * @see AccessibleControlListener
	 * @see #removeAccessibleControlListener
	 */
	public void addAccessibleControlListener(AccessibleControlListener listener) {
	}

	/**
	 * Removes the listener from the collection of listeners who will
	 * be notified when an accessible client asks for custom control
	 * specific information.
	 *
	 * @param listener the listener that should no longer be notified when the receiver
	 * is asked for custom control specific information
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
	 * </ul>
	 *
	 * @see AccessibleControlListener
	 * @see #addAccessibleControlListener
	 */
	public void removeAccessibleControlListener(AccessibleControlListener listener) {
	}
	
	/**
	 * Adds the listener to the collection of listeners who will
	 * be notified when an accessible client asks for custom text control
	 * specific information. The listener is notified by sending it
	 * one of the messages defined in the <code>AccessibleTextListener</code>
	 * and <code>AccessibleTextExtendedListener</code> interfaces.
	 *
	 * @param listener the listener that should be notified when the receiver
	 * is asked for custom text control specific information
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
	 * </ul>
	 *
	 * @see AccessibleTextListener
	 * @see AccessibleTextExtendedListener
	 * @see #removeAccessibleTextListener
	 * 
	 */
	public void addAccessibleTextListener (AccessibleTextListener listener) {
	}

	/**
	 * Removes the listener from the collection of listeners who will
	 * be notified when an accessible client asks for custom text control
	 * specific information.
	 *
	 * @param listener the listener that should no longer be notified when the receiver
	 * is asked for custom text control specific information
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
	 * </ul>
	 *
	 * @see AccessibleTextListener
	 * @see AccessibleTextExtendedListener
	 * @see #addAccessibleTextListener
	 * 
	 */
	public void removeAccessibleTextListener (AccessibleTextListener listener) {
	}

	/**
	 * Adds the listener to the collection of listeners that will be
	 * notified when an accessible client asks for any of the properties
	 * defined in the <code>AccessibleAction</code> interface.
	 *
	 * @param listener the listener that should be notified when the receiver
	 * is asked for <code>AccessibleAction</code> interface properties
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
	 * </ul>
	 *
	 * @see AccessibleActionListener
	 * @see #removeAccessibleActionListener
	 * 
	 */
	public void addAccessibleActionListener(AccessibleActionListener listener) {
	}

	/**
	 * Adds the listener to the collection of listeners that will be
	 * notified when an accessible client asks for any of the properties
	 * defined in the <code>AccessibleEditableText</code> interface.
	 *
	 * @param listener the listener that should be notified when the receiver
	 * is asked for <code>AccessibleEditableText</code> interface properties
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
	 * </ul>
	 *
	 * @see AccessibleEditableTextListener
	 * @see #removeAccessibleEditableTextListener
	 * 
	 */
	public void addAccessibleEditableTextListener(AccessibleEditableTextListener listener) {
	}

	/**
	 * Adds the listener to the collection of listeners that will be
	 * notified when an accessible client asks for any of the properties
	 * defined in the <code>AccessibleHyperlink</code> interface.
	 *
	 * @param listener the listener that should be notified when the receiver
	 * is asked for <code>AccessibleHyperlink</code> interface properties
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
	 * </ul>
	 *
	 * @see AccessibleHyperlinkListener
	 * @see #removeAccessibleHyperlinkListener
	 * 
	 */
	public void addAccessibleHyperlinkListener(AccessibleHyperlinkListener listener) {
	}

	/**
	 * Adds the listener to the collection of listeners that will be
	 * notified when an accessible client asks for any of the properties
	 * defined in the <code>AccessibleTable</code> interface.
	 *
	 * @param listener the listener that should be notified when the receiver
	 * is asked for <code>AccessibleTable</code> interface properties
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
	 * </ul>
	 *
	 * @see AccessibleTableListener
	 * @see #removeAccessibleTableListener
	 * 
	 */
	public void addAccessibleTableListener(AccessibleTableListener listener) {
	}

	/**
	 * Adds the listener to the collection of listeners that will be
	 * notified when an accessible client asks for any of the properties
	 * defined in the <code>AccessibleTableCell</code> interface.
	 *
	 * @param listener the listener that should be notified when the receiver
	 * is asked for <code>AccessibleTableCell</code> interface properties
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
	 * </ul>
	 *
	 * @see AccessibleTableCellListener
	 * @see #removeAccessibleTableCellListener
	 * 
	 */
	public void addAccessibleTableCellListener(AccessibleTableCellListener listener) {
	}

	/**
	 * Adds the listener to the collection of listeners that will be
	 * notified when an accessible client asks for any of the properties
	 * defined in the <code>AccessibleValue</code> interface.
	 *
	 * @param listener the listener that should be notified when the receiver
	 * is asked for <code>AccessibleValue</code> interface properties
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
	 * </ul>
	 *
	 * @see AccessibleValueListener
	 * @see #removeAccessibleValueListener
	 * 
	 */
	public void addAccessibleValueListener(AccessibleValueListener listener) {
	}

	/**
	 * Adds the listener to the collection of listeners that will be
	 * notified when an accessible client asks for any of the properties
	 * defined in the <code>AccessibleAttribute</code> interface.
	 *
	 * @param listener the listener that should be notified when the receiver
	 * is asked for <code>AccessibleAttribute</code> interface properties
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
	 * </ul>
	 *
	 * @see AccessibleAttributeListener
	 * @see #removeAccessibleAttributeListener
	 * 
	 */
	public void addAccessibleAttributeListener(AccessibleAttributeListener listener) {
	}

	/**
	 * Adds a relation with the specified type and target
	 * to the receiver's set of relations.
	 * 
	 * @param type an <code>ACC</code> constant beginning with RELATION_* indicating the type of relation
	 * @param target the accessible that is the target for this relation
	 * 
	 */
	public void addRelation(int type, Accessible target) {
	}
	
	/**
	 * Disposes of the operating system resources associated with
	 * the receiver, and removes the receiver from its parent's
	 * list of children.
	 * <p>
	 * This method should be called when an accessible that was created
	 * with the public constructor <code>Accessible(Accessible parent)</code>
	 * is no longer needed. You do not need to call this when the receiver's
	 * control is disposed, because all <code>Accessible</code> instances
	 * associated with a control are released when the control is disposed.
	 * It is also not necessary to call this for instances of <code>Accessible</code>
	 * that were retrieved with <code>Control.getAccessible()</code>.
	 * </p>
	 * 
	 */
	public void dispose () {
	}

	/**
	 * Returns the control for this Accessible object. 
	 *
	 * @return the receiver's control
	 */
	public Control getControl() {
		return control;
	}

	/**
	 * Removes the listener from the collection of listeners that will be
	 * notified when an accessible client asks for any of the properties
	 * defined in the <code>AccessibleAction</code> interface.
	 *
	 * @param listener the listener that should no longer be notified when the receiver
	 * is asked for <code>AccessibleAction</code> interface properties
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
	 * </ul>
	 *
	 * @see AccessibleActionListener
	 * @see #addAccessibleActionListener
	 * 
	 */
	public void removeAccessibleActionListener(AccessibleActionListener listener) {
	}

	/**
	 * Removes the listener from the collection of listeners that will be
	 * notified when an accessible client asks for any of the properties
	 * defined in the <code>AccessibleEditableText</code> interface.
	 *
	 * @param listener the listener that should no longer be notified when the receiver
	 * is asked for <code>AccessibleEditableText</code> interface properties
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
	 * </ul>
	 *
	 * @see AccessibleEditableTextListener
	 * @see #addAccessibleEditableTextListener
	 * 
	 */
	public void removeAccessibleEditableTextListener(AccessibleEditableTextListener listener) {
	}
	
	/**
	 * Removes the listener from the collection of listeners that will be
	 * notified when an accessible client asks for any of the properties
	 * defined in the <code>AccessibleHyperlink</code> interface.
	 *
	 * @param listener the listener that should no longer be notified when the receiver
	 * is asked for <code>AccessibleHyperlink</code> interface properties
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
	 * </ul>
	 *
	 * @see AccessibleHyperlinkListener
	 * @see #addAccessibleHyperlinkListener
	 * 
	 */
	public void removeAccessibleHyperlinkListener(AccessibleHyperlinkListener listener) {
	}

	/**
	 * Removes the listener from the collection of listeners that will be
	 * notified when an accessible client asks for any of the properties
	 * defined in the <code>AccessibleTable</code> interface.
	 *
	 * @param listener the listener that should no longer be notified when the receiver
	 * is asked for <code>AccessibleTable</code> interface properties
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
	 * </ul>
	 *
	 * @see AccessibleTableListener
	 * @see #addAccessibleTableListener
	 * 
	 */
	public void removeAccessibleTableListener(AccessibleTableListener listener) {
	}

	/**
	 * Removes the listener from the collection of listeners that will be
	 * notified when an accessible client asks for any of the properties
	 * defined in the <code>AccessibleTableCell</code> interface.
	 *
	 * @param listener the listener that should no longer be notified when the receiver
	 * is asked for <code>AccessibleTableCell</code> interface properties
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
	 * </ul>
	 *
	 * @see AccessibleTableCellListener
	 * @see #addAccessibleTableCellListener
	 * 
	 */
	public void removeAccessibleTableCellListener(AccessibleTableCellListener listener) {
	}

	/**
	 * Removes the listener from the collection of listeners that will be
	 * notified when an accessible client asks for any of the properties
	 * defined in the <code>AccessibleValue</code> interface.
	 *
	 * @param listener the listener that should no longer be notified when the receiver
	 * is asked for <code>AccessibleValue</code> interface properties
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
	 * </ul>
	 *
	 * @see AccessibleValueListener
	 * @see #addAccessibleValueListener
	 * 
	 */
	public void removeAccessibleValueListener(AccessibleValueListener listener) {
	}

	/**
	 * Removes the listener from the collection of listeners that will be
	 * notified when an accessible client asks for any of the properties
	 * defined in the <code>AccessibleAttribute</code> interface.
	 *
	 * @param listener the listener that should no longer be notified when the receiver
	 * is asked for <code>AccessibleAttribute</code> interface properties
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
	 * </ul>
	 *
	 * @see AccessibleAttributeListener
	 * @see #addAccessibleAttributeListener
	 * 
	 */
	public void removeAccessibleAttributeListener(AccessibleAttributeListener listener) {
	}

	/**
	 * Removes the relation with the specified type and target
	 * from the receiver's set of relations.
	 * 
	 * @param type an <code>ACC</code> constant beginning with RELATION_* indicating the type of relation
	 * @param target the accessible that is the target for this relation
	 * 
	 */
	public void removeRelation(int type, Accessible target) {
	}
	
	/**
	 * Sends a message with event-specific data to accessible clients
	 * indicating that something has changed within a custom control.
	 *
	 * @param event an <code>ACC</code> constant beginning with EVENT_* indicating the message to send
	 * @param eventData an object containing event-specific data, or null if there is no event-specific data
	 * 
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
	 * </ul>
	 * 
	 * @see ACC#EVENT_ACTION_CHANGED
	 * @see ACC#EVENT_ATTRIBUTE_CHANGED
	 * @see ACC#EVENT_DESCRIPTION_CHANGED
	 * @see ACC#EVENT_DOCUMENT_LOAD_COMPLETE
	 * @see ACC#EVENT_DOCUMENT_LOAD_STOPPED
	 * @see ACC#EVENT_DOCUMENT_RELOAD
	 * @see ACC#EVENT_HYPERLINK_ACTIVATED
	 * @see ACC#EVENT_HYPERLINK_ANCHOR_COUNT_CHANGED
	 * @see ACC#EVENT_HYPERLINK_END_INDEX_CHANGED
	 * @see ACC#EVENT_HYPERLINK_SELECTED_LINK_CHANGED
	 * @see ACC#EVENT_HYPERLINK_START_INDEX_CHANGED
	 * @see ACC#EVENT_HYPERTEXT_LINK_COUNT_CHANGED
	 * @see ACC#EVENT_HYPERTEXT_LINK_SELECTED
	 * @see ACC#EVENT_LOCATION_CHANGED
	 * @see ACC#EVENT_NAME_CHANGED
	 * @see ACC#EVENT_PAGE_CHANGED
	 * @see ACC#EVENT_SECTION_CHANGED
	 * @see ACC#EVENT_SELECTION_CHANGED
	 * @see ACC#EVENT_STATE_CHANGED
	 * @see ACC#EVENT_TABLE_CAPTION_CHANGED
	 * @see ACC#EVENT_TABLE_CHANGED
	 * @see ACC#EVENT_TABLE_COLUMN_DESCRIPTION_CHANGED
	 * @see ACC#EVENT_TABLE_COLUMN_HEADER_CHANGED
	 * @see ACC#EVENT_TABLE_ROW_DESCRIPTION_CHANGED
	 * @see ACC#EVENT_TABLE_ROW_HEADER_CHANGED
	 * @see ACC#EVENT_TABLE_SUMMARY_CHANGED
	 * @see ACC#EVENT_TEXT_ATTRIBUTE_CHANGED
	 * @see ACC#EVENT_TEXT_CARET_MOVED
	 * @see ACC#EVENT_TEXT_CHANGED
	 * @see ACC#EVENT_TEXT_COLUMN_CHANGED
	 * @see ACC#EVENT_TEXT_SELECTION_CHANGED
	 * @see ACC#EVENT_VALUE_CHANGED
	 * 
	 */
	public void sendEvent(int event, Object eventData) {
	}

	/**
	 * Sends a message to accessible clients that the child selection
	 * within a custom container control has changed.
	 *
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
	 * </ul>
	 * 
	 */
	public void selectionChanged () {
	}
	
	/**
	 * Sends a message to accessible clients that the text
	 * caret has moved within a custom control.
	 *
	 * @param index the new caret index within the control
	 * 
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
	 * </ul>
	 *
	 */
	public void textCaretMoved (int index) {
	}
	
	/**
	 * Sends a message to accessible clients that the text
	 * within a custom control has changed.
	 *
	 * @param type the type of change, one of <code>ACC.TEXT_INSERT</code>
	 * or <code>ACC.TEXT_DELETE</code>
	 * @param startIndex the text index within the control where the insertion or deletion begins
	 * @param length the non-negative length in characters of the insertion or deletion
	 *
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
	 * </ul>
	 * 
	 * @see ACC#TEXT_INSERT
	 * @see ACC#TEXT_DELETE
	 * 
	 */
	public void textChanged (int type, int startIndex, int length) {
	}
	
	/**
	 * Sends a message to accessible clients that the text
	 * selection has changed within a custom control.
	 *
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
	 * </ul>
	 *
	 */
	public void textSelectionChanged () {
	}
	
	/**
	 * Sends a message to accessible clients indicating that the focus
	 * has changed within a custom control.
	 *
	 * @param childID an identifier specifying a child of the control
	 * 
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver's control has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver's control</li>
	 * </ul>
	 */
	public void setFocus(int childID) {
	}

	/**
	 * Invokes platform specific functionality to dispose an accessible object.
	 * <p>
	 * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
	 * API for <code>Accessible</code>. It is marked public only so that it
	 * can be shared within the packages provided by SWT. It is not
	 * available on all platforms, and should never be called from
	 * application code.
	 * </p>
	 * 
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public void internal_dispose_Accessible() {
	}
	
	/**
	 * Invokes platform specific functionality to handle a window message.
	 * <p>
	 * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
	 * API for <code>Accessible</code>. It is marked public only so that it
	 * can be shared within the packages provided by SWT. It is not
	 * available on all platforms, and should never be called from
	 * application code.
	 * </p>
	 * 
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public int internal_WM_GETOBJECT (int wParam, int lParam) {
		return 0;
	}	
}
