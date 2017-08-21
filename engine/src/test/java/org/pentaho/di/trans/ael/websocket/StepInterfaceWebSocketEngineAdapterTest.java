/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.ael.websocket.event.MessageEventService;
import org.pentaho.di.trans.ael.websocket.handler.MessageEventHandler;
import org.pentaho.di.trans.ael.websocket.impl.DaemonMessageEvent;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;

import static org.hamcrest.CoreMatchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class StepInterfaceWebSocketEngineAdapterTest {
  @Mock private Operation op;
  @Mock private MessageEventService messageEventService;
  @Mock private StepMeta stepMeta;
  @Mock private TransMeta transMeta;
  @Mock private StepDataInterface dataInterface;
  @Mock private Trans tran;
  private StepInterfaceWebSocketEngineAdapter stepInterfaceWebSocketEngineAdapter;

  @Before
  public void before() throws KettleException {
    when( stepMeta.getName() ).thenReturn( "foo" );
    stepInterfaceWebSocketEngineAdapter =
      new StepInterfaceWebSocketEngineAdapter( op, messageEventService, stepMeta, transMeta, dataInterface, tran );
  }

  @Test
  public void testHandlerCreation() throws KettleException {
    verify( messageEventService, times( 3 ) )
      .addHandler( argThat( any( DaemonMessageEvent.class ) ), argThat( any( MessageEventHandler.class ) ) );
  }
}
