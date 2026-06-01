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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleException;


public class SessionBasedAuthStrategyTest {

  private SessionBasedAuthStrategy strategy;
  private URI serverUri;

  @BeforeClass
  public static void setUpClass() throws KettleException {
    if ( !KettleClientEnvironment.isInitialized() ) {
      KettleClientEnvironment.init();
    }
  }

  @Before
  public void setUp() throws Exception {
    strategy = new SessionBasedAuthStrategy();
    serverUri = new URI( "http://localhost:8080/pentaho" );
  }

  // ===== getAuthType =====

  @Test
  public void getAuthTypeReturnsSession() {
    assertEquals( "SESSION", strategy.getAuthType() );
  }

  // ===== storeCredentials / getCredentials =====

  @Test
  public void storeAndGetCredentialsRoundTrip() {
    Map<String, Object> creds = new HashMap<>();
    creds.put( "jsessionid", "ABC" );
    creds.put( "extra", "val" );

    strategy.storeCredentials( serverUri, creds );

    Map<String, Object> result = strategy.getCredentials( serverUri );
    assertNotNull( result );
    assertEquals( "ABC", result.get( "jsessionid" ) );
    assertEquals( "val", result.get( "extra" ) );
  }

  @Test
  public void getCredentialsReturnsDefensiveCopy() {
    Map<String, Object> creds = new HashMap<>();
    creds.put( "key", "value" );
    strategy.storeCredentials( serverUri, creds );

    Map<String, Object> first = strategy.getCredentials( serverUri );
    Map<String, Object> second = strategy.getCredentials( serverUri );
    assertNotSame( first, second );
  }

  @Test
  public void storeCredentialsMakesDefensiveCopy() {
    Map<String, Object> creds = new HashMap<>();
    creds.put( "key", "original" );
    strategy.storeCredentials( serverUri, creds );

    creds.put( "key", "modified" );
    assertEquals( "original", strategy.getCredentials( serverUri ).get( "key" ) );
  }

  @Test
  public void isAuthenticatedReturnsTrueWhenJsessionIdStored() {
    strategy.storeJSessionId( serverUri, "SID" );

    assertTrue( strategy.isAuthenticated( serverUri ) );
  }

  @Test
  public void isAuthenticatedReturnsFalseWhenNoCredentials() {
    assertFalse( strategy.isAuthenticated( serverUri ) );
  }

  @Test
  public void isAuthenticatedReturnsFalseWhenCredentialsExistButNoJsessionId() {
    Map<String, Object> creds = new HashMap<>();
    creds.put( "other_key", "value" );
    strategy.storeCredentials( serverUri, creds );

    assertFalse( strategy.isAuthenticated( serverUri ) );
  }

  // ===== clearCredentials =====

  @Test
  public void clearCredentialsRemovesStoredData() {
    strategy.storeJSessionId( serverUri, "SID" );
    assertTrue( strategy.isAuthenticated( serverUri ) );

    strategy.clearCredentials( serverUri );
    assertFalse( strategy.isAuthenticated( serverUri ) );
  }

  @Test
  public void clearCredentialsDoesNotAffectOtherServers() throws Exception {
    URI otherUri = new URI( "http://other:9090/pentaho" );
    strategy.storeJSessionId( serverUri, "S1" );
    strategy.storeJSessionId( otherUri, "S2" );

    strategy.clearCredentials( serverUri );
    assertNotNull( strategy.getCredentials( otherUri ) );
  }

  // ===== getCredentialValue =====

  @Test
  public void getCredentialValueReturnsStringValue() {
    strategy.storeCredentialValue( serverUri, "mykey", "myval" );

    assertEquals( "myval", strategy.getCredentialValue( serverUri, "mykey" ) );
  }

  @Test
  public void getCredentialValueReturnsNullWhenKeyMissing() {
    strategy.storeCredentialValue( serverUri, "exists", "val" );

    assertNull( strategy.getCredentialValue( serverUri, "missing" ) );
  }

  @Test
  public void getCredentialValueReturnsNullWhenNoCredentials() {
    assertNull( strategy.getCredentialValue( serverUri, "anything" ) );
  }

  @Test
  public void storeCredentialValueCreatesEntryIfAbsent() {
    strategy.storeCredentialValue( serverUri, "newkey", "newval" );

    assertEquals( "newval", strategy.getCredentialValue( serverUri, "newkey" ) );
  }

  @Test
  public void storeCredentialValueAddsToExistingEntry() {
    strategy.storeCredentialValue( serverUri, "key1", "val1" );
    strategy.storeCredentialValue( serverUri, "key2", "val2" );

    assertEquals( "val1", strategy.getCredentialValue( serverUri, "key1" ) );
    assertEquals( "val2", strategy.getCredentialValue( serverUri, "key2" ) );
  }

  @Test
  public void storeCredentialValueOverwritesExistingKey() {
    strategy.storeCredentialValue( serverUri, "key", "old" );
    strategy.storeCredentialValue( serverUri, "key", "new" );

    assertEquals( "new", strategy.getCredentialValue( serverUri, "key" ) );
  }

  // ===== getJSessionId =====

  @Test
  public void getJSessionIdReturnsStoredId() {
    strategy.storeJSessionId( serverUri, "JSID_123" );

    assertEquals( "JSID_123", strategy.getJSessionId( serverUri ) );
  }

  @Test
  public void getJSessionIdReturnsNullWhenNotStored() {
    assertNull( strategy.getJSessionId( serverUri ) );
  }

  // ===== storeJSessionId =====

  @Test
  public void storeJSessionIdStoresBothIdAndMarker() {
    strategy.storeJSessionId( serverUri, "JSID" );

    assertEquals( "JSID", strategy.getJSessionId( serverUri ) );
    assertTrue( strategy.hasBrowserAuthMarker( serverUri ) );
  }

  // ===== hasBrowserAuthMarker =====

  @Test
  public void hasBrowserAuthMarkerReturnsTrueAfterStoreJSessionId() {
    strategy.storeJSessionId( serverUri, "JSID" );

    assertTrue( strategy.hasBrowserAuthMarker( serverUri ) );
  }

  @Test
  public void hasBrowserAuthMarkerReturnsFalseWhenNeverStored() {
    assertFalse( strategy.hasBrowserAuthMarker( serverUri ) );
  }

  @Test
  public void hasBrowserAuthMarkerReturnsFalseWhenMarkerIsNotBrowser() {
    strategy.storeCredentialValue( serverUri, "_auth_method_marker", "password" );

    assertFalse( strategy.hasBrowserAuthMarker( serverUri ) );
  }

  // ===== getServerKey: port resolution =====

  @Test
  public void sameServerAndPortShareCredentials() throws Exception {
    URI uri1 = new URI( "http://host:8080/path1" );
    URI uri2 = new URI( "http://host:8080/path2" );
    strategy.storeJSessionId( uri1, "SHARED" );

    assertEquals( "SHARED", strategy.getJSessionId( uri2 ) );
  }

  @Test
  public void differentPortsDoNotShareCredentials() throws Exception {
    URI uri1 = new URI( "http://host:8080/pentaho" );
    URI uri2 = new URI( "http://host:9090/pentaho" );
    strategy.storeJSessionId( uri1, "PORT1" );

    assertNull( strategy.getJSessionId( uri2 ) );
  }

  @Test
  public void httpDefaultPortIs80WhenNotSpecified() throws Exception {
    URI noPort = new URI( "http://host/pentaho" );
    URI explicit80 = new URI( "http://host:80/pentaho" );
    strategy.storeJSessionId( noPort, "HTTP80" );

    assertEquals( "HTTP80", strategy.getJSessionId( explicit80 ) );
  }

  @Test
  public void httpsDefaultPortIs443WhenNotSpecified() throws Exception {
    URI noPort = new URI( "https://host/pentaho" );
    URI explicit443 = new URI( "https://host:443/pentaho" );
    strategy.storeJSessionId( noPort, "HTTPS443" );

    assertEquals( "HTTPS443", strategy.getJSessionId( explicit443 ) );
  }

  @Test
  public void httpAndHttpsDefaultPortsDiffer() throws Exception {
    URI http = new URI( "http://host/pentaho" );
    URI https = new URI( "https://host/pentaho" );
    strategy.storeJSessionId( http, "HTTP_SESSION" );

    assertNull( strategy.getJSessionId( https ) );
  }

  @Test
  public void httpAndHttpsSameExplicitPortDoNotShareCredentials() throws Exception {
    URI http = new URI( "http://host:443/pentaho" );
    URI https = new URI( "https://host:443/pentaho" );
    strategy.storeJSessionId( http, "HTTP_ON_443" );

    assertNull( strategy.getJSessionId( https ) );
  }

  @Test
  public void explicitPortOverridesDefaultSchemePort() throws Exception {
    URI custom = new URI( "http://host:9999/pentaho" );
    URI defaultPort = new URI( "http://host/pentaho" );
    strategy.storeJSessionId( custom, "CUSTOM" );

    assertNull( strategy.getJSessionId( defaultPort ) );
  }

  // ===== clearCredentialValue =====

  @Test
  public void clearCredentialValueRemovesSpecificKey() {
    strategy.storeCredentialValue( serverUri, "key1", "val1" );
    strategy.storeCredentialValue( serverUri, "key2", "val2" );

    strategy.clearCredentialValue( serverUri, "key1" );

    assertNull( strategy.getCredentialValue( serverUri, "key1" ) );
    assertEquals( "val2", strategy.getCredentialValue( serverUri, "key2" ) );
  }

  @Test
  public void clearCredentialValueRemovesJsessionId() {
    strategy.storeJSessionId( serverUri, "JSID_123" );
    assertTrue( strategy.isAuthenticated( serverUri ) );

    strategy.clearCredentialValue( serverUri, "jsessionid" );

    assertFalse( strategy.isAuthenticated( serverUri ) );
    assertTrue( strategy.hasBrowserAuthMarker( serverUri ) );
  }

  @Test
  public void clearCredentialValueRemovesEntireMapWhenEmpty() {
    strategy.storeCredentialValue( serverUri, "onlykey", "onlyval" );

    strategy.clearCredentialValue( serverUri, "onlykey" );

    assertEquals( 0, strategy.getCredentials( serverUri ).size() );
  }

  @Test
  public void clearCredentialValuePreservesOtherKeys() {
    strategy.storeCredentialValue( serverUri, "keep1", "val1" );
    strategy.storeCredentialValue( serverUri, "remove", "remove_val" );
    strategy.storeCredentialValue( serverUri, "keep2", "val2" );

    strategy.clearCredentialValue( serverUri, "remove" );

    assertEquals( "val1", strategy.getCredentialValue( serverUri, "keep1" ) );
    assertEquals( "val2", strategy.getCredentialValue( serverUri, "keep2" ) );
    assertNull( strategy.getCredentialValue( serverUri, "remove" ) );
  }

  @Test
  public void clearCredentialValueDoesNotAffectOtherServers() throws Exception {
    URI otherUri = new URI( "http://other:9090/pentaho" );
    strategy.storeCredentialValue( serverUri, "shared_key", "val1" );
    strategy.storeCredentialValue( otherUri, "shared_key", "val2" );

    strategy.clearCredentialValue( serverUri, "shared_key" );

    assertEquals( "val2", strategy.getCredentialValue( otherUri, "shared_key" ) );
  }

  @Test
  public void clearCredentialValueWhenServerNotExists() {
    strategy.clearCredentialValue( serverUri, "nonexistent" );

    assertTrue( strategy.getCredentials( serverUri ).isEmpty() );
  }

  @Test
  public void clearCredentialValueIsIdempotent() {
    strategy.storeCredentialValue( serverUri, "key", "val" );

    strategy.clearCredentialValue( serverUri, "key" );
    strategy.clearCredentialValue( serverUri, "key" );

    assertNull( strategy.getCredentialValue( serverUri, "key" ) );
  }

  @Test( expected = IllegalArgumentException.class )
  public void clearCredentialValueThrowsOnNullServerUri() {
    strategy.clearCredentialValue( null, "key" );
  }

  @Test( expected = IllegalArgumentException.class )
  public void clearCredentialValueThrowsOnNullCredentialKey() {
    strategy.clearCredentialValue( serverUri, null );
  }

  @Test( expected = IllegalArgumentException.class )
  public void clearCredentialValueThrowsOnEmptyCredentialKey() {
    strategy.clearCredentialValue( serverUri, "" );
  }

  @Test( expected = IllegalArgumentException.class )
  public void clearCredentialValueThrowsOnWhitespaceCredentialKey() {
    strategy.clearCredentialValue( serverUri, "   " );
  }

  @Test
  public void clearCredentialValuePreservesBrowserAuthMarker() {
    strategy.storeJSessionId( serverUri, "JSID" );
    strategy.storeCredentialValue( serverUri, "extra", "data" );

    strategy.clearCredentialValue( serverUri, "extra" );

    assertTrue( strategy.hasBrowserAuthMarker( serverUri ) );
    assertEquals( "JSID", strategy.getJSessionId( serverUri ) );
  }

  // ===== clearAll =====

  @Test
  public void clearAllRemovesAllServerCredentials() throws URISyntaxException {
    strategy.storeJSessionId( serverUri, "S1" );
    URI otherUri = new URI( "http://other:9090/pentaho" );
    strategy.storeJSessionId( otherUri, "S2" );

    strategy.clearAll();

    assertFalse( strategy.isAuthenticated( serverUri ) );
    assertFalse( strategy.isAuthenticated( otherUri ) );
  }

  // ===== Input Validation =====

  @Test( expected = IllegalArgumentException.class )
  public void storeCredentialsThrowsOnNullServerUri() {
    strategy.storeCredentials( null, new HashMap<>() );
  }

  @Test( expected = IllegalArgumentException.class )
  public void storeCredentialsThrowsOnNullCredentials() {
    strategy.storeCredentials( serverUri, null );
  }

  @Test( expected = IllegalArgumentException.class )
  public void getCredentialsThrowsOnNullServerUri() {
    strategy.getCredentials( null );
  }

  @Test( expected = IllegalArgumentException.class )
  public void isAuthenticatedThrowsOnNullServerUri() {
    strategy.isAuthenticated( null );
  }

  @Test( expected = IllegalArgumentException.class )
  public void clearCredentialsThrowsOnNullServerUri() {
    strategy.clearCredentials( null );
  }

  @Test( expected = IllegalArgumentException.class )
  public void getCredentialValueThrowsOnNullCredentialKey() {
    strategy.getCredentialValue( serverUri, null );
  }

  @Test( expected = IllegalArgumentException.class )
  public void getCredentialValueThrowsOnEmptyCredentialKey() {
    strategy.getCredentialValue( serverUri, "" );
  }

  @Test( expected = IllegalArgumentException.class )
  public void getCredentialValueThrowsOnWhitespaceCredentialKey() {
    strategy.getCredentialValue( serverUri, "   " );
  }

  @Test( expected = IllegalArgumentException.class )
  public void storeCredentialValueThrowsOnNullServerUri() {
    strategy.storeCredentialValue( null, "key", "value" );
  }

  @Test( expected = IllegalArgumentException.class )
  public void storeCredentialValueThrowsOnNullCredentialKey() {
    strategy.storeCredentialValue( serverUri, null, "value" );
  }

  @Test( expected = IllegalArgumentException.class )
  public void storeCredentialValueThrowsOnEmptyCredentialKey() {
    strategy.storeCredentialValue( serverUri, "", "value" );
  }

  @Test( expected = IllegalArgumentException.class )
  public void storeCredentialValueThrowsOnWhitespaceCredentialKey() {
    strategy.storeCredentialValue( serverUri, "   ", "value" );
  }

  // ===== Concurrent Operations =====

  @Test
  public void multipleConcurrentStoreOperationsAreAtomic() throws Exception {
    Thread t1 = new Thread( () -> strategy.storeCredentialValue( serverUri, "key1", "val1" ) );
    Thread t2 = new Thread( () -> strategy.storeCredentialValue( serverUri, "key2", "val2" ) );
    Thread t3 = new Thread( () -> strategy.storeCredentialValue( serverUri, "key3", "val3" ) );

    t1.start();
    t2.start();
    t3.start();
    t1.join();
    t2.join();
    t3.join();

    Map<String, Object> creds = strategy.getCredentials( serverUri );
    assertEquals( 3, creds.size() );
    assertEquals( "val1", creds.get( "key1" ) );
    assertEquals( "val2", creds.get( "key2" ) );
    assertEquals( "val3", creds.get( "key3" ) );
  }

  @Test
  public void concurrentClearAndStoreOperations() throws Exception {
    strategy.storeCredentialValue( serverUri, "key1", "val1" );

    Thread clearThread = new Thread( () -> strategy.clearCredentialValue( serverUri, "key1" ) );
    Thread storeThread = new Thread( () -> strategy.storeCredentialValue( serverUri, "key2", "val2" ) );

    clearThread.start();
    storeThread.start();
    clearThread.join();
    storeThread.join();

    assertNull( strategy.getCredentialValue( serverUri, "key1" ) );
    assertEquals( "val2", strategy.getCredentialValue( serverUri, "key2" ) );
  }

  @Test
  public void storeCredentialValueWithEmptyString() {
    strategy.storeCredentialValue( serverUri, "emptykey", "" );

    assertEquals( "", strategy.getCredentialValue( serverUri, "emptykey" ) );
  }

  @Test
  public void getCredentialsForMultipleServersAreIndependent() throws Exception {
    URI server1 = new URI( "http://server1:8080/pentaho" );
    URI server2 = new URI( "http://server2:8080/pentaho" );

    strategy.storeCredentialValue( server1, "key", "server1_value" );
    strategy.storeCredentialValue( server2, "key", "server2_value" );

    assertEquals( "server1_value", strategy.getCredentialValue( server1, "key" ) );
    assertEquals( "server2_value", strategy.getCredentialValue( server2, "key" ) );
  }

  @Test
  public void isAuthenticatedReturnsFalseForEmptyJsessionId() {
    strategy.storeCredentialValue( serverUri, "jsessionid", "   " );

    assertFalse( strategy.isAuthenticated( serverUri ) );
  }

  @Test
  public void storeCredentialsWithObjectValues() {
    Map<String, Object> creds = new HashMap<>();
    creds.put( "string", "value" );
    creds.put( "number", 123 );
    creds.put( "boolean", true );

    strategy.storeCredentials( serverUri, creds );
    Map<String, Object> retrieved = strategy.getCredentials( serverUri );

    assertEquals( "value", retrieved.get( "string" ) );
    assertEquals( 123, retrieved.get( "number" ) );
    assertEquals( true, retrieved.get( "boolean" ) );
  }

  @Test
  public void getCredentialValueConvertsObjectToString() {
    Map<String, Object> creds = new HashMap<>();
    creds.put( "key", 456 );
    strategy.storeCredentials( serverUri, creds );

    assertEquals( "456", strategy.getCredentialValue( serverUri, "key" ) );
  }

  @Test
  public void getCredentialValueReturnsNullWhenKeyExistsButValueIsNull() {
    // The internal ConcurrentHashMap does not allow null values, so we use a spy
    // to return a HashMap with a null-valued key to exercise the defensive null check
    SessionBasedAuthStrategy spyStrategy = spy( new SessionBasedAuthStrategy() );
    Map<String, Object> credsWithNull = new HashMap<>();
    credsWithNull.put( "nullkey", null );
    doReturn( credsWithNull ).when( spyStrategy ).getCredentials( serverUri );

    assertNull( spyStrategy.getCredentialValue( serverUri, "nullkey" ) );
  }

  @Test
  public void getCredentialValueReturnsNullWhenCredentialsMapIsNull() {
    // getCredentials() normally never returns null, but the code defensively checks for it.
    // Use a spy to force getCredentials() to return null to cover the false branch of
    // "credentials != null" at line 109.
    SessionBasedAuthStrategy spyStrategy = spy( new SessionBasedAuthStrategy() );
    doReturn( null ).when( spyStrategy ).getCredentials( serverUri );

    assertNull( spyStrategy.getCredentialValue( serverUri, "anykey" ) );
  }

  @Test
  public void clearJsessionIdWhilePreservingMarker() {
    strategy.storeJSessionId( serverUri, "SESSION" );
    strategy.storeCredentialValue( serverUri, "extra", "data" );

    strategy.clearCredentialValue( serverUri, "jsessionid" );

    assertFalse( strategy.isAuthenticated( serverUri ) );
    assertTrue( strategy.hasBrowserAuthMarker( serverUri ) );
    assertEquals( "data", strategy.getCredentialValue( serverUri, "extra" ) );
  }

  @Test
  public void getServerKeyDefaultsToHttpWhenSchemeIsNull() throws URISyntaxException {
    URI noSchemeUri = new URI( null, null, "myhost", 9090, "/pentaho", null, null );

    strategy.storeCredentialValue( noSchemeUri, "jsessionid", "ABC" );

    assertEquals( "ABC", strategy.getCredentialValue( noSchemeUri, "jsessionid" ) );
  }
}

