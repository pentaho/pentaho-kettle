/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.job.entries.setvariables;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;

public class JobEntrySetVariablesTest {
  private Job job;
  private JobEntrySetVariables entry;

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
    entry = new JobEntrySetVariables();
    job.getJobMeta().addJobEntry( new JobEntryCopy( entry ) );
    entry.setParentJob( job );
    job.setStopped( false );
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testASCIIText() throws Exception {
    // properties file with native2ascii
    entry.setFilename( "src/test/resources/org/pentaho/di/job/entries/setvariables/ASCIIText.properties" );
    entry.setVariableName( new String[] {} ); // For absence of null check in execute method
    entry.setReplaceVars( true );
    Result result = entry.execute( new Result(), 0 );
    assertTrue( "Result should be true", result.getResult() );
    assertEquals( "日本語", entry.getVariable( "Japanese" ) );
    assertEquals( "English", entry.getVariable( "English" ) );
    assertEquals( "中文", entry.getVariable( "Chinese" ) );
  }

  @Test
  public void testUTF8Text() throws Exception {
    // properties files without native2ascii
    entry.setFilename( "src/test/resources/org/pentaho/di/job/entries/setvariables/UTF8Text.properties" );
    entry.setVariableName( new String[] {} ); // For absence of null check in execute method
    entry.setReplaceVars( true );
    Result result = entry.execute( new Result(), 0 );
    assertTrue( "Result should be true", result.getResult() );
    assertEquals( "日本語", entry.getVariable( "Japanese" ) );
    assertEquals( "English", entry.getVariable( "English" ) );
    assertEquals( "中文", entry.getVariable( "Chinese" ) );
  }

  @Test
  public void testInputStreamClosed() throws Exception {
    // properties files without native2ascii
    String propertiesFilename = "src/test/resources/org/pentaho/di/job/entries/setvariables/UTF8Text.properties";
    entry.setFilename( propertiesFilename );
    entry.setVariableName( new String[] {} ); // For absence of null check in execute method
    entry.setReplaceVars( true );
    Result result = entry.execute( new Result(), 0 );
    assertTrue( "Result should be true", result.getResult() );

    RandomAccessFile fos = null;
    try {
      File file = new File( propertiesFilename );
      if ( file.exists() ) {
        fos = new RandomAccessFile( file, "rw" );
      }
    } catch ( FileNotFoundException | SecurityException e ) {
      fail( "the file with properties should be unallocated" );
    } finally {
      if ( fos != null ) {
        fos.close();
      }
    }
  }
}
