/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

public class SourceStepField {

  private String stepname;
  private String field;

  /**
   * @param stepname
   * @param field
   */
  public SourceStepField(String stepname, String field) {
    this.stepname = stepname;
    this.field = field;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof SourceStepField)) return false;
    if (obj==this) return true;
    
    SourceStepField source = (SourceStepField) obj;
    return stepname.equalsIgnoreCase(source.getStepname()) && field.equals(source.getField());
  }
  
  @Override
  public int hashCode() {
    return stepname.hashCode() ^ field.hashCode();
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
  public void setStepname(String stepname) {
    this.stepname = stepname;
  }

  /**
   * @return the field
   */
  public String getField() {
    return field;
  }

  /**
   * @param field
   *          the field to set
   */
  public void setField(String field) {
    this.field = field;
  }

}
