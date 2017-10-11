/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.repository;

public class StringObjectId implements ObjectId, Comparable<StringObjectId> {
  private String id;

  public StringObjectId( String id ) {
    this.id = id;
  }

  public StringObjectId( ObjectId objectId ) {
    if ( objectId instanceof StringObjectId ) {
      this.id = ( (StringObjectId) objectId ).id;
    } else {
      this.id = objectId.getId();
    }
  }

  @Override
  public boolean equals( Object obj ) {
    if ( this == obj ) {
      return true;
    }

    if ( obj == null ) {
      return false;
    }

    ObjectId objectId = (ObjectId) obj;

    return id.equalsIgnoreCase( objectId.getId() );
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public int compareTo( StringObjectId o ) {
    return id.compareTo( o.id );
  }

  @Override
  public String toString() {
    return id;
  }

  /**
   * @return the id
   */
  @Override
  public String getId() {
    return id;
  }
}
