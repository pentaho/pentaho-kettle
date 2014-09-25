package org.pentaho.di.job.entries.connectedtorepository;

import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;

import java.util.Arrays;
import java.util.List;

public class JobEntryConnectedToRepositoryLoadSaveTest
  extends JobEntryLoadSaveTestSupport<JobEntryConnectedToRepository> {

  @Override protected Class<JobEntryConnectedToRepository> getJobEntryClass() {
    return JobEntryConnectedToRepository.class;
  }

  @Override protected List<String> listCommonAttributes() {
    return Arrays.asList( "specificRep", "repName", "specificUser", "userName" );
  }
}