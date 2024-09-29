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

package org.pentaho.di.job;

import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.JobTracker;
import org.pentaho.di.core.logging.BaseLogTable;
import org.pentaho.di.core.logging.JobEntryLogTable;
import org.pentaho.di.core.logging.JobLogTable;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LoggingObjectLifecycleInterface;
import org.pentaho.di.core.logging.LogStatus;
import org.pentaho.di.core.logging.LogTableField;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.job.entries.special.JobEntrySpecial;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.HasDatabasesInterface;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.pentaho.test.util.InternalState.setInternalState;


public class JobTest {
  private static final String STRING_DEFAULT = "<def>";
  private Job mockedJob;
  private Database mockedDataBase;
  private VariableSpace mockedVariableSpace;
  private HasDatabasesInterface hasDatabasesInterface;
  private JobMeta mockedJobMeta;
  private JobEntryCopy mockedJobEntryCopy;
  private JobEntrySpecial mockedJobEntrySpecial;
  private LogChannel mockedLogChannel;


  @Before
  public void init() {
    mockedDataBase = mock( Database.class );
    mockedJob = mock( Job.class );
    mockedVariableSpace = mock( VariableSpace.class );
    hasDatabasesInterface = mock( HasDatabasesInterface.class );
    mockedJobMeta = mock( JobMeta.class );
    mockedJobEntryCopy = mock( JobEntryCopy.class );
    mockedJobEntrySpecial = mock( JobEntrySpecial.class );
    mockedLogChannel = mock( LogChannel.class );

    when( mockedJob.createDataBase( any( DatabaseMeta.class ) ) ).thenReturn( mockedDataBase );
  }

  @Ignore( "Test is validating against a mock object... not a real test" )
  @Test
  public void recordsCleanUpMethodIsCalled_JobEntryLogTable() throws Exception {

    JobEntryLogTable jobEntryLogTable = JobEntryLogTable.getDefault( mockedVariableSpace, hasDatabasesInterface );
    setAllTableParamsDefault( jobEntryLogTable );

    JobMeta jobMeta = new JobMeta(  );
    jobMeta.setJobEntryLogTable( jobEntryLogTable );

    when( mockedJob.getJobMeta() ).thenReturn( jobMeta );
    doCallRealMethod().when( mockedJob ).writeJobEntryLogInformation();

    when( mockedJob.createDataBase( any() ) ).thenReturn( mockedDataBase );
    mockedJob.writeJobEntryLogInformation();

    verify( mockedDataBase ).cleanupLogRecords( eq( jobEntryLogTable ), anyString() );
  }

  @Ignore( "Test is validating against a mock object... not a real test" )
  @Test
  public void recordsCleanUpMethodIsCalled_JobLogTable() throws Exception {
    JobLogTable jobLogTable = JobLogTable.getDefault( mockedVariableSpace, hasDatabasesInterface );
    setAllTableParamsDefault( jobLogTable );

    doCallRealMethod().when( mockedJob ).writeLogTableInformation( jobLogTable, LogStatus.END );

    mockedJob.writeLogTableInformation( jobLogTable, LogStatus.END );

    verify( mockedDataBase ).cleanupLogRecords( eq( jobLogTable ), anyString() );
  }

  public void setAllTableParamsDefault( BaseLogTable table ) {
    table.setSchemaName( STRING_DEFAULT );
    table.setConnectionName( STRING_DEFAULT );
    table.setTimeoutInDays( STRING_DEFAULT );
    table.setTableName( STRING_DEFAULT );
    table.setFields( new ArrayList<LogTableField>() );
  }

  @Test
  public void testNewJobWithContainerObjectId() {
    Repository repository = mock( Repository.class );
    JobMeta meta = mock( JobMeta.class );

    String carteId = UUID.randomUUID().toString();
    doReturn( carteId ).when( meta ).getContainerObjectId();

    Job job = new Job( repository, meta );

    assertEquals( carteId, job.getContainerObjectId() );
  }

  /**
   * This test demonstrates the issue fixed in PDI-17398.
   * When a job is scheduled twice, it gets the same log channel Id and both logs get merged
   */
  @Test
  public void testTwoJobsGetSameLogChannelId() {
    Repository repository = mock( Repository.class );
    JobMeta meta = mock( JobMeta.class );

    Job job1 = new Job( repository, meta );
    Job job2 = new Job( repository, meta );

    assertEquals( job1.getLogChannelId(), job2.getLogChannelId() );
  }

  /**
   * This test demonstrates the fix for PDI-17398.
   * Two schedules -> two Carte object Ids -> two log channel Ids
   */
  @Test
  public void testTwoJobsGetDifferentLogChannelIdWithDifferentCarteId() {
    Repository repository = mock( Repository.class );
    JobMeta meta1 = mock( JobMeta.class );
    JobMeta meta2 = mock( JobMeta.class );

    String carteId1 = UUID.randomUUID().toString();
    String carteId2 = UUID.randomUUID().toString();

    doReturn( carteId1 ).when( meta1 ).getContainerObjectId();
    doReturn( carteId2 ).when( meta2 ).getContainerObjectId();

    Job job1 = new Job( repository, meta1 );
    Job job2 = new Job( repository, meta2 );

    assertNotEquals( job1.getContainerObjectId(), job2.getContainerObjectId() );
    assertNotEquals( job1.getLogChannelId(), job2.getLogChannelId() );
  }

  @Test
  public void testSetInternalEntryCurrentDirectoryWithFilename( ) {
    Job jobTest = new Job(  );
    boolean hasFilename = true;
    boolean hasRepoDir = false;
    jobTest.copyVariablesFrom( null );
    jobTest.setVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY, "Original value defined at run execution" );
    jobTest.setVariable( Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY, "file:///C:/SomeFilenameDirectory" );
    jobTest.setVariable( Const.INTERNAL_VARIABLE_JOB_REPOSITORY_DIRECTORY, "/SomeRepDirectory" );
    jobTest.setInternalEntryCurrentDirectory( hasFilename, hasRepoDir );

    assertEquals( "file:///C:/SomeFilenameDirectory", jobTest.getVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY ) );

  }

  @Test
  public void testSetInternalEntryCurrentDirectoryWithRepository( ) {
    Job jobTest = new Job(  );
    boolean hasFilename = false;
    boolean hasRepoDir = true;
    jobTest.copyVariablesFrom( null );
    jobTest.setVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY, "Original value defined at run execution" );
    jobTest.setVariable( Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY, "file:///C:/SomeFilenameDirectory" );
    jobTest.setVariable( Const.INTERNAL_VARIABLE_JOB_REPOSITORY_DIRECTORY, "/SomeRepDirectory" );
    jobTest.setInternalEntryCurrentDirectory( hasFilename, hasRepoDir );

    assertEquals( "/SomeRepDirectory", jobTest.getVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY ) );
  }

  @Test
  public void testSetInternalEntryCurrentDirectoryWithoutFilenameOrRepository( ) {
    Job jobTest = new Job(  );
    jobTest.copyVariablesFrom( null );
    boolean hasFilename = false;
    boolean hasRepoDir = false;
    jobTest.setVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY, "Original value defined at run execution" );
    jobTest.setVariable( Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY, "file:///C:/SomeFilenameDirectory" );
    jobTest.setVariable( Const.INTERNAL_VARIABLE_JOB_REPOSITORY_DIRECTORY, "/SomeRepDirectory" );
    jobTest.setInternalEntryCurrentDirectory( hasFilename, hasRepoDir );

    assertEquals( "Original value defined at run execution", jobTest.getVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY )  );
  }

  /**
   * Tests the execution of Job With Previous Results (Called by JobExecutor - Job Calling another Job)
   * The called Job is not set to Repeat
   */
  @Test
  public void executeWithPreviousResultsNoRepeatTest() {
    executeWithPreviousResultsTest( false );
  }

  /**
   * Tests the execution of Job With Previous Results (Called by JobExecutor - Job Calling another Job)
   * The called Job is set to Repeat
   */
  @Test
  public void executeWithPreviousResultsWithRepeatTest() {
    executeWithPreviousResultsTest( true );
  }

  private void executeWithPreviousResultsTest( boolean repeat ) {
    setupJobMockExecution();
    try {
      when( mockedJobEntrySpecial.execute( any( Result.class ), anyInt() ) ).thenReturn( new Result(  ) );
      when( mockedJob.execute( anyInt(), any( Result.class ) ) ).thenCallRealMethod();
      when( mockedJobEntrySpecial.isRepeat() ).thenReturn( repeat );
      if ( repeat ) {
        //The job will repeat its execution until it is stopped
        scheduleStopJobExecution();
      }
      mockedJob.execute( 0, new Result(  ) );
      //Test expected invocations. If repeat setActive(false) will be called at least twice. With no repeat, only once.
      verify( mockedJob, repeat ? atLeast( 2 ) : times( 1 ) ).setActive( false );
    } catch ( KettleException e ) {
      Assert.fail( "Could not execute job" );
    }
  }

  private void setupJobMockExecution() {
    setInternalState( mockedJob, "jobMeta", mockedJobMeta );
    setInternalState( mockedJob, "log", mockedLogChannel );
    setInternalState( mockedJob, "jobTracker", new JobTracker( mockedJobMeta ) );
    setInternalState( mockedJob, "jobEntryListeners", new ArrayList<>(  ) );
    setInternalState( mockedJob, "jobEntryResults", new LinkedList<>(  ) );
    setInternalState( mockedJob, "status", new AtomicInteger( 0 ) );
    setInternalState( mockedJob, "lastNr", new MutableInt( -1 ) );
    when( mockedJobMeta.findJobEntry( JobMeta.STRING_SPECIAL_START, 0, false ) ).thenReturn( mockedJobEntryCopy );
    when( mockedJobEntryCopy.getEntry() ).thenReturn( mockedJobEntrySpecial );
    when( mockedJobEntrySpecial.getLogChannel() ).thenReturn( mockedLogChannel );
    when( mockedJobEntrySpecial.clone() ).thenReturn( mockedJobEntrySpecial );
    when( mockedJob.isStopped() ).thenCallRealMethod();
    doCallRealMethod().when( mockedJob ).setStopped( anyBoolean() );
    KettleLogStore.init();
  }

  private void scheduleStopJobExecution() {
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    Runnable task = () -> mockedJob.setStopped( true );
    scheduler.schedule( task, 1, TimeUnit.SECONDS );
    scheduler.shutdown();
  }

  /**
   * Tests the re-execution of a sub-job with a checkpoint and Previous Results (Called by JobExecutor - Job Calling
   * another Job) The called Job is set to Repeat
   */
  @Test
  public void executeWithPreviousCheckpointTest() {

    setupJobMockExecution();
    try {
      when( mockedJob.execute( anyInt(), any( Result.class ) ) ).thenCallRealMethod();
      JobEntryCopy startJobEntryCopy = mock( JobEntryCopy.class );
      Result startJobEntryResult = mock( Result.class );
      JobEntryInterface mockJobEntryInterface =
        mock( JobEntryInterface.class, withSettings().extraInterfaces( VariableSpace.class ) );
      when( startJobEntryCopy.getEntry() ).thenReturn( mockJobEntryInterface );
      when( mockJobEntryInterface.getLogChannel() ).thenReturn( mockedLogChannel );
      when( mockJobEntryInterface.clone() ).thenReturn( mockJobEntryInterface );
      when( startJobEntryResult.clone() ).thenReturn( startJobEntryResult );
      setInternalState( mockedJob, "startJobEntryCopy", startJobEntryCopy );
      setInternalState( mockedJob, "startJobEntryResult", startJobEntryResult );
      setInternalState( mockedJob, "lastNr", new MutableInt( -1 ) );
      when( mockJobEntryInterface.execute( startJobEntryResult, 0 ) ).thenReturn( new Result() );

      mockedJob.execute( 0, new Result() );

      //Verify that the execute used the start point supplied with result supplied instead of starting from the start
      verify( mockJobEntryInterface, times( 1 ) ).execute( eq( startJobEntryResult ), eq( 0 ) );
      verify( mockedJobEntrySpecial, times( 0 ) ).execute( any( Result.class ), anyInt() );
    } catch ( KettleException e ) {
      Assert.fail( "Could not execute job" );
    }
  }


 @Ignore( "Not really a valid test... testing the methods of an interface ")
 @Test
  public void testJobLoggingObjectLifecycleInterface() {
    Job job = new Job();

    assertTrue( job instanceof LoggingObjectLifecycleInterface );
//    assertEquals( 2, getMethods( Job.class, "callBeforeLog", "callAfterLog" ).length );

  }

  @Test
  public void testJobCallBeforeLog() {
    Job job = new Job();
    LoggingObjectInterface parentLoggingObject = mock( LoggingObjectInterface.class );
    setInternalState( job, "parentLoggingObject", parentLoggingObject );

    job.callBeforeLog();
    verify( parentLoggingObject, times( 1 ) ).callBeforeLog();
  }

  @Test
  public void testJobCallAfterLog() {
    Job job = new Job();
    LoggingObjectInterface parentLoggingObject = mock( LoggingObjectInterface.class );
    setInternalState( job, "parentLoggingObject", parentLoggingObject );

    job.callAfterLog();
    verify( parentLoggingObject, times( 1 ) ).callAfterLog();
  }

}
