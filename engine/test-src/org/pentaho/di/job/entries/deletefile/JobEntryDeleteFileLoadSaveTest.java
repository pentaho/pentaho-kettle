package org.pentaho.di.job.entries.deletefile;

import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;

import java.util.Arrays;
import java.util.List;

public class JobEntryDeleteFileLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryDeleteFile> {
  @Override protected Class<JobEntryDeleteFile> getJobEntryClass() {
    return JobEntryDeleteFile.class;
  }

  @Override protected List<String> listCommonAttributes() {
    return Arrays.asList( "filename", "failIfFileNotExists" );
  }
}