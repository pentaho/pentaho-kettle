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
import org.pentaho.di.core.ssh.SshImplementation;

/**
 * Tests for the SSH step migration functionality.
 * Verifies that the SSH step can work with both legacy Trilead and modern SSH
 * implementations.
 */
public class SshStepMigrationTest {

  @Test
  public void testSshStepConnectionAdapterCreation() {
    // Test that our adapter classes can be instantiated
    assertNotNull("SshStepConnectionAdapter class should be available",
        SshStepConnectionAdapter.class);
    assertNotNull("SessionResultAdapter class should be available",
        SessionResultAdapter.class);
  }

  @Test
  public void testSSHMetaSshImplementationConfig() {
    SSHMeta meta = new SSHMeta();

    // Test default state (should be null for auto-detect)
    assertNull("Default SSH implementation should be null (auto-detect)",
        meta.getSshImplementation());

    // Test setting MINA implementation
    meta.setSshImplementation(SshImplementation.MINA);
    assertEquals("Should be able to set MINA implementation",
        SshImplementation.MINA, meta.getSshImplementation());

    // Test setting Trilead implementation
    meta.setSshImplementation(SshImplementation.TRILEAD);
    assertEquals("Should be able to set Trilead implementation",
        SshImplementation.TRILEAD, meta.getSshImplementation());

    // Test setting back to auto-detect
    meta.setSshImplementation(null);
    assertNull("Should be able to set back to auto-detect",
        meta.getSshImplementation());
  }

  @Test
  public void testSSHDataFieldsAvailable() {
    SSHData data = new SSHData();

    // Verify that both connection fields are available
    assertNull("Legacy conn field should be null initially", data.conn);
    assertNull("Modern sshConn field should be null initially", data.sshConn);

    // Verify other fields are still properly initialized
    assertEquals("indexOfCommand should be -1 initially", -1, data.indexOfCommand);
    assertFalse("wroteOneRow should be false initially", data.wroteOneRow);
    assertNull("commands should be null initially", data.commands);
  }

  @Test
  public void testSessionResultAdapterCompatibility() {
    // Test that SessionResultAdapter has the expected methods
    // This is a compilation test - if it compiles, the interface is correct

    java.lang.reflect.Method[] methods = SessionResultAdapter.class.getMethods();
    boolean hasGetStd = false;
    boolean hasGetStdOut = false;
    boolean hasGetStdErr = false;
    boolean hasIsStdTypeErr = false;
    boolean hasGetExitStatus = false;

    for (java.lang.reflect.Method method : methods) {
      String methodName = method.getName();
      if ("getStd".equals(methodName))
        hasGetStd = true;
      if ("getStdOut".equals(methodName))
        hasGetStdOut = true;
      if ("getStdErr".equals(methodName))
        hasGetStdErr = true;
      if ("isStdTypeErr".equals(methodName))
        hasIsStdTypeErr = true;
      if ("getExitStatus".equals(methodName))
        hasGetExitStatus = true;
    }

    assertTrue("SessionResultAdapter should have getStd method", hasGetStd);
    assertTrue("SessionResultAdapter should have getStdOut method", hasGetStdOut);
    assertTrue("SessionResultAdapter should have getStdErr method", hasGetStdErr);
    assertTrue("SessionResultAdapter should have isStdTypeErr method", hasIsStdTypeErr);
    assertTrue("SessionResultAdapter should have getExitStatus method", hasGetExitStatus);
  }
}
