package org.pentaho.di.job.entries.abort;

import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JobEntryAbortLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryAbort> {
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