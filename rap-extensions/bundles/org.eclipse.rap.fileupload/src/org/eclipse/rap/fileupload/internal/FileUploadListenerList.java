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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.rap.fileupload.FileUploadEvent;
import org.eclipse.rap.fileupload.FileUploadListener;


public final class FileUploadListenerList {

  private final Set<FileUploadListener> listeners;

  public FileUploadListenerList() {
    listeners = new HashSet<>();
  }

  public void addUploadListener( FileUploadListener listener ) {
    listeners.add( listener );
  }

  public void removeUploadListener( FileUploadListener listener ) {
    listeners.remove( listener );
  }

  public void notifyUploadProgress( FileUploadEvent event ) {
    for( FileUploadListener listener : listeners ) {
      listener.uploadProgress( event );
    }
  }

  public void notifyUploadFinished( FileUploadEvent event ) {
    for( FileUploadListener listener : listeners ) {
      listener.uploadFinished( event );
    }
  }

  public void notifyUploadFailed( FileUploadEvent event ) {
    for( FileUploadListener listener : listeners ) {
      listener.uploadFailed( event );
    }
  }

}
