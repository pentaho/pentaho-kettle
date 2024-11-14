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


package org.pentaho.di.job.entries.sftpput;

import org.junit.ClassRule;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobEntrySFTPPUTLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntrySFTPPUT> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Override
  protected Class<JobEntrySFTPPUT> getJobEntryClass() {
    return JobEntrySFTPPUT.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList( new String[] { "serverName", "serverPort", "userName", "password", "scpDirectory",
      "localDirectory", "wildcard", "copyPrevious", "copyPreviousFiles", "addFilenameResut", "useKeyFile",
      "keyFilename", "keyPassPhrase", "compression", "proxyType", "proxyHost", "proxyPort", "proxyUsername",
      "proxyPassword", "createRemoteFolder", "afterFTPS", "destinationFolder", "createDestinationFolder",
      "successWhenNoFile" } );
  }

  @Override
  protected Map<String, FieldLoadSaveValidator<?>> createAttributeValidatorsMap() {
    Map<String, FieldLoadSaveValidator<?>> validators = new HashMap<String, FieldLoadSaveValidator<?>>();
    validators.put( "afterFTPS", new IntLoadSaveValidator( JobEntrySFTPPUT.afterFTPSCode.length ) );

    return validators;
  }
}
