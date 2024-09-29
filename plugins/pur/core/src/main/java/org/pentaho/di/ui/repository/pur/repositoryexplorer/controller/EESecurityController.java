/*!
 * Copyright 2010 - 2019 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.di.ui.repository.pur.repositoryexplorer.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.ObjectRecipient;
import org.pentaho.di.repository.ObjectRecipient.Type;
import org.pentaho.di.repository.RepositorySecurityManager;
import org.pentaho.di.repository.pur.model.IRole;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.IUIEEUser;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.IUIRole;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.UIEEObjectRegistery;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.model.UIEERepositoryUser;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.model.UIEESecurity;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.model.UIEESecurityUser;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.model.UISecurityRole;
import org.pentaho.di.ui.repository.pur.services.IRoleSupportSecurityManager;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.SecurityController;
import org.pentaho.di.ui.repository.repositoryexplorer.model.IUIUser;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIObjectCreationException;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIObjectRegistry;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UISecurity.Mode;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulConfirmBox;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulRadio;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulHbox;
import org.pentaho.ui.xul.containers.XulListbox;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.util.XulDialogCallback;

/**
 * {@code XulEventHandler} for the Security panel of the repository explorer.
 * <p/>
 * <p>
 * This class handles only role-related functionality. {@link SecurityController} handles users.
 * </p>
 * 
 * @see org.pentaho.di.ui.repository.pur.repositoryexplorer.abs.controller.AbsController
 */
public class EESecurityController extends SecurityController implements java.io.Serializable {

  private static final long serialVersionUID = -4011949721807392436L; /* EESOURCE: UPDATE SERIALVERUID */

  private static final Class<?> PKG = IUIEEUser.class;

  public static final int ROLE_DECK = 1;

  protected ResourceBundle messages = new ResourceBundle() {

    @Override
    public Enumeration<String> getKeys() {
      return null;
    }

    @Override
    protected Object handleGetObject( String key ) {
      return BaseMessages.getString( PKG, key );
    }

  };
  private XulRadio systemRoleRadioButton;

  private XulRadio roleRadioButton;

  private XulRadio userRadioButton;

  private XulTree userDetailTable;

  private XulTree roleDetailTable;

  private XulDialog roleDialog;

  private XulDeck userRoleDeck;

  private XulListbox roleListBox;

  protected XulListbox userListBox;

  private XulListbox availableRoles;

  private XulListbox assignedRoles;

  private XulTextbox rolename;

  private XulTextbox roleDescription;

  private XulListbox availableUsers;

  private XulListbox assignedUsers;

  private XulButton roleEditButton;

  private XulButton roleAddButton;

  private XulButton roleRemoveButton;

  private XulButton addUserToRoleButton;

  private XulButton removeUserFromRoleButton;

  private XulButton addRoleToUserButton;

  private XulButton removeRoleFromUserButton;

  private XulButton assignRoleToUserButton;

  private XulButton unassignRoleFromUserButton;

  private XulButton assignUserToRoleButton;

  private XulButton unassignUserFromRoleButton;

  protected Binding roleDetailBinding;

  protected Binding userDetailBinding;

  protected UIEESecurityUser eeSecurityUser;

  protected UISecurityRole securityRole;

  protected UIEESecurity eeSecurity;

  private XulListbox systemRoleListBox;
  private XulVbox innerRoleVbox;

  private XulHbox roleHbox;

  private XulVbox roleVboxNonManaged;

  private XulLabel instructionalTextLabel;

  public EESecurityController() {
  }

  @Override
  protected boolean doLazyInit() {
    boolean superSucceeded = super.doLazyInit();
    if ( !superSucceeded ) {
      return false;
    }
    if ( !managed ) {
      userRadioButton.setVisible( false );
      roleHbox.removeChild( innerRoleVbox );
      roleHbox.addChild( roleVboxNonManaged );
    }
    return true;
  }

  @Override
  protected boolean initService() {
    try {
      if ( repository.hasService( IRoleSupportSecurityManager.class ) ) {
        service = (RepositorySecurityManager) repository.getService( IRoleSupportSecurityManager.class );
      } else {
        return false;
      }
    } catch ( KettleException e ) {
      throw new RuntimeException( e );
    }
    return true;
  }

  @Override
  protected void createModel() throws Exception {
    super.createModel();
    securityRole = new UISecurityRole();
  }

  protected void createSecurityUser() throws Exception {
    securityUser = eeSecurityUser = new UIEESecurityUser( service );
  }

  protected void createSecurity() throws Exception {
    security = eeSecurity = new UIEESecurity( service );
  }

  @Override
  protected void setInitialDeck() {
    if ( managed ) {
      super.setInitialDeck();
    } else {
      changeToRoleDeck();
    }
  }

  @Override
  protected void createBindings() {
    super.createBindings();
    // User Role Binding
    systemRoleRadioButton = (XulRadio) document.getElementById( "system-role-radio-button" );
    roleRadioButton = (XulRadio) document.getElementById( "role-radio-button" );
    userRadioButton = (XulRadio) document.getElementById( "user-radio-button" );

    roleAddButton = (XulButton) document.getElementById( "role-add" );
    roleEditButton = (XulButton) document.getElementById( "role-edit" );
    roleRemoveButton = (XulButton) document.getElementById( "role-remove" );

    addUserToRoleButton = (XulButton) document.getElementById( "add-user-to-role" );
    removeUserFromRoleButton = (XulButton) document.getElementById( "remove-user-from-role" );
    addRoleToUserButton = (XulButton) document.getElementById( "add-role-to-user" );
    removeRoleFromUserButton = (XulButton) document.getElementById( "remove-role-from-user" );

    roleDialog = (XulDialog) document.getElementById( "add-role-dialog" );
    userRoleDeck = (XulDeck) document.getElementById( "user-role-deck" );
    roleListBox = (XulListbox) document.getElementById( "roles-list" );
    roleDetailTable = (XulTree) document.getElementById( "role-detail-table" );

    // Add User Binding
    userListBox = (XulListbox) document.getElementById( "users-list" );
    userDetailTable = (XulTree) document.getElementById( "user-detail-table" );

    availableRoles = (XulListbox) document.getElementById( "available-roles-list" );
    assignedRoles = (XulListbox) document.getElementById( "selected-roles-list" );
    assignRoleToUserButton = (XulButton) document.getElementById( "assign-role-to-user" );
    unassignRoleFromUserButton = (XulButton) document.getElementById( "unassign-role-from-user" );

    systemRoleListBox = (XulListbox) document.getElementById( "system-roles-list" );

    innerRoleVbox = (XulVbox) document.getElementById( "inner-role-vbox" );
    roleVboxNonManaged = (XulVbox) document.getElementById( "role-vbox-nonmanaged" );
    roleHbox = (XulHbox) document.getElementById( "role-hbox" );
    instructionalTextLabel = (XulLabel) document.getElementById( "instructional-text-label" );

    bf.setBindingType( Binding.Type.BI_DIRECTIONAL );
    bf.createBinding( eeSecurityUser, "assignedRoles", assignedRoles, "elements" );
    bf.createBinding( eeSecurityUser, "availableRoles", availableRoles, "elements" );

    // Binding to convert role array to a role list object and vice versa
    BindingConvertor<List<IUIRole>, Object[]> arrayToListRoleConverter =
        new BindingConvertor<List<IUIRole>, Object[]>() {
        @Override
        public Object[] sourceToTarget( List<IUIRole> roles ) {
          if ( roles != null ) {
            Object[] retVal = new Object[roles.size()];
            int i = 0;
            for ( IUIRole role : roles ) {
              retVal[i++] = role;
            }
            return retVal;
          }
          return null;
        }

        @Override
        public List<IUIRole> targetToSource( Object[] roles ) {
          if ( roles != null ) {
            List<IUIRole> retVal = new ArrayList<IUIRole>();
            for ( int i = 0; i < roles.length; i++ ) {
              retVal.add( (IUIRole) roles[i] );
            }
            return retVal;
          }
          return null;
        }

      };

    // Binding to convert user array to a user list object and vice versa
    BindingConvertor<List<IUIUser>, Object[]> arrayToListUserConverter =
        new BindingConvertor<List<IUIUser>, Object[]>() {

        @Override
        public Object[] sourceToTarget( List<IUIUser> users ) {
          if ( users != null ) {
            Object[] retVal = new Object[users.size()];
            int i = 0;
            for ( IUIUser user : users ) {
              retVal[i++] = user;
            }
            return retVal;
          }
          return null;
        }

        @Override
        public List<IUIUser> targetToSource( Object[] users ) {
          if ( users != null ) {
            List<IUIUser> retVal = new ArrayList<IUIUser>();
            for ( int i = 0; i < users.length; i++ ) {
              retVal.add( (IUIUser) users[i] );
            }
            return retVal;
          }
          return null;
        }

      };

    bf.createBinding( eeSecurityUser, "availableSelectedRoles", availableRoles, "selectedItems",
        arrayToListRoleConverter );
    bf.createBinding( eeSecurityUser, "assignedSelectedRoles", assignedRoles, "selectedItems", arrayToListRoleConverter );

    BindingConvertor<Integer, Boolean> accumulatorButtonConverter = new BindingConvertor<Integer, Boolean>() {

      @Override
      public Boolean sourceToTarget( Integer value ) {
        if ( value != null && value >= 0 ) {
          return true;
        }
        return false;
      }

      @Override
      public Integer targetToSource( Boolean value ) {
        return null;
      }
    };

    bf.setBindingType( Binding.Type.ONE_WAY );
    bf.createBinding( assignedRoles, "selectedIndex", eeSecurityUser, "roleUnassignmentPossible",
        accumulatorButtonConverter );
    bf.createBinding( availableRoles, "selectedIndex", eeSecurityUser, "roleAssignmentPossible",
        accumulatorButtonConverter );
    bf.createBinding( eeSecurityUser, "roleUnassignmentPossible", unassignRoleFromUserButton, "!disabled" );
    bf.createBinding( eeSecurityUser, "roleAssignmentPossible", assignRoleToUserButton, "!disabled" );

    // Add Role Binding
    rolename = (XulTextbox) document.getElementById( "role-name" );
    roleDescription = (XulTextbox) document.getElementById( "role-description" );
    availableUsers = (XulListbox) document.getElementById( "available-users-list" );
    assignedUsers = (XulListbox) document.getElementById( "selected-users-list" );
    assignUserToRoleButton = (XulButton) document.getElementById( "assign-user-to-role" );
    unassignUserFromRoleButton = (XulButton) document.getElementById( "unassign-user-from-role" );

    bf.setBindingType( Binding.Type.BI_DIRECTIONAL );
    bf.createBinding( securityRole, "name", rolename, "value" );
    bf.createBinding( securityRole, "description", roleDescription, "value" );
    bf.createBinding( securityRole, "assignedUsers", assignedUsers, "elements" );
    bf.createBinding( securityRole, "availableUsers", availableUsers, "elements" );
    bf.createBinding( securityRole, "availableSelectedUsers", availableUsers, "selectedItems", arrayToListUserConverter );
    bf.createBinding( securityRole, "assignedSelectedUsers", assignedUsers, "selectedItems", arrayToListUserConverter );
    bf.createBinding( eeSecurity, "selectedRoleIndex", roleListBox, "selectedIndex" );
    bf.createBinding( eeSecurity, "selectedSystemRoleIndex", systemRoleListBox, "selectedIndex" );

    bf.setBindingType( Binding.Type.ONE_WAY );
    bf.createBinding( assignedUsers, "selectedIndex", securityRole, "userUnassignmentPossible",
        accumulatorButtonConverter );
    bf.createBinding( availableUsers, "selectedIndex", securityRole, "userAssignmentPossible",
        accumulatorButtonConverter );
    bf.createBinding( securityRole, "userUnassignmentPossible", unassignUserFromRoleButton, "!disabled" );
    bf.createBinding( securityRole, "userAssignmentPossible", assignUserToRoleButton, "!disabled" );

    try {
      bf.setBindingType( Binding.Type.ONE_WAY );
      bf.createBinding( roleListBox, "selectedIndex", this, "enableButtons" );
      bf.setBindingType( Binding.Type.ONE_WAY );
      // Action based security permissions
      bf.createBinding( roleListBox, "selectedItem", eeSecurity, "selectedRole" );
      bf.createBinding( eeSecurity, "roleList", roleListBox, "elements" ).fireSourceChanged();
      bf.createBinding( roleListBox, "selectedItem", eeSecurity, "selectedRole" );

      bf.createBinding( systemRoleListBox, "selectedItem", eeSecurity, "selectedSystemRole" );
      bf.createBinding( eeSecurity, "systemRoleList", systemRoleListBox, "elements" ).fireSourceChanged();
      bf.createBinding( systemRoleListBox, "selectedItem", eeSecurity, "selectedSystemRole" );

      if ( managed ) {
        userDetailBinding = bf.createBinding( eeSecurity, "selectedUser", userDetailTable, "elements",
            new BindingConvertor<IUIUser, List<IUIRole>>() {

              @Override
              public List<IUIRole> sourceToTarget( IUIUser ru ) {
                if ( ru instanceof IUIEEUser ) {
                  return new ArrayList<IUIRole>( ( (IUIEEUser) ru ).getRoles() );
                } else {
                  return null;
                }
              }

              @Override
              public IUIUser targetToSource( List<IUIRole> arg0 ) {
                // TODO Auto-generated method stub
                return null;
              }

            } );
        roleDetailBinding = bf.createBinding( eeSecurity, "selectedRole", roleDetailTable, "elements",
            new BindingConvertor<IUIRole, List<IUIUser>>() {

              @Override
              public List<IUIUser> sourceToTarget( IUIRole rr ) {
                return new ArrayList<IUIUser>( rr.getUsers() );
              }

              @Override
              public IUIRole targetToSource( List<IUIUser> arg0 ) {
                // TODO Auto-generated method stub
                return null;
              }

            } );
      }
      bf.createBinding( eeSecurity, "selectedDeck", userRoleDeck, "selectedIndex",
          new BindingConvertor<ObjectRecipient.Type, Integer>() {

            @Override
            public Integer sourceToTarget( Type arg0 ) {
              if ( arg0 == Type.ROLE ) {
                userRadioButton.setSelected( false );
                roleRadioButton.setSelected( true );
                systemRoleRadioButton.setSelected( false );
                return 1;
              } else if ( arg0 == Type.USER ) {
                userRadioButton.setSelected( true );
                roleRadioButton.setSelected( false );
                systemRoleRadioButton.setSelected( false );
                return 0;
              } else if ( arg0 == Type.SYSTEM_ROLE ) {
                userRadioButton.setSelected( false );
                roleRadioButton.setSelected( false );
                systemRoleRadioButton.setSelected( true );
                return 2;
              } else {
                return -1;
              }
            }

            @Override
            public Type targetToSource( Integer arg0 ) {
              return null;
            }

          } );
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
      BindingConvertor<Mode, Boolean> anotherModeBindingConverter = new BindingConvertor<Mode, Boolean>() {

        @Override
        public Boolean sourceToTarget( Mode arg0 ) {
          if ( arg0.equals( Mode.EDIT_MEMBER ) ) {
            return true;
          } else {
            return false;
          }
        }

        @Override
        public Mode targetToSource( Boolean arg0 ) {
          // TODO Auto-generated method stub
          return null;
        }

      };
      bf.createBinding( securityRole, "mode", rolename, "disabled", modeBindingConverter );
      bf.createBinding( securityRole, "mode", roleDescription, "disabled", anotherModeBindingConverter );

      bf.createBinding( securityUser, "mode", userPassword, "disabled", anotherModeBindingConverter );
      bf.createBinding( securityUser, "mode", userDescription, "disabled", anotherModeBindingConverter );

    } catch ( Exception e ) {
      // convert to runtime exception so it bubbles up through the UI
      throw new RuntimeException( e );
    }
  }

  public void assignUsersToRole() {
    securityRole.assignUsers( Arrays.asList( availableUsers.getSelectedItems() ) );
  }

  public void unassignUsersFromRole() {
    securityRole.unassignUsers( Arrays.asList( assignedUsers.getSelectedItems() ) );
  }

  public void assignRolesToUser() {

    eeSecurityUser.assignRoles( Arrays.asList( availableRoles.getSelectedItems() ) );
  }

  public void unassignRolesFromUser() {
    eeSecurityUser.unassignRoles( Arrays.asList( assignedRoles.getSelectedItems() ) );
  }

  @Override
  public void showAddUserDialog() throws Exception {
    try {
      if ( service != null && ( (IRoleSupportSecurityManager) service ).getRoles() != null ) {
        eeSecurityUser.clear();
        eeSecurityUser.setAvailableRoles( convertToUIRoleModel( ( (IRoleSupportSecurityManager) service ).getRoles() ) );
        eeSecurityUser.updateAssignedRoles( convertToUIRoleModel( ( (IRoleSupportSecurityManager) service )
            .getDefaultRoles() ) );
      }
      eeSecurityUser.setMode( Mode.ADD );
      userDialog.setTitle( BaseMessages.getString( PKG, "AddUserDialog.Title" ) );
      userDialog.show();
    } catch ( KettleException e ) {
      if ( mainController == null || !mainController.handleLostRepository( e ) ) {
        messageBox.setTitle( BaseMessages.getString( PKG, "Dialog.Error" ) );
        messageBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Ok" ) );
        messageBox.setMessage( BaseMessages.getString( PKG,
            "SecurityController.AddUser.UnableToShowAddUser", e.getLocalizedMessage() ) );
        messageBox.open();
      }
    }
  }

  public void cancelAddRoleDialog() throws Exception {
    roleDialog.hide();
  }

  /**
   * addRole method is called when user has click ok on a add role dialog. The method add the role
   * 
   * @throws Exception
   */
  @Override
  protected void addUser() {
    if ( service != null ) {
      try {
        if ( eeSecurityUser.getUserInfo().getUsername().isEmpty() ) {
          throw new Exception( BaseMessages.getString( PKG, "CantCreateUserDialog.UsernameIsMandatory" ) );
        }
        if ( eeSecurityUser.getUserInfo().getPassword().isEmpty() ) {
          throw new Exception( BaseMessages.getString( PKG, "CantCreateUserDialog.PasswordIsMandatory" ) );
        }
        service.saveUserInfo( eeSecurityUser.getUserInfo() );
        eeSecurity.addUser( UIObjectRegistry.getInstance().constructUIRepositoryUser( eeSecurityUser.getUserInfo() ) );
        userDialog.hide();
      } catch ( Throwable th ) {
        if ( mainController == null || !mainController.handleLostRepository( th ) ) {
          messageBox.setTitle( BaseMessages.getString( PKG, "CantCreateUserDialog.Title" ) );
          messageBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Close" ) );
          messageBox
              .setMessage( BaseMessages.getString( PKG, "CantCreateUserDialog.Message", th.getLocalizedMessage() ) );
          messageBox.open();
        }
      }
    }
  }

  @Override
  public void showEditUserDialog() throws Exception {
    if ( service != null && ( (IRoleSupportSecurityManager) service ).getRoles() != null ) {
      eeSecurityUser.clear();
      eeSecurityUser.setUser( security.getSelectedUser(),
          convertToUIRoleModel( ( (IRoleSupportSecurityManager) service ).getRoles() ) );
      eeSecurityUser.setMode( Mode.EDIT );
      userDialog.setTitle( BaseMessages.getString( PKG, "EditUserDialog.Title" ) );
      userDialog.show();
    }
  }

  /**
   * updateUser method is called when user has click ok on a edit user dialog. The method updates the user
   * 
   * @throws Exception
   */
  @Override
  protected void updateUser() {
    if ( service != null ) {
      try {
        UIEERepositoryUser uiUser = (UIEERepositoryUser) eeSecurity.getSelectedUser();
        Set<IUIRole> previousRoleList = new HashSet<IUIRole>();
        previousRoleList.addAll( uiUser.getRoles() );
        uiUser.setDescription( eeSecurityUser.getDescription() );
        uiUser.setPassword( eeSecurityUser.getPassword() );
        uiUser.setRoles( new HashSet<IUIRole>( eeSecurityUser.getAssignedRoles() ) );
        service.updateUser( uiUser.getUserInfo() );
        eeSecurity.updateUser( uiUser, previousRoleList );
        userDialog.hide();
      } catch ( Throwable th ) {
        if ( mainController == null || !mainController.handleLostRepository( th ) ) {
          messageBox.setTitle( BaseMessages.getString( PKG, "Dialog.Error" ) );
          messageBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Ok" ) );
          messageBox.setMessage( BaseMessages
              .getString( PKG, "UpdateUser.UnableToUpdateUser", th.getLocalizedMessage() ) );
          messageBox.open();
        }
      }
    }
  }

  public void showAddRoleDialog() throws Exception {
    try {
      if ( service != null && service.getUsers() != null ) {
        securityRole.clear();
        securityRole.setAvailableUsers( convertToUIUserModel( service.getUsers() ) );
      }
      roleDialog.setTitle( BaseMessages.getString( PKG, "AddRoleDialog.Title" ) );
      roleDialog.show();
    } catch ( KettleException e ) {
      if ( mainController == null || !mainController.handleLostRepository( e ) ) {
        messageBox.setTitle( BaseMessages.getString( PKG, "Dialog.Error" ) );
        messageBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Ok" ) );
        messageBox.setMessage( BaseMessages.getString( PKG,
            "SecurityController.AddRole.UnableToShowAddRole", e.getLocalizedMessage() ) );
        messageBox.open();
      }

    }
  }

  public void showAddUserToRoleDialog() throws Exception {
    if ( service != null && service.getUsers() != null ) {
      securityRole.clear();
      securityRole.setRole( ( (UIEESecurity) security ).getSelectedRole(), convertToUIUserModel( service.getUsers() ) );
      securityRole.setMode( Mode.EDIT_MEMBER );
    }
    roleDialog.setTitle( BaseMessages.getString( PKG, "AddUserToRoleDialog.Title" ) );
    roleDialog.show();
  }

  public void showAddRoleToUserDialog() throws Exception {
    if ( service != null && ( (IRoleSupportSecurityManager) service ).getRoles() != null ) {
      eeSecurityUser.clear();
      eeSecurityUser.setUser( security.getSelectedUser(),
          convertToUIRoleModel( ( (IRoleSupportSecurityManager) service ).getRoles() ) );
      eeSecurityUser.setMode( Mode.EDIT_MEMBER );
      userDialog.setTitle( BaseMessages.getString( PKG, "AddRoleToUserDialog.Title" ) );
      userDialog.show();
    }
  }

  public void removeRolesFromUser() throws Exception {
    ( (UIEESecurity) security ).removeRolesFromSelectedUser( userDetailTable.getSelectedItems() );
    service.updateUser( security.getSelectedUser().getUserInfo() );
  }

  public void removeUsersFromRole() throws Exception {
    ( (UIEESecurity) security ).removeUsersFromSelectedRole( roleDetailTable.getSelectedItems() );
    ( (IRoleSupportSecurityManager) service ).updateRole( ( (UIEESecurity) security ).getSelectedRole().getRole() );
  }

  /**
   * addRole method is called when user has click ok on a add role dialog. The method add the role
   * 
   * @throws Exception
   */

  private void addRole() {
    if ( service != null ) {
      try {
        if ( securityRole.getName().isEmpty() ) {
          throw new Exception( BaseMessages.getString( PKG, "CantCreateRoleDialog.RoleNameIsMandatory" ) );
        }
        IRole role = securityRole.getRole( (IRoleSupportSecurityManager) service );
        ( (IRoleSupportSecurityManager) service ).createRole( role );
        eeSecurity.addRole( UIEEObjectRegistery.getInstance().constructUIRepositoryRole( role ) );
        roleDialog.hide();
      } catch ( Throwable th ) {
        if ( mainController == null || !mainController.handleLostRepository( th ) ) {
          messageBox.setTitle( BaseMessages.getString( PKG, "CantCreateRoleDialog.Title" ) );
          messageBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Close" ) );
          messageBox
              .setMessage( BaseMessages.getString( PKG, "CantCreateRoleDialog.Message", th.getLocalizedMessage() ) );
          messageBox.open();
        }
      }
    }
  }

  /**
   * updateRole method is called when user has click ok on a edit role dialog. The method updates the role
   * 
   * @throws Exception
   */


  private void updateRole() {
    if ( service != null ) {
      try {
        IUIRole uiRole = eeSecurity.getSelectedRole();
        Set<IUIUser> previousUserList = new HashSet<IUIUser>();
        previousUserList.addAll( uiRole.getUsers() );
        uiRole.setDescription( securityRole.getDescription() );
        uiRole.setUsers( new HashSet<IUIUser>( securityRole.getAssignedUsers() ) );
        ( (IRoleSupportSecurityManager) service ).updateRole( uiRole.getRole() );
        eeSecurity.updateRole( uiRole, previousUserList );
        roleDialog.hide();
      } catch ( Throwable th ) {
        messageBox.setTitle( BaseMessages.getString( PKG, "Dialog.Error" ) );
        messageBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Ok" ) );
        messageBox
            .setMessage( BaseMessages.getString( PKG, "UpdateRole.UnableToUpdateRole", th.getLocalizedMessage() ) );
        messageBox.open();
      }
    }
  }

  /**
   * removeRole method is called when user has click on a remove button in a role deck. It first displays a confirmation
   * message to the user and once the user selects ok, it remove the role
   * 
   * @throws Exception
   */

  public void removeRole() throws Exception {
    XulConfirmBox confirmBox = (XulConfirmBox) document.createElement( "confirmbox" );
    confirmBox.setTitle( BaseMessages.getString( PKG, "ConfirmDialog.Title" ) );
    confirmBox.setMessage( BaseMessages.getString( PKG, "RemoveRoleConfirmDialog.Message" ) );
    confirmBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Ok" ) );
    confirmBox.setCancelLabel( BaseMessages.getString( PKG, "Dialog.Cancel" ) );
    confirmBox.addDialogCallback( new XulDialogCallback<Object>() {

      public void onClose( XulComponent sender, Status returnCode, Object retVal ) {
        if ( returnCode == Status.ACCEPT ) {
          if ( service != null ) {
            if ( eeSecurity != null && eeSecurity.getSelectedRole() != null ) {
              try {
                ( (IRoleSupportSecurityManager) service ).deleteRole( eeSecurity.getSelectedRole().getName() );
                eeSecurity.removeRole( eeSecurity.getSelectedRole().getName() );
              } catch ( Throwable th ) {
                messageBox.setTitle( BaseMessages.getString( PKG, "Dialog.Error" ) );
                messageBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Ok" ) );
                messageBox.setMessage( BaseMessages.getString( PKG,
                    "RemoveRole.UnableToRemoveRole", th.getLocalizedMessage() ) );
                messageBox.open();
              }
            } else {
              messageBox.setTitle( BaseMessages.getString( PKG, "Dialog.Error" ) );
              messageBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Ok" ) );
              messageBox.setMessage( BaseMessages.getString( PKG, "RemoveRole.NoRoleSelected" ) );
              messageBox.open();
            }
          }
        }
      }

      public void onError( XulComponent sender, Throwable t ) {
        if ( mainController == null || !mainController.handleLostRepository( t ) ) {
          messageBox.setTitle( BaseMessages.getString( PKG, "Dialog.Error" ) );
          messageBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Ok" ) );
          messageBox
              .setMessage( BaseMessages.getString( PKG, "RemoveRole.UnableToRemoveRole", t.getLocalizedMessage() ) );
          messageBox.open();
        }
      }
    } );
    confirmBox.open();
  }

  public void showEditRoleDialog() throws Exception {
    if ( service != null && service.getUsers() != null ) {
      securityRole.clear();
      securityRole.setRole( eeSecurity.getSelectedRole(), convertToUIUserModel( service.getUsers() ) );
      securityRole.setMode( Mode.EDIT );
      roleDialog.setTitle( BaseMessages.getString( PKG, "EditRoleDialog.Title" ) );
      roleDialog.show();
    }
  }

  public void changeToRoleDeck() {
    security.setSelectedDeck( ObjectRecipient.Type.ROLE );
    instructionalTextLabel.setValue( BaseMessages.getString( PKG, "SecurityTab.RoleInstructionalText" ) );
  }

  public void changeToSystemRoleDeck() {
    security.setSelectedDeck( ObjectRecipient.Type.SYSTEM_ROLE );
    instructionalTextLabel.setValue( BaseMessages.getString( PKG, "SecurityTab.SystemRoleInstructionalText" ) );
  }

  /**
   * saveRole method is called when the user click on the ok button of a Add or Edit Role dialog Depending on the mode
   * it calls add of update role method
   * 
   * @throws Exception
   */
  public void saveRole() throws Exception {
    if ( securityRole.getMode().equals( Mode.ADD ) ) {
      addRole();
    } else {
      updateRole();
    }
  }

  private List<IUIRole> convertToUIRoleModel( List<IRole> roles ) throws UIObjectCreationException {
    List<IUIRole> rroles = new ArrayList<IUIRole>();
    for ( IRole role : roles ) {
      rroles.add( UIEEObjectRegistery.getInstance().constructUIRepositoryRole( role ) );
    }
    return rroles;
  }

  private List<IUIUser> convertToUIUserModel( List<IUser> users ) throws UIObjectCreationException {
    List<IUIUser> rusers = new ArrayList<IUIUser>();
    for ( IUser user : users ) {
      rusers.add( UIObjectRegistry.getInstance().constructUIRepositoryUser( user ) );
    }
    return rusers;
  }

  @Override
  protected void enableButtons( boolean enableNew, boolean enableEdit, boolean enableRemove ) {
    super.enableButtons( enableNew, enableEdit, enableRemove );
    roleAddButton.setDisabled( !enableNew );
    roleEditButton.setDisabled( !enableEdit );
    roleRemoveButton.setDisabled( !enableRemove );
    addUserToRoleButton.setDisabled( !enableNew );
    removeUserFromRoleButton.setDisabled( !enableNew );
    addRoleToUserButton.setDisabled( !enableNew );
    removeRoleFromUserButton.setDisabled( !enableNew );
  }

  @Override
  protected void showButtons( boolean showNew, boolean showEdit, boolean showRemove ) {
    super.showButtons( showNew, showEdit, showRemove );
    roleAddButton.setVisible( showNew );
    roleEditButton.setVisible( showEdit );
    roleRemoveButton.setVisible( showRemove );
    addUserToRoleButton.setVisible( showNew );
    removeUserFromRoleButton.setVisible( showNew );
    addRoleToUserButton.setVisible( showNew );
    removeRoleFromUserButton.setVisible( showNew );
  }
}
