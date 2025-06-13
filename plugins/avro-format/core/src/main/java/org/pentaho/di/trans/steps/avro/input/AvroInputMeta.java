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

package org.pentaho.di.trans.steps.avro.input;

import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.plugins.ParentFirst;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;

//keep ID as new because we will have old step with ID AvroInput
@Step( id = "AvroInputNew", image = "AI.svg", name = "AvroInput.Name", description = "AvroInput.Description",
  categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.BigData",
  documentationUrl = "mk-95pdia003/pdi-transformation-steps/avro-input",
  i18nPackageName = "org.pentaho.di.trans.steps.avro.input" )
@InjectionSupported( localizationPrefix = "AvroInput.Injection.", groups = { "FIELDS", "LOOKUP_FIELDS" }, hide = {
  "FIELD_POSITION", "FIELD_LENGTH", "FIELD_IGNORE", "FIELD_FORMAT", "FIELD_PRECISION", "FIELD_CURRENCY",
  "FIELD_DECIMAL", "FIELD_GROUP", "FIELD_REPEAT", "FIELD_TRIM_TYPE"
} )
@ParentFirst( patterns = { ".*" } )
public class AvroInputMeta extends AvroInputMetaBase {


  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
                                Trans trans ) {
    return new AvroInput( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new AvroInputData();
  }
}
