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

package org.pentaho.di.job.entries.ftp;

import java.util.Arrays;
import java.util.List;

import org.junit.ClassRule;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class JobEntryFTPLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryFTP> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Override
  protected Class<JobEntryFTP> getJobEntryClass() {
    return JobEntryFTP.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList( new String[] { "port",
      "serverName",
      "userName",
      "password",
      "ftpDirectory",
      "targetDirectory",
      "wildcard",
      "binaryMode",
      "timeout",
      "remove",
      "onlyGettingNewFiles",
      "activeConnection",
      "controlEncoding",
      "moveFiles",
      "moveToDirectory",
      "dateInFilename",
      "timeInFilename",
      "specifyFormat",
      "date_time_format",
      "addDateBeforeExtension",
      "addToResult",
      "createMoveFolder",
      "proxyHost",
      "proxyPort",
      "proxyUsername",
      "proxyPassword",
      "socksProxyHost",
      "socksProxyPort",
      "socksProxyUsername",
      "socksProxyPassword",
      "SifFileExists",
      "limit",
      "success_condition" } );
  }

}
