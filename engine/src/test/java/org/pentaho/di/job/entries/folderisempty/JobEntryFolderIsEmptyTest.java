/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.job.entries.folderisempty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;

public class JobEntryFolderIsEmptyTest {
  private Job job;
  private JobEntryFolderIsEmpty entry;

  private String emptyDir;
  private String nonEmptyDir;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    KettleLogStore.init();
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    job = new Job( null, new JobMeta() );
    entry = new JobEntryFolderIsEmpty();

    job.getJobMeta().addJobEntry( new JobEntryCopy( entry ) );
    entry.setParentJob( job );
    JobMeta mockJobMeta = mock( JobMeta.class );
    entry.setParentJobMeta( mockJobMeta );

    job.setStopped( false );

    File dir = Files.createTempDirectory( "dir", new FileAttribute<?>[0] ).toFile();
    dir.deleteOnExit();
    emptyDir = dir.getPath();

    dir = Files.createTempDirectory( "dir", new FileAttribute<?>[0] ).toFile();
    dir.deleteOnExit();
    nonEmptyDir = dir.getPath();

    File file = File.createTempFile( "existingFile", "ext", dir );
    file.deleteOnExit();
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testSetNrErrorsSuccess() throws Exception {
    entry.setFoldername( emptyDir );

    Result result = entry.execute( new Result(), 0 );

    assertTrue( "For empty folder result should be true", result.getResult() );
    assertEquals( "There should be no errors", 0, result.getNrErrors() );
  }

  @Test
  public void testSetNrErrorsNewBehaviorFail() throws Exception {
    entry.setFoldername( nonEmptyDir );

    Result result = entry.execute( new Result(), 0 );

    assertFalse( "For non-empty folder result should be false", result.getResult() );
    assertEquals( "There should be still no errors", 0, result.getNrErrors() );
  }

  @Test
  public void testSetNrErrorsOldBehaviorFail() throws Exception {
    entry.setFoldername( nonEmptyDir );

    entry.setVariable( Const.KETTLE_COMPATIBILITY_SET_ERROR_ON_SPECIFIC_JOB_ENTRIES, "Y" );

    Result result = entry.execute( new Result(), 0 );

    assertFalse( "For non-empty folder result should be false", result.getResult() );
    assertEquals( "According to old behaviour there should be an error", 1, result.getNrErrors() );
  }
}
