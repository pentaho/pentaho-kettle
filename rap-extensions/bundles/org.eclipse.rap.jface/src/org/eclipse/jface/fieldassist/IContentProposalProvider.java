/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.fieldassist;

import java.io.Serializable;

/**
 * IContentProposalProvider provides an array of IContentProposals that are
 * appropriate for a textual dialog field, given the field's current content and
 * the current cursor position. 
 * 
 * @since 1.0
 * 
 * @see SimpleContentProposalProvider
 */
public interface IContentProposalProvider extends Serializable {

	/**
	 * Return an array of content proposals representing the valid proposals for a
	 * field.
	 * 
	 * @param contents
	 *            the current contents of the text field
	 * @param position
	 *            the current position of the cursor in the contents
	 * 
	 * @return the array of {@link IContentProposal} that represent valid
	 *         proposals for the field.
	 */
	IContentProposal[] getProposals(String contents, int position);
}
