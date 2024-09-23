/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2021-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.job.entries.http;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

public class JobEntryHTTP_PDI_19104_Test {

  @ClassRule
  public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @BeforeClass
  public static void setupBeforeClass() throws KettleException, IOException {
    KettleClientEnvironment.init();
  }

  @Test
  public void testHTTPResultDefaultRows() throws IOException {
    File localSourceFile = getInputFile( "sourceFile", ".tmp" );
    File localTargetFile = File.createTempFile( "targetFile", ".tmp" );
    localSourceFile.deleteOnExit();
    localTargetFile.deleteOnExit();

    Object[] r = new Object[] {
      "file://" + localSourceFile.toURI().toURL().getFile(),
      null,
      localTargetFile.getCanonicalPath() };

    RowMeta rowMetaDefault = new RowMeta();
    rowMetaDefault.addValueMeta( new ValueMetaString( "URL" ) );
    rowMetaDefault.addValueMeta( new ValueMetaString( "UPLOAD" ) );
    rowMetaDefault.addValueMeta( new ValueMetaString( "DESTINATION" ) );
    List<RowMetaAndData> rows = new ArrayList<RowMetaAndData>();
    rows.add( new RowMetaAndData( rowMetaDefault, r ) );
    Result previousResult = new Result();
    previousResult.setRows( rows );

    JobEntryHTTP http = new JobEntryHTTP();
    http.setParentJob( new Job() );
    http.setParentJobMeta( new JobMeta() );
    http.setRunForEveryRow( true );
    http.setAddFilenameToResult( false );
    http.execute( previousResult, 0 );
    assertTrue( FileUtils.contentEquals( localSourceFile, localTargetFile ) );
  }

  private File getInputFile( String prefix, String suffix ) throws IOException {
    File inputFile = File.createTempFile( prefix, suffix );
    FileUtils.writeStringToFile( inputFile, UUID.randomUUID().toString(), "UTF-8" );
    return inputFile;
  }
}
