/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.job.entries.waitforfile;

import java.util.Arrays;

import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class JobEntryWaitForFileTest extends JobEntryLoadSaveTestSupport<JobEntryWaitForFile> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Override
  protected Class<JobEntryWaitForFile> getJobEntryClass() {
    return JobEntryWaitForFile.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList(
        "filename",
        "maximumTimeout",
        "checkCycleTime",
        "successOnTimeout",
        "fileSizeCheck",
        "addFilenameToResult" );
  }

  @Override
  protected Map<String, String> createGettersMap() {
    return toMap(
        "filename", "getFilename",
        "maximumTimeout", "getMaximumTimeout",
        "checkCycleTime", "getCheckCycleTime",
        "successOnTimeout", "isSuccessOnTimeout",
        "fileSizeCheck", "isFileSizeCheck",
        "addFilenameToResult", "isAddFilenameToResult" );
  }

  @Override
  protected Map<String, String> createSettersMap() {
    return toMap(
        "filename", "setFilename",
        "maximumTimeout", "setMaximumTimeout",
        "checkCycleTime", "setCheckCycleTime",
        "successOnTimeout", "setSuccessOnTimeout",
        "fileSizeCheck", "setFileSizeCheck",
        "addFilenameToResult", "setAddFilenameToResult" );
  }

}
