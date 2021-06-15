/*******************************************************************************
 * Copyright (c) 2011, 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.fileupload;

import java.util.EventObject;


/**
 * Event object that provides information on a file upload. The source of this kind of events is
 * always a file upload handler.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @see FileUploadListener
 */
public abstract class FileUploadEvent extends EventObject {

  private static final long serialVersionUID = 1L;

  protected FileUploadEvent( FileUploadHandler source ) {
    super( source );
  }

  /**
   * Array with details about successfully uploaded files.
   *
   * @return an array with details about successfully uploaded files.
   */
  public abstract FileDetails[] getFileDetails();

  /**
   * The total number of bytes which are expected in total, as transmitted by the uploading client.
   * May be unknown.
   *
   * @return the content length in bytes or -1 if unknown
   */
  public abstract long getContentLength();

  /**
   * The number of bytes that have been received so far.
   *
   * @return the number of bytes received
   */
  public abstract long getBytesRead();

  /**
   * If the upload has failed, this method will return the exception that has occurred.
   *
   * @return the exception if the upload has failed, <code>null</code> otherwise
   */
  public abstract Exception getException();

  protected void dispatchProgress() {
    ( ( FileUploadHandler )source ).getListeners().notifyUploadProgress( this );
  }

  protected void dispatchFinished() {
    ( ( FileUploadHandler )source ).getListeners().notifyUploadFinished( this );
  }

  protected void dispatchFailed() {
    ( ( FileUploadHandler )source ).getListeners().notifyUploadFailed( this );
  }

}
