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
