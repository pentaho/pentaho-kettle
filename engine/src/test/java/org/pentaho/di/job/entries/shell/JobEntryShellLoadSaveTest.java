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

package org.pentaho.di.job.entries.shell;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.trans.steps.loadsave.validator.EnumLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;

public class JobEntryShellLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryShell> {

  @Override
  protected Class<JobEntryShell> getJobEntryClass() {
    return JobEntryShell.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList( new String[] { "filename",
      "workDirectory",
      "argFromPrevious",
      "execPerRow",
      "setLogfile",
      "logfile",
      "setAppendLogfile",
      "logext",
      "addDate",
      "addTime",
      "insertScript",
      "script",
      "logFileLevel",
      "arguments" } );
  }

  @Override
  protected Map<String, FieldLoadSaveValidator<?>> createTypeValidatorsMap() {
    Map<String, FieldLoadSaveValidator<?>> validators = new HashMap<String, FieldLoadSaveValidator<?>>();
    validators.put( LogLevel.class.getName(), new EnumLoadSaveValidator<LogLevel>( LogLevel.class ) );
    return validators;
  }

}
