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
