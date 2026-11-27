/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.ws.rs.core.MediaType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

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
   * Verifies that a PUT request with a null body value is normalized to an empty-string entity before the
   * actual HTTP call is invoked.
   * @throws KettleException
   */
  @Test
  public void testPutWithEmptyBody() throws KettleException {

    Invocation.Builder builder = Mockito.mock( Invocation.Builder.class );
    WebTarget resource = Mockito.mock( WebTarget.class );

    // Original bug: this path was producing a null entity for an empty body, which Jersey surfaces as an
    // IllegalStateException when the PUT request is built. The fix is to normalize the value to the empty string
    // before calling builder.method(..., Entity.entity(...)), so the request carries an explicit empty payload instead
    // of a null one.
    Mockito.lenient().when( resource.request() ).thenReturn( builder );
    Mockito.lenient().when( builder.put( Mockito.<Entity<?>>argThat( entity ->
      entity.getEntity() instanceof String && "".equals( entity.getEntity() ) ) ) )
      .thenReturn( Response.ok( "true" ).build() );

    Client client = Mockito.mock( Client.class );
    Mockito.lenient().when( client.target( Mockito.anyString() ) ).thenReturn( resource );

    RestMeta meta = Mockito.mock( RestMeta.class );
    Mockito.lenient().when( meta.isUrlInField() ).thenReturn( false );
    Mockito.lenient().when( meta.isDynamicMethod() ).thenReturn( false );

    RowMetaInterface rmi = Mockito.mock( RowMetaInterface.class );
    Mockito.lenient().when( rmi.size() ).thenReturn( 1 );
    Mockito.lenient().when( rmi.getString( Mockito.any(), Mockito.eq( 0 ) ) ).thenReturn( null );

    RestData data = Mockito.mock( RestData.class );
    data.method = RestMeta.HTTP_METHOD_PUT;
    data.config = new ClientConfig();
    data.inputRowMeta = rmi;
    data.resultFieldName = "result";
    data.resultCodeFieldName = "status";
    data.realUrl = "http://localhost:8080/pentaho";
    data.mediaType = MediaType.TEXT_PLAIN_TYPE;
    data.useBody = true;
    data.indexOfBodyField = 0;

    Rest rest = Mockito.mock( Rest.class );
    Mockito.lenient().doCallRealMethod().when( rest ).callRest( Mockito.any() );
    Mockito.lenient().doReturn( client ).when( rest ).getClient( Mockito.any() );
    Mockito.lenient().doCallRealMethod().when( rest ).buildRequest( Mockito.any(), Mockito.any() );
    Mockito.lenient().doCallRealMethod().when( rest ).searchForHeaders( Mockito.any() );

    ReflectionTestUtils.setField( rest, "meta", meta );
    ReflectionTestUtils.setField( rest, "data", data );

    Object[] output = rest.callRest( new Object[] { 0 } );
    Assert.assertNotNull( output );
    Assert.assertEquals( 200L, output[ 2 ] );

    Mockito.verify( builder ).put( Mockito.<Entity<?>>argThat( entity ->
      entity.getEntity() instanceof String && "".equals( entity.getEntity() ) ) );
  }

}
