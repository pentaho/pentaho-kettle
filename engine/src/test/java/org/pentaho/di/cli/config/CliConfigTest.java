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
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.pentaho.di.cli.config.CliConfig.DEFAULT_PREFERRED_IDP;

public class CliConfigTest {

  @ClassRule
  public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private String originalUserHome;
  private String originalKettleHome;
  private Path tempHome;

  @Before
  public void setUp() throws IOException {
    originalUserHome = System.getProperty( "user.home" );
    originalKettleHome = System.getProperty( "KETTLE_HOME" );
    tempHome = Files.createTempDirectory( "cli-config-test" );
    System.setProperty( "user.home", tempHome.toString() );
    System.setProperty( "KETTLE_HOME", tempHome.toString() );
  }

  @After
  public void tearDown() throws IOException {
    if ( originalUserHome != null ) {
      System.setProperty( "user.home", originalUserHome );
    } else {
      System.clearProperty( "user.home" );
    }
    if ( originalKettleHome != null ) {
      System.setProperty( "KETTLE_HOME", originalKettleHome );
    } else {
      System.clearProperty( "KETTLE_HOME" );
    }
    deleteRecursively( tempHome );
  }

  @Test
  public void constructorCreatesDefaultConfigFileInTempKettleDirectory() throws Exception {
    CliConfig config = newConfig();
    File configFile = config.getConfigFile();

    assertTrue( configFile.getAbsolutePath().startsWith( tempHome.toString() ) );
    assertTrue( Files.exists( configFile.toPath() ) );
    assertEquals( 300, config.getAuthTimeoutSeconds() );
    assertEquals( 30, config.getBrokerReadTimeoutSeconds() );
  }

  @Test
  public void setBrokerReadTimeoutSecondsPersistsValidValuesAndRejectsInvalidOnes() throws Exception {
    CliConfig config = newConfig();

    assertTrue( config.setBrokerReadTimeoutSeconds( 300 ) );
    assertEquals( 300, config.getBrokerReadTimeoutSeconds() );

    assertFalse( config.setBrokerReadTimeoutSeconds( 9 ) );
    assertEquals( 300, config.getBrokerReadTimeoutSeconds() );
  }

  @Test
  public void tokenStoreBackendAcceptsOnlyKnownValues() throws Exception {
    CliConfig config = newConfig();

    assertEquals( CliConfig.TOKEN_STORE_BACKEND_FILE, config.getTokenStoreBackend() );
    assertTrue( config.setTokenStoreBackend( CliConfig.TOKEN_STORE_BACKEND_ENC ) );
    assertEquals( CliConfig.TOKEN_STORE_BACKEND_ENC, config.getTokenStoreBackend() );

    assertFalse( config.setTokenStoreBackend( "vault" ) );
    assertEquals( CliConfig.TOKEN_STORE_BACKEND_ENC, config.getTokenStoreBackend() );
  }

  @Test
  public void resetToDefaultsClearsCustomValuesAndRestoresBuiltIns() throws Exception {
    CliConfig config = newConfig();
    assertTrue( config.setAuthTimeoutSeconds( 900 ) );
    assertTrue( config.setTokenStoreBackend( CliConfig.TOKEN_STORE_BACKEND_FILE ) );
    assertTrue( config.setBrokerReadTimeoutSeconds( 300 ) );
    assertTrue( config.set( CliConfig.KEY_AUTH_PREFERRED_IDP, "keycloak" ) );
    assertTrue( config.set( "custom.key", "custom-value" ) );

    config.resetToDefaults();

    Properties properties = config.list();
    assertEquals( "300", properties.getProperty( CliConfig.KEY_AUTH_TIMEOUT_SECONDS ) );
    assertEquals( CliConfig.TOKEN_STORE_BACKEND_FILE, properties.getProperty( CliConfig.KEY_TOKEN_STORE_BACKEND ) );
    assertEquals( DEFAULT_PREFERRED_IDP, properties.getProperty( CliConfig.KEY_AUTH_PREFERRED_IDP ) );
    assertFalse( properties.containsKey( CliConfig.KEY_AUTH_BROKER_READ_TIMEOUT_SECONDS ) );
    assertFalse( properties.containsKey( "custom.key" ) );
  }

  @Test
  public void genericSetRejectsInvalidNumericBrokerTimeout() throws Exception {
    CliConfig config = newConfig();

    assertFalse( config.set( CliConfig.KEY_AUTH_BROKER_READ_TIMEOUT_SECONDS, "not-a-number" ) );
    assertEquals( 30, config.getBrokerReadTimeoutSeconds() );
  }

  @Test
  public void preferredIdpIsTrimmedAndBlankRemovesTheProperty() throws Exception {
    CliConfig config = newConfig();

    assertTrue( config.set( CliConfig.KEY_AUTH_PREFERRED_IDP, "  keycloak  " ) );
    assertEquals( "keycloak", config.getAuthPreferredIdp() );

    assertTrue( config.set( CliConfig.KEY_AUTH_PREFERRED_IDP, "   " ) );
    assertNull( config.getAuthPreferredIdp() );
  }

  private CliConfig newConfig() throws Exception {
    Constructor<CliConfig> constructor = CliConfig.class.getDeclaredConstructor();
    constructor.setAccessible( true );
    return constructor.newInstance();
  }

  private void deleteRecursively( Path path ) throws IOException {
    if ( path == null || Files.notExists( path ) ) {
      return;
    }
    Files.walk( path )
      .sorted( java.util.Comparator.reverseOrder() )
      .forEach( p -> {
        try {
          Files.deleteIfExists( p );
        } catch ( IOException ignored ) {
          // best-effort cleanup for temp test files
        }
      } );
  }

}
