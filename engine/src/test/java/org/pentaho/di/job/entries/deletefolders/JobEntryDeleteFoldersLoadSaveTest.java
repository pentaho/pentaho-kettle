/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.job.entries.deletefolders;

import org.junit.ClassRule;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class JobEntryDeleteFoldersLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryDeleteFolders> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
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
