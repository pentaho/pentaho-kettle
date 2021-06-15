/*******************************************************************************
 * Copyright (c) 2012, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl <tom.schindl@bestsolution.at> - port to RAP
 ******************************************************************************/
package org.eclipse.jface.internal;

import org.eclipse.jface.action.IMenuListener2;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.rap.rwt.SingletonUtil;

/**
 * @since 2.3.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noreference This class is not intended to be referenced by clients.
 */
public class MenuManagerEventHelper {
	private IMenuListener2 showHelper;
	private IMenuListener2 hideHelper;

	/**
	 * Get a session instance
	 * 
	 * @return the session instance
	 */
	public static MenuManagerEventHelper getInstance() {
		return SingletonUtil.getSessionInstance(MenuManagerEventHelper.class);
	}

	/**
	 * Set a show helper
	 * 
	 * @param showHelper
	 *            show helper instance
	 */
	public void setShowHelper(IMenuListener2 showHelper) {
		this.showHelper = showHelper;
	}

	/**
	 * Set a hide helper
	 * 
	 * @param hideHelper
	 *            hide helper instance
	 */
	public void setHideHelper(IMenuListener2 hideHelper) {
		this.hideHelper = hideHelper;
	}

	/**
	 * Get the current show helper
	 * 
	 * @return the helper instance or <code>null</code>
	 */
	public IMenuListener2 getShowHelper() {
		return showHelper;
	}

	/**
	 * Get the current hide helper
	 * 
	 * @return the helper instance or <code>null</code>
	 */
	public IMenuListener2 getHideHelper() {
		return hideHelper;
	}

	/**
	 * Do show pre-processing.
	 *
	 * @param manager
	 */
	public void showEventPreHelper(MenuManager manager) {
		if (showHelper != null) {
			showHelper.menuAboutToShow(manager);
		}
	}

	/**
	 * Do show post-processing.
	 *
	 * @param manager
	 */
	public void showEventPostHelper(MenuManager manager) {
		if (showHelper != null) {
			showHelper.menuAboutToHide(manager);
		}
	}

	/**
	 * Do hide pre-processing.
	 *
	 * @param manager
	 */
	public void hideEventPreHelper(MenuManager manager) {
		if (hideHelper != null) {
			hideHelper.menuAboutToShow(manager);
		}
	}

	/**
	 * Do hide post-processing.
	 *
	 * @param manager
	 */
	public void hideEventPostHelper(MenuManager manager) {
		if (hideHelper != null) {
			hideHelper.menuAboutToHide(manager);
		}
	}
}