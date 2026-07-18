/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.di.ui.core;

import static org.junit.Assert.*;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.IHandler;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for Eclipse Core Commands library upgrade from 3.3.0 to 3.11.100
 */
public class EclipseCoreCommandsUpgradeTest {

  private CommandManager commandManager;

  @Before
  public void setUp() {
    commandManager = new CommandManager();
  }

  @Test
  public void testCommandManagerAvailable() {
    assertNotNull( "CommandManager should be available", CommandManager.class );
    assertNotNull( "CommandManager instance should be created", commandManager );
  }

  @Test
  public void testCommandCreation() {
    try {
      Command cmd = commandManager.getCommand( "org.pentaho.di.test.command1" );
      assertNotNull( "Command should be created", cmd );
      assertEquals( "Command ID should match", "org.pentaho.di.test.command1", cmd.getId() );
    } catch ( Exception e ) {
      fail( "Command creation failed: " + e.getMessage() );
    }
  }

  @Test
  public void testHandlerRegistration() {
    try {
      Command cmd = commandManager.getCommand( "org.pentaho.di.test.command2" );
      
      IHandler handler = new IHandler() {
        @Override
        public Object execute( org.eclipse.core.commands.ExecutionEvent event ) {
          return "Handler executed";
        }
        @Override
        public void addHandlerListener( org.eclipse.core.commands.IHandlerListener handlerListener ) {
          // Intentionally empty - test fixture for handler listener interface
        }
        @Override
        public void removeHandlerListener( org.eclipse.core.commands.IHandlerListener handlerListener ) {
          // Intentionally empty - test fixture for handler listener interface
        }
        @Override
        public void dispose() {
          // Intentionally empty - test fixture for cleanup interface
        }
        @Override
        public boolean isEnabled() { return true; }
        @Override
        public boolean isHandled() { return true; }
      };
      
      cmd.setHandler( handler );
      assertNotNull( "Handler should be registered", cmd.getHandler() );
      assertEquals( "Handler should match", handler, cmd.getHandler() );
    } catch ( Exception e ) {
      fail( "Handler registration failed: " + e.getMessage() );
    }
  }

  @Test
  public void testCommandCategory() {
    try {
      Category category = commandManager.getCategory( "org.pentaho.di.test.category" );
      assertNotNull( "Category should be created", category );
      assertEquals( "Category ID should match", "org.pentaho.di.test.category", category.getId() );
    } catch ( Exception e ) {
      fail( "Handler registration failed: " + e.getMessage() );
    }
  }

  @Test
  public void testMultipleCommands() {
    try {
      Command cmd1 = commandManager.getCommand( "org.pentaho.di.test.cmd1" );
      Command cmd2 = commandManager.getCommand( "org.pentaho.di.test.cmd2" );
      Command cmd3 = commandManager.getCommand( "org.pentaho.di.test.cmd3" );
      
      assertNotNull( "Command 1 should be created", cmd1 );
      assertNotNull( "Command 2 should be created", cmd2 );
      assertNotNull( "Command 3 should be created", cmd3 );
    } catch ( Exception e ) {
      fail( "Handler registration failed: " + e.getMessage() );
    }
  }

  @Test
  public void testAPICompatibility() {
    try {
      assertNotNull( "Command class should be available", Command.class );
      assertNotNull( "CommandManager class should be available", CommandManager.class );
      assertNotNull( "Category class should be available", Category.class );
      assertNotNull( "IHandler interface should be available", IHandler.class );
    } catch ( Exception e ) {
      fail( "API compatibility check failed: " + e.getMessage() );
    }
  }

  @Test
  public void testCommandLifecycle() {
    try {
      Command cmd = commandManager.getCommand( "org.pentaho.di.test.lifecycle" );
      assertNotNull( "Command should be created", cmd );
      
      IHandler handler = new IHandler() {
        @Override
        public Object execute( org.eclipse.core.commands.ExecutionEvent event ) { return "Test"; }
        @Override
        public void addHandlerListener( org.eclipse.core.commands.IHandlerListener handlerListener ) {
          // Intentionally empty - test fixture for handler listener interface
        }
        @Override
        public void removeHandlerListener( org.eclipse.core.commands.IHandlerListener handlerListener ) {
          // Intentionally empty - test fixture for handler listener interface
        }
        @Override
        public void dispose() {
          // Intentionally empty - test fixture for cleanup interface
        }
        @Override
        public boolean isEnabled() { return true; }
        @Override
        public boolean isHandled() { return true; }
      };
      cmd.setHandler( handler );
      assertNotNull( "Handler should be set", cmd.getHandler() );
      assertEquals( "Handler should match", handler, cmd.getHandler() );
    } catch ( Exception e ) {
      fail( "Command lifecycle test failed: " + e.getMessage() );
    }
  }

  @Test
  public void testCommandRetrievalById() {
    try {
      String commandId = "org.pentaho.di.test.retrieve";
      
      Command cmd1 = commandManager.getCommand( commandId );
      assertNotNull( "Command should be created", cmd1 );
      
      Command cmd2 = commandManager.getCommand( commandId );
      assertNotNull( "Command should be retrievable", cmd2 );
      
      assertEquals( "Retrieved command should be same instance", cmd1, cmd2 );
    } catch ( Exception e ) {
      fail( "Command retrieval test failed: " + e.getMessage() );
    }
  }

  @Test
  public void testNoBreakingChanges() {
    try {
      CommandManager cm = new CommandManager();
      assertNotNull( "getCommand method should exist", 
          cm.getClass().getMethod( "getCommand", String.class ) );
      assertNotNull( "getCategory method should exist",
          cm.getClass().getMethod( "getCategory", String.class ) );
      
      Command cmd = cm.getCommand( "test" );
      assertNotNull( "setHandler method should exist",
          cmd.getClass().getMethod( "setHandler", IHandler.class ) );
    } catch ( NoSuchMethodException e ) {
      fail( "Breaking API change detected: " + e.getMessage() );
    } catch ( Exception e ) {
      fail( "API check failed: " + e.getMessage() );
    }
  }

  @Test
  public void testCommandManagerState() {
    try {
      CommandManager cm = new CommandManager();
      
      Command cmd1 = cm.getCommand( "cmd1" );
      Command cmd2 = cm.getCommand( "cmd2" );
      Command cmd3 = cm.getCommand( "cmd3" );
      
      assertNotNull( "Command 1 should exist", cmd1 );
      assertNotNull( "Command 2 should exist", cmd2 );
      assertNotNull( "Command 3 should exist", cmd3 );
      
      // Verify commands are retrievable from the manager
      Command retrieved1 = cm.getCommand( "cmd1" );
      assertEquals( "Retrieved command should match", cmd1, retrieved1 );
    } catch ( Exception e ) {
      fail( "Command manager state test failed: " + e.getMessage() );
    }
  }
}
