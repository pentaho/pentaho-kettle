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
