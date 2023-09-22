/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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
