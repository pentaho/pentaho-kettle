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


package org.pentaho.di.trans.steps.avro.output;

import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.plugins.ParentFirst;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;

@Step( id = "AvroOutput", image = "AO.svg", name = "AvroOutput.Name", description = "AvroOutput.Description",
    categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.BigData",
    documentationUrl = "mk-95pdia003/pdi-transformation-steps/avro-output",
    i18nPackageName = "org.pentaho.di.trans.steps.avro.output" )
@InjectionSupported( localizationPrefix = "AvroOutput.Injection.", groups = { "FIELDS" } )
@ParentFirst( patterns = { ".*" } )
public class AvroOutputMeta extends AvroOutputMetaBase {



  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
      Trans trans ) {
    return new AvroOutput( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new AvroOutputData();
  }

}
