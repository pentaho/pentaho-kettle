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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.net.URI;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleException;


public class SpoonSessionManagerTest {

  @BeforeClass
  public static void setUpClass() throws KettleException {
    if ( !KettleClientEnvironment.isInitialized() ) {
      KettleClientEnvironment.init();
    }
  }

  @Before
  public void setUp() {
    SpoonSessionManager.setInstance( null );
  }

  @After
  public void tearDown() {
    SpoonSessionManager.setInstance( null );
  }

  // ===== getInstance =====

  @Test
  public void getInstanceCreatesNewInstanceWhenNull() {
    SpoonSessionManager manager = SpoonSessionManager.getInstance();

    assertNotNull( manager );
  }

  @Test
  public void getInstanceReturnsSameInstanceOnSubsequentCalls() {
    SpoonSessionManager first = SpoonSessionManager.getInstance();
    SpoonSessionManager second = SpoonSessionManager.getInstance();

    assertSame( first, second );
  }

  // ===== getAuthenticationContext( URI ) =====

  @Test
  public void getAuthContextByUriCreatesNewContextForUnknownServer() throws Exception {
    SpoonSessionManager manager = SpoonSessionManager.getInstance();
    URI uri = new URI( "http://localhost:8080/pentaho" );

    AuthenticationContext ctx = manager.getAuthenticationContext( uri );

    assertNotNull( ctx );
    assertEquals( "SESSION", ctx.getAuthType() );
  }

  @Test
  public void getAuthContextByUriReturnsCachedContextForSameServer() throws Exception {
    SpoonSessionManager manager = SpoonSessionManager.getInstance();
    URI uri = new URI( "http://localhost:8080/pentaho" );

    AuthenticationContext first = manager.getAuthenticationContext( uri );
    AuthenticationContext second = manager.getAuthenticationContext( uri );

    assertSame( first, second );
  }

  @Test
  public void getAuthContextByUriCachesByHostAndPort() throws Exception {
    SpoonSessionManager manager = SpoonSessionManager.getInstance();
    URI uri1 = new URI( "http://host:8080/path1" );
    URI uri2 = new URI( "http://host:8080/path2" );

    AuthenticationContext ctx1 = manager.getAuthenticationContext( uri1 );
    AuthenticationContext ctx2 = manager.getAuthenticationContext( uri2 );

    assertSame( ctx1, ctx2 );
  }

  @Test
  public void getAuthContextByUriSeparatesByPort() throws Exception {
    SpoonSessionManager manager = SpoonSessionManager.getInstance();
    URI uri1 = new URI( "http://host:8080/pentaho" );
    URI uri2 = new URI( "http://host:9090/pentaho" );

    AuthenticationContext ctx1 = manager.getAuthenticationContext( uri1 );
    AuthenticationContext ctx2 = manager.getAuthenticationContext( uri2 );

    assertNotNull( ctx1 );
    assertNotNull( ctx2 );
    // different port → different key → different context objects
    assertFalse( ctx1 == ctx2 );
  }

  // ===== getKey: default port resolution =====

  @Test
  public void httpDefaultPortIs80() throws Exception {
    SpoonSessionManager manager = SpoonSessionManager.getInstance();
    URI noPort = new URI( "http://host/pentaho" );
    URI explicit80 = new URI( "http://host:80/pentaho" );

    assertSame( manager.getAuthenticationContext( noPort ),
                manager.getAuthenticationContext( explicit80 ) );
  }

  @Test
  public void httpsDefaultPortIs443() throws Exception {
    SpoonSessionManager manager = SpoonSessionManager.getInstance();
    URI noPort = new URI( "https://host/pentaho" );
    URI explicit443 = new URI( "https://host:443/pentaho" );

    assertSame( manager.getAuthenticationContext( noPort ),
                manager.getAuthenticationContext( explicit443 ) );
  }

  @Test
  public void httpAndHttpsDefaultPortsDiffer() throws Exception {
    SpoonSessionManager manager = SpoonSessionManager.getInstance();
    URI http = new URI( "http://host/pentaho" );
    URI https = new URI( "https://host/pentaho" );

    AuthenticationContext httpCtx = manager.getAuthenticationContext( http );
    AuthenticationContext httpsCtx = manager.getAuthenticationContext( https );

    assertFalse( httpCtx == httpsCtx );
  }

  @Test
  public void httpAndHttpsSameExplicitPortDiffer() throws Exception {
    SpoonSessionManager manager = SpoonSessionManager.getInstance();
    URI http = new URI( "http://host:443/pentaho" );
    URI https = new URI( "https://host:443/pentaho" );

    AuthenticationContext httpCtx = manager.getAuthenticationContext( http );
    AuthenticationContext httpsCtx = manager.getAuthenticationContext( https );

    assertFalse( httpCtx == httpsCtx );
  }

  // ===== getAuthenticationContext( String ) =====

  @Test
  public void getAuthContextByStringDelegatesToUriOverload() {
    SpoonSessionManager manager = SpoonSessionManager.getInstance();

    AuthenticationContext ctx = manager.getAuthenticationContext( "http://localhost:8080/pentaho" );

    assertNotNull( ctx );
    assertEquals( "SESSION", ctx.getAuthType() );
  }

  @Test
  public void getAuthContextByStringReturnsSameContextAsUriOverload() throws Exception {
    SpoonSessionManager manager = SpoonSessionManager.getInstance();
    URI uri = new URI( "http://localhost:8080/pentaho" );

    AuthenticationContext fromUri = manager.getAuthenticationContext( uri );
    AuthenticationContext fromString = manager.getAuthenticationContext( "http://localhost:8080/pentaho" );

    assertSame( fromUri, fromString );
  }

  @Test
  public void getAuthContextByStringReturnsNullForInvalidUrl() {
    SpoonSessionManager manager = SpoonSessionManager.getInstance();

    AuthenticationContext ctx = manager.getAuthenticationContext( "://not a valid url{}" );

    assertNull( ctx );
  }

  @Test
  public void getAuthContextByStringReturnsNullForNullUrl() {
    SpoonSessionManager manager = SpoonSessionManager.getInstance();

    AuthenticationContext ctx = manager.getAuthenticationContext( (String) null );

    assertNull( ctx );
  }

  // ===== getAuthenticationContext( URI ) – validation guards =====

  @Test( expected = IllegalArgumentException.class )
  public void getAuthContextByUriThrowsOnNullUri() {
    SpoonSessionManager manager = SpoonSessionManager.getInstance();

    manager.getAuthenticationContext( (URI) null );
  }

  @Test
  public void getAuthContextByUriThrowsOnNullUriWithExpectedMessage() {
    SpoonSessionManager manager = SpoonSessionManager.getInstance();

    try {
      manager.getAuthenticationContext( (URI) null );
      org.junit.Assert.fail( "Expected IllegalArgumentException" );
    } catch ( IllegalArgumentException e ) {
      assertEquals( "serverUri must not be null", e.getMessage() );
    }
  }

  @Test
  public void getAuthContextByUriThrowsForUrisWithNoHost() throws Exception {
    SpoonSessionManager manager = SpoonSessionManager.getInstance();
    String[] hostlessUris = {
      "urn:isbn:0451450523",
      "file:///tmp/data",
      "/just/a/path"
    };

    for ( String uriStr : hostlessUris ) {
      URI uri = new URI( uriStr );
      try {
        manager.getAuthenticationContext( uri );
        org.junit.Assert.fail( "Expected IllegalArgumentException for URI: " + uriStr );
      } catch ( IllegalArgumentException e ) {
        assertEquals( "serverUri must have a valid host: " + uri, e.getMessage() );
      }
    }
  }

  @Test
  public void getAuthContextByUriSucceedsWhenHostIsPresent() throws Exception {
    SpoonSessionManager manager = SpoonSessionManager.getInstance();

    AuthenticationContext ctx = manager.getAuthenticationContext(
      new URI( "http://validhost:8080/pentaho" ) );

    assertNotNull( ctx );
  }

  @Test
  public void getAuthContextByStringReturnsNullWhenUriHasNoHost() {
    SpoonSessionManager manager = SpoonSessionManager.getInstance();

    AuthenticationContext ctx = manager.getAuthenticationContext( "urn:isbn:0451450523" );

    assertNull( ctx );
  }

  @Test
  public void getKeyDefaultsToHttpWhenSchemeIsNull() throws Exception {
    SpoonSessionManager manager = SpoonSessionManager.getInstance();
    URI noSchemeUri = new URI( null, null, "myhost", 8080, "/pentaho", null, null );

    AuthenticationContext ctx = manager.getAuthenticationContext( noSchemeUri );

    assertNotNull( ctx );
  }

  // ===== helper =====

  private static void assertFalse( boolean condition ) {
    org.junit.Assert.assertFalse( condition );
  }
}

