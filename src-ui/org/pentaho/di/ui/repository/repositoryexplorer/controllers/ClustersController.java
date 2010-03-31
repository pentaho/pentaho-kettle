/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2009 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.di.ui.repository.repositoryexplorer.controllers;

import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.cluster.dialog.ClusterSchemaDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.repository.dialog.RepositoryExplorerDialog;
import org.pentaho.di.ui.repository.repositoryexplorer.ControllerInitializationException;
import org.pentaho.di.ui.repository.repositoryexplorer.IUISupportController;
import org.pentaho.di.ui.repository.repositoryexplorer.RepositoryExplorer;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UICluster;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIClusters;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.swt.tags.SwtDialog;

public class ClustersController extends AbstractXulEventHandler implements IUISupportController {

  private ResourceBundle messages = new ResourceBundle() {

    @Override
    public Enumeration<String> getKeys() {
      return null;
    }

    @Override
    protected Object handleGetObject(String key) {
      return BaseMessages.getString(RepositoryExplorer.class, key);
    }

  };

  private static Class<?> PKG = RepositoryExplorerDialog.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  protected BindingFactory bf = null;

  private Shell shell = null;

  private Repository repository = null;

  private XulTree clustersTable = null;

  private UIClusters clusterList = new UIClusters();

  @Override
  public String getName() {
    return "clustersController"; //$NON-NLS-1$
  }

  public void init(Repository repository) throws ControllerInitializationException {
    this.repository = repository;
    // Load the SWT Shell from the explorer dialog
    shell = ((SwtDialog) document.getElementById("repository-explorer-dialog")).getShell(); //$NON-NLS-1$
    bf = new DefaultBindingFactory();
    bf.setDocument(this.getXulDomContainer().getDocumentRoot());
    enableButtons(true, false, false);
    if (bf != null) {
      createBindings();
    }
  }

  public void createBindings() {
    try {
      clustersTable = (XulTree) document.getElementById("clusters-table"); //$NON-NLS-1$
      bf.setBindingType(Binding.Type.ONE_WAY);
      bf.createBinding(clusterList, "children", clustersTable, "elements"); //$NON-NLS-1$ //$NON-NLS-2$
      bf.createBinding(clustersTable, "selectedItems", this, "enableButtons"); //$NON-NLS-1$ //$NON-NLS-2$
    } catch (Exception e) {
      // convert to runtime exception so it bubbles up through the UI
      throw new RuntimeException(e);
    }
    refreshClusters();
  }

  public void editCluster() {
    String clusterSchemaName = ""; //$NON-NLS-1$
    try {
      Collection<UICluster> clusters = clustersTable.getSelectedItems();

      if (clusters != null && !clusters.isEmpty()) {
        // Grab the first item in the list & send it to the cluster schema dialog
        ClusterSchema clusterSchema = ((UICluster) clusters.toArray()[0]).getClusterSchema();
        clusterSchemaName = clusterSchema.getName();
        // Make sure the cluster already exists
        ObjectId clusterId = repository.getClusterID(clusterSchema.getName());
        if (clusterId == null) {
          MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
          mb.setMessage(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Cluster.DoesNotExists.Message")); //$NON-NLS-1$
          mb.setText(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Cluster.Edit.Title")); //$NON-NLS-1$
          mb.open();
        } else {
          ClusterSchemaDialog csd = new ClusterSchemaDialog(shell, clusterSchema, repository.getSlaveServers());
          if (csd.open()) {
            if (clusterSchema.getName() != null && !clusterSchema.getName().equals("")) {//$NON-NLS-1$
              repository.insertLogEntry(BaseMessages.getString(PKG,
                  "ClusterController.Message.UpdatingCluster", clusterSchema.getName())); //$NON-NLS-1$
              repository.save(clusterSchema, Const.VERSION_COMMENT_EDIT_VERSION, null);
            } else {
              MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
              mb.setMessage(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Cluster.Edit.InvalidName.Message")); //$NON-NLS-1$
              mb.setText(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Cluster.Edit.Title")); //$NON-NLS-1$
              mb.open();
            }
          }
        }
      } else {
        MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
        mb.setMessage(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Cluster.NoItemSelected.Message")); //$NON-NLS-1$
        mb.setText(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Cluster.Edit.Title")); //$NON-NLS-1$
        mb.open();
      }

      refreshClusters();
    } catch (KettleException e) {
      new ErrorDialog(
          shell,
          BaseMessages.getString(PKG, "RepositoryExplorerDialog.Cluster.Edit.Title"), BaseMessages.getString(PKG, "RepositoryExplorerDialog.Cluster.Edit.UnexpectedError.Message") + clusterSchemaName + "]", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
  }

  public void createCluster() {
    try {
      ClusterSchema cluster = new ClusterSchema();
      ClusterSchemaDialog clusterDialog = new ClusterSchemaDialog(shell, cluster, repository.getSlaveServers());
      if (clusterDialog.open()) {
        // See if this cluster already exists...
        ObjectId idCluster = repository.getClusterID(cluster.getName());
        if (idCluster == null) {
          if (cluster.getName() != null && !cluster.getName().equals("")) {//$NON-NLS-1$
            repository.insertLogEntry(BaseMessages.getString(RepositoryExplorer.class,
                "ClusterController.Message.CreatingNewCluster", cluster.getName())); //$NON-NLS-1$
            repository.save(cluster, Const.VERSION_COMMENT_INITIAL_VERSION, null);
          } else {
            MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
            mb.setMessage(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Cluster.Edit.InvalidName.Message")); //$NON-NLS-1$
            mb.setText(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Cluster.Edit.Title")); //$NON-NLS-1$
            mb.open();
          }
        } else {
          MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
          mb.setMessage(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Cluster.Create.AlreadyExists.Message")); //$NON-NLS-1$
          mb.setText(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Cluster.Create.AlreadyExists.Title")); //$NON-NLS-1$
          mb.open();
        }
      }
    } catch (KettleException e) {
      new ErrorDialog(shell, BaseMessages.getString(PKG,
          "RepositoryExplorerDialog.Cluster.Create.UnexpectedError.Title"), //$NON-NLS-1$
          BaseMessages.getString(PKG, "RepositoryExplorerDialog.Cluster.Create.UnexpectedError.Message"), e); //$NON-NLS-1$
    } finally {
      refreshClusters();
    }
  }

  public void removeCluster() {
    String clusterSchemaName = ""; //$NON-NLS-1$
    try {
      Collection<UICluster> clusters = clustersTable.getSelectedItems();

      if (clusters != null && !clusters.isEmpty()) {
        for (Object obj : clusters) {
          if (obj != null && obj instanceof UICluster) {
            UICluster cluster = (UICluster) obj;
            ClusterSchema clusterSchema = cluster.getClusterSchema();
            clusterSchemaName = clusterSchema.getName();
            // Make sure the cluster to delete exists in the repository
            ObjectId clusterId = repository.getClusterID(clusterSchema.getName());
            if (clusterId == null) {
              MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
              mb.setMessage(BaseMessages.getString(PKG,
                  "RepositoryExplorerDialog.Cluster.DoesNotExists.Message", clusterSchema.getName())); //$NON-NLS-1$
              mb.setText(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Cluster.Delete.Title")); //$NON-NLS-1$
              mb.open();
            } else {
              repository.deleteClusterSchema(clusterId);
            }
          }
        }
      } else {
        MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
        mb.setMessage(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Cluster.NoItemSelected.Message")); //$NON-NLS-1$
        mb.setText(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Cluster.Delete.Title")); //$NON-NLS-1$
        mb.open();
      }
    } catch (KettleException e) {
      new ErrorDialog(
          shell,
          BaseMessages.getString(PKG, "RepositoryExplorerDialog.Cluster.Delete.Title"), BaseMessages.getString(PKG, "RepositoryExplorerDialog.Cluster.Delete.UnexpectedError.Message") + clusterSchemaName + "]", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    } finally {
      refreshClusters();
    }
  }

  public void refreshClusters() {
    if (repository != null) {
      try {
        clusterList.clear();
        ObjectId[] clusterIdList = repository.getClusterIDs(false);

        for (ObjectId clusterId : clusterIdList) {
          ClusterSchema cluster = repository.loadClusterSchema(clusterId, repository.getSlaveServers(), null);
          // Add the cluster schema to the list
          clusterList.add(new UICluster(cluster));
        }
      } catch (KettleException e) {
        // convert to runtime exception so it bubbles up through the UI
        throw new RuntimeException(e);
      }
    }
  }

  public void setEnableButtons(List<UICluster> clusters) {
    boolean enableEdit = false;
    boolean enableRemove = false;
    if(clusters != null && clusters.size() > 0) {
      enableRemove = true;
      if(clusters.size() == 1) {
        enableEdit = true;
      }
    }
    // Convenience - Leave 'new' enabled, modify 'edit' and 'remove'
    enableButtons(true, enableEdit, enableRemove);
  }

  public void enableButtons(boolean enableNew, boolean enableEdit, boolean enableRemove) {
    XulButton bNew = (XulButton) document.getElementById("clusters-new"); //$NON-NLS-1$
    XulButton bEdit = (XulButton) document.getElementById("clusters-edit"); //$NON-NLS-1$
    XulButton bRemove = (XulButton) document.getElementById("clusters-remove"); //$NON-NLS-1$

    bNew.setDisabled(!enableNew);
    bEdit.setDisabled(!enableEdit);
    bRemove.setDisabled(!enableRemove);
  }
  
}
