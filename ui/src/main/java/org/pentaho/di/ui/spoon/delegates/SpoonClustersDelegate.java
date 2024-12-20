/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.spoon.delegates;

import java.util.List;

import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.ClusterSchemaManagementInterface;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.cluster.SlaveServerManagementInterface;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.cluster.dialog.ClusterSchemaDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.tree.provider.ClustersFolderProvider;

public class SpoonClustersDelegate extends SpoonSharedObjectDelegate {

  public SpoonClustersDelegate( Spoon spoon ) {
    super( spoon );
  }

  public void newClusteringSchema( TransMeta transMeta ) {
    try {
      ClusterSchema clusterSchema = new ClusterSchema();

      // management bowl for managing, checking for duplicates
      ClusterSchemaManagementInterface clusterSchemaManagementInterface =
        spoon.getBowl().getManager( ClusterSchemaManagementInterface.class );
      // execution bowl for listing all slave servers
      SlaveServerManagementInterface slaveServerManagementInterface =
        spoon.getExecutionBowl().getManager( SlaveServerManagementInterface.class );

      List<SlaveServer> slaveServers = transMeta != null ? transMeta.getSlaveServers()
        : slaveServerManagementInterface.getAll();

      ClusterSchemaDialog dialog =
        new ClusterSchemaDialog(
            spoon.getShell(), clusterSchema, clusterSchemaManagementInterface.getAll(), slaveServers );

      if ( dialog.open() ) {
        clusterSchemaManagementInterface.add( clusterSchema );

        if ( spoon.rep != null ) {
          try {
            if ( !spoon.rep.getSecurityProvider().isReadOnly() ) {
              spoon.rep.save( clusterSchema, Const.VERSION_COMMENT_INITIAL_VERSION, null );
              if ( sharedObjectSyncUtil != null ) {
                sharedObjectSyncUtil.reloadTransformationRepositoryObjects( false );
              }
            } else {
              throw new KettleException( BaseMessages.getString(
                PKG, "Spoon.Dialog.Exception.ReadOnlyRepositoryUser" ) );
            }
          } catch ( KettleException e ) {
            showSaveError( clusterSchema, e );
          }
        }

        refreshTree();
      }
    } catch ( KettleException e ) {
      new ErrorDialog(
        spoon.getShell(), BaseMessages.getString( PKG, "Spoon.Dialog.ErrorSavingCluster.Title" ), BaseMessages
          .getString( PKG, "Spoon.Dialog.ErrorSavingCluster.Message" ), e );
    }
  }

  private void showSaveError( ClusterSchema clusterSchema, KettleException e ) {
    new ErrorDialog(
      spoon.getShell(), getMessage( "Spoon.Dialog.ErrorSavingCluster.Title" ),
      getMessage( "Spoon.Dialog.ErrorSavingCluster.Message", clusterSchema.getName() ), e );
  }

  public void editClusterSchema( TransMeta transMeta, ClusterSchemaManagementInterface manager, ClusterSchema clusterSchema ) {
    String originalName = clusterSchema.getName().trim();
    try {
      // execution bowl for listing all slave servers
      SlaveServerManagementInterface slaveServerManagementInterface =
        spoon.getExecutionBowl().getManager( SlaveServerManagementInterface.class );

      List<SlaveServer> slaveServers = transMeta != null ? transMeta.getSlaveServers()
        : slaveServerManagementInterface.getAll();

      ClusterSchemaDialog dialog =
          new ClusterSchemaDialog( spoon.getShell(), clusterSchema, manager.getAll(), slaveServers );
      if ( dialog.open() ) {
        String newName = clusterSchema.getName().trim();
        if ( !newName.equals( originalName ) ) {
          manager.remove( originalName );
        }
        manager.add( clusterSchema );
        if ( spoon.rep != null && clusterSchema.getObjectId() != null ) {
          try {
            saveSharedObjectToRepository( clusterSchema, null );
          } catch ( KettleException e ) {
            showSaveError( clusterSchema, e );
          }
        }
        refreshTree();
      }
    } catch ( KettleException e ) {
      new ErrorDialog(
        spoon.getShell(), BaseMessages.getString( PKG, "Spoon.Dialog.ErrorSavingCluster.Title" ), BaseMessages
          .getString( PKG, "Spoon.Dialog.ErrorSavingCluster.Message" ), e );
    }
  }

  public void delClusterSchema( ClusterSchemaManagementInterface manager, ClusterSchema clusterSchema ) {
    try {
      manager.remove( clusterSchema );

      if ( spoon.rep != null && clusterSchema.getObjectId() != null ) {
        // remove the partition schema from the repository too...
        spoon.rep.deleteClusterSchema( clusterSchema.getObjectId() );
        if ( sharedObjectSyncUtil != null ) {
          sharedObjectSyncUtil.deleteClusterSchema( clusterSchema );
        }
      }

      refreshTree();
    } catch ( KettleException e ) {
      new ErrorDialog(
        spoon.getShell(), BaseMessages.getString( PKG, "Spoon.Dialog.ErrorDeletingPartitionSchema.Title" ), BaseMessages
          .getString( PKG, "Spoon.Dialog.ErrorDeletingPartitionSchema.Message" ), e );
    }
  }

  public void moveToGlobal( ClusterSchemaManagementInterface clusterSchemaManager, ClusterSchema clusterSchema )
      throws KettleException {
    moveCopy( clusterSchemaManager, DefaultBowl.getInstance().getManager( ClusterSchemaManagementInterface.class ),
              clusterSchema, true, "Spoon.Message.OverwriteClusterSchemaYN" );
  }

  public void moveToProject( ClusterSchemaManagementInterface clusterSchemaManager, ClusterSchema clusterSchema )
      throws KettleException {
    moveCopy( clusterSchemaManager, spoon.getBowl().getManager( ClusterSchemaManagementInterface.class ), clusterSchema, true,
              "Spoon.Message.OverwriteClusterSchemaYN" );
  }

  public void copyToGlobal( ClusterSchemaManagementInterface clusterSchemaManager, ClusterSchema clusterSchema )
      throws KettleException {
    moveCopy( clusterSchemaManager, DefaultBowl.getInstance().getManager( ClusterSchemaManagementInterface.class ),
              clusterSchema, false, "Spoon.Message.OverwriteClusterSchemaYN" );
  }

  public void copyToProject( ClusterSchemaManagementInterface clusterSchemaManager, ClusterSchema clusterSchema )
      throws KettleException {
    moveCopy( clusterSchemaManager, spoon.getBowl().getManager( ClusterSchemaManagementInterface.class ), clusterSchema,
              false, "Spoon.Message.OverwriteClusterSchemaYN" );
  }

  public void dupeClusterSchema( ClusterSchemaManagementInterface clusterSchemaManager, ClusterSchema clusterSchema ) {
    ShowEditDialog<ClusterSchema> sed = ( cs, clusters ) -> {
      SlaveServerManagementInterface slaveServerManagementInterface =
        spoon.getExecutionBowl().getManager( SlaveServerManagementInterface.class );
      List<SlaveServer> slaveServers = slaveServerManagementInterface.getAll();

      ClusterSchemaDialog dialog = new ClusterSchemaDialog( spoon.getShell(), cs, clusters, slaveServers );
      if ( dialog.open() ) {
        String newServerName = cs.getName().trim();
        clusterSchemaManager.add( cs );
      }
    };

    dupeSharedObject( clusterSchemaManager, clusterSchema, sed );
  }


  @Override
  protected void refreshTree() {
    spoon.refreshTree( ClustersFolderProvider.STRING_CLUSTERS );
  }
}
