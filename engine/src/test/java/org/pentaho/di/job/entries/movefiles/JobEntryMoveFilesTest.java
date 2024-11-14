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

package org.pentaho.di.job.entries.movefiles;

import java.util.Arrays;

import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class JobEntryMoveFilesTest extends JobEntryLoadSaveTestSupport<JobEntryMoveFiles> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Override
  protected Class<JobEntryMoveFiles> getJobEntryClass() {
    return JobEntryMoveFiles.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList(
        "add_date",
        "add_time",
        "SpecifyFormat",
        "date_time_format",
        "AddDateBeforeExtension",
        "DoNotKeepFolderStructure",
        "iffileexists",
        "destinationFolder",
        "ifmovedfileexists",
        "moved_date_time_format",
        "AddMovedDateBeforeExtension",
        "add_moved_date",
        "add_moved_time",
        "SpecifyMoveFormat",
        "create_move_to_folder",
        "simulate" );
  }

  @Override
  protected Map<String, String> createGettersMap() {
    return toMap(
        "add_date", "isAddDate",
        "add_time", "isAddTime",
        "SpecifyFormat", "isSpecifyFormat",
        "date_time_format", "getDateTimeFormat",
        "AddDateBeforeExtension", "isAddDateBeforeExtension",
        "DoNotKeepFolderStructure", "isDoNotKeepFolderStructure",
        "iffileexists", "getIfFileExists",
        "destinationFolder", "getDestinationFolder",
        "ifmovedfileexists", "getIfMovedFileExists",
        "moved_date_time_format", "getMovedDateTimeFormat",
        "AddMovedDateBeforeExtension", "isAddMovedDateBeforeExtension",
        "add_moved_date", "isAddMovedDate",
        "add_moved_time", "isAddMovedTime",
        "SpecifyMoveFormat", "isSpecifyMoveFormat" );
  }

  @Override
  protected Map<String, String> createSettersMap() {
    return toMap(
        "add_date", "setAddDate",
        "add_time", "setAddTime",
        "SpecifyFormat", "setSpecifyFormat",
        "date_time_format", "setDateTimeFormat",
        "AddDateBeforeExtension", "setAddDateBeforeExtension",
        "DoNotKeepFolderStructure", "setDoNotKeepFolderStructure",
        "iffileexists", "setIfFileExists",
        "destinationFolder", "setDestinationFolder",
        "ifmovedfileexists", "setIfMovedFileExists",
        "moved_date_time_format", "setMovedDateTimeFormat",
        "AddMovedDateBeforeExtension", "setAddMovedDateBeforeExtension",
        "add_moved_date", "setAddMovedDate",
        "add_moved_time", "setAddMovedTime",
        "SpecifyMoveFormat", "setSpecifyMoveFormat",
        "simulate", "setSimulate" );
  }
}
