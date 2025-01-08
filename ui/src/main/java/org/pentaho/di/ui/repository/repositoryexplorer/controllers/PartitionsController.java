/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018  by Hitachi Vantara : http://www.pentaho.com
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryExtended;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.partition.dialog.PartitionSchemaDialog;
import org.pentaho.di.ui.repository.dialog.RepositoryExplorerDialog;
import org.pentaho.di.ui.repository.repositoryexplorer.ControllerInitializationException;
import org.pentaho.di.ui.repository.repositoryexplorer.IUISupportController;
import org.pentaho.di.ui.repository.repositoryexplorer.RepositoryExplorer;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIPartition;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIPartitions;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.swt.SwtBindingFactory;
import org.pentaho.ui.xul.swt.tags.SwtDialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PartitionsController extends LazilyInitializedController implements IUISupportController {

  private static Class<?> PKG = RepositoryExplorerDialog.class; // for i18n purposes, needed by Translator2!!

  protected BindingFactory bf = null;

  private Shell shell = null;

  private XulTree partitionsTable = null;

  private UIPartitions partitionList = new UIPartitions();

  private MainController mainController;

  private VariableSpace variableSpace = Variables.getADefaultVariableSpace();

  @Override
  public String getName() {
    return "partitionsController";
  }

  public void init( Repository repository ) throws ControllerInitializationException {
    this.repository = repository;
  }

  public void createBindings() {
    refreshPartitions();
    try {
      partitionsTable = (XulTree) document.getElementById( "partitions-table" );
      bf.setBindingType( Binding.Type.ONE_WAY );
      bf.createBinding( partitionList, "children", partitionsTable, "elements" ).fireSourceChanged();
      bf.createBinding( partitionsTable, "selectedItems", this, "enableButtons" );
    } catch ( Exception e ) {
      if ( mainController == null || !mainController.handleLostRepository( e ) ) {
        // convert to runtime exception so it bubbles up through the UI
        throw new RuntimeException( e );
      }
    }
  }

  protected boolean doLazyInit() {
    // Load the SWT Shell from the explorer dialog
    shell = ( (SwtDialog) document.getElementById( "repository-explorer-dialog" ) ).getShell();

    try {
      mainController = (MainController) this.getXulDomContainer().getEventHandler( "mainController" );
    } catch ( XulException e ) {
      return false;
    }

    enableButtons( true, false, false );
    bf = new SwtBindingFactory();
    bf.setDocument( this.getXulDomContainer().getDocumentRoot() );

    if ( bf != null ) {
      createBindings();
    }

    return true;
  }

  public void setVariableSpace( VariableSpace variableSpace ) {
    this.variableSpace = variableSpace;
  }

  public void editPartition() {
    String partitionSchemaName = "";
    try {
      Collection<UIPartition> partitions = partitionsTable.getSelectedItems();

      if ( partitions != null && !partitions.isEmpty() ) {
        // Grab the first item in the list & send it to the partition schema dialog
        PartitionSchema partitionSchema = ( (UIPartition) partitions.toArray()[0] ).getPartitionSchema();
        partitionSchemaName = partitionSchema.getName();
        // Make sure the partition already exists
        ObjectId partitionId = repository.getPartitionSchemaID( partitionSchema.getName() );
        if ( partitionId == null ) {
          MessageBox mb = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
          mb.setMessage( BaseMessages.getString(
            PKG, "RepositoryExplorerDialog.Partition.DoesNotExists.Message", partitionSchemaName ) );
          mb.setText( BaseMessages.getString( PKG, "RepositoryExplorerDialog.Partition.Edit.Title" ) );
          mb.open();
        } else {
          PartitionSchemaDialog partitionDialog =
            new PartitionSchemaDialog( shell, partitionSchema, repository.readDatabases(), variableSpace );
          if ( partitionDialog.open() ) {
            if ( partitionSchema.getName() != null && !partitionSchema.getName().equals( "" ) ) {
              repository.insertLogEntry( BaseMessages.getString(
                RepositoryExplorer.class, "PartitionsController.Message.UpdatingPartition", partitionSchema
                  .getName() ) );
              repository.save( partitionSchema, Const.VERSION_COMMENT_EDIT_VERSION, null );
              if ( mainController != null && mainController.getSharedObjectSyncUtil() != null ) {
                mainController.getSharedObjectSyncUtil().synchronizePartitionSchemas(
                    partitionSchema, partitionSchemaName );
              }
            } else {
              MessageBox mb = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
              mb.setMessage( BaseMessages.getString(
                PKG, "RepositoryExplorerDialog.Partition.Edit.InvalidName.Message" ) );
              mb.setText( BaseMessages.getString( PKG, "RepositoryExplorerDialog.Partition.Edit.Title" ) );
              mb.open();
            }
          }
        }
      } else {
        MessageBox mb = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
        mb.setMessage( BaseMessages.getString( PKG, "RepositoryExplorerDialog.Partition.NoItemSelected.Message" ) );
        mb.setText( BaseMessages.getString( PKG, "RepositoryExplorerDialog.Partition.Edit.Title" ) );
        mb.open();
      }
    } catch ( KettleException e ) {
      if ( mainController == null || !mainController.handleLostRepository( e ) ) {
        new ErrorDialog(
          shell, BaseMessages.getString( PKG, "RepositoryExplorerDialog.Partition.Edit.Title" ), BaseMessages
            .getString( PKG, "RepositoryExplorerDialog.Partition.Edit.UnexpectedError.Message" )
            + partitionSchemaName + "]", e );
      }
    } finally {
      refreshPartitions();
    }
  }

  public void createPartition() {
    try {
      PartitionSchema partition = new PartitionSchema();
      PartitionSchemaDialog partitionDialog =
        new PartitionSchemaDialog( shell, partition, repository.readDatabases(), variableSpace );
      if ( partitionDialog.open() ) {
        // See if this partition already exists...
        ObjectId idPartition = repository.getPartitionSchemaID( partition.getName() );
        if ( idPartition == null ) {
          if ( partition.getName() != null && !partition.getName().equals( "" ) ) {
            repository.insertLogEntry( BaseMessages.getString(
              RepositoryExplorer.class, "PartitionsController.Message.CreatingPartition", partition.getName() ) );
            repository.save( partition, Const.VERSION_COMMENT_INITIAL_VERSION, null );
          } else {
            MessageBox mb = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
            mb.setMessage( BaseMessages.getString(
              PKG, "RepositoryExplorerDialog.Partition.Edit.InvalidName.Message" ) );
            mb.setText( BaseMessages.getString( PKG, "RepositoryExplorerDialog.Partition.Create.Title" ) );
            mb.open();
          }
        } else {
          MessageBox mb = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
          mb.setMessage( BaseMessages.getString(
            PKG, "RepositoryExplorerDialog.Partition.Create.AlreadyExists.Message" ) );
          mb.setText( BaseMessages
            .getString( PKG, "RepositoryExplorerDialog.Partition.Create.AlreadyExists.Title" ) );
          mb.open();
        }
      }
    } catch ( KettleException e ) {
      if ( mainController == null || !mainController.handleLostRepository( e ) ) {
        new ErrorDialog( shell,
          BaseMessages.getString( PKG, "RepositoryExplorerDialog.Partition.Create.UnexpectedError.Title" ),
          BaseMessages.getString( PKG, "RepositoryExplorerDialog.Partition.Create.UnexpectedError.Message" ), e );
      }
    } finally {
      refreshPartitions();
    }
  }

  public void removePartition() {
    String partitionSchemaName = "";
    try {
      Collection<UIPartition> partitions = partitionsTable.getSelectedItems();

      if ( partitions != null && !partitions.isEmpty() ) {
        for ( Object obj : partitions ) {
          if ( obj != null && obj instanceof UIPartition ) {
            UIPartition partition = (UIPartition) obj;
            PartitionSchema partitionSchema = partition.getPartitionSchema();
            partitionSchemaName = partitionSchema.getName();
            // Make sure the partition to delete exists in the repository
            ObjectId partitionId = repository.getPartitionSchemaID( partitionSchema.getName() );
            if ( partitionId == null ) {
              MessageBox mb = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
              mb.setMessage( BaseMessages.getString(
                PKG, "RepositoryExplorerDialog.Partition.DoesNotExists.Message", partitionSchemaName ) );
              mb.setText( BaseMessages.getString( PKG, "RepositoryExplorerDialog.Partition.Delete.Title" ) );
              mb.open();
            } else {
              repository.deletePartitionSchema( partitionId );
              if ( mainController != null && mainController.getSharedObjectSyncUtil() != null ) {
                mainController.getSharedObjectSyncUtil().deletePartitionSchema( partitionSchema );
              }
            }
          }
        }
      } else {
        MessageBox mb = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
        mb.setMessage( BaseMessages.getString( PKG, "RepositoryExplorerDialog.Partition.NoItemSelected.Message" ) );
        mb.setText( BaseMessages.getString( PKG, "RepositoryExplorerDialog.Partition.Delete.Title" ) );
        mb.open();
      }
    } catch ( KettleException e ) {
      if ( mainController == null || !mainController.handleLostRepository( e ) ) {
        new ErrorDialog(
          shell,
          BaseMessages.getString( PKG, "RepositoryExplorerDialog.Partition.Delete.Title" ), BaseMessages.getString(
            PKG, "RepositoryExplorerDialog.Partition.Delete.UnexpectedError.Message" )
            + partitionSchemaName + "]", e );
      }
    } finally {
      refreshPartitions();
    }
  }

  public void refreshPartitions() {
    if ( repository != null ) {
      final List<UIPartition> tmpList = new ArrayList<UIPartition>();
      Runnable r = () -> {
        try {
          if ( repository instanceof RepositoryExtended ) {
            List<PartitionSchema> partitionSchemas = ((RepositoryExtended) repository).getPartitions( false );
            partitionSchemas.forEach( partitionSchema -> tmpList.add( new UIPartition( partitionSchema ) ) );
          } else {
            ObjectId[] partitionIdList = repository.getPartitionSchemaIDs( false );
            for ( ObjectId partitionId : partitionIdList ) {
              PartitionSchema partition = repository.loadPartitionSchema( partitionId, null );
              // Add the partition schema to the list
              tmpList.add( new UIPartition( partition ) );
            }
          }
        } catch ( KettleException e ) {
          if ( mainController == null || !mainController.handleLostRepository( e ) ) {
            // convert to runtime exception so it bubbles up through the UI
            throw new RuntimeException( e );
          }
        }
      };
      doWithBusyIndicator( r );
      partitionList.setChildren( tmpList );
    }
  }

  public void setEnableButtons( List<UIPartition> partitions ) {
    boolean enableEdit = false;
    boolean enableRemove = false;
    if ( partitions != null && partitions.size() > 0 ) {
      enableRemove = true;
      if ( partitions.size() == 1 ) {
        enableEdit = true;
      }
    }
    // Convenience - Leave 'new' enabled, modify 'edit' and 'remove'
    enableButtons( true, enableEdit, enableRemove );
  }

  public void enableButtons( boolean enableNew, boolean enableEdit, boolean enableRemove ) {
    XulButton bNew = (XulButton) document.getElementById( "partitions-new" );
    XulButton bEdit = (XulButton) document.getElementById( "partitions-edit" );
    XulButton bRemove = (XulButton) document.getElementById( "partitions-remove" );

    bNew.setDisabled( !enableNew );
    bEdit.setDisabled( !enableEdit );
    bRemove.setDisabled( !enableRemove );
  }

  public void tabClicked() {
    lazyInit();
  }

}
