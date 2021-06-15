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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.Policy;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.internal.graphics.ImageFactory;

/**
 * An image descriptor that loads its image information from a file.
 */
class FileImageDescriptor extends ImageDescriptor {

    /**
	 * The class whose resource directory contain the file, or <code>null</code>
	 * if none.
     */
    private Class location;

    /**
     * The name of the file.
     */
    private String name;

    /**
	 * Creates a new file image descriptor. The file has the given file name and
	 * is located in the given class's resource directory. If the given class is
	 * <code>null</code>, the file name must be absolute.
     * <p>
	 * Note that the file is not accessed until its <code>getImageDate</code>
	 * method is called.
     * </p>
     *
	 * @param clazz
	 *            class for resource directory, or <code>null</code>
	 * @param filename
	 *            the name of the file
     */
    FileImageDescriptor(Class clazz, String filename) {
        this.location = clazz;
        this.name = filename;
    }

	/*
	 * (non-Javadoc) Method declared on Object.
     */
    public boolean equals(Object o) {
        if (!(o instanceof FileImageDescriptor)) {
            return false;
        }
        FileImageDescriptor other = (FileImageDescriptor) o;
        if (location != null) {
            if (!location.equals(other.location)) {
                return false;
            }
        } else {
            if (other.location != null) {
                return false;
            }
        }
        return name.equals(other.name);
    }

	/**
	 * @see org.eclipse.jface.resource.ImageDescriptor#getImageData() The
	 *      FileImageDescriptor implementation of this method is not used by
	 *      {@link ImageDescriptor#createImage(boolean, Device)} as of version
	 *      3.4 so that the SWT OS optimised loading can be used.
     */
    public ImageData getImageData() {
        InputStream in = getStream();
        ImageData result = null;
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
					// System.err.println(getClass().getName()+".getImageData():
					// "+
                    //  "Exception while closing InputStream : "+e);
                }
            }
        }
        return result;
    }

    // RAP [bm] alternative to ImageData for performance reasons
    public Image createImage(boolean returnMissingImageOnError, Device device) {
      Image result = null;
      if( location != null ) {
        String path = location.getPackage().getName().replace( '.', '/' )
                    + "/" //$NON-NLS-1$
                    + name;
        InputStream inputStream = getStream();
        if( inputStream != null ) {
          try {
            ImageFactory imageFactory = getApplicationContext().getImageFactory();
            result = imageFactory.createImage( device, path, inputStream );
          } finally {
            try {
              inputStream.close();
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
      }
      return result;
    }

    /**
	 * Returns a stream on the image contents. Returns null if a stream could
	 * not be opened.
     *
	 * @return the buffered stream on the file or <code>null</code> if the
	 *         file cannot be found
     */
    private InputStream getStream() {
        InputStream is = null;

        if (location != null) {
            is = location.getResourceAsStream(name);

        } else {
            try {
                is = new FileInputStream(name);
            } catch (FileNotFoundException e) {
                return null;
            }
        }
        if (is == null) {
			return null;
		}
			return new BufferedInputStream(is);

    }

	/*
	 * (non-Javadoc) Method declared on Object.
     */
    public int hashCode() {
        int code = name.hashCode();
        if (location != null) {
            code += location.hashCode();
        }
        return code;
    }

	/*
	 * (non-Javadoc) Method declared on Object.
     */
    /**
	 * The <code>FileImageDescriptor</code> implementation of this
	 * <code>Object</code> method returns a string representation of this
	 * object which is suitable only for debugging.
     */
    public String toString() {
        return "FileImageDescriptor(location=" + location + ", name=" + name + ")";//$NON-NLS-3$//$NON-NLS-2$//$NON-NLS-1$
    }

//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see org.eclipse.jface.resource.ImageDescriptor#createImage(boolean,
//	 *      org.eclipse.swt.graphics.Device)
//	 */
//	public Image createImage(boolean returnMissingImageOnError, Device device) {
//		String path = getFilePath();
//		if (path == null)
//			return createDefaultImage(returnMissingImageOnError, device);
//		try {			
//			return new Image(device, path);
//		} catch (SWTException exception) {
//			//if we fail try the default way using a stream
//		}
//		return super.createImage(returnMissingImageOnError, device);
//	}
//
//	/**
//	 * Return default image if returnMissingImageOnError is true.
//	 * 
//	 * @param device
//	 * @return Image or <code>null</code>
//	 */
//	private Image createDefaultImage(boolean returnMissingImageOnError,
//			Device device) {
//		try {
//			if (returnMissingImageOnError)
//				return new Image(device, DEFAULT_IMAGE_DATA);
//		} catch (SWTException nextException) {
//			return null;
//		}
//		return null;
//	}
//
//	/**
//	 * Returns the filename for the ImageData.
//	 * 
//	 * @return {@link String} or <code>null</code> if the file cannot be found
//	 */
//	private String getFilePath() {
//
//		if (location == null)
//			return new Path(name).toOSString();
//
//		URL resource = location.getResource(name);
//
//		if (resource == null)
//			return null;
//		try {
//			if (!InternalPolicy.OSGI_AVAILABLE) {// Stand-alone case
//
//				return new Path(resource.getFile()).toOSString();
//			}
//			return new Path(FileLocator.toFileURL(resource).getPath()).toOSString();
//		} catch (IOException e) {
//			Policy.logException(e);
//			return null;
//		}
//	}
}
