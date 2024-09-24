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
package org.pentaho.di.job.entries.abort;

import org.junit.ClassRule;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JobEntryAbortLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryAbort> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  @Override
  protected Class<JobEntryAbort> getJobEntryClass() {
    return JobEntryAbort.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Collections.singletonList( "message" );
  }

  @Override
  protected Map<String, String> createGettersMap() {
    return Collections.singletonMap( "message", "getMessageabort" );
  }

  @Override
  protected Map<String, String> createSettersMap() {
    return Collections.singletonMap( "message", "setMessageabort" );
  }
}
