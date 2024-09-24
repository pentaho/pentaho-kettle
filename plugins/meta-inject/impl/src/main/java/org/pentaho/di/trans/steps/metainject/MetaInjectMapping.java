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
