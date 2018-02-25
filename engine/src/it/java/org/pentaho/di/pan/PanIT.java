/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.FileUtil;
import org.pentaho.di.core.util.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Permission;
import java.util.Enumeration;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
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

  private static final String[] KTRS_EXPECTED_COMPLETE_WITH_FAILURE = new String[] {
          "fail_on_exec_hello_world.ktr",
          "fail_on_exec_2_hello_world.ktr",
          "fail_on_prep_hello_world.ktr"
  };

  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

  private PrintStream oldOut;
  private PrintStream oldErr;
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
    String file = this.getClass().getResource( "test-ktr.zip" ).getFile();
    String[] args = new String[] { "/file:zip:file://" + file + "!Pan.ktr" };
    oldOut = System.out;
    oldErr = System.err;
    System.setOut( new PrintStream( outContent ) );
    System.setErr( new PrintStream( errContent ) );
    try {
      Pan.main( args );
    } catch ( SecurityException e ) {
      System.setOut( oldOut );
      System.setErr( oldErr );
      System.out.println( outContent );
      assertFalse( outContent.toString().contains( "error" ) );
      assertFalse( outContent.toString().contains( "stopped" ) );
    }
  }

  @Test
  public void testFileTransExecution() throws Exception {
    String file = this.getClass().getResource( "Pan.ktr" ).getFile();
    String[] args = new String[] { "/file:" + file };
    oldOut = System.out;
    oldErr = System.err;
    System.setOut( new PrintStream( outContent ) );
    System.setErr( new PrintStream( errContent ) );
    try {
      Pan.main( args );
    } catch ( SecurityException e ) {
      System.setOut( oldOut );
      System.setErr( oldErr );
      System.out.println( outContent );
      assertFalse( outContent.toString().contains( "error" ) );
      assertFalse( outContent.toString().contains( "stopped" ) );
    }
  }

  @Test
  public void testFileTransExpectedExecutionWithFailure() throws Exception {

    for ( String testKTR : KTRS_EXPECTED_COMPLETE_WITH_FAILURE ) {

      String testKTRPath = EXPECTED_COMPLETE_WITH_FAILURE_PATH + File.separator + testKTR;
      String testKTRFilePath = this.getClass().getResource( testKTRPath ).getFile();
      String logFilePath = testKTRFilePath.replace( ".ktr" , "." + System.currentTimeMillis() ) + ".log";

      try {

        Pan.main( new String[]{ "/file:" + testKTRFilePath, "/level:Basic", "/logfile:" + logFilePath } );

      } catch ( SecurityException e ) {
        // All OK / expected: SecurityException is purposely thrown when Pan triggers System.exitJVM()

        // get log file contents
        String logFileContent = new String( Files.readAllBytes( Paths.get( logFilePath ) ) );

        // use FINISHED_PROCESSING_ERROR_COUNT_REGEX to get execution error count
        int errorCount = parseErrorCount( logFileContent );

        assertTrue( logFileContent.contains( FAILED_TO_INITIALIZE_ERROR_PATTERN ) ||  errorCount > 0 );

      } finally {
        // sanitize
        new File( logFilePath ).deleteOnExit();
      }
    }
  }

  @Test
  public void testFileTransExpectedExecutionWithSuccess() throws Exception {

    for ( String testKTR : KTRS_EXPECTED_COMPLETE_WITH_SUCCESS ) {

      String testKTRPath = EXPECTED_COMPLETE_WITH_SUCCESS_PATH + File.separator + testKTR;
      String testKTRFilePath = this.getClass().getResource( testKTRPath ).getFile();
      String logFilePath = testKTRFilePath.replace( ".ktr" , "." + System.currentTimeMillis() ) + ".log";

      try {

        Pan.main( new String[]{ "/file:" + testKTRFilePath, "/level:Basic", "/logfile:" + logFilePath } );

      } catch ( SecurityException e ) {
        // All OK / expected: SecurityException is purposely thrown when Pan triggers System.exitJVM()

        // get log file contents
        String logFileContent = new String( Files.readAllBytes( Paths.get( logFilePath ) ) );

        // use FINISHED_PROCESSING_ERROR_COUNT_REGEX to get execution error count
        int errorCount = parseErrorCount( logFileContent );

        assertTrue( !logFileContent.contains( FAILED_TO_INITIALIZE_ERROR_PATTERN ) &&  errorCount == 0 );

      } finally {
        // sanitize
        new File( logFilePath ).deleteOnExit();
      }
    }
  }

  @Test
  public void testFileTransWithParams() throws Exception {

    String param1Name = "p1";
    String param2Name = "p2";

    String param1Val = UUID.randomUUID().toString();
    String param2Val = UUID.randomUUID().toString();

    String testKTRFilePath = this.getClass().getResource( "print_received_params.ktr" ).getFile();
    String logFilePath = testKTRFilePath.replace( ".ktr" , "." + System.currentTimeMillis() ) + ".log";


    try {

      Pan.main( new String[] {
              "/file:" + testKTRFilePath,
              "/level:Basic",
              "/logfile:" + logFilePath,
              "/param:" + param1Name + "=" + param1Val,
              "/param:" + param2Name + "=" + param2Val
      } );

    } catch ( SecurityException e ) {
      // All OK / expected: SecurityException is purposely thrown when Pan triggers System.exitJVM()

      // get log file contents
      String logFileContent = new String( Files.readAllBytes( Paths.get( logFilePath ) ) );

      // use FINISHED_PROCESSING_ERROR_COUNT_REGEX to get execution error count
      int errorCount = parseErrorCount( logFileContent );

      assertTrue( !logFileContent.contains( FAILED_TO_INITIALIZE_ERROR_PATTERN ) && errorCount == 0 );

      String expectedOutput = "Received params " + param1Name + ":" + param1Val + ", " + param2Name + ":" + param2Val;

      assertTrue( logFileContent.contains( expectedOutput ) );

    } finally {
      // sanitize
      new File( logFilePath ).deleteOnExit();
    }
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
