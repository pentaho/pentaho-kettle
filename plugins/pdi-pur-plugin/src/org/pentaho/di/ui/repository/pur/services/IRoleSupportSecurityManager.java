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
