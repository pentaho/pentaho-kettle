/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.graphics;


import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.SerializableCompatibility;

/**
 * Instances of this class describe the color data used by an image.
 * <p>
 * Depending on the depth of the image, the PaletteData can take one
 * of two forms, indicated by the isDirect field:
 * </p>
 * <dl>
 * <dt>
 * <em>isDirect is false</em>
 * </dt>
 * <dd>
 * If isDirect is <code>false</code>, this palette is an indexed
 * palette which maps pixel values to RGBs. The actual RGB values
 * may be retrieved by using the getRGBs() method.
 * </dd>
 * <dt>
 * <em>isDirect is true</em>
 * </dt>
 * <dd>
 * If isDirect is <code>true</code>, this palette is a direct color
 * palette. Instead of containing RGB values, it contains red,
 * green and blue mask and shift information which indicates how
 * the color components may be extracted from a given pixel.
 * This means that the RGB value is actually encoded in the pixel value.
 * <p>
 * In this case, the shift data is the number of bits required to shift
 * the RGB value to the left in order to align the high bit of the
 * corresponding mask with the high bit of the first byte. This number
 * may be negative, so care must be taken when shifting. For example,
 * with a red mask of 0xFF0000, the red shift would be -16. With a red
 * mask of 0x1F, the red shift would be 3.
 * </p>
 * </dd>
 * </dl>
 *
 * @see Image
 * @see RGB
 * @since 1.3
 */
 
@SuppressWarnings("all")
public final class PaletteData implements SerializableCompatibility {

    /**
     * true if the receiver is a direct palette, 
     * and false otherwise
     */
    public boolean isDirect;
    
    /**
     * the RGB values for an indexed palette, where the
     * indices of the array correspond to pixel values
     */
    public RGB[] colors;
    
    /**
     * the red mask for a direct palette
     */
    public int redMask;
    
    /**
     * the green mask for a direct palette
     */
    public int greenMask;
    
    /**
     * the blue mask for a direct palette
     */
    public int blueMask;
    
    /**
     * the red shift for a direct palette
     */
    public int redShift;
    
    /**
     * the green shift for a direct palette
     */
    public int greenShift;
    
    /**
     * the blue shift for a direct palette
     */
    public int blueShift;

/**
 * Constructs a new indexed palette given an array of RGB values.
 *
 * @param colors the array of <code>RGB</code>s for the palette
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the argument is null</li>
 * </ul>
 */
public PaletteData(RGB... colors) {
    if (colors == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
    this.colors = colors;
    this.isDirect = false;
}

/**
 * Constructs a new direct palette given the red, green and blue masks.
 *
 * @param redMask the red mask
 * @param greenMask the green mask
 * @param blueMask the blue mask
 */
public PaletteData(int redMask, int greenMask, int blueMask) {
    this.redMask = redMask;
    this.greenMask = greenMask;
    this.blueMask = blueMask;
    this.isDirect = true;
    this.redShift = shiftForMask(redMask);
    this.greenShift = shiftForMask(greenMask);
    this.blueShift = shiftForMask(blueMask);
}

/**
 * Returns the pixel value corresponding to the given <code>RGB</code>.
 *
 * @param rgb the RGB to get the pixel value for
 * @return the pixel value for the given RGB
 * 
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the argument is null</li>
 *    <li>ERROR_INVALID_ARGUMENT - if the RGB is not found in the palette</li>
 * </ul>
 */
public int getPixel(RGB rgb) {
    if (rgb == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
    if (isDirect) {
        int pixel = 0;
        pixel |= (redShift < 0 ? rgb.red << -redShift : rgb.red >>> redShift) & redMask;
        pixel |= (greenShift < 0 ? rgb.green << -greenShift : rgb.green >>> greenShift) & greenMask;
        pixel |= (blueShift < 0 ? rgb.blue << -blueShift : rgb.blue >>> blueShift) & blueMask;
        return pixel;
    } else {
        for (int i = 0; i < colors.length; i++) {
            if (colors[i].equals(rgb)) return i;
        }
        /* The RGB did not exist in the palette */
        SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        return 0;
    }
}

/**
 * Returns an <code>RGB</code> corresponding to the given pixel value.
 *
 * @param pixel the pixel to get the RGB value for
 * @return the RGB value for the given pixel
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the argument is null</li>
 *    <li>ERROR_INVALID_ARGUMENT - if the pixel does not exist in the palette</li>
 * </ul>
 */
public RGB getRGB(int pixel) {
    if (isDirect) {
        int r = pixel & redMask;
        r = (redShift < 0) ? r >>> -redShift : r << redShift;
        int g = pixel & greenMask;
        g = (greenShift < 0) ? g >>> -greenShift : g << greenShift;
        int b = pixel & blueMask;
        b = (blueShift < 0) ? b >>> -blueShift : b << blueShift;
        return new RGB(r, g, b);
    } else {
        if (pixel < 0 || pixel >= colors.length) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        return colors[pixel];
    }
}

/**
 * Returns all the RGB values in the receiver if it is an
 * indexed palette, or null if it is a direct palette.
 *
 * @return the <code>RGB</code>s for the receiver or null
 */
public RGB[] getRGBs() {
    return colors;
}

/**
 * Computes the shift value for a given mask.
 *
 * @param mask the mask to compute the shift for
 * @return the shift amount
 *
 * @see PaletteData
 */
int shiftForMask(int mask) {
    for (int i = 31; i >= 0; i--) { 
        if (((mask >> i) & 0x1) != 0) return 7 - i;
    }
    return 32;
}

}
