/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.bindings;

import org.eclipse.core.commands.common.AbstractNamedHandleEvent;

/**
 * An instance of this class describes changes to an instance of
 * <code>IScheme</code>.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 1.4
 * @see ISchemeListener#schemeChanged(SchemeEvent)
 */
public final class SchemeEvent extends AbstractNamedHandleEvent {

	/**
	 * The bit used to represent whether the scheme has changed its parent.
	 */
	private static final int CHANGED_PARENT_ID = LAST_USED_BIT << 1;

	/**
	 * The scheme that has changed; this value is never <code>null</code>.
	 */
	private final Scheme scheme;

	/**
	 * Creates a new instance of this class.
	 * 
	 * @param scheme
	 *            the instance of the interface that changed; must not be
	 *            <code>null</code>.
	 * @param definedChanged
	 *            true, iff the defined property changed.
	 * @param nameChanged
	 *            true, iff the name property changed.
	 * @param descriptionChanged
	 *            <code>true</code> if the description property changed;
	 *            <code>false</code> otherwise.
	 * @param parentIdChanged
	 *            true, iff the parentId property changed.
	 */
	public SchemeEvent(Scheme scheme, boolean definedChanged,
			boolean nameChanged, boolean descriptionChanged,
			boolean parentIdChanged) {
		super(definedChanged, descriptionChanged, nameChanged);

		if (scheme == null) {
			throw new NullPointerException();
		}
		this.scheme = scheme;

		if (parentIdChanged) {
			changedValues |= CHANGED_PARENT_ID;
		}
	}

	/**
	 * Returns the instance of the scheme that changed.
	 * 
	 * @return the instance of the scheme that changed. Guaranteed not to be
	 *         <code>null</code>.
	 */
	public final Scheme getScheme() {
		return scheme;
	}

	/**
	 * Returns whether or not the parentId property changed.
	 * 
	 * @return true, iff the parentId property changed.
	 */
	public final boolean isParentIdChanged() {
		return ((changedValues & CHANGED_PARENT_ID) != 0);
	}
}
