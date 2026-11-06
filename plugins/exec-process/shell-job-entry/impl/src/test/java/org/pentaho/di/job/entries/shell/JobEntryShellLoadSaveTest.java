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


package org.pentaho.di.job.entries.shell;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.validator.EnumLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;

public class JobEntryShellLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryShell> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

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
