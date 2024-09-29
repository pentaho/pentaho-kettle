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


package org.pentaho.di.job.entries.folderisempty;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class JobEntryFolderIsEmptyLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryFolderIsEmpty> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Override
  protected Class<JobEntryFolderIsEmpty> getJobEntryClass() {
    return JobEntryFolderIsEmpty.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList( "foldername", "includeSubfolders", "specifywildcard", "wildcard" );
  }

  @Override
  protected Map<String, String> createGettersMap() {
    return toMap(
        "foldername", "getFoldername",
        "includeSubfolders", "isIncludeSubFolders",
        "specifywildcard", "isSpecifyWildcard",
        "wildcard", "getWildcard" );
  }

  @Override
  protected Map<String, String> createSettersMap() {
    return toMap(
        "foldername", "setFoldername",
        "includeSubfolders", "setIncludeSubFolders",
        "specifywildcard", "setSpecifyWildcard",
        "wildcard", "setWildcard" );
  }
}
