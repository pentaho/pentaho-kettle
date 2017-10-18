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

public class LongObjectId implements ObjectId, Comparable<LongObjectId> {
  private long id;

  public LongObjectId( long id ) {
    this.id = id;
  }

  public LongObjectId( ObjectId objectId ) {
    if ( objectId == null ) {
      this.id = -1L; // backward compatible
    } else {
      if ( objectId instanceof LongObjectId ) {
        this.id = ( (LongObjectId) objectId ).longValue();
      } else {
        this.id = Long.valueOf( objectId.getId() );
      }
    }
  }

  @Override
  public boolean equals( Object obj ) {
    if ( this == obj ) {
      return true;
    }
    if ( !( obj instanceof LongObjectId ) ) {
      return false;
    }

    LongObjectId objectId = (LongObjectId) obj;

    return id == objectId.longValue();
  }

  @Override
  public int hashCode() {
    return Long.valueOf( id ).hashCode();
  }

  @Override
  public int compareTo( LongObjectId o ) {
    return Long.valueOf( id ).compareTo( Long.valueOf( o.longValue() ) );
  }

  @Override
  public String toString() {
    return Long.toString( id );
  }

  /**
   * @return the id
   */
  @Override
  public String getId() {
    return Long.toString( id );
  }

  /**
   * @return the id in its original form.
   */
  public Long longValue() {
    return Long.valueOf( id );
  }

}
