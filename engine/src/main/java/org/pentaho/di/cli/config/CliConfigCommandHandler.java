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

import java.util.Map;
import java.util.Properties;

/**
 * Handles shared CLI configuration commands similar to 'npm config' or 'git config'.
 * <p>
 * Usage:
 * <pre>
 * Pan.bat -config:set <key> <value> - Set a configuration value
 * Pan.bat -config:get <key> - Get a configuration value
 * Pan.bat -config:delete <key> - Delete a configuration value
 * Pan.bat -config:list - List all configuration values
 * Pan.bat -config:reset - Reset to defaults
 * Pan.bat -config:path - Show configuration file path
 * </pre>
 * <p>
 * Example:
 * Pan.bat -config:set auth.timeout.seconds 600
 * Pan.bat -config:set auth.strict.user.match true
 */

public class CliConfigCommandHandler {

  private static final String CONFIG_PREFIX = "-config:";
  private static final String DEFAULT_SUFFIX = " (default)";

  private final CliConfig cliConfig;

  public CliConfigCommandHandler( ) {
    this( CliConfig.getInstance() );
  }

  CliConfigCommandHandler( CliConfig cliConfig ) {
    this.cliConfig = cliConfig;
  }

  /**
   * Check if the given arguments contain a config command.
   *
   * @param args Command line arguments
   * @return true if a config command is present
   */
  public static boolean hasConfigCommand( String[] args ) {
    if ( args == null ) {
      return false;
    }
    for ( String arg : args ) {
      if ( isConfigCommand( arg ) ) {
        return true;
      }
    }
    return false;
  }

  /**
   * Execute the config command and return the exit code.
   *
   * @param args Command line arguments
   * @return Exit code (0 for success, non-zero for error)
   */
  public int execute( String[] args ) {
    if ( args == null || args.length == 0 ) {
      printUsage();
      return 1;
    }

    String command = null;
    int commandIndex = -1;
    for ( int i = 0; i < args.length; i++ ) {
      if ( isConfigCommand( args[ i ] ) ) {
        command = args[ i ].substring( CONFIG_PREFIX.length() ).toLowerCase();
        commandIndex = i;
        break;
      }
    }

    if ( command == null ) {
      printError( "No config command found" );
      printUsage();
      return 1;
    }

    int argsStart = commandIndex + 1;

    switch ( command ) {
      case "set":
        return handleSet( args, argsStart );
      case "get":
        return handleGet( args, argsStart );
      case "delete", "remove":
        return handleDelete( args, argsStart );
      case "list":
        return handleList();
      case "reset":
        return handleReset();
      case "path":
        return handlePath();
      case "help":
        printUsage();
        return 0;
      default:
        printError( "Unknown config command: " + command );
        printUsage();
        return 1;
    }
  }

  private static boolean isConfigCommand( String arg ) {
    if ( arg == null ) {
      return false;
    }
    return arg.toLowerCase().startsWith( CONFIG_PREFIX );
  }

  private int handleSet( String[] args, int argsStart ) {
    if ( args.length < argsStart + 1 ) {
      printError( "Usage: Pan.bat -config:set <key> <value>" );
      print( "" );
      printAvailableKeys();
      return 1;
    }

    String key = args[ argsStart ];
    String value;
    int separatorIndex = key.indexOf( '=' );
    if ( separatorIndex > 0 ) {
      value = key.substring( separatorIndex + 1 );
      key = key.substring( 0, separatorIndex );
    } else if ( args.length >= argsStart + 2 ) {
      value = args[ argsStart + 1 ];
    } else {
      printError( "Usage: Pan.bat -config:set <key> <value>" );
      print( "" );
      printAvailableKeys();
      return 1;
    }

    if ( !isKnownKey( key ) ) {
      printError( "Unknown configuration key: " + key );
      print( "" );
      printAvailableKeys();
      return 1;
    }

    boolean success = cliConfig.set( key, value );
    if ( success ) {
      print( "Set " + key + " = " + value );
      return 0;
    }

    printError( "Failed to set " + key + ". Value may be invalid." );
    return 1;
  }

  private int handleGet( String[] args, int argsStart ) {
    if ( args.length < argsStart + 1 ) {
      printError( "Usage: Pan.bat -config:get <key>" );
      print( "" );
      printAvailableKeys();
      return 1;
    }

    String key = args[ argsStart ];
    String value = cliConfig.get( key );
    if ( value != null ) {
      print( key + " = " + value );
      return 0;
    }

    if ( CliConfig.KEY_AUTH_TIMEOUT_SECONDS.equals( key ) ) {
      print( key + " = " + cliConfig.getAuthTimeoutSeconds() + DEFAULT_SUFFIX );
    } else if ( CliConfig.KEY_TOKEN_STORE_BACKEND.equals( key ) ) {
      print( key + " = " + cliConfig.getTokenStoreBackend() + DEFAULT_SUFFIX );
    } else if ( CliConfig.KEY_AUTH_BROKER_READ_TIMEOUT_SECONDS.equals( key ) ) {
      print( key + " = " + cliConfig.getBrokerReadTimeoutSeconds() + DEFAULT_SUFFIX );
    } else {
      print( key + " is not set" );
    }
    return 0;
  }

  private int handleDelete( String[] args, int argsStart ) {
    if ( args.length < argsStart + 1 ) {
      printError( "Usage: Pan.bat -config:delete <key>" );
      return 1;
    }

    String key = args[ argsStart ];
    if ( cliConfig.remove( key ) ) {
      print( "Deleted " + key );
      return 0;
    }

    printError( "Failed to delete " + key );
    return 1;
  }

  private int handleList() {
    Properties properties = cliConfig.list();
    if ( properties.isEmpty() ) {
      print( "No configuration values set." );
      print( "Using defaults:" );
      print( "  " + CliConfig.KEY_AUTH_TIMEOUT_SECONDS + " = " + cliConfig.getAuthTimeoutSeconds() );
      print( "  " + CliConfig.KEY_TOKEN_STORE_BACKEND + " = " + cliConfig.getTokenStoreBackend() );
      print( "  " + CliConfig.KEY_AUTH_BROKER_READ_TIMEOUT_SECONDS + " = "
        + cliConfig.getBrokerReadTimeoutSeconds() );
    } else {
      print( "Current configuration:" );
      for ( Map.Entry<Object, Object> entry : properties.entrySet() ) {
        print( "  " + entry.getKey() + " = " + entry.getValue() );
      }
    }

    print( "" );
    print( "Configuration file: " + cliConfig.getConfigFilePath() );
    return 0;
  }

  private int handleReset() {
    cliConfig.resetToDefaults();
    print( "Configuration reset to defaults." );
    return 0;
  }

  private int handlePath() {
    print( cliConfig.getConfigFilePath() );
    return 0;
  }

  private boolean isKnownKey( String key ) {
    return CliConfig.KEY_AUTH_TIMEOUT_SECONDS.equals( key )
      || CliConfig.KEY_TOKEN_STORE_BACKEND.equals( key )
      || CliConfig.KEY_AUTH_BROKER_READ_TIMEOUT_SECONDS.equals( key )
      || CliConfig.KEY_AUTH_PREFERRED_IDP.equals( key );
  }

  private void printAvailableKeys() {
    print( "Available configuration keys:" );
    print( "  " + CliConfig.KEY_AUTH_TIMEOUT_SECONDS );
    print( "    Timeout in seconds for browser authentication (30-1800)." );
    print( "    Example: 300" );
    print( "" );
    print( "  " + CliConfig.KEY_TOKEN_STORE_BACKEND );
    print( "    Token storage backend to use for persisting OAuth tokens and sessions." );
    print( "    \"file\" - POSIX 600 plaintext file (~/.kettle/.kettle-sessions)" );
    print( "    Default: file" );
    print( "    Example: file" );
    print( "" );
    print( "  " + CliConfig.KEY_AUTH_PREFERRED_IDP );
    print( "    Preferred broker registration_id to use when multiple IdPs are enabled." );
    print( "    Example: keycloak" );
    print( "" );
  }

  private void printUsage() {
    print( "CLI Configuration Commands:" );
    print( "" );
    print( "  Pan.bat -config:set <key> <value>   Set a configuration value" );
    print( "  Pan.bat -config:get <key>           Get a configuration value" );
    print( "  Pan.bat -config:delete <key>        Delete a configuration value" );
    print( "  Pan.bat -config:list                List all configuration values" );
    print( "  Pan.bat -config:reset               Reset to defaults" );
    print( "  Pan.bat -config:path                Show configuration file path" );
    print( "  Pan.bat -config:help                Show this help" );
    print( "" );
    print( "Examples:" );
    print( "  Pan.bat -config:set auth.timeout.seconds 600" );
    print( "  Pan.bat -config:set auth.strict.user.match true" );
    print( "  Pan.bat -config:set auth.preferred.idp keycloak" );
    print( "  Pan.bat -config:set token.store.backend file" );
    print( "  Pan.bat -config:list" );
    print( "  Same commands also work with Kitchen.bat" );
    print( "" );
    printAvailableKeys();
  }

  @SuppressWarnings( "java:S106" )
  private void printError( String message ) {
    System.err.println( message );
  }

  @SuppressWarnings( "java:S106" )
  private void print( String message ) {
    System.out.println( message );
  }
}
