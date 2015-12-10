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
package org.pentaho.di.ui.repository.pur.repositoryexplorer.abs.controller;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.i18n.GlobalMessages;
import org.pentaho.di.i18n.LanguageChoice;
import org.pentaho.di.repository.RepositorySecurityManager;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.IUIRole;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.abs.IUIAbsRole;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.abs.model.UIAbsSecurity;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.controller.EESecurityController;
import org.pentaho.di.ui.repository.pur.services.IAbsSecurityManager;
import org.pentaho.di.ui.spoon.SpoonLifecycleListener;
import org.pentaho.di.ui.spoon.SpoonPluginManager;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulCheckbox;
import org.pentaho.ui.xul.components.XulMessageBox;
import org.pentaho.ui.xul.containers.XulListbox;
import org.pentaho.ui.xul.containers.XulVbox;

/**
 * {@code XulEventHandler} for the Security panel of the repository explorer. This class handles only task permission
 * (aka ABS)-related functionality. {@link SecurityController} handles users and {@link EESecurityController} handles
 * roles.
 */
public class AbsController extends EESecurityController implements java.io.Serializable {

  private static final long serialVersionUID = -9005536054475853743L; /* EESOURCE: UPDATE SERIALVERUID */
  protected boolean initialized = false;

  private static final Class<?> PKG = IUIAbsRole.class;

  protected ResourceBundle messages = new ResourceBundle() {

    @Override
    public Enumeration<String> getKeys() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    protected Object handleGetObject( String key ) {
      return BaseMessages.getString( PKG, key );
    }

  };

  private XulButton applyLogicalRolesButton;

  private XulButton applyLogicalSystemRolesButton;

  private XulVbox logicalRolesBox;

  private XulVbox logicalSystemRolesBox;

  private XulListbox roleListBox;

  private XulListbox systemRoleListBox;

  Map<XulCheckbox, String> logicalRoleChecboxMap = new HashMap<XulCheckbox, String>();

  Map<XulCheckbox, String> logicalSystemRoleChecboxMap = new HashMap<XulCheckbox, String>();

  private BindingConvertor<Integer, Boolean> buttonConverter;

  private UIAbsSecurity absSecurity;

  public AbsController() {

  }

  @Override
  protected boolean initService() {
    try {
      if ( repository.hasService( IAbsSecurityManager.class ) ) {
        service = (RepositorySecurityManager) repository.getService( IAbsSecurityManager.class );
        String localeValue = null;
        try {
          localeValue = GlobalMessages.getLocale().getDisplayName();
        } catch ( MissingResourceException e ) {
          try {
            localeValue = LanguageChoice.getInstance().getFailoverLocale().getDisplayName();
          } catch ( MissingResourceException e2 ) {
            localeValue = "en_US"; //$NON-NLS-1$
          }
        }
        ( (IAbsSecurityManager) service ).initialize( localeValue );
      } else {
        return false;
      }
    } catch ( Exception e ) {
      throw new RuntimeException( e );
    }
    return true;
  }

  @Override
  protected void setInitialDeck() {
    super.setInitialDeck();
    initializeLogicalRolesUI();
    initializeLogicalSystemRolesUI();
  }

  protected void createSecurity() throws Exception {
    security = eeSecurity = absSecurity = new UIAbsSecurity( service );
  }

  @Override
  protected void createBindings() {
    super.createBindings();
    roleListBox = (XulListbox) document.getElementById( "roles-list" );//$NON-NLS-1$
    systemRoleListBox = (XulListbox) document.getElementById( "system-roles-list" );//$NON-NLS-1$
    applyLogicalRolesButton = (XulButton) document.getElementById( "apply-action-permission" );//$NON-NLS-1$
    applyLogicalSystemRolesButton = (XulButton) document.getElementById( "apply-system-role-action-permission" );//$NON-NLS-1$

    logicalRolesBox = (XulVbox) document.getElementById( "role-action-permissions-vbox" );//$NON-NLS-1$
    logicalSystemRolesBox = (XulVbox) document.getElementById( "system-role-action-permissions-vbox" );//$NON-NLS-1$
    bf.setBindingType( Binding.Type.ONE_WAY );
    // Action based security permissions
    buttonConverter = new BindingConvertor<Integer, Boolean>() {

      @Override
      public Boolean sourceToTarget( Integer value ) {
        if ( value != null && value >= 0 ) {
          return false;
        }
        return true;
      }

      @Override
      public Integer targetToSource( Boolean value ) {
        // TODO Auto-generated method stub
        return null;
      }
    };
    bf.createBinding( roleListBox, "selectedIndex", applyLogicalRolesButton, "disabled", buttonConverter );//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding( systemRoleListBox, "selectedIndex", applyLogicalSystemRolesButton, "disabled", buttonConverter );//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding( absSecurity, "selectedRole", this, "selectedRoleChanged" );//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding( absSecurity, "selectedSystemRole", this, "selectedSystemRoleChanged" );//$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Update the model with the current status
   */
  public void updateRoleActionPermission() {
    for ( Entry<XulCheckbox, String> currentEntry : logicalRoleChecboxMap.entrySet() ) {
      XulCheckbox permissionCheckbox = currentEntry.getKey();
      if ( permissionCheckbox.isChecked() ) {
        absSecurity.addLogicalRole( currentEntry.getValue() );
      } else {
        absSecurity.removeLogicalRole( currentEntry.getValue() );
      }
    }
  }

  /**
   * Update the model with the current status
   */
  public void updateSystemRoleActionPermission() {
    for ( Entry<XulCheckbox, String> currentEntry : logicalSystemRoleChecboxMap.entrySet() ) {
      XulCheckbox permissionCheckbox = currentEntry.getKey();
      if ( permissionCheckbox.isChecked() ) {
        absSecurity.addLogicalRole( currentEntry.getValue() );
      } else {
        absSecurity.removeLogicalRole( currentEntry.getValue() );
      }
    }
  }

  /**
   * Save the permission for the selected role
   */
  public void applyRoleActionPermission() {
    XulMessageBox messageBox = this.getMessageBox();
    IUIRole role = null;
    IUIAbsRole absRole = null;
    try {
      role = absSecurity.getSelectedRole();
      if ( role instanceof IUIAbsRole ) {
        absRole = (IUIAbsRole) role;
      } else {
        throw new IllegalStateException();
      }
      ( (IAbsSecurityManager) service ).setLogicalRoles( absRole.getName(), absRole.getLogicalRoles() );
      messageBox.setTitle( BaseMessages.getString( PKG, "Dialog.Success" ) );//$NON-NLS-1$
      messageBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Ok" ) );//$NON-NLS-1$
      messageBox.setMessage( BaseMessages.getString( PKG, "AbsController.RoleActionPermission.Success" ) );//$NON-NLS-1$
      messageBox.open();
      // Refresh permissions in open tabs
      SpoonPluginManager.getInstance().notifyLifecycleListeners(
          SpoonLifecycleListener.SpoonLifeCycleEvent.REPOSITORY_CHANGED );
    } catch ( KettleException e ) {
      messageBox.setTitle( BaseMessages.getString( PKG, "Dialog.Error" ) );//$NON-NLS-1$
      messageBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Ok" ) );//$NON-NLS-1$
      messageBox.setMessage( BaseMessages.getString( PKG,
          "AbsController.RoleActionPermission.UnableToApplyPermissions", role.getName(), e.getLocalizedMessage() ) );//$NON-NLS-1$
      messageBox.open();
    }
  }

  /**
   * Save the permission for the selected system role
   */
  public void applySystemRoleActionPermission() {
    XulMessageBox messageBox = this.getMessageBox();
    IUIRole role = null;
    IUIAbsRole absRole = null;
    try {
      role = absSecurity.getSelectedSystemRole();
      if ( role instanceof IUIAbsRole ) {
        absRole = (IUIAbsRole) role;
      } else {
        throw new IllegalStateException();
      }
      ( (IAbsSecurityManager) service ).setLogicalRoles( absRole.getName(), absRole.getLogicalRoles() );
      messageBox.setTitle( BaseMessages.getString( PKG, "Dialog.Success" ) );//$NON-NLS-1$
      messageBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Ok" ) );//$NON-NLS-1$
      messageBox.setMessage( BaseMessages.getString( PKG, "AbsController.RoleActionPermission.Success" ) );//$NON-NLS-1$
      messageBox.open();
      // Refresh permissions in open tabs
      SpoonPluginManager.getInstance().notifyLifecycleListeners(
          SpoonLifecycleListener.SpoonLifeCycleEvent.REPOSITORY_CHANGED );
    } catch ( KettleException e ) {
      messageBox.setTitle( BaseMessages.getString( PKG, "Dialog.Error" ) );//$NON-NLS-1$
      messageBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Ok" ) );//$NON-NLS-1$
      messageBox.setMessage( BaseMessages.getString( PKG,
          "AbsController.RoleActionPermission.UnableToApplyPermissions", role.getName(), e.getLocalizedMessage() ) );//$NON-NLS-1$
      messageBox.open();
    }
  }

  /**
   * The method is called when a user select a role from the role list. This method reads the current selected role and
   * populates the Action Permission UI with the details
   */
  public void setSelectedRoleChanged( IUIRole role ) throws Exception {
    IUIAbsRole absRole = null;
    uncheckAllActionPermissions();
    if ( role instanceof IUIAbsRole ) {
      absRole = (IUIAbsRole) role;
      if ( absRole != null && absRole.getLogicalRoles() != null ) {
        for ( String permission : absRole.getLogicalRoles() ) {
          XulCheckbox permissionCheckbox = findRoleCheckbox( permission );
          if ( permissionCheckbox != null ) {
            permissionCheckbox.setChecked( true );
          }
        }
      }
    } else {
      throw new IllegalStateException();
    }
  }

  /**
   * The method is called when a user select a role from the role list. This method reads the current selected role and
   * populates the Action Permission UI with the details
   */
  public void setSelectedSystemRoleChanged( IUIRole role ) throws Exception {
    IUIAbsRole absRole = null;
    uncheckAllSystemActionPermissions();
    if ( role instanceof IUIAbsRole ) {
      absRole = (IUIAbsRole) role;
      if ( absRole != null && absRole.getLogicalRoles() != null ) {
        for ( String permission : absRole.getLogicalRoles() ) {
          XulCheckbox permissionCheckbox = findSystemRoleCheckbox( permission );
          if ( permissionCheckbox != null ) {
            permissionCheckbox.setChecked( true );
          }
        }
      }
    } else {
      throw new IllegalStateException();
    }
  }

  private XulCheckbox findRoleCheckbox( String permission ) {
    for ( Entry<XulCheckbox, String> currentEntry : logicalRoleChecboxMap.entrySet() ) {
      if ( currentEntry.getValue().equals( permission ) ) {
        return currentEntry.getKey();
      }
    }
    return null;
  }

  private XulCheckbox findSystemRoleCheckbox( String permission ) {
    for ( Entry<XulCheckbox, String> currentEntry : logicalSystemRoleChecboxMap.entrySet() ) {
      if ( currentEntry.getValue().equals( permission ) ) {
        return currentEntry.getKey();
      }
    }
    return null;
  }

  /**
   * Initialized the ActionPermissions UI with all the possible values from LogicalRoles enum
   */
  private void initializeLogicalRolesUI() {
    try {
      Map<String, String> logicalRoles =
          ( (IAbsSecurityManager) service ).getAllLogicalRoles( GlobalMessages.getLocale().getDisplayName() );
      for ( Entry<String, String> logicalRole : logicalRoles.entrySet() ) {
        XulCheckbox logicalRoleCheckbox;
        logicalRoleCheckbox = (XulCheckbox) document.createElement( "checkbox" );//$NON-NLS-1$
        logicalRoleCheckbox.setLabel( logicalRole.getValue() );
        logicalRoleCheckbox.setId( logicalRole.getValue() );
        logicalRoleCheckbox.setCommand( "iSecurityController.updateRoleActionPermission()" );//$NON-NLS-1$
        logicalRoleCheckbox.setFlex( 1 );
        logicalRoleCheckbox.setDisabled( true );
        logicalRolesBox.addChild( logicalRoleCheckbox );
        logicalRoleChecboxMap.put( logicalRoleCheckbox, logicalRole.getKey() );
        bf.setBindingType( Binding.Type.ONE_WAY );
        bf.createBinding( roleListBox, "selectedIndex", logicalRoleCheckbox, "disabled", buttonConverter );//$NON-NLS-1$ //$NON-NLS-2$
      }
    } catch ( XulException xe ) {

    } catch ( KettleException xe ) {

    }
  }

  /**
   * Initialized the ActionPermissions UI with all the possible values from LogicalSystemRoles enum
   */
  private void initializeLogicalSystemRolesUI() {
    try {
      Map<String, String> logicalRoles =
          ( (IAbsSecurityManager) service ).getAllLogicalRoles( GlobalMessages.getLocale().getDisplayName() );
      for ( Entry<String, String> logicalRole : logicalRoles.entrySet() ) {
        XulCheckbox logicalSystemRoleCheckbox;
        logicalSystemRoleCheckbox = (XulCheckbox) document.createElement( "checkbox" );//$NON-NLS-1$
        logicalSystemRoleCheckbox.setLabel( logicalRole.getValue() );
        logicalSystemRoleCheckbox.setId( logicalRole.getValue() );
        logicalSystemRoleCheckbox.setCommand( "iSecurityController.updateSystemRoleActionPermission()" );//$NON-NLS-1$
        logicalSystemRoleCheckbox.setFlex( 1 );
        logicalSystemRoleCheckbox.setDisabled( true );
        logicalSystemRolesBox.addChild( logicalSystemRoleCheckbox );
        logicalSystemRoleChecboxMap.put( logicalSystemRoleCheckbox, logicalRole.getKey() );
        bf.setBindingType( Binding.Type.ONE_WAY );
        bf.createBinding( systemRoleListBox, "selectedIndex", logicalSystemRoleCheckbox, "disabled", buttonConverter );//$NON-NLS-1$ //$NON-NLS-2$
      }
    } catch ( XulException xe ) {

    } catch ( KettleException xe ) {

    }
  }

  private void uncheckAllActionPermissions() {
    for ( XulCheckbox permissionCheckbox : logicalRoleChecboxMap.keySet() ) {
      permissionCheckbox.setChecked( false );
    }
  }

  private void uncheckAllSystemActionPermissions() {
    for ( XulCheckbox permissionCheckbox : logicalSystemRoleChecboxMap.keySet() ) {
      permissionCheckbox.setChecked( false );
    }
  }
}
