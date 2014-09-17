package org.pentaho.di.job.entries.deletefiles;

import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;

import java.util.Arrays;
import java.util.List;

public class JobEntryDeleteFilesLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryDeleteFiles> {
  @Override protected Class<JobEntryDeleteFiles> getJobEntryClass() {
    return JobEntryDeleteFiles.class;
  }

  @Override protected List<String> listCommonAttributes() {
    return Arrays.asList( "argFromPrevious", "includeSubfolders" );
  }
}