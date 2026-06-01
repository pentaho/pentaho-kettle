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

package org.pentaho.di.ui.spoon.session;

import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory class for creating authentication strategy instances.
 * <p>
 * This factory implements the Factory design pattern, providing a single point
 * for creating authentication strategies. This makes it easy to add new
 * authentication mechanisms without changing client code.
 * <p>
 * Usage:
 * <pre>
 * // Get the default strategy (SESSION)
 * AuthenticationStrategy strategy = AuthenticationStrategyFactory.getDefaultStrategy();
 *
 * // Register a custom strategy (for future extensions like OAuth, SAML)
 * AuthenticationStrategyFactory.registerStrategy( "CUSTOM", new CustomAuthStrategy() );
 * </pre>
 */
public class AuthenticationStrategyFactory {

  private static final LogChannelInterface log = new LogChannel( "AuthenticationStrategyFactory" );

  // Registry of authentication strategies
  private static final Map<String, AuthenticationStrategy> strategyRegistry = new ConcurrentHashMap<>();

  // Default strategy type
  private static final String DEFAULT_STRATEGY_TYPE = "SESSION";

  // Static initialization - register built-in strategies
  static {
    registerStrategy( DEFAULT_STRATEGY_TYPE, new SessionBasedAuthStrategy() );
  }

  /**
   * Private constructor to prevent instantiation
   */
  private AuthenticationStrategyFactory() {
  }

  /**
   * Get the default authentication strategy (SESSION-based)
   *
   * @return The default authentication strategy
   */
  public static AuthenticationStrategy getDefaultStrategy() {
    return getStrategy( DEFAULT_STRATEGY_TYPE );
  }

  /**
   * Get an authentication strategy by type
   *
   * @param authType The authentication type (e.g., "SESSION")
   * @return The authentication strategy, or default strategy if type not found
   */
  public static AuthenticationStrategy getStrategy( String authType ) {
    if ( authType == null || authType.trim().isEmpty() ) {
      log.logError( "Authentication type cannot be null or empty, returning default strategy" );
      return strategyRegistry.get( DEFAULT_STRATEGY_TYPE );
    }

    String normalizedType = authType.trim().toUpperCase();
    AuthenticationStrategy strategy = strategyRegistry.get( normalizedType );

    if ( strategy == null ) {
      log.logError( "Authentication strategy '" + authType + "' not found, returning default strategy" );
      return strategyRegistry.get( DEFAULT_STRATEGY_TYPE );
    }

    log.logDetailed( "Retrieved authentication strategy: " + normalizedType );
    return strategy;
  }

  /**
   * Register a new authentication strategy
   *
   * @param authType The authentication type identifier
   * @param strategy The authentication strategy instance
   */
  public static void registerStrategy( String authType, AuthenticationStrategy strategy ) {
    if ( authType == null || authType.trim().isEmpty() ) {
      log.logError( "Cannot register strategy with null or empty type" );
      return;
    }

    if ( strategy == null ) {
      log.logError( "Cannot register null strategy" );
      return;
    }

    String normalizedType = authType.trim().toUpperCase();
    strategyRegistry.put( normalizedType, strategy );
    log.logBasic( "Registered authentication strategy: " + normalizedType );
  }
}
