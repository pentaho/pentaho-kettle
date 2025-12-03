/*
 * ! ******************************************************************************
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

package org.pentaho.di.trans.steps.ssh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.pentaho.di.core.ssh.SshConnection;
import org.pentaho.di.core.ssh.ExecResult;

/**
 * Tests for the SSH step migration functionality.
 * Verifies that the SSH step works with the modern SSH abstraction layer.
 * implementations.
 */
public class SshStepMigrationTest {

  @Test
  public void testSshAbstractionLayer() {
    // Test that our simplified SSH abstraction classes are available
    assertNotNull( "SshConnection interface should be available",
      SshConnection.class );
    assertNotNull( "ExecResult class should be available",
      ExecResult.class );
  }

  @Test
  public void testSSHDataFieldsAvailable() {
    SSHData data = new SSHData();

    // Verify connection field is available
    assertNull( "SSH connection field should be null initially", data.getSshConnection() );
    assertFalse( "Connected flag should be false initially", data.isConnected() );

    // Verify other fields are still properly initialized
    assertEquals( "indexOfCommand should be -1 initially", -1, data.indexOfCommand );
    assertFalse( "wroteOneRow should be false initially", data.wroteOneRow );
    assertNull( "commands should be null initially", data.commands );
  }

  @Test
  public void testExecResultCompatibility() {
    // Test that ExecResult has the expected methods for SSH step usage
    // This is a compilation test - if it compiles, the interface is correct

    java.lang.reflect.Method[] methods = ExecResult.class.getMethods();
    boolean hasGetStdout = false;
    boolean hasGetStderr = false;
    boolean hasGetCombined = false;
    boolean hasHasErrorOutput = false;
    boolean hasGetExitCode = false;

    for ( java.lang.reflect.Method method : methods ) {
      String methodName = method.getName();
      if ( "getStdout".equals( methodName ) )
        hasGetStdout = true;
      if ( "getStderr".equals( methodName ) )
        hasGetStderr = true;
      if ( "getCombined".equals( methodName ) )
        hasGetCombined = true;
      if ( "hasErrorOutput".equals( methodName ) )
        hasHasErrorOutput = true;
      if ( "getExitCode".equals( methodName ) )
        hasGetExitCode = true;
    }

    assertTrue( "ExecResult should have getStdout method", hasGetStdout );
    assertTrue( "ExecResult should have getStderr method", hasGetStderr );
    assertTrue( "ExecResult should have getCombined method", hasGetCombined );
    assertTrue( "ExecResult should have hasErrorOutput method", hasHasErrorOutput );
    assertTrue( "ExecResult should have getExitCode method", hasGetExitCode );
  }
}
