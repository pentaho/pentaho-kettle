/*******************************************************************************
 * Copyright (c) 2013, 2019 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.widgets;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.rap.fileupload.DiskFileUploadReceiver;
import org.eclipse.rap.fileupload.FileUploadEvent;
import org.eclipse.rap.fileupload.FileUploadHandler;
import org.eclipse.rap.fileupload.FileUploadListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Display;


public class FileUploadRunnable implements Runnable {

  static enum State { WAITING, UPLOADING, FINISHED, FAILED }

  private final Display display;
  private final UploadPanel uploadPanel;
  private final ProgressCollector progressCollector;
  private final Uploader uploader;
  private final FileUploadHandler handler;
  private final UploadProgressListener listener;
  private final AtomicReference<State> state;
  private final Object lock;

  public FileUploadRunnable( UploadPanel uploadPanel,
                             ProgressCollector progressCollector,
                             Uploader uploader,
                             FileUploadHandler handler )
  {
    this.uploadPanel = uploadPanel;
    this.progressCollector = progressCollector;
    this.uploader = uploader;
    this.handler = handler;
    display = uploadPanel.getDisplay();
    state = new AtomicReference<>( State.WAITING );
    lock = new Object();
    listener = new UploadProgressListener();
    setupFileUploadHandler();
    uploadPanel.updateIcons( State.WAITING );
  }

  @Override
  public void run() {
    asyncExec( new Runnable() {
      @Override
      public void run() {
        uploader.submit( handler.getUploadUrl() );
      }
    } );
    if( !display.isDisposed() ) {
      doWait();
    }
    asyncExec( new Runnable() {
      @Override
      public void run() {
        uploader.dispose();
        handler.removeUploadListener( listener );
        handler.dispose();
      }
    } );
  }

  private void setupFileUploadHandler() {
    handler.addUploadListener( listener );
    uploadPanel.addDisposeListener( new DisposeListener() {
      @Override
      public void widgetDisposed( DisposeEvent event ) {
        doNotify();
        handler.removeUploadListener( listener );
        handler.dispose();
      }
    } );
  }

  void handleProgress( long bytesRead, long contentLength ) {
    if( state.compareAndSet( State.WAITING, State.UPLOADING ) ) {
      uploadPanel.updateIcons( State.UPLOADING );
    }
    double fraction = bytesRead / ( double )contentLength;
    int percent = ( int )Math.floor( fraction * 100 );
    progressCollector.updateProgress( percent );
  }

  void handleFinished( List<String> targetFileNames ) {
    state.set( State.FINISHED );
    uploadPanel.updateIcons( State.FINISHED );
    progressCollector.resetToolTip();
    progressCollector.updateCompletedFiles( targetFileNames );
  }

  void handleFailed( Exception exception ) {
    state.set( State.FAILED );
    uploadPanel.updateIcons( State.FAILED );
    progressCollector.resetToolTip();
    if( exception != null ) {
      progressCollector.addException( exception );
      uploadPanel.updateTexts( exception );
    }
  }

  State getState() {
    return state.get();
  }

  private void doWait() {
    synchronized( lock ) {
      try {
        lock.wait();
      } catch( @SuppressWarnings( "unused" ) InterruptedException exception ) {
        // allow executor to properly shutdown
      }
    }
  }

  private void doNotify() {
    synchronized( lock ) {
      lock.notify();
    }
  }

  private void asyncExec( Runnable runnable ) {
    if( !display.isDisposed() ) {
      display.asyncExec( runnable );
    }
  }

  private final class UploadProgressListener implements FileUploadListener {

    @Override
    public void uploadProgress( final FileUploadEvent event ) {
      asyncExec( new Runnable() {
        @Override
        public void run() {
          handleProgress( event.getBytesRead(), event.getContentLength() );
        }
      } );
    }

    @Override
    public void uploadFinished( FileUploadEvent event ) {
      FileUploadHandler uploadHandler = ( FileUploadHandler )event.getSource();
      DiskFileUploadReceiver receiver = ( DiskFileUploadReceiver )uploadHandler.getReceiver();
      final List<String> targetFileNames = getTargetFileNames( receiver );
      asyncExec( new Runnable() {
        @Override
        public void run() {
          handleFinished( targetFileNames );
        }
      } );
      doNotify();
    }

    @Override
    public void uploadFailed( final FileUploadEvent event ) {
      asyncExec( new Runnable() {
        @Override
        public void run() {
          handleFailed( event.getException() );
        }
      } );
      doNotify();
    }

    private List<String> getTargetFileNames( DiskFileUploadReceiver receiver ) {
      List<String> result = new ArrayList<>();
      for( File targetFile : receiver.getTargetFiles() ) {
        result.add( targetFile.getAbsolutePath() );
      }
      return result;
    }

  }

}
