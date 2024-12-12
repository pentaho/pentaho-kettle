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
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryExtended;
import org.pentaho.di.ui.cluster.dialog.SlaveServerDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.repository.dialog.RepositoryExplorerDialog;
import org.pentaho.di.ui.repository.repositoryexplorer.ControllerInitializationException;
import org.pentaho.di.ui.repository.repositoryexplorer.IUISupportController;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UISlave;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UISlaves;
import org.pentaho.di.ui.spoon.SharedObjectSyncUtil;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.swt.SwtBindingFactory;
import org.pentaho.ui.xul.swt.tags.SwtDialog;

public class SlavesController extends LazilyInitializedController implements IUISupportController {

  private static Class<?> PKG = RepositoryExplorerDialog.class; // for i18n purposes, needed by Translator2!!

  private XulTree slavesTable = null;

  protected BindingFactory bf = null;

  private Shell shell = null;

  private UISlaves slaveList = new UISlaves();

  private MainController mainController;

  public SlavesController() {
  }

  public void init( Repository repository ) throws ControllerInitializationException {
    this.repository = repository;
  }

  @Override
  public String getName() {
    return "slavesController";
  }

  public void createBindings() {
    refreshSlaves();
    try {
      slavesTable = (XulTree) document.getElementById( "slaves-table" );
      bf.setBindingType( Binding.Type.ONE_WAY );
      bf.createBinding( slaveList, "children", slavesTable, "elements" ).fireSourceChanged();
      bf.createBinding( slavesTable, "selectedItems", this, "enableButtons" );
    } catch ( Exception e ) {
      if ( mainController == null || !mainController.handleLostRepository( e ) ) {
        // convert to runtime exception so it bubbles up through the UI
        throw new RuntimeException( e );
      }
    }
  }

  @Override
  protected boolean doLazyInit() {
    // Load the SWT Shell from the explorer dialog
    shell = ( (SwtDialog) document.getElementById( "repository-explorer-dialog" ) ).getShell();

    enableButtons( true, false, false );
    bf = new SwtBindingFactory();
    bf.setDocument( this.getXulDomContainer().getDocumentRoot() );

    try {
      mainController = (MainController) this.getXulDomContainer().getEventHandler( "mainController" );
    } catch ( XulException e ) {
      return false;
    }

    if ( bf != null ) {
      createBindings();
    }

    return true;
  }

  public void refreshSlaves() {
    if ( repository != null ) {
      final List<UISlave> tmpList = new ArrayList<UISlave>();
      Runnable r = () -> {
        try {
          List<SlaveServer> slaveServers;
          if ( repository instanceof RepositoryExtended ) {
            slaveServers = ((RepositoryExtended) repository).getSlaveServers( false );
          } else {
            slaveServers = repository.getSlaveServers();
          }
          if ( slaveServers != null ) {
            slaveServers.forEach( slaveServer -> tmpList.add( new UISlave( slaveServer ) ) );
          }
        } catch ( KettleException e ) {
          if ( mainController == null || !mainController.handleLostRepository( e ) ) {
            // convert to runtime exception so it bubbles up through the UI
            throw new RuntimeException( e );
          }
        }
      };
      doWithBusyIndicator( r );
      slaveList.setChildren( tmpList );
    }
  }

  public void createSlave() {
    try {
      // Create a new SlaveServer for storing the result
      SlaveServer slaveServer = new SlaveServer();

      SlaveServerDialog ssd = new SlaveServerDialog( shell, slaveServer );
      if ( ssd.open() ) {
        ObjectId slaveId = repository.getSlaveID( slaveServer.getName() );

        // Make sure the slave does not already exist
        if ( slaveId != null ) {
          MessageBox mb = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
          mb.setMessage( BaseMessages.getString(
            PKG, "RepositoryExplorerDialog.Slave.AlreadyExists.Message", slaveServer.getName() ) );
          mb.setText( BaseMessages.getString( PKG, "RepositoryExplorerDialog.Slave.Create.Title" ) );
          mb.open();
        } else {
          if ( slaveServer.getName() != null && !slaveServer.getName().equals( "" ) ) {
            repository.insertLogEntry( BaseMessages.getString(
              PKG, "SlavesController.Message.CreatingSlave", slaveServer.getName() ) );
            repository.save( slaveServer, Const.VERSION_COMMENT_INITIAL_VERSION, null );
          } else {
            MessageBox mb = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
            mb
              .setMessage( BaseMessages.getString(
                PKG, "RepositoryExplorerDialog.Slave.Edit.InvalidName.Message" ) );
            mb.setText( BaseMessages.getString( PKG, "RepositoryExplorerDialog.Slave.Create.Title" ) );
            mb.open();
          }
        }
      }
    } catch ( KettleException e ) {
      if ( mainController == null || !mainController.handleLostRepository( e ) ) {
        new ErrorDialog(
          shell, BaseMessages.getString( PKG, "RepositoryExplorerDialog.Slave.Create.Title" ), BaseMessages
            .getString( PKG, "RepositoryExplorerDialog.Slave.Create.UnexpectedError.Message" ), e );
      }
    } finally {
      refreshSlaves();
    }
  }

  public void editSlave() {
    String slaveServerName = "";
    try {
      Collection<UISlave> slaves = slavesTable.getSelectedItems();

      if ( slaves != null && !slaves.isEmpty() ) {
        // Grab the first item in the list & send it to the slave dialog
        SlaveServer slaveServer = ( (UISlave) slaves.toArray()[0] ).getSlaveServer();
        slaveServerName = slaveServer.getName();
        // Make sure the slave already exists
        ObjectId slaveId = repository.getSlaveID( slaveServer.getName() );
        if ( slaveId == null ) {
          MessageBox mb = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
          mb.setMessage( BaseMessages.getString(
            PKG, "RepositoryExplorerDialog.Slave.DoesNotExists.Message", slaveServerName ) );
          mb.setText( BaseMessages.getString( PKG, "RepositoryExplorerDialog.Slave.Edit.Title" ) );
          mb.open();
        } else {
          SlaveServerDialog ssd = new SlaveServerDialog( shell, slaveServer );
          if ( ssd.open() ) {
            if ( slaveServer.getName() != null && !slaveServer.getName().equals( "" ) ) {
              repository.insertLogEntry( BaseMessages.getString(
                PKG, "SlavesController.Message.UpdatingSlave", slaveServer.getName() ) );
              repository.save( slaveServer, Const.VERSION_COMMENT_EDIT_VERSION, null );
              if ( getSharedObjectSyncUtil() != null ) {
                getSharedObjectSyncUtil().synchronizeSlaveServers( slaveServer, slaveServerName );
              }
            } else {
              MessageBox mb = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
              mb.setMessage( BaseMessages.getString(
                PKG, "RepositoryExplorerDialog.Slave.Edit.InvalidName.Message" ) );
              mb.setText( BaseMessages.getString( PKG, "RepositoryExplorerDialog.Slave.Edit.Title" ) );
              mb.open();
            }
          }
        }
      } else {
        MessageBox mb = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
        mb.setMessage( BaseMessages.getString( PKG, "RepositoryExplorerDialog.Slave.NoItemSelected.Message" ) );
        mb.setText( BaseMessages.getString( PKG, "RepositoryExplorerDialog.Slave.Edit.Title" ) );
        mb.open();
      }
    } catch ( KettleException e ) {
      if ( mainController == null || !mainController.handleLostRepository( e ) ) {
        new ErrorDialog(
          shell, BaseMessages.getString( PKG, "RepositoryExplorerDialog.Slave.Edit.Title" ), BaseMessages
            .getString( PKG, "RepositoryExplorerDialog.Slave.Edit.UnexpectedError.Message" )
            + slaveServerName + "]", e );
      }
    } finally {
      refreshSlaves();
    }
  }

  public void removeSlave() {
    String slaveServerName = "";
    try {
      Collection<UISlave> slaves = slavesTable.getSelectedItems();
      if ( slaves != null && !slaves.isEmpty() ) {
        for ( Object obj : slaves ) {
          if ( obj != null && obj instanceof UISlave ) {
            UISlave slave = (UISlave) obj;
            SlaveServer slaveServer = slave.getSlaveServer();
            slaveServerName = slaveServer.getName();
            // Make sure the slave to delete exists in the repository
            ObjectId slaveId = repository.getSlaveID( slaveServer.getName() );
            if ( slaveId == null ) {
              MessageBox mb = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
              mb.setMessage( BaseMessages.getString(
                PKG, "RepositoryExplorerDialog.Slave.DoesNotExists.Message", slaveServerName ) );
              mb.setText( BaseMessages.getString( PKG, "RepositoryExplorerDialog.Slave.Delete.Title" ) );
              mb.open();
            } else {
              repository.deleteSlave( slaveId );
              if ( getSharedObjectSyncUtil() != null ) {
                getSharedObjectSyncUtil().deleteSlaveServer( slaveServer );
              }
            }
          }
        }
      } else {
        MessageBox mb = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
        mb.setMessage( BaseMessages.getString( PKG, "RepositoryExplorerDialog.Slave.NoItemSelected.Message" ) );
        mb.setText( BaseMessages.getString( PKG, "RepositoryExplorerDialog.Slave.Delete.Title" ) );
        mb.open();
      }
    } catch ( KettleException e ) {
      if ( mainController == null || !mainController.handleLostRepository( e ) ) {
        new ErrorDialog(
          shell, BaseMessages.getString( PKG, "RepositoryExplorerDialog.Slave.Delete.Title" ), BaseMessages
            .getString( PKG, "RepositoryExplorerDialog.Slave.Delete.UnexpectedError.Message" )
            + slaveServerName + "]", e );
      }
    } finally {
      refreshSlaves();
    }
  }

  public void setEnableButtons( List<UISlave> slaves ) {
    boolean enableEdit = false;
    boolean enableRemove = false;
    if ( slaves != null && slaves.size() > 0 ) {
      enableRemove = true;
      if ( slaves.size() == 1 ) {
        enableEdit = true;
      }
    }
    // Convenience - Leave 'new' enabled, modify 'edit' and 'remove'
    enableButtons( true, enableEdit, enableRemove );
  }

  public void enableButtons( boolean enableNew, boolean enableEdit, boolean enableRemove ) {
    XulButton bNew = (XulButton) document.getElementById( "slaves-new" );
    XulButton bEdit = (XulButton) document.getElementById( "slaves-edit" );
    XulButton bRemove = (XulButton) document.getElementById( "slaves-remove" );

    bNew.setDisabled( !enableNew );
    bEdit.setDisabled( !enableEdit );
    bRemove.setDisabled( !enableRemove );
  }

  public void tabClicked() {
    lazyInit();
  }

  private SharedObjectSyncUtil getSharedObjectSyncUtil() {
    return mainController != null ? mainController.getSharedObjectSyncUtil() : null;
  }

}
