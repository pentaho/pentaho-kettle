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

import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.ClusterSchemaManagementInterface;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.shared.SharedObjectInterface;
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
    Bowl currentBowl = Spoon.getInstance().getBowl();
    Bowl globalBowl = Spoon.getInstance().getGlobalManagementBowl();
    try {
      Set<String> projectClusterNames = new HashSet<>();
      // Bowl specific
      if ( currentBowl != globalBowl ) {
        ClusterSchemaManagementInterface clusterSchemaManager =
          currentBowl.getManager( ClusterSchemaManagementInterface.class );
        List<ClusterSchema> clusterSchemas = clusterSchemaManager.getAll();

        for ( SharedObjectInterface sharedObject : clusterSchemas ) {
          if ( !filterMatch( sharedObject.getName(), filter ) ) {
            continue;
          }
          projectClusterNames.add( sharedObject.getName() );
          TreeNode childTreeNode = createTreeNode( treeNode, sharedObject.getName(), guiResource.getImageClusterMedium(),
                                                   LeveledTreeNode.LEVEL.PROJECT, false );
        }
      }

      // Global
      ClusterSchemaManagementInterface globalClusterMgr =
        globalBowl.getManager( ClusterSchemaManagementInterface.class );
      Set<String> globalClusterNames = new HashSet<>();
      List<ClusterSchema> clusters = globalClusterMgr.getAll();
      for ( SharedObjectInterface clusterSchema : clusters ) {
        if ( !filterMatch( clusterSchema.getName(), filter ) ) {
          continue;
        }

        globalClusterNames.add( clusterSchema.getName() );
        TreeNode childTreeNode = createTreeNode( treeNode, clusterSchema.getName(), guiResource.getImageClusterMedium(),
          LeveledTreeNode.LEVEL.GLOBAL, projectClusterNames.contains( clusterSchema.getName() ) );
      }

      // Local ClusterSchemas
      if ( meta.isPresent() ) {
        AbstractMeta realMeta = meta.get();
        if ( realMeta instanceof TransMeta ) {
          ClusterSchemaManagementInterface localClusterMgr =
            realMeta.getSharedObjectManager( ClusterSchemaManagementInterface.class );
          List<ClusterSchema> localClusters = localClusterMgr.getAll();
          for ( SharedObjectInterface sharedObjectInterface : localClusters ) {
            if ( !filterMatch( sharedObjectInterface.getName(), filter ) ) {
              continue;
            }

            TreeNode childTreeNode = createTreeNode( treeNode, sharedObjectInterface.getName(),
              guiResource.getImageClusterMedium(), LeveledTreeNode.LEVEL.LOCAL,
              projectClusterNames.contains( sharedObjectInterface.getName() ) ||
                globalClusterNames.contains( sharedObjectInterface.getName() ) );
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

  public TreeNode createTreeNode( TreeNode parent, String name, Image image, LeveledTreeNode.LEVEL level,
      boolean overridden ) {
    LeveledTreeNode childTreeNode = new LeveledTreeNode( name, level, overridden );
    childTreeNode.setImage( image );

    parent.addChild( childTreeNode );
    return childTreeNode;
  }

  @Override
  public String getTitle() {
    return STRING_CLUSTERS;
  }

  @Override
  public Class getType() {
    return ClusterSchema.class;
  }
}
