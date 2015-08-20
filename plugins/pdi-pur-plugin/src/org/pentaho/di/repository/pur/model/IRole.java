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
 * Repository Role object
 * @author rmansoor
 *
 */
public interface IRole {
  /**
   * Set the name of the role
   * 
   * @param name of the role
   */
  public void setName(String name);
  /**
   * Retrieve the name of the role
   * 
   * @return role name
   */  
  public String getName();
  /**
   * Retrieve the role description
   * 
   * @return role name
   */    
  public String getDescription();
  /**
   * Set the description of the role
   * 
   * @param name of the role
   */
  public void setDescription(String description);
  /**
   * Associate set of users to the role
   * 
   * @param set of users
   */  
  public void setUsers(Set<IUser> users);
  /**
   * Retrieve the set of users associate to this particular role
   * 
   * @return set of associated users
   */      
  public Set<IUser> getUsers();
  /**
   * Associate a user to this particular role
   * 
   * @return status if the user association was successful
   */  
  public boolean addUser(IUser user);
  /**
   * Remove the user associate from this particular role
   * 
   * @return status if the user un association was successful 
   */    
  public boolean removeUser(IUser user);
  /**
   * Clear all the user association for this particular role
   */
  public void clearUsers();
  /**
   * Get the repository role  
   * 
   * @return repository role object 
   */    
  public IRole getRole();
}
