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


package org.pentaho.di.job.entries.evalfilesmetrics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.ClassRule;
import org.pentaho.di.job.entries.simpleeval.JobEntrySimpleEval;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class JobEntryEvalFilesMetricsLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryEvalFilesMetrics> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Override
  protected Class<JobEntryEvalFilesMetrics> getJobEntryClass() {
    return JobEntryEvalFilesMetrics.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList( new String[] { "resultFilenamesWildcard", "resultFieldFile", "resultFieldWildcard",
      "resultFieldIncludeSubfolders", "sourceFileFolder", "sourceWildcard", "sourceIncludeSubfolders",
      "compareValue", "minValue", "maxValue", "successConditionType", "sourceFiles", "evaluationType", "scale" } );
  }

  @Override
  protected Map<String, FieldLoadSaveValidator<?>> createAttributeValidatorsMap() {
    Map<String, FieldLoadSaveValidator<?>> validators = new HashMap<String, FieldLoadSaveValidator<?>>();
    int sourceFileCount = new Random().nextInt( 50 ) + 1;
    validators.put( "sourceFileFolder", new ArrayLoadSaveValidator<String>(
      new StringLoadSaveValidator(), sourceFileCount ) );
    validators.put( "sourceWildcard", new ArrayLoadSaveValidator<String>(
      new StringLoadSaveValidator(), sourceFileCount ) );
    validators.put( "sourceIncludeSubfolders", new ArrayLoadSaveValidator<String>(
      new StringLoadSaveValidator(), sourceFileCount ) );
    validators.put( "successConditionType",
      new IntLoadSaveValidator( JobEntrySimpleEval.successNumberConditionCode.length ) );
    validators.put( "sourceFiles",
      new IntLoadSaveValidator( JobEntryEvalFilesMetrics.SourceFilesCodes.length ) );
    validators.put( "evaluationType",
      new IntLoadSaveValidator( JobEntryEvalFilesMetrics.EvaluationTypeCodes.length ) );
    validators.put( "scale",
      new IntLoadSaveValidator( JobEntryEvalFilesMetrics.scaleCodes.length ) );

    return validators;
  }
}
