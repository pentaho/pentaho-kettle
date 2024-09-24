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
