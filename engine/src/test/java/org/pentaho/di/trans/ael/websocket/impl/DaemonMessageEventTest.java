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
package org.pentaho.di.trans.ael.websocket.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.engine.api.events.DataEvent;
import org.pentaho.di.engine.api.events.LogEvent;
import org.pentaho.di.engine.api.events.MetricsEvent;
import org.pentaho.di.engine.api.events.StatusEvent;
import org.pentaho.di.engine.api.model.Operation;
import org.pentaho.di.engine.api.model.Transformation;
import org.pentaho.di.engine.api.remote.Message;
import org.pentaho.di.engine.api.remote.StopMessage;
import org.pentaho.di.trans.ael.websocket.event.MessageEventType;
import org.pentaho.di.trans.ael.websocket.exception.MessageEventFireEventException;

import static org.pentaho.di.core.util.Assert.assertNull;
import static org.pentaho.di.core.util.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

@RunWith( MockitoJUnitRunner.class )
public class DaemonMessageEventTest {
  @Mock private MetricsEvent metricsEvent;
  @Mock private LogEvent logEvent;
  @Mock private StatusEvent statusEvent;
  @Mock private DataEvent dataEvent;
  @Mock private StopMessage stopEvent;
  @Mock private Transformation transformation;
  @Mock private Operation operation;
  private DaemonMessageEvent daemonMessageEvent;

  @Before
  public void before() {
    doReturn( "operationId" ).when( operation ).getId();
  }

  @Test
  public void testEventType() {
    daemonMessageEvent = new DaemonMessageEvent( MessageEventType.TRANSFORMATION_LOG );

    assertTrue( daemonMessageEvent.getType() == MessageEventType.TRANSFORMATION_LOG );
    assertNull( daemonMessageEvent.getObjectId() );
  }

  @Test
  public void testEventTypeAndObjectId() {
    daemonMessageEvent = new DaemonMessageEvent( MessageEventType.OPERATION_LOG, "OBJECT_ID" );

    assertTrue( daemonMessageEvent.getType() == MessageEventType.OPERATION_LOG );
    assertTrue( "OBJECT_ID".equals( daemonMessageEvent.getObjectId() ) );
  }

  @Test
  public void testMetricEvent() throws MessageEventFireEventException {
    doReturn( operation ).when( metricsEvent ).getSource();
    daemonMessageEvent = new DaemonMessageEvent( metricsEvent );

    assertTrue( daemonMessageEvent.getType() == MessageEventType.METRICS );
    assertTrue( "operationId".equals( daemonMessageEvent.getObjectId() ) );
  }

  @Test
  public void testOperationLogEvent() throws MessageEventFireEventException {
    doReturn( operation ).when( logEvent ).getSource();
    daemonMessageEvent = new DaemonMessageEvent( logEvent );

    assertTrue( daemonMessageEvent.getType() == MessageEventType.OPERATION_LOG );
    assertTrue( "operationId".equals( daemonMessageEvent.getObjectId() ) );
  }

  @Test
  public void testTransformationLogEvent() throws MessageEventFireEventException {
    doReturn( transformation ).when( logEvent ).getSource();
    daemonMessageEvent = new DaemonMessageEvent( logEvent );

    assertTrue( daemonMessageEvent.getType() == MessageEventType.TRANSFORMATION_LOG );
    assertNull( daemonMessageEvent.getObjectId() );
  }

  @Test
  public void testOperationStatusEvent() throws MessageEventFireEventException {
    doReturn( operation ).when( statusEvent ).getSource();
    daemonMessageEvent = new DaemonMessageEvent( statusEvent );

    assertTrue( daemonMessageEvent.getType() == MessageEventType.OPERATION_STATUS );
    assertTrue( "operationId".equals( daemonMessageEvent.getObjectId() ) );
  }

  @Test
  public void testTransformationStatusEvent() throws MessageEventFireEventException {
    doReturn( transformation ).when( statusEvent ).getSource();
    daemonMessageEvent = new DaemonMessageEvent( statusEvent );

    assertTrue( daemonMessageEvent.getType() == MessageEventType.TRANSFORMATION_STATUS );
    assertNull( daemonMessageEvent.getObjectId() );
  }

  @Test
  public void testDataEvent() throws MessageEventFireEventException {
    doReturn( operation ).when( dataEvent ).getSource();
    daemonMessageEvent = new DaemonMessageEvent( dataEvent );

    assertTrue( daemonMessageEvent.getType() == MessageEventType.ROWS );
    assertTrue( "operationId".equals( daemonMessageEvent.getObjectId() ) );
  }

  @Test
  public void testStopEvent() throws MessageEventFireEventException {
    doReturn( "Stop Message" ).when( stopEvent ).getReasonPhrase();
    daemonMessageEvent = new DaemonMessageEvent( stopEvent );

    assertTrue( daemonMessageEvent.getType() == MessageEventType.STOP );
  }

  @Test( expected = MessageEventFireEventException.class )
  public void testInvalidMessageType() throws MessageEventFireEventException {
    doReturn( "Stop Message" ).when( stopEvent ).getReasonPhrase();
    daemonMessageEvent = new DaemonMessageEvent( new Message() {
      @Override public int hashCode() {
        return super.hashCode();
      }

      @Override public boolean equals( Object obj ) {
        return super.equals( obj );
      }

      @Override protected Object clone() throws CloneNotSupportedException {
        return super.clone();
      }

      @Override public String toString() {
        return super.toString();
      }

      @Override protected void finalize() throws Throwable {
        super.finalize();
      }
    } );

    assertTrue( daemonMessageEvent.getType() == MessageEventType.STOP );
  }

}
