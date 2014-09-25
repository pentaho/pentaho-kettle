package org.pentaho.di.job.entries.createfolder;

import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;

import java.util.Arrays;
import java.util.List;

public class JobEntryCreateFolderLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryCreateFolder> {
  @Override protected Class<JobEntryCreateFolder> getJobEntryClass() {
    return JobEntryCreateFolder.class;
  }

  @Override protected List<String> listCommonAttributes() {
    return Arrays.asList( "foldername", "failOfFolderExists" );
  }
}