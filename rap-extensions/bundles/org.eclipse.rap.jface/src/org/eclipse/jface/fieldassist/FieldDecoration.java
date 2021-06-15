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

import java.io.Serializable;

import org.eclipse.swt.graphics.Image;

/**
 * FieldDecoration is a simple data structure class for specifying a decoration
 * for a field. A decoration may be rendered in different ways depending on the
 * type of field it is used with.
 * 
 * @see FieldDecorationRegistry
 * 
 * @since 1.0
 */
public class FieldDecoration implements Serializable {

	/*
	 * The image to be shown in the decoration.
	 */
	private Image image;

	/*
	 * The description to show in the decoration's hover.
	 */
	private String description;

	/**
	 * Create a decoration for a field with the specified image and description
	 * text.
	 * 
	 * @param image
	 *            the image shown in the decoration. A <code>null</code> image
	 *            will result in a blank decoration, which may be used to
	 *            reserve space near the field.
	 * @param description
	 *            the description shown when the user hovers over the
	 *            decoration. A <code>null</code> description indicates that
	 *            there will be no hover for the decoration.
	 */
	public FieldDecoration(Image image, String description) {
		this.image = image;
		this.description = description;
	}

	/**
	 * Return the image shown in the decoration, or <code>null</code> if no
	 * image is specified.
	 * 
	 * @return the image shown in the decoration. A return value of
	 *         <code>null</code> signifies a blank decoration.
	 */
	public Image getImage() {
		return image;
	}

	/**
	 * Set the image shown in the decoration, or <code>null</code> if no image
	 * is specified. It is up to the caller to update any decorated fields that
	 * are showing the description in order to display the new image.
	 * 
	 * @param image
	 *            the image shown in the decoration. A value of
	 *            <code>null</code> signifies a blank decoration.
	 */
	public void setImage(Image image) {
		this.image = image;
	}

	/**
	 * Return the description for the decoration shown when the user hovers over
	 * the decoration.
	 * 
	 * @return the String description of the decoration. A return value of
	 *         <code>null</code> indicates that no description will be shown.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set the description for the decoration shown when the user hovers over
	 * the decoration. It is up to the caller to update any decorated fields
	 * showing the description.
	 * 
	 * @param description
	 *            the String description of the decoration. A value of
	 *            <code>null</code> indicates that no description will be
	 *            shown.
	 */
	public void setDescription(String description) {
		this.description = description;
	}
}
