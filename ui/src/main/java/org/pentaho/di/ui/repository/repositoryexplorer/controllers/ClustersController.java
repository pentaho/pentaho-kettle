/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.repository.repositoryexplorer.controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryExtended;
import org.pentaho.di.ui.cluster.dialog.ClusterSchemaDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.repository.dialog.RepositoryExplorerDialog;
import org.pentaho.di.ui.repository.repositoryexplorer.IUISupportController;
import org.pentaho.di.ui.repository.repositoryexplorer.RepositoryExplorer;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UICluster;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIClusters;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.swt.SwtBindingFactory;
import org.pentaho.ui.xul.swt.tags.SwtDialog;

public class ClustersController extends LazilyInitializedController implements IUISupportController {

  private static Class<?> PKG = RepositoryExplorerDialog.class; // for i18n purposes, needed by Translator2!!

  protected BindingFactory bf = null;

  private Shell shell = null;

  private XulTree clustersTable = null;

  private UIClusters clusterList = new UIClusters();

  private MainController mainController;

  @Override
  public String getName() {
    return "clustersController";
  }

  public void createBindings() {
    refreshClusters();
    try {
      clustersTable = (XulTree) document.getElementById( "clusters-table" );
      bf.setBindingType( Binding.Type.ONE_WAY );
      bf.createBinding( clusterList, "children", clustersTable, "elements" ).fireSourceChanged();
      bf.createBinding( clustersTable, "selectedItems", this, "enableButtons" );
    } catch ( Exception e ) {
      if ( mainController == null || !mainController.handleLostRepository( e ) ) {
        throw new RuntimeException( e );
      }
    }
  }

  protected boolean doLazyInit() {
    try {
      // Load the SWT Shell from the explorer dialog
      mainController = (MainController) this.getXulDomContainer().getEventHandler( "mainController" );
      shell = ( (SwtDialog) document.getElementById( "repository-explorer-dialog" ) ).getShell();
      bf = new SwtBindingFactory();
      bf.setDocument( this.getXulDomContainer().getDocumentRoot() );
      enableButtons( true, false, false );
      if ( bf != null ) {
        createBindings();
      }
      return true;
    } catch ( Exception e ) {
      if ( mainController == null || !mainController.handleLostRepository( e ) ) {
        return false;
      }

      return false;
    }
  }

  public void editCluster() {
    String clusterSchemaName = "";
    try {
      Collection<UICluster> clusters = clustersTable.getSelectedItems();

      if ( clusters != null && !clusters.isEmpty() ) {
        // Grab the first item in the list & send it to the cluster schema dialog
        ClusterSchema clusterSchema = ( (UICluster) clusters.toArray()[0] ).getClusterSchema();
        clusterSchemaName = clusterSchema.getName();
        // Make sure the cluster already exists
        ObjectId clusterId = repository.getClusterID( clusterSchema.getName() );
        if ( clusterId == null ) {
          MessageBox mb = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
          mb.setMessage( BaseMessages.getString(
            PKG, "RepositoryExplorerDialog.Cluster.DoesNotExists.Message", clusterSchemaName ) );
          mb.setText( BaseMessages.getString( PKG, "RepositoryExplorerDialog.Cluster.Edit.Title" ) );
          mb.open();
        } else {
          ClusterSchemaDialog csd = new ClusterSchemaDialog( shell, clusterSchema, repository.getSlaveServers() );
          if ( csd.open() ) {
            if ( clusterSchema.getName() != null && !clusterSchema.getName().equals( "" ) ) {
              repository.insertLogEntry( BaseMessages.getString(
                PKG, "ClusterController.Message.UpdatingCluster", clusterSchema.getName() ) );
              repository.save( clusterSchema, Const.VERSION_COMMENT_EDIT_VERSION, null );
              if ( mainController != null && mainController.getSharedObjectSyncUtil() != null ) {
                mainController.getSharedObjectSyncUtil().synchronizeClusterSchemas( clusterSchema, clusterSchemaName );
              }
            } else {
              MessageBox mb = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
              mb.setMessage( BaseMessages.getString(
                PKG, "RepositoryExplorerDialog.Cluster.Edit.InvalidName.Message" ) );
              mb.setText( BaseMessages.getString( PKG, "RepositoryExplorerDialog.Cluster.Edit.Title" ) );
              mb.open();
            }
          }
        }
      } else {
        MessageBox mb = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
        mb.setMessage( BaseMessages.getString( PKG, "RepositoryExplorerDialog.Cluster.NoItemSelected.Message" ) );
        mb.setText( BaseMessages.getString( PKG, "RepositoryExplorerDialog.Cluster.Edit.Title" ) );
        mb.open();
      }

      refreshClusters();
    } catch ( KettleException e ) {
      if ( mainController == null || !mainController.handleLostRepository( e ) ) {
        new ErrorDialog(
          shell,
          BaseMessages.getString( PKG, "RepositoryExplorerDialog.Cluster.Edit.Title" ), BaseMessages.getString(
            PKG, "RepositoryExplorerDialog.Cluster.Edit.UnexpectedError.Message" )
            + clusterSchemaName + "]", e );
      }
    }
  }

  public void createCluster() {
    try {
      ClusterSchema cluster = new ClusterSchema();
      ClusterSchemaDialog clusterDialog = new ClusterSchemaDialog( shell, cluster, repository.getSlaveServers() );
      if ( clusterDialog.open() ) {
        // See if this cluster already exists...
        ObjectId idCluster = repository.getClusterID( cluster.getName() );
        if ( idCluster == null ) {
          if ( cluster.getName() != null && !cluster.getName().equals( "" ) ) {
            repository.insertLogEntry( BaseMessages.getString(
              RepositoryExplorer.class, "ClusterController.Message.CreatingNewCluster", cluster.getName() ) );
            repository.save( cluster, Const.VERSION_COMMENT_INITIAL_VERSION, null );
            if ( mainController != null && mainController.getSharedObjectSyncUtil() != null ) {
              mainController.getSharedObjectSyncUtil().reloadTransformationRepositoryObjects( true );
            }
          } else {
            MessageBox mb = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
            mb.setMessage( BaseMessages.getString(
              PKG, "RepositoryExplorerDialog.Cluster.Edit.InvalidName.Message" ) );
            mb.setText( BaseMessages.getString( PKG, "RepositoryExplorerDialog.Cluster.Edit.Title" ) );
            mb.open();
          }
        } else {
          MessageBox mb = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
          mb.setMessage( BaseMessages.getString(
            PKG, "RepositoryExplorerDialog.Cluster.Create.AlreadyExists.Message" ) );
          mb
            .setText( BaseMessages
              .getString( PKG, "RepositoryExplorerDialog.Cluster.Create.AlreadyExists.Title" ) );
          mb.open();
        }
      }
    } catch ( KettleException e ) {
      if ( mainController == null || !mainController.handleLostRepository( e ) ) {
        new ErrorDialog( shell,
          BaseMessages.getString( PKG, "RepositoryExplorerDialog.Cluster.Create.UnexpectedError.Title" ),
          BaseMessages.getString( PKG, "RepositoryExplorerDialog.Cluster.Create.UnexpectedError.Message" ), e );
      }
    } finally {
      refreshClusters();
    }
  }

  public void removeCluster() {
    String clusterSchemaName = "";
    try {
      Collection<UICluster> clusters = clustersTable.getSelectedItems();

      if ( clusters != null && !clusters.isEmpty() ) {
        for ( Object obj : clusters ) {
          if ( obj != null && obj instanceof UICluster ) {
            UICluster cluster = (UICluster) obj;
            ClusterSchema clusterSchema = cluster.getClusterSchema();
            clusterSchemaName = clusterSchema.getName();
            // Make sure the cluster to delete exists in the repository
            ObjectId clusterId = repository.getClusterID( clusterSchema.getName() );
            if ( clusterId == null ) {
              MessageBox mb = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
              mb.setMessage( BaseMessages.getString(
                PKG, "RepositoryExplorerDialog.Cluster.DoesNotExists.Message", clusterSchema.getName() ) );
              mb.setText( BaseMessages.getString( PKG, "RepositoryExplorerDialog.Cluster.Delete.Title" ) );
              mb.open();
            } else {
              repository.deleteClusterSchema( clusterId );
              if ( mainController != null && mainController.getSharedObjectSyncUtil() != null ) {
                mainController.getSharedObjectSyncUtil().deleteClusterSchema( clusterSchema );
              }
            }
          }
        }
      } else {
        MessageBox mb = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
        mb.setMessage( BaseMessages.getString( PKG, "RepositoryExplorerDialog.Cluster.NoItemSelected.Message" ) );
        mb.setText( BaseMessages.getString( PKG, "RepositoryExplorerDialog.Cluster.Delete.Title" ) );
        mb.open();
      }
    } catch ( KettleException e ) {
      if ( mainController == null || !mainController.handleLostRepository( e ) ) {
        new ErrorDialog(
          shell,
          BaseMessages.getString( PKG, "RepositoryExplorerDialog.Cluster.Delete.Title" ),
          BaseMessages.getString( PKG, "RepositoryExplorerDialog.Cluster.Delete.UnexpectedError.Message" )
            + clusterSchemaName + "]", e );
      }
    } finally {
      refreshClusters();
    }
  }

  public void refreshClusters() {
    if ( repository != null ) {
      final List<UICluster> tmpList = new ArrayList<UICluster>();
      Runnable r = () -> {
        try {
          if ( repository instanceof RepositoryExtended ) {
            List<ClusterSchema> clusterSchemas = ((RepositoryExtended) repository).getClusters( false );
            clusterSchemas.forEach( clusterSchema -> tmpList.add( new UICluster( clusterSchema ) ) );
          } else {
            ObjectId[] clusterIdList = repository.getClusterIDs( false );
            for ( ObjectId clusterId : clusterIdList ) {
              ClusterSchema cluster = repository.loadClusterSchema( clusterId, repository.getSlaveServers(), null );
              // Add the cluster schema to the list
              tmpList.add( new UICluster( cluster ) );
            }
          }
        } catch ( KettleException e ) {
          // convert to runtime exception so it bubbles up through the UI
          throw new RuntimeException( e );
        }
      };
      doWithBusyIndicator( r );
      clusterList.setChildren( tmpList );
    }
  }

  public void setEnableButtons( List<UICluster> clusters ) {
    boolean enableEdit = false;
    boolean enableRemove = false;
    if ( clusters != null && clusters.size() > 0 ) {
      enableRemove = true;
      if ( clusters.size() == 1 ) {
        enableEdit = true;
      }
    }
    // Convenience - Leave 'new' enabled, modify 'edit' and 'remove'
    enableButtons( true, enableEdit, enableRemove );
  }

  public void enableButtons( boolean enableNew, boolean enableEdit, boolean enableRemove ) {
    XulButton bNew = (XulButton) document.getElementById( "clusters-new" );
    XulButton bEdit = (XulButton) document.getElementById( "clusters-edit" );
    XulButton bRemove = (XulButton) document.getElementById( "clusters-remove" );

    bNew.setDisabled( !enableNew );
    bEdit.setDisabled( !enableEdit );
    bRemove.setDisabled( !enableRemove );
  }

  public void tabClicked() {
    lazyInit();
  }

}
