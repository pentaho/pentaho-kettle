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
import org.pentaho.di.ui.cluster.dialog.SlaveServerDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.repository.dialog.RepositoryExplorerDialog;
import org.pentaho.di.ui.repository.repositoryexplorer.ControllerInitializationException;
import org.pentaho.di.ui.repository.repositoryexplorer.IUISupportController;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UISlave;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UISlaves;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.swt.SwtBindingFactory;
import org.pentaho.ui.xul.swt.tags.SwtDialog;

public class SlavesController extends LazilyInitializedController implements IUISupportController {

  private static Class<?> PKG = RepositoryExplorerDialog.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  private XulTree slavesTable = null;
  
  protected BindingFactory bf = null;
  
  private Shell shell = null;
  
  private UISlaves slaveList = new UISlaves();

  public SlavesController() {
  }

  public void init(Repository repository) throws ControllerInitializationException {
    this.repository = repository;
  }
  
  @Override
  public String getName() {
    return "slavesController"; //$NON-NLS-1$
  }

  public void createBindings() {
    refreshSlaves();
    try {
      slavesTable = (XulTree) document.getElementById("slaves-table"); //$NON-NLS-1$
      bf.setBindingType(Binding.Type.ONE_WAY);
      bf.createBinding(slaveList, "children", slavesTable, "elements").fireSourceChanged(); //$NON-NLS-1$ //$NON-NLS-2$
      bf.createBinding(slavesTable, "selectedItems", this, "enableButtons"); //$NON-NLS-1$ //$NON-NLS-2$
    } catch (Exception e) {
      // convert to runtime exception so it bubbles up through the UI
      throw new RuntimeException(e);
    }
  }

  @Override
  protected boolean doLazyInit() {
    // Load the SWT Shell from the explorer dialog
    shell = ((SwtDialog)document.getElementById("repository-explorer-dialog")).getShell(); //$NON-NLS-1$
    
    enableButtons(true, false, false);
    bf = new SwtBindingFactory();
    bf.setDocument(this.getXulDomContainer().getDocumentRoot());

    if (bf!=null){
      createBindings();
    }
    return true;
  }
  
  public void refreshSlaves() {
    if(repository != null) {
      final List<UISlave> tmpList = new ArrayList<UISlave>();
      Runnable r = new Runnable() {
        public void run() {
          try {
            ObjectId[] slaveIdList = repository.getSlaveIDs(false);
            
            for(ObjectId slaveId : slaveIdList) {
              SlaveServer slave = repository.loadSlaveServer(slaveId, null);
              // Add the database slave to the list
              tmpList.add(new UISlave(slave));
            }
          } catch (KettleException e) {
            // convert to runtime exception so it bubbles up through the UI
            throw new RuntimeException(e);
          }
        
        }
      };
      doWithBusyIndicator(r);
      slaveList.setChildren(tmpList);
    }
  }
  
  public void createSlave() {
    try
    {
      // Create a new SlaveServer for storing the result
      SlaveServer slaveServer = new SlaveServer();
      
      SlaveServerDialog ssd = new SlaveServerDialog(shell, slaveServer);
      if(ssd.open()) {
        ObjectId slaveId = repository.getSlaveID(slaveServer.getName());
        
        // Make sure the slave does not already exist
        if(slaveId != null) {
          MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
          mb.setMessage(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Slave.AlreadyExists.Message")); //$NON-NLS-1$
          mb.setText(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Slave.Create.Title")); //$NON-NLS-1$
          mb.open();
        } else {
          if(slaveServer.getName() != null && !slaveServer.getName().equals("")) {//$NON-NLS-1$
            repository.insertLogEntry(BaseMessages.getString(PKG, "SlavesController.Message.CreatingSlave",slaveServer.getName()));//$NON-NLS-1$
            
            
            repository.save(slaveServer, Const.VERSION_COMMENT_INITIAL_VERSION, null);
          } else {
            MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
            mb.setMessage(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Slave.Edit.InvalidName.Message")); //$NON-NLS-1$
            mb.setText(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Slave.Create.Title")); //$NON-NLS-1$
            mb.open();
          }
        }
      }
    }
    catch(KettleException e)
    {
      new ErrorDialog(shell, BaseMessages.getString(PKG, "RepositoryExplorerDialog.Slave.Create.Title"), //$NON-NLS-1$
          BaseMessages.getString(PKG, "RepositoryExplorerDialog.Slave.Create.UnexpectedError.Message"), e); //$NON-NLS-1$
    } finally {
      refreshSlaves();
    }
  }
  
  public void editSlave() {
    String slaveServerName = ""; //$NON-NLS-1$
    try
    {
      Collection<UISlave> slaves = slavesTable.getSelectedItems();
      
      if(slaves != null && !slaves.isEmpty()) {
        // Grab the first item in the list & send it to the slave dialog
        SlaveServer slaveServer = ((UISlave)slaves.toArray()[0]).getSlaveServer();
        slaveServerName = slaveServer.getName();
        // Make sure the slave already exists
        ObjectId slaveId = repository.getSlaveID(slaveServer.getName());
        if(slaveId == null) {
          MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
          mb.setMessage(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Slave.DoesNotExists.Message")); //$NON-NLS-1$
          mb.setText(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Slave.Edit.Title")); //$NON-NLS-1$
          mb.open();
        } else {
          SlaveServerDialog ssd = new SlaveServerDialog(shell, slaveServer);
          if(ssd.open()) {
            if(slaveServer.getName() != null && !slaveServer.getName().equals("")) {//$NON-NLS-1$
              repository.insertLogEntry(BaseMessages.getString(PKG, "SlavesController.Message.UpdatingSlave",slaveServer.getName()));//$NON-NLS-1$
              repository.save(slaveServer, Const.VERSION_COMMENT_EDIT_VERSION, null);
            } else {
              MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
              mb.setMessage(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Slave.Edit.InvalidName.Message")); //$NON-NLS-1$
              mb.setText(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Slave.Edit.Title")); //$NON-NLS-1$
              mb.open();
            }
          }
        }
      } else {
        MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
        mb.setMessage(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Slave.NoItemSelected.Message")); //$NON-NLS-1$
        mb.setText(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Slave.Edit.Title")); //$NON-NLS-1$
        mb.open();
      }
    }
    catch(KettleException e)
    {
        new ErrorDialog(shell, BaseMessages.getString(PKG, "RepositoryExplorerDialog.Slave.Edit.Title"),  //$NON-NLS-1$
            BaseMessages.getString(PKG, "RepositoryExplorerDialog.Slave.Edit.UnexpectedError.Message")+slaveServerName+"]", e); //$NON-NLS-1$ //$NON-NLS-2$
    } finally {
      refreshSlaves();
    }
  }
  
  public void removeSlave() {
    String slaveServerName = ""; //$NON-NLS-1$
    try
    {
      Collection<UISlave> slaves = slavesTable.getSelectedItems();
      if(slaves != null && !slaves.isEmpty()) {
        for(Object obj:slaves) {
          if(obj != null && obj instanceof UISlave) {
            UISlave slave = (UISlave) obj;
            SlaveServer slaveServer = slave.getSlaveServer();
            slaveServerName = slaveServer.getName();
            // Make sure the slave to delete exists in the repository
            ObjectId slaveId = repository.getSlaveID(slaveServer.getName());
            if(slaveId == null) {
              MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
              mb.setMessage(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Slave.DoesNotExists.Message")); //$NON-NLS-1$
              mb.setText(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Slave.Delete.Title")); //$NON-NLS-1$
              mb.open();
            } else {
              repository.deleteSlave(slaveId);
            }
          }
        }
      } else {
        MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
        mb.setMessage(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Slave.NoItemSelected.Message")); //$NON-NLS-1$
        mb.setText(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Slave.Delete.Title")); //$NON-NLS-1$
        mb.open();
      }
    }
    catch(KettleException e)
    {
      new ErrorDialog(shell, BaseMessages.getString(PKG, "RepositoryExplorerDialog.Slave.Delete.Title"),  //$NON-NLS-1$
          BaseMessages.getString(PKG, "RepositoryExplorerDialog.Slave.Delete.UnexpectedError.Message")+slaveServerName+"]", e); //$NON-NLS-1$ //$NON-NLS-2$
    } finally {
      refreshSlaves();
    }
  }
  
  public void setEnableButtons(List<UISlave> slaves) {
    boolean enableEdit = false;
    boolean enableRemove = false;
    if(slaves != null && slaves.size() > 0) {
      enableRemove = true;
      if(slaves.size() == 1) {
        enableEdit = true;
      }
    }
    // Convenience - Leave 'new' enabled, modify 'edit' and 'remove'
    enableButtons(true, enableEdit, enableRemove);
  }
  
  public void enableButtons(boolean enableNew, boolean enableEdit, boolean enableRemove) {
    XulButton bNew = (XulButton) document.getElementById("slaves-new"); //$NON-NLS-1$
    XulButton bEdit = (XulButton) document.getElementById("slaves-edit"); //$NON-NLS-1$
    XulButton bRemove = (XulButton) document.getElementById("slaves-remove"); //$NON-NLS-1$
    
    bNew.setDisabled(!enableNew);
    bEdit.setDisabled(!enableEdit);
    bRemove.setDisabled(!enableRemove);
  }

  public void tabClicked() {
    lazyInit();
  }
  
}
