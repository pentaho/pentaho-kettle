/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.connections.ui.tree;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.ui.spoon.TreeSelection;
import org.pentaho.di.ui.spoon.delegates.SpoonTreeDelegateExtension;
import org.pentaho.di.connections.vfs.VFSConnectionDetails;
import org.pentaho.di.ui.core.widget.tree.LeveledTreeNode;

import java.util.List;
import org.eclipse.swt.widgets.TreeItem;

@ExtensionPoint( id = "VFSConnectionTreeDelegateExtension", description = "",
  extensionPointId = "SpoonTreeDelegateExtension" )
public class ConnectionTreeDelegateExtension implements ExtensionPointInterface {

  public ConnectionTreeDelegateExtension() {

  }

  @Override public void callExtensionPoint( LogChannelInterface log, Object extension ) throws KettleException {
    SpoonTreeDelegateExtension treeDelExt = (SpoonTreeDelegateExtension) extension;

    int caseNumber = treeDelExt.getCaseNumber();
    String[] path = treeDelExt.getPath();
    List<TreeSelection> objects = treeDelExt.getObjects();

    TreeSelection object = null;

    if ( path[ 1 ].equals( ConnectionFolderProvider.STRING_VFS_CONNECTIONS ) ) {
      switch ( caseNumber ) {
        case 2:
          object = new TreeSelection( treeDelExt.getTreeItem(), path[ 1 ], VFSConnectionDetails.class );
          break;
        case 3:
          try {
            TreeItem treeItem = treeDelExt.getTreeItem();
            String name = LeveledTreeNode.getName( treeItem );
            LeveledTreeNode.LEVEL level = LeveledTreeNode.getLevel( treeItem );
            object = new TreeSelection( treeItem, name, new ConnectionTreeItem( name, level ) );
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

