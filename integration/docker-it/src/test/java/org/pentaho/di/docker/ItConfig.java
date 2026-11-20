/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/

package org.pentaho.di.docker;

import java.time.Duration;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/**
 * Reads the container ids and the settings that the Failsafe configuration passes as system properties.
 */
final class ItConfig {

  private ItConfig() {
  }

  static String pdiContainerId() {
    return required( "pdiContainerId" );
  }

  static String serverContainerId() {
    return required( "pentahoServerContainerId" );
  }

  static String postgresContainerId() {
    return required( "postgresContainerId" );
  }

  static String pdiHome() {
    return required( "pdi-plugin-tester.pdi-home" );
  }

  static String serverHome() {
    return required( "pdi-plugin-tester.server-home" );
  }

  static int carteHostPort() {
    return Integer.parseInt( required( "pdi-plugin-tester.carte-host-port" ) );
  }

  static int serverHostPort() {
    return Integer.parseInt( required( "pdi-plugin-tester.server-host-port" ) );
  }

  static Duration startupTimeout() {
    return Duration.ofMillis( Long.parseLong( required( "pdi-plugin-tester.startup-timeout-ms" ) ) );
  }

  static Duration serverStartupTimeout() {
    return Duration.ofMillis( Long.parseLong( required( "pdi-plugin-tester.server-startup-timeout-ms" ) ) );
  }

  static Set<ExecutionType> executionTypes() {
    Set<ExecutionType> types = EnumSet.noneOf( ExecutionType.class );
    Arrays.stream( required( "pdi-plugin-tester.execution-types" ).split( "," ) )
      .map( String::trim )
      .filter( value -> !value.isEmpty() )
      .map( value -> ExecutionType.valueOf( value.toUpperCase( Locale.ROOT ) ) )
      .forEach( types::add );
    return types;
  }

  private static String required( String name ) {
    String value = System.getProperty( name );
    if ( value == null || value.isBlank() ) {
      throw new IllegalStateException( "Missing system property " + name );
    }
    return value.trim();
  }
}
