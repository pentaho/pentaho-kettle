/*******************************************************************************
 * Copyright (c) 2014, 2019 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.widgets;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.ClientFile;
import org.eclipse.rap.rwt.client.service.ClientFileUploader;


public class UploaderService implements Uploader {

  private final ClientFile[] clientFiles;
  private String uploadId;

  public UploaderService( ClientFile[] clientFiles ) {
    this.clientFiles = clientFiles;
  }

  @Override
  public void submit( String url ) {
    ClientFileUploader service = RWT.getClient().getService( ClientFileUploader.class );
    if( service != null ) {
      uploadId = service.submit( url, clientFiles );
    }
  }

  @Override
  public void dispose() {
    ClientFileUploader service = RWT.getClient().getService( ClientFileUploader.class );
    if( service != null && uploadId != null ) {
      service.abort( uploadId );
    }
  }

}
