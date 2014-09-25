package org.pentaho.di.job.entries.delay;

import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;

import java.util.Arrays;
import java.util.List;

public class JobEntryDelayLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryDelay> {
  @Override protected Class<JobEntryDelay> getJobEntryClass() {
    return JobEntryDelay.class;
  }

  @Override protected List<String> listCommonAttributes() {
    return Arrays.asList( "maximumTimeout", "scaleTime" );
  }
}