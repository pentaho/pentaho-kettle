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

import static org.mockito.ArgumentMatchers.any;
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
    verify( activeServerDetection, never() ).setLastInactiveDate( any() );
    verify( activeServerDetection ).getXML();

    verify( inactiveSlaveServer ).getStatus();
    verify( inactiveServerDetection ).setActive( false );
    verify( inactiveServerDetection ).setLastInactiveDate( any() );
    verify( inactiveServerDetection ).getXML();
  }
}
