/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.kitchen;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.base.CommandExecutorCodes;
import org.pentaho.di.core.Result;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.security.Permission;
import java.util.Base64;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class KitchenIT {

  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

  private PrintStream oldOut;
  private PrintStream oldErr;
  private SecurityManager oldSecurityManager;

  private static final String JOB_EXEC_RESULT_FALSE = "(result=[false])";
  private static final String JOB_EXEC_RESULT_TRUE = "(result=[true])";
  private static final String FAILED_JOB_EXEC_PATTERN = "Finished with errors";

  private static final String BASE_PATH = "." +  File.separator + "test_kjbs" + File.separator;
  private static final String EXPECTED_COMPLETE_WITH_SUCCESS_PATH = BASE_PATH + "expected_complete_with_success";
  private static final String EXPECTED_COMPLETE_WITH_FAILURE_PATH = BASE_PATH + "expected_complete_with_failure";
  private static final String EXPECTED_PARAMS_PASSED_ALONG = BASE_PATH + "expected_params_passed_along";

  private static final String[] KTRS_EXPECTED_COMPLETE_WITH_SUCCESS = new String[] {
    "runs_well_hello_world.kjb"
  };

  private static final String[] KTRS_EXPECTED_COMPLETE_WITH_FAILURE = new String[] {
    "fail_on_exec_hello_world.kjb",
    "fail_on_prep_hello_world.kjb",
    "missing_referenced_ktr.kjb"
  };

  @Before
  public void setUp() {
    oldSecurityManager = System.getSecurityManager();
    System.setSecurityManager( new MySecurityManager( oldSecurityManager ) );
  }

  @After
  public void tearDown() {
    System.setSecurityManager( oldSecurityManager );
  }


  @Test
  public void testArchivedJobsExecution() throws Exception {
    String file = this.getClass().getResource( "test-kjb.zip" ).getFile();
    String[] args = new String[] { "/file:zip:file://" + file + "!Job.kjb" };
    oldOut = System.out;
    oldErr = System.err;
    System.setOut( new PrintStream( outContent ) );
    System.setErr( new PrintStream( errContent ) );
    try {
      Kitchen.main( args );
    } catch ( SecurityException e ) {
      System.setOut( oldOut );
      System.setErr( oldErr );
      System.out.println( outContent );
      assertFalse( outContent.toString().contains( "result=[false]" ) );
      assertFalse( outContent.toString().contains( "ERROR" ) );
    }
  }

  @Test
  public void testFileJobsExecution() throws Exception {
    String file = this.getClass().getResource( "Job.kjb" ).getFile();
    String[] args = new String[] { "/file:" + file };
    oldOut = System.out;
    oldErr = System.err;
    System.setOut( new PrintStream( outContent ) );
    System.setErr( new PrintStream( errContent ) );
    try {
      Kitchen.main( args );
    } catch ( SecurityException e ) {
      System.setOut( oldOut );
      System.setErr( oldErr );
      System.out.println( outContent );
      assertFalse( outContent.toString().contains( "result=[false]" ) );
      assertFalse( outContent.toString().contains( "ERROR" ) );
    }
  }

  @Test
  public void testFileJobExpectedExecutionWithFailure() throws Exception {

    for ( String testKJB : KTRS_EXPECTED_COMPLETE_WITH_FAILURE ) {

      long now = System.currentTimeMillis();

      String testKJBRelativePath = EXPECTED_COMPLETE_WITH_FAILURE_PATH + File.separator + testKJB;
      String testKJBFullPath = this.getClass().getResource( testKJBRelativePath ).getFile();
      String logFileRelativePath = testKJBRelativePath + "." + now + ".log";
      String logFileFullPath = testKJBFullPath + "." + now + ".log";

      try {

        Kitchen.main( new String[]{ "/file:" + testKJBFullPath, "/level:Basic", "/logfile:" + logFileFullPath } );

      } catch ( SecurityException e ) {
        // All OK / expected: SecurityException is purposely thrown when Kitchen triggers System.exitJVM()

        // get log file contents
        String logFileContent = getFileContentAsString( logFileRelativePath );

        assertTrue( logFileContent.contains( JOB_EXEC_RESULT_FALSE ) );
        assertTrue( logFileContent.contains( FAILED_JOB_EXEC_PATTERN ) );

        Result result = Kitchen.getCommandExecutor().getResult();
        assertNotNull( result );
        assertEquals( result.getExitStatus(), CommandExecutorCodes.Kitchen.ERRORS_DURING_PROCESSING.getCode() );

      } finally {
        // sanitize
        File f = new File( logFileFullPath );
        if ( f != null && f.exists() ) {
          f.deleteOnExit();
        }
      }
    }
  }

  @Test
  public void testFileJobExpectedExecutionWithSuccess() throws Exception {

    for ( String testKJB : KTRS_EXPECTED_COMPLETE_WITH_SUCCESS ) {

      long now = System.currentTimeMillis();

      String testKJBRelativePath = EXPECTED_COMPLETE_WITH_SUCCESS_PATH + File.separator + testKJB;
      String testKJBFullPath = this.getClass().getResource( testKJBRelativePath ).getFile();
      String logFileRelativePath = testKJBRelativePath + "." + now + ".log";
      String logFileFullPath = testKJBFullPath + "." + now + ".log";

      try {

        Kitchen.main( new String[]{ "/file:" + testKJBFullPath, "/level:Basic", "/logfile:" + logFileFullPath } );

      } catch ( SecurityException e ) {
        // All OK / expected: SecurityException is purposely thrown when Kitchen triggers System.exitJVM()

        // get log file contents
        String logFileContent = getFileContentAsString( logFileRelativePath );

        assertTrue( logFileContent.contains( JOB_EXEC_RESULT_TRUE ) );
        assertFalse( logFileContent.contains( FAILED_JOB_EXEC_PATTERN ) );

        Result result = Kitchen.getCommandExecutor().getResult();
        assertNotNull( result );
        assertEquals( result.getExitStatus(), CommandExecutorCodes.Kitchen.SUCCESS.getCode() );

      } finally {
        // sanitize
        File f = new File( logFileFullPath );
        if ( f != null && f.exists() ) {
          f.deleteOnExit();
        }
      }
    }
  }

  @Test
  public void testFileJobWithParams() throws Exception {

    String param1Name = "p1";
    String param2Name = "p2";

    String param1Val = UUID.randomUUID().toString();
    String param2Val = UUID.randomUUID().toString();

    final String EXPECTED_OUTPUT = "Received params " + param1Name + ":" + param1Val + ", " + param2Name + ":" + param2Val;

    long now = System.currentTimeMillis();

    String testKJBRelativePath = EXPECTED_PARAMS_PASSED_ALONG + File.separator + "print_received_params.kjb";
    String testKJBFullPath = this.getClass().getResource( testKJBRelativePath ).getFile();
    String logFileRelativePath = testKJBRelativePath + "." + now + ".log";
    String logFileFullPath = testKJBFullPath + "." + now + ".log";

    try {

      Kitchen.main( new String[] {
        "/file:" + testKJBFullPath,
        "/level:Basic",
        "/logfile:" + logFileFullPath,
        "/param:" + param1Name + "=" + param1Val,
        "/param:" + param2Name + "=" + param2Val
      } );

    } catch ( SecurityException e ) {
      // All OK / expected: SecurityException is purposely thrown when Pan triggers System.exitJVM()

      // get log file contents
      String logFileContent = getFileContentAsString( logFileRelativePath );

      assertTrue( logFileContent.contains( EXPECTED_OUTPUT ) );
      assertTrue( logFileContent.contains( JOB_EXEC_RESULT_TRUE ) );
      assertFalse( logFileContent.contains( FAILED_JOB_EXEC_PATTERN ) );

      Result result = Kitchen.getCommandExecutor().getResult();
      assertNotNull( result );
      assertEquals( result.getExitStatus(), CommandExecutorCodes.Kitchen.SUCCESS.getCode() );

    } finally {
      // sanitize
      File f = new File( logFileFullPath );
      if ( f != null && f.exists() ) {
        f.deleteOnExit();
      }
    }
  }

  private String getFileContentAsString( String relativePath ) {

    String content = "";

    try {
      BufferedReader rd = new BufferedReader(
              new InputStreamReader( this.getClass().getResourceAsStream( relativePath ) ) );
      StringBuffer logFileContentBuffer = new StringBuffer();
      String line = "";
      while ( ( line = rd.readLine() ) != null ) {
        logFileContentBuffer.append( line ).append( "\n" );
      }

      content = logFileContentBuffer.toString();

    } catch ( Throwable t ) {
      // no-op
    }

    return content;
  }

  @Test
  public void testJobExecutionFromWithinProvidedZip() throws Exception {

    String testZipRelativePath = "test-kjb.zip";
    String testKJBWithinZip = "Job.kjb";

    File zipFile = null;

    try {
      zipFile = new File( getClass().getResource( testZipRelativePath ).toURI() );
      String base64Zip = Base64.getEncoder().encodeToString( FileUtils.readFileToByteArray( zipFile ) );

      Kitchen.main( new String[] {
        "/file:" + testKJBWithinZip,
        "/level:Basic",
        "/zip:" + base64Zip } );

    } catch ( SecurityException e ) {
      // All OK / expected: SecurityException is purposely thrown when Kitchen triggers System.exitJVM()
      Result result = Kitchen.getCommandExecutor().getResult();
      assertNotNull( result );
      assertEquals( CommandExecutorCodes.Kitchen.SUCCESS.getCode(), result.getExitStatus() );
    }
  }

  public class MySecurityManager extends SecurityManager {

    private SecurityManager baseSecurityManager;

    public MySecurityManager( SecurityManager baseSecurityManager ) {
      this.baseSecurityManager = baseSecurityManager;
    }

    @Override
    public void checkPermission( Permission permission ) {
      if ( permission.getName().startsWith( "exitVM" ) ) {
        throw new SecurityException( "System exit not allowed" );
      }
      if ( baseSecurityManager != null ) {
        baseSecurityManager.checkPermission( permission );
      } else {
        return;
      }
    }

  }
}
