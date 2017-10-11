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

package org.pentaho.di.job.entries.job;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.job.Job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JobEntryJobRunnerTest {

  private JobEntryJobRunner jobRunner;
  private Job mockJob;
  private Result mockResult;
  private LogChannelInterface mockLog;
  private Job parentJob;

  @Before
  public void setUp() throws Exception {
    mockJob = mock( Job.class );
    mockResult = mock( Result.class );
    mockLog = mock( LogChannelInterface.class );
    jobRunner = new JobEntryJobRunner( mockJob, mockResult, 0, mockLog );
    parentJob = mock( Job.class );
  }

  @Test
  public void testRun() throws Exception {
    // Call all the NO-OP paths
    when( mockJob.isStopped() ).thenReturn( true );
    jobRunner.run();
    when( mockJob.isStopped() ).thenReturn( false );
    when( mockJob.getParentJob() ).thenReturn( null );
    jobRunner.run();
    when( parentJob.isStopped() ).thenReturn( true );
    when( mockJob.getParentJob() ).thenReturn( parentJob );
    jobRunner.run();
    when( parentJob.isStopped() ).thenReturn( false );
    when( mockJob.execute( Mockito.anyInt(), Mockito.any( Result.class ) ) ).thenReturn( mockResult );
    jobRunner.run();

  }

  @Test
  public void testRunSetsResult() throws Exception {
    when( mockJob.isStopped() ).thenReturn( false );
    when( mockJob.getParentJob() ).thenReturn( parentJob );
    when( parentJob.isStopped() ).thenReturn( false );
    when( mockJob.execute( Mockito.anyInt(), Mockito.any( Result.class ) ) ).thenReturn( mockResult );

    jobRunner.run();
    verify( mockJob, times( 1 ) ).setResult( Mockito.any( Result.class ) );
  }

  @Test
  public void testRunWithExceptionOnExecuteSetsResult() throws Exception {
    when( mockJob.isStopped() ).thenReturn( false );
    when( mockJob.getParentJob() ).thenReturn( parentJob );
    when( parentJob.isStopped() ).thenReturn( false );
    when( mockJob.execute( Mockito.anyInt(), Mockito.any( Result.class ) ) ).thenThrow( KettleException.class );

    jobRunner.run();
    verify( mockJob, times( 1 ) ).setResult( Mockito.any( Result.class ) );
  }

  @Test
  public void testRunWithExceptionOnFireJobSetsResult() throws KettleException {
    when( mockJob.isStopped() ).thenReturn( false );
    when( mockJob.getParentJob() ).thenReturn( parentJob );
    when( parentJob.isStopped() ).thenReturn( false );
    when( mockJob.execute( Mockito.anyInt(), Mockito.any( Result.class ) ) ).thenReturn( mockResult );

    doThrow( Exception.class ).when( mockJob ).fireJobFinishListeners();

    jobRunner.run();
    verify( mockJob, times( 1 ) ).setResult( Mockito.any( Result.class ) );
    assertTrue( jobRunner.isFinished() );
  }

  @Test
  public void testRunWithExceptionOnExecuteAndFireJobSetsResult() throws KettleException {
    when( mockJob.isStopped() ).thenReturn( false );
    when( mockJob.getParentJob() ).thenReturn( parentJob );
    when( parentJob.isStopped() ).thenReturn( false );
    when( mockJob.execute( Mockito.anyInt(), Mockito.any( Result.class ) ) ).thenReturn( mockResult );

    when( mockJob.execute( Mockito.anyInt(), Mockito.any( Result.class ) ) ).thenThrow( KettleException.class );
    doThrow( Exception.class ).when( mockJob ).fireJobFinishListeners();

    jobRunner.run();
    verify( mockJob, times( 1 ) ).setResult( Mockito.any( Result.class ) );
    assertTrue( jobRunner.isFinished() );
  }

  @Test
  public void testRunWithException() throws Exception {
    when( mockJob.isStopped() ).thenReturn( false );
    when( mockJob.getParentJob() ).thenReturn( parentJob );
    when( parentJob.isStopped() ).thenReturn( false );
    when( mockJob.execute( Mockito.anyInt(), Mockito.any( Result.class ) ) ).thenThrow( KettleException.class );
    jobRunner.run();
    verify( mockResult, times( 1 ) ).setNrErrors( Mockito.anyInt() );

    //[PDI-14981] catch more general exception to prevent thread hanging
    doThrow( Exception.class ).when( mockJob ).fireJobFinishListeners();
    jobRunner.run();

  }

  @Test
  public void testGetSetResult() throws Exception {
    assertEquals( mockResult, jobRunner.getResult() );
    jobRunner.setResult( null );
    assertNull( jobRunner.getResult() );
  }

  @Test
  public void testGetSetLog() throws Exception {
    assertEquals( mockLog, jobRunner.getLog() );
    jobRunner.setLog( null );
    assertNull( jobRunner.getLog() );
  }

  @Test
  public void testGetSetJob() throws Exception {
    assertEquals( mockJob, jobRunner.getJob() );
    jobRunner.setJob( null );
    assertNull( jobRunner.getJob() );
  }

  @Test
  public void testGetSetEntryNr() throws Exception {
    assertEquals( 0, jobRunner.getEntryNr() );
    jobRunner.setEntryNr( 1 );
    assertEquals( 1, jobRunner.getEntryNr() );
  }

  @Test
  public void testIsFinished() throws Exception {
    assertFalse( jobRunner.isFinished() );
    when( mockJob.isStopped() ).thenReturn( false );
    when( mockJob.getParentJob() ).thenReturn( parentJob );
    when( parentJob.isStopped() ).thenReturn( false );
    when( mockJob.execute( Mockito.anyInt(), Mockito.any( Result.class ) ) ).thenReturn( mockResult );
    jobRunner.run();
    assertTrue( jobRunner.isFinished() );
  }

  @Test
  public void testWaitUntilFinished() throws Exception {
    when( mockJob.isStopped() ).thenReturn( true );
    when( mockJob.getParentJob() ).thenReturn( parentJob );
    when( parentJob.isStopped() ).thenReturn( false );
    when( mockJob.execute( Mockito.anyInt(), Mockito.any( Result.class ) ) ).thenReturn( mockResult );
    jobRunner.waitUntilFinished();
  }
}
