/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.singlethreader;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.PDI_11948_StepsTestsParent;

/**
 * The PDI_11948_SimpleMappingTest class tests Single Threader step of PDI-11948 bug. It's check if HttpServletResponse
 * object is null and call or not setServletReponse( HttpServletResponse response ) method of appropriate Trans object.
 * 
 * @author Yury Bakhmutski
 */
public class PDI_11948_SingleThreaderTest extends PDI_11948_StepsTestsParent<SingleThreader, SingleThreaderData> {

  @Override
  @Before
  public void init() throws Exception {
    super.init();
    stepMock = mock( SingleThreader.class );
    stepDataMock = mock( SingleThreaderData.class );
  }

  @Test
  public void testMappingStep() {

    when( stepMock.getData() ).thenReturn( stepDataMock );
    when( stepDataMock.getMappingTrans() ).thenReturn( transMock );

    // stubbing methods for null-checking
    when( stepMock.getTrans() ).thenReturn( transMock );
    when( transMock.getServletResponse() ).thenReturn( null );

    doThrow( new RuntimeException( "The getServletResponse() mustn't be executed!" ) ).when( transMock )
        .setServletReponse( any( HttpServletResponse.class ) );

    doCallRealMethod().when( stepMock ).initServletConfig();
    stepMock.initServletConfig();
  }
}
