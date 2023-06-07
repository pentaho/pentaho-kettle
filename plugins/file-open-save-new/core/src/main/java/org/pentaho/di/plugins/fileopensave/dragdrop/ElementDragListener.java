/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2023 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package org.pentaho.di.plugins.fileopensave.dragdrop;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.plugins.fileopensave.api.providers.File;
import org.pentaho.di.plugins.fileopensave.api.providers.Utils;
import org.pentaho.di.plugins.fileopensave.dialog.FileOpenSaveDialog;
import org.pentaho.di.plugins.fileopensave.providers.repository.RepositoryFileProvider;
import org.pentaho.di.plugins.fileopensave.providers.repository.model.RepositoryFile;

/**
 * Supports dragging elements from a structured viewer.
 */
public class ElementDragListener extends DragSourceAdapter {
  private StructuredViewer viewer;
  private FileOpenSaveDialog fileOpenSaveDialog;
  private LogChannelInterface log;

  public ElementDragListener( StructuredViewer viewer, FileOpenSaveDialog fileOpenSaveDialog,
                              LogChannelInterface log ) {
    this.viewer = viewer;
    this.fileOpenSaveDialog = fileOpenSaveDialog;
    this.log = log;
  }

  /**
   * Method declared on DragSourceListener
   */
  @Override
  public void dragFinished( DragSourceEvent event ) {
    if ( !event.doit ) {
      return;
    }

    fileOpenSaveDialog.refreshDisplay( null );
  }

  /**
   * Method declared on DragSourceListener
   */
  @Override
  public void dragSetData( DragSourceEvent event ) {
    IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
    Object[] rawElements = selection.toList().toArray( new Object[ selection.size() ] );
    for ( int i = 0; i < rawElements.length; i++ ) {
      if ( rawElements[ i ] instanceof RepositoryFile && "Recents".equals(
        ( (RepositoryFile) rawElements[ i ] ).getRoot() ) ) {
        //Recents in the repo are RepositoryFile elements.  They are RecentFiles when not connected to a repo.  Once
        // serialized they will look like any other respository file except the extension will be missing so
        // we won't be able to tell if a ktr or job.  We therefore have to modify it here to make it look like a real
        // repository object is being dragged.
        RepositoryFile repoFile = ( (RepositoryFile) rawElements[ i ] );
        String extension = Utils.getExtension( repoFile.getPath() );
        if ( extension.isEmpty() ) {
          if ( repoFile.getType().equals( RepositoryFileProvider.TRANSFORMATION ) ) {
            repoFile.setPath( repoFile.getPath() + File.KTR );
          } else if ( repoFile.getType().equals( RepositoryFileProvider.JOB ) ) {
            repoFile.setPath( repoFile.getPath() + File.KJB );
          }
        }
      }
    }
    if ( ElementTransfer.getInstance().isSupportedType( event.dataType ) ) {
      event.data = rawElements;
    }
  }

  /**
   * Method declared on DragSourceListener
   */
  @Override
  public void dragStart( DragSourceEvent event ) {
    event.doit = !viewer.getSelection().isEmpty();
  }

  public void printEvent( DragSourceEvent e ) {
    if ( log.isBasic() ) {
      StringBuilder sb = new StringBuilder();
      sb.append( "EventData [" );
      sb.append( "widget: " );
      sb.append( e.widget );
      sb.append( ", time: " );
      sb.append( e.time );
      sb.append( ", operation: " );
      sb.append( e.detail );
      sb.append( ", type: " );
      sb.append( e.dataType != null ? e.dataType.type : 0 );
      sb.append( ", doit: " );
      sb.append( e.doit );
      sb.append( ", data: " );
      sb.append( e.data );
      sb.append( "]" );
      log.logBasic( sb.toString() );
    }
  }


}