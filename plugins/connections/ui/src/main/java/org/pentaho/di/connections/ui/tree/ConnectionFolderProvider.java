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

import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.connections.ConnectionDetails;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.tree.LeveledTreeNode;
import org.pentaho.di.ui.core.widget.tree.TreeNode;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.tree.TreeFolderProvider;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.eclipse.swt.graphics.Image;


/**
 * Created by bmorrise on 7/6/18.
 */
public class ConnectionFolderProvider extends TreeFolderProvider {

  private static final Class<?> PKG = ConnectionFolderProvider.class;
  public static final String STRING_VFS_CONNECTIONS = BaseMessages.getString( PKG, "VFSConnectionsTree.Title" );

  public ConnectionFolderProvider() {
  }

  @Override
  public void refresh( Optional<AbstractMeta> meta, TreeNode treeNode, String filter ) {
    try {
      Set<String> bowlNames = new HashSet<>();
      Bowl currentBowl = Spoon.getInstance().getManagementBowl();
      Bowl globalBowl = Spoon.getInstance().getGlobalManagementBowl();

      if ( !currentBowl.equals( globalBowl ) ) {
        ConnectionManager bowlCM = currentBowl.getManager( ConnectionManager.class );
        for ( String name : bowlCM.getNames() ) {
          if ( !filterMatch( name, filter ) ) {
            continue;
          }
          bowlNames.add( name );
          createTreeNode( treeNode, name, GUIResource.getInstance().getImageSlaveTree(),
                          LeveledTreeNode.LEVEL.PROJECT, currentBowl.getLevelDisplayName(), false );
        }
      }

      ConnectionManager globalCM = globalBowl.getManager( ConnectionManager.class );
      for ( String name : globalCM.getNames() ) {
        if ( !filterMatch( name, filter ) ) {
          continue;
        }
        createTreeNode( treeNode, name, GUIResource.getInstance().getImageSlaveTree(), LeveledTreeNode.LEVEL.GLOBAL,
                globalBowl.getLevelDisplayName(), containsIgnoreCase( bowlNames, name ) );
      }
    } catch ( KettleException e ) {
      new ErrorDialog( Spoon.getInstance().getShell(),
              BaseMessages.getString( PKG, "Spoon.ErrorDialog.Title" ),
              BaseMessages.getString( PKG, "Spoon.ErrorDialog.ErrorFetchingVFSConnections" ),
              e
      );
    }
  }

  @Override
  public String getTitle() {
    return STRING_VFS_CONNECTIONS;
  }

  @Override
  public Class getType() {
    return ConnectionDetails.class;
  }

  @Override
  public void create( Optional<AbstractMeta> meta, TreeNode parent ) {
    Repository repository = Spoon.getInstance().getRepository();
    if( repository != null && repository.getUserInfo() != null && repository.getUserInfo().isAdmin() != null
      && Boolean.FALSE.equals( repository.getUserInfo().isAdmin() ) ) {
      return;
    }
    refresh( meta, createTreeNode( parent, getTitle(), getTreeImage() ), null );

  }

  public TreeNode createTreeNode( TreeNode parent, String name, Image image, LeveledTreeNode.LEVEL level, String levelDisplayName,
                                  boolean overridden ) {
    LeveledTreeNode childTreeNode = new LeveledTreeNode( name, level, levelDisplayName, overridden );
    childTreeNode.setImage( image );

    parent.addChild( childTreeNode );
    return childTreeNode;
  }
}
