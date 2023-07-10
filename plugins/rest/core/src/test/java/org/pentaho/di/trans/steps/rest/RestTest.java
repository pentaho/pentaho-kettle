/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.ws.rs.core.MultivaluedMap;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.util.reflection.Whitebox.setInternalState;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith( PowerMockRunner.class )
@PowerMockIgnore( "jdk.internal.reflect.*" )
@PrepareForTest( Client.class )
public class RestTest {

  @Test
  public void testCreateMultivalueMap() {
    StepMeta stepMeta = new StepMeta();
    stepMeta.setName( "TestRest" );
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "TestRest" );
    transMeta.addStep( stepMeta );
    Rest rest = new Rest( stepMeta, mock( StepDataInterface.class ),
      1, transMeta, mock( Trans.class ) );
    MultivaluedMapImpl map = rest.createMultivalueMap( "param1", "{a:{[val1]}}" );
    String val1 = map.getFirst( "param1" );
    assertTrue( val1.contains( "%7D" ) );
  }

  @Test
  public void testCallEndpointWithDeleteVerb() throws KettleException {
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.add( "Content-Type", "application/json" );

    ClientResponse response = mock( ClientResponse.class );
    doReturn( 200 ).when( response ).getStatus();
    doReturn( headers ).when( response ).getHeaders();
    doReturn( "true" ).when( response ).getEntity( String.class );

    WebResource.Builder builder = mock( WebResource.Builder.class );
    doReturn( response ).when( builder ).delete( ClientResponse.class );

    WebResource resource = mock( WebResource.class );
    doReturn( builder ).when( resource ).getRequestBuilder();

    Client client = mock( Client.class );
    doReturn( resource ).when( client ).resource( anyString() );

    mockStatic( Client.class );
    when( Client.create( any() ) ).thenReturn( client );

    RestMeta meta = mock( RestMeta.class );
    doReturn( false ).when( meta ).isDetailed();
    doReturn( false ).when( meta ).isUrlInField();
    doReturn( false ).when( meta ).isDynamicMethod();

    RowMetaInterface rmi = mock( RowMetaInterface.class );
    doReturn( 1 ).when( rmi ).size();

    RestData data = mock( RestData.class );
    DefaultApacheHttpClient4Config config = mock( DefaultApacheHttpClient4Config.class );
    doReturn( new HashSet<>() ).when( config ).getSingletons();
    data.method = RestMeta.HTTP_METHOD_DELETE;
    data.inputRowMeta = rmi;
    data.resultFieldName = "result";
    data.resultCodeFieldName = "status";
    data.resultHeaderFieldName = "headers";
    data.config = config;

    Rest rest = mock( Rest.class );
    doCallRealMethod().when( rest ).callRest( any() );
    doCallRealMethod().when( rest ).searchForHeaders( any() );

    setInternalState( rest, "meta", meta );
    setInternalState( rest, "data", data );

    Object[] output = rest.callRest( new Object[] { 0 } );

    verify( builder, times( 1 ) ).delete( ClientResponse.class );
    assertEquals( "true", output[ 1 ] );
    assertEquals( 200L, output[ 2 ] );
    assertEquals( "{\"Content-Type\":\"application\\/json\"}", output[ 3 ] );
  }
}
