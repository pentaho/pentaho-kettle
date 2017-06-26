/*!
 * Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
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
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.KettleRepositoryLostException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositorySecurityManager;
import org.pentaho.di.repository.RepositorySecurityProvider;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.IAclObject;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.IUIEEUser;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.model.UIRepositoryObjectAcl;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.model.UIRepositoryObjectAclModel;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.model.UIRepositoryObjectAcls;
import org.pentaho.di.ui.repository.repositoryexplorer.ContextChangeVetoer.TYPE;
import org.pentaho.di.ui.repository.repositoryexplorer.ControllerInitializationException;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.MainController;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulCheckbox;
import org.pentaho.ui.xul.components.XulConfirmBox;
import org.pentaho.ui.xul.components.XulMessageBox;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulListbox;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.util.XulDialogCallback;

/**
 * This is shared code between the folder/file permissions controller and the database permissions controller.
 * 
 * @author Will Gorman (wgorman@pentaho.com)
 * 
 */
public abstract class AbstractPermissionsController extends AbstractXulEventHandler {

  private static final Class<?> PKG = IUIEEUser.class;

  protected BindingFactory bf;
  protected XulMessageBox messageBox;
  protected XulConfirmBox confirmBox;
  protected XulListbox userRoleList;
  protected XulListbox availableUserList;
  protected XulListbox availableRoleList;
  protected XulListbox selectedUserList;
  protected XulListbox selectedRoleList;
  protected XulCheckbox writeCheckbox;
  protected XulCheckbox readCheckbox;
  protected XulCheckbox manageAclCheckbox;
  protected XulCheckbox deleteCheckbox;
  protected XulButton addAclButton;
  protected XulButton removeAclButton;
  protected XulDialog manageAclsDialog;
  protected XulButton assignUserButton;
  protected XulButton unassignUserButton;
  protected XulButton assignRoleButton;
  protected XulButton unassignRoleButton;
  protected XulButton applyAclButton;
  protected Binding securityBinding;
  protected UIRepositoryObjectAcls viewAclsModel;
  protected UIRepositoryObjectAclModel manageAclsModel;
  protected RepositorySecurityProvider service;

  protected MainController mainController;

  protected abstract List<? extends Object> getSelectedObjects();

  protected PermissionsCheckboxHandler permissionsCheckboxHandler;

  protected void init( Repository rep ) throws Exception {
    if ( rep != null && rep.hasService( RepositorySecurityProvider.class ) ) {
      service = (RepositorySecurityProvider) rep.getService( RepositorySecurityProvider.class );
    } else {
      throw new ControllerInitializationException( BaseMessages.getString( PKG,
          "PermissionsController.ERROR_0001_UNABLE_TO_INITIAL_REPOSITORY_SERVICE", RepositorySecurityManager.class ) ); //$NON-NLS-1$
    }
    messageBox = (XulMessageBox) document.createElement( "messagebox" );//$NON-NLS-1$ 
    viewAclsModel = new UIRepositoryObjectAcls();
    manageAclsModel = new UIRepositoryObjectAclModel( viewAclsModel );
    bf = new DefaultBindingFactory();
    bf.setDocument( this.getXulDomContainer().getDocumentRoot() );

    mainController = (MainController) this.getXulDomContainer().getEventHandler( "mainController" );

    confirmBox = (XulConfirmBox) document.createElement( "confirmbox" );//$NON-NLS-1$
    confirmBox.setTitle( BaseMessages.getString( PKG, "PermissionsController.RemoveAclWarning" ) ); //$NON-NLS-1$
    confirmBox.setMessage( BaseMessages.getString( PKG, "PermissionsController.RemoveAclWarningText" ) ); //$NON-NLS-1$
    confirmBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Ok" ) ); //$NON-NLS-1$
    confirmBox.setCancelLabel( BaseMessages.getString( PKG, "Dialog.Cancel" ) ); //$NON-NLS-1$
    confirmBox.addDialogCallback( new XulDialogCallback<Object>() {
      public void onClose( XulComponent sender, Status returnCode, Object retVal ) {
        if ( returnCode == Status.ACCEPT ) {
          viewAclsModel.removeSelectedAcls();
        }
      }

      public void onError( XulComponent sender, Throwable t ) {
      }
    } );
  }

  protected String getXulPrefix() {
    return "";
  }

  protected void createBindings() {
    userRoleList = (XulListbox) document.getElementById( getXulPrefix() + "user-role-list" );//$NON-NLS-1$

    writeCheckbox = (XulCheckbox) document.getElementById( getXulPrefix() + "write-checkbox" );//$NON-NLS-1$ 
    readCheckbox = (XulCheckbox) document.getElementById( getXulPrefix() + "read-checkbox" );//$NON-NLS-1$ 
    manageAclCheckbox = (XulCheckbox) document.getElementById( getXulPrefix() + "manage-checkbox" );//$NON-NLS-1$ 
    deleteCheckbox = (XulCheckbox) document.getElementById( getXulPrefix() + "delete-checkbox" );//$NON-NLS-1$
    addAclButton = (XulButton) document.getElementById( getXulPrefix() + "add-acl-button" );//$NON-NLS-1$ 
    removeAclButton = (XulButton) document.getElementById( getXulPrefix() + "remove-acl-button" );//$NON-NLS-1$ 
    manageAclsDialog = (XulDialog) document.getElementById( getXulPrefix() + "manage-acls-dialog" );//$NON-NLS-1$ 
    permissionsCheckboxHandler =
        new PermissionsCheckboxHandler( readCheckbox, writeCheckbox, deleteCheckbox, manageAclCheckbox );

    // Add/Remove Acl Binding
    availableUserList = (XulListbox) document.getElementById( getXulPrefix() + "available-user-list" );//$NON-NLS-1$ 
    selectedUserList = (XulListbox) document.getElementById( getXulPrefix() + "selected-user-list" );//$NON-NLS-1$ 
    availableRoleList = (XulListbox) document.getElementById( getXulPrefix() + "available-role-list" );//$NON-NLS-1$ 
    selectedRoleList = (XulListbox) document.getElementById( getXulPrefix() + "selected-role-list" );//$NON-NLS-1$ 

    assignRoleButton = (XulButton) document.getElementById( getXulPrefix() + "assign-role" );//$NON-NLS-1$ 
    unassignRoleButton = (XulButton) document.getElementById( getXulPrefix() + "unassign-role" );//$NON-NLS-1$ 
    assignUserButton = (XulButton) document.getElementById( getXulPrefix() + "assign-user" );//$NON-NLS-1$ 
    unassignUserButton = (XulButton) document.getElementById( getXulPrefix() + "unassign-user" );//$NON-NLS-1$ 
    applyAclButton = (XulButton) document.getElementById( getXulPrefix() + "apply-acl" );//$NON-NLS-1$ 

    // Binding the model user or role list to the ui user or role list
    bf.setBindingType( Binding.Type.ONE_WAY );
    bf.createBinding( manageAclsModel, "availableUserList", availableUserList, "elements" );//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding( manageAclsModel, "selectedUserList", selectedUserList, "elements" );//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding( manageAclsModel, "availableRoleList", availableRoleList, "elements" ); //$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding( manageAclsModel, "selectedRoleList", selectedRoleList, "elements" ); //$NON-NLS-1$ //$NON-NLS-2$

    // indicesToObjectsConverter convert the selected indices to the list of objects and vice versa
    BindingConvertor<int[], List<UIRepositoryObjectAcl>> indicesToObjectsConverter =
        new BindingConvertor<int[], List<UIRepositoryObjectAcl>>() {
          @Override
          public int[] targetToSource( List<UIRepositoryObjectAcl> acls ) {
            if ( acls != null ) {
              int i = 0;
              int[] retVal = new int[acls.size()];
              for ( UIRepositoryObjectAcl acl : acls ) {
                retVal[i++] = viewAclsModel.getAceIndex( acl.getAce() );
              }
              return retVal;
            }
            return null;
          }

          @Override
          public List<UIRepositoryObjectAcl> sourceToTarget( int[] indices ) {
            if ( indices != null && indices.length > 0 ) {
              List<UIRepositoryObjectAcl> retVal = new ArrayList<UIRepositoryObjectAcl>();
              for ( int i = 0; i < indices.length; i++ ) {
                retVal.add( new UIRepositoryObjectAcl( viewAclsModel.getAceAtIndex( indices[i] ) ) );
              }
              return retVal;
            }
            return null;
          }
        };

    // indexToAvalableUserConverter convert the selected indices to the list of objects and vice versa
    BindingConvertor<int[], List<String>> indexToAvailableUserConverter = new BindingConvertor<int[], List<String>>() {

      @Override
      public List<String> sourceToTarget( int[] indices ) {
        List<String> userList = new ArrayList<String>();
        for ( int i = 0; i < indices.length; i++ ) {
          userList.add( manageAclsModel.getAvailableUser( indices[i] ) );
        }
        return userList;
      }

      @Override
      public int[] targetToSource( List<String> userList ) {
        int[] indices = new int[userList.size()];
        int i = 0;
        for ( String user : userList ) {
          indices[i++] = manageAclsModel.getAvailableUserIndex( user );
        }
        return indices;
      }

    };

    BindingConvertor<int[], List<String>> indexToAvailableRoleConverter = new BindingConvertor<int[], List<String>>() {

      @Override
      public List<String> sourceToTarget( int[] indices ) {
        List<String> roleList = new ArrayList<String>();
        for ( int i = 0; i < indices.length; i++ ) {
          roleList.add( manageAclsModel.getAvailableRole( indices[i] ) );
        }
        return roleList;
      }

      @Override
      public int[] targetToSource( List<String> roleList ) {
        int[] indices = new int[roleList.size()];
        int i = 0;
        for ( String role : roleList ) {
          indices[i++] = manageAclsModel.getAvailableRoleIndex( role );
        }
        return indices;
      }
    };

    BindingConvertor<int[], List<UIRepositoryObjectAcl>> indexToSelectedUserConverter =
        new BindingConvertor<int[], List<UIRepositoryObjectAcl>>() {

          @Override
          public List<UIRepositoryObjectAcl> sourceToTarget( int[] indices ) {
            List<UIRepositoryObjectAcl> userList = new ArrayList<UIRepositoryObjectAcl>();
            for ( int i = 0; i < indices.length; i++ ) {
              userList.add( manageAclsModel.getSelectedUser( indices[i] ) );
            }
            return userList;
          }

          @Override
          public int[] targetToSource( List<UIRepositoryObjectAcl> userList ) {
            int[] indices = new int[userList.size()];
            int i = 0;
            for ( UIRepositoryObjectAcl user : userList ) {
              indices[i++] = manageAclsModel.getSelectedUserIndex( user );
            }
            return indices;
          }

        };

    BindingConvertor<int[], List<UIRepositoryObjectAcl>> indexToSelectedRoleConverter =
        new BindingConvertor<int[], List<UIRepositoryObjectAcl>>() {

          @Override
          public List<UIRepositoryObjectAcl> sourceToTarget( int[] indices ) {
            List<UIRepositoryObjectAcl> roleList = new ArrayList<UIRepositoryObjectAcl>();
            for ( int i = 0; i < indices.length; i++ ) {
              roleList.add( manageAclsModel.getSelectedRole( indices[i] ) );
            }
            return roleList;
          }

          @Override
          public int[] targetToSource( List<UIRepositoryObjectAcl> roleList ) {
            int[] indices = new int[roleList.size()];
            int i = 0;
            for ( UIRepositoryObjectAcl role : roleList ) {
              indices[i++] = manageAclsModel.getSelectedRoleIndex( role );
            }
            return indices;
          }
        };

    // Binding between the selected incides of the lists to the mode list objects
    bf.setBindingType( Binding.Type.BI_DIRECTIONAL );

    bf.createBinding( availableUserList, "selectedIndices", manageAclsModel, "selectedAvailableUsers",//$NON-NLS-1$ //$NON-NLS-2$
        indexToAvailableUserConverter );
    bf.createBinding( selectedUserList, "selectedIndices", manageAclsModel, "selectedAssignedUsers",//$NON-NLS-1$ //$NON-NLS-2$
        indexToSelectedUserConverter );
    bf.createBinding( availableRoleList, "selectedIndices", manageAclsModel, "selectedAvailableRoles",//$NON-NLS-1$ //$NON-NLS-2$
        indexToAvailableRoleConverter );
    bf.createBinding( selectedRoleList, "selectedIndices", manageAclsModel, "selectedAssignedRoles",//$NON-NLS-1$ //$NON-NLS-2$
        indexToSelectedRoleConverter );

    // Binding the selected indices of acl list to the list of acl objects in the mode
    bf.createBinding( userRoleList, "selectedIndices", viewAclsModel, "selectedAclList", indicesToObjectsConverter ); //$NON-NLS-1$  //$NON-NLS-2$

    // accumulatorButtonConverter determine whether to enable of disable the accumulator buttons
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
        // One way binding, nothing to do here
        return null;
      }
    };
    bf.setBindingType( Binding.Type.ONE_WAY );
    bf.createBinding( selectedUserList, "selectedIndex", manageAclsModel, "userUnassignmentPossible",//$NON-NLS-1$ //$NON-NLS-2$
        accumulatorButtonConverter );
    bf.createBinding( availableUserList, "selectedIndex", manageAclsModel, "userAssignmentPossible",//$NON-NLS-1$ //$NON-NLS-2$
        accumulatorButtonConverter );
    bf.createBinding( manageAclsModel, "userUnassignmentPossible", unassignUserButton, "!disabled" );//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding( manageAclsModel, "userAssignmentPossible", assignUserButton, "!disabled" );//$NON-NLS-1$ //$NON-NLS-2$

    bf.createBinding( selectedRoleList, "selectedIndex", manageAclsModel, "roleUnassignmentPossible",//$NON-NLS-1$ //$NON-NLS-2$
        accumulatorButtonConverter );
    bf.createBinding( availableRoleList, "selectedIndex", manageAclsModel, "roleAssignmentPossible",//$NON-NLS-1$ //$NON-NLS-2$
        accumulatorButtonConverter );

    bf.createBinding( manageAclsModel, "roleUnassignmentPossible", unassignRoleButton, "!disabled" );//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding( manageAclsModel, "roleAssignmentPossible", assignRoleButton, "!disabled" );//$NON-NLS-1$ //$NON-NLS-2$

    // Only enable remove Acl button if the entries checkbox is unchecked and acl is selected from the list
    bf.createBinding( viewAclsModel, "removeEnabled", removeAclButton, "!disabled" ); //$NON-NLS-1$  //$NON-NLS-2$ 

    // Binding when the user select from the list
    bf.createBinding( viewAclsModel, "selectedAclList", this, "aclState", //$NON-NLS-1$  //$NON-NLS-2$
        new BindingConvertor<List<UIRepositoryObjectAcl>, UIRepositoryObjectAcl>() {

          @Override
          public UIRepositoryObjectAcl sourceToTarget( List<UIRepositoryObjectAcl> value ) {
            if ( value != null && value.size() > 0 ) {
              return value.get( 0 );
            }
            return null;
          }

          @Override
          public List<UIRepositoryObjectAcl> targetToSource( UIRepositoryObjectAcl value ) {
            return null;
          }
        } );

    bf.createBinding( userRoleList, "selectedItem", this, "recipientChanged" ); //$NON-NLS-1$  //$NON-NLS-2$

  }

  /**
   * 
   * assignUsers method is call to add selected user(s) to the assign users list
   */
  public void assignUsers() {
    manageAclsModel.assignUsers( Arrays.asList( availableUserList.getSelectedItems() ) );
  }

  /**
   * unassignUsers method is call to add unselected user(s) from the assign users list
   */
  public void unassignUsers() {
    manageAclsModel.unassign( Arrays.asList( selectedUserList.getSelectedItems() ) );
  }

  /**
   * assignRoles method is call to add selected role(s) to the assign roles list
   */
  public void assignRoles() {
    manageAclsModel.assignRoles( Arrays.asList( availableRoleList.getSelectedItems() ) );
  }

  /**
   * unassignRoles method is call to add unselected role(s) from the assign roles list
   */
  public void unassignRoles() {
    manageAclsModel.unassign( Arrays.asList( selectedRoleList.getSelectedItems() ) );
  }

  public void showManageAclsDialog() throws Exception {
    try {
      manageAclsModel.clear();
      manageAclsModel.setAclsList( service.getAllUsers(), service.getAllRoles() );
    } catch ( KettleException ke ) {
      messageBox.setTitle( BaseMessages.getString( PKG, "Dialog.Error" ) ); //$NON-NLS-1$
      messageBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Ok" ) ); //$NON-NLS-1$
      messageBox.setMessage( BaseMessages.getString( PKG,
          "PermissionsController.UnableToGetUserOrRole", ke.getLocalizedMessage() ) );//$NON-NLS-1$

      messageBox.open();
    }
    manageAclsDialog.show();
  }

  public void closeManageAclsDialog() {
    manageAclsDialog.hide();
  }

  /**
   * updateAcls method is called when the user click ok on the manage acl dialog. It updates the selection to the model
   * 
   * @throws Exception
   */
  public void updateAcls() {
    manageAclsModel.updateSelectedAcls();
    viewAclsModel.setSelectedAclList( null );
    setAclState( null );
    closeManageAclsDialog();
  }

  /**
   * removeAcl method is called when the user select a or a list of acls to remove from the list. It first display a
   * confirmation box to the user asking to confirm the removal. If the user selected ok, it deletes selected acls from
   * the list
   * 
   * @throws Exception
   */
  public void removeAcl() throws Exception {
    confirmBox.open();
  }

  /*
   * The method is called when a user select an acl from the acl list. This method reads the current selected acl and
   * populates the UI with the details
   */
  public void setRecipientChanged( UIRepositoryObjectAcl acl ) throws Exception {
    List<UIRepositoryObjectAcl> acls = new ArrayList<UIRepositoryObjectAcl>();
    // acl == null when user deselects recipient (CTRL-click)
    if ( acl != null ) {
      acls.add( acl );
    }
    viewAclsModel.setSelectedAclList( acls );
  }

  protected void updateCheckboxes( UIRepositoryObjectAcl acl ) {
    permissionsCheckboxHandler.updateCheckboxes( hasManageAclAccess(), acl.getPermissionSet() );
  }

  public void setAclState( UIRepositoryObjectAcl acl ) {
    if ( acl != null && acl.getPermissionSet() != null ) {
      updateCheckboxes( acl );
    } else {
      permissionsCheckboxHandler.setAllChecked( false );
      permissionsCheckboxHandler.setAllDisabled( true );
    }
  }

  protected boolean hasManageAclAccess() {
    try {
      Object ro = getSelectedObjects().get( 0 );
      if ( ro instanceof IAclObject ) {
        return ( (IAclObject) ro ).hasAccess( RepositoryFilePermission.ACL_MANAGEMENT );
      }
    } catch ( Exception e ) {
      if ( KettleRepositoryLostException.lookupStackStrace( e ) == null ) {
        throw new RuntimeException( e );
      }
    }
    return false;
  }

  private void clearSelectedObjAcl() {
    Object ro = getSelectedObjects().get( 0 );
    if ( ro instanceof IAclObject ) {
      ( (IAclObject) ro ).clearAcl();
    }
  }

  /*
   * updatePermission method is called when the user checks or uncheck any permission checkbox. This method updates the
   * current model with the update value from the UI
   */
  public void updatePermission() {
    UIRepositoryObjectAcl acl = (UIRepositoryObjectAcl) userRoleList.getSelectedItem();
    if ( acl == null ) {
      throw new IllegalStateException( BaseMessages.getString( PKG, "PermissionsController.NoSelectedRecipient" ) );
    }
    acl.setPermissionSet( permissionsCheckboxHandler.processCheckboxes() );
    clearSelectedObjAcl();
    viewAclsModel.updateAcl( acl );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.di.ui.repository.repositoryexplorer.ContextChangeListener#onContextChange() This method is called
   * whenever user change the folder or file selection
   */
  protected TYPE returnType;

  public TYPE onContextChange() {
    try {
      if ( viewAclsModel.isModelDirty() ) {

        if ( !hasManageAclAccess() ) {
          // if the user does not have permission to modify the acls,
          // ignore any changes, although this code shouldn't be executed
          // because all buttons should be disabled.

          viewAclsModel.clear();
          // Clear the ACL from the backing repo object
          clearSelectedObjAcl();
          viewAclsModel.setModelDirty( false );
          return TYPE.OK;
        }

        XulConfirmBox confirmBox = null;
        try {
          confirmBox = (XulConfirmBox) document.createElement( "confirmbox" );//$NON-NLS-1$
        } catch ( Exception e ) {
          // convert to runtime exception so it bubbles up through the UI
          throw new RuntimeException( e );
        }
        confirmBox.setTitle( BaseMessages.getString( PKG, "PermissionsController.ContextChangeWarning" ) ); //$NON-NLS-1$
        confirmBox.setMessage( BaseMessages.getString( PKG, "PermissionsController.ContextChangeWarningText" ) ); //$NON-NLS-1$
        confirmBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Yes" ) ); //$NON-NLS-1$
        confirmBox.setCancelLabel( BaseMessages.getString( PKG, "Dialog.No" ) ); //$NON-NLS-1$
        confirmBox.addDialogCallback( new XulDialogCallback<Object>() {
          public void onClose( XulComponent sender, Status returnCode, Object retVal ) {
            if ( returnCode == Status.ACCEPT ) {
              returnType = TYPE.OK;
              viewAclsModel.clear();
              // Clear the ACL from the backing repo object
              clearSelectedObjAcl();
              viewAclsModel.setModelDirty( false );
            } else {
              returnType = TYPE.CANCEL;
            }
          }

          public void onError( XulComponent sender, Throwable t ) {
            returnType = TYPE.NO_OP;
          }
        } );
        confirmBox.open();
      } else {
        returnType = TYPE.NO_OP;
      }
      return returnType;
    } catch ( Exception e ) {
      if ( KettleRepositoryLostException.lookupStackStrace( e ) != null ) {
        return TYPE.NO_OP;
      } else {
        throw new RuntimeException( e );
      }
    }
  }
}
