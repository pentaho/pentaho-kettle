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
package org.pentaho.di.ui.repository.pur.repositoryexplorer;

import java.util.Set;

import org.pentaho.di.repository.pur.model.IRole;
import org.pentaho.di.ui.repository.repositoryexplorer.model.IUIUser;

public interface IUIRole extends Comparable<IUIRole> {

  public void setName( String name );

  public String getName();

  public String getDescription();

  public void setDescription( String description );

  public void setUsers( Set<IUIUser> users );

  public Set<IUIUser> getUsers();

  public boolean addUser( IUIUser user );

  public boolean removeUser( IUIUser user );

  public void clearUsers();

  public IRole getRole();
}
