/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.spoon.tree.provider;

import org.eclipse.swt.graphics.Image;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.cluster.SlaveServerManagementInterface;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.tree.LeveledTreeNode;
import org.pentaho.di.ui.core.widget.tree.TreeNode;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.tree.TreeFolderProvider;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created by bmorrise on 6/28/18.
 */
public class SlavesFolderProvider extends TreeFolderProvider {

  private static Class<?> PKG = Spoon.class;
  public static final String STRING_SLAVES = BaseMessages.getString( PKG, "Spoon.STRING_SLAVES" );
  private GUIResource guiResource;

  public SlavesFolderProvider( GUIResource guiResource ) {
    this.guiResource = guiResource;
  }

  public SlavesFolderProvider() {
    this( GUIResource.getInstance() );
  }

  @Override
  public void refresh( Optional<AbstractMeta> meta, TreeNode treeNode, String filter ) {
    Bowl currentBowl = Spoon.getInstance().getBowl();
    try {
      Set<String> projectSlaveServerNames = new HashSet<>();
      // Bowl specific
      if ( currentBowl != DefaultBowl.getInstance() ) {
        SlaveServerManagementInterface slaveServerManager = currentBowl.getManager( SlaveServerManagementInterface.class );
        List<SlaveServer> slaveServers = slaveServerManager.getAll();
        slaveServers.sort( ( s1, s2 ) -> String.CASE_INSENSITIVE_ORDER.compare( s1.getName(), s2.getName() ) );

        for ( SharedObjectInterface sharedObject : slaveServers ) {
          if ( !filterMatch( sharedObject.getName(), filter ) ) {
            continue;
          }
          projectSlaveServerNames.add( sharedObject.getName() );
          TreeNode childTreeNode = createTreeNode( treeNode, sharedObject.getName(), guiResource.getImageSlaveTree(),
                                  LeveledTreeNode.LEVEL.PROJECT, false );
        }
      }

      // Global
      SlaveServerManagementInterface globalServerMgr = DefaultBowl.getInstance().getManager( SlaveServerManagementInterface.class );
      Set<String> globalServerNames = new HashSet<>();
      List<SlaveServer> servers = globalServerMgr.getAll();
      servers.sort( ( s1, s2 ) -> String.CASE_INSENSITIVE_ORDER.compare( s1.getName(), s2.getName() ) );
      for ( SharedObjectInterface slaveServer : servers ) {
        if ( !filterMatch( slaveServer.getName(), filter ) ) {
          continue;
        }

        globalServerNames.add( slaveServer.getName() );
        TreeNode childTreeNode = createTreeNode( treeNode, slaveServer.getName(), guiResource.getImageSlaveTree(),
          LeveledTreeNode.LEVEL.GLOBAL, projectSlaveServerNames.contains( slaveServer.getName() ) );
      }

      // Local SlaveServers
      if ( meta.isPresent() ) {
        SlaveServerManagementInterface localServerMgr = meta.get().getSlaveServerManagementInterface();
        List<SlaveServer> localServers = localServerMgr.getAll();
        for ( SharedObjectInterface sharedObjectInterface : localServers ) {
          if ( !filterMatch( sharedObjectInterface.getName(), filter ) ) {
            continue;
          }

          TreeNode childTreeNode = createTreeNode( treeNode, sharedObjectInterface.getName(), guiResource.getImageSlaveTree(),
            LeveledTreeNode.LEVEL.LOCAL, projectSlaveServerNames.contains( sharedObjectInterface.getName() ) || globalServerNames.contains( sharedObjectInterface.getName() ) );
        }
      }
    } catch ( KettleException exception ) {
      new ErrorDialog( Spoon.getInstance().getShell(),
        BaseMessages.getString( PKG, "Spoon.ErrorDialog.Title" ),
        BaseMessages.getString( PKG, "Spoon.ErrorDialog.ErrorFetchingFromRepo.SlaveServers" ),
        exception );
    }
  }

  @Override
  public String getTitle() {
    return STRING_SLAVES;
  }

  @Override
  public Class getType() {
    return SlaveServer.class;
  }

  public TreeNode createTreeNode( TreeNode parent, String name, Image image, LeveledTreeNode.LEVEL level,
                                 boolean overridden ) {
    LeveledTreeNode childTreeNode = new LeveledTreeNode( name, level, overridden );
    childTreeNode.setImage( image );

    parent.addChild( childTreeNode );
    return childTreeNode;
  }
}
