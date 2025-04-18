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


package org.pentaho.di.ui.spoon.tree.provider;

import org.eclipse.swt.graphics.Image;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.cluster.SlaveServerManagementInterface;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
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
    Bowl globalBowl = Spoon.getInstance().getGlobalManagementBowl();
    try {
      Set<String> projectSlaveServerNames = new HashSet<>();
      // Bowl specific
      if ( currentBowl != globalBowl ) {
        SlaveServerManagementInterface slaveServerManager = currentBowl.getManager( SlaveServerManagementInterface.class );
        List<SlaveServer> slaveServers = slaveServerManager.getAll();

        for ( SlaveServer sharedObject : slaveServers ) {
          if ( !filterMatch( sharedObject.getName(), filter ) ) {
            continue;
          }
          projectSlaveServerNames.add( sharedObject.getName() );
          createTreeNode( treeNode, sharedObject.getName(), guiResource.getImageSlaveTree(),
                  LeveledTreeNode.LEVEL.PROJECT, currentBowl.getLevelDisplayName(), false );
        }
      }

      // Global
      SlaveServerManagementInterface globalServerMgr = globalBowl.getManager( SlaveServerManagementInterface.class );
      Set<String> globalServerNames = new HashSet<>();
      List<SlaveServer> servers = globalServerMgr.getAll();
      for ( SlaveServer slaveServer : servers ) {
        if ( !filterMatch( slaveServer.getName(), filter ) ) {
          continue;
        }

        globalServerNames.add( slaveServer.getName() );
        createTreeNode( treeNode, slaveServer.getName(), guiResource.getImageSlaveTree(),
          LeveledTreeNode.LEVEL.GLOBAL, globalBowl.getLevelDisplayName(), containsIgnoreCase( projectSlaveServerNames, slaveServer.getName() ) );
      }

      // Local SlaveServers
      if ( meta.isPresent() ) {
        SlaveServerManagementInterface localServerMgr = meta.get().getSlaveServerManagementInterface();
        List<SlaveServer> localServers = localServerMgr.getAll();
        for ( SlaveServer sharedObjectInterface : localServers ) {
          if ( !filterMatch( sharedObjectInterface.getName(), filter ) ) {
            continue;
          }

          createTreeNode( treeNode, sharedObjectInterface.getName(), guiResource.getImageSlaveTree(),
            LeveledTreeNode.LEVEL.FILE, LeveledTreeNode.LEVEL_FILE_DISPLAY_NAME, containsIgnoreCase( projectSlaveServerNames, sharedObjectInterface.getName() ) ||
                    containsIgnoreCase( globalServerNames, sharedObjectInterface.getName() ) );
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
  public Class<?> getType() {
    return SlaveServer.class;
  }

  public TreeNode createTreeNode( TreeNode parent, String name, Image image, LeveledTreeNode.LEVEL level, String levelDisplayName,
                                 boolean overridden ) {
    LeveledTreeNode childTreeNode = new LeveledTreeNode( name, level, levelDisplayName, overridden );
    childTreeNode.setImage( image );

    parent.addChild( childTreeNode );
    return childTreeNode;
  }
}
