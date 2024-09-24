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
package org.pentaho.di.job.entries.copymoveresultfilenames;

import java.util.Arrays;

import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class JobEntryCopyMoveResultFilenamesTest extends JobEntryLoadSaveTestSupport<JobEntryCopyMoveResultFilenames> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Override
  protected Class<JobEntryCopyMoveResultFilenames> getJobEntryClass() {
    return JobEntryCopyMoveResultFilenames.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList(
        "foldername",
        "specifywildcard",
        "wildcard",
        "wildcardexclude",
        "destination_folder",
        "nr_errors_less_than",
        "success_condition",
        "add_date",
        "add_time",
        "SpecifyFormat",
        "date_time_format",
        "action",
        "AddDateBeforeExtension",
        "OverwriteFile",
        "CreateDestinationFolder",
        "RemovedSourceFilename",
        "AddDestinationFilename" );
  }

  @Override
  protected Map<String, String> createGettersMap() {
    return toMap(
        "foldername", "getFoldername",
        "specifywildcard", "isSpecifyWildcard",
        "wildcard", "getWildcard",
        "wildcardexclude", "getWildcardExclude",
        "destination_folder", "getDestinationFolder",
        "nr_errors_less_than",  "getNrErrorsLessThan",
        "success_condition", "getSuccessCondition",
        "add_date", "isAddDate",
        "add_time", "isAddTime",
        "SpecifyFormat", "isSpecifyFormat",
        "date_time_format", "getDateTimeFormat",
        "action", "getAction",
        "AddDateBeforeExtension", "isAddDateBeforeExtension",
        "OverwriteFile", "isOverwriteFile",
        "CreateDestinationFolder", "isCreateDestinationFolder",
        "RemovedSourceFilename", "isRemovedSourceFilename",
        "AddDestinationFilename", "isAddDestinationFilename" );
  }

  @Override
  protected Map<String, String> createSettersMap() {
    return toMap(
        "foldername", "setFoldername",
        "specifywildcard", "setSpecifyWildcard",
        "wildcard", "setWildcard",
        "wildcardexclude", "setWildcardExclude",
        "destination_folder", "setDestinationFolder",
        "nr_errors_less_than",  "setNrErrorsLessThan",
        "success_condition", "setSuccessCondition",
        "add_date", "setAddDate",
        "add_time", "setAddTime",
        "SpecifyFormat", "setSpecifyFormat",
        "date_time_format", "setDateTimeFormat",
        "action", "setAction",
        "AddDateBeforeExtension", "setAddDateBeforeExtension",
        "OverwriteFile", "setOverwriteFile",
        "CreateDestinationFolder", "setCreateDestinationFolder",
        "RemovedSourceFilename", "setRemovedSourceFilename",
        "AddDestinationFilename", "setAddDestinationFilename" );
  }

}
