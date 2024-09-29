/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2022 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.avro.output;

import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.injection.InjectionSupported;
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
