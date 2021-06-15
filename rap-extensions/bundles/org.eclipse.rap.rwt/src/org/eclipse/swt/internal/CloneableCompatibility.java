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
package org.eclipse.swt.internal; 


/**
 * This interface is the cross-platform version of the
 * java.lang.Cloneable interface.
 * <p>
 * It is part of our effort to provide support for both J2SE
 * and J2ME platforms. Under this scheme, classes need to 
 * implement CloneableCompatibility instead of java.lang.Cloneable.
 * </p>
 * <p>
 * Note: java.lang.Cloneable is not part of CLDC.
 * </p>
 */
public interface CloneableCompatibility extends Cloneable {
}
