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

package org.pentaho.di.trans.steps.webservices;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicHeader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class WebServiceTest {

  private static final String LOCATION_HEADER = "Location";

  private static final String TEST_URL = "TEST_URL";

  private static final String NOT_VALID_URL = "NOT VALID URL";

  private StepMockHelper<WebServiceMeta, WebServiceData> mockHelper;

  private WebService webServiceStep;

  @Before
  public void setUpBefore() {
    mockHelper =
      new StepMockHelper<>( "WebService", WebServiceMeta.class, WebServiceData.class );
    when( mockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        mockHelper.logChannelInterface );
    when( mockHelper.trans.isRunning() ).thenReturn( true );

    webServiceStep =
        spy( new WebService( mockHelper.stepMeta, mockHelper.stepDataInterface, 0, mockHelper.transMeta,
            mockHelper.trans ) );
  }

  @After
  public void cleanUp() {
    mockHelper.cleanUp();
  }

  @Test( expected = URISyntaxException.class )
  public void newHttpMethodWithInvalidUrl() throws URISyntaxException {
    webServiceStep.getHttpMethod( NOT_VALID_URL );
  }

  @Test
  public void getLocationFrom() {
    HttpPost postMethod = mock( HttpPost.class );
    Header locationHeader = new BasicHeader( LOCATION_HEADER, TEST_URL );
    doReturn( locationHeader ).when( postMethod ).getFirstHeader( LOCATION_HEADER );

    assertEquals( TEST_URL, WebService.getLocationFrom( postMethod ) );
  }

}
