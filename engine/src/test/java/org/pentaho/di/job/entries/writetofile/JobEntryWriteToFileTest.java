/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package org.pentaho.di.job.entries.writetofile;

import java.util.Arrays;

import java.util.List;
import java.util.Map;

import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;

public class JobEntryWriteToFileTest extends JobEntryLoadSaveTestSupport<JobEntryWriteToFile> {

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
