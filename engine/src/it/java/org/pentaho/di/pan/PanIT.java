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

import java.io.*;
import java.net.URL;
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

    long now = System.currentTimeMillis();

    String testKTRRelativePath = "." + File.separator + "test-ktr.zip";
    String testKTRFullPath = this.getClass().getResource( testKTRRelativePath ).getFile();
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

    String testKTRRelativePath = "." + File.separator + "Pan.ktr";
    String testKTRFullPath = this.getClass().getResource( testKTRRelativePath ).getFile();
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

    } finally {
      // sanitize
      File f = new File( logFileFullPath );
      if ( f != null && f.exists() ) {
        f.deleteOnExit();
      }
    }
  }

  @Test
  public void testFileTransExpectedExecutionWithFailure() throws Exception {

    for ( String testKTR : KTRS_EXPECTED_COMPLETE_WITH_FAILURE ) {

      long now = System.currentTimeMillis();

      String testKTRRelativePath = EXPECTED_COMPLETE_WITH_FAILURE_PATH + File.separator + testKTR;
      String testKTRFullPath = this.getClass().getResource( testKTRRelativePath ).getFile();
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
      String testKTRFullPath = this.getClass().getResource( testKTRRelativePath ).getFile();
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

    String testKTRRelativePath = "./print_received_params.ktr";
    String testKTRFullPath = this.getClass().getResource( testKTRRelativePath ).getFile();
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
