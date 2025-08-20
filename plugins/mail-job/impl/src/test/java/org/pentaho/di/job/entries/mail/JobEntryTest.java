/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.job.entries.mail;

import junit.framework.Assert;

import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicStatusLine;
import org.pentaho.di.core.Result;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import static org.mockito.ArgumentMatchers.any;
import org.pentaho.di.core.util.HttpClientManager;
import java.io.IOException;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertTrue;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class JobEntryTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  private JobEntryMail jobEntryMail;
  @Mock
  private HttpClientManager httpClientManager;
  @Mock
  private CloseableHttpClient httpClient;
  @Mock
  private CloseableHttpResponse httpResponse;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks( this );
    jobEntryMail = new JobEntryMail();
    httpClientManager = Mockito.mock( HttpClientManager.class );
    httpClient = Mockito.mock( CloseableHttpClient.class );
    httpResponse = Mockito.mock( CloseableHttpResponse.class );
  }


  @BeforeClass
  public static void setupBeforeClass() throws KettleException {
    KettleClientEnvironment.init();
  }

  @Test
  public void testJobEntrymailPasswordFixed() {
    JobEntryMail jem = new JobEntryMail();
    Assert.assertEquals( jem.getPassword( "asdf" ), "asdf" );
  }

  @Test
  public void testJobEntrymailPasswordEcr() {
    JobEntryMail jem = new JobEntryMail();
    Assert.assertEquals( jem.getPassword( "Encrypted 2be98afc86aa7f2e4cb79ce10df81abdc" ), "asdf" );
  }

  @Test
  public void testJobEntrymailPasswordVar() {
    JobEntryMail jem = new JobEntryMail();
    jem.setVariable( "my_pass", "asdf" );
    Assert.assertEquals( jem.getPassword( "${my_pass}" ), "asdf" );
  }

  @Test
  public void testJobEntrymailPasswordEncrVar() {
    JobEntryMail jem = new JobEntryMail();
    jem.setVariable( "my_pass", "Encrypted 2be98afc86aa7f2e4cb79ce10df81abdc" );
    Assert.assertEquals( jem.getPassword( "${my_pass}" ), "asdf" );
  }

  @Test
  public void testOauthAuthenticationFailsWithInvalidTokenUrl()  {
    JobEntryMail jobEntryMail = new JobEntryMail();
    jobEntryMail.setUsingAuthentication( JobEntryMail.AUTENTICATION_OAUTH );
    jobEntryMail.setTokenUrl( "http://invalidurl.com/token" );
    Result result = new Result();
    jobEntryMail.execute( result, 0 );
    assertTrue( result.getNrErrors() > 0 );
  }

  @Test
  public void testgrantTypeIsClientCredentials() {
    jobEntryMail.setGrant_type( JobEntryMail.GRANTTYPE_CLIENTCREDENTIALS );
    assertEquals( JobEntryMail.GRANTTYPE_CLIENTCREDENTIALS, jobEntryMail.getGrant_type() );
  }

  @Test
  public void testgrantTypeIsAuthorizationCode() {
    jobEntryMail.setGrant_type( JobEntryMail.GRANTTYPE_AUTHORIZATION_CODE );
    assertEquals( JobEntryMail.GRANTTYPE_AUTHORIZATION_CODE, jobEntryMail.getGrant_type() );
  }

  @Test( expected = NullPointerException.class )
  public void testgetOauthTokenThrowsExceptionOnUnsuccessfulResponse() throws IOException {
    String tokenUrl = "http://example.com/token";
    StatusLine statusLine = new BasicStatusLine( new ProtocolVersion( "HTTP", 1, 1 ), 400, "Bad Request" );
    when( httpClientManager.createDefaultClient() ).thenReturn( httpClient );
    when( httpClient.execute( any( HttpPost.class ) ) ).thenReturn( httpResponse );
    when( httpResponse.getStatusLine() ).thenReturn( statusLine );
    jobEntryMail.getOauthToken( tokenUrl );
  }

  @Test( expected = NullPointerException.class )
  public void testgetOauthTokenThrowsExceptionOnHttpClientExecuteFailure() throws IOException {
    String tokenUrl = "http://example.com/token";
    when( httpClientManager.createDefaultClient() ).thenReturn( httpClient );
    when( httpClient.execute( any( HttpPost.class ) ) ).thenThrow( new IOException() );
    jobEntryMail.getOauthToken( tokenUrl );
  }
  @Test( expected = RuntimeException.class )
  public void testGetOauthTokenThrowsExceptionOnHttpError() throws Exception {
    jobEntryMail.setGrant_type( JobEntryMail.GRANTTYPE_REFRESH_TOKEN );
    jobEntryMail.setRefresh_token( "refresh_token_value" );
    jobEntryMail.setTokenUrl( "http://example.com/token" );

    when( httpClient.execute( any( HttpPost.class ) ) ).thenReturn( httpResponse );
    when( httpResponse.getStatusLine().getStatusCode() ).thenReturn( 500 );

    jobEntryMail.getOauthToken( "http://example.com/token" );
  }
  @Test
  public void testAuthorizationCodeAndRedirectUri() {
    JobEntryMail jobEntry = new JobEntryMail();
    String authorizationCode = "testAuthCode";
    String redirectUri = "http://test.redirect.uri";

    jobEntry.setAuthorization_code( authorizationCode );
    jobEntry.setRedirectUri( redirectUri );

    assertEquals( authorizationCode, jobEntry.getAuthorization_code() );
    assertEquals( redirectUri, jobEntry.getRedirectUri() );
  }

  @Test( expected = RuntimeException.class )
  public void testGetOauthTokenThrowsExceptionOnIOException() throws Exception {
    jobEntryMail.setGrant_type( JobEntryMail.GRANTTYPE_REFRESH_TOKEN );
    jobEntryMail.setRefresh_token( "refresh_token_value" );
    jobEntryMail.setTokenUrl( "http://example.com/token" );

    when( httpClient.execute( any( HttpPost.class ) ) ).thenThrow( new IOException() );

    jobEntryMail.getOauthToken( "http://example.com/token" );
  }
  @Test( expected = RuntimeException.class )
  public void getOauthTokenWithAuthorizationCodeThrowsExceptionOnHttpError() throws Exception {
    jobEntryMail.setGrant_type(JobEntryMail.GRANTTYPE_AUTHORIZATION_CODE);
    jobEntryMail.setAuthorization_code( "auth_code_value" );
    jobEntryMail.setRedirectUri( "http://example.com/redirect" );
    jobEntryMail.setTokenUrl( "http://example.com/token" );

    when( httpClient.execute( any( HttpPost.class ) ) ).thenReturn( httpResponse );
    when( httpResponse.getStatusLine().getStatusCode() ).thenReturn( HttpStatus.SC_BAD_REQUEST );

    jobEntryMail.getOauthToken( "http://example.com/token" );
  }
}
