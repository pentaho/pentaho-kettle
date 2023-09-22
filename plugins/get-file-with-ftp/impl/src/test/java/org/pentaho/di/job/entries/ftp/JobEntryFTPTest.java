/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.job.entries.ftp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.utils.TestUtils;

public class JobEntryFTPTest {
  private Job job;
  private JobEntryFTP entry;
  private String existingDir;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    KettleClientEnvironment.init();
  }

  @Before
  public void setUp() throws Exception {
    job = new Job( null, new JobMeta() );
    entry = new MockedJobEntryFTP();

    job.getJobMeta().addJobEntry( new JobEntryCopy( entry ) );
    entry.setParentJob( job );

    job.setStopped( false );

    entry.setServerName( "some.server" );
    entry.setUserName( "anonymous" );
    entry.setFtpDirectory( "." );
    entry.setWildcard( "robots.txt" );
    entry.setBinaryMode( false );
    entry.setSuccessCondition( "success_if_no_errors" );

    existingDir = TestUtils.createTempDir();
  }

  @After
  public void tearDown() throws Exception {
    File fl = new File( existingDir );
    if ( !fl.exists() ) {
      return;
    }
    File[] fls = fl.listFiles();
    if ( fls == null || fls.length == 0 ) {
      return;
    }
    fls[0].delete();
    fl.delete();
  }

  @Test
  public void testFixedExistingTargetDir() throws Exception {
    entry.setTargetDirectory( existingDir );

    Result result = entry.execute( new Result(), 0 );

    assertTrue( "For existing folder should be true", result.getResult() );
    assertEquals( "There should be no errors", 0, result.getNrErrors() );
  }

  @Test
  public void testFixedNonExistingTargetDir() throws Exception {
    entry.setTargetDirectory( existingDir + File.separator + "sub" );

    Result result = entry.execute( new Result(), 0 );

    assertFalse( "For non existing folder should be false", result.getResult() );
    assertTrue( "There should be errors", 0 != result.getNrErrors() );
  }

  @Test
  public void testVariableExistingTargetDir() throws Exception {
    entry.setTargetDirectory( "${Internal.Job.Filename.Directory}" );
    entry.setVariable( "Internal.Job.Filename.Directory", existingDir );

    Result result = entry.execute( new Result(), 0 );

    assertTrue( "For existing folder should be true", result.getResult() );
    assertEquals( "There should be no errors", 0, result.getNrErrors() );
  }

  @Test
  public void testVariableNonExistingTargetDir() throws Exception {
    entry.setTargetDirectory( "${Internal.Job.Filename.Directory}/Worg" );
    entry.setVariable( "Internal.Job.Filename.Directory", existingDir + File.separator + "sub" );

    Result result = entry.execute( new Result(), 0 );

    assertFalse( "For non existing folder should be false", result.getResult() );
    assertTrue( "There should be errors", 0 != result.getNrErrors() );
  }

  @Test
  public void testProtocolVariableExistingTargetDir() throws Exception {
    entry.setTargetDirectory( "${Internal.Job.Filename.Directory}" );
    entry.setVariable( "Internal.Job.Filename.Directory", "file://" + existingDir );

    Result result = entry.execute( new Result(), 0 );

    assertTrue( "For existing folder should be true", result.getResult() );
    assertEquals( "There should be no errors", 0, result.getNrErrors() );
  }

  @Test
  public void testPtotocolVariableNonExistingTargetDir() throws Exception {
    entry.setTargetDirectory( "${Internal.Job.Filename.Directory}/Worg" );
    entry.setVariable( "Internal.Job.Filename.Directory", "file://" + existingDir + File.separator + "sub" );

    Result result = entry.execute( new Result(), 0 );

    assertFalse( "For non existing folder should be false", result.getResult() );
    assertTrue( "There should be errors", 0 != result.getNrErrors() );
  }

  @Test
  public void testTargetFilenameNoDateTime() throws Exception {
    File destFolder = tempFolder.newFolder( "pdi5558" );
    destFolder.deleteOnExit();
    JobEntryFTP entry = new JobEntryFTP();
    entry.setTargetDirectory( destFolder.getAbsolutePath() );
    entry.setAddDateBeforeExtension( false );

    assertNull( entry.returnTargetFilename( null ) );
    assertEquals( destFolder.getAbsolutePath() + Const.FILE_SEPARATOR + "testFile",
      entry.returnTargetFilename( "testFile" ) );
    assertEquals( destFolder.getAbsolutePath() + Const.FILE_SEPARATOR + "testFile.txt",
      entry.returnTargetFilename( "testFile.txt" ) );
  }

  @Test
  public void testTargetFilenameWithDateTime() throws Exception {
    SimpleDateFormat yyyyMMdd = new SimpleDateFormat( "yyyyMMdd" );
    SimpleDateFormat HHmmssSSS = new SimpleDateFormat( "HHmmssSSS" );
    SimpleDateFormat yyyyMMddHHmmssSSS = new SimpleDateFormat( "yyyyMMdd_HHmmssSSS" );
    File destFolder = tempFolder.newFolder( "pdi5558" );
    destFolder.deleteOnExit();
    String destFolderName = destFolder.getAbsolutePath();
    JobEntryFTP entry = new JobEntryFTP();
    entry.setTargetDirectory( destFolderName );
    entry.setAddDateBeforeExtension( true );

    //Test Date-Only
    entry.setDateInFilename( true );
    assertNull( entry.returnTargetFilename( null ) );
    assertEquals( "Test Add Date without file extension",
      destFolderName + Const.FILE_SEPARATOR + "testFile_" + yyyyMMdd.format( new Date() ),
      entry.returnTargetFilename( "testFile" ) );
    assertEquals( "Test Add Date with file extension",
      destFolderName + Const.FILE_SEPARATOR + "testFile_" + yyyyMMdd.format( new Date() ) + ".txt",
      entry.returnTargetFilename( "testFile.txt" ) );

    //Test Date-and-Time
    entry.setTimeInFilename( true );
    String beforeString = destFolderName + Const.FILE_SEPARATOR + "testFile_" + yyyyMMddHHmmssSSS.format( new Date() ) + ".txt";
    String actualValue = entry.returnTargetFilename( "testFile.txt" );
    String afterString = destFolderName + Const.FILE_SEPARATOR + "testFile_" + yyyyMMddHHmmssSSS.format( new Date() ) + ".txt";

    Pattern expectedFormat = Pattern.compile(
      Pattern.quote( destFolderName + Const.FILE_SEPARATOR + "testFile_" + yyyyMMdd.format( new Date() ) + "_" )
      + "([\\d]{9})\\.txt" );
    assertTrue( "Output file matches expected format", expectedFormat.matcher( actualValue ).matches() );
    assertTrue( "The actual time is not too early for test run", actualValue.compareTo( beforeString ) >= 0 );
    assertTrue( "The actual time is not too late for test run", actualValue.compareTo( afterString ) <= 0 );

    //Test Time-Only
    entry.setDateInFilename( false );
    beforeString = destFolderName + Const.FILE_SEPARATOR + "testFile_" + HHmmssSSS.format( new Date() ) + ".txt";
    actualValue = entry.returnTargetFilename( "testFile.txt" );
    afterString = destFolderName + Const.FILE_SEPARATOR + "testFile_" + HHmmssSSS.format( new Date() ) + ".txt";

    expectedFormat = Pattern.compile(
      Pattern.quote( destFolderName + Const.FILE_SEPARATOR + "testFile_" ) + "([\\d]{9})\\.txt" );
    assertTrue( "Output file matches expected format", expectedFormat.matcher( actualValue ).matches() );
    assertTrue( "The actual time is not too early for test run", actualValue.compareTo( beforeString ) >= 0 );
    assertTrue( "The actual time is not too late for test run", actualValue.compareTo( afterString ) <= 0 );
  }

  @Test
  public void testDirectoryWithSingleFile() throws IOException {
    final File destFolder = tempFolder.newFolder("pdi19431");
    entry.setTargetDirectory(destFolder.getAbsolutePath());
    entry.setFtpDirectory("SingleFile");
    final Result result = entry.execute(new Result(), 0);
    assertEquals("There should be one file", 1, result.getNrFilesRetrieved());
    assertEquals("There should be no errors", 0, result.getNrErrors());
    assertEquals("There should be one file", 1, Objects.requireNonNull(destFolder.list()).length);
    destFolder.deleteOnExit();
  }
}
