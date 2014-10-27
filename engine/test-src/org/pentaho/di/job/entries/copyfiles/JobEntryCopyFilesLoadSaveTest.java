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
package org.pentaho.di.job.entries.copyfiles;

import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class JobEntryCopyFilesLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryCopyFiles> {

  @Override protected Class<JobEntryCopyFiles> getJobEntryClass() {
    return JobEntryCopyFiles.class;
  }

  @Override protected List<String> listCommonAttributes() {
    return asList( "copy_empty_folders", "arg_from_previous", "overwrite_files", "include_subfolders",
      "remove_source_files", "add_result_filesname", "destination_is_a_file", "create_destination_folder" );
  }

  @Override protected Map<String, String> createGettersMap() {
    return toMap(
      "copy_empty_folders", "isCopyEmptyFolders",
      "arg_from_previous", "isArgFromPrevious",
      "overwrite_files", "isoverwrite_files",
      "include_subfolders", "isIncludeSubfolders",
      "remove_source_files", "isRemoveSourceFiles",
      "add_result_filesname", "isAddresultfilesname",
      "destination_is_a_file", "isDestinationIsAFile",
      "create_destination_folder", "isCreateDestinationFolder"
    );
  }

  @Override protected Map<String, String> createSettersMap() {
    return toMap(
      "copy_empty_folders", "setCopyEmptyFolders",
      "arg_from_previous", "setArgFromPrevious",
      "overwrite_files", "setoverwrite_files",
      "include_subfolders", "setIncludeSubfolders",
      "remove_source_files", "setRemoveSourceFiles",
      "add_result_filesname", "setAddresultfilesname",
      "destination_is_a_file", "setDestinationIsAFile",
      "create_destination_folder", "setCreateDestinationFolder"
    );
  }
}
