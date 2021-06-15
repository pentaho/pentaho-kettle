/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.viewers;

import java.util.EventObject;

import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.TraverseEvent;

/**
 * This event is passed on when a cell-editor is going to be activated
 * 
 * @since 1.2
 * 
 */
public class ColumnViewerEditorActivationEvent extends EventObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * if a key is pressed on a selected cell
	 */
	public static final int KEY_PRESSED = 1;

	/**
	 * if a cell is selected using a single click of the mouse
	 */
	public static final int MOUSE_CLICK_SELECTION = 2;

	/**
	 * if a cell is selected using double clicking of the mouse
	 */
	public static final int MOUSE_DOUBLE_CLICK_SELECTION = 3;

	/**
	 * if a cell is activated using code like e.g
	 * {@link ColumnViewer#editElement(Object, int)}
	 */
	public static final int PROGRAMMATIC = 4;

	/**
	 * is a cell is activated by traversing
	 */
	public static final int TRAVERSAL = 5;

	/**
	 * the original event triggered
	 */
	public EventObject sourceEvent;

	/**
	 * The time the event is triggered
	 */
	public int time;

	/**
	 * The event type triggered:
	 * <ul>
	 * <li>{@link #KEY_PRESSED} if a key is pressed on a selected cell</li>
	 * <li>{@link #MOUSE_CLICK_SELECTION} if a cell is selected using a single
	 * click of the mouse</li>
	 * <li>{@link #MOUSE_DOUBLE_CLICK_SELECTION} if a cell is selected using
	 * double clicking of the mouse</li>
	 * </ul>
	 */
	public int eventType;

	/**
	 * <b>Only set for {@link #KEY_PRESSED}</b>
	 */
	public int keyCode;

	/**
	 * <b>Only set for {@link #KEY_PRESSED}</b>
	 */
	public char character;

	/**
	 * The statemask
	 */
	public int stateMask;

	/**
	 * Cancel the event (=> editor is not activated)
	 */
	public boolean cancel = false;
	
	/**
	 * This constructor can be used when no event exists. The type set is
	 * {@link #PROGRAMMATIC}
	 * 
	 * @param cell
	 *            the cell
	 */
	public ColumnViewerEditorActivationEvent(ViewerCell cell) {
		super(cell);
		eventType = PROGRAMMATIC;
	}

	/**
	 * This constructor is used for all types of mouse events. Currently the
	 * type is can be {@link #MOUSE_CLICK_SELECTION} and
	 * {@link #MOUSE_DOUBLE_CLICK_SELECTION}
	 * 
	 * @param cell
	 *            the cell source of the event
	 * @param event
	 *            the event
	 */
	public ColumnViewerEditorActivationEvent(ViewerCell cell, MouseEvent event) {
		super(cell);

		if (event.count >= 2) {
			eventType = MOUSE_DOUBLE_CLICK_SELECTION;
		} else {
			eventType = MOUSE_CLICK_SELECTION;
		}

		this.sourceEvent = event;
		this.time = event.time;
	}

	/**
	 * @param cell
	 *            the cell source of the event
	 * @param event
	 *            the event
	 */
	public ColumnViewerEditorActivationEvent(ViewerCell cell, KeyEvent event) {
		super(cell);
		this.eventType = KEY_PRESSED;
		this.sourceEvent = event;
		this.time = event.time;
		this.keyCode = event.keyCode;
		this.character = event.character;
		this.stateMask = event.stateMask;
	}

	/**
	 * This constructor is used to mark the activation triggered by a traversal
	 * 
	 * @param cell
	 *            the cell source of the event
	 * @param event
	 *            the event
	 */
	public ColumnViewerEditorActivationEvent(ViewerCell cell, TraverseEvent event) {
		super(cell);
		this.eventType = TRAVERSAL;
		this.sourceEvent = event;
	}
}
