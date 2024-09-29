/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019-2024 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package org.pentaho.di.core;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicStatusLine;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

public class HTTPProtocolTest {

  public static final String HELLO_WORLD = "Hello world!";
  @ClassRule
  public static WireMockClassRule wireMockRule = new WireMockClassRule( 55554 );

  @Rule
  public WireMockClassRule instanceRule = wireMockRule;

  @Test
  public void getsAResponse() throws IOException, AuthenticationException {
    stubFor( get( urlEqualTo( "/some/thing" ) )
      .willReturn( aResponse()
        .withHeader( "Content-Type", "text/plain" )
        .withBody( HELLO_WORLD ) ) );
    HTTPProtocol httpProtocol = new HTTPProtocol();
    assertEquals( HELLO_WORLD, httpProtocol.get( "http://localhost:55554/some/thing", "", "" ) );
  }

  @Test
  public void httpClientGetsClosed() throws IOException, AuthenticationException {
    CloseableHttpClient httpClient = Mockito.mock( CloseableHttpClient.class );
    CloseableHttpResponse response = Mockito.mock( CloseableHttpResponse.class );
    HTTPProtocol httpProtocol = new HTTPProtocol() {
      @Override CloseableHttpClient openHttpClient( String username, String password ) {
        return httpClient;
      }
    };
    String urlAsString = "http://url/path";
    when( httpClient.execute( argThat( matchesGet() ) ) ).thenReturn( response );
    StatusLine statusLine = new BasicStatusLine( new ProtocolVersion( "http", 2, 0 ), HttpStatus.SC_OK, "blah" );
    BasicHttpEntity entity = new BasicHttpEntity();
    String content = "plenty of mocks for this test";
    entity.setContent( new ByteArrayInputStream( content.getBytes() ) );
    when( response.getEntity() ).thenReturn( entity );
    when( response.getStatusLine() ).thenReturn( statusLine );
    assertEquals( content, httpProtocol.get( urlAsString, "", "" ) );
    verify( httpClient ).close();
  }

  private Matcher<HttpUriRequest> matchesGet() {
    return new BaseMatcher<HttpUriRequest>() {
      @Override public void describeTo( Description description ) {
        description.appendText( "matching HttpGet" );
      }

      @Override public boolean matches( Object o ) {
        HttpGet httpGet = (HttpGet) o;
        return httpGet.getURI().toString().equals( "http://url/path" );
      }
    };
  }

}
