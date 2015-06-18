/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.utils.TestUtils;

public class JobEntryFTPTest {
  private Job job;
  private JobEntryFTP entry;
  private String existingDir;

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

}
