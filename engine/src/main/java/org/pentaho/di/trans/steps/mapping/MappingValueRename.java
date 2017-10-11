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
