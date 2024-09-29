/*!
 * Copyright 2010 - 2017 Hitachi Vantara.  All rights reserved.
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
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.IAclObject;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.ILockObject;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.IUIEEUser;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.model.UIRepositoryObjectAcl;
import org.pentaho.di.ui.repository.repositoryexplorer.AccessDeniedException;
import org.pentaho.di.ui.repository.repositoryexplorer.ContextChangeVetoer;
import org.pentaho.di.ui.repository.repositoryexplorer.ControllerInitializationException;
import org.pentaho.di.ui.repository.repositoryexplorer.IUISupportController;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.IBrowseController;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryContent;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryDirectory;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryObject;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.components.XulCheckbox;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.containers.XulDeck;

/**
 * 
 * This is the XulEventHandler for the browse panel of the repository explorer. It sets up the bindings for browse
 * functionality.
 * 
 */
public class PermissionsController extends AbstractPermissionsController implements ContextChangeVetoer,
    IUISupportController, java.io.Serializable {

  private static final long serialVersionUID = -6151060931568671109L; /* EESOURCE: UPDATE SERIALVERUID */

  private static final Class<?> PKG = IUIEEUser.class;

  private static final int NO_ACL = 0;

  private static final int ACL = 1;

  private XulDeck aclDeck;

  private XulCheckbox inheritParentPermissionCheckbox;

  private XulLabel fileFolderLabel;

  List<UIRepositoryObject> repoObject = new ArrayList<UIRepositoryObject>();

  private IBrowseController browseController;

  public PermissionsController() {
  }

  public List<UIRepositoryObject> getSelectedObjects() {
    return repoObject;
  }

  public void init( Repository rep ) throws ControllerInitializationException {
    try {
      super.init( rep );
      browseController = (IBrowseController) this.getXulDomContainer().getEventHandler( "browseController" );
      browseController.addContextChangeVetoer( this );
      createBindings();
    } catch ( Exception e ) {
      throw new ControllerInitializationException( e );
    }
  }

  protected void createBindings() {
    super.createBindings();
    fileFolderLabel = (XulLabel) document.getElementById( "file-folder-name" );//$NON-NLS-1$ 
    aclDeck = (XulDeck) document.getElementById( "acl-deck" );//$NON-NLS-1$ 
    inheritParentPermissionCheckbox = (XulCheckbox) document.getElementById( "inherit-from-parent-permission-checkbox" );//$NON-NLS-1$ 
    bf.setBindingType( Binding.Type.ONE_WAY );
    BindingConvertor<List<UIRepositoryObject>, List<UIRepositoryObjectAcl>> securityBindingConverter =
        new BindingConvertor<List<UIRepositoryObject>, List<UIRepositoryObjectAcl>>() {
          @Override
          public List<UIRepositoryObjectAcl> sourceToTarget( List<UIRepositoryObject> ro ) {
            if ( ro == null ) {
              return null;
            }
            if ( ro.size() <= 0 ) {
              return null;
            }
            setSelectedRepositoryObject( ro );
            if ( !hasManageAclAccess() ) {
              // disable everything
              applyAclButton.setDisabled( true );
              addAclButton.setDisabled( true );
              removeAclButton.setDisabled( true );
              inheritParentPermissionCheckbox.setDisabled( true );
              manageAclCheckbox.setDisabled( true );
              deleteCheckbox.setDisabled( true );
              writeCheckbox.setDisabled( true );
              readCheckbox.setDisabled( true );
              viewAclsModel.setHasManageAclAccess( false );
            } else {
              applyAclButton.setDisabled( false );
              inheritParentPermissionCheckbox.setDisabled( false );
              viewAclsModel.setHasManageAclAccess( true );
            }

            viewAclsModel.setRemoveEnabled( false );
            List<UIRepositoryObjectAcl> selectedAclList = Collections.emptyList();
            // we've moved to a new file/folder; need to clear out what the model thinks is selected
            viewAclsModel.setSelectedAclList( selectedAclList );
            permissionsCheckboxHandler.updateCheckboxes( EnumSet.noneOf( RepositoryFilePermission.class ) );
            UIRepositoryObject repoObject = ro.get( 0 );
            try {
              if ( repoObject instanceof IAclObject ) {
                ( (IAclObject) repoObject ).getAcls( viewAclsModel );
              } else {
                throw new IllegalStateException( BaseMessages.getString( PKG, "PermissionsController.NoAclSupport" ) ); //$NON-NLS-1$
              }

              fileFolderLabel
                  .setValue( BaseMessages.getString( PKG, "AclTab.UserRolePermission", repoObject.getName() ) ); //$NON-NLS-1$
              bf.setBindingType( Binding.Type.ONE_WAY );
              bf.createBinding( viewAclsModel, "acls", userRoleList, "elements" ); //$NON-NLS-1$ //$NON-NLS-2$
              updateInheritFromParentPermission();
            } catch ( AccessDeniedException ade ) {
              if ( mainController == null || !mainController.handleLostRepository( ade ) ) {
                messageBox.setTitle( BaseMessages.getString( PKG, "Dialog.Error" ) );//$NON-NLS-1$
                messageBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Ok" ) );//$NON-NLS-1$
                messageBox.setMessage( BaseMessages.getString( PKG,
                    "PermissionsController.UnableToGetAcls", repoObject.getName(), ade.getLocalizedMessage() ) );//$NON-NLS-1$

                messageBox.open();
              }
            } catch ( Exception e ) {
              if ( mainController == null || !mainController.handleLostRepository( e ) ) {
                messageBox.setTitle( BaseMessages.getString( PKG, "Dialog.Error" ) );//$NON-NLS-1$
                messageBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Ok" ) );//$NON-NLS-1$
                messageBox.setMessage( BaseMessages.getString( PKG,
                    "PermissionsController.UnableToGetAcls", repoObject.getName(), e.getLocalizedMessage() ) ); //$NON-NLS-1$
                messageBox.open();
              }
            }

            aclDeck.setSelectedIndex( ACL );
            return viewAclsModel.getAcls();
          }

          @Override
          public List<UIRepositoryObject> targetToSource( List<UIRepositoryObjectAcl> elements ) {
            return null;
          }
        };

    // Binding between the selected repository objects and the user role list for acls
    securityBinding =
        bf.createBinding( browseController, "repositoryObjects", userRoleList, "elements", securityBindingConverter );//$NON-NLS-1$ //$NON-NLS-2$
    securityBinding =
        bf.createBinding( browseController, "repositoryDirectories", userRoleList, "elements", securityBindingConverter );//$NON-NLS-1$ //$NON-NLS-2$

    bf.setBindingType( Binding.Type.BI_DIRECTIONAL );

    // Binding Add Remove button to the inherit check box. If the checkbox is checked that disable add remove
    bf.createBinding( viewAclsModel, "entriesInheriting", inheritParentPermissionCheckbox, "checked" ); //$NON-NLS-1$  //$NON-NLS-2$

    // Setting the default Deck to show no permission
    aclDeck.setSelectedIndex( NO_ACL );
    try {
      if ( securityBinding != null ) {
        securityBinding.fireSourceChanged();
      }
    } catch ( Exception e ) {
      if ( mainController == null || !mainController.handleLostRepository( e ) ) {
        // convert to runtime exception so it bubbles up through the UI
        throw new RuntimeException( e );
      }
    }
  }

  public void setSelectedRepositoryObject( List<UIRepositoryObject> roList ) {
    if ( roList != null ) {
      repoObject.clear();
      repoObject.addAll( roList );
    }
  }

  public String getName() {
    return "permissionsController";//$NON-NLS-1$
  }

  /**
   * apply method is called when the user clicks the apply button on the UI
   */
  public void apply() {
    List<UIRepositoryObject> roList = getSelectedObjects();
    /*
     * if (roList != null && roList.size() == 1 && (roList.get(0) instanceof UIRepositoryDirectory)) {
     * applyAclConfirmationDialog.show(); } else {
     */
    applyOnObjectOnly( roList, false );
    /* } */

  }

  /**
   * applyOnObjectOnly is called to save acl for a file object only
   * 
   * @param roList
   * @param hideDialog
   */
  private void applyOnObjectOnly( List<UIRepositoryObject> roList, boolean hideDialog ) {
    try {
      if ( roList.get( 0 ) instanceof UIRepositoryDirectory ) {
        UIRepositoryDirectory rd = (UIRepositoryDirectory) roList.get( 0 );
        if ( rd instanceof IAclObject ) {
          ( (IAclObject) rd ).setAcls( viewAclsModel );
        } else {
          throw new IllegalStateException( BaseMessages.getString( PKG, "PermissionsController.NoAclSupport" ) ); //$NON-NLS-1$
        }

      } else {
        UIRepositoryContent rc = (UIRepositoryContent) roList.get( 0 );
        if ( rc instanceof ILockObject && ( (ILockObject) rc ).isLocked() ) {
          messageBox.setTitle( BaseMessages.getString( PKG, "Dialog.Error" ) );//$NON-NLS-1$
          messageBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Ok" ) );//$NON-NLS-1$
          messageBox.setMessage( BaseMessages.getString( PKG, "PermissionsController.LockedObjectWarning" ) ); //$NON-NLS-1$
          messageBox.open();
          viewAclsModel.setModelDirty( false );
          return;
        } else if ( rc instanceof IAclObject ) {
          ( (IAclObject) rc ).setAcls( viewAclsModel );
        } else {
          throw new IllegalStateException( BaseMessages.getString( PKG, "PermissionsController.NoAclSupport" ) ); //$NON-NLS-1$
        }
      }
      /*
       * if (hideDialog) { applyAclConfirmationDialog.hide(); }
       */
      viewAclsModel.setModelDirty( false );
      messageBox.setTitle( BaseMessages.getString( PKG, "Dialog.Success" ) ); //$NON-NLS-1$
      messageBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Ok" ) ); //$NON-NLS-1$
      messageBox.setMessage( BaseMessages.getString( PKG, "PermissionsController.PermissionAppliedSuccessfully" ) ); //$NON-NLS-1$
      messageBox.open();
    } catch ( AccessDeniedException ade ) {
      /*
       * if (hideDialog) { applyAclConfirmationDialog.hide(); }
       */
      if ( mainController == null || !mainController.handleLostRepository( ade ) ) {
        messageBox.setTitle( BaseMessages.getString( PKG, "Dialog.Error" ) ); //$NON-NLS-1$
        messageBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Ok" ) ); //$NON-NLS-1$
        messageBox.setMessage( ade.getLocalizedMessage() );
        messageBox.open();
      }
    } catch ( KettleException kex ) {
      if ( mainController == null || !mainController.handleLostRepository( kex ) ) {
        messageBox.setTitle( BaseMessages.getString( PKG, "Dialog.Error" ) ); //$NON-NLS-1$
        messageBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Ok" ) ); //$NON-NLS-1$
        messageBox.setMessage( kex.getLocalizedMessage() );
        messageBox.open();
      }
    }
  }

  /*
   * TODO Once we have the functionality to apply permission recursively to the folder and its children we need to
   * uncomment the section below
   * 
   * public void setApplyOnly() { applyOnlyRadioButton.setSelected(true); applyRecursiveRadioButton.setSelected(false);
   * }
   * 
   * public void setApplyRecursive() { applyOnlyRadioButton.setSelected(false);
   * applyRecursiveRadioButton.setSelected(true); }
   */
  /**
   * applyAcl is called to save the acls back to the repository
   * 
   * @throws Exception
   */
  public void applyAcl() throws Exception {
    /*
     * TODO Once we have the functionality to apply permission recursively to the folder and its children we need to
     * uncomment the section below
     * 
     * // We will call the the server apply method that only applies this acls changes on the current object /*if
     * (applyOnlyRadioButton.isSelected()) {
     */
    List<UIRepositoryObject> roList = getSelectedObjects();
    applyOnObjectOnly( roList, true );
    /*
     * } else { // TODO We will call the the server apply method that applies this acls changes on the current object
     * and its children applyAclConfirmationDialog.hide(); messageBox.setTitle(BaseMessages.getString(PKG,
     * "Dialog.Error")); //$NON-NLS-1$ messageBox.setAcceptLabel(BaseMessages.getString(PKG, "Dialog.Ok"));
     * //$NON-NLS-1$ messageBox.setMessage(BaseMessages.getString(PKG,
     * "PermissionsController.Error.FunctionalityNotSupported")); //$NON-NLS-1$ messageBox.open(); }
     */
  }

  /*
   * public void closeApplyAclConfirmationDialog() { applyAclConfirmationDialog.hide(); }
   */

  /*
   * If the user check or unchecks the inherit from parent checkbox, this method is called.
   */
  public void updateInheritFromParentPermission() throws AccessDeniedException {
    viewAclsModel.setEntriesInheriting( inheritParentPermissionCheckbox.isChecked() );
    if ( inheritParentPermissionCheckbox.isChecked() ) {
      addAclButton.setDisabled( true );
      UIRepositoryObject ro = repoObject.get( 0 );
      if ( ro instanceof IAclObject ) {
        // force inherit to true to get effective ACLs before apply...
        ( (IAclObject) ro ).clearAcl();
        ( (IAclObject) ro ).getAcls( viewAclsModel, true );
      }
      permissionsCheckboxHandler.updateCheckboxes( EnumSet.noneOf( RepositoryFilePermission.class ) );
    } else {
      addAclButton.setDisabled( !hasManageAclAccess() );
      permissionsCheckboxHandler.processCheckboxes();
    }
  }

  @Override
  protected void updateCheckboxes( UIRepositoryObjectAcl acl ) {
    permissionsCheckboxHandler.updateCheckboxes( !inheritParentPermissionCheckbox.isChecked() && hasManageAclAccess(),
        acl.getPermissionSet() );
  }
}
