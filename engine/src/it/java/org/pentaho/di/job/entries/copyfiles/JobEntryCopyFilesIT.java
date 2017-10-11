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
package org.pentaho.di.job.entries.copyfiles;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.job.Job;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JobEntryCopyFilesIT {
  private final String EMPTY = "";

  private JobEntryCopyFiles entry;
  private Path source;
  private Path destination;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    KettleLogStore.init();
  }

  @Before
  public void setUp() throws Exception {
    entry = new JobEntryCopyFiles( "Job entry copy files" );
    entry.setParentJob( new Job() );

    source = Files.createTempDirectory( "src" );
    destination = Files.createTempDirectory( "dest" );

    entry.source_filefolder = new String[] { source.toString() };
    entry.destination_filefolder = new String[] { destination.toString() };
    entry.wildcard = new String[] { EMPTY };
  }

  @After
  public void tearDown() throws Exception {
    FileUtils.forceDelete( source.toFile() );
    FileUtils.forceDelete( destination.toFile() );
  }

  @Test
  public void copyFile() throws Exception {
    Files.createTempFile( source, "file", "" );

    Result result = entry.execute( new Result(), 0 );
    assertTrue(  result.getResult() );
    assertEquals( 0, result.getNrErrors() );
  }

  @Test
  public void copyFileFromSubDirectory() throws Exception {
    entry.setIncludeSubfolders( true );

    Path subDirectory = Files.createTempDirectory( source, "sub" );
    Files.createTempFile( subDirectory, "file", "" );

    Result result = entry.execute( new Result(), 0 );
    assertTrue(  result.getResult() );
    assertEquals( 0, result.getNrErrors() );
  }

  @Test
  public void copyFileWithoutOverwrite() throws Exception {
    entry.setoverwrite_files( false );

    Path pathToFile = Files.createTempFile( source, "file", "" );

    FileUtils.copyDirectory( source.toFile(), destination.toFile() );
    String path = destination.resolve( pathToFile.getFileName() ).toString();
    File file = new File( path );

    long createTime = file.lastModified();
    Result result = entry.execute( new Result(), 0 );
    long copyTime = file.lastModified();

    assertTrue(  result.getResult() );
    assertEquals( 0, result.getNrErrors() );
    assertTrue( "File shouldn't be overwritten", createTime == copyTime );
  }

  @Test
  public void copyFileFromSubDirectoryWithoutOverwrite() throws Exception {
    entry.setIncludeSubfolders( true );
    entry.setoverwrite_files( false );

    Path pathToSub = Files.createTempDirectory( source, "sub" );
    Path pathToFile = Files.createTempFile( pathToSub, "file", "" );

    FileUtils.copyDirectory( source.toFile(), destination.toFile() );
    String path = destination.resolve( pathToSub.getFileName() ).resolve( pathToFile.getFileName() ).toString();
    File file = new File( path );

    long createTime = file.lastModified();
    Result result = entry.execute( new Result(), 0 );
    long copyTime = file.lastModified();

    assertTrue(  result.getResult() );
    assertEquals( 0, result.getNrErrors() );
    assertTrue( "File shouldn't be overwritten", createTime == copyTime );
  }
}
