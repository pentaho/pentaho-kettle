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

import org.pentaho.di.repository.ObjectRecipient;

public class RepositoryObjectRecipient implements ObjectRecipient, java.io.Serializable {

  private static final long serialVersionUID = 3948870815049027653L; /* EESOURCE: UPDATE SERIALVERUID */

  @Override
  public boolean equals( Object obj ) {
    if ( obj != null ) {
      RepositoryObjectRecipient recipient = (RepositoryObjectRecipient) obj;
      if ( name == null && type == null && recipient.getName() == null && recipient.getType() == null ) {
        return true;
      } else if ( name != null && type != null ) {
        return name.equals( recipient.getName() ) && type.equals( recipient.getType() );
      } else if ( recipient.getName() != null && recipient.getType() != null ) {
        return recipient.getName().equals( name ) && recipient.getType().equals( type );
      } else if ( recipient.getType() == null && type == null ) {
        return name.equals( recipient.getName() );
      } else if ( recipient.getName() == null && name == null ) {
        return type.equals( recipient.getType() );
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  private String name;

  private Type type;

  // ~ Constructors
  // ====================================================================================================

  public RepositoryObjectRecipient( String name ) {
    this( name, Type.USER );
  }

  public RepositoryObjectRecipient( String name, Type type ) {
    super();
    this.name = name;
    this.type = type;
  }

  // ~ Methods
  // =========================================================================================================

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public Type getType() {
    return type;
  }

  public void setType( Type type ) {
    this.type = type;
  }

  @Override
  public String toString() {
    return name;
  }
}
