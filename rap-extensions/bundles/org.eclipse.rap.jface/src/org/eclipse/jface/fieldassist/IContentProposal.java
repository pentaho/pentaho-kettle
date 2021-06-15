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
 * IContentProposal describes a content proposal to be shown. It consists of the
 * content that will be provided if the proposal is accepted, an optional label
 * used to describe the content to the user, and an optional description that
 * further elaborates the meaning of the proposal. It also includes a a
 * zero-based index position within the contents where the cursor should be
 * placed after a proposal is accepted.
 * 
 * @since 1.0
 * 
 * @see ContentProposal
 */
public interface IContentProposal extends Serializable {
	/**
	 * Return the content represented by this proposal.
	 * 
	 * @return the String content represented by this proposal.
	 */
	public String getContent();

	/**
	 * Return the integer position within the contents that the cursor should be
	 * placed after the proposal is accepted.
	 * 
	 * @return the zero-based index position within the contents where the
	 *         cursor should be placed after the proposal is accepted. The range
	 *         of the cursor position is from 0..N where N is the number of
	 *         characters in the contents.
	 */
	public int getCursorPosition();

	/**
	 * Return the label used to describe this proposal.
	 * 
	 * @return the String label used to display the proposal. If
	 *         <code>null</code>, then the content will be displayed as the
	 *         label.
	 */
	public String getLabel();

	/**
	 * Return a description that describes this proposal.
	 * 
	 * @return the String label used to further the proposal. If
	 *         <code>null</code>, then no description will be displayed.
	 */
	public String getDescription();

}
