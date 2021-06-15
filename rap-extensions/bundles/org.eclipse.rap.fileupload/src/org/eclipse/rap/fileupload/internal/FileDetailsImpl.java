/*******************************************************************************
 * Copyright (c) 2011, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.fileupload.internal;

import org.eclipse.rap.fileupload.FileDetails;


public final class FileDetailsImpl implements FileDetails {

  private final String fileName;
  private final String contentType;

  public FileDetailsImpl( String fileName, String contentType ) {
    this.fileName = fileName;
    this.contentType = contentType;
  }

  @Override
  public String getFileName() {
    return fileName;
  }

  @Override
  public String getContentType() {
    return contentType;
  }

}
