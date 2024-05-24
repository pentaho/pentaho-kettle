/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

import java.lang.reflect.Method;
import java.util.Arrays;

import java.util.List;
import java.util.Map;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class JobEntryUnZipTest extends JobEntryLoadSaveTestSupport<JobEntryUnZip> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Override
  protected Class<JobEntryUnZip> getJobEntryClass() {
    return JobEntryUnZip.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList(
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


  @Test
  public void unzipPostProcessingTest() throws Exception {

    JobEntryUnZip jobEntryUnZip = new JobEntryUnZip();

    Method unzipPostprocessingMethod = jobEntryUnZip.getClass().getDeclaredMethod( "doUnzipPostProcessing", FileObject.class, FileObject.class, String.class );
    unzipPostprocessingMethod.setAccessible( true );
    FileObject sourceFileObject = Mockito.mock( FileObject.class );
    Mockito.doReturn( Mockito.mock( FileName.class ) ).when( sourceFileObject ).getName();

    //delete
    jobEntryUnZip.afterunzip = 1;
    unzipPostprocessingMethod.invoke( jobEntryUnZip, sourceFileObject, Mockito.mock( FileObject.class ), "" );
    Mockito.verify( sourceFileObject, Mockito.times( 1 ) ).delete();

    //move
    jobEntryUnZip.afterunzip = 2;
    unzipPostprocessingMethod.invoke( jobEntryUnZip, sourceFileObject, Mockito.mock( FileObject.class ), "" );
    Mockito.verify( sourceFileObject, Mockito.times( 1 ) ).moveTo( Mockito.any() );
  }

}
