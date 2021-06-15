/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.viewers;

/**
 * Parties interested in activation and deactivation of editors extend this
 * class and implement any or all of the methods
 * 
 * @since 1.2
 * 
 */
public abstract class ColumnViewerEditorActivationListener {
	/**
	 * Called before an editor is activated
	 * 
	 * @param event
	 *            the event
	 */
	public abstract void beforeEditorActivated(ColumnViewerEditorActivationEvent event);

	/**
	 * Called after an editor has been activated
	 * 
	 * @param event the event
	 */
	public abstract void afterEditorActivated(ColumnViewerEditorActivationEvent event);

	/**
	 * Called before an editor is deactivated
	 * 
	 * @param event
	 *            the event
	 */
	public abstract void beforeEditorDeactivated(ColumnViewerEditorDeactivationEvent event);

	
	/**
	 * Called after an editor is deactivated
	 * 
	 * @param event the event
	 */
	public abstract void afterEditorDeactivated(ColumnViewerEditorDeactivationEvent event);
}
