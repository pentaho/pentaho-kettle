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

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Manages authentication sessions for Pentaho repositories using Factory pattern.
 * 
 * This class uses the Strategy pattern with Factory for extensibility.
 * Use AuthenticationContext through this manager to handle all authentication operations.
 *
 * Uses a thread-safe lazy-initialized singleton with a volatile field, allowing
 * instance replacement for testing and future WebSpoon session-scoped support.
 */
public class SpoonSessionManager {

  private static final LogChannelInterface log = new LogChannel( "SpoonSessionManager" );

  private static final AtomicReference<SpoonSessionManager> instance = new AtomicReference<>();

  // Map of server key to authentication context
  private final Map<String, AuthenticationContext> authContexts = new ConcurrentHashMap<>();

  SpoonSessionManager() {
    // Package-private constructor to allow testing
  }

  /**
   * Returns the single instance of this class, creating it on first access.
   * Thread-safe via AtomicReference compareAndSet.
   *
   * @return The singleton SpoonSessionManager instance
   */
  public static SpoonSessionManager getInstance() {
    SpoonSessionManager mgr = instance.get();
    if ( mgr == null ) {
      instance.compareAndSet( null, new SpoonSessionManager() );
      mgr = instance.get();
    }
    return mgr;
  }

  /**
   * Replaces the singleton instance. Intended for testing and WebSpoon session-scoped usage.
   *
   * @param manager The instance to use, or {@code null} to reset so the next
   *                {@link #getInstance()} call creates a fresh instance.
   */
  static void setInstance( SpoonSessionManager manager ) {
    instance.set( manager );
  }

  /**
   * Get authentication context for a server URI
   *
   * @param serverUri The server URI
   * @return Authentication context for the server
   */
  public AuthenticationContext getAuthenticationContext( URI serverUri ) {
    String key = getKey( serverUri );
    return authContexts.computeIfAbsent( key, 
      k -> new AuthenticationContext( serverUri ) );
  }

  /**
   * Get authentication context for a server URL
   *
   * @param serverUrl The server URL as a string
   * @return Authentication context for the server, or null if URL is invalid
   */
  public AuthenticationContext getAuthenticationContext( String serverUrl ) {
    try {
      URI serverUri = new URI( serverUrl );
      return getAuthenticationContext( serverUri );
    } catch ( Exception e ) {
      log.logError( "Failed to get authentication context for URL: " + serverUrl, e );
      return null;
    }
  }


  /**
   * Get cache key for a server URI
   */
  private String getKey( URI serverUri ) {
    int port = serverUri.getPort();
    if ( port == -1 ) {
      port = "https".equals( serverUri.getScheme() ) ? 443 : 80;
    }
    return serverUri.getHost() + ":" + port;
  }
}