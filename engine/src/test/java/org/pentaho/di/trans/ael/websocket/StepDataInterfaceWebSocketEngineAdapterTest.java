/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 */
package org.pentaho.di.trans.ael.websocket;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.engine.api.model.Operation;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;
import static org.pentaho.di.trans.step.BaseStepData.StepExecutionStatus.STATUS_INIT;

@RunWith( MockitoJUnitRunner.class )
public class StepDataInterfaceWebSocketEngineAdapterTest {
  @Mock private Operation op;
  private MessageEventService messageEventService;
  private StepDataInterfaceWebSocketEngineAdapter stepDataInterfaceWebSocketEngineAdapter;

  @Before
  public void before() throws KettleException {
    when( op.getId() ).thenReturn( "Operation ID" );
    messageEventService = new MessageEventService();

    stepDataInterfaceWebSocketEngineAdapter =
      new StepDataInterfaceWebSocketEngineAdapter( op, messageEventService );
  }

  @Test
  public void testHandlerCreation() throws KettleException {
    assertTrue( messageEventService.hasHandlers( Util.getOperationStatusEvent( op.getId() ) ) );
    assertTrue( messageEventService.getHandlersFor( Util.getOperationStatusEvent( op.getId() ) ).size() == 1 );
  }

  @Test
  public void testInitValues() throws KettleException {
    //init values only
    assertTrue( stepDataInterfaceWebSocketEngineAdapter.getStatus() == STATUS_INIT );
    assertTrue( stepDataInterfaceWebSocketEngineAdapter.isInitialising() );
    assertFalse( stepDataInterfaceWebSocketEngineAdapter.isEmpty() );
    assertFalse( stepDataInterfaceWebSocketEngineAdapter.isRunning() );
    assertFalse( stepDataInterfaceWebSocketEngineAdapter.isIdle() );
    assertFalse( stepDataInterfaceWebSocketEngineAdapter.isDisposed() );
    assertFalse( stepDataInterfaceWebSocketEngineAdapter.isFinished() );
  }
}
