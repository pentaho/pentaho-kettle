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

package org.pentaho.di.trans.steps.mapping;

public class MappingValueRename implements Cloneable {
  private String sourceValueName;
  private String targetValueName;

  /**
   * @param sourceValueName
   * @param targetValueName
   */
  public MappingValueRename( String sourceValueName, String targetValueName ) {
    super();
    this.sourceValueName = sourceValueName;
    this.targetValueName = targetValueName;
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    // TODO Auto-generated method stub
    return super.clone();
  }

  @Override
  public String toString() {
    return sourceValueName + "-->" + targetValueName;
  }

  @Override
  public boolean equals( Object obj ) {
    return sourceValueName.equals( obj );
  }

  @Override
  public int hashCode() {
    return sourceValueName.hashCode();
  }

  /**
   * @return the sourceValueName
   */
  public String getSourceValueName() {
    return sourceValueName;
  }

  /**
   * @param sourceValueName
   *          the sourceValueName to set. If null set to empty String
   */
  public void setSourceValueName( String sourceValueName ) {
    this.sourceValueName = sourceValueName == null ? "" : sourceValueName;
  }

  /**
   * @return the targetValueName
   */
  public String getTargetValueName() {
    return targetValueName;
  }

  /**
   * @param targetValueName
   *          the targetValueName to set. If null set to empty String.
   */
  public void setTargetValueName( String targetValueName ) {
    this.targetValueName = targetValueName == null ? "" : targetValueName;
  }
}
