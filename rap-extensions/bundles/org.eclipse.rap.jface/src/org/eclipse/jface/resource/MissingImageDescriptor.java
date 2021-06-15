/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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

/**
 * The image descriptor for a missing image.
 * <p>
 * Use <code>MissingImageDescriptor.getInstance</code> to
 * access the singleton instance maintained in an
 * internal state variable. 
 * </p>
 */
class MissingImageDescriptor extends ImageDescriptor {
    private static MissingImageDescriptor instance;

    /**
     * Constructs a new missing image descriptor.
     */
    private MissingImageDescriptor() {
        super();
    }

    /* (non-Javadoc)
     * Method declared on ImageDesciptor.
     */
    public ImageData getImageData() {
        return DEFAULT_IMAGE_DATA;
    }

    /**
     * Returns the shared missing image descriptor instance.
     *
     * @return the image descriptor for a missing image
     */
    static MissingImageDescriptor getInstance() {
        if (instance == null) {
            instance = new MissingImageDescriptor();
        }
        return instance;
    }
}
