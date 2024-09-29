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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.IAclObject;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.IUIEEUser;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.model.UIRepositoryObjectAcl;
import org.pentaho.di.ui.repository.repositoryexplorer.AccessDeniedException;
import org.pentaho.di.ui.repository.repositoryexplorer.ContextChangeVetoer;
import org.pentaho.di.ui.repository.repositoryexplorer.ControllerInitializationException;
import org.pentaho.di.ui.repository.repositoryexplorer.IUISupportController;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.ConnectionsController;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIDatabaseConnection;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.components.XulLabel;

/**
 * This class acts as a controller in the Connections Repository Explorer tab, for managing the ACLs of each database
 * connection.
 * 
 * @author Will Gorman (wgorman@pentaho.com)
 * 
 */
public class ConnectionPermissionsController extends AbstractPermissionsController implements ContextChangeVetoer,
    IUISupportController, Serializable {

  private static final long serialVersionUID = -8922989722897392836L; /* EESOURCE: UPDATE SERIALVERUID */

  private static final Class<?> PKG = IUIEEUser.class;

  private ConnectionsController connectionsController;

  private XulLabel connNameLabel;
  private List<UIDatabaseConnection> selectedDbConns = new ArrayList<UIDatabaseConnection>();

  @Override
  public String getName() {
    return "connectionPermissionsController";//$NON-NLS-1$
  }

  public void init( Repository rep ) throws ControllerInitializationException {
    try {
      super.init( rep );
      connectionsController =
          (ConnectionsController) this.getXulDomContainer().getEventHandler( "connectionsController" );
      connectionsController.addContextChangeVetoer( this );
      createBindings();
    } catch ( Exception e ) {
      throw new ControllerInitializationException( e );
    }
  }

  @Override
  protected String getXulPrefix() {
    return "conn-";
  }

  protected void createBindings() {
    super.createBindings();
    connNameLabel = (XulLabel) document.getElementById( "conn-name" );//$NON-NLS-1$ 

    bf.setBindingType( Binding.Type.ONE_WAY );

    BindingConvertor<List<UIDatabaseConnection>, List<UIRepositoryObjectAcl>> securityBindingConverter =
        new BindingConvertor<List<UIDatabaseConnection>, List<UIRepositoryObjectAcl>>() {
          @Override
          public List<UIRepositoryObjectAcl> sourceToTarget( List<UIDatabaseConnection> ro ) {
            if ( ro == null ) {
              return null;
            }
            if ( ro.size() <= 0 ) {
              return null;
            }
            setSelectedDatabaseConnections( ro );

            if ( !hasManageAclAccess() ) {
              applyAclButton.setDisabled( true );
              addAclButton.setDisabled( true );
              removeAclButton.setDisabled( true );
              manageAclCheckbox.setDisabled( true );
              deleteCheckbox.setDisabled( true );
              writeCheckbox.setDisabled( true );
              readCheckbox.setDisabled( true );
              viewAclsModel.setHasManageAclAccess( false );
            } else {
              applyAclButton.setDisabled( false );
              addAclButton.setDisabled( false );
              viewAclsModel.setHasManageAclAccess( true );
            }

            viewAclsModel.setRemoveEnabled( false );
            List<UIRepositoryObjectAcl> selectedAclList = Collections.emptyList();
            // we've moved to a connection; need to clear out what the model thinks is selected
            viewAclsModel.setSelectedAclList( selectedAclList );
            permissionsCheckboxHandler.setAllChecked( false );
            UIDatabaseConnection dbconnObject = ro.get( 0 );
            try {
              if ( dbconnObject instanceof IAclObject ) {
                IAclObject aclObj = (IAclObject) dbconnObject;

                // This is a special case for DB Connections, wipe out the isEnherting flag.
                // This will cause the model to become "dirty", and prompt the user for changes the first time
                // let's make sure the default creation behavior of connections is to be that inheritance is
                // set to false, so this case never presents itself in the wild.

                aclObj.getAcls( viewAclsModel );
                if ( viewAclsModel.isEntriesInheriting() ) {
                  viewAclsModel.setEntriesInheriting( false );
                  aclObj.setAcls( viewAclsModel );
                  viewAclsModel.setModelDirty( false );
                }

              } else {
                throw new IllegalStateException( BaseMessages.getString( PKG, "PermissionsController.NoAclSupport" ) ); //$NON-NLS-1$
              }

              connNameLabel.setValue( BaseMessages.getString( PKG,
                  "AclTab.ConnectionPermission", dbconnObject.getDisplayName() ) ); //$NON-NLS-1$
              bf.setBindingType( Binding.Type.ONE_WAY );
              bf.createBinding( viewAclsModel, "acls", userRoleList, "elements" ); //$NON-NLS-1$ //$NON-NLS-2$
            } catch ( AccessDeniedException ade ) {
              messageBox.setTitle( BaseMessages.getString( PKG, "Dialog.Error" ) );//$NON-NLS-1$
              messageBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Ok" ) );//$NON-NLS-1$
              messageBox.setMessage( BaseMessages.getString( PKG,
                  "PermissionsController.UnableToGetAcls", dbconnObject.getName(), ade.getLocalizedMessage() ) );//$NON-NLS-1$

              messageBox.open();
            } catch ( Exception e ) {
              if ( mainController == null || !mainController.handleLostRepository( e ) ) {
                messageBox.setTitle( BaseMessages.getString( PKG, "Dialog.Error" ) );//$NON-NLS-1$
                messageBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Ok" ) );//$NON-NLS-1$
                messageBox.setMessage( BaseMessages.getString( PKG,
                    "PermissionsController.UnableToGetAcls", dbconnObject.getName(), e.getLocalizedMessage() ) ); //$NON-NLS-1$
                messageBox.open();
              }
            }
            return viewAclsModel.getAcls();
          }

          @Override
          public List<UIDatabaseConnection> targetToSource( List<UIRepositoryObjectAcl> elements ) {
            // One way binding, nothing to do here
            return null;
          }
        };

    // Binding between the selected repository objects and the user role list for acls
    securityBinding =
        bf.createBinding( connectionsController,
            "repositoryConnections", userRoleList, "elements", securityBindingConverter );//$NON-NLS-1$ //$NON-NLS-2$

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

  public void setSelectedDatabaseConnections( List<UIDatabaseConnection> dbconnList ) {
    if ( dbconnList != null ) {
      selectedDbConns.clear();
      selectedDbConns.addAll( dbconnList );
    }
  }

  public List<UIDatabaseConnection> getSelectedObjects() {
    return selectedDbConns;
  }

  /**
   * apply method is called when the user clicks the apply button on the UI
   */
  public void apply() {
    List<UIDatabaseConnection> roList = getSelectedObjects();
    applyOnObjectOnly( roList, false );
  }

  /**
   * applyOnObjectOnly is called to save acl for a file object only
   * 
   * @param roList
   * @param hideDialog
   */
  private void applyOnObjectOnly( List<UIDatabaseConnection> roList, boolean hideDialog ) {
    try {
      UIDatabaseConnection rd = roList.get( 0 );
      if ( rd instanceof IAclObject ) {
        ( (IAclObject) rd ).setAcls( viewAclsModel );
      } else {
        throw new IllegalStateException( BaseMessages.getString( PKG, "PermissionsController.NoAclSupport" ) ); //$NON-NLS-1$
      }
      viewAclsModel.setModelDirty( false );
      messageBox.setTitle( BaseMessages.getString( PKG, "Dialog.Success" ) ); //$NON-NLS-1$
      messageBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Ok" ) ); //$NON-NLS-1$
      messageBox.setMessage( BaseMessages.getString( PKG, "PermissionsController.PermissionAppliedSuccessfully" ) ); //$NON-NLS-1$
      messageBox.open();
    } catch ( AccessDeniedException ade ) {
      if ( mainController == null || !mainController.handleLostRepository( ade ) ) {
        messageBox.setTitle( BaseMessages.getString( PKG, "Dialog.Error" ) ); //$NON-NLS-1$
        messageBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Ok" ) ); //$NON-NLS-1$
        messageBox.setMessage( ade.getLocalizedMessage() );
        messageBox.open();
      }
    }
  }

  /**
   * applyAcl is called to save the acls back to the repository
   * 
   * @throws Exception
   */
  public void applyAcl() throws Exception {
    List<UIDatabaseConnection> roList = getSelectedObjects();
    applyOnObjectOnly( roList, true );
  }

}
