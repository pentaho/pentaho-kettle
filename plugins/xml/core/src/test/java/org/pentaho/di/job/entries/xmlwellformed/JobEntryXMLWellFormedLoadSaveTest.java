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

package org.pentaho.di.job.entries.xmlwellformed;

import static java.util.Arrays.asList;

import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class JobEntryXMLWellFormedLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryXMLWellFormed> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  @BeforeClass
  public static void setupClass() throws KettleException {
    KettleEnvironment.init();
  }

  @Override
  protected Map<String, FieldLoadSaveValidator<?>> createAttributeValidatorsMap() {
    return toMap( "source_filefolder", new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 50 ),
        "wildcard", new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 50 ) );
  }

  @Override
  protected Class<JobEntryXMLWellFormed> getJobEntryClass() {
    return JobEntryXMLWellFormed.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return asList( "arg_from_previous", "include_subfolders", "nr_errors_less_than", "success_condition",
        "resultfilenames", "source_filefolder", "wildcard" );
  }

  @Override
  protected Map<String, String> createGettersMap() {
    return toMap( "arg_from_previous", "isArgFromPrevious", "include_subfolders", "isIncludeSubfolders",
        "nr_errors_less_than", "getNrErrorsLessThan", "success_condition", "getSuccessCondition", "resultfilenames",
        "getResultFilenames", "source_filefolder", "getSourceFileFolders", "wildcard", "getSourceWildcards" );
  }

  @Override
  protected Map<String, String> createSettersMap() {
    return toMap( "arg_from_previous", "setArgFromPrevious", "include_subfolders", "setIncludeSubfolders",
        "nr_errors_less_than", "setNrErrorsLessThan", "success_condition", "setSuccessCondition", "resultfilenames",
        "setResultFilenames", "source_filefolder", "setSourceFileFolders", "wildcard", "setSourceWildcards" );
  }
}
