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

package org.pentaho.di.trans.steps.rest;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.ws.rs.core.MediaType;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class RestTest {
  @Ignore
  @Test
  public void testCallEndpointWithGetVerb() throws KettleException {
    Invocation.Builder builder = mock( Invocation.Builder.class );

    WebTarget resource = mock( WebTarget.class );
    lenient().doReturn( builder ).when( resource ).request();

    Client client = mock( Client.class );
    lenient().doReturn( resource ).when( client ).target( anyString() );

    ClientBuilder clientBuilder = mock( ClientBuilder.class );
    lenient().when( clientBuilder.build() ).thenReturn( client );

    RestMeta meta = mock( RestMeta.class );
    doReturn( false ).when( meta ).isUrlInField();
    doReturn( false ).when( meta ).isDynamicMethod();

    RowMetaInterface rmi = mock( RowMetaInterface.class );
    doReturn( 1 ).when( rmi ).size();

    RestData data = mock( RestData.class );
    data.method = RestMeta.HTTP_METHOD_GET;
    data.config = new ClientConfig();
    data.inputRowMeta = rmi;
    data.resultFieldName = "result";
    data.resultCodeFieldName = "status";
    data.resultHeaderFieldName = "headers";
    data.realUrl = "https://www.hitachivantara.com/en-us/home.html";

    Rest rest = mock( Rest.class );
    doCallRealMethod().when( rest ).callRest( any() );
    doCallRealMethod().when( rest ).getClient( any() );
    doCallRealMethod().when( rest ).buildRequest( any(), any() );
    doCallRealMethod().when( rest ).searchForHeaders( any() );

    ReflectionTestUtils.setField( rest, "meta", meta );
    ReflectionTestUtils.setField( rest, "data", data );

    Object[] output = rest.callRest( new Object[] { 0 } );
    //Should not get any exception but a non-null output
    assertNotNull( output );

    //GET request should succeed.
    assertEquals( 200L, output[ 2 ] );
  }

  /**
   * This test makes sure the parameters are uri encoded. If the parameters are not encoded, it will throw
   * IllegalStateException
   * @throws KettleException
   */
  @Test
  public void testEncodingParams() throws KettleException {

    Object[] params = new Object[2];
    params[0] = "{a:{[val1]}}";
    params[1] = "string with spaces";

    RestMeta meta = mock( RestMeta.class );
    doReturn( false ).when( meta ).isUrlInField();
    doReturn( false ).when( meta ).isDynamicMethod();

    RowMetaInterface rmi = mock( RowMetaInterface.class );
    doReturn( params[0] ).when( rmi ).getString( params, 0 );
    doReturn( params[1] ).when( rmi ).getString( params, 1 );

    RestData data = mock( RestData.class );
    data.method = RestMeta.HTTP_METHOD_POST;
    data.config = new ClientConfig();
    data.inputRowMeta = rmi;
    data.resultFieldName = "result";
    data.resultCodeFieldName = "status";
    data.resultHeaderFieldName = "headers";
    data.realUrl = "http://localhost:8080/pentaho";
    data.useParams = true;
    data.nrParams = 2;
    data.mediaType = MediaType.APPLICATION_JSON_TYPE;

    // Add one index to this array
    data.indexOfParamFields = new int[] {0, 1};
    data.paramNames = new String[] {"param1", "param2"};

    Rest rest = mock( Rest.class );
    doCallRealMethod().when( rest ).getClient( any() );
    doCallRealMethod().when( rest ).buildRequest( any(), any() );

    ReflectionTestUtils.setField( rest, "meta", meta );
    ReflectionTestUtils.setField( rest, "data", data );

    Client client = rest.getClient( params );
    WebTarget webResource = rest.buildRequest( client, params );
    String expected = "http://localhost:8080/pentaho?param1=%7Ba%3A%7B%5Bval1%5D%7D%7D&param2=string%20with%20spaces";
    assertEquals( expected, webResource.getUri().toString() );
  }

  /**
   * Verifies that a PUT request with an empty body does not trigger an IllegalStateException
   * @throws KettleException
   */
  @Test
  public void testPutWithEmptyBody() throws KettleException {

    Invocation.Builder builder = Mockito.mock( Invocation.Builder.class );
    WebTarget resource = Mockito.mock( WebTarget.class );

    Mockito.lenient().when( resource.request() ).thenReturn( builder );
    Mockito.lenient().when( builder.method( eq( "PUT" ), any( Entity.class ) ) ).thenReturn( Response.ok( "true" ).build() );

    Client client = Mockito.mock( Client.class );
    Mockito.lenient().when( client.target( Mockito.anyString() ) ).thenReturn( resource );

    RestMeta meta = Mockito.mock( RestMeta.class );
    Mockito.when( meta.isUrlInField() ).thenReturn( false );
    Mockito.when( meta.isDynamicMethod() ).thenReturn( false );

    RowMetaInterface rmi = Mockito.mock( RowMetaInterface.class );

    RestData data = Mockito.mock( RestData.class );
    data.method = RestMeta.HTTP_METHOD_PUT;
    data.config = new ClientConfig();
    data.inputRowMeta = rmi;
    // should be non-routable so we can consistetly not connect
    data.realUrl = "http://192.0.2.1:8080/pentaho";
    data.mediaType = MediaType.TEXT_PLAIN_TYPE;
    data.useBody = true;
    // do not set data.indexOfBodyField

    Rest rest = Mockito.mock( Rest.class );
    Mockito.doCallRealMethod().when( rest ).callRest( Mockito.any() );
    Mockito.doCallRealMethod().when( rest ).getClient( Mockito.any() );
    Mockito.doCallRealMethod().when( rest ).buildRequest( Mockito.any(), Mockito.any() );

    ReflectionTestUtils.setField( rest, "meta", meta );
    ReflectionTestUtils.setField( rest, "data", data );

    try {
      rest.callRest( new Object[] { 0 } );
      Assert.fail( "Expected an exception" );
    } catch ( KettleException exception ) {
      if ( exception.getCause() instanceof IllegalStateException ) {
        Assert.fail( "PUT request with an empty body should not have failed with an IllegalStateException" );
      }
    }
  }

}
