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
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.ui.xul.components.XulCheckbox;

/**
 * This class aims to handle the enabling, disabling, checking and unchecking of the permissions checkboxes for the
 * PurRepository
 * 
 * @author bryan
 */
public class PermissionsCheckboxHandler {
  /**
   * Class to keep track of relevant checkbox, as well as which checkboxes should be disabled and enabled when this is
   * the highest permission the user has
   * 
   * @author bryan
   */
  private static class PermissionsCheckboxes {
    public final RepositoryFilePermission repositoryFilePermission;

    public final XulCheckbox permissionCheckbox;

    public final Set<XulCheckbox> enabledBoxes;

    public final Set<XulCheckbox> disabledBoxes;

    /**
     * @param repositoryFilePermission
     *          the permission that this object is associated with
     * @param permissionCheckbox
     *          the checkbox for the permission
     * @param enabledBoxes
     *          the checkboxes that should be enabled when this is the highest permission
     * @param disabledBoxes
     *          the checkboxes that should be disabled when this is the highest permission
     */
    public PermissionsCheckboxes( RepositoryFilePermission repositoryFilePermission, XulCheckbox permissionCheckbox,
        Collection<XulCheckbox> enabledBoxes, Collection<XulCheckbox> disabledBoxes ) {
      this.repositoryFilePermission = repositoryFilePermission;
      this.permissionCheckbox = permissionCheckbox;
      this.enabledBoxes = Collections.unmodifiableSet( new HashSet<XulCheckbox>( enabledBoxes ) );
      this.disabledBoxes = Collections.unmodifiableSet( new HashSet<XulCheckbox>( disabledBoxes ) );
    }
  }

  private final List<PermissionsCheckboxes> ALL_PERMISSIONS;

  /**
   * Constructs the PermissionsCheckboxHandler by giving it references to the relevant checkboxes The ALL_PERMISSIONS
   * list is used to do cascading permissions, it is important that the permissions are in it in order of highest to
   * lowest
   */
  public PermissionsCheckboxHandler( XulCheckbox readCheckbox, XulCheckbox writeCheckbox, XulCheckbox deleteCheckbox,
      XulCheckbox manageCheckbox ) {
    List<PermissionsCheckboxes> permissionsList = new ArrayList<PermissionsCheckboxes>();
    permissionsList.add( new PermissionsCheckboxes( RepositoryFilePermission.ACL_MANAGEMENT, manageCheckbox, Arrays
        .asList( manageCheckbox ), Arrays.asList( readCheckbox, writeCheckbox, deleteCheckbox ) ) );
    permissionsList.add( new PermissionsCheckboxes( RepositoryFilePermission.DELETE, deleteCheckbox, Arrays.asList(
        manageCheckbox, deleteCheckbox ), Arrays.asList( readCheckbox, writeCheckbox ) ) );
    permissionsList.add( new PermissionsCheckboxes( RepositoryFilePermission.WRITE, writeCheckbox, Arrays.asList(
        writeCheckbox, deleteCheckbox ), Arrays.asList( readCheckbox, manageCheckbox ) ) );
    permissionsList.add( new PermissionsCheckboxes( RepositoryFilePermission.READ, readCheckbox, Arrays
        .asList( writeCheckbox ), Arrays.asList( readCheckbox, deleteCheckbox, manageCheckbox ) ) );
    ALL_PERMISSIONS = Collections.unmodifiableList( permissionsList );
  }

  /**
   * Controls the enabling and disabling of the checkboxes based on the permission level
   * 
   * @param enableAppropriate
   *          boolean indicating whether the appropriate boxes should be enabled (set to false to disable all)
   * @param permissionsCheckboxes
   *          the permission level object
   */
  private void enableDisableBoxes( boolean enableAppropriate, PermissionsCheckboxes permissionsCheckboxes ) {
    for ( XulCheckbox checkbox : permissionsCheckboxes.disabledBoxes ) {
      checkbox.setDisabled( true );
    }

    for ( XulCheckbox checkbox : permissionsCheckboxes.enabledBoxes ) {
      checkbox.setDisabled( !enableAppropriate );
    }
  }

  /**
   * Goes through the checkboxes and returns the resulting set of permissions
   * 
   * @return the resulting set of permissions
   */
  public EnumSet<RepositoryFilePermission> processCheckboxes() {
    return processCheckboxes( false );
  }

  /**
   * Goes through the checkboxes and returns the resulting set of permissions
   * 
   * @param enableAppropriate
   *          boolean indicating whether the appropriate boxes should be enabled (set to false to disable all)
   * @return the resulting set of permissions
   */
  public EnumSet<RepositoryFilePermission> processCheckboxes( boolean enableAppropriate ) {
    EnumSet<RepositoryFilePermission> result = EnumSet.noneOf( RepositoryFilePermission.class );
    boolean foundMaxPermission = false;
    for ( PermissionsCheckboxes permissionsCheckboxes : ALL_PERMISSIONS ) {
      if ( foundMaxPermission ) {
        result.add( permissionsCheckboxes.repositoryFilePermission );
      } else if ( permissionsCheckboxes.permissionCheckbox.isChecked() ) {
        enableDisableBoxes( enableAppropriate, permissionsCheckboxes );
        foundMaxPermission = true;
        result.add( permissionsCheckboxes.repositoryFilePermission );
      }
    }
    if ( !foundMaxPermission ) {
      setAllDisabled( true );
      if ( enableAppropriate ) {
        ALL_PERMISSIONS.get( ALL_PERMISSIONS.size() - 1 ).permissionCheckbox.setDisabled( false );
      }
    }
    return result;
  }

  /**
   * Updates the checkboxes so they reflect the current permissions
   * 
   * @param permissionEnumSet
   *          the current permissions
   */
  public void updateCheckboxes( EnumSet<RepositoryFilePermission> permissionEnumSet ) {
    updateCheckboxes( false, permissionEnumSet );
  }

  /**
   * Updates the checkboxes so they reflect the current permissions
   * 
   * @param enableAppropriate
   *          boolean indicating whether the appropriate boxes should be enabled (set to false to disable all)
   * @param permissionEnumSet
   *          the current permissions
   */
  public void updateCheckboxes( boolean enableAppropriate, EnumSet<RepositoryFilePermission> permissionEnumSet ) {
    Set<RepositoryFilePermission> permissions =
        new HashSet<RepositoryFilePermission>( Arrays.asList( permissionEnumSet
            .toArray( new RepositoryFilePermission[permissionEnumSet.size()] ) ) );
    boolean foundMaxPermission = false;
    if ( permissions.remove( RepositoryFilePermission.ALL ) ) {
      permissions.add( ALL_PERMISSIONS.get( 0 ).repositoryFilePermission );
    }
    for ( PermissionsCheckboxes permissionsCheckboxes : ALL_PERMISSIONS ) {
      if ( foundMaxPermission ) {
        permissionsCheckboxes.permissionCheckbox.setChecked( true );
      } else {
        if ( permissions.contains( permissionsCheckboxes.repositoryFilePermission ) ) {
          foundMaxPermission = true;
          permissionsCheckboxes.permissionCheckbox.setChecked( true );
          enableDisableBoxes( enableAppropriate, permissionsCheckboxes );
        } else {
          permissionsCheckboxes.permissionCheckbox.setChecked( false );
        }
      }
    }
    if ( !foundMaxPermission ) {
      setAllDisabled( true );
      if ( enableAppropriate ) {
        ALL_PERMISSIONS.get( ALL_PERMISSIONS.size() - 1 ).permissionCheckbox.setDisabled( false );
      }
    }
  }

  /**
   * Sets the checked value of all the checkboxes
   * 
   * @param value
   *          the value
   */
  public void setAllChecked( boolean value ) {
    for ( PermissionsCheckboxes permissionsCheckboxes : ALL_PERMISSIONS ) {
      permissionsCheckboxes.permissionCheckbox.setChecked( value );
    }
  }

  /**
   * Sets the disabled value of all the checkboxes
   * 
   * @param value
   *          the value
   */
  public void setAllDisabled( boolean value ) {
    for ( PermissionsCheckboxes permissionsCheckboxes : ALL_PERMISSIONS ) {
      permissionsCheckboxes.permissionCheckbox.setDisabled( value );
    }
  }
}
