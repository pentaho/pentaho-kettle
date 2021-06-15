/*******************************************************************************
 * Copyright (c) 2003, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.browser;

/**
 * This adapter class provides default implementations for the
 * methods described by the {@link LocationListener} interface.
 * <p>
 * Classes that wish to deal with {@link LocationEvent}'s can
 * extend this class and override only the methods which they are
 * interested in.
 * </p>
 * 
 * @since 1.3
 */
public abstract class LocationAdapter implements LocationListener {

public void changing(LocationEvent event) {
}

public void changed(LocationEvent event) {
}
}
