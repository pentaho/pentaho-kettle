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


package org.pentaho.di.pan;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.base.CommandExecutorCodes;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleSecurityException;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.NamedParamsDefault;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.RepositoryOperation;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

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

public class PanTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private static final String TEST_PARAM_NAME = "testParam";
  private static final String DEFAULT_PARAM_VALUE = "default value";
  private static final String NOT_DEFAULT_PARAM_VALUE = "not the default value";

  private ByteArrayOutputStream sysOutContent;
  private ByteArrayOutputStream sysErrContent;

  RepositoriesMeta mockRepositoriesMeta;
  RepositoryMeta mockRepositoryMeta;
  Repository mockRepository;
  RepositoryDirectoryInterface mockRepositoryDirectory;

  @BeforeClass
  public static void setUpClass() throws Exception {
    KettleEnvironment.init();
  }

  @Before
  public void setUp() {
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
  public void testPanStatusCodes() {

    assertNull( CommandExecutorCodes.Pan.getByCode( 9999 ) );
    assertNotNull( CommandExecutorCodes.Pan.getByCode( 0 ) );

    assertEquals( CommandExecutorCodes.Pan.UNEXPECTED_ERROR, CommandExecutorCodes.Pan.getByCode( 2 ) );
    assertEquals( CommandExecutorCodes.Pan.CMD_LINE_PRINT, CommandExecutorCodes.Pan.getByCode( 9 ) );

    assertEquals( "The transformation ran without a problem", CommandExecutorCodes.Pan.getByCode( 0 ).getDescription() );
    assertEquals( "The transformation couldn't be loaded from XML or the Repository", CommandExecutorCodes.Pan.getByCode( 7 ).getDescription() );

    assertTrue( CommandExecutorCodes.Pan.isFailedExecution( CommandExecutorCodes.Pan.COULD_NOT_LOAD_TRANS.getCode() ) );
    assertTrue( CommandExecutorCodes.Pan.isFailedExecution( CommandExecutorCodes.Pan.ERROR_LOADING_STEPS_PLUGINS.getCode() ) );
    assertFalse( CommandExecutorCodes.Pan.isFailedExecution( CommandExecutorCodes.Pan.SUCCESS.getCode() ) );
    assertFalse( CommandExecutorCodes.Pan.isFailedExecution( CommandExecutorCodes.Pan.ERRORS_DURING_PROCESSING.getCode() ) );
  }

  @Test
  public void testConfigureParameters() throws Exception {
    TransMeta transMeta = new TransMeta();
    transMeta.addParameterDefinition( TEST_PARAM_NAME, DEFAULT_PARAM_VALUE, "This tests a default parameter" );

    assertEquals( "Default parameter was not set correctly on TransMeta",
      DEFAULT_PARAM_VALUE, transMeta.getParameterDefault( TEST_PARAM_NAME ) );

    assertEquals( "Parameter value should be blank in TransMeta", "", transMeta.getParameterValue( TEST_PARAM_NAME ) );

    Trans trans = new Trans( transMeta );

    assertEquals( "Default parameter was not set correctly on Trans",
      DEFAULT_PARAM_VALUE, trans.getParameterDefault( TEST_PARAM_NAME ) );

    assertEquals( "Parameter value should be blank in Trans", "", trans.getParameterValue( TEST_PARAM_NAME ) );

    NamedParams params = new NamedParamsDefault();
    params.addParameterDefinition( TEST_PARAM_NAME, NOT_DEFAULT_PARAM_VALUE, "This tests a non-default parameter" );
    params.setParameterValue( TEST_PARAM_NAME, NOT_DEFAULT_PARAM_VALUE );
    Pan.configureParameters( trans, params, transMeta );
    assertEquals( "Parameter was not set correctly in Trans",
      NOT_DEFAULT_PARAM_VALUE, trans.getParameterValue( TEST_PARAM_NAME ) );
    assertEquals( "Parameter was not set correctly in TransMeta",
      NOT_DEFAULT_PARAM_VALUE, transMeta.getParameterValue( TEST_PARAM_NAME ) );
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

    PanCommandExecutor testPanCommandExecutor = new PanCommandExecutorForTesting( null, null, mockRepositoriesMeta );

    origSysOut = System.out;
    origSysErr = System.err;

    try {

      System.setOut( new PrintStream( sysOutContent ) );
      System.setErr( new PrintStream( sysErrContent ) );

      Pan.setCommandExecutor( testPanCommandExecutor );
      Pan.main( new String[] { "/listrep" } );

    } catch ( SecurityException e ) {
      // All OK / expected: SecurityException is purposely thrown when Pan triggers System.exitJVM()

      System.out.println( sysOutContent );

      assertTrue( sysOutContent.toString().contains( TEST_REPO_DUMMY_NAME ) );
      assertTrue( sysOutContent.toString().contains( TEST_REPO_DUMMY_DESC ) );

      Result result = Pan.getCommandExecutor().getResult();
      assertNotNull( result );
      assertEquals( result.getExitStatus(), CommandExecutorCodes.Pan.SUCCESS.getCode() );

    } finally {
      // sanitize

      Pan.setCommandExecutor( null );

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

    PanCommandExecutor testPanCommandExecutor =
      new PanCommandExecutorForTesting( mockRepository, mockRepositoryMeta, null );

    origSysOut = System.out;
    origSysErr = System.err;

    try {

      System.setOut( new PrintStream( sysOutContent ) );
      System.setErr( new PrintStream( sysErrContent ) );

      Pan.setCommandExecutor( testPanCommandExecutor );
      // (case-insensitive) should accept either 'Y' (default) or 'true'
      Pan.main( new String[] { "/listdir:true", "/rep:test-repo", "/level:Basic" } );

    } catch ( SecurityException e ) {
      // All OK / expected: SecurityException is purposely thrown when Pan triggers System.exitJVM()

      System.out.println( sysOutContent );

      assertTrue( sysOutContent.toString().contains( DUMMY_DIR_1 ) );
      assertTrue( sysOutContent.toString().contains( DUMMY_DIR_2 ) );

      Result result = Pan.getCommandExecutor().getResult();
      assertNotNull( result );
      assertEquals( result.getExitStatus(), CommandExecutorCodes.Pan.SUCCESS.getCode() );

    } finally {
      // sanitize

      Pan.setCommandExecutor( null );

      System.setOut( origSysOut );
      System.setErr( origSysErr );
    }
  }

  @Test
  public void testListTrans() throws Exception {

    PrintStream origSysOut;
    PrintStream origSysErr;

    final String DUMMY_TRANS_1 = "test-trans-name-1";
    final String DUMMY_TRANS_2 = "test-trans-name-2";

    when( mockRepository.getTransformationNames( any(), anyBoolean() ) ).thenReturn( new String[]{ DUMMY_TRANS_1, DUMMY_TRANS_2 } );
    when( mockRepository.loadRepositoryDirectoryTree() ).thenReturn( mockRepositoryDirectory );

    PanCommandExecutor testPanCommandExecutor =
      new PanCommandExecutorForTesting( mockRepository, mockRepositoryMeta, null );

    origSysOut = System.out;
    origSysErr = System.err;

    try {

      System.setOut( new PrintStream( sysOutContent ) );
      System.setErr( new PrintStream( sysErrContent ) );

      Pan.setCommandExecutor( testPanCommandExecutor );
      // (case-insensitive) should accept either 'Y' (default) or 'true'
      Pan.main( new String[] { "/listtrans:Y", "/rep:test-repo", "/level:Basic" } );

    } catch ( SecurityException e ) {
      // All OK / expected: SecurityException is purposely thrown when Pan triggers System.exitJVM()

      System.out.println( sysOutContent );

      assertTrue( sysOutContent.toString().contains( DUMMY_TRANS_1 ) );
      assertTrue( sysOutContent.toString().contains( DUMMY_TRANS_2 ) );

      Result result = Pan.getCommandExecutor().getResult();
      assertNotNull( result );
      assertEquals( result.getExitStatus(), CommandExecutorCodes.Pan.SUCCESS.getCode() );

    } finally {
      // sanitize

      Pan.setCommandExecutor( null );

      System.setOut( origSysOut );
      System.setErr( origSysErr );
    }
  }

  private static class PanCommandExecutorForTesting extends PanCommandExecutor {

    private final Repository testRepository;
    private final RepositoryMeta testRepositoryMeta;
    private final RepositoriesMeta testRepositoriesMeta;

    public PanCommandExecutorForTesting( Repository testRepository, RepositoryMeta testRepositoryMeta,
                                         RepositoriesMeta testRepositoriesMeta ) {
      super( Pan.class );
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
