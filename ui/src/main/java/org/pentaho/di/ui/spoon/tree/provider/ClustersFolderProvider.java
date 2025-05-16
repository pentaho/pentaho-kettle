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

import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.ClusterSchemaManagementInterface;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
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
import org.eclipse.swt.graphics.Image;

/**
 * Created by bmorrise on 6/28/18.
 */
public class ClustersFolderProvider extends TreeFolderProvider {

  private static Class<?> PKG = Spoon.class;
  public static final String STRING_CLUSTERS = BaseMessages.getString( PKG, "Spoon.STRING_CLUSTERS" );

  private GUIResource guiResource;

  public ClustersFolderProvider( GUIResource guiResource ) {
    this.guiResource = guiResource;
  }

  public ClustersFolderProvider() {
    this( GUIResource.getInstance() );
  }

  @Override
  public void refresh( Optional<AbstractMeta> meta, TreeNode treeNode, String filter ) {
    Bowl currentBowl = Spoon.getInstance().getManagementBowl();
    Bowl globalBowl = Spoon.getInstance().getGlobalManagementBowl();
    try {
      Set<String> projectClusterNames = new HashSet<>();
      // Bowl specific
      if ( currentBowl != globalBowl ) {
        ClusterSchemaManagementInterface clusterSchemaManager =
          currentBowl.getManager( ClusterSchemaManagementInterface.class );
        List<ClusterSchema> clusterSchemas = clusterSchemaManager.getAll();

        for ( ClusterSchema sharedObject : clusterSchemas ) {
          if ( !filterMatch( sharedObject.getName(), filter ) ) {
            continue;
          }
          projectClusterNames.add( sharedObject.getName() );
          createTreeNode( treeNode, sharedObject.getName(), guiResource.getImageClusterMedium(),
            LeveledTreeNode.LEVEL.PROJECT, currentBowl.getLevelDisplayName(), false );
        }
      }

      // Global
      ClusterSchemaManagementInterface globalClusterMgr =
        globalBowl.getManager( ClusterSchemaManagementInterface.class );
      Set<String> globalClusterNames = new HashSet<>();
      List<ClusterSchema> clusters = globalClusterMgr.getAll();
      for ( ClusterSchema clusterSchema : clusters ) {
        if ( !filterMatch( clusterSchema.getName(), filter ) ) {
          continue;
        }

        globalClusterNames.add( clusterSchema.getName() );
        createTreeNode( treeNode, clusterSchema.getName(), guiResource.getImageClusterMedium(),
          LeveledTreeNode.LEVEL.GLOBAL, globalBowl.getLevelDisplayName(), containsIgnoreCase( projectClusterNames, clusterSchema.getName() ) );
      }

      // Local ClusterSchemas
      if ( meta.isPresent() ) {
        AbstractMeta realMeta = meta.get();
        if ( realMeta instanceof TransMeta ) {
          ClusterSchemaManagementInterface localClusterMgr =
            realMeta.getSharedObjectManager( ClusterSchemaManagementInterface.class );
          List<ClusterSchema> localClusters = localClusterMgr.getAll();
          for ( ClusterSchema sharedObjectInterface : localClusters ) {
            if ( !filterMatch( sharedObjectInterface.getName(), filter ) ) {
              continue;
            }

            createTreeNode( treeNode, sharedObjectInterface.getName(),
              guiResource.getImageClusterMedium(), LeveledTreeNode.LEVEL.FILE, LeveledTreeNode.LEVEL_FILE_DISPLAY_NAME,
              containsIgnoreCase( projectClusterNames, sharedObjectInterface.getName() ) ||
                containsIgnoreCase( globalClusterNames, sharedObjectInterface.getName() ) );
          }
        }
      }
    } catch ( KettleException exception ) {
      new ErrorDialog( Spoon.getInstance().getShell(),
                       BaseMessages.getString( PKG, "Spoon.ErrorDialog.Title" ),
                       BaseMessages.getString( PKG, "Spoon.ErrorDialog.ErrorFetchingFromRepo.ClusterSchemas" ),
                       exception );
    }

  }

  public TreeNode createTreeNode( TreeNode parent, String name, Image image, LeveledTreeNode.LEVEL level, String levelDisplayName,
      boolean overridden ) {
    LeveledTreeNode childTreeNode = new LeveledTreeNode( name, level, levelDisplayName, overridden );
    childTreeNode.setImage( image );

    parent.addChild( childTreeNode );
    return childTreeNode;
  }

  @Override
  public String getTitle() {
    return STRING_CLUSTERS;
  }

  @Override
  public Class<?> getType() {
    return ClusterSchema.class;
  }
}
