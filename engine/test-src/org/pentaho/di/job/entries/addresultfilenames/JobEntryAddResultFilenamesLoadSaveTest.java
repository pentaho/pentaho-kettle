package org.pentaho.di.job.entries.addresultfilenames;

import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;

import java.util.Arrays;
import java.util.List;

public class JobEntryAddResultFilenamesLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryAddResultFilenames> {
  @Override protected Class<JobEntryAddResultFilenames> getJobEntryClass() {
    return JobEntryAddResultFilenames.class;
  }

  @Override protected List<String> listCommonAttributes() {
    return Arrays.asList( "argFromPrevious", "includeSubfolders", "deleteallbefore" );
  }
}