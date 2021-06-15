// RAP [bm]: Image with style bits
///*******************************************************************************
// * Copyright (c) 2005, 2006 IBM Corporation and others.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// *
// * Contributors:
// *     IBM Corporation - initial API and implementation
// *******************************************************************************/
//package org.eclipse.jface.resource;
//
//import org.eclipse.rwt.graphics.Graphics;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.SWTException;
//import org.eclipse.swt.graphics.Device;
//import org.eclipse.swt.graphics.Image;
//import org.eclipse.swt.graphics.ImageData;
//import org.eclipse.swt.widgets.Display;
//
///**
// * An image descriptor which creates images based on another ImageDescriptor, but with
// * additional SWT flags. Note that this is only intended for compatibility. 
// * 
// * @since 1.0
// */
//final class DerivedImageDescriptor extends ImageDescriptor {
//
//    private ImageDescriptor original;
//    private int flags;
//    
//    /**
//     * Create a new image descriptor
//     * @param original the original one
//     * @param swtFlags flags to be used when image is created {@link Image#Image(Device, Image, int)}
//     * @see SWT#IMAGE_COPY
//     * @see SWT#IMAGE_DISABLE
//     * @see SWT#IMAGE_GRAY
//     */
//    public DerivedImageDescriptor(ImageDescriptor original, int swtFlags) {
//        this.original = original;
//        flags = swtFlags;
//    }
//    
//    public Object createResource(Device device) throws DeviceResourceException {
//        try {
//            return internalCreateImage(device);
//        } catch (SWTException e) {
//            throw new DeviceResourceException(this, e);
//        }
//    }
//    
//    public Image createImage(Device device) {
//        return internalCreateImage(device);
//    }
//    
//    public int hashCode() {
//        return original.hashCode() + flags;
//    }
//    
//    public boolean equals(Object arg0) {
//        if (arg0 instanceof DerivedImageDescriptor) {
//            DerivedImageDescriptor desc = (DerivedImageDescriptor)arg0;
//            
//            return desc.original == original && flags == desc.flags;
//        }
//        
//        return false;
//    }
//    
//    /**
//     * Creates a new Image on the given device. Note that we defined a new
//     * method rather than overloading createImage since this needs to be
//     * called by getImageData(), and we want to be absolutely certain not
//     * to cause infinite recursion if the base class gets refactored. 
//     *
//     * @param device device to create the image on
//     * @return a newly allocated Image. Must be disposed by calling image.dispose().
//     */
//    private final Image internalCreateImage(Device device) {
//        Image originalImage = original.createImage(device);
//        Image result = new Image(device, originalImage, flags);
//        original.destroyResource(originalImage);
//        return result;
//    }
//    
//    public ImageData getImageData() {
//        Image image = internalCreateImage(Display.getCurrent());
//        ImageData result = image.getImageData();
//        image.dispose();
//        return result;
//    }
//
//}
