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

package org.pentaho.di.job.entries.filecompare;

import java.util.Arrays;

import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class JobEntryFileCompareTest extends JobEntryLoadSaveTestSupport<JobEntryFileCompare> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Override
  protected Class<JobEntryFileCompare> getJobEntryClass() {
    return JobEntryFileCompare.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList(
        "filename1",
        "filename2",
        "addFilenameToResult" );
  }

  @Override
  protected Map<String, String> createGettersMap() {
    return toMap(
        "filename1", "getFilename1",
        "filename2", "getFilename2",
        "addFilenameToResult", "isAddFilenameToResult" );
  }

  @Override
  protected Map<String, String> createSettersMap() {
    return toMap(
        "filename1", "setFilename1",
        "filename2", "setFilename2",
        "addFilenameToResult", "setAddFilenameToResult" );
  }

}
