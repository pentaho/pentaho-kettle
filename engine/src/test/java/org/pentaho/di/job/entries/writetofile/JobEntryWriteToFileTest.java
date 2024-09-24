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
package org.pentaho.di.job.entries.writetofile;

import java.util.Arrays;

import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class JobEntryWriteToFileTest extends JobEntryLoadSaveTestSupport<JobEntryWriteToFile> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Override
  protected Class<JobEntryWriteToFile> getJobEntryClass() {
    return JobEntryWriteToFile.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList(
        "filename",
        "createParentFolder",
        "appendFile",
        "content",
        "encoding" );
  }

  @Override
  protected Map<String, String> createGettersMap() {
    return toMap(
        "filename", "getFilename",
        "createParentFolder", "isCreateParentFolder",
        "appendFile",  "isAppendFile",
        "content", "getContent",
        "encoding", "getEncoding" );
  }

  @Override
  protected Map<String, String> createSettersMap() {
    return toMap(
        "filename", "setFilename",
        "createParentFolder", "setCreateParentFolder",
        "appendFile",  "setAppendFile",
        "content", "setContent",
        "encoding", "setEncoding" );
  }

}
