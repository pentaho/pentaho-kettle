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

package org.pentaho.di.job.entries.createfile;

import org.junit.ClassRule;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JobEntryCreateFileLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryCreateFile> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  @Override protected Class<JobEntryCreateFile> getJobEntryClass() {
    return JobEntryCreateFile.class;
  }

  @Override protected List<String> listCommonAttributes() {
    return Arrays.asList( "filename", "failIfFileExists", "addfilenameresult" );
  }

  @Override protected Map<String, String> createGettersMap() {
    return Collections.singletonMap( "addfilenameresult", "isAddFilenameToResult" );
  }

  @Override protected Map<String, String> createSettersMap() {
    return Collections.singletonMap( "addfilenameresult", "setAddFilenameToResult" );
  }
}
