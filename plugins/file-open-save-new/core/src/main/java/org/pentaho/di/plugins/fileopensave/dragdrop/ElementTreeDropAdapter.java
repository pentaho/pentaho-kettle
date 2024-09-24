/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2023-2024 by Hitachi Vantara : http://www.pentaho.com
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
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.MessageBox;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.plugins.fileopensave.api.overwrite.OverwriteStatus;
import org.pentaho.di.plugins.fileopensave.api.providers.Entity;
import org.pentaho.di.plugins.fileopensave.api.providers.EntityType;
import org.pentaho.di.plugins.fileopensave.api.providers.File;
import org.pentaho.di.plugins.fileopensave.providers.vfs.model.VFSDirectory;

import java.util.Arrays;

/**
 * Supports dropping Elements into a tree viewer.
 */
public class ElementTreeDropAdapter extends ViewerDropAdapter {
  Object lastTarget;
  TreeViewer viewer;
  LogChannelInterface log;

  public ElementTreeDropAdapter( TreeViewer viewer, LogChannelInterface log ) {
    super( viewer );
    this.viewer = viewer;
    this.log = log;
  }

  /**
   * Method declared on ViewerDropAdapter
   */
  public boolean performDrop( Object data ) {
    int location = getCurrentLocation();
    Object genericTarget = getCurrentTarget() == null ? getViewer().getInput() : getCurrentTarget();
    if ( !( genericTarget instanceof Entity )
      || ( genericTarget instanceof VFSDirectory && !( (VFSDirectory) genericTarget ).isCanAddChildren() ) ) {
      MessageBox errorBox = new MessageBox( getViewer().getControl().getShell(), SWT.ICON_ERROR | SWT.OK );
      errorBox.setMessage( "Error.  This item is not a valid drop item. Please pick a real folder to drop on." );
      errorBox.open();
      return false;
    }
    Element target = new Element( genericTarget );

    log.logDebug( "TreeDrop: last target element was \"" + target.getPath() + "\" location was " + location );
    if ( location == ViewerDropAdapter.LOCATION_AFTER || location == ViewerDropAdapter.LOCATION_BEFORE
      || ( location == ViewerDropAdapter.LOCATION_ON && target.getEntityType().isFile() ) ) {
      File targetFile = (File) genericTarget;
      String parent = targetFile.getParent();
      if ( parent != null ) {
        if ( parent.endsWith( "\\" ) || parent.endsWith( "/" ) ) {
          parent = parent.substring( 0, parent.length() - 1 );
        }
        String name = parent.replaceAll( "^.*[\\/\\\\]", "" ); //Strip off the path leaving file name
        // Making parent of target the new actual target to use.
        target =
          new Element( name, target.calcParentEntityType(), parent, target.getProvider(), target.getRepositoryName() );
      }
    }

    Element[] toDrop;
    if ( data instanceof Element[] ) {
      // If here we are getting data from our file browser
      toDrop = (Element[]) data;
      for ( int i=0; i<toDrop.length; i++ ) {
        if ( toDrop[i].getEntityType().equals( EntityType.RECENT_FILE ) ) {
            //Convert recent files to real type, because the RecentProvider has no implementations
            toDrop[i] = toDrop[i].convertRecent();
        }
      }
    } else {
      // If here we are getting data from explorer
      toDrop =
        Arrays.stream( ( (String[]) data ) ).map( this::convertExplorerFileToElement ).toArray( Element[]::new );
    }
    //Send info to the drag and drop processor to do the work.
    VariableSpace variables = Variables.getADefaultVariableSpace();
    try {
      ElementDndProcessor.process( toDrop, target, ElementDndProcessor.ElementOperation.COPY, variables,
        new OverwriteStatus( getViewer().getControl().getShell() ), log );
    } catch ( Exception e ) {
      MessageBox errorBox = new MessageBox( getViewer().getControl().getShell(), SWT.ICON_ERROR | SWT.OK );
      errorBox.setMessage( "Error.  Cannot perform drag and drop function.  " + e.getMessage() );
      errorBox.open();
      return false;
    }

    //This is to select the target on the viewer
    File genericTargetObject = target.convertToFile( variables );
    IStructuredSelection selectionAsStructuredSelection = new StructuredSelection( genericTargetObject );
    viewer.setSelection( selectionAsStructuredSelection, true );
    return true;
  }

  /**
   * Method declared on ViewerDropAdapter
   */
  public boolean validateDrop( Object target, int op, TransferData type ) {
    lastTarget = target;
    return ElementTransfer.getInstance().isSupportedType( type ) || FileTransfer.getInstance().isSupportedType( type );
  }

  private Element convertExplorerFileToElement( String path ) {
    java.io.File file = new java.io.File( path );
    if ( file.exists() ) {
      if ( file.isDirectory() ) {
        return new Element( file.getName(), EntityType.LOCAL_DIRECTORY, path, "local" );
      } else {
        return new Element( file.getName(), EntityType.LOCAL_FILE, path, "local" );
      }
    }
    return null;
  }
}
