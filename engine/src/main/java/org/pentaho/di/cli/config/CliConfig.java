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

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.cli.auth.store.CredentialFileSupport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Shared configuration manager for Pan and Kitchen command-line tools.
 * Provides secure storage and retrieval of CLI configuration settings.
 */
@SuppressWarnings( "java:S6548" )
public class CliConfig {

  private static String message( String key, String... tokens ) {
    return BaseMessages.getString( CliConfig.class, key, tokens );
  }

  public static final String CONFIG_FILE_NAME = "cli-config.properties";
  public static final String KEY_AUTH_TIMEOUT_SECONDS = "auth.timeout.seconds";

  /**
   * PanConfig key to override the broker HTTP read timeout in seconds.
   * <p>
   * Raise this when debugging server-side code — breakpoints stall response
   * delivery and the default 30-second timeout fires before the breakpoint is
   * resumed. Setting a higher value (e.g., 300) prevents spurious
   * SocketTimeoutException during debug sessions.
   * <p>
   * Valid range: 10–600 seconds. Default: 30.
   * <p>
   * Usage: {@code Pan.bat -config:set auth.broker.read.timeout.seconds 300}
   */
  public static final String KEY_AUTH_BROKER_READ_TIMEOUT_SECONDS = "auth.broker.read.timeout.seconds";
  public static final String KEY_AUTH_PREFERRED_IDP = "auth.preferred.idp";
  public static final String KEY_TOKEN_STORE_BACKEND = "token.store.backend";

  public static final String TOKEN_STORE_BACKEND_ENC = "enc";
  public static final String TOKEN_STORE_BACKEND_FILE = "file";

  private static final int DEFAULT_AUTH_TIMEOUT_SECONDS = 300;
  private static final int DEFAULT_AUTH_BROKER_READ_TIMEOUT_SECONDS = 30;
  public static final String DEFAULT_PREFERRED_IDP = "";
  private static final String DEFAULT_TOKEN_STORE_BACKEND = TOKEN_STORE_BACKEND_FILE;

  private final Properties cliConfigProperties = new Properties();
  private final File configFile;

  private CliConfig() {
    this.configFile = new File( Const.getKettleDirectory(), CONFIG_FILE_NAME );
    loadCliConfig();
  }

  public static CliConfig getInstance() {
    return InstanceHolder.INSTANCE;
  }

  private static final class InstanceHolder {
    private static final CliConfig INSTANCE = new CliConfig();
  }

  public synchronized int getBrokerReadTimeoutSeconds() {
    String value = cliConfigProperties.getProperty( KEY_AUTH_BROKER_READ_TIMEOUT_SECONDS,
      String.valueOf( DEFAULT_AUTH_BROKER_READ_TIMEOUT_SECONDS ) );
    try {
      int timeout = Integer.parseInt( value );
      return ( timeout >= 10 && timeout <= 600 ) ? timeout : DEFAULT_AUTH_BROKER_READ_TIMEOUT_SECONDS;
    } catch ( NumberFormatException e ) {
      return DEFAULT_AUTH_BROKER_READ_TIMEOUT_SECONDS;
    }
  }

  public synchronized boolean setBrokerReadTimeoutSeconds( int seconds ) {
    if ( seconds < 10 || seconds > 600 ) {
      printError( message( "CliConfig.InvalidBrokerReadTimeoutRange" ) );
      return false;
    }
    cliConfigProperties.setProperty( KEY_AUTH_BROKER_READ_TIMEOUT_SECONDS, String.valueOf( seconds ) );
    return saveCliConfig();
  }

  public synchronized int getAuthTimeoutSeconds() {
    String value = cliConfigProperties.getProperty( KEY_AUTH_TIMEOUT_SECONDS,
      String.valueOf( DEFAULT_AUTH_TIMEOUT_SECONDS ) );
    try {
      int timeout = Integer.parseInt( value );
      return ( timeout >= 30 && timeout <= 1800 ) ? timeout : DEFAULT_AUTH_TIMEOUT_SECONDS;
    } catch ( NumberFormatException e ) {
      return DEFAULT_AUTH_TIMEOUT_SECONDS;
    }
  }

  public synchronized boolean setAuthTimeoutSeconds( int seconds ) {
    if ( seconds < 30 || seconds > 1800 ) {
      printError( message( "CliConfig.InvalidAuthTimeoutRange" ) );
      return false;
    }
    cliConfigProperties.setProperty( KEY_AUTH_TIMEOUT_SECONDS, String.valueOf( seconds ) );
    return saveCliConfig();
  }

  public synchronized String getTokenStoreBackend() {
    String value = cliConfigProperties.getProperty( KEY_TOKEN_STORE_BACKEND, DEFAULT_TOKEN_STORE_BACKEND );
    if ( TOKEN_STORE_BACKEND_FILE.equals( value ) || TOKEN_STORE_BACKEND_ENC.equals( value ) ) {
      return value;
    }
    printError( message( "CliConfig.InvalidTokenStoreBackendUsingDefault",
      value, DEFAULT_TOKEN_STORE_BACKEND ) );
    return DEFAULT_TOKEN_STORE_BACKEND;
  }

  public synchronized boolean setTokenStoreBackend( String backend ) {
    if ( !TOKEN_STORE_BACKEND_ENC.equals( backend ) && !TOKEN_STORE_BACKEND_FILE.equals( backend ) ) {
      printError( message( "CliConfig.InvalidTokenStoreBackend" ) );
      return false;
    }
    cliConfigProperties.setProperty( KEY_TOKEN_STORE_BACKEND, backend );
    return saveCliConfig();
  }

  public synchronized String getAuthPreferredIdp() {
    return trimToNull( cliConfigProperties.getProperty( KEY_AUTH_PREFERRED_IDP ) );
  }

  public synchronized boolean setAuthPreferredIdp( String preferredIdp ) {
    String normalizedPreferredIdp = trimToNull( preferredIdp );
    if ( normalizedPreferredIdp == null ) {
      cliConfigProperties.remove( KEY_AUTH_PREFERRED_IDP );
    } else {
      cliConfigProperties.setProperty( KEY_AUTH_PREFERRED_IDP, normalizedPreferredIdp );
    }
    return saveCliConfig();
  }

  public synchronized String get( String key ) {
    return key != null ? cliConfigProperties.getProperty( key ) : null;
  }

  public synchronized boolean set( String key, String value ) {
    if ( key == null || value == null ) {
      return false;
    }

    return switch ( key ) {
      case KEY_AUTH_TIMEOUT_SECONDS -> {
        try {
          yield setAuthTimeoutSeconds( Integer.parseInt( value ) );
        } catch ( NumberFormatException e ) {
          printError( message( "CliConfig.InvalidNumberFormat", key ) );
          yield false;
        }
      }
      case KEY_TOKEN_STORE_BACKEND -> setTokenStoreBackend( value );
      case KEY_AUTH_BROKER_READ_TIMEOUT_SECONDS -> {
        try {
          yield setBrokerReadTimeoutSeconds( Integer.parseInt( value ) );
        } catch ( NumberFormatException e ) {
          printError( message( "CliConfig.InvalidNumberFormat", key ) );
          yield false;
        }
      }
      case KEY_AUTH_PREFERRED_IDP -> setAuthPreferredIdp( value );
      default -> {
        cliConfigProperties.setProperty( key, value );
        yield saveCliConfig();
      }
    };
  }

  public synchronized void setProperty( String key, String value ) {
    if ( !set( key, value ) ) {
      throw new IllegalArgumentException( message( "CliConfig.SetPropertyFailed", key ) );
    }
  }

  public synchronized boolean remove( String key ) {
    if ( key == null ) {
      return false;
    }
    cliConfigProperties.remove( key );
    return saveCliConfig();
  }

  public synchronized Properties list() {
    Properties copy = new Properties();
    copy.putAll( cliConfigProperties );
    return copy;
  }

  public synchronized void reset() {
    resetToDefaults();
  }

  public synchronized void resetToDefaults() {
    cliConfigProperties.clear();
    cliConfigProperties.setProperty( KEY_AUTH_TIMEOUT_SECONDS, String.valueOf( DEFAULT_AUTH_TIMEOUT_SECONDS ) );
    cliConfigProperties.setProperty( KEY_TOKEN_STORE_BACKEND, DEFAULT_TOKEN_STORE_BACKEND );
    cliConfigProperties.setProperty( KEY_AUTH_PREFERRED_IDP, DEFAULT_PREFERRED_IDP );
    saveCliConfig();
  }

  public File getConfigFile() {
    return configFile;
  }

  public String getConfigFilePath() {
    return configFile.getAbsolutePath();
  }

  private void loadCliConfig() {
    File sourceFile = resolveSourceFile();
    if ( sourceFile == null ) {
      resetToDefaults();
      return;
    }

    try ( FileInputStream inputStream = new FileInputStream( sourceFile ) ) {
      cliConfigProperties.load( inputStream );
    } catch ( IOException e ) {
      printError( message( "CliConfig.LoadFailedUsingDefaults" ) );
      resetToDefaults();
    }
  }

  private File resolveSourceFile() {
    if ( configFile.exists() ) {
      return configFile;
    }

    return null;
  }

  private boolean saveCliConfig() {
    try {
      CredentialFileSupport.ensureParentDirectoryExists( configFile );
      try ( FileOutputStream outputStream = new FileOutputStream( configFile ) ) {
        cliConfigProperties.store( outputStream, message( "CliConfig.PropertiesComment" ) );
      }
      setRestrictiveFilePermissions( configFile );
      return true;
    } catch ( IOException e ) {
      printError( message( "CliConfig.SaveFailed" ) );
      return false;
    }
  }

  private void setRestrictiveFilePermissions( File file ) {
    CredentialFileSupport.applyOwnerOnlyPermissions( file, message( "CliConfig.TargetDescription" ) );
  }

  private static String trimToNull( String value ) {
    return StringUtils.isBlank( value ) ? null : value.trim();
  }

  @SuppressWarnings( "java:S106" )
  private void printError( String message ) {
    System.err.println( message );
  }
}
