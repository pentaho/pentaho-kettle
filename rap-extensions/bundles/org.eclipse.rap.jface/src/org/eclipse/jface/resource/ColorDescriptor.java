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
 * Lightweight descriptor for an SWT color. Each ColorDescriptor will create a particular SWT 
 * Color on demand. This object will be compared so hashCode(...) and equals(...) must 
 * return meaningful values.
 * 
 * @since 1.0
 */
public abstract class ColorDescriptor extends DeviceResourceDescriptor {
    
    /**
     * Creates a ColorDescriptor from an existing Color, given the Device associated
     * with the original Color. This is the usual way to convert a Color into
     * a ColorDescriptor. Note that the returned ColorDescriptor depends on the
     * original Color, and disposing the Color will invalidate the ColorDescriptor.
     * 
     * @deprecated use {@link ColorDescriptor#createFrom(Color)}
     * 
     * @since 1.0
     *
     * @param toCreate Color to convert into a ColorDescriptor.
     * @param originalDevice this must be the same Device that was passed into the
     * original Color's constructor.
     * @return a newly created ColorDescriptor that describes the given Color.
     */
    public static ColorDescriptor createFrom(Color toCreate, Device originalDevice) {
        return new RGBColorDescriptor(toCreate);
    }
    
    /**
     * Creates a ColorDescriptor from an existing color. 
     * 
     * The returned ColorDescriptor depends on the original Color. Disposing
     * the original colour while the color descriptor is still in use may cause 
     * SWT to throw a graphic disposed exception.
     * 
     * @since 1.0
     *
     * @param toCreate Color to generate a ColorDescriptor from
     * @return a newly created ColorDescriptor
     */
    public static ColorDescriptor createFrom(Color toCreate) {
        return new RGBColorDescriptor(toCreate);
    }
    
    /**
     * Returns a color descriptor for the given RGB values
     * @since 1.0 
     *
     * @param toCreate RGB values to create
     * @return a new ColorDescriptor
     */
    public static ColorDescriptor createFrom(RGB toCreate) {
        return new RGBColorDescriptor(toCreate);
    }
    
    /**
     * Returns the Color described by this descriptor.
     * 
     * @param device SWT device on which to allocate the Color
     * @return a newly allocated SWT Color object (never null)
     * @throws DeviceResourceException if unable to allocate the Color
     */
    public abstract Color createColor(Device device) throws DeviceResourceException;
    
    /**
     * Undoes whatever was done by createColor. 
     * 
     * @since 1.0 
     *
     * @param toDestroy a Color that was previously allocated by an equal ColorDescriptor
     */
    public abstract void destroyColor(Color toDestroy);
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.resource.DeviceResourceDescriptor#createResource(org.eclipse.swt.graphics.Device)
     */
    public final Object createResource(Device device) throws DeviceResourceException {
        return createColor(device);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.resource.DeviceResourceDescriptor#destroyResource(java.lang.Object)
     */
    public final void destroyResource(Object previouslyCreatedObject) {
        destroyColor((Color)previouslyCreatedObject);
    }
}
