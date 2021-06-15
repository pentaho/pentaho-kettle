/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.core.commands.common.NamedHandleObject;
import org.eclipse.core.commands.common.NotDefinedException;

/**
 * A label provider for instances of <code>NamedHandlerObject</code>, which
 * exposes the name as the label.
 * 
 * @since 1.0
 */
public final class NamedHandleObjectLabelProvider extends LabelProvider {
	
	/**
	 * The text of the element is simply the name of the element if its a
	 * defined instance of <code>NamedHandleObject</code>. Otherwise, this
	 * method just returns <code>null</code>.
	 * 
	 * @param element
	 *            The element for which the text should be retrieved; may be
	 *            <code>null</code>.
	 * @return the name of the handle object; <code>null</code> if there is no
	 *         name or if the element is not a named handle object.
	 */
	public final String getText(final Object element) {
		if (element instanceof NamedHandleObject) {
			try {
				return ((NamedHandleObject) element).getName();
			} catch (final NotDefinedException e) {
				return null;
			}
		}

		return null;
	}
}

