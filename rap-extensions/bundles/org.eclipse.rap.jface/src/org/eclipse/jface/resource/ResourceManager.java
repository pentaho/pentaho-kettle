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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.Policy;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;

/**
 * This class manages SWT resources. It manages reference-counted instances of resources
 * such as Fonts, Images, and Colors, and allows them to be accessed using descriptors.
 * Everything allocated through the registry should also be disposed through the registry.
 * Since the resources are shared and reference counted, they should never be disposed
 * directly.
 * <p>
 * ResourceManager handles correct allocation and disposal of resources. It differs from 
 * the various JFace *Registry classes, which also map symbolic IDs onto resources. In 
 * general, you should use a *Registry class to map IDs onto descriptors, and use a 
 * ResourceManager to convert the descriptors into real Images/Fonts/etc.
 * </p>
 * 
 * @since 1.0
 */
public abstract class ResourceManager implements Serializable {
    
	/**
	 * List of Runnables scheduled to run when the ResourceManager is disposed.
	 * null if empty.
	 */
    private List disposeExecs = null;
    
    /**
     * Returns the Device for which this ResourceManager will create resources 
     * 
     * @since 1.0
     *
     * @return the Device associated with this ResourceManager
     */
    public abstract Device getDevice();
    
    /**
     * Returns the resource described by the given descriptor. If the resource already
     * exists, the reference count is incremented and the exiting resource is returned.
     * Otherwise, a new resource is allocated. Every call to this method should have
     * a corresponding call to {@link #destroy(DeviceResourceDescriptor)}.
     * 
     * <p>If the resource is intended to live for entire lifetime of the resource manager, 
     * a subsequent call to {@link #destroy(DeviceResourceDescriptor)} may be omitted and the
     * resource will be cleaned up when the resource manager is disposed. This pattern
     * is useful for short-lived {@link LocalResourceManager}s, but should never be used 
     * with the global resource manager since doing so effectively leaks the resource.</p>
     * 
     * <p>The resources returned from this method are reference counted and may be shared 
     * internally with other resource managers. They should never be disposed outside of the 
     * ResourceManager framework, or it will cause exceptions in other code that shares
     * them. For example, never call {@link org.eclipse.swt.graphics.Resource#dispose()} 
     * on anything returned from this method.</p>
     * 
     * <p>Callers may safely downcast the result to the resource type associated with 
     * the descriptor. For example, when given an ImageDescriptor, the return
     * value of this method will always be an Image.</p>
     * 
     * @since 1.0 
     *
     * @param descriptor descriptor for the resource to allocate
     * @return the newly allocated resource (not null)
     * @throws DeviceResourceException if unable to allocate the resource
     */
    public abstract Object create(DeviceResourceDescriptor descriptor);
    
    /**
     * Deallocates a resource previously allocated by {@link #create(DeviceResourceDescriptor)}. 
     * Descriptors are compared by equality, not identity. If the same resource was 
     * created multiple times, this may decrement a reference count rather than 
     * disposing the actual resource.  
     * 
     * @since 1.0 
     *
     * @param descriptor identifier for the resource
     */
    public abstract void destroy(DeviceResourceDescriptor descriptor);
    
    /**
     * <p>Returns a previously-allocated resource or allocates a new one if none
     * exists yet. The resource will remain allocated for at least the lifetime
     * of this resource manager. If necessary, the resource will be deallocated 
     * automatically when the resource manager is disposed.</p>
     * 
     * <p>The resources returned from this method are reference counted and may be shared 
     * internally with other resource managers. They should never be disposed outside of the 
     * ResourceManager framework, or it will cause exceptions in other code that shares
     * them. For example, never call {@link org.eclipse.swt.graphics.Resource#dispose()}
     * on anything returned from this method.</p>
     * 
     * <p>
     * Callers may safely downcast the result to the resource type associated with 
     * the descriptor. For example, when given an ImageDescriptor, the return
     * value of this method may be downcast to Image.
     * </p>
     * 
     * <p>
     * This method should only be used for resources that should remain
     * allocated for the lifetime of the resource manager. To allocate shorter-lived
     * resources, manage them with <code>create</code>, and <code>destroy</code>
     * rather than this method.
     * </p>
     * 
     * <p>
     * This method should never be called on the global resource manager,
     * since all resources will remain allocated for the lifetime of the app and
     * will be effectively leaked.
     * </p>
     * 
     * @param descriptor identifier for the requested resource
     * @return the requested resource. Never null.
     * @throws DeviceResourceException if the resource does not exist yet and cannot
     * be created for any reason.
     * 
     * @since 1.0
     */
    public final Object get(DeviceResourceDescriptor descriptor) {
    	Object result = find(descriptor);
    	
    	if (result == null) {
    		result = create(descriptor);
    	}
    	
    	return result;
    }
    
    /**
     * <p>Creates an image, given an image descriptor. Images allocated in this manner must
     * be disposed by {@link #destroyImage(ImageDescriptor)}, and never by calling 
     * {@link Image#dispose()}.</p>
     * 
     * <p>
     * If the image is intended to remain allocated for the lifetime of the ResourceManager,
     * the call to destroyImage may be omitted and the image will be cleaned up automatically
     * when the ResourceManager is disposed. This should only be done with short-lived ResourceManagers,
     * as doing so with the global manager effectively leaks the resource.
     * </p>
     * 
     * @since 1.0 
     *
     * @param descriptor descriptor for the image to create
     * @return the Image described by this descriptor (possibly shared by other equivalent
     * ImageDescriptors)
     * @throws DeviceResourceException if unable to allocate the Image
     */
    public final Image createImage(ImageDescriptor descriptor) {
    	// Assertion added to help diagnose client bugs.  See bug #83711 and bug #90454.
    	Assert.isNotNull(descriptor);
    	
        return (Image)create(descriptor);
    }
    
    /**
     * Creates an image, given an image descriptor. Images allocated in this manner must
     * be disposed by {@link #destroyImage(ImageDescriptor)}, and never by calling 
     * {@link Image#dispose()}.
     * 
     * @since 1.0 
     *
     * @param descriptor descriptor for the image to create
     * @return the Image described by this descriptor (possibly shared by other equivalent
     * ImageDescriptors)
     */
    public final Image createImageWithDefault(ImageDescriptor descriptor) {
        if (descriptor == null) {
        	return getDefaultImage();
        }
        
        try {
			return (Image) create(descriptor);
		} catch (DeviceResourceException e) {
			Policy.getLog().log(
					new Status(IStatus.WARNING, "org.eclipse.jface", 0, //$NON-NLS-1$
							"The image could not be loaded: " + descriptor, //$NON-NLS-1$
							e));
			return getDefaultImage();
		} catch (SWTException e) {
			Policy.getLog().log(
					new Status(IStatus.WARNING, "org.eclipse.jface", 0, //$NON-NLS-1$
							"The image could not be loaded: " + descriptor, //$NON-NLS-1$
							e));
			return getDefaultImage();
		}
    }
    
    /**
     * Returns the default image that will be returned in the event that the intended
     * image is missing.
     * 
     * @since 1.0
     *
     * @return a default image that will be returned in the event that the intended
     * image is missing.
     */
    protected abstract Image getDefaultImage();

    /**
     * Undoes everything that was done by {@link #createImage(ImageDescriptor)}.
     * 
     * @since 1.0 
     *
     * @param descriptor identifier for the image to dispose
     */
    public final void destroyImage(ImageDescriptor descriptor) {
        destroy(descriptor);
    }

    /**
     * Allocates a color, given a color descriptor. Any color allocated in this
     * manner must be disposed by calling {@link #destroyColor(ColorDescriptor)}, 
     * or by an eventual call to {@link #dispose()}. {@link Color#dispose()} must
     * never been called directly on the returned color.
     * 
     * @since 1.0 
     *
     * @param descriptor descriptor for the color to create
     * @return the Color described by the given ColorDescriptor (not null)
     * @throws DeviceResourceException if unable to create the color
     */
    public final Color createColor(ColorDescriptor descriptor) {
        return (Color)create(descriptor);
    }

    /**
     * Allocates a color, given its RGB value. Any color allocated in this
     * manner must be disposed by calling {@link #destroyColor(RGB)}, 
     * or by an eventual call to {@link #dispose()}. {@link Color#dispose()} must
     * never been called directly on the returned color.
     * 
     * @since 1.0 
     *
     * @param descriptor descriptor for the color to create
     * @return the Color described by the given ColorDescriptor (not null)
     * @throws DeviceResourceException if unable to create the color
     */
    public final Color createColor(RGB descriptor) {
        return createColor(new RGBColorDescriptor(descriptor));
    }
    
    /**
     * Undoes everything that was done by a call to {@link #createColor(RGB)}.
     * 
     * @since 1.0 
     *
     * @param descriptor RGB value of the color to dispose
     */
    public final void destroyColor(RGB descriptor) {
        destroyColor(new RGBColorDescriptor(descriptor));
    }

    /**
     * Undoes everything that was done by a call to {@link #createColor(ColorDescriptor)}.
     * 
     * 
     * @since 1.0 
     *
     * @param descriptor identifier for the color to dispose
     */
    public final void destroyColor(ColorDescriptor descriptor) {
        destroy(descriptor);
    }
    
    /**
     * Returns the Font described by the given FontDescriptor. Any Font
     * allocated in this manner must be deallocated by calling disposeFont(...),
     * or by an eventual call to {@link #dispose()}.  The method {@link Font#dispose()}
     * must never be called directly on the returned font.
     * 
     * @since 1.0 
     *
     * @param descriptor description of the font to create
     * @return the Font described by the given descriptor
     * @throws DeviceResourceException if unable to create the font
     */
    public final Font createFont(FontDescriptor descriptor) {
        return (Font)create(descriptor);
    }
    
    /**
     * Undoes everything that was done by a previous call to {@link #createFont(FontDescriptor)}.
     * 
     * @since 1.0 
     *
     * @param descriptor description of the font to destroy
     */
    public final void destroyFont(FontDescriptor descriptor) {
        destroy(descriptor);
    }
    
    /**
     * Disposes any remaining resources allocated by this manager. 
     */
    public void dispose() {
        if (disposeExecs == null) {
            return;
        }
        
        // If one of the runnables throws an exception, we need to propagate it.
        // However, this should not prevent the remaining runnables from being 
        // notified. If any runnables throw an exception, we remember one of them
        // here and throw it at the end of the method.
        RuntimeException foundException = null;
        
        Runnable[] execs = (Runnable[]) disposeExecs.toArray(new Runnable[disposeExecs.size()]);
        for (int i = 0; i < execs.length; i++) {
            Runnable exec = execs[i];            
            
            try {
                exec.run();
            } catch (RuntimeException e) {
                // Ensure that we propagate an exception, but don't stop notifying
                // the remaining runnables.
                foundException = e;
            }
        }
        
        if (foundException != null) {
            // If any runnables threw an exception, propagate one of them.
            throw foundException;
        }
    }
    
    /**
     * Returns a previously allocated resource associated with the given descriptor, or
     * null if none exists yet. 
     * 
     * @since 1.0
     *
     * @param descriptor descriptor to find
     * @return a previously allocated resource for the given descriptor or null if none.
     */
    public abstract Object find(DeviceResourceDescriptor descriptor);
    
    /**
     * Causes the <code>run()</code> method of the runnable to
     * be invoked just before the receiver is disposed. The runnable
     * can be subsequently canceled by a call to <code>cancelDisposeExec</code>.
     * 
     * @param r runnable to execute.
     */
    public void disposeExec(Runnable r) {
        Assert.isNotNull(r);
        
        if (disposeExecs == null) {
            disposeExecs = new ArrayList();
        }
        
        disposeExecs.add(r);
    }
    
    /**
     * Cancels a runnable that was previously scheduled with <code>disposeExec</code>.
     * Has no effect if the given runnable was not previously registered with
     * disposeExec.
     * 
     * @param r runnable to cancel
     */
    public void cancelDisposeExec(Runnable r) {
        Assert.isNotNull(r);
        
        if (disposeExecs == null) {
            return;
        }
        
        disposeExecs.remove(r);
        
        if (disposeExecs.isEmpty()) {
            disposeExecs = null;
        }
    }
}
