/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.salesforce;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

public class SalesforceStepTest {

  private StepMockHelper<SalesforceStepMeta, SalesforceStepData> smh;

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init( false );
  }

  @Before
  public void setUp() throws KettleException {
    smh =
      new StepMockHelper<SalesforceStepMeta, SalesforceStepData>( "Salesforce", SalesforceStepMeta.class,
          SalesforceStepData.class );
    when( smh.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
      smh.logChannelInterface );
    when( smh.trans.isRunning() ).thenReturn( true );
  }

  @Test
  public void testErrorHandling() {
    SalesforceStepMeta meta = mock( SalesforceStepMeta.class, Mockito.CALLS_REAL_METHODS );
    assertFalse( meta.supportsErrorHandling() );
  }

  @Test
  public void testInitDispose() {
    SalesforceStepMeta meta = mock( SalesforceStepMeta.class, Mockito.CALLS_REAL_METHODS );
    SalesforceStep step = spy( new MockSalesforceStep( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans ) );

    /*
     * Salesforce Step should fail if username and password are not set
     * We should not set a default account for all users
     */
    meta.setDefault();
    assertFalse( step.init( meta, smh.stepDataInterface ) );

    meta.setDefault();
    meta.setTargetURL( null );
    assertFalse( step.init( meta, smh.stepDataInterface ) );

    meta.setDefault();
    meta.setUsername( "anonymous" );
    assertFalse( step.init( meta, smh.stepDataInterface ) );

    meta.setDefault();
    meta.setUsername( "anonymous" );
    meta.setPassword( "myPwd" );
    meta.setModule( null );
    assertFalse( step.init( meta, smh.stepDataInterface ) );

    /*
     * After setting username and password, we should have enough defaults to properly init
     */
    meta.setDefault();
    meta.setUsername( "anonymous" );
    meta.setPassword( "myPwd" );
    assertTrue( step.init( meta, smh.stepDataInterface ) );

    // Dispose check
    assertNotNull( smh.stepDataInterface.connection );
    step.dispose( meta, smh.stepDataInterface );
    assertNull( smh.stepDataInterface.connection );
  }

  class MockSalesforceStep extends SalesforceStep {
    public MockSalesforceStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
        TransMeta transMeta, Trans trans ) {
      super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
    }
  }
}
