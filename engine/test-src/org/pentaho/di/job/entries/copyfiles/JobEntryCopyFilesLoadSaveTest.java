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