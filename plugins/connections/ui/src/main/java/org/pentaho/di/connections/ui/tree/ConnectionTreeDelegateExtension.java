/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.connections.ui.tree;

import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.ui.spoon.TreeSelection;
import org.pentaho.di.ui.spoon.delegates.SpoonTreeDelegateExtension;
import org.pentaho.di.connections.vfs.VFSConnectionDetails;

import java.util.List;

@ExtensionPoint( id = "VFSConnectionTreeDelegateExtension", description = "",
  extensionPointId = "SpoonTreeDelegateExtension" )
public class ConnectionTreeDelegateExtension implements ExtensionPointInterface {

  public ConnectionTreeDelegateExtension() {

  }

  @Override public void callExtensionPoint( LogChannelInterface log, Object extension ) throws KettleException {
    SpoonTreeDelegateExtension treeDelExt = (SpoonTreeDelegateExtension) extension;

    int caseNumber = treeDelExt.getCaseNumber();
    AbstractMeta meta = treeDelExt.getTransMeta();
    String[] path = treeDelExt.getPath();
    List<TreeSelection> objects = treeDelExt.getObjects();

    TreeSelection object = null;

    if ( path[ 2 ].equals( ConnectionFolderProvider.STRING_VFS_CONNECTIONS ) ) {
      switch ( caseNumber ) {
        case 3:
          object = new TreeSelection( path[ 2 ], VFSConnectionDetails.class, meta );
          break;
        case 4:
          try {
            final String name = path[ 3 ];
            object = new TreeSelection( name, new ConnectionTreeItem( name ), meta );
          } catch ( Exception e ) {
            // Do Nothing
          }
          break;
      }
    }

    if ( object != null ) {
      objects.add( object );
    }
  }
}

