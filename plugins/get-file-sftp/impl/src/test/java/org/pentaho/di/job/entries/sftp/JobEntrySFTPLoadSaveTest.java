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


package org.pentaho.di.job.entries.sftp;

import org.junit.ClassRule;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

import java.util.Arrays;
import java.util.List;

public class JobEntrySFTPLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntrySFTP> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Override
  protected Class<JobEntrySFTP> getJobEntryClass() {
    return JobEntrySFTP.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList( new String[] { "serverName", "serverPort", "userName", "password", "scpDirectory",
      "targetDirectory", "wildcard", "remove", "addToResult", "createTargetFolder", "copyPrevious",
      "useKeyFile", "keyFilename", "keyPassPhrase", "compression", "proxyType", "proxyHost", "proxyPort",
      "proxyUsername", "proxyPassword" } );
  }

}
