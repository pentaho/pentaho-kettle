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
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.partition.dialog.PartitionSchemaDialog;
import org.pentaho.di.ui.repository.dialog.RepositoryExplorerDialog;
import org.pentaho.di.ui.repository.repositoryexplorer.RepositoryExplorer;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIPartition;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIPartitions;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.swt.tags.SwtDialog;

public class PartitionsController extends AbstractXulEventHandler {

  private ResourceBundle messages;
  
  private static Class<?> PKG = RepositoryExplorerDialog.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  private BindingFactory bf = null;

  private Shell shell = null;

  private Repository repository = null;

  private XulTree partitionsTable = null;

  private UIPartitions partitionList = new UIPartitions();
  
  private VariableSpace variableSpace = null;

  @Override
  public String getName() {
    return "partitionsController"; //$NON-NLS-1$
  }

  public void init() {
    // Load the SWT Shell from the explorer dialog
    shell = ((SwtDialog) document.getElementById("repository-explorer-dialog")).getShell(); //$NON-NLS-1$

    setEnableButtons(false);
    
    if (bf != null) {
      createBindings();
    }
  }

  public void createBindings() {
    try {
      partitionsTable = (XulTree) document.getElementById("partitions-table"); //$NON-NLS-1$
      bf.createBinding(partitionList, "children", partitionsTable, "elements"); //$NON-NLS-1$ //$NON-NLS-2$
      
      bf.createBinding(partitionsTable, "selectedItems", this, "enableButtons", //$NON-NLS-1$ //$NON-NLS-2$
          new BindingConvertor<List<UIPartition>, Boolean>() {
            @Override
            public Boolean sourceToTarget(List<UIPartition> partitions) {
              // Enable / Disable New,Edit,Remove buttons
              if(partitions != null && partitions.size() > 0) {
                return true;
              }
              
              return false;
            }
            @Override
            public List<UIPartition> targetToSource(Boolean enabled) {
              return null;
            }
        });
    } catch (Exception e) {
      //TODO: Better error handling
      System.err.println(e.getMessage());
    }
    refreshPartitions();
  }

  public void setBindingFactory(BindingFactory bindingFactory) {
    this.bf = bindingFactory;
  }

  public void setRepository(Repository rep) {
    this.repository = rep;
  }
  
  public void setVariableSpace(VariableSpace variableSpace) {
    this.variableSpace = variableSpace;
  }

  public void editPartition() {
    String partitionSchemaName = ""; //$NON-NLS-1$
    try {
      Collection<UIPartition> partitions = partitionsTable.getSelectedItems();

      if (partitions != null && !partitions.isEmpty()) {
        // Grab the first item in the list & send it to the partition schema dialog
        PartitionSchema partitionSchema = ((UIPartition) partitions.toArray()[0]).getPartitionSchema();
        partitionSchemaName = partitionSchema.getName();
        // Make sure the partition already exists
        ObjectId partitionId = repository.getPartitionSchemaID(partitionSchema.getName());
        if (partitionId == null) {
          MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
          mb.setMessage(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Partition.DoesNotExists.Message")); //$NON-NLS-1$
          mb.setText(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Partition.Edit.Title")); //$NON-NLS-1$
          mb.open();
        } else {
          PartitionSchemaDialog partitionDialog = new PartitionSchemaDialog(shell, partitionSchema, repository.readDatabases(), variableSpace);
          if (partitionDialog.open()) {
            if(partitionSchema.getName() != null && !partitionSchema.getName().equals("")) {//$NON-NLS-1$
              repository.insertLogEntry(BaseMessages.getString(RepositoryExplorer.class,
                  "PartitionsController.Message.UpdatingPartition",partitionSchema.getName()));//$NON-NLS-1$
              repository.save(partitionSchema, Const.VERSION_COMMENT_EDIT_VERSION, null);
            } else {
              MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
              mb.setMessage(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Partition.Edit.InvalidName.Message")); //$NON-NLS-1$
              mb.setText(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Partition.Edit.Title")); //$NON-NLS-1$
              mb.open();
            }
          }
        }
      } else {
        MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
        mb.setMessage(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Partition.NoItemSelected.Message")); //$NON-NLS-1$
        mb.setText(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Partition.Edit.Title")); //$NON-NLS-1$
        mb.open();
      }
    } catch (KettleException e) {
      new ErrorDialog(
          shell,
          BaseMessages.getString(PKG, "RepositoryExplorerDialog.Partition.Edit.Title"), //$NON-NLS-1$
          BaseMessages.getString(PKG, "RepositoryExplorerDialog.Partition.Edit.UnexpectedError.Message") + partitionSchemaName + "]", e); //$NON-NLS-1$ //$NON-NLS-2$
    } finally {
      refreshPartitions();
    }
  }

  public void createPartition() {
    try {
      PartitionSchema partition = new PartitionSchema();
      PartitionSchemaDialog partitionDialog = new PartitionSchemaDialog(shell, partition, repository.readDatabases(), variableSpace);
      if (partitionDialog.open()) {
        // See if this partition already exists...
        ObjectId idPartition = repository.getPartitionSchemaID(partition.getName());
        if (idPartition == null) {
          if(partition.getName() != null && !partition.getName().equals("")) {//$NON-NLS-1$
            repository.insertLogEntry(BaseMessages.getString(RepositoryExplorer.class, "PartitionsController.Message.CreatingPartition",partition.getName()));//$NON-NLS-1$
            repository.save(partition, Const.VERSION_COMMENT_INITIAL_VERSION, null);
          } else {
            MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
            mb.setMessage(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Partition.Edit.InvalidName.Message")); //$NON-NLS-1$
            mb.setText(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Partition.Create.Title")); //$NON-NLS-1$
            mb.open();
          }
        } else {
          MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
          mb.setMessage(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Partition.Create.AlreadyExists.Message")); //$NON-NLS-1$
          mb.setText(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Partition.Create.AlreadyExists.Title")); //$NON-NLS-1$
          mb.open();
        }
      }
    } catch (KettleException e) {
      new ErrorDialog(shell, BaseMessages.getString(PKG,"RepositoryExplorerDialog.Partition.Create.UnexpectedError.Title"), //$NON-NLS-1$
          BaseMessages.getString(PKG, "RepositoryExplorerDialog.Partition.Create.UnexpectedError.Message"), e); //$NON-NLS-1$
    } finally {
      refreshPartitions();
    }
  }

  public void removePartition() {
    String partitionSchemaName = ""; //$NON-NLS-1$
    try {
      Collection<UIPartition> partitions = partitionsTable.getSelectedItems();

      if (partitions != null && !partitions.isEmpty()) {
        // Grab the first item in the list for deleting
        PartitionSchema partitionSchema = ((UIPartition) partitions.toArray()[0]).getPartitionSchema();
        partitionSchemaName = partitionSchema.getName();
        // Make sure the partition to delete exists in the repository
        ObjectId partitionId = repository.getPartitionSchemaID(partitionSchema.getName());
        if (partitionId == null) {
          MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
          mb.setMessage(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Partition.DoesNotExists.Message")); //$NON-NLS-1$
          mb.setText(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Partition.Delete.Title")); //$NON-NLS-1$
          mb.open();
        } else {
          repository.deletePartitionSchema(partitionId);
        }
      } else {
        MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
        mb.setMessage(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Partition.NoItemSelected.Message")); //$NON-NLS-1$
        mb.setText(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Partition.Delete.Title")); //$NON-NLS-1$
        mb.open();
      }
    } catch (KettleException e) {
      new ErrorDialog(
          shell,
          BaseMessages.getString(PKG, "RepositoryExplorerDialog.Partition.Delete.Title"), BaseMessages.getString(PKG, "RepositoryExplorerDialog.Partition.Delete.UnexpectedError.Message") + partitionSchemaName + "]", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    } finally {
      refreshPartitions();
    }
  }

  public void refreshPartitions() {
    if (repository != null) {
      try {
        partitionList.clear();
        ObjectId[] partitionIdList = repository.getPartitionSchemaIDs(false);

        for (ObjectId partitionId : partitionIdList) {
          PartitionSchema partition = repository.loadPartitionSchema(partitionId, null);
          // Add the partition schema to the list
          partitionList.add(new UIPartition(partition));
        }
      } catch (KettleException e) {
        // TODO: Better error handling
        System.err.println(e.getMessage());
      }
    }
  }

  public void setEnableButtons(boolean enable) {
    // Convenience - Leave 'new' enabled, modify 'edit' and 'remove'
    enableButtons(true, enable, enable);
  }
  
  public void enableButtons(boolean enableNew, boolean enableEdit, boolean enableRemove) {
    XulButton bNew = (XulButton) document.getElementById("partitions-new"); //$NON-NLS-1$
    XulButton bEdit = (XulButton) document.getElementById("partitions-edit"); //$NON-NLS-1$
    XulButton bRemove = (XulButton) document.getElementById("partitions-remove"); //$NON-NLS-1$
    
    bNew.setDisabled(!enableNew);
    bEdit.setDisabled(!enableEdit);
    bRemove.setDisabled(!enableRemove);
  }

  public void setMessages(ResourceBundle messages) {
    this.messages = messages;
  }

  public ResourceBundle getMessages() {
    return messages;
  }
}
