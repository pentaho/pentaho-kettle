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

import org.pentaho.di.core.injection.Injection;

public class MetaInjectMapping {

  @Injection( name = "MAPPING_SOURCE_STEP", group = "MAPPING_FIELDS" )
  private String sourceStep;

  @Injection( name = "MAPPING_SOURCE_FIELD", group = "MAPPING_FIELDS" )
  private String sourceField;

  @Injection( name = "MAPPING_TARGET_STEP", group = "MAPPING_FIELDS" )
  private String targetStep;

  @Injection( name = "MAPPING_TARGET_FIELD", group = "MAPPING_FIELDS" )
  private String targetField;

  public MetaInjectMapping() {
  }

  public String getSourceStep() {
    return sourceStep;
  }

  public void setSourceStep( String sourceStep ) {
    this.sourceStep = sourceStep;
  }

  public String getSourceField() {
    return sourceField;
  }

  public void setSourceField( String sourceField ) {
    this.sourceField = sourceField;
  }

  public String getTargetStep() {
    return targetStep;
  }

  public void setTargetStep( String targetStep ) {
    this.targetStep = targetStep;
  }

  public String getTargetField() {
    return targetField;
  }

  public void setTargetField( String targetField ) {
    this.targetField = targetField;
  }

}
