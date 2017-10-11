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
package org.pentaho.di.job.entries.filecompare;

import java.util.Arrays;

import java.util.List;
import java.util.Map;

import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;

public class JobEntryFileCompareTest extends JobEntryLoadSaveTestSupport<JobEntryFileCompare> {

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
