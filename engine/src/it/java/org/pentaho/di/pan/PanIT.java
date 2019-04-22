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
package org.pentaho.di.pan;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.base.CommandExecutorCodes;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.security.Permission;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PanIT {

  private static final String FAILED_TO_INITIALIZE_ERROR_PATTERN = "failed to initialize!";

  private static final String FINISHED_PROCESSING_ERROR_COUNT_REGEX = "(.*)\\sFinished processing\\s(.*)(E=){1}(.*)\\)";
  private static final Pattern FINISHED_PROCESSING_ERROR_COUNT_PATTERN = Pattern.compile( FINISHED_PROCESSING_ERROR_COUNT_REGEX );

  private static final String BASE_PATH = "." +  File.separator + "test_ktrs" + File.separator;
  private static final String EXPECTED_COMPLETE_WITH_SUCCESS_PATH = BASE_PATH + "expected_complete_with_success";
  private static final String EXPECTED_COMPLETE_WITH_FAILURE_PATH = BASE_PATH + "expected_complete_with_failure";

  private static final String[] KTRS_EXPECTED_COMPLETE_WITH_SUCCESS = new String[] {
    "runs_well_hello_world.ktr"
  };

  private static final Map<String, Integer> KTRS_TO_FAIL = new HashMap<>( 3 );
  {
    // runtime fail on validate step
    KTRS_TO_FAIL.put( "fail_on_exec_hello_world.ktr", CommandExecutorCodes.Pan.ERRORS_DURING_PROCESSING.getCode() );
    // missing db on table input, caught on init
    KTRS_TO_FAIL.put( "fail_on_exec_2_hello_world.ktr", CommandExecutorCodes.Pan.UNABLE_TO_PREP_INIT_TRANS.getCode() );
    // unconfigured kafka consumer, caught on init
    KTRS_TO_FAIL.put( "fail_on_prep_hello_world.ktr", CommandExecutorCodes.Pan.UNABLE_TO_PREP_INIT_TRANS.getCode() );
  };

  private SecurityManager oldSecurityManager;

  @Before
  public void setUp() throws KettleException {
    KettleEnvironment.init();
    oldSecurityManager = System.getSecurityManager();
    System.setSecurityManager( new PanIT.MySecurityManager( oldSecurityManager ) );
  }

  @After
  public void tearDown() {
    System.setSecurityManager( oldSecurityManager );
  }


  @Test
  public void testArchivedTransExecution() throws Exception {

    long now = System.currentTimeMillis();

    String testKTRRelativePath = "test-ktr.zip";
    String testKTRFullPath = getRelativePathKTR( testKTRRelativePath );
    String logFileRelativePath = testKTRRelativePath + "." + now + ".log";
    String logFileFullPath = testKTRFullPath + "." + now + ".log";

    try {

      Pan.main( new String[] { "/file:zip:file://" + testKTRFullPath + "!Pan.ktr", "/level:Basic", "/logfile:" + logFileFullPath } );

    } catch ( SecurityException e ) {
      // All OK / expected: SecurityException is purposely thrown when Pan triggers System.exitJVM()

      // get log file contents
      String logFileContent = getFileContentAsString( logFileRelativePath );

      // use FINISHED_PROCESSING_ERROR_COUNT_REGEX to get execution error count
      int errorCount = parseErrorCount( logFileContent );

      assertTrue( !logFileContent.contains( FAILED_TO_INITIALIZE_ERROR_PATTERN ) &&  errorCount == 0 );

      Result result = Pan.getCommandExecutor().getResult();
      assertNotNull( result );
      assertEquals( CommandExecutorCodes.Pan.SUCCESS.getCode(), result.getExitStatus() );

    } finally {
      // sanitize
      File f = new File( logFileFullPath );
      if ( f != null && f.exists() ) {
        f.deleteOnExit();
      }
    }
  }

  @Test
  public void testFileTransExecution() throws Exception {

    long now = System.currentTimeMillis();

    String testKTRRelativePath = "Pan.ktr";
    String testKTRFullPath = getRelativePathKTR( testKTRRelativePath );
    String logFileRelativePath = testKTRRelativePath + "." + now + ".log";
    String logFileFullPath = testKTRFullPath + "." + now + ".log";

    try {

      Pan.main( new String[] { "/file:" + testKTRFullPath, "/level:Basic", "/logfile:" + logFileFullPath } );

    } catch ( SecurityException e ) {
      // All OK / expected: SecurityException is purposely thrown when Pan triggers System.exitJVM()

      // get log file contents
      String logFileContent = getFileContentAsString( logFileRelativePath );

      // use FINISHED_PROCESSING_ERROR_COUNT_REGEX to get execution error count
      int errorCount = parseErrorCount( logFileContent );

      assertTrue( !logFileContent.contains( FAILED_TO_INITIALIZE_ERROR_PATTERN ) &&  errorCount == 0 );

      Result result = Pan.getCommandExecutor().getResult();
      assertNotNull( result );
      assertEquals( CommandExecutorCodes.Pan.SUCCESS.getCode(), result.getExitStatus() );

    } finally {
      // sanitize
      File f = new File( logFileFullPath );
      if ( f != null && f.exists() ) {
        f.deleteOnExit();
      }
    }
  }

  @Test
  public void testTransExecutionFromWithinProvidedZip() throws Exception {

    long now = System.currentTimeMillis();

    String testZipRelativePath = "test-ktr.zip";
    String testZipFullPath = getRelativePathKTR( testZipRelativePath );

    String testKTRWithinZip = "Pan.ktr";

    String logFileRelativePath = testZipRelativePath + "." + now + ".log";
    String logFileFullPath = testZipFullPath + "." + now + ".log";

    File zipFile = null;

    try {
      zipFile = new File( getClass().getResource( testZipRelativePath ).toURI() );
      String base64Zip = Base64.getEncoder().encodeToString( FileUtils.readFileToByteArray( zipFile ) );

      Pan.main( new String[] {
        "/file:" + testKTRWithinZip,
        "/level:Basic",
        "/logfile:" + logFileFullPath,
        "/zip:" + base64Zip } );

    } catch ( SecurityException e ) {
      // All OK / expected: SecurityException is purposely thrown when Pan triggers System.exitJVM()

      // get log file contents
      String logFileContent = getFileContentAsString( logFileRelativePath );

      // use FINISHED_PROCESSING_ERROR_COUNT_REGEX to get execution error count
      int errorCount = parseErrorCount( logFileContent );

      assertTrue( !logFileContent.contains( FAILED_TO_INITIALIZE_ERROR_PATTERN ) && errorCount == 0 );

      Result result = Pan.getCommandExecutor().getResult();
      assertNotNull( result );
      assertEquals( CommandExecutorCodes.Pan.SUCCESS.getCode(), result.getExitStatus() );

    } finally {
      zipFile = null;

      // sanitize
      File f = new File( logFileFullPath );
      if ( f != null && f.exists() ) {
        f.deleteOnExit();
      }
    }
  }

  @Test
  public void testFileTransExpectedExecutionWithFailure() throws Exception {

    for ( Map.Entry<String, Integer> failure : KTRS_TO_FAIL.entrySet() ) {

      long now = System.currentTimeMillis();
      final String testKTR = failure.getKey();
      String testKTRRelativePath = EXPECTED_COMPLETE_WITH_FAILURE_PATH + File.separator + testKTR;
      String testKTRFullPath = getRelativePathKTR( testKTRRelativePath );
      String logFileRelativePath = testKTRRelativePath + "." + now + ".log";
      String logFileFullPath = testKTRFullPath + "." + now + ".log";

      try {

        Pan.main( new String[]{ "/file:" + testKTRFullPath, "/level:Basic", "/logfile:" + logFileFullPath } );

      } catch ( SecurityException e ) {
        // All OK / expected: SecurityException is purposely thrown when Pan triggers System.exitJVM()

        // get log file contents
        String logFileContent = getFileContentAsString( logFileRelativePath );

        // use FINISHED_PROCESSING_ERROR_COUNT_REGEX to get execution error count
        int errorCount = parseErrorCount( logFileContent );

        assertTrue( logFileContent.contains( FAILED_TO_INITIALIZE_ERROR_PATTERN ) ||  errorCount > 0 );

        Result result = Pan.getCommandExecutor().getResult();
        assertNotNull( result );
        assertEquals( testKTR + " error code", (int) failure.getValue(), result.getExitStatus() );

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
  public void testFileTransExpectedExecutionWithSuccess() throws Exception {

    for ( String testKTR : KTRS_EXPECTED_COMPLETE_WITH_SUCCESS ) {

      long now = System.currentTimeMillis();

      String testKTRRelativePath = EXPECTED_COMPLETE_WITH_SUCCESS_PATH + File.separator + testKTR;
      String testKTRFullPath = getRelativePathKTR( testKTRRelativePath );
      String logFileRelativePath = testKTRRelativePath + "." + now + ".log";
      String logFileFullPath = testKTRFullPath + "." + now + ".log";

      try {

        Pan.main( new String[]{ "/file:" + testKTRFullPath, "/level:Basic", "/logfile:" + logFileFullPath } );

      } catch ( SecurityException e ) {
        // All OK / expected: SecurityException is purposely thrown when Pan triggers System.exitJVM()

        // get log file contents
        String logFileContent = getFileContentAsString( logFileRelativePath );

        // use FINISHED_PROCESSING_ERROR_COUNT_REGEX to get execution error count
        int errorCount = parseErrorCount( logFileContent );

        assertTrue( !logFileContent.contains( FAILED_TO_INITIALIZE_ERROR_PATTERN ) &&  errorCount == 0 );

        Result result = Pan.getCommandExecutor().getResult();
        assertNotNull( result );
        assertEquals( CommandExecutorCodes.Pan.SUCCESS.getCode(), result.getExitStatus() );

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
  public void testFileTransWithParams() throws Exception {

    String param1Name = "p1";
    String param2Name = "p2";

    String param1Val = UUID.randomUUID().toString();
    String param2Val = UUID.randomUUID().toString();

    final String EXPECTED_OUTPUT = "Received params " + param1Name + ":" + param1Val + ", " + param2Name + ":" + param2Val;

    long now = System.currentTimeMillis();

    String testKTRRelativePath = "print_received_params.ktr";
    String testKTRFullPath = getRelativePathKTR( testKTRRelativePath );
    String logFileRelativePath = testKTRRelativePath + "." + now + ".log";
    String logFileFullPath = testKTRFullPath + "." + now + ".log";

    try {

      Pan.main( new String[] {
        "/file:" + testKTRFullPath,
        "/level:Basic",
        "/logfile:" + logFileFullPath,
        "/param:" + param1Name + "=" + param1Val,
        "/param:" + param2Name + "=" + param2Val
      } );

    } catch ( SecurityException e ) {
      // All OK / expected: SecurityException is purposely thrown when Pan triggers System.exitJVM()

      // get log file contents
      String logFileContent = getFileContentAsString( logFileRelativePath );

      // use FINISHED_PROCESSING_ERROR_COUNT_REGEX to get execution error count
      int errorCount = parseErrorCount( logFileContent );

      assertTrue( !logFileContent.contains( FAILED_TO_INITIALIZE_ERROR_PATTERN ) && errorCount == 0 );
      assertTrue( logFileContent.contains( EXPECTED_OUTPUT ) );

      Result result = Pan.getCommandExecutor().getResult();
      assertNotNull( result );
      assertEquals( CommandExecutorCodes.Pan.SUCCESS.getCode(), result.getExitStatus() );


    } finally {
      // sanitize
      File f = new File( logFileFullPath );
      if ( f != null && f.exists() ) {
        f.deleteOnExit();
      }
    }
  }

  /**
   * Tests that step 'Mapping (sub-transformation)' successfully passes parameters to the transformation.
   * @throws Exception
   */
  @Test
  public void testFileTransMappingSubParameters() throws Exception {

    /* NOTE:
     * 'tr_main_local.ktr' should call 'tr_child.ktr'. The parameter that specifies 'tr_child.ktr' is specified in
     *  'Parameters' tab in the step in 'tr_main_local.ktr' ;
     */

    long now = System.currentTimeMillis();

    String testKTRRelativePath = "test_parameters/mapping/tr_main_local.ktr";
    String testKTRFullPath = getRelativePathKTR( testKTRRelativePath );
    String logFileRelativePath = testKTRRelativePath + "." + now + ".log";
    String logFileFullPath = testKTRFullPath + "." + now + ".log";

    try {

      Pan.main( new String[] { "/file:" + testKTRFullPath, "/level:Basic", "/logfile:" + logFileFullPath } );

    } catch ( SecurityException e ) {
      // All OK / expected: SecurityException is purposely thrown when Pan triggers System.exitJVM()

      // get log file contents
      String logFileContent = getFileContentAsString( logFileRelativePath );

      // use FINISHED_PROCESSING_ERROR_COUNT_REGEX to get execution error count
      int errorCount = parseErrorCount( logFileContent );

      assertTrue( !logFileContent.contains( FAILED_TO_INITIALIZE_ERROR_PATTERN ) &&  errorCount == 0 );

      Result result = Pan.getCommandExecutor().getResult();
      assertNotNull( result );
      assertEquals( CommandExecutorCodes.Pan.SUCCESS.getCode(), result.getExitStatus() );

    } finally {
      // sanitize
      File f = new File( logFileFullPath );
      if ( f != null && f.exists() ) {
        f.deleteOnExit();
      }
    }
  }

  /**
   * Tests that step 'Transformation Executor' successfully passes parameters to the transformation.
   * @throws Exception
   */
  @Test
  public void testFileTransExecutorSubParameters() throws Exception {

    /* NOTE:
     * '03_Trans_to_TransExec.ktr' should call 'transformation_value.ktr'. The log of '' should contain the snippet
     * Write to log.0 - DATE_ID = 2018-04-01, where DATE_ID is passed as a parameter from step 'Transformation Executor'
     * with a value specified from step 'Generate Rows'.
     */

    long now = System.currentTimeMillis();

    String testKTRRelativePath = "test_parameters/executor/03_Trans_to_TransExec.ktr";
    String testKTRFullPath = getRelativePathKTR( testKTRRelativePath );
    String logFileRelativePath = testKTRRelativePath + "." + now + ".log";
    String logFileFullPath = testKTRFullPath + "." + now + ".log";

    try {

      Pan.main( new String[] { "/file:" + testKTRFullPath, "/level:Basic", "/logfile:" + logFileFullPath } );

    } catch ( SecurityException e ) {
      // All OK / expected: SecurityException is purposely thrown when Pan triggers System.exitJVM()

      // get log file contents
      String logFileContent = getFileContentAsString( logFileRelativePath );

      // use FINISHED_PROCESSING_ERROR_COUNT_REGEX to get execution error count
      int errorCount = parseErrorCount( logFileContent );

      assertTrue( !logFileContent.contains( FAILED_TO_INITIALIZE_ERROR_PATTERN ) &&  errorCount == 0 );
      assertTrue( logFileContent.contains( "DATE_ID = 2018-04-01" ) ); // most significant assert

      Result result = Pan.getCommandExecutor().getResult();
      assertNotNull( result );
      assertEquals( CommandExecutorCodes.Pan.SUCCESS.getCode(), result.getExitStatus() );

    } finally {
      // sanitize
      File f = new File( logFileFullPath );
      if ( f != null && f.exists() ) {
        f.deleteOnExit();
      }
    }
  }

  /**
   * Get file contentsf from absolute path.
   * @param relativePath
   * @return
   */
  private String getFileContentAsString( String relativePath ) {

    String content = "";

    try {
      BufferedReader rd = new BufferedReader(
        new InputStreamReader( this.getClass().getResourceAsStream( relativePath ) ) );
      StringBuffer logFileContentBuffer = new StringBuffer();
      String line;
      while ( ( line = rd.readLine() ) != null ) {
        logFileContentBuffer.append( line ).append( "\n" );
      }

      content = logFileContentBuffer.toString();

    } catch ( Throwable t ) {
      // no-op
    }

    return content;
  }


  private int parseErrorCount( String text ) {

    int maxErrorCount = 0;

    if ( !Utils.isEmpty( text ) ) {

      // its common to have more than one "Finished processing (I=X, O=X, R=X, W=X, U=X, E=X)" throughout the output
      // the one we care for is the one that displays the highest error count; usually that ends up being the last one
      Matcher m = FINISHED_PROCESSING_ERROR_COUNT_PATTERN.matcher( text );
      while ( m.find() ) {

        if ( m.groupCount() > 3 ) {

          try {
            int errorCount = Integer.parseInt( m.group( 4 ) );

            if ( errorCount > maxErrorCount ) {
              maxErrorCount = errorCount;
            }
          } catch ( NumberFormatException nfe ) {
            // no-op
          }
        }
      }
    }
    return maxErrorCount;
  }

  /**
   * Get relative path to KTR
   * @param ktrRelativePath 'relative' to test file package directory
   * @return
   */
  private String getRelativePathKTR( String ktrRelativePath ) {
    //return new File( "./src/it/resources/org/pentaho/di/pan/" + ktrRelativePath ).getAbsolutePath();
    return this.getClass().getResource( ktrRelativePath ).getPath();
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
