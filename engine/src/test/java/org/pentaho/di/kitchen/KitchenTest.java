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


package org.pentaho.di.kitchen;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.base.CommandExecutorCodes;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleSecurityException;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.RepositoryOperation;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.security.Permission;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KitchenTest {

  private static final String TEST_PARAM_NAME = "testParam";
  private static final String DEFAULT_PARAM_VALUE = "default value";
  private static final String NOT_DEFAULT_PARAM_VALUE = "not the default value";

  private ByteArrayOutputStream sysOutContent;
  private ByteArrayOutputStream sysErrContent;


  RepositoriesMeta mockRepositoriesMeta;
  RepositoryMeta mockRepositoryMeta;
  Repository mockRepository;
  RepositoryDirectoryInterface mockRepositoryDirectory;

  @Before
  public void setUp() throws KettleException {
    KettleEnvironment.init();
    sysOutContent = new ByteArrayOutputStream();
    sysErrContent = new ByteArrayOutputStream();
    mockRepositoriesMeta = mock( RepositoriesMeta.class );
    mockRepositoryMeta = mock( RepositoryMeta.class );
    mockRepository = mock( Repository.class );
    mockRepositoryDirectory = mock( RepositoryDirectoryInterface.class );
  }

  @After
  public void tearDown() {
    sysOutContent = null;
    sysErrContent = null;
    mockRepositoriesMeta = null;
    mockRepositoryMeta = null;
    mockRepository = null;
    mockRepositoryDirectory = null;
  }

  @Test
  public void testKitchenStatusCodes() throws Exception {

    assertNull( CommandExecutorCodes.Kitchen.getByCode( 9999 ) );
    assertNotNull( CommandExecutorCodes.Kitchen.getByCode( 0 ) );

    assertEquals( CommandExecutorCodes.Kitchen.UNEXPECTED_ERROR, CommandExecutorCodes.Kitchen.getByCode( 2 ) );
    assertEquals( CommandExecutorCodes.Kitchen.CMD_LINE_PRINT, CommandExecutorCodes.Kitchen.getByCode( 9 ) );

    assertEquals( "The job ran without a problem", CommandExecutorCodes.Kitchen.getByCode( 0 ).getDescription() );
    assertEquals( "The job couldn't be loaded from XML or the Repository", CommandExecutorCodes.Kitchen.getByCode( 7 ).getDescription() );

    assertTrue( CommandExecutorCodes.Kitchen.isFailedExecution( CommandExecutorCodes.Kitchen.COULD_NOT_LOAD_JOB.getCode() ) );
    assertTrue( CommandExecutorCodes.Kitchen.isFailedExecution( CommandExecutorCodes.Kitchen.ERROR_LOADING_STEPS_PLUGINS.getCode() ) );
    assertFalse( CommandExecutorCodes.Kitchen.isFailedExecution( CommandExecutorCodes.Kitchen.SUCCESS.getCode() ) );
    assertFalse( CommandExecutorCodes.Kitchen.isFailedExecution( CommandExecutorCodes.Kitchen.ERRORS_DURING_PROCESSING.getCode() ) );
  }

  @Test
  public void testConfigureParameters() throws Exception {
    JobMeta jobMeta = new JobMeta();
    jobMeta.addParameterDefinition( TEST_PARAM_NAME, DEFAULT_PARAM_VALUE, "This tests a default parameter" );

    assertEquals( "Default parameter was not set correctly on JobMeta", DEFAULT_PARAM_VALUE,
            jobMeta.getParameterDefault( TEST_PARAM_NAME ) );

    assertEquals( "Parameter value should be blank in JobMeta", "", jobMeta.getParameterValue( TEST_PARAM_NAME ) );

    Job job = new Job( null, jobMeta );
    job.copyParametersFrom( jobMeta );

    assertEquals( "Default parameter was not set correctly on Job", DEFAULT_PARAM_VALUE,
            job.getParameterDefault( TEST_PARAM_NAME ) );

    assertEquals( "Parameter value should be blank in Job", "", job.getParameterValue( TEST_PARAM_NAME ) );
  }

  @Test
  public void testListRepos() throws Exception {

    PrintStream origSysOut;
    PrintStream origSysErr;

    final String TEST_REPO_DUMMY_NAME = "dummy-repo-name";
    final String TEST_REPO_DUMMY_DESC = "dummy-repo-description";

    when( mockRepositoryMeta.getName() ).thenReturn( TEST_REPO_DUMMY_NAME );
    when( mockRepositoryMeta.getDescription() ).thenReturn( TEST_REPO_DUMMY_DESC );

    when( mockRepositoriesMeta.nrRepositories() ).thenReturn( 1 );
    when( mockRepositoriesMeta.getRepository( 0 ) ).thenReturn( mockRepositoryMeta );

    KitchenCommandExecutor testPanCommandExecutor = new KitchenCommandExecutorForTesting( null, null, mockRepositoriesMeta );

    origSysOut = System.out;
    origSysErr = System.err;

    try {

      System.setOut( new PrintStream( sysOutContent ) );
      System.setErr( new PrintStream( sysErrContent ) );

      Kitchen.setCommandExecutor( testPanCommandExecutor );
      Kitchen.main( new String[] { "/listrep" } );

    } catch ( SecurityException e ) {
      // All OK / expected: SecurityException is purposely thrown when Pan triggers System.exitJVM()

      System.out.println( sysOutContent );

      assertTrue( sysOutContent.toString().contains( TEST_REPO_DUMMY_NAME ) );
      assertTrue( sysOutContent.toString().contains( TEST_REPO_DUMMY_DESC ) );

      Result result = Kitchen.getCommandExecutor().getResult();
      assertNotNull( result );
      assertEquals( result.getExitStatus(), CommandExecutorCodes.Kitchen.COULD_NOT_LOAD_JOB.getCode() );

    } finally {
      // sanitize

      Kitchen.setCommandExecutor( null );

      System.setOut( origSysOut );
      System.setErr( origSysErr );

    }
  }

  @Test
  public void testListDirs() throws Exception {

    PrintStream origSysOut;
    PrintStream origSysErr;

    final String DUMMY_DIR_1 = "test-dir-1";
    final String DUMMY_DIR_2 = "test-dir-2";

    when( mockRepository.getDirectoryNames( any() ) ).thenReturn( new String[]{ DUMMY_DIR_1, DUMMY_DIR_2 } );
    when( mockRepository.loadRepositoryDirectoryTree() ).thenReturn( mockRepositoryDirectory );

    KitchenCommandExecutor testPanCommandExecutor = new KitchenCommandExecutorForTesting( mockRepository, mockRepositoryMeta, null );

    origSysOut = System.out;
    origSysErr = System.err;

    try {

      System.setOut( new PrintStream( sysOutContent ) );
      System.setErr( new PrintStream( sysErrContent ) );

      Kitchen.setCommandExecutor( testPanCommandExecutor );
      // (case-insensitive) should accept either 'Y' (default) or 'true'
      Kitchen.main( new String[] { "/listdir:true", "/rep:test-repo", "/level:Basic" } );

    } catch ( SecurityException e ) {
      // All OK / expected: SecurityException is purposely thrown when Pan triggers System.exitJVM()

      System.out.println( sysOutContent );

      assertTrue( sysOutContent.toString().contains( DUMMY_DIR_1 ) );
      assertTrue( sysOutContent.toString().contains( DUMMY_DIR_2 ) );

      Result result = Kitchen.getCommandExecutor().getResult();
      assertNotNull( result );
      assertEquals( result.getExitStatus(), CommandExecutorCodes.Kitchen.SUCCESS.getCode() );

    } finally {
      // sanitize

      Kitchen.setCommandExecutor( null );

      System.setOut( origSysOut );
      System.setErr( origSysErr );
    }
  }

  @Test
  public void testListJobs() throws Exception {

    PrintStream origSysOut;
    PrintStream origSysErr;

    final String DUMMY_JOB_1 = "test-job-name-1";
    final String DUMMY_JOB_2 = "test-job-name-2";

    when( mockRepository.getJobNames( any(), anyBoolean() ) ).thenReturn( new String[]{ DUMMY_JOB_1, DUMMY_JOB_2 } );
    when( mockRepository.loadRepositoryDirectoryTree() ).thenReturn( mockRepositoryDirectory );

    KitchenCommandExecutor testPanCommandExecutor = new KitchenCommandExecutorForTesting( mockRepository, mockRepositoryMeta,  null );

    origSysOut = System.out;
    origSysErr = System.err;

    try {

      System.setOut( new PrintStream( sysOutContent ) );
      System.setErr( new PrintStream( sysErrContent ) );

      Kitchen.setCommandExecutor( testPanCommandExecutor );
      // (case-insensitive) should accept either 'Y' (default) or 'true'
      Kitchen.main( new String[] { "/listjobs:Y", "/rep:test-repo", "/level:Basic" } );

    } catch ( SecurityException e ) {
      // All OK / expected: SecurityException is purposely thrown when Pan triggers System.exitJVM()

      System.out.println( sysOutContent );

      assertTrue( sysOutContent.toString().contains( DUMMY_JOB_1 ) );
      assertTrue( sysOutContent.toString().contains( DUMMY_JOB_2 ) );

      Result result = Kitchen.getCommandExecutor().getResult();
      assertNotNull( result );
      assertEquals( result.getExitStatus(), CommandExecutorCodes.Kitchen.SUCCESS.getCode() );

    } finally {
      // sanitize

      Kitchen.setCommandExecutor( null );

      System.setOut( origSysOut );
      System.setErr( origSysErr );
    }
  }

  private class KitchenCommandExecutorForTesting extends KitchenCommandExecutor {

    private Repository testRepository;
    private RepositoryMeta testRepositoryMeta;
    private RepositoriesMeta testRepositoriesMeta;

    public KitchenCommandExecutorForTesting( Repository testRepository, RepositoryMeta testRepositoryMeta,
                                             RepositoriesMeta testRepositoriesMeta ) {
      super( Kitchen.class );
      this.testRepository = testRepository;
      this.testRepositoryMeta = testRepositoryMeta;
      this.testRepositoriesMeta = testRepositoriesMeta;
    }

    @Override
    public RepositoriesMeta loadRepositoryInfo( String loadingAvailableRepMsgTkn, String noRepsDefinedMsgTkn ) throws KettleException {
      return testRepositoriesMeta != null ? testRepositoriesMeta : super.loadRepositoryInfo( loadingAvailableRepMsgTkn, noRepsDefinedMsgTkn );
    }

    @Override
    public RepositoryMeta loadRepositoryConnection( final String repoName, String loadingAvailableRepMsgTkn,
                                                        String noRepsDefinedMsgTkn, String findingRepMsgTkn ) throws KettleException {
      return testRepositoryMeta != null ? testRepositoryMeta : super.loadRepositoryConnection( repoName, loadingAvailableRepMsgTkn,
                    noRepsDefinedMsgTkn, findingRepMsgTkn );
    }

    @Override
    public Repository establishRepositoryConnection( RepositoryMeta repositoryMeta, final String username, final String password,
                                                         final RepositoryOperation... operations ) throws KettleException, KettleSecurityException {
      return testRepository != null ? testRepository : super.establishRepositoryConnection( repositoryMeta, username, password, operations );
    }
  }
}
