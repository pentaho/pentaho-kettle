/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.resource;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;

/**
 * Abstract base class for image descriptors that synthesize an image from other
 * images in order to simulate the effect of custom drawing. For example, this
 * could be used to superimpose a red bar dexter symbol across an image to
 * indicate that something was disallowed.
 * <p>
 * Subclasses must implement the <code>getSize</code> and <code>fill</code>
 * methods. Little or no work happens until the image descriptor's image is
 * actually requested by a call to <code>createImage</code> (or to
 * <code>getImageData</code> directly).
 * </p>
 */
public abstract class CompositeImageDescriptor extends ImageDescriptor {

	/**
	 * The image data for this composite image.
	 */
	private ImageData imageData;

	/**
	 * Constructs an uninitialized composite image.
	 */
	protected CompositeImageDescriptor() {
	}

	/**
	 * Draw the composite images.
	 * <p>
	 * Subclasses must implement this framework method to paint images within
	 * the given bounds using one or more calls to the <code>drawImage</code>
	 * framework method.
	 * </p>
	 *
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 */
	protected abstract void drawCompositeImage(int width, int height);

	/**
	 * Draws the given source image data into this composite image at the given
	 * position.
	 * <p>
	 * Call this internal framework method to superimpose another image atop
	 * this composite image.
	 * </p>
	 *
	 * @param src
	 *            the source image data
	 * @param ox
	 *            the x position
	 * @param oy
	 *            the y position
	 */
	final protected void drawImage(ImageData src, int ox, int oy) {
		ImageData dst = imageData;
		PaletteData srcPalette = src.palette;
		ImageData srcMask = null;
		int alphaMask = 0, alphaShift = 0;
		if (src.maskData != null) {
			srcMask = src.getTransparencyMask ();
			if (src.depth == 32) {
				alphaMask = ~(srcPalette.redMask | srcPalette.greenMask | srcPalette.blueMask);
				while (alphaMask != 0 && ((alphaMask >>> alphaShift) & 1) == 0) alphaShift++;
			}
		}
		for (int srcY = 0, dstY = srcY + oy; srcY < src.height; srcY++, dstY++) {
			for (int srcX = 0, dstX = srcX + ox; srcX < src.width; srcX++, dstX++) {
				if (!(0 <= dstX && dstX < dst.width && 0 <= dstY && dstY < dst.height)) continue;
				int srcPixel = src.getPixel(srcX, srcY);
				int srcAlpha = 255;
				if (src.maskData != null) {
					if (src.depth == 32) {
						srcAlpha = (srcPixel & alphaMask) >>> alphaShift;
						if (srcAlpha == 0) {
							srcAlpha = srcMask.getPixel(srcX, srcY) != 0 ? 255 : 0;
						}
					} else {
						if (srcMask.getPixel(srcX, srcY) == 0) srcAlpha = 0;
					}
				} else if (src.transparentPixel != -1) {
					if (src.transparentPixel == srcPixel) srcAlpha = 0;
				} else if (src.alpha != -1) {
					srcAlpha = src.alpha;
				} else if (src.alphaData != null) {
					srcAlpha = src.getAlpha(srcX, srcY);
				}
				if (srcAlpha == 0) continue;
				int srcRed, srcGreen, srcBlue;
				if (srcPalette.isDirect) {
					srcRed = srcPixel & srcPalette.redMask;
					srcRed = (srcPalette.redShift < 0) ? srcRed >>> -srcPalette.redShift : srcRed << srcPalette.redShift;
					srcGreen = srcPixel & srcPalette.greenMask;
					srcGreen = (srcPalette.greenShift < 0) ? srcGreen >>> -srcPalette.greenShift : srcGreen << srcPalette.greenShift;
					srcBlue = srcPixel & srcPalette.blueMask;
					srcBlue = (srcPalette.blueShift < 0) ? srcBlue >>> -srcPalette.blueShift : srcBlue << srcPalette.blueShift;
				} else {
					RGB rgb = srcPalette.getRGB(srcPixel);
					srcRed = rgb.red;
					srcGreen = rgb.green;
					srcBlue = rgb.blue;
				}
				int dstRed, dstGreen, dstBlue, dstAlpha;
				if (srcAlpha == 255) {
					dstRed = srcRed;
					dstGreen = srcGreen;
					dstBlue= srcBlue;
					dstAlpha = srcAlpha;
				} else {
					int dstPixel = dst.getPixel(dstX, dstY);
					dstAlpha = dst.getAlpha(dstX, dstY);
					dstRed = (dstPixel & 0xFF) >>> 0;
					dstGreen = (dstPixel & 0xFF00) >>> 8;
					dstBlue = (dstPixel & 0xFF0000) >>> 16;
					dstRed += (srcRed - dstRed) * srcAlpha / 255;
					dstGreen += (srcGreen - dstGreen) * srcAlpha / 255;
					dstBlue += (srcBlue - dstBlue) * srcAlpha / 255;
					dstAlpha += (srcAlpha - dstAlpha) * srcAlpha / 255;
				}
				dst.setPixel(dstX, dstY, ((dstRed & 0xFF) << 0) | ((dstGreen & 0xFF) << 8) | ((dstBlue & 0xFF) << 16));
				dst.setAlpha(dstX, dstY, dstAlpha);
			}
		}
	}

	/*
	 * (non-Javadoc) Method declared on ImageDesciptor.
	 */
	public ImageData getImageData() {
		Point size = getSize();

		/* Create a 24 bit image data with alpha channel */
		imageData = new ImageData(size.x, size.y, 24, new PaletteData(0xFF, 0xFF00, 0xFF0000));
		imageData.alphaData = new byte[imageData.width * imageData.height];

		drawCompositeImage(size.x, size.y);

		/* Detect minimum transparency */
		boolean transparency = false;
		byte[] alphaData = imageData.alphaData;
		for (int i = 0; i < alphaData.length; i++) {
			int alpha = alphaData[i] & 0xFF;
			if (!(alpha == 0 || alpha == 255)) {
				/* Full alpha channel transparency */
				return imageData;
			}
			if (!transparency && alpha == 0) transparency = true;
		}
		if (transparency) {
			/* Reduce to 1-bit alpha channel transparency */
			PaletteData palette = new PaletteData(new RGB[]{new RGB(0, 0, 0), new RGB(255, 255, 255)});
			ImageData mask = new ImageData(imageData.width, imageData.height, 1, palette);
			for (int y = 0; y < mask.height; y++) {
				for (int x = 0; x < mask.width; x++) {
					mask.setPixel(x, y, imageData.getAlpha(x, y) == 255 ? 1 : 0);
				}
			}
		} else {
			/* no transparency */
			imageData.alphaData = null;
		}
		return imageData;
	}

	/**
	 * Return the transparent pixel for the receiver.
	 * <strong>NOTE</strong> This value is not currently in use in the
	 * default implementation.
	 * @return int
	 */
	protected int getTransparentPixel() {
		return 0;
	}

	/**
	 * Return the size of this composite image.
	 * <p>
	 * Subclasses must implement this framework method.
	 * </p>
	 *
	 * @return the x and y size of the image expressed as a point object
	 */
	protected abstract Point getSize();

	/**
	 * @param imageData The imageData to set.
	 */
	protected void setImageData(ImageData imageData) {
		this.imageData = imageData;
	}
}
