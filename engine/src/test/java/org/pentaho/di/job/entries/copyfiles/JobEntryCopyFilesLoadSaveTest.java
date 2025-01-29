/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.job.entries.copyfiles;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.ClassRule;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class JobEntryCopyFilesLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryCopyFiles> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Override
  protected Class<JobEntryCopyFiles> getJobEntryClass() {
    return JobEntryCopyFiles.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList( "copy_empty_folders", "arg_from_previous", "overwrite_files", "include_subfolders",
      "remove_source_files", "add_result_filesname", "destination_is_a_file", "create_destination_folder",
      "source_filefolder", "destination_filefolder", "wildcard" );
  }

  @Override
  protected Map<String, String> createGettersMap() {
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

  @Override
  protected Map<String, String> createSettersMap() {
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

  @Override
  protected Map<String, FieldLoadSaveValidator<?>> createAttributeValidatorsMap() {
    int fileArraySize = new Random().nextInt( 5 ) + 1;
    Map<String, FieldLoadSaveValidator<?>> attrMap = new HashMap<>();
    attrMap.put( "source_filefolder",
      new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), fileArraySize ) );
    attrMap.put( "destination_filefolder",
      new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), fileArraySize ) );
    attrMap.put( "wildcard", new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), fileArraySize ) );
    return attrMap;
  }

}
