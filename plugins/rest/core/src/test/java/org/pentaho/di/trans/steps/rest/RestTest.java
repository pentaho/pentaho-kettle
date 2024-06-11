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

import org.glassfish.jersey.client.ClientConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.springframework.test.util.ReflectionTestUtils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class RestTest {

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

    Object[] params = new Object[1];
    params[0] = "{a:{[val1]}}";

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
    doReturn( params[0] ).when( rmi ).getString( params, 0 );

    RestData data = mock( RestData.class );
    data.method = RestMeta.HTTP_METHOD_POST;
    data.config = new ClientConfig();
    data.inputRowMeta = rmi;
    data.resultFieldName = "result";
    data.resultCodeFieldName = "status";
    data.resultHeaderFieldName = "headers";
    data.realUrl = "http://localhost:8080/pentaho";
    data.useParams = true;
    data.nrParams = 1;
    data.mediaType = MediaType.APPLICATION_JSON_TYPE;

    // Add one index to this array
    data.indexOfParamFields = new int[] {0};
    data.paramNames = new String[] {"param1"};

    Rest rest = mock( Rest.class );
    doCallRealMethod().when( rest ).callRest( any() );

    ReflectionTestUtils.setField( rest, "meta", meta );
    ReflectionTestUtils.setField( rest, "data", data );

    try {
      Object[] output = rest.callRest( params );
    } catch ( Exception exception ) {
      // Ignore the ConnectExcepion which is expected as rest call to localhost:8080 will fail in unit test
      // IllegalStateException is throws when the parameters are not encoded
      if ( exception.getCause() instanceof IllegalStateException ) {
        Assert.fail();
      }
    }
  }

  /**
   * Verifies that a PUT request with an empty body does not trigger an IllegalStateException
   * @throws KettleException
   */
  @Test
  public void testPutWithEmptyBody() throws KettleException {

    Invocation.Builder builder = mock( Invocation.Builder.class );

    WebTarget resource = mock( WebTarget.class );
    lenient().doReturn( builder ).when( resource ).request();

    Client client = mock( Client.class );
    lenient().doReturn( resource ).when( client ).target( anyString() );

    ClientBuilder clientBuilder = mock( ClientBuilder.class );
    lenient().when( clientBuilder.build() ).thenReturn( client );

    RestMeta meta = mock( RestMeta.class );
    lenient().doReturn( false ).when( meta ).isUrlInField();
    lenient().doReturn( false ).when( meta ).isDynamicMethod();

    RowMetaInterface rmi = mock( RowMetaInterface.class );

    RestData data = mock( RestData.class );
    data.method = RestMeta.HTTP_METHOD_PUT;
    data.config = new ClientConfig();
    data.inputRowMeta = rmi;
    data.realUrl = "http://localhost:8080/pentaho";
    data.mediaType = MediaType.TEXT_PLAIN_TYPE;
    data.useBody = true;
    // do not set data.indexOfBodyField

    Rest rest = mock( Rest.class );
    doCallRealMethod().when( rest ).callRest( any() );

    ReflectionTestUtils.setField( rest, "meta", meta );
    ReflectionTestUtils.setField( rest, "data", data );

    try {
      rest.callRest( new Object[] { 0 } );
    } catch ( Exception exception ) {
      // Ignore the ConnectException which is expected as rest call to localhost:8080 will fail in unit test
      // IllegalStateException is throws when the body is null
      if ( exception.getCause().getCause() instanceof IllegalStateException ) {
          Assert.fail( "PUT request with an empty body should not have failed with an IllegalStateException" );
      }
    }
  }

}
