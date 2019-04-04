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

package org.pentaho.di.www;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.logging.LogChannelInterface;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

public class GetSlavesServletTest {

  private GetSlavesServlet servlet;
  private HttpServletRequest request;
  private HttpServletResponse response;

  @Before
  public void before() {
    servlet = mock( GetSlavesServlet.class );
    request = mock( HttpServletRequest.class );
    response = mock( HttpServletResponse.class );
  }

  @Test
  @SuppressWarnings( "ResultOfMethodCallIgnored" )
  public void testUpdateActivityStatusInDoGet() throws Exception {
    LogChannelInterface log = mock( LogChannelInterface.class );
    ServletOutputStream outputStream = mock( ServletOutputStream.class );
    SlaveServerDetection activeServerDetection = mock( SlaveServerDetection.class );
    SlaveServerDetection inactiveServerDetection = mock( SlaveServerDetection.class );
    SlaveServer activeSlaveServer = mock( SlaveServer.class );
    SlaveServer inactiveSlaveServer = mock( SlaveServer.class );

    servlet.log = log;

    List<SlaveServerDetection> detections = new ArrayList<>();
    detections.add( activeServerDetection );
    detections.add( inactiveServerDetection );

    doReturn( false ).when( log ).isDebug();
    doReturn( outputStream ).when( response ).getOutputStream();
    doReturn( detections ).when( servlet ).getDetections();
    doReturn( activeSlaveServer ).when( activeServerDetection ).getSlaveServer();
    doReturn( inactiveSlaveServer ).when( inactiveServerDetection ).getSlaveServer();
    doThrow( new Exception() ).when( inactiveSlaveServer ).getStatus();

    doCallRealMethod().when( servlet ).doGet( request, response );
    servlet.doGet( request, response );

    verify( activeSlaveServer ).getStatus();
    verify( activeServerDetection, never() ).setActive( false );
    verify( activeServerDetection, never() ).setLastInactiveDate( anyObject() );
    verify( activeServerDetection ).getXML();

    verify( inactiveSlaveServer ).getStatus();
    verify( inactiveServerDetection ).setActive( false );
    verify( inactiveServerDetection ).setLastInactiveDate( anyObject() );
    verify( inactiveServerDetection ).getXML();
  }
}
