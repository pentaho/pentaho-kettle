/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
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
