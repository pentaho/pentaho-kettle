/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Widget;

/**
 * This class is not part of the public API of JFace. See bug 267722.
 * 
 * @since 1.3
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class StructuredViewerInternals {

	/**
	 * Nothing to see here.
	 * 
	 * @noextend This interface is not intended to be extended by clients.
	 * @noimplement This interface is not intended to be implemented by clients.
	 */
	protected static interface AssociateListener {

		/**
		 * Call when an element is associated with an Item
		 * 
		 * @param element
		 * @param item
		 */
		void associate(Object element, Item item);

		/**
		 * Called when an Item is no longer associated
		 * 
		 * @param item
		 */
		void disassociate(Item item);
		
		/**
		 * Called when an element has been filtered out.
		 * 
		 * @param element 
		 */
		void filteredOut(Object element);
	}

	/**
	 * Nothing to see here. Sets or resets the AssociateListener for the given
	 * Viewer.
	 * 
	 * @param viewer
	 *            the viewer
	 * @param listener
	 *            the {@link AssociateListener}
	 * @noreference This method is not intended to be referenced by clients.
	 */
	protected static void setAssociateListener(StructuredViewer viewer,
			AssociateListener listener) {
		viewer.setAssociateListener(listener);
	}

	/**
	 * Nothing to see here. Returns the items for the given element.
	 * 
	 * @param viewer
	 * @param element
	 * @return the Widgets corresponding to the element
	 * 
	 * @noreference This method is not intended to be referenced by clients.
	 */
	protected static Widget[] getItems(StructuredViewer viewer, Object element) {
		return viewer.findItems(element);
	}

}
