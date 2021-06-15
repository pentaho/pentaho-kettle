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
package org.eclipse.jface.fieldassist;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * An {@link IControlCreator} for SWT Text controls. This is a convenience class
 * for creating text controls to be supplied to a decorated field.
 * 
 * @since 1.0
* @deprecated As of 3.3, clients should use {@link ControlDecoration} instead
*             of {@link DecoratedField}.
* 
 */
public class TextControlCreator implements IControlCreator {

	public Control createControl(Composite parent, int style) {
		return new Text(parent, style);
	}
}
