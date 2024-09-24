/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/
package org.pentaho.di.ui.repository.pur.services;

import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.RepositorySecurityManager;
import org.pentaho.di.repository.pur.model.IRole;

/**
 * Repository Security Manager with the Role support
 * 
 * @author rmansoor
 * 
 */
public interface IRoleSupportSecurityManager extends RepositorySecurityManager {
  /**
   * Constructs the repository version of the IRole implementation
   * 
   * @return return the instance of IRole
   * @throws KettleException
   */
  public IRole constructRole() throws KettleException;

  /**
   * Creates a role in the system with the given information
   * 
   * @param role
   *          to be created
   * @throws KettleException
   */
  public void createRole( IRole role ) throws KettleException;

  /**
   * Retrieves the role with a given name
   * 
   * @param name
   *          of the role to be searched
   * @return role object matching the name
   * @throws KettleException
   */
  public IRole getRole( String name ) throws KettleException;

  /**
   * Retrieves all available roles in the system
   * 
   * @return the list of available roles
   * @throws KettleException
   */
  public List<IRole> getRoles() throws KettleException;

  /**
   * Retrieves the default roles in the system.
   * 
   * @return the list of default roles
   * @throws KettleException
   */
  public List<IRole> getDefaultRoles() throws KettleException;

  /**
   * Save the list of roles in the system
   * 
   * @param list
   *          of role objects to be saved
   * @throws KettleException
   */
  public void setRoles( List<IRole> roles ) throws KettleException;

  /**
   * Updates a particular role in the system
   * 
   * @param role
   *          object to be updated
   * @throws KettleException
   */
  public void updateRole( IRole role ) throws KettleException;

  /**
   * Deletes a list of roles in the system
   * 
   * @param list
   *          of role object to be deleted
   * @throws KettleException
   */
  public void deleteRoles( List<IRole> roles ) throws KettleException;

  /**
   * Delete a particular role matching the role name
   * 
   * @param name
   *          of the role to be deleted
   * @throws KettleException
   */
  public void deleteRole( String name ) throws KettleException;
}
