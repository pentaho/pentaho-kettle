/*******************************************************************************
 * Copyright (c) 2013, 2017 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.fileupload;

public class UploadSizeLimitExceededException extends Exception {

  private final long sizeLimit;
  private final String fileName;

  /**
   * Constructs a <code>UploadSizeLimitExceededException</code> with permitted size.
   *
   * @param sizeLimit The maximum permitted file upload size in bytes.
   * @param fileName The name of the uploaded file when the execption occurs.
   *
   * @since 3.3
   */
  public UploadSizeLimitExceededException( long sizeLimit, String fileName ) {
    this.sizeLimit = sizeLimit;
    this.fileName = fileName;
  }

  /**
   * Returns the maximum permitted file upload size in bytes.
   */
  public long getSizeLimit() {
    return sizeLimit;
  }

  /**
   * Return the name of the uploaded file when the execption occurs.
   */
  public String getFileName() {
    return fileName;
  }

}
