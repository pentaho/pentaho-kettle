/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.util;

/**
 * This class is used to maintain a list of listeners, and is used in the
 * implementations of several classes within JFace which allow you to register
 * listeners of various kinds. It is a fairly lightweight object, occupying
 * minimal space when no listeners are registered.
 * <p>
 * Note that the <code>add</code> method checks for and eliminates duplicates
 * based on identity (not equality). Likewise, the <code>remove</code> method
 * compares based on identity.
 * </p>
 * <p>
 * Use the <code>getListeners</code> method when notifying listeners. Note
 * that no garbage is created if no listeners are registered. The recommended
 * code sequence for notifying all registered listeners of say,
 * <code>FooListener.eventHappened</code>, is:
 * 
 * <pre>
 * Object[] listeners = myListenerList.getListeners();
 * for (int i = 0; i &lt; listeners.length; ++i) {
 * 	((FooListener) listeners[i]).eventHappened(event);
 * }
 * </pre>
 * 
 * </p>
 * 
 * @deprecated Please use {@link org.eclipse.core.runtime.ListenerList} instead.
 *             Please note that the {@link #ListenerList(int)} and
 *             {@link org.eclipse.core.runtime.ListenerList#ListenerList(int)}
 *             constructors have different semantics. Please read the javadoc
 *             carefully. Also note that the equivalent of
 *             {@link #ListenerList()} is actually
 *             {@link org.eclipse.core.runtime.ListenerList#ListenerList(int)}
 *             with {@link org.eclipse.core.runtime.ListenerList#IDENTITY} as
 *             the argument.
 */
public class ListenerList extends org.eclipse.core.runtime.ListenerList {

	/**
	 * Creates a listener list with an initial capacity of 1.
	 */
	public ListenerList() {
		super(IDENTITY);
	}

	/**
	 * Creates a listener list with the given initial capacity.
	 * 
	 * @param capacity
	 *            the number of listeners which this list can initially accept
	 *            without growing its internal representation; must be at least
	 *            1
	 */
	public ListenerList(int capacity) {
		// the runtime ListenerList does not support capacity
		this();
	}
}
