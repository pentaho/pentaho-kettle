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

import java.lang.reflect.InvocationTargetException;
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
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.database.dialog.DatabaseDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.repository.dialog.RepositoryExplorerDialog;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIDatabaseConnection;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIDatabaseConnections;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryContent;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryDirectory;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryObject;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryObjectRevision;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryObjectRevisions;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.swt.tags.SwtDialog;

public class ConnectionsController extends AbstractXulEventHandler {

  private static Class<?> PKG = RepositoryExplorerDialog.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
  
  private XulTree connectionsTable = null;
  
  private XulTree revisionTable = null;
  
  private BindingFactory bf = null;
  
  private Repository repository = null;;
  
  private boolean isRepReadOnly = true;
  
  private Binding bindButtonNew = null;
  
  private Binding bindButtonEdit = null;
  
  private Binding bindButtonRemove = null;
  
  private boolean isRevSupported = false;
  
  private Binding bindRevisionTableDisable = null;
  
  private Binding bindRevisionLabelDisable = null;
  
  private Shell shell = null;
  
  private boolean initComplete = false;
  
  private UIDatabaseConnections dbConns = new UIDatabaseConnections();
  
  public ConnectionsController() {
  }
  
  @Override
  public String getName() {
    return "connectionsController"; //$NON-NLS-1$
  }
  
  public void init() {
    // Load the SWT Shell from the explorer dialog
    shell = ((SwtDialog)document.getElementById("repository-explorer-dialog")).getShell(); //$NON-NLS-1$

    if (bf!=null){
      createBindings();
    }
    setEnableButtons(false);
    initComplete = true;
  }
  
  private void createBindings(){
    connectionsTable = (XulTree) document.getElementById("connections-table"); //$NON-NLS-1$
    revisionTable = (XulTree) document.getElementById("connection-revision-table"); //$NON-NLS-1$
    
    // Bind the connection table to a list of connections
    bf.setBindingType(Binding.Type.ONE_WAY);

    try{
      bf.createBinding(dbConns, "children", connectionsTable, "elements").fireSourceChanged(); //$NON-NLS-1$ //$NON-NLS-2$
      
      (bindButtonNew = bf.createBinding(this, "repReadOnly", "connections-new", "disabled")).fireSourceChanged(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      (bindButtonEdit = bf.createBinding(this, "repReadOnly", "connections-edit", "disabled")).fireSourceChanged(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      (bindButtonRemove = bf.createBinding(this, "repReadOnly", "connections-remove", "disabled")).fireSourceChanged(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      
      (bindRevisionTableDisable = bf.createBinding(this, "revSupported", "connection-revision-table", "!disabled")).fireSourceChanged(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      (bindRevisionLabelDisable = bf.createBinding(this, "revSupported", "connection-revision-label", "!disabled")).fireSourceChanged(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      
      if (repository != null){
        bf.createBinding(connectionsTable, "selectedItems", this, "enableButtons", //$NON-NLS-1$ //$NON-NLS-2$
          new BindingConvertor<List<UIDatabaseConnection>, Boolean>() {
            @Override
            public Boolean sourceToTarget(List<UIDatabaseConnection> dbConnList) {
              // Enable / Disable New,Edit,Remove buttons
              if(dbConnList != null && dbConnList.size() > 0) {
                return true;
              }
                return false;
            }
            
            @Override
            public List<UIDatabaseConnection> targetToSource(Boolean enable) {
              return null;
            }
        });
        
        bf.createBinding(connectionsTable, "selectedItems", revisionTable, "elements", //$NON-NLS-1$ //$NON-NLS-2$
          new BindingConvertor<List<UIDatabaseConnection>, UIRepositoryObjectRevisions>() {
            @Override
            public UIRepositoryObjectRevisions sourceToTarget(List<UIDatabaseConnection> dbConnList) {
              UIRepositoryObjectRevisions revisions = new UIRepositoryObjectRevisions();
              if(repository.getRepositoryMeta().getRepositoryCapabilities().supportsRevisions()) {
                if(dbConnList==null){
                  return null;
                }
                if (dbConnList.size()<=0){
                  return null;
                }
                try {
                  UIDatabaseConnection dbConn = (UIDatabaseConnection)dbConnList.get(0);
                  revisions = dbConn.getRevisions();
                  bf.createBinding(revisions,"children", revisionTable, "elements").fireSourceChanged(); //$NON-NLS-1$ //$NON-NLS-2$
                } catch (Exception e) {
                  // how do we handle exceptions in a binding? dialog here? 
                  // TODO: handle exception
                }
              }
              return revisions;
            }
            @Override
            public List<UIDatabaseConnection> targetToSource(UIRepositoryObjectRevisions elements) {
              return null;
            }
          });
      }
    } catch (Exception ex) {
      System.err.println(ex.getMessage());
    }
    refreshConnectionList();
  }
  
  public void setBindingFactory(BindingFactory bindingFactory) {
    this.bf = bindingFactory;
  }

  public void setRepository(Repository repository) {
    if(this.repository == null || !this.repository.equals(repository)) {
      this.repository = repository;
      
      setRepReadOnly(this.repository.getRepositoryMeta().getRepositoryCapabilities().isReadOnly());
      setRevSupported(this.repository.getRepositoryMeta().getRepositoryCapabilities().supportsRevisions());
    }
  }

  public Repository getRepository() {
    return repository;
  }
  
  public void setRepReadOnly(boolean isRepReadOnly) {
    try {
      if(this.isRepReadOnly != isRepReadOnly) {
        this.isRepReadOnly = isRepReadOnly;
        
        if(initComplete) {
          bindButtonNew.fireSourceChanged();
          bindButtonEdit.fireSourceChanged();
          bindButtonRemove.fireSourceChanged();
        }
      }
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }

  public boolean isRepReadOnly() {
    return isRepReadOnly;
  }

  public void setRevSupported(boolean isRevSupported) {
    try {
      if(this.isRevSupported != isRevSupported) {
        this.isRevSupported = isRevSupported;
        
        if(initComplete) {
          bindRevisionTableDisable.fireSourceChanged();
          bindRevisionLabelDisable.fireSourceChanged();
        }
      }
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }

  public boolean isRevSupported() {
    return isRevSupported;
  }

  private void refreshConnectionList() {
    try {
      dbConns.clear();
      ObjectId[] dbIdList = repository.getDatabaseIDs(false);
      for(ObjectId dbId : dbIdList) {
        DatabaseMeta dbMeta = repository.loadDatabaseMeta(dbId, null);
        // Fetch revision history if applicable
        List<ObjectRevision> revHist = null;
        if(isRevSupported) {
          revHist = repository.getRevisions(dbMeta);
        }
        
        // Add the database connection to the list
        dbConns.add(new UIDatabaseConnection(dbMeta, revHist));
      }
    } catch (KettleException e) {
        System.err.println(e.getMessage());
    }
  }
  
  public void createConnection() {
    try
    {
      DatabaseMeta databaseMeta = new DatabaseMeta();
      databaseMeta.initializeVariablesFrom(null);
      DatabaseDialog dd = new DatabaseDialog(shell, databaseMeta);
      String dbName = dd.open();
      if (dbName!=null)
      {
        // See if this user connection exists...
        ObjectId idDatabase = repository.getDatabaseID(dbName);
        if (idDatabase==null)
        {
          repository.insertLogEntry("Creating new database '"+databaseMeta.getName()+"'");
          repository.save(databaseMeta, Const.VERSION_COMMENT_INITIAL_VERSION, null);
        }
        else
        {
          MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
          mb.setMessage(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Create.AlreadyExists.Message")); //$NON-NLS-1$
          mb.setText(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Create.AlreadyExists.Title")); //$NON-NLS-1$
          mb.open();
        }
        
        refreshConnectionList();        
      }
    }
    catch(KettleException e)
    {
      new ErrorDialog(shell, BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Create.UnexpectedError.Title"), BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Create.UnexpectedError.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }
  
  public void editConnection() {
    try
    {
      Collection<UIDatabaseConnection> connections = connectionsTable.getSelectedItems();
      
      if(connections != null && !connections.isEmpty()) {
        // Grab the first item in the list & send it to the database dialog
        DatabaseMeta databaseMeta = ((UIDatabaseConnection)connections.toArray()[0]).getDatabaseMeta();

        // Make sure this connection already exists and store its id for updating
        ObjectId idDatabase = repository.getDatabaseID(databaseMeta.getName());
        String originalDbName = databaseMeta.getName();
        if (idDatabase==null) {
          MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
          mb.setMessage(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Edit.DoesNotExists.Message")); //$NON-NLS-1$
          mb.setText(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Edit.DoesNotExists.Title")); //$NON-NLS-1$
          mb.open();
        } else {
          boolean save = true;
          DatabaseDialog dd = new DatabaseDialog(shell, databaseMeta);
          String dbName = dd.open();
          if (dbName!=null)
          {
            if(!dbName.equals(originalDbName)) {
              // Make sure the new name does not already exist
              if(repository.getDatabaseID(dbName) != null) {
                MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
                mb.setMessage(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Edit.AlreadyExists.Message", dbName)); //$NON-NLS-1$
                mb.setText(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Edit.AlreadyExists.Title")); //$NON-NLS-1$
                mb.open();
                save = false;
              } else {
                repository.insertLogEntry("Updating database connection name from '" + originalDbName + "' to '" + databaseMeta.getName() + "'");
                repository.renameDatabase(idDatabase, dbName);
              }
            }
            
            if(save) {
              repository.insertLogEntry("Updating database connection '"+databaseMeta.getName()+"'");
              repository.save(databaseMeta, Const.VERSION_COMMENT_EDIT_VERSION, null);
              refreshConnectionList();
            }
          }
        }
      } else {
        MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
        mb.setMessage(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Edit.NoItemSelected.Message")); //$NON-NLS-1$
        mb.setText(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Edit.NoItemSelected.Title")); //$NON-NLS-1$
        mb.open();
      }
    }
    catch(KettleException e)
    {
      new ErrorDialog(shell, BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Create.UnexpectedError.Title"), BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Create.UnexpectedError.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }
  
  public void removeConnection() {
    try
    {
      Collection<UIDatabaseConnection> connections = connectionsTable.getSelectedItems();
      
      if(connections != null && !connections.isEmpty()) {
        // Grab the first item in the list & send it to the database dialog
        DatabaseMeta databaseMeta = ((UIDatabaseConnection)connections.toArray()[0]).getDatabaseMeta();

        // Make sure this connection already exists and store its id for updating
        ObjectId idDatabase = repository.getDatabaseID(databaseMeta.getName());
        if (idDatabase==null) {
          MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
          mb.setMessage(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Delete.DoesNotExists.Message", databaseMeta.getName())); //$NON-NLS-1$
          mb.setText(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Delete.Title")); //$NON-NLS-1$
          mb.open();
        } else {
          repository.deleteDatabaseMeta(databaseMeta.getName());
          refreshConnectionList();
        }
      } else {
        MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
        mb.setMessage(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Edit.NoItemSelected.Message")); //$NON-NLS-1$
        mb.setText(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Delete.Title")); //$NON-NLS-1$
        mb.open();
      }
    }
    catch(KettleException e)
    {
      new ErrorDialog(shell, BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Create.UnexpectedError.Title"), BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Create.UnexpectedError.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
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