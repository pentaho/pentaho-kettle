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

package org.pentaho.di.job.entries.eval;

import org.junit.ClassRule;
import org.pentaho.di.core.Const;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class JobEntryEvalLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryEval> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

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
      String lineTerminator = Const.isWindows() ? "\n" : Const.CR;
      StringBuilder text = new StringBuilder();
      int lines = new Random().nextInt( 10 ) + 1;
      for ( int i = 0; i < lines; i++ ) {
        text.append( super.getTestObject() );
        if ( i + 1 < lines ) {
          text.append( lineTerminator );
        }
      }
      return text.toString();
    }

  }
}
