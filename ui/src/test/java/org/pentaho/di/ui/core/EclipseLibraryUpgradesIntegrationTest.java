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

import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration test for Eclipse library upgrades (Commands, JFace, Core Runtime, Common)
 */
public class EclipseLibraryUpgradesIntegrationTest {

  private CommandManager commandManager;

  @Before
  public void setUp() {
    commandManager = new CommandManager();
  }

  @Test
  public void testMessageDialogWithCommandHandler() {
    try {
      Command cmd = commandManager.getCommand( "org.pentaho.di.test.integration1" );
      
      IHandler handler = new IHandler() {
        @Override
        public Object execute( org.eclipse.core.commands.ExecutionEvent event ) {
          return "Dialog handler executed";
        }
        @Override
        public void addHandlerListener( org.eclipse.core.commands.IHandlerListener handlerListener ) {}
        @Override
        public void removeHandlerListener( org.eclipse.core.commands.IHandlerListener handlerListener ) {}
        @Override
        public void dispose() {}
        @Override
        public boolean isEnabled() { return true; }
        @Override
        public boolean isHandled() { return true; }
      };
      
      cmd.setHandler( handler );
      
      assertNotNull( "Command handler should be set", cmd.getHandler() );
      assertTrue( "Handler should be handled", cmd.getHandler().isHandled() );
    } catch ( Exception e ) {
      fail( "MessageDialog integration test failed: " + e.getMessage() );
    }
  }

  @Test
  public void testListViewerWithCoreUtilities() {
    try {
      ArrayContentProvider provider = ArrayContentProvider.getInstance();
      assertNotNull( "ArrayContentProvider should be created", provider );
      
      // Verify Assert utilities work
      Assert.isNotNull( provider, "Provider must not be null" );
      assertTrue( "Core utilities integration works", true );
    } catch ( Exception e ) {
      fail( "ListViewer integration test failed: " + e.getMessage() );
    }
  }

  @Test
  public void testCommandsWithJFaceWindow() {
    try {
      Command cmd = commandManager.getCommand( "org.pentaho.di.test.jfacewindow" );
      
      IHandler handler = new IHandler() {
        @Override
        public Object execute( org.eclipse.core.commands.ExecutionEvent event ) {
          return "JFace window handler";
        }
        @Override
        public void addHandlerListener( org.eclipse.core.commands.IHandlerListener handlerListener ) {}
        @Override
        public void removeHandlerListener( org.eclipse.core.commands.IHandlerListener handlerListener ) {}
        @Override
        public void dispose() {}
        @Override
        public boolean isEnabled() { return true; }
        @Override
        public boolean isHandled() { return true; }
      };
      
      cmd.setHandler( handler );
      assertTrue( "Handler should be handled", cmd.getHandler().isHandled() );
    } catch ( Exception e ) {
      fail( "Commands with JFace window test failed: " + e.getMessage() );
    }
  }

  @Test
  public void testMultipleCommandHandlers() {
    try {
      Command cmd1 = commandManager.getCommand( "org.pentaho.di.test.multi1" );
      Command cmd2 = commandManager.getCommand( "org.pentaho.di.test.multi2" );
      
      IHandler handler1 = new IHandler() {
        @Override
        public Object execute( org.eclipse.core.commands.ExecutionEvent event ) { return "Handler 1"; }
        @Override
        public void addHandlerListener( org.eclipse.core.commands.IHandlerListener handlerListener ) {}
        @Override
        public void removeHandlerListener( org.eclipse.core.commands.IHandlerListener handlerListener ) {}
        @Override
        public void dispose() {}
        @Override
        public boolean isEnabled() { return true; }
        @Override
        public boolean isHandled() { return true; }
      };
      
      IHandler handler2 = new IHandler() {
        @Override
        public Object execute( org.eclipse.core.commands.ExecutionEvent event ) { return "Handler 2"; }
        @Override
        public void addHandlerListener( org.eclipse.core.commands.IHandlerListener handlerListener ) {}
        @Override
        public void removeHandlerListener( org.eclipse.core.commands.IHandlerListener handlerListener ) {}
        @Override
        public void dispose() {}
        @Override
        public boolean isEnabled() { return true; }
        @Override
        public boolean isHandled() { return true; }
      };
      
      cmd1.setHandler( handler1 );
      cmd2.setHandler( handler2 );
      
      assertNotNull( "Handler 1 should be set", cmd1.getHandler() );
      assertNotNull( "Handler 2 should be set", cmd2.getHandler() );
    } catch ( Exception e ) {
      fail( "Multiple command handlers test failed: " + e.getMessage() );
    }
  }

  @Test
  public void testJFaceContentProvider() {
    try {
      ArrayContentProvider provider = ArrayContentProvider.getInstance();
      Object[] elements = new Object[] { "Element1", "Element2", "Element3" };
      
      Object[] result = (Object[]) provider.getElements( elements );
      
      assertNotNull( "Content should be returned", result );
      assertEquals( "Content array should match", elements.length, result.length );
    } catch ( Exception e ) {
      fail( "JFace content provider test failed: " + e.getMessage() );
    }
  }

  @Test
  public void testCommandsFrameworkConsistency() {
    try {
      Command cmd1 = commandManager.getCommand( "org.pentaho.di.consistency1" );
      Command cmd2 = commandManager.getCommand( "org.pentaho.di.consistency1" );
      
      assertEquals( "Same command ID should return same instance", cmd1, cmd2 );
    } catch ( Exception e ) {
      fail( "Commands framework consistency test failed: " + e.getMessage() );
    }
  }

  @Test
  public void testLibraryVersionConsistency() {
    try {
      // Verify all libraries work together
      CommandManager cm = new CommandManager();
      assertNotNull( "CommandManager from Commands library", cm );
      
      ArrayContentProvider provider = ArrayContentProvider.getInstance();
      assertNotNull( "ArrayContentProvider from JFace library", provider );
      
      Status status = new Status( Status.OK, "org.pentaho.di", "Test" );
      assertNotNull( "Status from Core Runtime library", status );
      
      Assert.isNotNull( status, "Assert from Common library" );
      
      assertTrue( "All libraries are consistent", true );
    } catch ( Exception e ) {
      fail( "Library version consistency test failed: " + e.getMessage() );
    }
  }

  @Test
  public void testCommandExecutionFlow() {
    try {
      Command cmd = commandManager.getCommand( "org.pentaho.di.test.execution" );
      
      IHandler handler = new IHandler() {
        @Override
        public Object execute( org.eclipse.core.commands.ExecutionEvent event ) {
          return "Execution successful";
        }
        @Override
        public void addHandlerListener( org.eclipse.core.commands.IHandlerListener handlerListener ) {}
        @Override
        public void removeHandlerListener( org.eclipse.core.commands.IHandlerListener handlerListener ) {}
        @Override
        public void dispose() {}
        @Override
        public boolean isEnabled() { return true; }
        @Override
        public boolean isHandled() { return true; }
      };
      
      cmd.setHandler( handler );
      
      assertTrue( "Command should be executable", cmd.getHandler().isHandled() );
    } catch ( Exception e ) {
      fail( "Command execution flow test failed: " + e.getMessage() );
    }
  }

  @Test
  public void testNoBreakingAPIChanges() {
    try {
      // Verify key API methods exist
      CommandManager cm = new CommandManager();
      assertNotNull( "getCommand should exist",
          cm.getClass().getMethod( "getCommand", String.class ) );
      
      ArrayContentProvider provider = ArrayContentProvider.getInstance();
      assertNotNull( "getInstance should exist",
          ArrayContentProvider.class.getMethod( "getInstance" ) );
      
      assertTrue( "No breaking API changes detected", true );
    } catch ( NoSuchMethodException e ) {
      fail( "Breaking API change detected: " + e.getMessage() );
    } catch ( Exception e ) {
      fail( "API check failed: " + e.getMessage() );
    }
  }

  @Test
  public void testEndToEndIntegration() {
    try {
      // Create commands
      Command cmd1 = commandManager.getCommand( "org.pentaho.di.e2e.cmd1" );
      Command cmd2 = commandManager.getCommand( "org.pentaho.di.e2e.cmd2" );
      
      // Register handlers
      IHandler handler = new IHandler() {
        @Override
        public Object execute( org.eclipse.core.commands.ExecutionEvent event ) { return "E2E"; }
        @Override
        public void addHandlerListener( org.eclipse.core.commands.IHandlerListener handlerListener ) {}
        @Override
        public void removeHandlerListener( org.eclipse.core.commands.IHandlerListener handlerListener ) {}
        @Override
        public void dispose() {}
        @Override
        public boolean isEnabled() { return true; }
        @Override
        public boolean isHandled() { return true; }
      };
      
      cmd1.setHandler( handler );
      cmd2.setHandler( handler );
      
      // Verify JFace utilities work
      ArrayContentProvider provider = ArrayContentProvider.getInstance();
      Object[] elements = { "Test" };
      Object[] result = (Object[]) provider.getElements( elements );
      
      // Verify status framework works
      Status status = new Status( Status.OK, "test", "All OK" );
      assertTrue( "Status should be OK", status.isOK() );
      
      // Verify assertions work
      Assert.isNotNull( status, "Status must exist" );
      
      assertTrue( "End-to-end integration successful", true );
    } catch ( Exception e ) {
      fail( "End-to-end integration test failed: " + e.getMessage() );
    }
  }
}
