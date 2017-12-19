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

package org.pentaho.di.job.entries.writetolog;

import java.util.Arrays;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.trans.steps.loadsave.validator.EnumLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;

public class JobEntryWriteToLogLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryWriteToLog> {

  @Override
  protected Class<JobEntryWriteToLog> getJobEntryClass() {
    return JobEntryWriteToLog.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList(
      "logmessage",
      "loglevel",
      "logsubject" );
  }

  @Override
  protected Map<String, String> createGettersMap() {
    return toMap(
      "logmessage", "getLogMessage",
      "loglevel", "getEntryLogLevel",
      "logsubject", "getLogSubject" );
  }

  @Override
  protected Map<String, String> createSettersMap() {
    return toMap(
      "logmessage", "setLogMessage",
      "loglevel", "setEntryLogLevel",
      "logsubject", "setLogSubject" );
  }

  @Override
  protected Map<String, FieldLoadSaveValidator<?>> createAttributeValidatorsMap() {
    EnumSet<LogLevel> logLevels = EnumSet.allOf( LogLevel.class );
    LogLevel random = (LogLevel) logLevels.toArray()[new Random().nextInt( logLevels.size() )];
    return toMap( "loglevel", new EnumLoadSaveValidator<LogLevel>( random ) );
  }
}
