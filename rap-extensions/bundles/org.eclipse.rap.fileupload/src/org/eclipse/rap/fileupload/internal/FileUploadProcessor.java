/*******************************************************************************
 * Copyright (c) 2011, 2017 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.fileupload.internal;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.eclipse.rap.fileupload.FileDetails;
import org.eclipse.rap.fileupload.FileUploadHandler;
import org.eclipse.rap.fileupload.FileUploadReceiver;
import org.eclipse.rap.fileupload.UploadSizeLimitExceededException;
import org.eclipse.rap.fileupload.UploadTimeLimitExceededException;


final class FileUploadProcessor {

  private final FileUploadHandler handler;
  private final FileUploadTracker tracker;
  private String fileName;
  private long deadline;

  FileUploadProcessor( FileUploadHandler handler ) {
    this.handler = handler;
    tracker = new FileUploadTracker( handler );
    deadline = -1;
  }

  void handleFileUpload( HttpServletRequest request, HttpServletResponse response )
    throws IOException
  {
    if( handler.getUploadTimeLimit() > 0 ) {
      deadline = System.currentTimeMillis() + handler.getUploadTimeLimit();
    }
    try {
      ServletFileUpload upload = createUpload();
      FileItemIterator iter = upload.getItemIterator( request );
      while( iter.hasNext() ) {
        FileItemStream item = iter.next();
        if( !item.isFormField() ) {
          receive( item );
        }
      }
      if( tracker.isEmpty() ) {
        String errorMessage = "No file upload data found in request";
        tracker.setException( new Exception( errorMessage ) );
        tracker.handleFailed();
        response.sendError( HttpServletResponse.SC_BAD_REQUEST, errorMessage );
      } else {
        tracker.handleFinished();
      }
    } catch( Exception exception ) {
      Throwable cause = exception.getCause();
      if( cause instanceof FileSizeLimitExceededException ) {
        long sizeLimit = handler.getMaxFileSize();
        exception = new UploadSizeLimitExceededException( sizeLimit, fileName );
      } else if( cause instanceof UploadTimeLimitExceededException ) {
        exception = ( UploadTimeLimitExceededException )cause;
      }
      tracker.setException( exception );
      tracker.handleFailed();
      int errorCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
      if( exception instanceof UploadSizeLimitExceededException ) {
        errorCode = HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE;
      } else if( exception instanceof UploadTimeLimitExceededException ) {
        errorCode = HttpServletResponse.SC_REQUEST_TIMEOUT;
      }
      response.sendError( errorCode, exception.getMessage() );
    }
  }

  private ServletFileUpload createUpload() {
    ServletFileUpload upload = new ServletFileUpload();
    upload.setFileSizeMax( handler.getMaxFileSize() );
    upload.setProgressListener( createProgressListener() );
    return upload;
  }

  private ProgressListener createProgressListener() {
    ProgressListener result = new ProgressListener() {
      long prevTotalBytesRead = -1;
      @Override
      public void update( long totalBytesRead, long contentLength, int item ) {
        // Depending on the servlet engine and other environmental factors,
        // this listener may be notified for every network packet, so don't notify unless there
        // is an actual increase.
        if ( totalBytesRead > prevTotalBytesRead ) {
          if( deadline > 0 && System.currentTimeMillis() > deadline ) {
            long timeLimit = handler.getUploadTimeLimit();
            Exception exception = new UploadTimeLimitExceededException( timeLimit, fileName );
            throw new RuntimeException( exception );
          }
          prevTotalBytesRead = totalBytesRead;
          tracker.setContentLength( contentLength );
          tracker.setBytesRead( totalBytesRead );
          tracker.handleProgress();
        }
      }
    };
    return result;
  }

  private void receive( FileItemStream item ) throws IOException {
    InputStream stream = item.openStream();
    try {
      fileName = stripFileName( item.getName() );
      String contentType = item.getContentType();
      FileDetails details = new FileDetailsImpl( fileName, contentType );
      FileUploadReceiver receiver = handler.getReceiver();
      receiver.receive( stream, details );
      tracker.addFile( details );
    } finally {
      stream.close();
    }
  }

  private static String stripFileName( String name ) {
    String result = name;
    int lastSlash = result.lastIndexOf( '/' );
    if( lastSlash != -1 ) {
      result = result.substring( lastSlash + 1 );
    } else {
      int lastBackslash = result.lastIndexOf( '\\' );
      if( lastBackslash != -1 ) {
        result = result.substring( lastBackslash + 1 );
      }
    }
    return result;
  }

}
