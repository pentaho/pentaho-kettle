/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.cluster;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;

/**
 * Tests for SlaveServer class
 *
 * @author Pavel Sakun
 * @see SlaveServer
 */
public class SlaveServerTest {
  SlaveServer slaveServer;

  @Before
  public void init() throws IOException {
    HttpClient httpClient = mock( HttpClient.class );
    doReturn( 404 ).when( httpClient ).executeMethod( any( HttpMethod.class ) );

    slaveServer = spy( new SlaveServer() );
    doReturn( httpClient ).when( slaveServer ).getHttpClient();
    doReturn( "response_body" ).when( slaveServer ).getResponseBodyAsString( any( InputStream.class ) );
  }

  @Test ( expected = KettleException.class )
  public void testExecService() throws Exception {
    doReturn( mock( GetMethod.class ) ).when( slaveServer ).buildExecuteServiceMethod( anyString(),
        anyMapOf( String.class, String.class ) );
    slaveServer.execService( "wrong_app_name" );
    fail( "Incorrect connection details had been used, but no exception was thrown" );
  }

  @Test ( expected = KettleException.class )
  public void testSendXML() throws Exception {
    doReturn( mock( PostMethod.class ) ).when( slaveServer ).buildSendXMLMethod( any( byte[].class ), anyString() );
    slaveServer.sendXML( "", "" );
    fail( "Incorrect connection details had been used, but no exception was thrown" );
  }

  @Test ( expected = KettleException.class )
  public void testSendExport() throws Exception {
    doReturn( mock( PostMethod.class ) ).when( slaveServer ).buildSendExportMethod( anyString(), anyString(),
        any( InputStream.class ) );
    File tempFile;
    tempFile = File.createTempFile( "PDI-", "tmp" );
    tempFile.deleteOnExit();
    slaveServer.sendExport( tempFile.getAbsolutePath(), "", "" );
    fail( "Incorrect connection details had been used, but no exception was thrown" );
  }
}
