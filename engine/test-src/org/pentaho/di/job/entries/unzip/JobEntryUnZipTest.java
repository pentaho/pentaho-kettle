/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
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
package org.pentaho.di.job.entries.unzip;

import static java.util.Arrays.asList;

import java.util.List;
import java.util.Map;

import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;

public class JobEntryUnZipTest extends JobEntryLoadSaveTestSupport<JobEntryUnZip> {

  @Override
  protected Class<JobEntryUnZip> getJobEntryClass() {
    return JobEntryUnZip.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return asList(
        "zipfilename",
        "wildcard",
        "wildcardexclude",
        "targetdirectory",
        "movetodirectory",
        "addfiletoresult",
        "isfromprevious",
        "adddate",
        "addtime",
        "addOriginalTimestamp",
        "SpecifyFormat",
        "date_time_format",
        "rootzip",
        "createfolder",
        "nr_limit",
        "wildcardSource",
        "success_condition",
        "create_move_to_directory",
        "setOriginalModificationDate" );
  }

  @Override
  protected Map<String, String> createGettersMap() {
    return toMap(
        "zipfilename", "getZipFilename",
        "wildcard", "getWildcard",
        "wildcardexclude", "getWildcardExclude",
        "targetdirectory", "getSourceDirectory",
        "movetodirectory", "getMoveToDirectory",
        "addfiletoresult", "isAddFileToResult",
        "isfromprevious", "getDatafromprevious",
        "adddate", "isDateInFilename",
        "addtime", "isTimeInFilename",
        "addOriginalTimestamp", "isOriginalTimestamp",
        "SpecifyFormat", "isSpecifyFormat",
        "date_time_format", "getDateTimeFormat",
        "rootzip", "isCreateRootFolder",
        "createfolder",  "isCreateFolder",
        "nr_limit", "getLimit",
        "wildcardSource", "getWildcardSource",
        "success_condition", "getSuccessCondition",
        "create_move_to_directory", "isCreateMoveToDirectory",
        "setOriginalModificationDate", "isOriginalModificationDate" );
  }

  @Override
  protected Map<String, String> createSettersMap() {
    return toMap(
        "zipfilename", "setZipFilename",
        "wildcard", "setWildcard",
        "wildcardexclude", "setWildcardExclude",
        "targetdirectory", "setSourceDirectory",
        "movetodirectory", "setMoveToDirectory",
        "addfiletoresult", "setAddFileToResult",
        "isfromprevious", "setDatafromprevious",
        "adddate", "setDateInFilename",
        "addtime", "setTimeInFilename",
        "addOriginalTimestamp", "setAddOriginalTimestamp",
        "SpecifyFormat", "setSpecifyFormat",
        "date_time_format", "setDateTimeFormat",
        "rootzip", "setCreateRootFolder",
        "createfolder",  "setCreateFolder",
        "nr_limit", "setLimit",
        "wildcardSource", "setWildcardSource",
        "success_condition", "setSuccessCondition",
        "create_move_to_directory", "setCreateMoveToDirectory",
        "setOriginalModificationDate", "setOriginalModificationDate" );
  }

}
