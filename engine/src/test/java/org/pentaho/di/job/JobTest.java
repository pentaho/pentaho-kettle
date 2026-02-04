/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
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
import org.pentaho.di.core.logging.LogStatus;
import org.pentaho.di.core.logging.LogTableField;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectLifecycleInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
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
import static org.junit.Assert.assertFalse;
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

  @Test
  public void testIsVfs_WhenFileNameHasVfsPath() {
    Repository repository = mock( Repository.class );
    Job job = new Job( repository, mockedJobMeta );
    LoggingObjectInterface parentLoggingObject = mock( LoggingObjectInterface.class );
    setInternalState( job, "parentLoggingObject", parentLoggingObject );
    when( mockedJobMeta.getFilename() ).thenReturn( "pvfs://LocalVFS/samples/test-transformation.ktr" );

    assertTrue( job.isVfs() );
  }

  @Test
  public void testIsVfs_WhenFileNameHasFileSystemPath() {
    Repository repository = mock( Repository.class );
    Job job = new Job( repository, mockedJobMeta );
    LoggingObjectInterface parentLoggingObject = mock( LoggingObjectInterface.class );
    setInternalState( job, "parentLoggingObject", parentLoggingObject );
    when( mockedJobMeta.getFilename() ).thenReturn( "C://test-transformation.ktr" );

    assertFalse( job.isVfs() );
  }

  /**
   * Tests that variables explicitly set on a Job before run() is called are preserved
   * during execution. This is critical for scheduled jobs where variables are set
   * via shareVariablesWith() or setVariable() before the job starts.
   * 
   * This test verifies the fix for the regression introduced in BACKLOG-44138 where
   * Job.run() would overwrite all pre-set variables with system properties.
   */
  @Test
  public void testVariablesSetBeforeRunArePreserved() {
    // Setup: Create a job with a variable set before run()
    String testVarName = "TEST_PRESERVED_VAR_" + UUID.randomUUID().toString();
    String presetValue = "preset_value_should_be_preserved";
    
    // Ensure the variable is NOT in system properties (or has a different value)
    String systemValue = System.getProperty( testVarName );
    assertFalse( "Test variable should not exist in system properties for this test to be valid",
        presetValue.equals( systemValue ) );
    
    Repository repository = mock( Repository.class );
    JobMeta jobMeta = new JobMeta();
    Job job = new Job( repository, jobMeta );
    
    // Set the variable before run() - this simulates what PdiAction does
    job.setVariable( testVarName, presetValue );
    
    // Verify it's set
    assertEquals( "Variable should be set before run", presetValue, job.getVariable( testVarName ) );
    
    // Simulate what run() does when there's no parent job
    job.initializeVariablesFromDefaultSpace();
    
    // The variable should still have the preset value, not be overwritten
    assertEquals( "Variable set before run() should be preserved after initialization",
        presetValue, job.getVariable( testVarName ) );
  }

  /**
   * Tests that variables from the default variable space (system properties) are still
   * available for variables that were NOT explicitly set before run().
   * This ensures the fix doesn't break the BACKLOG-44138 feature.
   */
  @Test
  public void testDefaultVariablesAreAvailableWhenNotExplicitlySet() {
    // Use user.dir as a system property that should be available
    String systemVarName = "user.dir"; // This should always exist
    String systemValue = System.getProperty( systemVarName );
    
    // Verify the system property exists for this test to be valid
    Assert.assertNotNull( "System property should exist for this test", systemValue );
    
    Repository repository = mock( Repository.class );
    JobMeta jobMeta = new JobMeta();
    Job job = new Job( repository, jobMeta );
    
    // Don't set the variable explicitly - it should come from system properties
    // Simulate what run() does when there's no parent job
    job.initializeVariablesFromDefaultSpace();
    
    // The system property should be available
    assertEquals( "System property should be available after initialization",
        systemValue, job.getVariable( systemVarName ) );
  }

  /**
   * Tests that when a preset variable value differs from the system property value,
   * the preset value wins (is preserved).
   */
  @Test
  public void testPresetVariableOverridesSystemProperty() {
    // Use a system property that we know exists
    String varName = "user.dir";
    String systemValue = System.getProperty( varName );
    String presetValue = "my_custom_override_value";
    
    // Verify system property exists and our preset is different
    Assert.assertNotNull( "System property should exist", systemValue );
    assertNotEquals( "Preset value should differ from system value", systemValue, presetValue );
    
    Repository repository = mock( Repository.class );
    JobMeta jobMeta = new JobMeta();
    Job job = new Job( repository, jobMeta );
    
    // Set the variable to override the system property
    job.setVariable( varName, presetValue );
    
    // Simulate what run() does when there's no parent job
    job.initializeVariablesFromDefaultSpace();
    
    // The preset value should win over the system property
    assertEquals( "Preset variable should override system property",
        presetValue, job.getVariable( varName ) );
  }
}
