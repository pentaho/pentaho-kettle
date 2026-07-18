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
package org.pentaho.di.core.database;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.util.HttpClientManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicStatusLine;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class CmsTokenProviderTest {

  private static final String TOKEN_URL = "https://keycloak.example.com/realms/pdi/protocol/openid-connect/token";
  private static final String CLIENT_ID = "pdi-service";
  private static final String CLIENT_SECRET = "my-secret-key";
  private static final String VALID_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.test.signature";

  @Mock
  private CloseableHttpClient mockHttpClient;

  @Mock
  private HttpEntity mockHttpEntity;

  @Before
  public void setUp() throws Exception {
    clearCachedToken();
  }

  @After
  public void tearDown() throws Exception {
    clearCachedToken();
  }

  @Test
  public void testGetInstance() {
    CmsTokenProvider provider1 = CmsTokenProvider.getInstance();
    CmsTokenProvider provider2 = CmsTokenProvider.getInstance();
    assertEquals( provider1, provider2 );
  }

  @Test
  public void testGetTokenReturnsNullWhenNotConfigured() throws Exception {
    try ( MockedStatic<Const> mockConst = mockStatic( Const.class ) ) {
      mockConst.when( Const::getCmsTokenUrl ).thenReturn( null );
      mockConst.when( Const::getCmsClientId ).thenReturn( CLIENT_ID );
      mockConst.when( Const::getCmsClientSecret ).thenReturn( CLIENT_SECRET );

      String token = CmsTokenProvider.getInstance().getToken();
      assertNull( token );
    }
  }

  @Test
  public void testGetTokenReturnsNullWhenClientIdMissing() throws Exception {
    try ( MockedStatic<Const> mockConst = mockStatic( Const.class ) ) {
      mockConst.when( Const::getCmsTokenUrl ).thenReturn( TOKEN_URL );
      mockConst.when( Const::getCmsClientId ).thenReturn( null );
      mockConst.when( Const::getCmsClientSecret ).thenReturn( CLIENT_SECRET );

      String token = CmsTokenProvider.getInstance().getToken();
      assertNull( token );
    }
  }

  @Test
  public void testGetTokenReturnsNullWhenClientSecretMissing() throws Exception {
    try ( MockedStatic<Const> mockConst = mockStatic( Const.class ) ) {
      mockConst.when( Const::getCmsTokenUrl ).thenReturn( TOKEN_URL );
      mockConst.when( Const::getCmsClientId ).thenReturn( CLIENT_ID );
      mockConst.when( Const::getCmsClientSecret ).thenReturn( null );

      String token = CmsTokenProvider.getInstance().getToken();
      assertNull( token );
    }
  }

  @Test
  public void testGetTokenFetchesAndCachesNewToken() throws Exception {
    try ( MockedStatic<Const> mockConst = mockStatic( Const.class );
          MockedStatic<HttpClientManager> mockManager = mockStatic( HttpClientManager.class ) ) {

      setupConfigurationMocks( mockConst );
      setupHttpClientMocks( mockManager, HttpURLConnection.HTTP_OK );

      String token1 = CmsTokenProvider.getInstance().getToken();
      String token2 = CmsTokenProvider.getInstance().getToken();

      assertEquals( VALID_TOKEN, token1 );
      assertEquals( VALID_TOKEN, token2 );
      verify( mockHttpClient, times( 1 ) ).execute( any( HttpUriRequest.class ) );
    }
  }

  @Test
  public void testGetTokenHandlesCustomExpirationTime() throws Exception {
    try ( MockedStatic<Const> mockConst = mockStatic( Const.class );
          MockedStatic<HttpClientManager> mockManager = mockStatic( HttpClientManager.class ) ) {

      setupConfigurationMocks( mockConst );

      Map<String, Object> response = new HashMap<>();
      response.put( "access_token", VALID_TOKEN );
      response.put( "expires_in", 600 );

      setupHttpClientMocks( mockManager, HttpURLConnection.HTTP_OK, response );

      String token = CmsTokenProvider.getInstance().getToken();
      assertEquals( VALID_TOKEN, token );
      verify( mockHttpClient, times( 1 ) ).execute( any( HttpUriRequest.class ) );
    }
  }

  @Test
  public void testGetTokenThrowsExceptionOnHttpError() throws Exception {
    try ( MockedStatic<Const> mockConst = mockStatic( Const.class );
          MockedStatic<HttpClientManager> mockManager = mockStatic( HttpClientManager.class ) ) {

      setupConfigurationMocks( mockConst );
      setupHttpClientMocks( mockManager, HttpURLConnection.HTTP_UNAUTHORIZED );

      try {
        CmsTokenProvider.getInstance().getToken();
        fail( "Expected KettleDatabaseException" );
      } catch ( KettleDatabaseException e ) {
        assertTrue( e.getMessage().contains( "HTTP 401" ) );
      }
    }
  }

  @Test
  public void testGetTokenThrowsExceptionOnMissingAccessToken() throws Exception {
    try ( MockedStatic<Const> mockConst = mockStatic( Const.class );
          MockedStatic<HttpClientManager> mockManager = mockStatic( HttpClientManager.class ) ) {

      setupConfigurationMocks( mockConst );

      Map<String, Object> response = new HashMap<>();
      response.put( "token_type", "Bearer" );

      setupHttpClientMocks( mockManager, HttpURLConnection.HTTP_OK, response );

      try {
        CmsTokenProvider.getInstance().getToken();
        fail( "Expected KettleDatabaseException" );
      } catch ( KettleDatabaseException e ) {
        assertTrue( e.getMessage().contains( "access_token" ) );
      }
    }
  }

  @Test
  public void testGetTokenThrowsExceptionOnNetworkFailure() throws Exception {
    try ( MockedStatic<Const> mockConst = mockStatic( Const.class );
          MockedStatic<HttpClientManager> mockManager = mockStatic( HttpClientManager.class ) ) {

      setupConfigurationMocks( mockConst );

      HttpClientManager manager = mock( HttpClientManager.class );
      mockManager.when( HttpClientManager::getInstance ).thenReturn( manager );
      when( manager.createDefaultClient() ).thenThrow( new RuntimeException( "Network error" ) );

      try {
        CmsTokenProvider.getInstance().getToken();
        fail( "Expected KettleDatabaseException" );
      } catch ( KettleDatabaseException e ) {
        assertTrue( e.getMessage().contains( "failed to fetch token" ) );
      }
    }
  }

  @Test
  public void testGetTokenSendsCorrectHttpRequest() throws Exception {
    try ( MockedStatic<Const> mockConst = mockStatic( Const.class );
          MockedStatic<HttpClientManager> mockManager = mockStatic( HttpClientManager.class ) ) {

      setupConfigurationMocks( mockConst );
      setupHttpClientMocks( mockManager, HttpURLConnection.HTTP_OK );

      CmsTokenProvider.getInstance().getToken();

      ArgumentCaptor<HttpUriRequest> requestCaptor = ArgumentCaptor.forClass( HttpUriRequest.class );
      verify( mockHttpClient ).execute( requestCaptor.capture() );

      HttpUriRequest request = requestCaptor.getValue();
      assertTrue( request instanceof HttpPost );
      assertEquals( TOKEN_URL, request.getURI().toString() );
    }
  }

  @Test
  public void testGetTokenDefaultsTo5MinutesWhenExpirationMissing() throws Exception {
    try ( MockedStatic<Const> mockConst = mockStatic( Const.class );
          MockedStatic<HttpClientManager> mockManager = mockStatic( HttpClientManager.class ) ) {

      setupConfigurationMocks( mockConst );

      Map<String, Object> response = new HashMap<>();
      response.put( "access_token", VALID_TOKEN );

      setupHttpClientMocks( mockManager, HttpURLConnection.HTTP_OK, response );

      String token = CmsTokenProvider.getInstance().getToken();
      assertNotNull( token );
      assertEquals( VALID_TOKEN, token );
    }
  }

  private void setupConfigurationMocks( MockedStatic<Const> mockConst ) {
    mockConst.when( Const::getCmsTokenUrl ).thenReturn( TOKEN_URL );
    mockConst.when( Const::getCmsClientId ).thenReturn( CLIENT_ID );
    mockConst.when( Const::getCmsClientSecret ).thenReturn( CLIENT_SECRET );
  }

  private void setupHttpClientMocks( MockedStatic<HttpClientManager> mockManager,
                                     int httpStatus ) throws Exception {
    Map<String, Object> response = new HashMap<>();
    response.put( "access_token", VALID_TOKEN );
    response.put( "expires_in", 300 );
    setupHttpClientMocks( mockManager, httpStatus, response );
  }

  private void setupHttpClientMocks( MockedStatic<HttpClientManager> mockManager,
                                     int httpStatus,
                                     Map<String, Object> responseBody ) throws Exception {
    HttpClientManager manager = mock( HttpClientManager.class );
    mockManager.when( HttpClientManager::getInstance ).thenReturn( manager );
    when( manager.createDefaultClient() ).thenReturn( mockHttpClient );
    CloseableHttpResponse mockResponse = mock( CloseableHttpResponse.class );
    StatusLine statusLine = new BasicStatusLine(
      new org.apache.http.HttpVersion( 1, 1 ),
      httpStatus,
      httpStatus == HttpURLConnection.HTTP_OK ? "OK" : "Error" );
    when( mockResponse.getStatusLine() ).thenReturn( statusLine );

    String json = new ObjectMapper().writeValueAsString( responseBody );
    InputStream is = new ByteArrayInputStream( json.getBytes( StandardCharsets.UTF_8 ) );
    when( mockHttpEntity.getContent() ).thenReturn( is );
    when( mockResponse.getEntity() ).thenReturn( mockHttpEntity );

    when( mockHttpClient.execute( any( HttpUriRequest.class ) ) ).thenReturn( mockResponse );
  }

  private void clearCachedToken() throws Exception {
    Field cachedField = CmsTokenProvider.class.getDeclaredField( "cached" );
    cachedField.setAccessible( true );
    java.util.concurrent.atomic.AtomicReference<?> cached =
      (java.util.concurrent.atomic.AtomicReference<?>) cachedField.get( CmsTokenProvider.getInstance() );
    cached.set( null );
  }
}
