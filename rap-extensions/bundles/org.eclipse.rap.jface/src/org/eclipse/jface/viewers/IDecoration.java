/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

/**
 * Defines the result of decorating an element.
 * 
 * This interface is not meant to be implemented and will be provided to
 * instances of <code>ILightweightLabelDecorator</code>.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IDecoration{

	/**
	 * Constants for placement of image decorations.
	 */
	public static final int TOP_LEFT = 0;

	/**
	 * Constant for the top right quadrant.
	 */
	public static final int TOP_RIGHT = 1;

	/**
	 * Constant for the bottom left quadrant.
	 */
	public static final int BOTTOM_LEFT = 2;

	/**
	 * Constant for the bottom right quadrant.
	 */
	public static final int BOTTOM_RIGHT = 3;

	/**
	 * Constant for the underlay.
	 */
	public static final int UNDERLAY = 4;

	/**
	 * Constant for replacing the original image. Note that for this to have an
	 * effect on the resulting decorated image, {@link #ENABLE_REPLACE} has to
	 * be set to {@link Boolean#TRUE} in the {@link IDecorationContext} (opt-in
	 * model). If replacement behavior is enabled, the resulting decorated image
	 * will be constructed by first painting the underlay, then the replacement
	 * image, and then the regular quadrant images.
	 * 
	 * @since 1.1
	 */
	public static final int REPLACE = 5;
	
	/**
	 * Constant that is used as the property key on an
	 * {@link IDecorationContext}. To enable image replacement, set to
	 * {@link Boolean#TRUE}.
	 * 
	 * @since 1.1
	 * @see IDecorationContext
	 */
	public static final String ENABLE_REPLACE = "org.eclipse.jface.viewers.IDecoration.disableReplace"; //$NON-NLS-1$
	
	/**
	 * Adds a prefix to the element's label.
	 * 
	 * @param prefix
	 *            the prefix
	 */
	public void addPrefix(String prefix);

	/**
	 * Adds a suffix to the element's label.
	 * 
	 * @param suffix
	 *            the suffix
	 */
	public void addSuffix(String suffix);

	/**
	 * Adds an overlay to the element's image.
	 * 
	 * @param overlay
	 *            the overlay image descriptor
	 */
	public void addOverlay(ImageDescriptor overlay);

	/**
	 * Adds an overlay to the element's image.
	 * 
	 * @param overlay
	 *            the overlay image descriptor
	 * @param quadrant
	 *            The constant for the quadrant to draw the image on.
	 */
	public void addOverlay(ImageDescriptor overlay, int quadrant);
	
	/**
	 * Set the foreground color for this decoration.
	 * @param color the color to be set for the foreground
	 * 
	 * @since 1.0
	 */
	public void setForegroundColor(Color color);
	
	/**
	 * Set the background color for this decoration.
	 * @param color the color to be set for the background
	 * 
	 * @since 1.0
	 */
	public void setBackgroundColor(Color color);
	
	/**
	 * Set the font for this decoration.
	 * @param font the font to use in this decoration
	 * 
	 * @since 1.0
	 */
	public void setFont(Font font);

	/**
	 * Return the decoration context in which this decoration
	 * will be applied.
	 * @return the decoration context
	 * 
	 * @since 1.0
	 */
	public IDecorationContext getDecorationContext();
}
