/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.resource;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.RGB;

/**
 * Describes a color by its RGB values.
 * 
 * @since 1.0
 */
class RGBColorDescriptor extends ColorDescriptor {

    private RGB color;
    
    /**
     * Color being copied, or null if none
     */
    private Color originalColor = null;
    
    /**
     * Creates a new RGBColorDescriptor given some RGB values
     * 
     * @param color RGB values (not null)
     */
    public RGBColorDescriptor(RGB color) {
        this.color = color;
    }
    
	/**
     * Creates a new RGBColorDescriptor that describes an existing color. 
     * 
     * @since 1.0
     *
     * @param originalColor a color to describe
     */
    public RGBColorDescriptor(Color originalColor) {
        this(originalColor.getRGB());
        this.originalColor = originalColor;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof RGBColorDescriptor) {
            RGBColorDescriptor other = (RGBColorDescriptor) obj;
            
            return other.color.equals(color) && other.originalColor == originalColor;
        }
        
        return false;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return color.hashCode();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.resources.ColorDescriptor#createColor()
     */
    public Color createColor(Device device) {
        // If this descriptor is wrapping an existing color, then we can return the original color
        // if this is the same device.
        if (originalColor != null) {
            // If we're allocating on the same device as the original color, return the original.
            if (originalColor.getDevice() == device) {
                return originalColor;
            }            
        }
        
        return new Color(device, color);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.resource.ColorDescriptor#destroyColor(org.eclipse.swt.graphics.Color)
     */
    public void destroyColor(Color toDestroy) {
        if (toDestroy == originalColor) {
            return;
        }
        
        toDestroy.dispose();
    }
}
