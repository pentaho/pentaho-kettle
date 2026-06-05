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
 *******************************************************************************/

package org.pentaho.di.ui.core;

import static org.junit.Assert.*;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.junit.Test;

/**
 * Unit test for Eclipse Core Runtime compatibility
 */
public class EclipseCoreRuntimeCompatibilityTest {

  private static final String PLUGIN_ID = "org.pentaho.di.ui";

  @Test
  public void testPlatformAvailable() {
    try {
      assertNotNull( "IStatus class should be available", IStatus.class );
      assertNotNull( "Status class should be available", Status.class );
    } catch ( Exception e ) {
      fail( "Core Runtime availability check failed: " + e.getMessage() );
    }
  }

  @Test
  public void testStatusCreation() {
    try {
      IStatus status = new Status( IStatus.OK, PLUGIN_ID, "Test message" );
      assertNotNull( "Status should be created", status );
      assertEquals( "Status severity should be OK", IStatus.OK, status.getSeverity() );
      assertEquals( "Status plugin ID should match", PLUGIN_ID, status.getPlugin() );
    } catch ( Exception e ) {
      fail( "Status creation failed: " + e.getMessage() );
    }
  }

  @Test
  public void testStatusWithThrowable() {
    try {
      Exception exception = new Exception( "Test exception" );
      IStatus status = new Status( IStatus.ERROR, PLUGIN_ID, "Error occurred", exception );
      
      assertNotNull( "Status should be created with throwable", status );
      assertEquals( "Status severity should be ERROR", IStatus.ERROR, status.getSeverity() );
      assertNotNull( "Exception should be attached", status.getException() );
    } catch ( Exception e ) {
      fail( "Status with throwable test failed: " + e.getMessage() );
    }
  }

  @Test
  public void testMultiStatus() {
    try {
      IStatus status1 = new Status( IStatus.OK, PLUGIN_ID, "Message 1" );
      IStatus status2 = new Status( IStatus.WARNING, PLUGIN_ID, "Message 2" );
      
      MultiStatus multi = new MultiStatus( PLUGIN_ID, IStatus.OK, 
          new IStatus[] { status1, status2 }, "Multi-status message", null );
      
      assertNotNull( "MultiStatus should be created", multi );
      assertTrue( "MultiStatus should have children", multi.getChildren().length >= 2 );
    } catch ( Exception e ) {
      fail( "MultiStatus test failed: " + e.getMessage() );
    }
  }

  @Test
  public void testAPIMethodsExist() {
    try {
      IStatus status = new Status( IStatus.OK, PLUGIN_ID, "Test" );
      assertNotNull( "getSeverity method should exist", 
          status.getClass().getMethod( "getSeverity" ) );
      assertNotNull( "getMessage method should exist",
          status.getClass().getMethod( "getMessage" ) );
      assertNotNull( "getPlugin method should exist",
          status.getClass().getMethod( "getPlugin" ) );
      assertNotNull( "getException method should exist",
          status.getClass().getMethod( "getException" ) );
    } catch ( NoSuchMethodException e ) {
      fail( "Required API method not found: " + e.getMessage() );
    } catch ( Exception e ) {
      fail( "API method check failed: " + e.getMessage() );
    }
  }

  @Test
  public void testIsOK() {
    try {
      IStatus okStatus = new Status( IStatus.OK, PLUGIN_ID, "OK" );
      IStatus errorStatus = new Status( IStatus.ERROR, PLUGIN_ID, "Error" );
      
      assertTrue( "OK status should have isOK() return true", okStatus.isOK() );
      assertFalse( "ERROR status should have isOK() return false", errorStatus.isOK() );
    } catch ( Exception e ) {
      fail( "isOK test failed: " + e.getMessage() );
    }
  }

  @Test
  public void testStatusMessage() {
    try {
      String message = "Test status message";
      IStatus status = new Status( IStatus.OK, PLUGIN_ID, message );
      
      assertNotNull( "Message should not be null", status.getMessage() );
      assertEquals( "Message should match", message, status.getMessage() );
    } catch ( Exception e ) {
      fail( "Status message test failed: " + e.getMessage() );
    }
  }

  @Test
  public void testStatusPluginId() {
    try {
      String plugin = "org.test.plugin";
      IStatus status = new Status( IStatus.OK, plugin, "Test" );
      
      assertNotNull( "Plugin ID should not be null", status.getPlugin() );
      assertEquals( "Plugin ID should match", plugin, status.getPlugin() );
    } catch ( Exception e ) {
      fail( "Status plugin ID test failed: " + e.getMessage() );
    }
  }

  @Test
  public void testExceptionHandling() {
    try {
      Exception testException = new RuntimeException( "Test runtime exception" );
      IStatus status = new Status( IStatus.ERROR, PLUGIN_ID, "Exception occurred", testException );
      
      assertNotNull( "Exception should be stored", status.getException() );
      assertTrue( "Exception should be RuntimeException", 
          status.getException() instanceof RuntimeException );
      assertEquals( "Exception message should match", "Test runtime exception", 
          status.getException().getMessage() );
    } catch ( Exception e ) {
      fail( "Exception handling test failed: " + e.getMessage() );
    }
  }

  @Test
  public void testStatusLevels() {
    try {
      Status ok = new Status( IStatus.OK, PLUGIN_ID, "OK" );
      Status warning = new Status( IStatus.WARNING, PLUGIN_ID, "Warning" );
      Status error = new Status( IStatus.ERROR, PLUGIN_ID, "Error" );
      Status info = new Status( IStatus.INFO, PLUGIN_ID, "Info" );
      
      assertEquals( "OK status should have correct level", IStatus.OK, ok.getSeverity() );
      assertEquals( "WARNING status should have correct level", IStatus.WARNING, warning.getSeverity() );
      assertEquals( "ERROR status should have correct level", IStatus.ERROR, error.getSeverity() );
      assertEquals( "INFO status should have correct level", IStatus.INFO, info.getSeverity() );
    } catch ( Exception e ) {
      fail( "Status levels test failed: " + e.getMessage() );
    }
  }
}
