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
package org.pentaho.di.job.entries.copyfiles;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.steps.named.cluster.NamedClusterEmbedManager;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JobEntryCopyFilesTest {
  private JobEntryCopyFiles entry;
  private NamedClusterEmbedManager mockNamedClusterEmbedManager;

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
    JobMeta mockJobMeta = mock( JobMeta.class );
    mockNamedClusterEmbedManager = mock( NamedClusterEmbedManager.class );
    when( mockJobMeta.getNamedClusterEmbedManager() ).thenReturn( mockNamedClusterEmbedManager );
    entry.setParentJobMeta( mockJobMeta );
    entry = spy( entry );
  }

  @Test
  public void fileNotCopied() throws Exception {
    entry.source_filefolder = new String[] { EMPTY };
    entry.destination_filefolder = new String[] { EMPTY };
    entry.wildcard = new String[] { EMPTY };

    entry.execute( new Result(), 0 );

    verify( entry, never() ).processFileFolder( anyString(), anyString(),
        anyString(), any( Job.class ), any( Result.class ) );
  }

  @Test
  public void fileCopied() throws Exception {
    String srcPath = "path/to/file";
    String destPath = "path/to/dir";

    entry.source_filefolder = new String[] { srcPath };
    entry.destination_filefolder = new String[] { destPath };
    entry.wildcard = new String[] { EMPTY };

    Result result = entry.execute( new Result(), 0 );

    verify( entry ).processFileFolder( anyString(), anyString(),
        anyString(), any( Job.class ), any( Result.class ) );
    verify( entry, atLeast( 1 ) ).preprocessfilefilder( any( String[].class ) );
    assertFalse( result.getResult() );
    assertEquals( 1, result.getNrErrors() );
  }

  @Test
  public void filesCopied() throws Exception {
    String[] srcPath = new String[] { "path1", "path2", "path3" };
    String[] destPath = new String[] { "dest1", "dest2", "dest3" };

    entry.source_filefolder = srcPath;
    entry.destination_filefolder = destPath;
    entry.wildcard = new String[] { EMPTY, EMPTY, EMPTY };

    Result result = entry.execute( new Result(), 0 );

    verify( entry, times( srcPath.length ) ).processFileFolder( anyString(), anyString(),
        anyString(), any( Job.class ), any( Result.class ) );
    assertFalse( result.getResult() );
    assertEquals( 3, result.getNrErrors() );
  }

  @Test
  public void saveLoad() throws Exception {
    String[] srcPath = new String[] { "EMPTY_SOURCE_URL-0-" };
    String[] destPath = new String[] { "EMPTY_DEST_URL-0-" };

    entry.source_filefolder = srcPath;
    entry.destination_filefolder = destPath;
    entry.wildcard = new String[] { EMPTY };

    String xml = "<entry>" + entry.getXML() + "</entry>";
    assertTrue( xml.contains( srcPath[0] ) );
    assertTrue( xml.contains( destPath[0] ) );
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
    assertTrue( loadedentry.destination_filefolder[0].equals( destPath[0] ) );
    assertTrue( loadedentry.source_filefolder[0].equals( srcPath[0] ) );
    verify( mockNamedClusterEmbedManager, times( 2 ) ).registerUrl( anyString() );
  }

  @Test
  public void saveLoadWithPassword() throws Exception {
    String[] srcPath = new String[] { "EMPTY_SOURCE_URL-0-hdfs://user321:321fake@foo.bar.com:8020/user/user321" };
    String[] destPath = new String[] { "EMPTY_DEST_URL-0-hdfs://user123:fake123@foo.bar.com:8020/user/user123" };

    String srcPathScrubbedEncoded = "EMPTY_SOURCE_URL-0-hdfs://user321:&lt;password&gt;@foo.bar.com:8020/user/user321";
    String destPathScrubbedEncoded = "EMPTY_DEST_URL-0-hdfs://user123:&lt;password&gt;@foo.bar.com:8020/user/user123";

    String srcPathScrubbed = "EMPTY_SOURCE_URL-0-hdfs://user321:<password>@foo.bar.com:8020/user/user321";
    String destPathScrubbed = "EMPTY_DEST_URL-0-hdfs://user123:<password>@foo.bar.com:8020/user/user123";

    entry.source_filefolder = srcPath;
    entry.destination_filefolder = destPath;
    entry.wildcard = new String[] { EMPTY };

    String xml = "<entry>" + entry.getXML() + "</entry>";
    assertTrue( xml.contains( srcPathScrubbedEncoded ) );
    assertTrue( xml.contains( destPathScrubbedEncoded ) );
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
    assertTrue( loadedentry.destination_filefolder[0].equals( destPathScrubbed ) );
    assertTrue( loadedentry.source_filefolder[0].equals( srcPathScrubbed ) );
    verify( mockNamedClusterEmbedManager, times( 2 ) ).registerUrl( anyString() );
  }
}
