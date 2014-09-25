package org.pentaho.di.job.entries.createfile;

import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JobEntryCreateFileLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryCreateFile> {
  @Override protected Class<JobEntryCreateFile> getJobEntryClass() {
    return JobEntryCreateFile.class;
  }

  @Override protected List<String> listCommonAttributes() {
    return Arrays.asList( "filename", "failIfFileExists", "addfilenameresult" );
  }

  @Override protected Map<String, String> createGettersMap() {
    return Collections.singletonMap( "addfilenameresult", "isAddFilenameToResult" );
  }

  @Override protected Map<String, String> createSettersMap() {
    return Collections.singletonMap( "addfilenameresult", "setAddFilenameToResult" );
  }
}