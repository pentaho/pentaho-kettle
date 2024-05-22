/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.connections.ui.tree;

import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.connections.ConnectionDetails;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.tree.LeveledTreeNode;
import org.pentaho.di.ui.core.widget.tree.TreeNode;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.tree.TreeFolderProvider;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

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
      Bowl currentBowl = Spoon.getInstance().getBowl();
      if ( !currentBowl.equals( DefaultBowl.getInstance() ) ) {
        ConnectionManager bowlCM = currentBowl.getExplicitConnectionManager();
        for ( String name : bowlCM.getNames() ) {
          if ( !filterMatch( name, filter ) ) {
            continue;
          }
          bowlNames.add( name );
          createTreeNode( treeNode, name, GUIResource.getInstance().getImageSlaveTree(),
                          LeveledTreeNode.LEVEL.PROJECT, false );
        }
      }

      ConnectionManager globalCM = DefaultBowl.getInstance().getExplicitConnectionManager();
      for ( String name : globalCM.getNames() ) {
        if ( !filterMatch( name, filter ) ) {
          continue;
        }
        createTreeNode( treeNode, name, GUIResource.getInstance().getImageSlaveTree(), LeveledTreeNode.LEVEL.GLOBAL,
                        bowlNames.contains( name ) );
      }
    } catch ( MetaStoreException e ) {
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

  public TreeNode createTreeNode( TreeNode parent, String name, Image image, LeveledTreeNode.LEVEL level,
                                  boolean overridden ) {
    LeveledTreeNode childTreeNode = new LeveledTreeNode( name, level, overridden );
    childTreeNode.setImage( image );

    parent.addChild( childTreeNode );
    return childTreeNode;
  }
}
