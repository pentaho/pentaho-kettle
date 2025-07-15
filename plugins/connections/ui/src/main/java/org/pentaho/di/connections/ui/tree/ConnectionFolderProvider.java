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

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Display;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.connections.ConnectionDetails;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.ConstUI;
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
    String levelName;
    Image image;
    String connectionName;
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
          ConnectionDetails connectionDetails = bowlCM.getConnectionDetails( name );
          if ( connectionDetails.getName().equals( ConnectionManager.STRING_REPO_CONNECTION ) ) {
            continue;
          }
          bowlNames.add( name );
          createTreeNode( treeNode, name, GUIResource.getInstance().getImageSlaveTree(),
                          LeveledTreeNode.LEVEL.PROJECT, currentBowl.getLevelDisplayName(), false, false );
        }
      }

      ConnectionManager globalCM = globalBowl.getManager( ConnectionManager.class );
      for ( String name : globalCM.getNames() ) {
        connectionName = name;
        if ( !filterMatch( name, filter ) ) {
          continue;
        }

        LeveledTreeNode.LEVEL level = LeveledTreeNode.LEVEL.GLOBAL;
        levelName = globalBowl.getLevelDisplayName();
        image  = GUIResource.getInstance().getImageSlaveTree();
        ConnectionDetails connectionDetails = globalCM.getConnectionDetails( name );
        // For repo connection, set the name, level and image
        if ( connectionDetails.getName().equals( ConnectionManager.STRING_REPO_CONNECTION ) ) {
          connectionName = ConnectionManager.STRING_REPO_CONNECTION;
          level = LeveledTreeNode.LEVEL.DEFAULT;
          levelName = LeveledTreeNode.LEVEL_DEFAULT_DISPLAY_NAME;
          image = getRunConfigurationImage( GUIResource.getInstance(), "images/PentahoRepository.svg" );
        }

        createTreeNode( treeNode, connectionName, image, level, levelName, containsIgnoreCase( bowlNames, name ),
                            ( level == LeveledTreeNode.LEVEL.DEFAULT ) ? true : false );

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
    if ( repository != null && repository.getUserInfo() != null && repository.getUserInfo().isAdmin() != null
      && Boolean.FALSE.equals( repository.getUserInfo().isAdmin() ) ) {
      return;
    }
    refresh( meta, createTreeNode( parent, getTitle(), getTreeImage() ), null );

  }

  public TreeNode createTreeNode( TreeNode parent, String name, Image image, LeveledTreeNode.LEVEL level, String levelDisplayName,
                                  boolean overridden, boolean displayAsDiabled ) {
    LeveledTreeNode childTreeNode = new LeveledTreeNode( name, level, levelDisplayName, overridden );
    childTreeNode.setImage( image );

    parent.addChild( childTreeNode );
    // If the level is DEFAULT, it is readonly and is displayed as disabled
    if ( displayAsDiabled ) {
      childTreeNode.setForeground( getDisabledColor() );
    }
    return childTreeNode;
  }

  private Image getRunConfigurationImage( GUIResource guiResource, String file ) {
    return guiResource
      .getImage( file, getClass().getClassLoader(), ConstUI.MEDIUM_ICON_SIZE, ConstUI.MEDIUM_ICON_SIZE );
  }

  private Color getDisabledColor() {
    Device device = Display.getCurrent();
    return new Color( device, 188, 188, 188 );
  }
}
