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
package org.pentaho.di.repository.pur.model;

import java.util.HashSet;
import java.util.Set;

import org.pentaho.di.repository.IUser;

public class EERoleInfo implements IRole, java.io.Serializable {

  private static final long serialVersionUID = -7422069585209086417L; /* EESOURCE: UPDATE SERIALVERUID */

  public static final String REPOSITORY_ELEMENT_TYPE = "role"; //$NON-NLS-1$

  // ~ Instance fields
  // =================================================================================================

  private String name;

  private String description;

  private Set<IUser> users;

  // ~ Constructors
  // ====================================================================================================

  public EERoleInfo() {
    this.name = null;
    this.description = null;
    users = new HashSet<IUser>();
  }

  public void setName( String name ) {
    this.name = name;
  }

  public EERoleInfo( String name ) {
    this( name, null );
  }

  public EERoleInfo( String name, String description ) {
    this();
    this.name = name;
    this.description = description;
  }

  public EERoleInfo( String name, String description, Set<IUser> users ) {
    this( name, description );
    this.users = users;
  }

  // ~ Methods
  // =========================================================================================================

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  public void setUsers( Set<IUser> users ) {
    this.users = users;
  }

  public Set<IUser> getUsers() {
    return users;
  }

  public boolean addUser( IUser user ) {
    return users.add( user );
  }

  public boolean removeUser( IUser user ) {
    return users.remove( user );
  }

  public void clearUsers() {
    users.clear();
  }

  public IRole getRole() {
    // TODO Auto-generated method stub
    return this;
  }
}
