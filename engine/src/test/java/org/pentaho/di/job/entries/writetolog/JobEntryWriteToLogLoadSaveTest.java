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


package org.pentaho.di.job.entries.writetolog;

import java.util.Arrays;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.ClassRule;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.validator.EnumLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;

public class JobEntryWriteToLogLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryWriteToLog> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

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
