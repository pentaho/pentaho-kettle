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

package org.pentaho.di.job.entries.setvariables;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.pentaho.di.job.entries.pgpencryptfiles.JobEntryPGPEncryptFiles;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveIntArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class JobEntrySetVariablesLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntrySetVariables> {

  @Override
  protected Class<JobEntrySetVariables> getJobEntryClass() {
    return JobEntrySetVariables.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList( new String[] { "replaceVars", "filename", "fileVariableType",
      "variableName", "variableValue", "variableType" } );
  }

  @Override
  protected Map<String, FieldLoadSaveValidator<?>> createAttributeValidatorsMap() {
    Map<String, FieldLoadSaveValidator<?>> validators = new HashMap<String, FieldLoadSaveValidator<?>>();
    validators.put( "fileVariableType", new IntLoadSaveValidator( JobEntrySetVariables.variableTypeCode.length ) );

    int count = new Random().nextInt( 50 ) + 1;

    validators.put( "variableName", new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), count ) );
    validators.put( "variableValue", new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), count ) );
    validators.put( "variableType", new PrimitiveIntArrayLoadSaveValidator(
      new IntLoadSaveValidator( JobEntryPGPEncryptFiles.actionTypeCodes.length ), count ) );

    return validators;
  }
}
