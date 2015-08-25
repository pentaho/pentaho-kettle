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

package org.pentaho.di.repository.pur.model;

import java.util.Set;

import org.pentaho.di.repository.IUser;

/**
 * Repository User object with role support
 * @author rmansoor
 *
 */
public interface IEEUser extends IUser {
  /**
   * Associate a role to this particular user
   * 
   * @param role to be associate
   * @return return the status whether the role association to this user was successful or not
   */
  public boolean addRole(IRole role);
  /**
   * Remove the association of a role to this particular user
   * 
   * @param role to be un associated
   * @return return the status whether the role un association to this user was successful or not
   */  
  public boolean removeRole(IRole role);
  /**
   * Clear all the role association from this particular user
   * 
   */    
  public void clearRoles();
  /**
   * Associate set of roles to this particular user
   * 
   * @param set of roles
   */  
  public void setRoles(Set<IRole> roles);
  /**
   * Retrieve the set of roles associated to this particular user
   * 
   * @return set of associated roles
   */      
  public Set<IRole> getRoles();
}
