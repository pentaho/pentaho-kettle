/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.commands;

import org.eclipse.jface.menus.IMenuStateIds;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * <p>
 * A piece of state storing a {@link Boolean}.
 * </p>
 * <p>
 * If this state is registered using {@link IMenuStateIds#STYLE}, then it will
 * control the presentation of the command if displayed in the menus, tool bars
 * or status line.
 * </p>
 * <p>
 * Clients may instantiate this class, but must not extend.
 * </p>
 * 
 * @since 1.0
 */
public class ToggleState extends PersistentState {

	/**
	 * Constructs a new <code>ToggleState</code>. By default, the toggle is
	 * off (e.g., <code>false</code>).
	 */
	public ToggleState() {
		setValue(Boolean.FALSE);
	}

	public final void load(final IPreferenceStore store,
			final String preferenceKey) {
		final boolean currentValue = ((Boolean) getValue()).booleanValue(); 
		store.setDefault(preferenceKey, currentValue);
		if (shouldPersist() && (store.contains(preferenceKey))) {
			final boolean value = store.getBoolean(preferenceKey);
			setValue(value ? Boolean.TRUE : Boolean.FALSE);
		}
	}

	public final void save(final IPreferenceStore store,
			final String preferenceKey) {
		if (shouldPersist()) {
			final Object value = getValue();
			if (value instanceof Boolean) {
				store.setValue(preferenceKey, ((Boolean) value).booleanValue());
			}
		}
	}

	public void setValue(final Object value) {
		if (!(value instanceof Boolean)) {
			throw new IllegalArgumentException(
					"ToggleState takes a Boolean as a value"); //$NON-NLS-1$
		}

		super.setValue(value);
	}
}
