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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.pentaho.di.repository.KettleAuthenticationException;
import org.pentaho.di.repository.KettleRepositoryLostException;


public class RepositoryExceptionUtilsTest {


  @Test
  public void returnsFalseForNullThrowable() {
    assertFalse( RepositoryExceptionUtils.isSessionExpired( null ) );
  }


  @Test
  public void returnsTrueForKettleAuthenticationException() {
    assertTrue( RepositoryExceptionUtils.isSessionExpired( new KettleAuthenticationException() ) );
  }

  @Test
  public void returnsTrueForKettleAuthenticationExceptionSubclass() {
    assertTrue( RepositoryExceptionUtils.isSessionExpired(
        new KettleAuthenticationException( "msg", null ) ) );
  }

  @Test
  public void returnsTrueForRepoLostExceptionWithAuthCause() {
    KettleAuthenticationException authEx = new KettleAuthenticationException();
    KettleRepositoryLostException repoLost = new KettleRepositoryLostException( authEx );

    assertTrue( RepositoryExceptionUtils.isSessionExpired( repoLost ) );
  }

  @Test
  public void returnsFalseForRepoLostExceptionWithUnrelatedCause() {
    KettleRepositoryLostException custom =
        new KettleRepositoryLostException( "something broke", new IllegalStateException( "disk full" ) );

    assertFalse( RepositoryExceptionUtils.isSessionExpired( custom ) );
  }

  @Test
  public void returnsFalseForRepoLostExceptionWithNullCause() {
    KettleRepositoryLostException repoLost = new KettleRepositoryLostException( "no cause", null );

    assertFalse( RepositoryExceptionUtils.isSessionExpired( repoLost ) );
  }

  // ===== Message keyword matching =====

  @Test
  public void returnsTrueForMessageContainingSessionExpired() {
    assertTrue( RepositoryExceptionUtils.isSessionExpired(
        new RuntimeException( "The session expired for this user" ) ) );
  }

  @Test
  public void returnsTrueForMessageContaining401() {
    assertTrue( RepositoryExceptionUtils.isSessionExpired(
        new RuntimeException( "Server returned 401 error" ) ) );
  }

  @Test
  public void returnsTrueForBare401Message() {
    assertTrue( RepositoryExceptionUtils.isSessionExpired(
        new RuntimeException( "401" ) ) );
  }

  @Test
  public void returnsFalseFor401EmbeddedInLargerNumber() {
    assertFalse( RepositoryExceptionUtils.isSessionExpired(
        new RuntimeException( "Connected to port 4010" ) ) );
  }

  @Test
  public void returnsFalseFor401AsSubstringOfIdentifier() {
    assertFalse( RepositoryExceptionUtils.isSessionExpired(
        new RuntimeException( "See PDI-10401 for details" ) ) );
  }

  @Test
  public void returnsTrueForMessageContainingUnauthorized() {
    assertTrue( RepositoryExceptionUtils.isSessionExpired(
        new RuntimeException( "Unauthorized access" ) ) );
  }

  @Test
  public void returnsTrueForMessageContainingAuthentication() {
    assertTrue( RepositoryExceptionUtils.isSessionExpired(
        new RuntimeException( "Authentication failed" ) ) );
  }

  @Test
  public void returnsTrueForMessageContainingCloseMethodInvoked() {
    assertTrue( RepositoryExceptionUtils.isSessionExpired(
        new RuntimeException( "close method has already been invoked" ) ) );
  }

  @Test
  public void keywordMatchIsCaseInsensitive() {
    assertTrue( RepositoryExceptionUtils.isSessionExpired(
        new RuntimeException( "SESSION EXPIRED for user" ) ) );
    assertTrue( RepositoryExceptionUtils.isSessionExpired(
        new RuntimeException( "UNAUTHORIZED request" ) ) );
  }

  @Test
  public void returnsFalseForMessageWithNoKeywords() {
    assertFalse( RepositoryExceptionUtils.isSessionExpired(
        new RuntimeException( "File not found on disk" ) ) );
  }

  @Test
  public void returnsFalseForNullMessage() {
    assertFalse( RepositoryExceptionUtils.isSessionExpired( new RuntimeException( (String) null ) ) );
  }

  // ===== ClientTransportException (by class name) =====

  @Test
  public void returnsTrueForClientTransportExceptionWith401() {
    assertTrue( RepositoryExceptionUtils.isSessionExpired(
        new ClientTransportException( "The server sent HTTP status code 401" ) ) );
  }

  @Test
  public void returnsFalseForClientTransportExceptionWithout401() {
    // Message "connection refused" doesn't contain any AUTH_KEYWORDS either
    assertFalse( RepositoryExceptionUtils.isSessionExpired(
        new ClientTransportException( "connection refused" ) ) );
  }

  @Test
  public void returnsFalseForClientTransportExceptionWithNullMessage() {
    assertFalse( RepositoryExceptionUtils.isSessionExpired(
        new ClientTransportException( null ) ) );
  }

  // ===== WebServiceException (by class name) =====

  @Test
  public void returnsTrueForWebServiceExceptionWithCloseMessage() {
    assertTrue( RepositoryExceptionUtils.isSessionExpired(
        new WebServiceException( "close method has already been invoked on this request" ) ) );
  }

  @Test
  public void returnsFalseForWebServiceExceptionWithOtherMessage() {
    assertFalse( RepositoryExceptionUtils.isSessionExpired(
        new WebServiceException( "connection timeout" ) ) );
  }

  @Test
  public void returnsFalseForWebServiceExceptionWithNullMessage() {
    assertFalse( RepositoryExceptionUtils.isSessionExpired(
        new WebServiceException( null ) ) );
  }

  // ===== Cause chain recursion =====

  @Test
  public void returnsTrueWhenAuthExceptionIsNestedInCauseChain() {
    KettleAuthenticationException authEx = new KettleAuthenticationException();
    RuntimeException mid = new RuntimeException( "mid", authEx );
    Exception outer = new Exception( "outer", mid );

    assertTrue( RepositoryExceptionUtils.isSessionExpired( outer ) );
  }

  @Test
  public void returnsTrueWhenKeywordIsInNestedCauseMessage() {
    RuntimeException inner = new RuntimeException( "401 Unauthorized" );
    Exception outer = new Exception( "wrapper", inner );

    assertTrue( RepositoryExceptionUtils.isSessionExpired( outer ) );
  }

  @Test
  public void returnsFalseWhenEntireCauseChainHasNoAuthIndicators() {
    RuntimeException inner = new RuntimeException( "disk full" );
    Exception outer = new Exception( "io error", inner );

    assertFalse( RepositoryExceptionUtils.isSessionExpired( outer ) );
  }

  @Test
  public void returnsFalseWhenCauseIsNull() {
    assertFalse( RepositoryExceptionUtils.isSessionExpired(
        new RuntimeException( "no cause" ) ) );
  }

  @Test
  public void handlesSelfReferencingCauseWithoutStackOverflow() {
    SelfCausingException selfRef = new SelfCausingException( "loop" );

    assertFalse( RepositoryExceptionUtils.isSessionExpired( selfRef ) );
  }

  // ===== Non-matching class names =====

  @Test
  public void returnsFalseForOrdinaryExceptionWithNoKeywords() {
    assertFalse( RepositoryExceptionUtils.isSessionExpired(
        new IllegalArgumentException( "bad input" ) ) );
  }

  // ===== Test doubles that simulate class-name checks without hard dependencies =====

  /**
   * Simulates com.sun.xml.ws.client.ClientTransportException by simple class name.
   */
  static class ClientTransportException extends RuntimeException {
    ClientTransportException( String message ) {
      super( message );
    }
  }

  /**
   * Simulates jakarta.xml.ws.WebServiceException by simple class name.
   */
  static class WebServiceException extends RuntimeException {
    WebServiceException( String message ) {
      super( message );
    }
  }

  /**
   * An exception whose getCause() returns itself, to test infinite-loop guard.
   */
  static class SelfCausingException extends RuntimeException {
    SelfCausingException( String message ) {
      super( message );
    }

    @Override
    public synchronized Throwable getCause() {
      return this;
    }
  }
}

