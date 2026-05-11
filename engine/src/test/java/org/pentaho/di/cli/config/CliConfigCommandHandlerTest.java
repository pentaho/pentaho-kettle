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

package org.pentaho.di.cli.config;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CliConfigCommandHandlerTest {

  private final ByteArrayOutputStream sysOut = new ByteArrayOutputStream();
  private final ByteArrayOutputStream sysErr = new ByteArrayOutputStream();
  private PrintStream originalOut;
  private PrintStream originalErr;

  @Before
  public void setUp() {
    originalOut = System.out;
    originalErr = System.err;
    System.setOut( new PrintStream( sysOut ) );
    System.setErr( new PrintStream( sysErr ) );
  }

  @After
  public void tearDown() {
    System.setOut( originalOut );
    System.setErr( originalErr );
  }

  @Test
  public void hasConfigCommandHandlesNullAndCaseInsensitiveArguments() {
    assertTrue( CliConfigCommandHandler.hasConfigCommand( new String[] { "-CONFIG:PATH" } ) );
    assertTrue( CliConfigCommandHandler.hasConfigCommand( new String[] { "--flag", "-config:list" } ) );
    assertFalse( CliConfigCommandHandler.hasConfigCommand( null ) );
  }

  @Test
  public void executeSetAcceptsEqualsSyntaxForKnownKeys() {
    CliConfig cliConfig = mock( CliConfig.class );
    when( cliConfig.set( CliConfig.KEY_AUTH_PREFERRED_IDP, "keycloak" ) ).thenReturn( true );

    CliConfigCommandHandler handler = new CliConfigCommandHandler( cliConfig );
    int result = handler.execute( new String[] { "-config:set", CliConfig.KEY_AUTH_PREFERRED_IDP + "=keycloak" } );

    assertEquals( 0, result );
    assertTrue( sysOut.toString().contains( "Set " + CliConfig.KEY_AUTH_PREFERRED_IDP + " = keycloak" ) );
  }

  @Test
  public void executeGetPrintsDefaultSuffixForUnsetKnownKey() {
    CliConfig cliConfig = mock( CliConfig.class );
    when( cliConfig.get( CliConfig.KEY_TOKEN_STORE_BACKEND ) ).thenReturn( null );
    when( cliConfig.getTokenStoreBackend() ).thenReturn( CliConfig.TOKEN_STORE_BACKEND_FILE );

    CliConfigCommandHandler handler = new CliConfigCommandHandler( cliConfig );
    int result = handler.execute( new String[] { "-config:get", CliConfig.KEY_TOKEN_STORE_BACKEND } );

    assertEquals( 0, result );
    assertTrue( sysOut.toString().contains( CliConfig.KEY_TOKEN_STORE_BACKEND + " = file (default)" ) );
  }

  @Test
  public void executeListPrintsDefaultsAndConfigPathWhenNoValuesAreSet() {
    CliConfig cliConfig = mock( CliConfig.class );
    when( cliConfig.list() ).thenReturn( new Properties() );
    when( cliConfig.getAuthTimeoutSeconds() ).thenReturn( 300 );
    when( cliConfig.getTokenStoreBackend() ).thenReturn( CliConfig.TOKEN_STORE_BACKEND_FILE );
    when( cliConfig.getBrokerReadTimeoutSeconds() ).thenReturn( 30 );
    when( cliConfig.getConfigFilePath() ).thenReturn( "C:/temp/cli-config.properties" );

    CliConfigCommandHandler handler = new CliConfigCommandHandler( cliConfig );
    int result = handler.execute( new String[] { "-config:list" } );

    assertEquals( 0, result );
    assertTrue( sysOut.toString().contains( "No configuration values set." ) );
    assertTrue( sysOut.toString().contains( CliConfig.KEY_AUTH_TIMEOUT_SECONDS + " = 300" ) );
    assertTrue( sysOut.toString().contains( "Configuration file: C:/temp/cli-config.properties" ) );
  }

  @Test
  public void executeDeletePrintsErrorWhenRemovalFails() {
    CliConfig cliConfig = mock( CliConfig.class );
    when( cliConfig.remove( CliConfig.KEY_AUTH_PREFERRED_IDP ) ).thenReturn( false );

    CliConfigCommandHandler handler = new CliConfigCommandHandler( cliConfig );
    int result = handler.execute( new String[] { "-config:delete", CliConfig.KEY_AUTH_PREFERRED_IDP } );

    assertEquals( 1, result );
    assertTrue( sysErr.toString().contains( "Failed to delete " + CliConfig.KEY_AUTH_PREFERRED_IDP ) );
  }

  @Test
  public void executeWithoutArgumentsPrintsUsage() {
    CliConfigCommandHandler handler = new CliConfigCommandHandler( mock( CliConfig.class ) );

    int result = handler.execute( null );

    assertEquals( 1, result );
    assertTrue( sysOut.toString().contains( "CLI Configuration Commands:" ) );
  }

  @Test
  public void executeWithoutConfigCommandPrintsErrorAndUsage() {
    CliConfigCommandHandler handler = new CliConfigCommandHandler( mock( CliConfig.class ) );

    int result = handler.execute( new String[] { "-file:job.kjb" } );

    assertEquals( 1, result );
    assertTrue( sysErr.toString().contains( "No config command found" ) );
    assertTrue( sysOut.toString().contains( "CLI Configuration Commands:" ) );
  }

  @Test
  public void executeUnknownCommandPrintsErrorAndUsage() {
    CliConfigCommandHandler handler = new CliConfigCommandHandler( mock( CliConfig.class ) );

    int result = handler.execute( new String[] { "-config:oops" } );

    assertEquals( 1, result );
    assertTrue( sysErr.toString().contains( "Unknown config command: oops" ) );
    assertTrue( sysOut.toString().contains( "CLI Configuration Commands:" ) );
  }

  @Test
  public void executeSetPrintsUsageWhenValueIsMissing() {
    CliConfigCommandHandler handler = new CliConfigCommandHandler( mock( CliConfig.class ) );

    int result = handler.execute( new String[] { "-config:set", CliConfig.KEY_AUTH_TIMEOUT_SECONDS } );

    assertEquals( 1, result );
    assertTrue( sysErr.toString().contains( "Usage: Pan.bat -config:set <key> <value>" ) );
    assertTrue( sysOut.toString().contains( "Available configuration keys:" ) );
  }

  @Test
  public void executeSetRejectsUnknownKey() {
    CliConfigCommandHandler handler = new CliConfigCommandHandler( mock( CliConfig.class ) );

    int result = handler.execute( new String[] { "-config:set", "unknown.key", "value" } );

    assertEquals( 1, result );
    assertTrue( sysErr.toString().contains( "Unknown configuration key: unknown.key" ) );
    assertTrue( sysOut.toString().contains( "Available configuration keys:" ) );
  }

  @Test
  public void executeSetPrintsErrorWhenValueIsRejected() {
    CliConfig cliConfig = mock( CliConfig.class );
    when( cliConfig.set( CliConfig.KEY_AUTH_TIMEOUT_SECONDS, "bad" ) ).thenReturn( false );

    CliConfigCommandHandler handler = new CliConfigCommandHandler( cliConfig );
    int result = handler.execute( new String[] { "-config:set", CliConfig.KEY_AUTH_TIMEOUT_SECONDS, "bad" } );

    assertEquals( 1, result );
    assertTrue( sysErr.toString().contains( "Failed to set " + CliConfig.KEY_AUTH_TIMEOUT_SECONDS ) );
  }

  @Test
  public void executeGetPrintsDefaultTimeoutWhenUnset() {
    CliConfig cliConfig = mock( CliConfig.class );
    when( cliConfig.get( CliConfig.KEY_AUTH_TIMEOUT_SECONDS ) ).thenReturn( null );
    when( cliConfig.getAuthTimeoutSeconds() ).thenReturn( 300 );

    CliConfigCommandHandler handler = new CliConfigCommandHandler( cliConfig );
    int result = handler.execute( new String[] { "-config:get", CliConfig.KEY_AUTH_TIMEOUT_SECONDS } );

    assertEquals( 0, result );
    assertTrue( sysOut.toString().contains( CliConfig.KEY_AUTH_TIMEOUT_SECONDS + " = 300 (default)" ) );
  }

  @Test
  public void executeGetPrintsDefaultBrokerReadTimeoutWhenUnset() {
    CliConfig cliConfig = mock( CliConfig.class );
    when( cliConfig.get( CliConfig.KEY_AUTH_BROKER_READ_TIMEOUT_SECONDS ) ).thenReturn( null );
    when( cliConfig.getBrokerReadTimeoutSeconds() ).thenReturn( 45 );

    CliConfigCommandHandler handler = new CliConfigCommandHandler( cliConfig );
    int result = handler.execute( new String[] { "-config:get", CliConfig.KEY_AUTH_BROKER_READ_TIMEOUT_SECONDS } );

    assertEquals( 0, result );
    assertTrue( sysOut.toString().contains( CliConfig.KEY_AUTH_BROKER_READ_TIMEOUT_SECONDS + " = 45 (default)" ) );
  }

  @Test
  public void executeGetPrintsNotSetForUnknownKey() {
    CliConfig cliConfig = mock( CliConfig.class );
    when( cliConfig.get( "custom.key" ) ).thenReturn( null );

    CliConfigCommandHandler handler = new CliConfigCommandHandler( cliConfig );
    int result = handler.execute( new String[] { "-config:get", "custom.key" } );

    assertEquals( 0, result );
    assertTrue( sysOut.toString().contains( "custom.key is not set" ) );
  }

  @Test
  public void executeDeleteSucceedsWhenRemovalSucceeds() {
    CliConfig cliConfig = mock( CliConfig.class );
    when( cliConfig.remove( CliConfig.KEY_AUTH_PREFERRED_IDP ) ).thenReturn( true );

    CliConfigCommandHandler handler = new CliConfigCommandHandler( cliConfig );
    int result = handler.execute( new String[] { "-config:remove", CliConfig.KEY_AUTH_PREFERRED_IDP } );

    assertEquals( 0, result );
    assertTrue( sysOut.toString().contains( "Deleted " + CliConfig.KEY_AUTH_PREFERRED_IDP ) );
  }

  @Test
  public void executeListPrintsConfiguredValuesWhenPresent() {
    CliConfig cliConfig = mock( CliConfig.class );
    Properties properties = new Properties();
    properties.setProperty( CliConfig.KEY_AUTH_PREFERRED_IDP, "keycloak" );
    when( cliConfig.list() ).thenReturn( properties );
    when( cliConfig.getConfigFilePath() ).thenReturn( "C:/temp/cli-config.properties" );

    CliConfigCommandHandler handler = new CliConfigCommandHandler( cliConfig );
    int result = handler.execute( new String[] { "-config:list" } );

    assertEquals( 0, result );
    assertTrue( sysOut.toString().contains( "Current configuration:" ) );
    assertTrue( sysOut.toString().contains( CliConfig.KEY_AUTH_PREFERRED_IDP + " = keycloak" ) );
  }

  @Test
  public void executeResetInvokesCliConfigReset() {
    CliConfig cliConfig = mock( CliConfig.class );

    CliConfigCommandHandler handler = new CliConfigCommandHandler( cliConfig );
    int result = handler.execute( new String[] { "-config:reset" } );

    assertEquals( 0, result );
    verify( cliConfig ).resetToDefaults();
    assertTrue( sysOut.toString().contains( "Configuration reset to defaults." ) );
  }

  @Test
  public void executePathPrintsConfigFilePath() {
    CliConfig cliConfig = mock( CliConfig.class );
    when( cliConfig.getConfigFilePath() ).thenReturn( "C:/temp/cli-config.properties" );

    CliConfigCommandHandler handler = new CliConfigCommandHandler( cliConfig );
    int result = handler.execute( new String[] { "-config:path" } );

    assertEquals( 0, result );
    assertTrue( sysOut.toString().contains( "C:/temp/cli-config.properties" ) );
  }

  @Test
  public void executeHelpPrintsUsageAndSucceeds() {
    CliConfigCommandHandler handler = new CliConfigCommandHandler( mock( CliConfig.class ) );

    int result = handler.execute( new String[] { "-config:help" } );

    assertEquals( 0, result );
    assertTrue( sysOut.toString().contains( "CLI Configuration Commands:" ) );
    assertTrue( sysOut.toString().contains( "Pan.bat -config:set <key> <value>" ) );
  }
}
