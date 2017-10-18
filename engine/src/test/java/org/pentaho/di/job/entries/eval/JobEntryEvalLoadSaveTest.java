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

package org.pentaho.di.job.entries.eval;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.pentaho.di.core.Const;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class JobEntryEvalLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryEval> {

  @Override
  protected Class<JobEntryEval> getJobEntryClass() {
    return JobEntryEval.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList( new String[] { "script" } );
  }

  @Override
  protected Map<String, FieldLoadSaveValidator<?>> createAttributeValidatorsMap() {
    Map<String, FieldLoadSaveValidator<?>> validators = new HashMap<String, FieldLoadSaveValidator<?>>();
    validators.put( "script", new MultiLineStringFieldLoadSaveValidator() );
    return validators;
  }

  public static class MultiLineStringFieldLoadSaveValidator extends StringLoadSaveValidator {

    @Override
    public String getTestObject() {
      StringBuilder text = new StringBuilder();
      int lines = new Random().nextInt( 10 ) + 1;
      for ( int i = 0; i < lines; i++ ) {
        text.append( super.getTestObject() );
        if ( i + 1 < lines ) {
          text.append( Const.CR );
        }
      }
      return text.toString();
    }

  }
}
