/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectRecipient;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositorySecurityManager;
import org.pentaho.di.ui.repository.repositoryexplorer.ControllerInitializationException;
import org.pentaho.di.ui.repository.repositoryexplorer.IUISupportController;
import org.pentaho.di.ui.repository.repositoryexplorer.RepositoryExplorer;
import org.pentaho.di.ui.repository.repositoryexplorer.model.IUIUser;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIObjectRegistry;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UISecurity;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UISecurity.Mode;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UISecurityUser;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulConfirmBox;
import org.pentaho.ui.xul.components.XulMessageBox;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulListbox;
import org.pentaho.ui.xul.swt.SwtBindingFactory;
import org.pentaho.ui.xul.util.XulDialogCallback;

/**
 * {@code XulEventHandler} for the Security panel of the repository explorer.
 *
 * <p>
 * This class handles only user-related functionality. If supported, other controllers handle roles, etc.
 * </p>
 */
public class SecurityController extends LazilyInitializedController implements IUISupportController {

  private static Class<?> PKG = RepositoryExplorer.class; // for i18n purposes, needed by Translator2!!

  public static final int USER_DECK = 0;

  protected XulDialog userDialog;

  private XulListbox userListBox;

  private XulTextbox username;

  protected XulTextbox userPassword;

  protected XulTextbox userDescription;

  private XulButton userAddButton;

  private XulButton userEditButton;

  private XulButton userRemoveButton;

  protected RepositorySecurityManager service;

  protected MainController mainController;

  protected UISecurity security;

  protected BindingFactory bf;

  protected UISecurityUser securityUser;

  protected XulMessageBox messageBox = null;

  protected boolean managed = false;

  public SecurityController() {
  }

  public void init( Repository rep ) throws ControllerInitializationException {
    this.repository = rep;
  }

  @Override
  protected boolean doLazyInit() {
    try {
      mainController = (MainController) this.getXulDomContainer().getEventHandler( "mainController" );
    } catch ( XulException e ) {
      return false;
    }

    try {
      boolean serviceInitialized = initService();
      if ( !serviceInitialized ) {
        return false;
      }
    } catch ( Exception e ) {
      if ( mainController == null || !mainController.handleLostRepository( e ) ) {
        throw new RuntimeException( e );
      }

      return false;
    }

    try {
      managed = service.isManaged();
      createModel();
      messageBox = (XulMessageBox) document.createElement( "messagebox" );
      bf = new SwtBindingFactory();
      bf.setDocument( this.getXulDomContainer().getDocumentRoot() );

    } catch ( Exception e ) {
      if ( mainController == null || !mainController.handleLostRepository( e ) ) {
        throw new RuntimeException( e );
      }
    }
    if ( bf != null ) {
      createBindings();
    }
    if ( !managed ) {
      showButtons( false, false, false );
    }
    setInitialDeck();
    return true;
  }

  protected boolean initService() {
    try {
      // Get the service from the repository
      if ( repository != null && repository.hasService( RepositorySecurityManager.class ) ) {
        service = (RepositorySecurityManager) repository.getService( RepositorySecurityManager.class );
        return true;
      } else {
        return false;
      }
    } catch ( Exception e ) {
      throw new RuntimeException( e );
    }
  }

  protected void setInitialDeck() {
    changeToUserDeck();
  }

  public XulMessageBox getMessageBox() {
    return messageBox;
  }

  public void setMessageBox( XulMessageBox messageBox ) {
    this.messageBox = messageBox;
  }

  protected void createModel() throws Exception {
    createSecurityUser();
    createSecurity();
  }

  protected void createSecurityUser() throws Exception {
    securityUser = new UISecurityUser( service );
  }

  protected void createSecurity() throws Exception {
    security = new UISecurity( service );
  }

  protected void createBindings() {
    // User Details Binding
    userAddButton = (XulButton) document.getElementById( "user-add" );
    userEditButton = (XulButton) document.getElementById( "user-edit" );
    userRemoveButton = (XulButton) document.getElementById( "user-remove" );
    userDialog = (XulDialog) document.getElementById( "add-user-dialog" );
    userListBox = (XulListbox) document.getElementById( "users-list" );

    // Add User Binding

    username = (XulTextbox) document.getElementById( "user-name" );
    userPassword = (XulTextbox) document.getElementById( "user-password" );
    userDescription = (XulTextbox) document.getElementById( "user-description" );

    bf.setBindingType( Binding.Type.BI_DIRECTIONAL );
    bf.createBinding( securityUser, "name", username, "value" );
    bf.createBinding( securityUser, "password", userPassword, "value" );
    bf.createBinding( securityUser, "description", userDescription, "value" );
    bf.createBinding( security, "selectedUserIndex", userListBox, "selectedIndex" );
    bf.setBindingType( Binding.Type.ONE_WAY );
    try {
      bf.createBinding( userListBox, "selectedIndex", this, "enableButtons" );
      bf.createBinding( userListBox, "selectedItem", security, "selectedUser" );
      bf.createBinding( security, "userList", userListBox, "elements" ).fireSourceChanged();

      BindingConvertor<Mode, Boolean> modeBindingConverter = new BindingConvertor<Mode, Boolean>() {

        @Override
        public Boolean sourceToTarget( Mode arg0 ) {
          if ( arg0.equals( Mode.ADD ) ) {
            return false;
          }
          return true;
        }

        @Override
        public Mode targetToSource( Boolean arg0 ) {
          // TODO Auto-generated method stub
          return null;
        }

      };
      bf.createBinding( securityUser, "mode", username, "disabled", modeBindingConverter );

    } catch ( Exception e ) {
      if ( mainController == null || !mainController.handleLostRepository( e ) ) {
        // convert to runtime exception so it bubbles up through the UI
        throw new RuntimeException( e );
      }
    }
  }

  public String getName() {
    return "iSecurityController";
  }

  public void showAddUserDialog() throws Exception {
    securityUser.clear();
    securityUser.setMode( Mode.ADD );
    userDialog.setTitle( BaseMessages.getString( PKG, "AddUserDialog.Title" ) );
    userDialog.show();
  }

  public void cancelAddUserDialog() throws Exception {
    userDialog.hide();
  }

  /**
   * addRole method is called when user has click ok on a add role dialog. The method add the role
   *
   * @throws Exception
   */
  protected void addUser() {
    if ( service != null ) {
      try {
        service.saveUserInfo( securityUser.getUserInfo() );
        security.addUser( UIObjectRegistry.getInstance().constructUIRepositoryUser( securityUser.getUserInfo() ) );
        userDialog.hide();
      } catch ( Throwable th ) {
        if ( mainController == null || !mainController.handleLostRepository( th ) ) {
          messageBox.setTitle( BaseMessages.getString( PKG, "CantCreateUserDialog.Title" ) );
          messageBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Close" ) );
          messageBox.setMessage( BaseMessages.getString( PKG, "CantCreateUserDialog.Message", th.getLocalizedMessage() ) );
          messageBox.open();
        }
      }
    }
  }

  public void showEditUserDialog() throws Exception {
    if ( service != null ) {
      securityUser.clear();
      securityUser.setUser( security.getSelectedUser() );
      securityUser.setMode( Mode.EDIT );
      userDialog.setTitle( BaseMessages.getString( PKG, "EditUserDialog.Title" ) );
      userDialog.show();
    }
  }

  /**
   * updateUser method is called when user has click ok on a edit user dialog. The method updates the user
   *
   * @throws Exception
   */

  protected void updateUser() {
    if ( service != null ) {
      try {
        IUIUser uiUser = security.getSelectedUser();
        uiUser.setDescription( securityUser.getDescription() );
        uiUser.setPassword( securityUser.getPassword() );
        service.updateUser( uiUser.getUserInfo() );
        security.updateUser( uiUser );
        userDialog.hide();
      } catch ( Throwable th ) {
        if ( mainController == null || !mainController.handleLostRepository( th ) ) {
          messageBox.setTitle( BaseMessages.getString( PKG, "Dialog.Error" ) );
          messageBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Ok" ) );
          messageBox.setMessage( BaseMessages.getString( PKG, "UpdateUser.UnableToUpdateUser", th
            .getLocalizedMessage() ) );
          messageBox.open();
        }
      }
    }
  }

  /**
   * removeUser method is called when user has click on a remove button in a user deck. It first displays a confirmation
   * message to the user and once the user selects ok, it remove the user
   *
   * @throws Exception
   */
  public void removeUser() throws Exception {
    XulConfirmBox confirmBox = (XulConfirmBox) document.createElement( "confirmbox" );
    confirmBox.setTitle( BaseMessages.getString( PKG, "ConfirmDialog.Title" ) );
    confirmBox.setMessage( BaseMessages.getString( PKG, "RemoveUserConfirmDialog.Message" ) );
    confirmBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Ok" ) );
    confirmBox.setCancelLabel( BaseMessages.getString( PKG, "Dialog.Cancel" ) );
    confirmBox.addDialogCallback( new XulDialogCallback<Object>() {

      public void onClose( XulComponent sender, Status returnCode, Object retVal ) {
        if ( returnCode == Status.ACCEPT ) {
          if ( service != null ) {
            if ( security != null && security.getSelectedUser() != null ) {
              try {
                service.delUser( security.getSelectedUser().getName() );
                security.removeUser( security.getSelectedUser().getName() );
              } catch ( Throwable th ) {
                if ( mainController == null || !mainController.handleLostRepository( th ) ) {
                  messageBox.setTitle( BaseMessages.getString( PKG, "Dialog.Error" ) );
                  messageBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Ok" ) );
                  messageBox.setMessage( BaseMessages.getString( PKG, "RemoveUser.UnableToRemoveUser", th
                    .getLocalizedMessage() ) );
                  messageBox.open();
                }
              }
            } else {
              messageBox.setTitle( BaseMessages.getString( PKG, "Dialog.Error" ) );
              messageBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Ok" ) );
              messageBox.setMessage( BaseMessages.getString( PKG, "RemoveUser.NoUserSelected" ) );
              messageBox.open();
            }
          }
        }
      }

      public void onError( XulComponent sender, Throwable t ) {
        if ( mainController == null || !mainController.handleLostRepository( t ) ) {
          messageBox.setTitle( BaseMessages.getString( PKG, "Dialog.Error" ) );
          messageBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Ok" ) );
          messageBox.setMessage( BaseMessages.getString( PKG, "RemoveUser.UnableToRemoveUser", t
            .getLocalizedMessage() ) );
          messageBox.open();
        }
      }
    } );
    confirmBox.open();
  }

  public void changeToUserDeck() {
    security.setSelectedDeck( ObjectRecipient.Type.USER );
  }

  /**
   * saveUser method is called when the user click on the ok button of a Add or Edit User dialog Depending on the mode
   * it calls add of update user method
   *
   * @throws Exception
   */

  public void saveUser() throws Exception {
    if ( securityUser.getMode().equals( Mode.ADD ) ) {
      addUser();
    } else {
      updateUser();
    }
  }

  public void setEnableButtons( int selectedIndex ) {
    boolean enableAdd = true;
    boolean enableEdit = false;
    boolean enableRemove = false;
    if ( managed ) {
      if ( selectedIndex >= 0 ) {
        enableRemove = true;
        enableEdit = true;
      } else {
        enableRemove = false;
        enableEdit = false;
      }
    } else {
      enableAdd = false;
    }
    enableButtons( enableAdd, enableEdit, enableRemove );
  }

  protected void enableButtons( boolean enableNew, boolean enableEdit, boolean enableRemove ) {
    userAddButton.setDisabled( !enableNew );
    userEditButton.setDisabled( !enableEdit );
    userRemoveButton.setDisabled( !enableRemove );
  }

  protected void showButtons( boolean showNew, boolean showEdit, boolean showRemove ) {
    userAddButton.setVisible( showNew );
    userEditButton.setVisible( showEdit );
    userRemoveButton.setVisible( showRemove );
  }

  public void tabClicked() {
    lazyInit();
  }

}
