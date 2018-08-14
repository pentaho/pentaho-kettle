/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.metainject;

public class TargetStepAttribute {

  private String stepname;
  private String attributeKey;
  private boolean detail;

  /**
   * @param stepname
   * @param attributeKey
   */
  public TargetStepAttribute( String stepname, String attributeKey, boolean detail ) {
    this.stepname = stepname;
    this.attributeKey = attributeKey;
    this.detail = detail;
  }

  @Override
  public boolean equals( Object obj ) {
    if ( !( obj instanceof TargetStepAttribute ) ) {
      return false;
    }
    if ( obj == this ) {
      return true;
    }

    TargetStepAttribute target = (TargetStepAttribute) obj;
    return stepname.equalsIgnoreCase( target.getStepname() ) && attributeKey.equals( target.getAttributeKey() );
  }

  @Override
  public int hashCode() {
    return stepname.hashCode() ^ attributeKey.hashCode();
  }

  /**
   * @return the stepname
   */
  public String getStepname() {
    return stepname;
  }

  /**
   * @param stepname
   *          the stepname to set
   */
  public void setStepname( String stepname ) {
    this.stepname = stepname;
  }

  /**
   * @return the attributeKey
   */
  public String getAttributeKey() {
    return attributeKey;
  }

  /**
   * @param attributeKey
   *          the attributeKey to set
   */
  public void setAttributeKey( String attributeKey ) {
    this.attributeKey = attributeKey;
  }

  public void setDetail( boolean detail ) {
    this.detail = detail;
  }

  public boolean isDetail() {
    return detail;
  }

}
