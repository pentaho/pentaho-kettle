/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.browser;

import org.eclipse.swt.internal.SWTEventListener;


/**
 * This listener interface may be implemented in order to receive
 * a {@link LocationEvent} notification when a {@link Browser}
 * navigates to a different URL.
 * 
 * @see Browser#addLocationListener(LocationListener)
 * @see Browser#removeLocationListener(LocationListener)
 * 
 * @since 1.0
 */
public interface LocationListener extends SWTEventListener {

/**
 * This method is called when the current location is about to be changed.
 * <p>
 *
 * <p>The following fields in the <code>LocationEvent</code> apply:
 * <ul>
 * <li>(in) location the location to be loaded
 * <li>(in) widget the <code>Browser</code> whose location is changing
 * <li>(in/out) doit can be set to <code>false</code> to prevent the location
 * from being loaded 
 * </ul>
 * 
 * @param event the <code>LocationEvent</code> that specifies the location
 * to be loaded by a <code>Browser</code>
 */ 
public void changing(LocationEvent event);

/**
 * This method is called when the current location is changed.
 * <p>
 *
 * <p>The following fields in the <code>LocationEvent</code> apply:
 * <ul>
 * <li>(in) location the current location
 * <li>(in) top <code>true</code> if the location opens in the top frame or
 * <code>false</code> otherwise
 * <li>(in) widget the <code>Browser</code> whose location has changed
 * </ul>
 * 
 * @param event the <code>LocationEvent</code> that specifies  the new
 * location of a <code>Browser</code>
 */ 
public void changed(LocationEvent event);

}
