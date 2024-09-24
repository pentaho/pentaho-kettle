/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.spoon;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.job.JobEntryJob;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryCopy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class JobFileListenerTest {

  JobFileListener jobFileListener;

  @Before
  public void setUp() {
    jobFileListener = new JobFileListener();
  }

  @Test
  public void testAccepts() throws Exception {
    assertFalse( jobFileListener.accepts( null ) );
    assertFalse( jobFileListener.accepts( "NoDot" ) );
    assertTrue( jobFileListener.accepts( "Job.kjb" ) );
    assertTrue( jobFileListener.accepts( ".kjb" ) );
  }

  @Test
  public void testAcceptsXml() throws Exception {
    assertFalse( jobFileListener.acceptsXml( null ) );
    assertFalse( jobFileListener.acceptsXml( "" ) );
    assertFalse( jobFileListener.acceptsXml( "Job" ) );
    assertTrue( jobFileListener.acceptsXml( "job" ) );
  }

  @Test
  public void testGetFileTypeDisplayNames() throws Exception {
    String[] names = jobFileListener.getFileTypeDisplayNames( null );
    assertNotNull( names );
    assertEquals( 2, names.length );
    assertEquals( "Jobs", names[0] );
    assertEquals( "XML", names[1] );
  }

  @Test
  public void testGetRootNodeName() throws Exception {
    assertEquals( "job", jobFileListener.getRootNodeName() );
  }

  @Test
  public void testGetSupportedExtensions() throws Exception {
    String[] extensions = jobFileListener.getSupportedExtensions();
    assertNotNull( extensions );
    assertEquals( 2, extensions.length );
    assertEquals( "kjb", extensions[0] );
    assertEquals( "xml", extensions[1] );
  }

  @Test
  public void testProcessLinkedTransWithFilename() {
    JobEntryTrans jobTransExecutor = spy( new JobEntryTrans() );
    jobTransExecutor.setFileName( "/path/to/Transformation2.ktr" );
    jobTransExecutor.setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );
    JobEntryCopy jobEntry = mock( JobEntryCopy.class );
    when( jobEntry.getEntry() ).thenReturn( jobTransExecutor );

    JobMeta parent = mock( JobMeta.class );
    when( parent.nrJobEntries() ).thenReturn( 1 );
    when( parent.getJobEntry( 0 ) ).thenReturn( jobEntry );

    JobMeta result = jobFileListener.processLinkedTrans( parent );

    JobEntryCopy meta = result.getJobEntry( 0 );
    assertNotNull( meta );
    JobEntryTrans resultExecMeta = (JobEntryTrans) meta.getEntry();
    assertEquals( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME, resultExecMeta.getSpecificationMethod() );
    assertEquals( "/path/to", resultExecMeta.getDirectory() );
    assertEquals( "Transformation2", resultExecMeta.getTransname() );
  }

  @Test
  public void testProcessLinkedTransWithNoFilenameMethodFileName() {
    testProcessLinkedTransWithNoFilename( ObjectLocationSpecificationMethod.FILENAME );
  }

  @Test
  public void testProcessLinkedTransWithNoFilenameMethodRepoByName() {
    testProcessLinkedTransWithNoFilename( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
  }

  private void testProcessLinkedTransWithNoFilename( final ObjectLocationSpecificationMethod method ) {
    JobEntryTrans jobTransExecutor = spy( new JobEntryTrans() );
    jobTransExecutor.setFileName( null );
    jobTransExecutor.setDirectory( "/path/to" );
    jobTransExecutor.setTransname( "Transformation2" );
    jobTransExecutor.setSpecificationMethod( method );
    JobEntryCopy jobEntry = mock( JobEntryCopy.class );
    when( jobEntry.getEntry() ).thenReturn( jobTransExecutor );

    JobMeta parent = mock( JobMeta.class );
    when( parent.nrJobEntries() ).thenReturn( 1 );
    when( parent.getJobEntry( 0 ) ).thenReturn( jobEntry );

    JobMeta result = jobFileListener.processLinkedTrans( parent );

    JobEntryCopy meta = result.getJobEntry( 0 );
    assertNotNull( meta );
    JobEntryTrans resultExecMeta = (JobEntryTrans) meta.getEntry();
    assertEquals( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME, resultExecMeta.getSpecificationMethod() );
    assertEquals( "/path/to", resultExecMeta.getDirectory() );
    assertEquals( "Transformation2", resultExecMeta.getTransname() );
  }

  @Test
  public void testProcessLinkedJobsWithFilename() {
    JobEntryJob jobJobExecutor = spy( new JobEntryJob() );
    jobJobExecutor.setFileName( "/path/to/Job1.kjb" );
    jobJobExecutor.setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );
    JobEntryCopy jobEntry = mock( JobEntryCopy.class );
    when( jobEntry.getEntry() ).thenReturn( jobJobExecutor );

    JobMeta parent = mock( JobMeta.class );
    when( parent.nrJobEntries() ).thenReturn( 1 );
    when( parent.getJobEntry( 0 ) ).thenReturn( jobEntry );

    JobMeta result = jobFileListener.processLinkedJobs( parent );

    JobEntryCopy meta = result.getJobEntry( 0 );
    assertNotNull( meta );
    JobEntryJob resultExecMeta = (JobEntryJob) meta.getEntry();
    assertEquals( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME, resultExecMeta.getSpecificationMethod() );
    assertEquals( resultExecMeta.getDirectory(), "/path/to" );
    assertEquals( resultExecMeta.getJobName(), "Job1" );
  }

  @Test
  public void testProcessLinkedJobsWithNoFilenameMethodFilename() {
    testProcessLinkedJobsWithNoFilename( ObjectLocationSpecificationMethod.FILENAME );
  }

  @Test
  public void testProcessLinkedJobsWithNoFilenameMethodRepoByName() {
    testProcessLinkedJobsWithNoFilename( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
  }

  private void testProcessLinkedJobsWithNoFilename( final ObjectLocationSpecificationMethod method ) {
    JobEntryJob jobJobExecutor = spy( new JobEntryJob() );
    jobJobExecutor.setFileName( null );
    jobJobExecutor.setDirectory( "/path/to" );
    jobJobExecutor.setJobName( "Job1" );
    jobJobExecutor.setSpecificationMethod( method );
    JobEntryCopy jobEntry = mock( JobEntryCopy.class );
    when( jobEntry.getEntry() ).thenReturn( jobJobExecutor );

    JobMeta parent = mock( JobMeta.class );
    when( parent.nrJobEntries() ).thenReturn( 1 );
    when( parent.getJobEntry( 0 ) ).thenReturn( jobEntry );

    JobMeta result = jobFileListener.processLinkedJobs( parent );

    JobEntryCopy meta = result.getJobEntry( 0 );
    assertNotNull( meta );
    JobEntryJob resultExecMeta = (JobEntryJob) meta.getEntry();
    assertEquals( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME, resultExecMeta.getSpecificationMethod() );
    assertEquals( resultExecMeta.getDirectory(), "/path/to" );
    assertEquals( resultExecMeta.getJobName(), "Job1" );
  }

  @Test
  public void testClearCurrentDirectoryChangedListenersWhenImporting() {
    JobMeta jm = mock( JobMeta.class );
    jobFileListener.clearCurrentDirectoryChangedListenersWhenImporting( true, jm );
    verify( jm, times( 1 ) ).clearCurrentDirectoryChangedListeners();
  }

  @Test
  public void testClearCurrentDirectoryChangedListenersWhenNotImporting() {
    JobMeta jm = mock( JobMeta.class );
    jobFileListener.clearCurrentDirectoryChangedListenersWhenImporting( false, jm );
    verify( jm, times( 0 ) ).clearCurrentDirectoryChangedListeners();
  }

}
