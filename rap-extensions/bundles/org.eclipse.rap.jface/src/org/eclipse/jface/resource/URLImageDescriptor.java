/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *    EclipseSource - adaptation for RAP
 ******************************************************************************/
package org.eclipse.jface.resource;

import static org.eclipse.rap.rwt.internal.service.ContextProvider.getApplicationContext;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.Policy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.internal.graphics.ImageFactory;

/**
 * An ImageDescriptor that gets its information from a URL. This class is not
 * public API. Use ImageDescriptor#createFromURL to create a descriptor that
 * uses a URL.
 */
class URLImageDescriptor extends ImageDescriptor {
	/**
	 * Constant for the file protocol for optimized loading
	 */ 
	private static final String FILE_PROTOCOL = "file";  //$NON-NLS-1$
    private URL url;

    /**
     * Creates a new URLImageDescriptor.
	 * 
	 * @param url
	 *            The URL to load the image from. Must be non-null.
     */
    URLImageDescriptor(URL url) {
        this.url = url;
    }

	/*
	 * (non-Javadoc) Method declared on Object.
     */
    public boolean equals(Object o) {
        if (!(o instanceof URLImageDescriptor)) {
            return false;
        }
		return ((URLImageDescriptor) o).url.toExternalForm().equals(this.url.toExternalForm());
    }

	/*
	 * (non-Javadoc) Method declared on ImageDesciptor. Returns null if the
	 * image data cannot be read.
     */
    public ImageData getImageData() {
        ImageData result = null;
        InputStream in = getStream();
        if (in != null) {
            try {
                result = new ImageData(in);
            } catch (SWTException e) {
                if (e.code != SWT.ERROR_INVALID_IMAGE) {
					throw e;
                // fall through otherwise
				}
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
					Policy.getLog().log(
							new Status(IStatus.ERROR, Policy.JFACE, e
									.getLocalizedMessage(), e));
                }
            }
        }
        return result;
    }

    /**
	 * Returns a stream on the image contents. Returns null if a stream could
	 * not be opened.
	 * 
     * @return the stream for loading the data
     */
    protected InputStream getStream() {
        try {
            return new BufferedInputStream(url.openStream());
        } catch (IOException e) {
            return null;
        }
    }

	/*
	 * (non-Javadoc) Method declared on Object.
     */
    public int hashCode() {
        return url.toExternalForm().hashCode();
    }

	/*
	 * (non-Javadoc) Method declared on Object.
     */
    /**
	 * The <code>URLImageDescriptor</code> implementation of this
	 * <code>Object</code> method returns a string representation of this
	 * object which is suitable only for debugging.
     */
    public String toString() {
        return "URLImageDescriptor(" + url + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

  // RAP [bm] alternative to ImageData for performance reasons
  public Image createImage( boolean returnMissingImageOnError, Device device ) {
    String path = url.toString();
    String schema = "bundleentry://"; //$NON-NLS-1$
    int pos = path.indexOf( schema );
    if( pos != -1 ) {
      path = path.substring( pos + schema.length() );
    }
    schema = "bundleresource://"; //$NON-NLS-1$
    pos = path.indexOf( schema );
    if( pos != -1 ) {
      path = path.substring( pos + schema.length() );
    }
    schema = "platform:/"; //$NON-NLS-1$
    pos = path.indexOf( schema );
    if( pos != -1 ) {
      path = path.substring( pos + schema.length() );
    }
    Image result = null;
    InputStream stream = getStream();
    if( stream != null ) {
      try {
        result = getApplicationContext().getImageFactory().createImage( device, path, stream );
      } finally {
        try {
          stream.close();
        } catch( IOException e ) {
          Policy.getLog().log( new Status( IStatus.ERROR,
                                           Policy.JFACE,
                                           e.getMessage(),
                                           e ) );
        }
      }
    } else if( returnMissingImageOnError ) {
      try {
        result = new Image( device, DEFAULT_IMAGE_DATA );
      } catch ( SWTException nextException ) {
        result = null;
      }
    }
    return result;
  }
}
