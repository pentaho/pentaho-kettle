/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Steven Ketcham (sketcham@dsicdi.com) - Bug 42451
 *     [Dialogs] ImageRegistry throws null pointer exception in
 *     application with multiple Display's
 *******************************************************************************/
package org.eclipse.jface.resource;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

/**
 * An image registry maintains a mapping between symbolic image names 
 * and SWT image objects or special image descriptor objects which
 * defer the creation of SWT image objects until they are needed.
 * <p>
 * An image registry owns all of the image objects registered
 * with it, and automatically disposes of them when the SWT Display
 * that creates the images is disposed. Because of this, clients do not 
 * need to (indeed, must not attempt to) dispose of these images themselves.
 * </p>
 * <p>
 * Clients may instantiate this class (it was not designed to be subclassed).
 * </p>
 * <p>
 * Unlike the FontRegistry, it is an error to replace images. As a result
 * there are no events that fire when values are changed in the registry
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ImageRegistry {
    /**
     * display used when getting images
     */
    private Display display;

    private ResourceManager manager;

    private Map table;
    
    private Runnable disposeRunnable = new Runnable() {
        public void run() {
            dispose();
        }
    };
    
    /**
     * Contains the data for an entry in the registry. 
     */
    private static class Entry {
    	/** the image */
        protected Image image;

        /** the descriptor */
        protected ImageDescriptor descriptor;
    }
    
    private static class OriginalImageDescriptor extends ImageDescriptor {
        private Image original;
        private int refCount = 0;
        private Device originalDisplay;
        
        /**
         * @param original the original image
         * @param originalDisplay the device the image is part of
         */
        public OriginalImageDescriptor(Image original, Device originalDisplay) {
            this.original = original;
            this.originalDisplay = originalDisplay;
        }
        
        public Object createResource(Device device) throws DeviceResourceException {
            if (device == originalDisplay) {
                refCount++;
                return original;
            }
            return super.createResource(device);
        }
        
        public void destroyResource(Object toDispose) {
            if (original == toDispose) {
                refCount--;
                if (refCount == 0) {
                    original.dispose();
                    original = null;
                }
            } else {
                super.destroyResource(toDispose);
            }
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.resource.ImageDescriptor#getImageData()
         */
        public ImageData getImageData() {
            return original.getImageData();
        }
    }
        
    /**
     * Creates an empty image registry.
     * <p>
     * There must be an SWT Display created in the current 
     * thread before calling this method.
     * </p>
     */
    public ImageRegistry() {
        this(Display.getCurrent());
    }

    /**
     * Creates an empty image registry using the given resource manager to allocate images.
     * 
     * @param manager the resource manager used to allocate images
     * 
     * @since 1.0
     */
    public ImageRegistry(ResourceManager manager) {
        Assert.isNotNull(manager);
        Device dev = manager.getDevice();
        if (dev instanceof Display) {
            this.display = (Display)dev;
        }
        this.manager = manager;
        manager.disposeExec(disposeRunnable);
    }
    
    /**
     * Creates an empty image registry.
     * 
     * @param display this <code>Display</code> must not be 
     *        <code>null</code> and must not be disposed in order
     *        to use this registry
     */
    public ImageRegistry(Display display) {
        this(JFaceResources.getResources(display));
    }
    
    /**
     * Returns the image associated with the given key in this registry, 
     * or <code>null</code> if none.
     * 
     * @param key the key
     * @return the image, or <code>null</code> if none
     */
    public Image get(String key) {

        // can be null
        if (key == null) {
            return null;
        }
        
        if (display != null) {
            /**
             * NOTE, for backwards compatibility the following images are supported
             * here, they should never be disposed, hence we explicitly return them 
             * rather then registering images that SWT will dispose.  
             * 
             * Applications should go direclty to SWT for these icons.
             * 
             * @see Display.getSystemIcon(int ID)
             */
            int swtKey = -1;
            if (key.equals(Dialog.DLG_IMG_INFO)) {
                swtKey = SWT.ICON_INFORMATION;
            }
            if (key.equals(Dialog.DLG_IMG_QUESTION)) {
                swtKey = SWT.ICON_QUESTION;
            }
            if (key.equals(Dialog.DLG_IMG_WARNING)) {
                swtKey = SWT.ICON_WARNING;
            }
            if (key.equals(Dialog.DLG_IMG_ERROR)) {
                swtKey = SWT.ICON_ERROR;
            }
            // if we actually just want to return an SWT image do so without
            // looking in the registry
            if (swtKey != -1) {
                final Image[] image = new Image[1];
                final int id = swtKey;
                display.syncExec(new Runnable() {
                    public void run() {
                        image[0] = display.getSystemImage(id);
                    }
                });
                return image[0];
            }
        }

        Entry entry = getEntry(key);
        if (entry == null) {
            return null;
        }
        
        if (entry.image == null) {
            entry.image = manager.createImageWithDefault(entry.descriptor);
        }
        
        return entry.image;
    }

    /**
     * Returns the descriptor associated with the given key in this registry, 
     * or <code>null</code> if none.
     *
     * @param key the key
     * @return the descriptor, or <code>null</code> if none
     * @since 1.0
     */
    public ImageDescriptor getDescriptor(String key) {
        Entry entry = getEntry(key);
        if (entry == null) {
            return null;
        }
        
        return entry.descriptor;
    }

    /**
     * Adds (or replaces) an image descriptor to this registry. The first time
     * this new entry is retrieved, the image descriptor's image will be computed 
     * (via </code>ImageDescriptor.createImage</code>) and remembered. 
     * This method replaces an existing image descriptor associated with the 
     * given key, but fails if there is a real image associated with it.
     *
     * @param key the key
     * @param descriptor the ImageDescriptor
     * @exception IllegalArgumentException if the key already exists
     */
    public void put(String key, ImageDescriptor descriptor) {
        Entry entry = getEntry(key);
        if (entry == null) {
            entry = new Entry();
            getTable().put(key, entry);
        }
        
        if (entry.image != null) {
            throw new IllegalArgumentException(
                    "ImageRegistry key already in use: " + key); //$NON-NLS-1$            
        }
        
        entry.descriptor = descriptor;
    }

    /**
     * Adds an image to this registry.  This method fails if there
     * is already an image or descriptor for the given key.
     * <p>
     * Note that an image registry owns all of the image objects registered
     * with it, and automatically disposes of them when the SWT Display is disposed. 
     * Because of this, clients must not register an image object
     * that is managed by another object.
     * </p>
     *
     * @param key the key
     * @param image the image, should not be <code>null</code>
     * @exception IllegalArgumentException if the key already exists
     */
    public void put(String key, Image image) {
        Entry entry = getEntry(key);
        
        if (entry == null) {
            entry = new Entry();
            putEntry(key, entry);
        }
        
        if (entry.image != null || entry.descriptor != null) {
            throw new IllegalArgumentException(
                    "ImageRegistry key already in use: " + key); //$NON-NLS-1$            
        }
        
        // Check for a null image here, otherwise the problem won't appear
        // until dispose.
        // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=130315
        Assert.isNotNull(image, "Cannot register a null image."); //$NON-NLS-1$
        entry.image = image;
        entry.descriptor = new OriginalImageDescriptor(image, manager.getDevice());
        
        try {
            manager.create(entry.descriptor);
        } catch (DeviceResourceException e) {            
        }
    }

    /**
     * Removes an image from this registry.  
     * If an SWT image was allocated, it is disposed.
     * This method has no effect if there is no image or descriptor for the given key.
     * @param key the key
     */
    public void remove(String key) {
        ImageDescriptor descriptor = getDescriptor(key);
        if (descriptor != null) {
            manager.destroy(descriptor);
            getTable().remove(key);
        }
    }

    private Entry getEntry(String key) {
        return (Entry) getTable().get(key);
    }

    private void putEntry(String key, Entry entry) {
        getTable().put(key, entry);
    }

    private Map getTable() {
        if (table == null) {
            table = new HashMap(10);
        }
        return table;
    }
    
    /**
     * Disposes this image registry, disposing any images
     * that were allocated for it, and clearing its entries.
     * 
     * @since 1.0
     */
    public void dispose() {
        manager.cancelDisposeExec(disposeRunnable);
        
        if (table != null) {
            for (Iterator i = table.values().iterator(); i.hasNext();) {
                Entry entry = (Entry) i.next();
                if (entry.image != null) {
                    manager.destroyImage(entry.descriptor);
                }
            }
            table = null;
        }
        display = null;
    }
}
