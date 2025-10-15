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

package org.pentaho.di.core.ssh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for ExecResult class.
 * Tests all constructors, getters, and edge cases.
 */
public class ExecResultTest {

  @Test
  public void testConstructorWithStdoutStderrExitCode() {
    String stdout = "output";
    String stderr = "error";
    int exitCode = 0;

    ExecResult result = new ExecResult( stdout, stderr, exitCode );

    assertEquals( stdout, result.getStdout() );
    assertEquals( stderr, result.getStderr() );
    assertEquals( exitCode, result.getExitCode() );
    assertEquals( "outputerror", result.getCombined() );
    assertFalse( result.isError() );
    assertTrue( result.hasErrorOutput() );
  }

  @Test
  public void testConstructorWithAllParameters() {
    String stdout = "success";
    String stderr = "warning";
    String combined = "combined output";
    int exitCode = 1;
    boolean error = true;

    ExecResult result = new ExecResult( stdout, stderr, combined, exitCode, error );

    assertEquals( stdout, result.getStdout() );
    assertEquals( stderr, result.getStderr() );
    assertEquals( combined, result.getCombined() );
    assertEquals( exitCode, result.getExitCode() );
    assertTrue( result.isError() );
    assertTrue( result.hasErrorOutput() );
  }

  @Test
  public void testSuccessfulExecution() {
    ExecResult result = new ExecResult( "command succeeded", null, 0 );

    assertEquals( 0, result.getExitCode() );
    assertFalse( result.isError() );
    assertFalse( result.hasErrorOutput() );
    assertEquals( "command succeeded", result.getStdout() );
    assertEquals( "command succeeded", result.getCombined() );
  }

  @Test
  public void testFailedExecution() {
    ExecResult result = new ExecResult( "", "command failed", 1 );

    assertEquals( 1, result.getExitCode() );
    assertTrue( result.isError() );
    assertTrue( result.hasErrorOutput() );
    assertEquals( "command failed", result.getStderr() );
    assertEquals( "command failed", result.getCombined() );
  }

  @Test
  public void testNullStderr() {
    ExecResult result = new ExecResult( "output", null, 0 );

    assertEquals( "output", result.getStdout() );
    assertEquals( null, result.getStderr() );
    assertEquals( "output", result.getCombined() );
    assertFalse( result.hasErrorOutput() );
  }

  @Test
  public void testEmptyStderr() {
    ExecResult result = new ExecResult( "output", "", 0 );

    assertEquals( "", result.getStderr() );
    assertFalse( result.hasErrorOutput() );
  }

  @Test
  public void testNonZeroExitCodeCausesError() {
    ExecResult result = new ExecResult( "done", "warning", 127 );

    assertEquals( 127, result.getExitCode() );
    assertTrue( result.isError() );
  }

  @Test
  public void testZeroExitCodeNoError() {
    ExecResult result = new ExecResult( "done", "some info", 0 );

    assertEquals( 0, result.getExitCode() );
    assertFalse( result.isError() );
  }

  @Test
  public void testCombinedOutputWithBothStreams() {
    String stdout = "line1\n";
    String stderr = "error1\n";

    ExecResult result = new ExecResult( stdout, stderr, 0 );

    assertEquals( "line1\nerror1\n", result.getCombined() );
  }

  @Test
  public void testCustomErrorFlagOverridesExitCode() {
    // Exit code is 0, but error flag is set to true
    ExecResult result = new ExecResult( "output", "stderr", "combined", 0, true );

    assertEquals( 0, result.getExitCode() );
    assertTrue( result.isError() );
  }

  @Test
  public void testErrorFlagCanBeFalseWithNonZeroExitCode() {
    // Exit code is non-zero, but error flag is false
    ExecResult result = new ExecResult( "output", "stderr", "combined", 1, false );

    assertEquals( 1, result.getExitCode() );
    assertFalse( result.isError() );
  }

  @Test
  public void testNegativeExitCode() {
    ExecResult result = new ExecResult( "timeout", "process killed", -1 );

    assertEquals( -1, result.getExitCode() );
    assertTrue( result.isError() );
  }

  @Test
  public void testLargeOutput() {
    StringBuilder largeOutput = new StringBuilder();
    for ( int i = 0; i < 10000; i++ ) {
      largeOutput.append( "line " ).append( i ).append( "\n" );
    }
    String output = largeOutput.toString();

    ExecResult result = new ExecResult( output, null, 0 );

    assertEquals( output, result.getStdout() );
    assertEquals( output, result.getCombined() );
  }
}
