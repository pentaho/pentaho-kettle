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

import java.io.Serializable;

import org.eclipse.swt.graphics.Device;

/**
 * Instances of this class can allocate and dispose SWT resources. Each
 * instance describes a particular resource (such as a Color, Font, or Image)
 * and can create and destroy that resource on demand. DeviceResourceDescriptors
 * are managed by a ResourceRegistry.
 * 
 * @see org.eclipse.jface.resource.ResourceManager
 * 
 * @since 1.0
 */
public abstract class DeviceResourceDescriptor implements Serializable {
    /**
     * Creates the resource described by this descriptor
     * 
     * @since 1.0 
     *
     * @param device the Device on which to allocate the resource
     * @return the newly allocated resource (not null)
     * @throws DeviceResourceException if unable to allocate the resource
     */
    public abstract Object createResource(Device device);
    
    /**
     * Undoes everything that was done by a previous call to create(...), given
     * the object that was returned by create(...).
     * 
     * @since 1.0 
     *
     * @param previouslyCreatedObject an object that was returned by an equal 
     * descriptor in a previous call to createResource(...).
     */
    public abstract void destroyResource(Object previouslyCreatedObject);
}
