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

package org.pentaho.di.trans.steps.mapping;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.trans.steps.PDI_11948_StepsTestsParent;

import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The PDI_11948_MappingTest class tests mapping step of PDI-11948 bug. It's check if HttpServletResponse object is null
 * and call or not setServletReponse( HttpServletResponse response ) method of appropriate Trans object.
 * 
 * @author Yury Bakhmutski
 * @see org.pentaho.di.trans.steps.mapping.Mapping
 * @see org.pentaho.di.trans.steps.simplemapping.SimpleMapping
 * @see org.pentaho.di.trans.steps.transexecutor.TransExecutor
 * @see org.pentaho.di.trans.steps.singlethreader.SingleThreader
 */
public class PDI_11948_MappingTest extends PDI_11948_StepsTestsParent<Mapping, MappingData> {

  @Override
  @Before
  public void init() throws Exception {
    super.init();
    stepMock = mock( Mapping.class );
    stepDataMock = mock( MappingData.class );
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
