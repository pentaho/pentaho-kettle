/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.core;

import java.util.Comparator;

public class ObjectUsageCount implements Comparator<ObjectUsageCount>, Comparable<ObjectUsageCount> {
  private String objectName;

  private int nrUses;

  /**
   * @param objectName
   * @param nrUses
   */
  public ObjectUsageCount( String objectName, int nrUses ) {
    this.objectName = objectName;
    this.nrUses = nrUses;
  }

  @Override
  public String toString() {
    return objectName + ";" + nrUses;
  }

  public static ObjectUsageCount fromString( String string ) {
    String[] splits = string.split( ";" );
    if ( splits.length >= 2 ) {
      return new ObjectUsageCount( splits[0], Const.toInt( splits[1], 1 ) );
    }
    return new ObjectUsageCount( string, 1 );
  }

  @Override
  public int compare( ObjectUsageCount count1, ObjectUsageCount count2 ) {
    return count1.compareTo( count2 );
  }

  @Override
  public int compareTo( ObjectUsageCount count ) {
    return Integer.valueOf( count.getNrUses() ).compareTo( Integer.valueOf( getNrUses() ) );
  }

  public void reset() {
    nrUses = 0;
  }

  /**
   * Increment the nr of uses with 1
   *
   * @return the nr of uses
   */
  public int increment() {
    nrUses++;
    return nrUses;
  }

  /**
   * @return the nrUses
   */
  public int getNrUses() {
    return nrUses;
  }

  /**
   * @param nrUses
   *          the nrUses to set
   */
  public void setNrUses( int nrUses ) {
    this.nrUses = nrUses;
  }

  /**
   * @return the objectName
   */
  public String getObjectName() {
    return objectName;
  }

  /**
   * @param objectName
   *          the objectName to set
   */
  public void setObjectName( String objectName ) {
    this.objectName = objectName;
  }

}
