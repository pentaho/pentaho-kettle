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

import org.pentaho.di.vfs.connections.ui.dialog.ConnectionDelegate;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.ui.core.widget.tree.LeveledTreeNode;
import org.pentaho.di.ui.spoon.SelectionTreeExtension;
import org.pentaho.di.ui.spoon.Spoon;
import org.eclipse.swt.widgets.TreeItem;

@ExtensionPoint( id = "ConnectionViewTreeExtension", description = "",
  extensionPointId = "SpoonViewTreeExtension" )
public class ConnectionViewTreeExtension implements ExtensionPointInterface {

  private ConnectionDelegate connectionDelegate;

  public ConnectionViewTreeExtension() {
    this.connectionDelegate = ConnectionDelegate.getInstance();
  }

  @Override public void callExtensionPoint( LogChannelInterface log, Object object ) throws KettleException {
    SelectionTreeExtension selectionTreeExtension = (SelectionTreeExtension) object;
    if ( selectionTreeExtension.getAction().equals( Spoon.EDIT_SELECTION_EXTENSION ) ) {
      if ( selectionTreeExtension.getSelection() instanceof ConnectionTreeItem ) {
        TreeItem treeItem = selectionTreeExtension.getTreeItem();
        String name = LeveledTreeNode.getName( treeItem );
        LeveledTreeNode.LEVEL level = LeveledTreeNode.getLevel( treeItem );

        connectionDelegate.openDialog( name, level );
      }
    }
  }
}
