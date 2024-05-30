/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2022 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.trans.steps.orabulkloader;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * User: Dzmitry Stsiapanau Date: 4/8/14 Time: 1:44 PM
 */
public class OraBulkLoaderTest {
  private static final String expectedDataContents1 = "OPTIONS(" + Const.CR + "  ERRORS='null'" + Const.CR + "  , "
    + "ROWS='null'" + Const.CR + ")" + Const.CR + "LOAD DATA" + Const.CR + "INFILE '";
  private static final String expectedDataContents2 = "'" + Const.CR + "INTO TABLE null" + Const.CR + "null"
    + Const.CR + "FIELDS TERMINATED BY ',' " + "ENCLOSED BY '\"'" + Const.CR + "TRAILING NULLCOLS"
    + Const.CR + "(null, " + Const.CR + "null CHAR)";
  private StepMockHelper<OraBulkLoaderMeta, OraBulkLoaderData> stepMockHelper;
  private OraBulkLoader oraBulkLoader;
  private File tempControlFile;
  private File tempDataFile;
  private String tempControlFilepath;
  private String tempDataFilepath;
  private String tempControlVfsFilepath;
  private String tempDataVfsFilepath;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @BeforeClass
  public static void setupBeforeClass() throws KettleException {
    KettleClientEnvironment.init();
  }

  @Before
  public void setUp() throws Exception {
    stepMockHelper = new StepMockHelper<>( "TEST_CREATE_COMMANDLINE",
      OraBulkLoaderMeta.class, OraBulkLoaderData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
      stepMockHelper.logChannelInterface );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
    oraBulkLoader = spy( new OraBulkLoader( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0,
      stepMockHelper.transMeta, stepMockHelper.trans ) );

    String tmpDir = System.getProperty("java.io.tmpdir");
    tempControlFile = File.createTempFile("control", "test" );
    tempControlFile.deleteOnExit();
    tempDataFile = File.createTempFile("data", "test" );
    tempDataFile.deleteOnExit();
    tempControlFilepath = tempControlFile.getAbsolutePath();
    tempDataFilepath = tempDataFile.getAbsolutePath();
    tempControlVfsFilepath = "file:///" + tempControlFilepath;
    tempDataVfsFilepath = "file:///" + tempDataFilepath;
  }

  @After
  public void cleanUp() {
    stepMockHelper.cleanUp();
  }

  @Test
  public void testGetControlFileContents() throws Exception {
    String[] streamFields = { "id", "name" };
    String[] streamTable = { "id", "name" };
    String[] dateMask = { "", "" };
    DatabaseMeta databaseMeta = mock( DatabaseMeta.class );
    Object[] rowData = { 1, "rowdata", new Date() };
    RowMetaInterface rowMetaInterface = mock( RowMetaInterface.class );
    TransMeta transMeta = spy( new TransMeta( ) );
    ValueMetaInterface idVmi = new ValueMetaNumber( "id" );
    ValueMetaInterface nameVmi = new ValueMetaString( "name", 20, -1 );

    OraBulkLoaderMeta oraBulkLoaderMeta = spy( new OraBulkLoaderMeta() );
    oraBulkLoaderMeta.setDatabaseMeta( databaseMeta );
    oraBulkLoaderMeta.setControlFile( tempControlVfsFilepath );
    oraBulkLoaderMeta.setDataFile( tempDataVfsFilepath );

    doReturn( transMeta ).when( oraBulkLoader ).getTransMeta();
    doReturn( streamFields ).when( oraBulkLoaderMeta ).getFieldStream();
    doReturn( streamTable ).when( oraBulkLoaderMeta ).getFieldTable();
    doReturn( dateMask ).when( oraBulkLoaderMeta ).getDateMask();
    doReturn( 0 ).when( rowMetaInterface ).indexOfValue( "id" );
    doReturn( idVmi ).when( rowMetaInterface ).getValueMeta( 0 );
    doReturn( 1 ).when( rowMetaInterface ).indexOfValue( "name" );
    doReturn( nameVmi ).when( rowMetaInterface ).getValueMeta( 1 );

    String expectedDataContents = expectedDataContents1 + tempDataFilepath + expectedDataContents2;
    String actualDataContents = oraBulkLoader.getControlFileContents( oraBulkLoaderMeta, rowMetaInterface, rowData );
    assertEquals( "The Expected Control File Contents do not match Actual Contents", expectedDataContents,
      actualDataContents );
  }

  @Test
  public void testCreateControlFile() throws Exception {
    // Create a tempfile, so we can use the temp file path when we run the createControlFile method
    String tempTrueControlFilepath = tempControlFile.getAbsolutePath() + "A.txt";
    String expectedControlContents = "test";
    OraBulkLoaderMeta oraBulkLoaderMeta = mock( OraBulkLoaderMeta.class );
    RowMetaInterface rowMetaInterface = mock( RowMetaInterface.class );
    Object[] objectRow = {};

    doReturn( rowMetaInterface ).when( oraBulkLoader ).getInputRowMeta();
    doReturn( expectedControlContents ).when( oraBulkLoader ).getControlFileContents( oraBulkLoaderMeta, rowMetaInterface, objectRow );
    oraBulkLoader.createControlFile( tempTrueControlFilepath,  objectRow, oraBulkLoaderMeta );

    assertTrue( Files.exists( Paths.get( tempTrueControlFilepath ) ) );

    File tempTrueControlFile = new File( tempTrueControlFilepath );
    String tempTrueControlFileContents = new String( Files.readAllBytes( tempTrueControlFile.toPath() ) );
    assertEquals( expectedControlContents, tempTrueControlFileContents );
    tempTrueControlFile.delete();
  }

  @Test
  public void testCreateCommandLine() throws Exception {
    File tmp = File.createTempFile( "testCreateCOmmandLine", "tmp" );
    tmp.deleteOnExit();
    OraBulkLoaderMeta meta = new OraBulkLoaderMeta();
    meta.setSqlldr( tmp.getAbsolutePath() );
    meta.setControlFile( tmp.getAbsolutePath() );
    DatabaseMeta dm = mock( DatabaseMeta.class );
    when( dm.getUsername() ).thenReturn( "user" );
    when( dm.getPassword() ).thenReturn( "Encrypted 2be98afc86aa7f2e4cb298b5eeab387f5" );
    meta.setDatabaseMeta( dm );
    String cmd = oraBulkLoader.createCommandLine( meta, true );
    String expected = tmp.getAbsolutePath() + " control='" + tmp.getAbsolutePath() + "' userid=user/PENTAHO@";
    assertEquals( "Comandline for oracle bulkloader is not as expected", expected, cmd );
  }

  @Test
  public void testDispose() throws Exception {
    TransMeta transMeta = spy( new TransMeta( ) );
    OraBulkLoaderData oraBulkLoaderData = new OraBulkLoaderData();
    OraBulkLoaderMeta oraBulkLoaderMeta = new OraBulkLoaderMeta();
    oraBulkLoaderMeta.setDataFile( tempDataVfsFilepath );
    oraBulkLoaderMeta.setControlFile( tempControlVfsFilepath );
    oraBulkLoaderMeta.setEraseFiles( true );
    oraBulkLoaderMeta.setLoadMethod( "AUTO_END" );

    assertTrue( Files.exists( Paths.get( tempControlFilepath ) ) );
    assertTrue( Files.exists( Paths.get( tempDataFilepath ) ) );

    doReturn( transMeta ).when( oraBulkLoader ).getTransMeta();
    oraBulkLoader.dispose( oraBulkLoaderMeta, oraBulkLoaderData );

    assertFalse( Files.exists( Paths.get( tempControlFilepath ) ) );
    assertFalse( Files.exists( Paths.get( tempDataFilepath ) ) );
  }

  @Test
  public void testNoDatabaseConnection() {
    assertFalse( oraBulkLoader.init( stepMockHelper.initStepMetaInterface, stepMockHelper.initStepDataInterface ) );

    try {
      // Verify that the database connection being set to null throws a KettleException with the following message.
      oraBulkLoader.verifyDatabaseConnection();
      // If the method does not throw a Kettle Exception, then the DB was set and not null for this test. Fail it.
      fail( "Database Connection is not null, this fails the test." );
    } catch ( KettleException aKettleException ) {
      assertThat( aKettleException.getMessage(), containsString( "There is no connection defined in this step." ) );
    }
  }
}
