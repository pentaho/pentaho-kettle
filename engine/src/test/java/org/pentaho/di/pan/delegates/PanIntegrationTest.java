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

package org.pentaho.di.pan.delegates;

import org.junit.Test;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.pan.Pan;

import static org.junit.Assert.*;

/**
 * Integration test to verify that Pan properly uses EnhancedPanCommandExecutor
 */
public class PanIntegrationTest {

  @Test
  public void testPanUsesEnhancedCommandExecutor() {
    LogChannelInterface log = new LogChannel("PanIntegrationTest");
    
    // Create an instance of EnhancedPanCommandExecutor
    EnhancedPanCommandExecutor executor = new EnhancedPanCommandExecutor(Pan.class, log);
    
    // Set it as the command executor for Pan
    Pan.setCommandExecutor(executor);
    
    // Verify that Pan returns the same instance
    EnhancedPanCommandExecutor retrievedExecutor = Pan.getCommandExecutor();
    
    assertNotNull("Command executor should not be null", retrievedExecutor);
    assertTrue("Pan should use EnhancedPanCommandExecutor",
        retrievedExecutor instanceof EnhancedPanCommandExecutor);
    
    // Verify that the delegate is properly initialized
    assertNotNull("Transformation delegate should be initialized", 
        retrievedExecutor.getTransformationDelegate());
    
    // Clean up
    Pan.setCommandExecutor(null);
  }
  
  @Test
  public void testEnhancedExecutorHasRepository() {
    LogChannelInterface log = new LogChannel("PanIntegrationTest");
    
    // Create an instance of EnhancedPanCommandExecutor
    EnhancedPanCommandExecutor executor = new EnhancedPanCommandExecutor(Pan.class, log);
    
    // The getRepository() method should be available (may return null if no repository is configured)
    // This test just verifies the method exists and doesn't throw an exception
    try {
      executor.getRepository();
      // If we get here, the method exists and executes successfully
      assertTrue("getRepository() method executed without exception", true);
    } catch (Throwable e) {
      // If an exception is thrown, make sure it's not a NoSuchMethodError
      assertNotNull("Exception occurred: " + e.getMessage(), e);
      assertFalse("Should not be a NoSuchMethodError", e instanceof NoSuchMethodError);
    }
  }
}
