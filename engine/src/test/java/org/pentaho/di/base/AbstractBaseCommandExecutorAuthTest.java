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

package org.pentaho.di.base;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.pan.Pan;
import org.pentaho.di.cli.auth.BrokerAuthClient;
import org.pentaho.di.cli.auth.BrokerAuthClient.BrokerFlowResult;
import org.pentaho.di.cli.auth.BrokerDiscoveryClient;
import org.pentaho.di.cli.auth.BrowserAuthSessionHolder;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AbstractBaseCommandExecutorAuthTest {

  @ClassRule
  public static final RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private static final String SERVER_URL = "http://localhost:8080/pentaho";
  private static final String USERNAME = "alice";
  private static final String ACCESS_TOKEN = "access-token";
  private static final String IDP_REGISTRATION_ID = "azure";
  private static final String KEYCLOAK_REGISTRATION_ID = "keycloak";
  private static final String RESOLVE_SERVICE_ACCOUNT_AUTHENTICATION = "resolveServiceAccountAuthentication";
  private static final String STATUS_COMPLETED = "COMPLETED";
  private static final String ROTATED_REFRESH_HANDLE = "rotated-refresh-handle";
  private static final String REFRESH_HANDLE = "refresh-handle";
  private static final String AUTH_HANDLE = "auth-handle";

  private TestCommandExecutor executor;
  private BrowserAuthSessionHolder holder;
  private BrokerAuthClient brokerClient;
  private BrokerDiscoveryClient discoveryClient;

  @Before
  public void setUp() {
    holder = mock( BrowserAuthSessionHolder.class );
    brokerClient = mock( BrokerAuthClient.class );
    discoveryClient = mock( BrokerDiscoveryClient.class );
    executor = new TestCommandExecutor( discoveryClient );
  }

  @Test
  public void resolveServiceAccountAuthenticationPrefersExistingOAuthToken() throws Exception {
    when( holder.hasValidOAuthToken( SERVER_URL ) ).thenReturn( true );
    when( holder.getSessionUsername( SERVER_URL ) ).thenReturn( USERNAME );

    Object authResult = invokePrivate(
      RESOLVE_SERVICE_ACCOUNT_AUTHENTICATION,
      new Class<?>[] { BrowserAuthSessionHolder.class, BrokerAuthClient.class,
        String.class, boolean.class, String.class },
      holder, brokerClient, SERVER_URL, true, IDP_REGISTRATION_ID );

    assertTrue( isEstablished( authResult ) );
    assertEquals( USERNAME, extractUsername( authResult ) );
    verify( brokerClient, never() ).clientCredentials( SERVER_URL, IDP_REGISTRATION_ID );
  }

  @Test
  public void resolveServiceAccountAuthenticationUsesClientCredentialsWhenNoTokenExists() throws Exception {
    when( holder.hasValidOAuthToken( SERVER_URL ) ).thenReturn( false );
    when( brokerClient.clientCredentials( SERVER_URL, IDP_REGISTRATION_ID ) ).thenReturn(
      new BrokerFlowResult( null, STATUS_COMPLETED, "client_credentials", USERNAME, null,
        null, null, null, null, null, null, null, ACCESS_TOKEN, 0,
        null, 1893456000L, null ) );

    Object authResult = invokePrivate(
      RESOLVE_SERVICE_ACCOUNT_AUTHENTICATION,
      new Class<?>[] { BrowserAuthSessionHolder.class, BrokerAuthClient.class,
        String.class, boolean.class, String.class },
      holder, brokerClient, SERVER_URL, true, IDP_REGISTRATION_ID );

    assertTrue( isEstablished( authResult ) );
    assertEquals( USERNAME, extractUsername( authResult ) );
    verify( brokerClient ).clientCredentials( SERVER_URL, IDP_REGISTRATION_ID );
    verify( holder ).clearSession( SERVER_URL );
    verify( holder ).storeOAuthToken(
      org.mockito.ArgumentMatchers.any( BrowserAuthSessionHolder.OAuthTokenData.class ) );
    verify( holder ).setRefreshHandle( SERVER_URL, null );
    verify( holder ).setBrokerAuthHandle( SERVER_URL, null );
  }

  @Test
  public void resolveExistingAuthenticationRefreshesExpiredTokenBeforeFallingBack() throws Exception {
    when( holder.hasValidOAuthToken( SERVER_URL ) ).thenReturn( false );
    when( holder.isOAuthTokenExpiredButRefreshable( SERVER_URL ) ).thenReturn( true );
    when( holder.getRefreshHandle( SERVER_URL ) ).thenReturn( REFRESH_HANDLE );
    when( holder.getBrokerAuthHandle( SERVER_URL ) ).thenReturn( AUTH_HANDLE );
    when( holder.getOAuthIdpRegistrationId( SERVER_URL ) ).thenReturn( IDP_REGISTRATION_ID );
    when( holder.getSessionUsername( SERVER_URL ) ).thenReturn( USERNAME );
    when( brokerClient.refreshAccessToken( SERVER_URL, REFRESH_HANDLE, AUTH_HANDLE ) ).thenReturn(
      new BrokerFlowResult( null, STATUS_COMPLETED, "refresh_token", null, null,
        null, null, null, null, null, null, null, ACCESS_TOKEN, 0,
        null, 1893456001L, ROTATED_REFRESH_HANDLE ) );

    Object authResult = invokePrivate(
      "resolveExistingAuthentication",
      new Class<?>[] { BrowserAuthSessionHolder.class, BrokerAuthClient.class, String.class, boolean.class },
      holder, brokerClient, SERVER_URL, true );

    assertTrue( isEstablished( authResult ) );
    assertEquals( USERNAME, extractUsername( authResult ) );
    verify( holder ).storeOAuthToken(
      org.mockito.ArgumentMatchers.any( BrowserAuthSessionHolder.OAuthTokenData.class ) );
    verify( holder ).setRefreshHandle( SERVER_URL, ROTATED_REFRESH_HANDLE );
  }

  @Test
  public void resolveExistingAuthenticationFallsBackToBrowserSessionWhenRefreshFails() throws Exception {
    when( holder.hasValidOAuthToken( SERVER_URL ) ).thenReturn( false );
    when( holder.isOAuthTokenExpiredButRefreshable( SERVER_URL ) ).thenReturn( true );
    when( holder.getRefreshHandle( SERVER_URL ) ).thenReturn( REFRESH_HANDLE );
    when( holder.getBrokerAuthHandle( SERVER_URL ) ).thenReturn( AUTH_HANDLE );
    when( brokerClient.refreshAccessToken( SERVER_URL, REFRESH_HANDLE, AUTH_HANDLE ) ).thenReturn(
      new BrokerFlowResult( null, "FAILED", null, null, "invalid_grant",
        null, null, null, null, null, null, null, null, 0,
        null, 0L, null ) );
    when( holder.hasValidSession( SERVER_URL ) ).thenReturn( true );
    when( holder.getSessionUsername( SERVER_URL ) ).thenReturn( USERNAME );

    Object authResult = invokePrivate(
      "resolveExistingAuthentication",
      new Class<?>[] { BrowserAuthSessionHolder.class, BrokerAuthClient.class, String.class, boolean.class },
      holder, brokerClient, SERVER_URL, true );

    assertTrue( isEstablished( authResult ) );
    assertEquals( USERNAME, extractUsername( authResult ) );
    verify( holder ).clearOAuthToken( SERVER_URL );
  }

  @Test
  public void attemptSilentRefreshRecoveryPathUsesConfiguredBrokerClient() throws Exception {
    when( holder.isOAuthTokenExpiredButRefreshable( SERVER_URL ) ).thenReturn( true );
    when( holder.getRefreshHandle( SERVER_URL ) ).thenReturn( REFRESH_HANDLE );
    when( holder.getBrokerAuthHandle( SERVER_URL ) ).thenReturn( AUTH_HANDLE );
    when( holder.getOAuthIdpRegistrationId( SERVER_URL ) ).thenReturn( IDP_REGISTRATION_ID );
    when( holder.getSessionUsername( SERVER_URL ) ).thenReturn( USERNAME );
    when( brokerClient.refreshAccessToken( SERVER_URL, REFRESH_HANDLE, AUTH_HANDLE ) ).thenReturn(
      new BrokerFlowResult( null, STATUS_COMPLETED, "refresh_token", null, null,
        null, null, null, null, null, null, null, ACCESS_TOKEN, 0,
        null, 1893456002L, ROTATED_REFRESH_HANDLE ) );

    Object refreshed = invokePrivate(
      "attemptSilentRefresh",
      new Class<?>[] { BrowserAuthSessionHolder.class, BrokerAuthClient.class, String.class },
      holder, brokerClient, SERVER_URL );

    assertTrue( (Boolean) refreshed );
    verify( brokerClient ).refreshAccessToken( SERVER_URL, REFRESH_HANDLE, AUTH_HANDLE );
    verify( holder ).storeOAuthToken(
      org.mockito.ArgumentMatchers.any( BrowserAuthSessionHolder.OAuthTokenData.class ) );
    verify( holder ).setRefreshHandle( SERVER_URL, ROTATED_REFRESH_HANDLE );
  }

  @Test
  public void resolveInteractiveAuthenticationPrefersExplicitDeviceCode() throws Exception {
    when( brokerClient.startDeviceCodeFlow( SERVER_URL, IDP_REGISTRATION_ID ) ).thenReturn(
      new BrokerFlowResult( AUTH_HANDLE, "PENDING", BrokerAuthClient.GRANT_DEVICE_CODE, null, null,
        "ABCD-EFGH", "https://idp.example/device", null, null, null, null, null, null, 0,
        null, 0L, null ) );
    when( brokerClient.pollUntilComplete( SERVER_URL, AUTH_HANDLE ) ).thenReturn(
      new BrokerFlowResult( AUTH_HANDLE, STATUS_COMPLETED, BrokerAuthClient.GRANT_DEVICE_CODE, USERNAME, null,
        null, null, null, null, null, null, null, ACCESS_TOKEN, 0,
        null, 1893456000L, REFRESH_HANDLE ) );

    Object authResult = invokePrivate(
      "resolveInteractiveAuthentication",
      new Class<?>[] { BrowserAuthSessionHolder.class, BrokerAuthClient.class, String.class, boolean.class,
        boolean.class, boolean.class, String.class },
      holder, brokerClient, SERVER_URL, true, false, true, IDP_REGISTRATION_ID );

    assertTrue( isEstablished( authResult ) );
    assertEquals( USERNAME, extractUsername( authResult ) );
    verify( brokerClient, never() ).startPkceFlow( SERVER_URL, IDP_REGISTRATION_ID );
  }

  @Test
  public void resolveInteractiveAuthenticationReturnsNoneWhenNoInteractiveFlagsAreSet() throws Exception {
    Object authResult = invokePrivate(
      "resolveInteractiveAuthentication",
      new Class<?>[] { BrowserAuthSessionHolder.class, BrokerAuthClient.class, String.class, boolean.class,
        boolean.class, boolean.class, String.class },
      holder, brokerClient, SERVER_URL, true, false, false, IDP_REGISTRATION_ID );

    assertFalse( isEstablished( authResult ) );
    verify( brokerClient, never() ).startPkceFlow( SERVER_URL, IDP_REGISTRATION_ID );
  }

  @Test
  public void resolveBrokerRegistrationIdUsesSingleAdvertisedIdpWhenNoneConfigured() throws Exception {
    when( discoveryClient.getAvailableIdps( SERVER_URL ) ).thenReturn( java.util.List.of( IDP_REGISTRATION_ID ) );

    String registrationId = (String) invokePrivate(
      "resolveBrokerRegistrationId",
      new Class<?>[] { String.class, boolean.class, boolean.class, boolean.class, String.class },
      SERVER_URL, false, false, false, null );

    assertEquals( IDP_REGISTRATION_ID, registrationId );
  }

  @Test
  public void resolveBrokerRegistrationIdRejectsExplicitOAuthWhenMultipleIdpsAreAvailable() throws Exception {
    when( discoveryClient.getAvailableIdps( SERVER_URL ) ).thenReturn(
      List.of( IDP_REGISTRATION_ID, KEYCLOAK_REGISTRATION_ID ) );

    try {
      invokePrivate(
        "resolveBrokerRegistrationId",
        new Class<?>[] { String.class, boolean.class, boolean.class, boolean.class, String.class },
        SERVER_URL, true, false, false, null );
      fail( "Expected multi-IdP ambiguity to reject explicit OAuth" );
    } catch ( java.lang.reflect.InvocationTargetException invocationTargetException ) {
      Throwable cause = invocationTargetException.getCause();
      assertTrue( cause instanceof org.pentaho.di.core.exception.KettleException );
      assertTrue( cause.getMessage().contains( "Multiple OAuth IdPs are enabled" ) );
    }
  }

  private Object invokePrivate( String methodName, Class<?>[] parameterTypes, Object... args ) throws Exception {
    try {
      Method method = AbstractBaseCommandExecutor.class.getDeclaredMethod( methodName, parameterTypes );
      method.setAccessible( true );
      return method.invoke( executor, args );
    } catch ( NoSuchMethodException noSuchMethodException ) {
      if ( "resolveServiceAccountAuthentication".equals( methodName ) ) {
        Method legacyMethod = AbstractBaseCommandExecutor.class.getDeclaredMethod( methodName,
          BrowserAuthSessionHolder.class, BrokerAuthClient.class, BrokerDiscoveryClient.class,
          String.class, boolean.class, String.class );
        legacyMethod.setAccessible( true );
        return legacyMethod.invoke( executor,
          args[ 0 ], args[ 1 ], discoveryClient, args[ 2 ], args[ 3 ], args[ 4 ] );
      }
      throw noSuchMethodException;
    }
  }

  private boolean isEstablished( Object authResult ) throws Exception {
    Method method = authResult.getClass().getDeclaredMethod( "isEstablished" );
    method.setAccessible( true );
    return (Boolean) method.invoke( authResult );
  }

  private String extractUsername( Object authResult ) throws Exception {
    Method method = authResult.getClass().getDeclaredMethod( "username" );
    method.setAccessible( true );
    return (String) method.invoke( authResult );
  }

  private static final class TestCommandExecutor extends AbstractBaseCommandExecutor {
    private final BrokerDiscoveryClient discoveryClient;

    private TestCommandExecutor( BrokerDiscoveryClient discoveryClient ) {
      this.discoveryClient = discoveryClient;
      setPkgClazz( Pan.class );
      setLog( new LogChannel( "AbstractBaseCommandExecutorAuthTest" ) );
    }

    @Override
    protected BrokerDiscoveryClient createBrokerDiscoveryClient() {
      return discoveryClient;
    }
  }
}
