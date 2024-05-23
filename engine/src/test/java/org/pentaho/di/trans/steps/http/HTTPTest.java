/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.http;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.util.HttpClientManager;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.test.util.InternalState.setInternalState;

/**
 * @author Luis Martins
 * @since 14-Aug-2018
 */
public class HTTPTest {

  private final LogChannelInterface log = mock( LogChannelInterface.class );
  private final RowMetaInterface rmi = mock( RowMetaInterface.class );
  private final HTTPData data = mock( HTTPData.class );
  private final HTTPMeta meta = mock( HTTPMeta.class );
  private final HTTP http = mock( HTTP.class );

  private final HttpClientManager manager = mock( HttpClientManager.class );
  private final CloseableHttpClient client = mock( CloseableHttpClient.class );

  private final String DATA = "This is the description, there's some HTML here, like &lt;strong&gt;this&lt;/strong&gt;. "
    + "Sometimes this text is another language that might contain these characters:\n"
    + "&lt;p&gt;é, è, ô, ç, à, ê, â.&lt;/p&gt; They can, of course, come in uppercase as well: &lt;p&gt;É, È Ô, Ç, À,"
    + " Ê, Â&lt;/p&gt;. UTF-8 handles this well.";

  @Before
  public void setup() throws Exception {
    HttpClientManager.HttpClientBuilderFacade builder = mock( HttpClientManager.HttpClientBuilderFacade.class );

    doReturn( builder ).when( manager ).createBuilder();

    doReturn( client ).when( builder ).build();

    CloseableHttpResponse response = mock( CloseableHttpResponse.class );
    doReturn( response ).when( client ).execute( any( HttpGet.class ), any( HttpClientContext.class ) );

    BasicHttpEntity entity = new BasicHttpEntity();
    entity.setContent( new ByteArrayInputStream( DATA.getBytes() ) );
    doReturn( entity ).when( response ).getEntity();

    setInternalState( data, "realUrl", "http://pentaho.com" );
    setInternalState( data, "argnrs", new int[0] );

    doReturn( false ).when( meta ).isUrlInField();
    doReturn( "body" ).when( meta ).getFieldName();

    doReturn( false ).when( log ).isDetailed();

    doCallRealMethod().when( http ).callHttpService( any( RowMetaInterface.class ), any( Object[].class ) );
    doReturn( HttpURLConnection.HTTP_OK ).when( http ).requestStatusCode( any( CloseableHttpResponse.class ) );
    doReturn( new Header[0] ).when( http ).searchForHeaders( any( CloseableHttpResponse.class ) );
    setInternalState( http, "log", log );
    setInternalState( http, "data", data );
    setInternalState( http, "meta", meta );
  }

  @Test
  public void callHttpServiceWithUTF8Encoding() throws Exception {
    try ( MockedStatic<HttpClientManager> httpClientManagerMockedStatic = mockStatic( HttpClientManager.class ) ) {
      httpClientManagerMockedStatic.when( HttpClientManager::getInstance ).thenReturn( manager );
      doReturn( "UTF-8" ).when( meta ).getEncoding();
      assertEquals( DATA, http.callHttpService( rmi, new Object[] { 0 } )[ 0 ] );
    }
  }

  @Test
  public void callHttpServiceWithoutEncoding() throws Exception {
    try ( MockedStatic<HttpClientManager> httpClientManagerMockedStatic = mockStatic( HttpClientManager.class ) ) {
      httpClientManagerMockedStatic.when( HttpClientManager::getInstance ).thenReturn( manager );
      doReturn( null ).when( meta ).getEncoding();
      assertNotEquals( DATA, http.callHttpService( rmi, new Object[] { 0 } )[ 0 ] );
    }
  }

  @Test
  public void testCallHttpServiceWasCalledWithContext() throws Exception {
    try ( MockedStatic<HttpClientManager> httpClientManagerMockedStatic = mockStatic( HttpClientManager.class ) ) {
      httpClientManagerMockedStatic.when( HttpClientManager::getInstance ).thenReturn( manager );
      doReturn( null ).when( meta ).getEncoding();
      http.callHttpService( rmi, new Object[] { 0 } );
      verify( client, times( 1 ) ).execute( any( HttpGet.class ), any( HttpClientContext.class ) );
    }
  }
}
