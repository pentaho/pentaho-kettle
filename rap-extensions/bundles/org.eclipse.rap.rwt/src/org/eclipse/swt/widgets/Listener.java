/*******************************************************************************
 * Copyright (c) 2002, 2011 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Innoopract Informationssysteme GmbH - initial API and implementation
 *     EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.widgets;

import org.eclipse.swt.internal.SerializableCompatibility;


/**
 * Implementers of <code>Listener</code> provide a simple
 * <code>handleEvent()</code> method that is used internally by SWT to
 * dispatch events.
 * <p>
 * After creating an instance of a class that implements this interface it can
 * be added to a widget using the
 * <code>addListener(int eventType, Listener handler)</code> method and
 * removed using the
 * <code>removeListener (int eventType, Listener handler)</code> method. When
 * the specified event occurs, <code>handleEvent(...)</code> will be sent to
 * the instance.
 * </p>
 * <p>
 * Classes which implement this interface are described within SWT as providing
 * the <em>untyped listener</em> API. Typically, widgets will also provide a
 * higher-level <em>typed listener</em> API, that is based on the standard
 * <code>java.util.EventListener</code> pattern.
 * </p>
 * 
 * @see Widget#addListener
 * @see java.util.EventListener
 * @see org.eclipse.swt.events
 */
public interface Listener extends SerializableCompatibility {

/**
 * Sent when an event that the receiver has registered for occurs.
 *
 * @param event the event which occurred
 */
  void handleEvent( Event event );
}
