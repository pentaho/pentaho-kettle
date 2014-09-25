package org.pentaho.di.job.entries.deletefolders;

import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class JobEntryDeleteFoldersLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryDeleteFolders> {
  @Override protected Class<JobEntryDeleteFolders> getJobEntryClass() {
    return JobEntryDeleteFolders.class;
  }

  @Override protected List<String> listCommonAttributes() {
    return Arrays.asList( "argFromPrevious", "success_condition", "limit_folders" );
  }

  @Override protected Map<String, String> createGettersMap() {
    return toMap(
      "success_condition", "getSuccessCondition",
      "limit_folders", "getLimitFolders"
    );
  }

  @Override protected Map<String, String> createSettersMap() {
    return toMap(
      "argFromPrevious", "setPrevious",
      "success_condition", "setSuccessCondition",
      "limit_folders", "setLimitFolders"
    );
  }
}