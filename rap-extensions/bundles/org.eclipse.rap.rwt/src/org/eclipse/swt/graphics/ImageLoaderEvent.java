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
package org.eclipse.swt.graphics;

import org.eclipse.swt.internal.SWTEventObject;




/**
 * Instances of this class are sent as a result of the incremental
 * loading of image data.
 * <p>
 * <b>Notes:</b>
 * </p><ul>
 * <li>The number of events which will be sent when loading images
 * is not constant. It varies by image type, and for JPEG images it 
 * varies from image to image.</li>
 * <li>For image sources which contain multiple images, the 
 * <code>endOfImage</code> flag in the event will be set to true
 * after each individual image is loaded.</li>
 * </ul>
 * 
 * @see ImageLoader
 * @see ImageLoaderListener
 * @since 1.3
 */

public class ImageLoaderEvent extends SWTEventObject {
	
	/**
	 * if the <code>endOfImage</code> flag is false, then this is a
	 * partially complete copy of the current <code>ImageData</code>,
	 * otherwise this is a completely loaded <code>ImageData</code>
	 */
	public ImageData imageData;

	/**
	 * the zero-based count of image data increments -- this is
	 * equivalent to the number of events that have been generated
	 * while loading a particular image
	 */
	public int incrementCount;

	/**
	 * If this flag is true, then the current image data has been
	 * completely loaded, otherwise the image data is only partially
	 * loaded, and further ImageLoader events will occur unless an
	 * exception is thrown
	 */
	public boolean endOfImage;
	
	static final long serialVersionUID = 3257284738325558065L;

/**
 * Constructs a new instance of this class given the event source and
 * the values to store in its fields.
 *
 * @param source the ImageLoader that was loading when the event occurred
 * @param imageData the image data for the event
 * @param incrementCount the image data increment for the event
 * @param endOfImage the end of image flag for the event
 */
public ImageLoaderEvent(ImageLoader source, ImageData imageData, int incrementCount, boolean endOfImage) {
  super(source);
	this.imageData = imageData;
	this.incrementCount = incrementCount;
	this.endOfImage = endOfImage;
}

/**
 * Returns a string containing a concise, human-readable
 * description of the receiver.
 *
 * @return a string representation of the event
 */
public String toString () {
	return "ImageLoaderEvent {source=" + source + " imageData=" + imageData + " incrementCount=" + incrementCount + " endOfImage=" + endOfImage + "}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
}

}
