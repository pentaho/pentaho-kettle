/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.core.namedconfig.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Configuration implements Serializable {

  private String name;
  private List<Group> groups;

  public Configuration( String name ) {
    this.name = name;
    this.groups = new ArrayList();
  }

  public void setProperty( String groupName, String propertyName, Object propertyValue ) {
    Group group = null;
    if ( containsGroup( groupName ) ) {
      group = getGroup( groupName );
    } else {
      group = new Group( groupName );
      groups.add( group );
    }
    group.addProperty( propertyName, propertyValue );
  }

  public void deleteProperty( String groupName, String propertyName ) {
    if ( containsGroup( groupName ) ) {
      Group group = getGroup( groupName );
      group.deleteProperty( propertyName );
    }
  }

  public String getName() {
    return name;
  }

  public Group getGroup( String name ) {
    for ( Group group : groups ) {
      if ( group.getName().equals( name ) ) {
        return group;
      }
    }
    return null;
  }

  public boolean containsGroup( String name ) {
    Group group = getGroup( name );
    return group != null;
  }

  public void addGroup( String name ) {
    if ( !containsGroup( name ) ) {
      groups.add( new Group( name ) );
    }
  }

  public void deleteGroup( String name ) {
    if ( containsGroup( name ) ) {
      Group group = getGroup( name );
      groups.remove( group );
    }
  }
}
