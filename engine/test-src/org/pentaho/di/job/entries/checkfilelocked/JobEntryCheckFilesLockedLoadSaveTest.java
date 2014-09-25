package org.pentaho.di.job.entries.checkfilelocked;


import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;

import java.util.List;

import static java.util.Arrays.asList;

public class JobEntryCheckFilesLockedLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryCheckFilesLocked> {
  @Override protected Class<JobEntryCheckFilesLocked> getJobEntryClass() {
    return JobEntryCheckFilesLocked.class;
  }

  @Override protected List<String> listCommonAttributes() {
    return asList( "argFromPrevious", "includeSubfolders" );
  }
}