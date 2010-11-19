/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.di.ui.repository.controllers;

import java.util.ResourceBundle;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.ui.repository.ILoginCallback;
import org.pentaho.di.ui.repository.RepositoriesHelper;
import org.pentaho.di.ui.repository.dialog.RepositoryDialogInterface;
import org.pentaho.di.ui.repository.model.RepositoriesModel;
import org.pentaho.di.ui.repository.repositoryexplorer.ControllerInitializationException;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.WaitBoxRunnable;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulCheckbox;
import org.pentaho.ui.xul.components.XulConfirmBox;
import org.pentaho.ui.xul.components.XulMessageBox;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.components.XulWaitBox;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulListbox;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

public class RepositoriesController extends AbstractXulEventHandler {
  
  private static Class<?> PKG = RepositoryDialogInterface.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
  
  private ResourceBundle messages;

  private BindingFactory bf;

  private XulDialog loginDialog;

  private XulTextbox username;

  private XulTextbox userPassword;

  private XulListbox availableRepositories;

  private XulButton repositoryEditButton;

  private XulButton repositoryRemoveButton;

  private XulCheckbox showAtStartup;

  private RepositoriesModel loginModel;
  
  private XulButton okButton;
  
  private XulButton cancelButton;
  
  private XulMessageBox messageBox;

  private XulConfirmBox confirmBox;

  private RepositoriesHelper helper;
  private String preferredRepositoryName;
  private ILoginCallback callback;
  
  private Shell shell;
  
  public RepositoriesController() {
    super();
    loginModel = new RepositoriesModel();
  }

  public void init() throws ControllerInitializationException {
    // TODO Initialize the Repository Login Dialog
    try {
      messageBox = (XulMessageBox) document.createElement("messagebox");//$NON-NLS-1$
      confirmBox = (XulConfirmBox) document.createElement("confirmbox");//$NON-NLS-1$
    } catch (Exception e) {
      throw new ControllerInitializationException(e);
    }
    if (bf != null) {
      createBindings();
    }
  }

  private void createBindings() {
    loginDialog = (XulDialog) document.getElementById("repository-login-dialog");//$NON-NLS-1$

    repositoryEditButton = (XulButton) document.getElementById("repository-edit");//$NON-NLS-1$
    repositoryRemoveButton = (XulButton) document.getElementById("repository-remove");//$NON-NLS-1$

    username = (XulTextbox) document.getElementById("user-name");//$NON-NLS-1$
    userPassword = (XulTextbox) document.getElementById("user-password");//$NON-NLS-1$
    availableRepositories = (XulListbox) document.getElementById("available-repository-list");//$NON-NLS-1$
    showAtStartup = (XulCheckbox) document.getElementById("show-login-dialog-at-startup");//$NON-NLS-1$
    okButton = (XulButton) document.getElementById("repository-login-dialog_accept"); //$NON-NLS-1$
    cancelButton = (XulButton) document.getElementById("repository-login-dialog_cancel"); //$NON-NLS-1$
    bf.setBindingType(Binding.Type.BI_DIRECTIONAL);
    bf.createBinding(loginModel, "username", username, "value");//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(loginModel, "password", userPassword, "value");//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(loginModel, "availableRepositories", availableRepositories, "elements");//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(loginModel, "selectedRepository", availableRepositories, "selectedItem");//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(loginModel, "showDialogAtStartup", showAtStartup, "checked");//$NON-NLS-1$ //$NON-NLS-2$
    bf.setBindingType(Binding.Type.ONE_WAY);
    bf.createBinding(loginModel, "valid", okButton, "!disabled");//$NON-NLS-1$ //$NON-NLS-2$

    BindingConvertor<RepositoryMeta, Boolean> buttonConverter = new BindingConvertor<RepositoryMeta, Boolean>() {
      @Override
      public Boolean sourceToTarget(RepositoryMeta value) {
        return (value == null);
      }
      @Override
      public RepositoryMeta targetToSource(Boolean value) {
        return null;
      }
    };

    BindingConvertor<RepositoryMeta, Boolean> userpassConverter = new BindingConvertor<RepositoryMeta, Boolean>() {
      @Override
      public Boolean sourceToTarget(RepositoryMeta value) {
        return (value == null) || !value.getRepositoryCapabilities().supportsUsers();
      }
      @Override
      public RepositoryMeta targetToSource(Boolean value) {
        return null;
      }
    };

    bf.createBinding(loginModel, "selectedRepository", username, "disabled", userpassConverter);//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(loginModel, "selectedRepository", userPassword, "disabled", userpassConverter);//$NON-NLS-1$ //$NON-NLS-2$

    bf.createBinding(loginModel, "selectedRepository", repositoryEditButton, "disabled", buttonConverter);//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(loginModel, "selectedRepository", repositoryRemoveButton, "disabled", buttonConverter);//$NON-NLS-1$ //$NON-NLS-2$

    final Shell loginShell = (Shell) loginDialog.getRootObject();

    helper = new RepositoriesHelper(loginModel, document, loginShell);
    helper.setPreferredRepositoryName(preferredRepositoryName);
    helper.getMetaData();
  }

  public void setBindingFactory(BindingFactory bf) {
    this.bf = bf;
  }

  public BindingFactory getBindingFactory() {
    return this.bf;
  }

  public String getName() {
    return "repositoryLoginController"; //$NON-NLS-1$
  }
  
  public void show(){
    if(loginModel.getUsername() != null){
      userPassword.setFocus();
    } else {
      username.setFocus();
    }
    loginDialog.show();
  }

  public void login() {
    if(loginModel.isValid() == false){
      return;
    }
    XulWaitBox box;
    try {
      box = (XulWaitBox) document.createElement("waitbox");
      box.setIndeterminate(true);
      box.setCanCancel(false);
      box.setTitle(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Wait.Title"));
      box.setMessage(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Wait.Message"));
      final Shell loginShell = (Shell) loginDialog.getRootObject();
      final Display display = loginShell.getDisplay();
      box.setDialogParent(loginShell);
      box.setRunnable(new WaitBoxRunnable(box){
        @Override
        public void run() {
          try {
            helper.loginToRepository();

            waitBox.stop();
            display.syncExec(new Runnable(){
              public void run() {                
                loginDialog.hide();
                okButton.setDisabled(false);
                cancelButton.setDisabled(false);

                if (helper.getConnectedRepository().getConnectMessage() != null) {
                  getMessageBox().setTitle(BaseMessages.getString(PKG, "ConnectMessageTitle")); //$NON-NLS-1$
                  getMessageBox().setMessage(helper.getConnectedRepository().getConnectMessage());
                  getMessageBox().open();
                }

                getCallback().onSuccess(helper.getConnectedRepository());
              }
            });
            
          } catch (final Throwable th) {

            waitBox.stop();
            
            try {
              display.syncExec(new Runnable(){
                public void run() {
                  
                  getCallback().onError(th);
                  okButton.setDisabled(false);
                  cancelButton.setDisabled(false);
                }
              });
            } catch (Exception e) {
              e.printStackTrace();
            }
              
          }
        }

        @Override
        public void cancel() {
        }
        
      });
      okButton.setDisabled(true);
      cancelButton.setDisabled(true);
      box.start();
    } catch (XulException e1) {
      getCallback().onError(e1);
    }
  }

  /**
   * Executed when the user clicks the new repository image from the Repository Login Dialog
   * It present a new dialog where the user can selected what type of repository to create
   */
  public void newRepository() {
    helper.newRepository();
  }
  /**
   * Executed when the user clicks the edit repository image from the Repository Login Dialog
   * It presents an edit dialog where the user can edit information about the currently
   * selected repository
   */

  public void editRepository() {
    helper.editRepository();
  }
  
  /**
   * Executed when the user clicks the delete repository image from the Repository Login Dialog
   * It prompts the user with a warning about the action to be performed and upon the approval
   * of this action from the user, the selected repository is deleted
   */

  public void deleteRepository() {
    helper.deleteRepository();
  }
  /**
   * Executed when user clicks cancel button on the Repository Login Dialog
   */
  public void closeRepositoryLoginDialog() {
    loginDialog.hide();
    getCallback().onCancel();
  }
  /**
   * Executed when the user checks or uncheck the "show this dialog at startup checkbox"
   * It saves the current selection. 
   */
  public void updateShowDialogAtStartup() {
    helper.updateShowDialogOnStartup(showAtStartup.isChecked());
  }
  public XulMessageBox getMessageBox() {
    return messageBox;
  }

  public void setMessageBox(XulMessageBox messageBox) {
    this.messageBox = messageBox;
  }

  public void setMessages(ResourceBundle messages) {
    this.messages = messages;
  }

  public ResourceBundle getMessages() {
    return messages;
  }
  public String getPreferredRepositoryName() {
    return preferredRepositoryName;
  }

  public void setPreferredRepositoryName(String preferredRepositoryName) {
    this.preferredRepositoryName = preferredRepositoryName;
  }

  public void setCallback(ILoginCallback callback) {
    this.callback = callback;
  }

  public ILoginCallback getCallback() {
    return callback;
  }

  public void setShell(Shell shell) {
    this.shell = shell;
  }

  public Shell getShell() {
    return shell;
  }
}
