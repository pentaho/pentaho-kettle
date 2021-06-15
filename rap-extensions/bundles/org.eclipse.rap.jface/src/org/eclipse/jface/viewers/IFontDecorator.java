/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers;

import org.eclipse.swt.graphics.Font;

/**
 * The IFontDecorator is an interface for objects that return a font to
 * decorate an object.
 * 
 * If an IFontDecorator decorates a font in an object that also has
 * an IFontProvider the IFontDecorator will take precedence.
 * @see IFontProvider
 * 
 * @since 1.0
 */
public interface IFontDecorator {
	
	/**
	 * Return the font for element or <code>null</code> if there
	 * is not one.
	 * 
	 * @param element
	 * @return Font or <code>null</code>
	 */
	public Font decorateFont(Object element);

}
