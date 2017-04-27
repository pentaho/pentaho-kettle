/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;




public class JobEntryCopyFilesTest {
  private JobEntryCopyFiles entry;

  private final String EMPTY = "";

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    KettleLogStore.init();
  }

  @Before
  public void setUp() {
    entry = new JobEntryCopyFiles();
    Job parentJob = new Job();
    entry.setParentJob( parentJob );
    entry = Mockito.spy( entry );
  }

  @Test
  public void fileNotCopied() throws Exception {
    entry.source_filefolder = new String[] { EMPTY };
    entry.destination_filefolder = new String[] { EMPTY };
    entry.wildcard = new String[] { EMPTY };

    entry.execute( new Result(), 0 );

    Mockito.verify( entry, Mockito.never() ).processFileFolder( Mockito.anyString(), Mockito.anyString(),
        Mockito.anyString(), Mockito.any( Job.class ), Mockito.any( Result.class ) );
  }

  @Test
  public void fileCopied() throws Exception {
    String srcPath = "path/to/file";
    String destPath = "path/to/dir";

    entry.source_filefolder = new String[] { srcPath };
    entry.destination_filefolder = new String[] { destPath };
    entry.wildcard = new String[] { EMPTY };

    Result result = entry.execute( new Result(), 0 );

    Mockito.verify( entry ).processFileFolder( Mockito.anyString(), Mockito.anyString(),
      Mockito.anyString(), Mockito.any( Job.class ), Mockito.any( Result.class ) );
    Mockito.verify( entry, Mockito.atLeast( 1 ) ).preprocessfilefilder( Mockito.any( String[].class ) );
    Assert.assertFalse( result.getResult() );
    Assert.assertEquals( 1, result.getNrErrors() );

  }

  @Test
  public void filesCopied() throws Exception {
    String[] srcPath = new String[] { "path1", "path2", "path3" };
    String[] destPath = new String[] { "dest1", "dest2", "dest3" };

    entry.source_filefolder = srcPath;
    entry.destination_filefolder = destPath;
    entry.wildcard = new String[] { EMPTY, EMPTY, EMPTY };

    Result result = entry.execute( new Result(), 0 );

    Mockito.verify( entry, Mockito.times( srcPath.length ) ).processFileFolder( Mockito.anyString(), Mockito.anyString(),
        Mockito.anyString(), Mockito.any( Job.class ), Mockito.any( Result.class ) );
    Assert.assertFalse( result.getResult() );
    Assert.assertEquals( 3, result.getNrErrors() );
  }

  @Test
  public void saveLoad() throws Exception {
    String[] srcPath = new String[] { "EMPTY_SOURCE_URL-0-" };
    String[] destPath = new String[] { "EMPTY_DEST_URL-0-" };

    entry.source_filefolder = srcPath;
    entry.destination_filefolder = destPath;
    entry.wildcard = new String[] { EMPTY };

    String xml = "<entry>" + entry.getXML() + "</entry>";
    Assert.assertTrue( xml.contains( srcPath[0] ) );
    Assert.assertTrue( xml.contains( destPath[0] ) );
    JobEntryCopyFiles loadedentry = new JobEntryCopyFiles();
    InputStream is = new ByteArrayInputStream( xml.getBytes() );
    loadedentry.loadXML( XMLHandler.getSubNode(
      XMLHandler.loadXMLFile( is,
        null,
        false,
        false ),
      "entry" ),
      new ArrayList<DatabaseMeta>(),
      null,
      null,
      null );
    Assert.assertTrue( loadedentry.destination_filefolder[0].equals( destPath[0] ) );
    Assert.assertTrue( loadedentry.source_filefolder[0].equals( srcPath[0] ) );

  }
}
