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
package org.pentaho.di.job.entries.deleteresultfilenames;

import java.util.Arrays;

import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class JobEntryDeleteResultFilenamesTest extends JobEntryLoadSaveTestSupport<JobEntryDeleteResultFilenames> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Override
  protected Class<JobEntryDeleteResultFilenames> getJobEntryClass() {
    return JobEntryDeleteResultFilenames.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList(
        "foldername",
        "specifywildcard",
        "wildcard",
        "wildcardexclude" );
  }

  @Override
  protected Map<String, String> createGettersMap() {
    return toMap(
        "foldername", "getFoldername",
        "specifywildcard", "isSpecifyWildcard",
        "wildcard", "getWildcard",
        "wildcardexclude", "getWildcardExclude" );
  }

  @Override
  protected Map<String, String> createSettersMap() {
    return toMap(
        "foldername", "setFoldername",
        "specifywildcard", "setSpecifyWildcard",
        "wildcard", "setWildcard",
        "wildcardexclude", "setWildcardExclude" );
  }
}
