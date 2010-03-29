/* Copyright (c) 2009 Pentaho Corporation.  All rights reserved. 
* This software was developed by Pentaho Corporation and is provided under the terms 
* of the GNU Lesser General Public License, Version 2.1. You may not use 
* this file except in compliance with the license. If you need a copy of the license, 
* please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
* Data Integration.  The Initial Developer is Pentaho Corporation.
*
* Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
* the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.ui.repository.repositoryexplorer.controllers;

import java.util.Collection;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.database.dialog.DatabaseDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.repository.repositoryexplorer.ControllerInitializationException;
import org.pentaho.di.ui.repository.repositoryexplorer.IUISupportController;
import org.pentaho.di.ui.repository.repositoryexplorer.RepositoryExplorer;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIDatabaseConnection;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIDatabaseConnections;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.swt.tags.SwtDialog;

public class ConnectionsController extends AbstractXulEventHandler implements IUISupportController {

  private static Class<?> PKG = RepositoryExplorer.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  private XulTree connectionsTable = null;

  protected BindingFactory bf = null;

  private Repository repository = null;;

  private boolean isRepReadOnly = true;

  private Binding bindButtonNew = null;

  private Binding bindButtonEdit = null;

  private Binding bindButtonRemove = null;

  private Shell shell = null;

  private boolean initComplete = false;

  private UIDatabaseConnections dbConnectionList = new UIDatabaseConnections();
  
  private DatabaseDialog databaseDialog;

  public ConnectionsController() {
  }

  @Override
  public String getName() {
    return "connectionsController"; //$NON-NLS-1$
  }

  public void init(Repository repository) throws ControllerInitializationException {
    this.repository = repository;
    setRepReadOnly(this.repository.getRepositoryMeta().getRepositoryCapabilities().isReadOnly());

    // Load the SWT Shell from the explorer dialog
    shell = ((SwtDialog) document.getElementById("repository-explorer-dialog")).getShell(); //$NON-NLS-1$
    bf = new DefaultBindingFactory();
    bf.setDocument(this.getXulDomContainer().getDocumentRoot());

    if (bf != null) {
      createBindings();
    }
    setEnableButtons(false);

    initComplete = true;
  }

  private DatabaseDialog getDatabaseDialog(){
    if(databaseDialog != null){
      return databaseDialog;
    }
    databaseDialog = new DatabaseDialog(shell);
    return databaseDialog;
  }
  
  private void createBindings() {
    connectionsTable = (XulTree) document.getElementById("connections-table"); //$NON-NLS-1$

    // Bind the connection table to a list of connections
    bf.setBindingType(Binding.Type.ONE_WAY);

    try {
      bf.createBinding(dbConnectionList, "children", connectionsTable, "elements").fireSourceChanged(); //$NON-NLS-1$ //$NON-NLS-2$
      (bindButtonNew = bf.createBinding(this, "repReadOnly", "connections-new", "disabled")).fireSourceChanged(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      (bindButtonEdit = bf.createBinding(this, "repReadOnly", "connections-edit", "disabled")).fireSourceChanged(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      (bindButtonRemove = bf.createBinding(this, "repReadOnly", "connections-remove", "disabled")).fireSourceChanged(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

      if (repository != null) {
        bf.createBinding(connectionsTable, "selectedItems", this, "enableButtons", //$NON-NLS-1$ //$NON-NLS-2$
            new BindingConvertor<List<UIDatabaseConnection>, Boolean>() {
              @Override
              public Boolean sourceToTarget(List<UIDatabaseConnection> dbConnList) {
                // Enable / Disable New,Edit,Remove buttons
                if (dbConnList != null && dbConnList.size() > 0) {
                  return true;
                }
                return false;
              }

              @Override
              public List<UIDatabaseConnection> targetToSource(Boolean enable) {
                return null;
              }
            });
      }
    } catch (Exception ex) {
      // convert to runtime exception so it bubbles up through the UI
      throw new RuntimeException(ex);
    }
    refreshConnectionList();
  }

  public Repository getRepository() {
    return repository;
  }

  public void setRepReadOnly(boolean isRepReadOnly) {
    try {
      if (this.isRepReadOnly != isRepReadOnly) {
        this.isRepReadOnly = isRepReadOnly;

        if (initComplete) {
          bindButtonNew.fireSourceChanged();
          bindButtonEdit.fireSourceChanged();
          bindButtonRemove.fireSourceChanged();
        }
      }
    } catch (Exception e) {
      // convert to runtime exception so it bubbles up through the UI
      throw new RuntimeException(e);
    }
  }

  public boolean isRepReadOnly() {
    return isRepReadOnly;
  }

  private void refreshConnectionList() {
    try {
      dbConnectionList.clear();
      ObjectId[] dbIdList = repository.getDatabaseIDs(false);
      for (ObjectId dbId : dbIdList) {
        DatabaseMeta dbMeta = repository.loadDatabaseMeta(dbId, null);

        // Add the database connection to the list
        dbConnectionList.add(new UIDatabaseConnection(dbMeta));
      }
    } catch (KettleException e) {
      // convert to runtime exception so it bubbles up through the UI
      throw new RuntimeException(e);
    }
  }

  public void createConnection() {
    try {
      DatabaseMeta databaseMeta = new DatabaseMeta();
      databaseMeta.initializeVariablesFrom(null);
      getDatabaseDialog().setDatabaseMeta(databaseMeta);
      
      String dbName = getDatabaseDialog().open();
      if (dbName != null && !dbName.equals(""))//$NON-NLS-1$
      {
        // See if this user connection exists...
        ObjectId idDatabase = repository.getDatabaseID(dbName);
        if (idDatabase == null) {
          repository.insertLogEntry(BaseMessages.getString(PKG, "ConnectionsController.Message.CreatingDatabase", getDatabaseDialog().getDatabaseMeta().getName()));//$NON-NLS-1$
          repository.save(getDatabaseDialog().getDatabaseMeta(), Const.VERSION_COMMENT_INITIAL_VERSION, null);
        } else {
          MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
          mb.setMessage(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Create.AlreadyExists.Message")); //$NON-NLS-1$
          mb.setText(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Create.AlreadyExists.Title")); //$NON-NLS-1$
          mb.open();
        }
      }
//    We should be able to tell the difference between a cancel and an empty database name
//
//      else {
//        MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
//        mb.setMessage(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Edit.MissingName.Message")); //$NON-NLS-1$
//        mb.setText(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Edit.MissingName.Title")); //$NON-NLS-1$
//        mb.open();
//      }
    } catch (KettleException e) {
      new ErrorDialog(
          shell,
          BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Create.UnexpectedError.Title"),  //$NON-NLS-1$
          BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Create.UnexpectedError.Message"), e); //$NON-NLS-1$
    } finally {
      refreshConnectionList();
    }
  }

  public void editConnection() {
    try {
      Collection<UIDatabaseConnection> connections = connectionsTable.getSelectedItems();

      if (connections != null && !connections.isEmpty()) {
        // Grab the first item in the list & send it to the database dialog
        DatabaseMeta databaseMeta = ((UIDatabaseConnection) connections.toArray()[0]).getDatabaseMeta();

        // Make sure this connection already exists and store its id for updating
        ObjectId idDatabase = repository.getDatabaseID(databaseMeta.getName());
        if (idDatabase == null) {
          MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
          mb.setMessage(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Edit.DoesNotExists.Message")); //$NON-NLS-1$
          mb.setText(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Edit.DoesNotExists.Title")); //$NON-NLS-1$
          mb.open();
        } else {
          getDatabaseDialog().setDatabaseMeta(databaseMeta);
          String dbName = getDatabaseDialog().open();
          if (dbName != null && !dbName.equals("")) //$NON-NLS-1$
          {
            repository.insertLogEntry(BaseMessages.getString(PKG, "ConnectionsController.Message.UpdatingDatabase", databaseMeta.getName()));//$NON-NLS-1$
            repository.save(databaseMeta, Const.VERSION_COMMENT_EDIT_VERSION, null);
          }
//          We should be able to tell the difference between a cancel and an empty database name 
//          
//          else {
//            MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
//            mb.setMessage(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Edit.MissingName.Message")); //$NON-NLS-1$
//            mb.setText(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Edit.MissingName.Title")); //$NON-NLS-1$
//            mb.open();            
//          }
        }
      } else {
        MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
        mb.setMessage(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Edit.NoItemSelected.Message")); //$NON-NLS-1$
        mb.setText(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Edit.NoItemSelected.Title")); //$NON-NLS-1$
        mb.open();
      }
    } catch (KettleException e) {
      new ErrorDialog(
          shell,
          BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Create.UnexpectedError.Title"), //$NON-NLS-1$
          BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Create.UnexpectedError.Message"), e); //$NON-NLS-1$
    } finally {
      refreshConnectionList();
    }
  }

  public void removeConnection() {
    try {
      Collection<UIDatabaseConnection> connections = connectionsTable.getSelectedItems();

      if (connections != null && !connections.isEmpty()) {
        // Grab the first item in the list & send it to the database dialog
        DatabaseMeta databaseMeta = ((UIDatabaseConnection) connections.toArray()[0]).getDatabaseMeta();

        // Make sure this connection already exists and store its id for updating
        ObjectId idDatabase = repository.getDatabaseID(databaseMeta.getName());
        if (idDatabase == null) {
          MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
          mb.setMessage(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Delete.DoesNotExists.Message", databaseMeta.getName())); //$NON-NLS-1$
          mb.setText(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Delete.Title")); //$NON-NLS-1$
          mb.open();
        } else {
          repository.deleteDatabaseMeta(databaseMeta.getName());
        }
      } else {
        MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
        mb.setMessage(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Edit.NoItemSelected.Message")); //$NON-NLS-1$
        mb.setText(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Delete.Title")); //$NON-NLS-1$
        mb.open();
      }
    } catch (KettleException e) {
      new ErrorDialog(
          shell,
          BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Create.UnexpectedError.Title"),  //$NON-NLS-1$
          BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Create.UnexpectedError.Message"), e); //$NON-NLS-1$
    } finally {
      refreshConnectionList();
    }
  }

  public void setEnableButtons(boolean enable) {
    // Convenience - Leave 'new' enabled, modify 'edit' and 'remove'
    enableButtons(true, enable, enable);
  }

  public void enableButtons(boolean enableNew, boolean enableEdit, boolean enableRemove) {
    XulButton bNew = (XulButton) document.getElementById("connections-new"); //$NON-NLS-1$
    XulButton bEdit = (XulButton) document.getElementById("connections-edit"); //$NON-NLS-1$
    XulButton bRemove = (XulButton) document.getElementById("connections-remove"); //$NON-NLS-1$

    bNew.setDisabled(!enableNew);
    bEdit.setDisabled(!enableEdit);
    bRemove.setDisabled(!enableRemove);
  }
}