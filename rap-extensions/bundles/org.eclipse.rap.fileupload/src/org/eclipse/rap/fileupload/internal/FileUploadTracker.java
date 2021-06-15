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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rap.fileupload.FileDetails;
import org.eclipse.rap.fileupload.FileUploadEvent;
import org.eclipse.rap.fileupload.FileUploadHandler;


final class FileUploadTracker {

  private final FileUploadHandler handler;
  private final List<FileDetails> files;
  private long contentLength;
  private long bytesRead;
  private Exception exception;

  FileUploadTracker( FileUploadHandler handler ) {
    this.handler = handler;
    files = new ArrayList<>();
  }

  void addFile( FileDetails details ) {
    files.add( details );
  }

  boolean isEmpty() {
    return files.isEmpty();
  }

  void setContentLength( long contentLength ) {
    this.contentLength = contentLength;
  }

  void setBytesRead( long bytesRead ) {
    this.bytesRead = bytesRead;
  }

  void setException( Exception exception ) {
    this.exception = exception;
  }

  void handleProgress() {
    new InternalFileUploadEvent( handler ).dispatchAsProgress();
  }

  void handleFinished() {
    new InternalFileUploadEvent( handler ).dispatchAsFinished();
  }

  void handleFailed() {
    new InternalFileUploadEvent( handler ).dispatchAsFailed();
  }

  private final class InternalFileUploadEvent extends FileUploadEvent {

    private static final long serialVersionUID = 1L;

    private InternalFileUploadEvent( FileUploadHandler source ) {
      super( source );
    }

    @Override
    public FileDetails[] getFileDetails() {
      return files.toArray( new FileDetails[ 0 ] );
    }

    @Override
    public long getContentLength() {
      return contentLength;
    }

    @Override
    public long getBytesRead() {
      return bytesRead;
    }

    @Override
    public Exception getException() {
      return exception;
    }

    void dispatchAsProgress() {
      super.dispatchProgress();
    }

    void dispatchAsFinished() {
      super.dispatchFinished();
    }

    void dispatchAsFailed() {
      super.dispatchFailed();
    }
  }
}
