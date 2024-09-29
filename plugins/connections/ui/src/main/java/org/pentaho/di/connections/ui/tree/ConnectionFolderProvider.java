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
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.tree.TreeNode;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.tree.TreeFolderProvider;
import org.pentaho.metastore.locator.api.MetastoreLocator;

/**
 * Created by bmorrise on 7/6/18.
 */
public class ConnectionFolderProvider extends TreeFolderProvider {

  private static final Class<?> PKG = ConnectionFolderProvider.class;
  public static final String STRING_VFS_CONNECTIONS = BaseMessages.getString( PKG, "VFSConnectionsTree.Title" );
  private ConnectionManager connectionManager;

  public ConnectionFolderProvider( MetastoreLocator metastoreLocator ) {
    this.connectionManager = ConnectionManager.getInstance();
    connectionManager.setMetastoreSupplier( metastoreLocator::getMetastore );
  }

  @Override
  public void refresh( AbstractMeta meta, TreeNode treeNode, String filter ) {
    for ( String name : connectionManager.getNames() ) {
      if ( !filterMatch( name, filter ) ) {
        continue;
      }
      super.createTreeNode( treeNode, name, GUIResource.getInstance().getImageSlaveTree() );
    }
  }

  @Override
  public String getTitle() {
    return STRING_VFS_CONNECTIONS;
  }

  @Override
  public void create( AbstractMeta meta, TreeNode parent ) {
    Repository repository = Spoon.getInstance().getRepository();
    if( repository != null && repository.getUserInfo() != null && repository.getUserInfo().isAdmin() != null
      && Boolean.FALSE.equals( repository.getUserInfo().isAdmin() ) ) {
      return;
    }
    refresh( meta, createTreeNode( parent, getTitle(), getTreeImage() ), null );

  }
}
