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

package org.pentaho.di.ui.repository.exception;

import java.util.regex.Pattern;

import org.pentaho.di.repository.KettleAuthenticationException;
import org.pentaho.di.repository.KettleRepositoryLostException;

/**
 * Utility class for handling repository-related exceptions and session expiry detection.
 * Provides shared logic for identifying authentication/session expiry exceptions across the application.
 */
public class RepositoryExceptionUtils {

  private static final String[] AUTH_KEYWORDS = {
      "session expired", "unauthorized", "authentication",
      "close method has already been invoked"
  };

  private static final Pattern STATUS_401_PATTERN =
      Pattern.compile( "\\b401\\b" );

  private RepositoryExceptionUtils() {
    // Utility class - prevent instantiation
  }

  /**
   * Check if an exception indicates session expiry or authentication failure.
   * Examines the entire exception cause chain for session-related indicators.
   *
   * @param throwable The exception to check
   * @return true if the exception indicates session expiry/authentication failure, false otherwise
   */
  public static boolean isSessionExpired( Throwable throwable ) {
    return isSessionExpired( throwable, new java.util.HashSet<>() );
  }

  /**
   * Internal recursive helper that tracks already-visited throwables to handle
   * cyclic cause graphs (e.g. A→B→A) without causing a StackOverflowError.
   *
   * @param throwable The exception to check
   * @param visited   Set of throwables already examined in this traversal
   * @return true if the exception indicates session expiry/authentication failure, false otherwise
   */
  private static boolean isSessionExpired( Throwable throwable, java.util.Set<Throwable> visited ) {
    if ( throwable == null || !visited.add( throwable ) ) {
      return false;
    }

    // Check for KettleAuthenticationException directly
    if ( throwable instanceof KettleAuthenticationException ) {
      return true;
    }

    // Check for KettleRepositoryLostException which may wrap auth exceptions
    if ( throwable instanceof KettleRepositoryLostException && isSessionExpired( throwable.getCause(), visited ) ) {
      return true;
    }

    // Check exception message for session indicators
    String message = throwable.getMessage();
    if ( message != null && containsAnyKeyword( message.toLowerCase() ) ) {
      return true;
    }

    // Recursively check cause chain, cycle-safe via visited set
    return isSessionExpired( throwable.getCause(), visited );
  }

  /**
   * Check if a message contains any of the authentication-related keywords
   *
   * @param lowerMsg The lowercase message to check
   * @return true if any keyword is found, false otherwise
   */
  private static boolean containsAnyKeyword( String lowerMsg ) {
    for ( String keyword : AUTH_KEYWORDS ) {
      if ( lowerMsg.contains( keyword ) ) {
        return true;
      }
    }
    return STATUS_401_PATTERN.matcher( lowerMsg ).find();
  }
}
