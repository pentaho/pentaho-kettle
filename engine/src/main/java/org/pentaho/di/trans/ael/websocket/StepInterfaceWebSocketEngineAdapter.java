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

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.engine.api.events.PDIEvent;
import org.pentaho.di.engine.api.model.Operation;
import org.pentaho.di.engine.api.model.Row;
import org.pentaho.di.engine.api.model.Rows;
import org.pentaho.di.engine.api.remote.Message;
import org.pentaho.di.engine.api.reporting.Metrics;
import org.pentaho.di.engine.api.reporting.Status;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.ael.websocket.event.MessageEvent;
import org.pentaho.di.trans.ael.websocket.event.MessageEventService;
import org.pentaho.di.trans.ael.websocket.event.MessageEventType;
import org.pentaho.di.trans.ael.websocket.exception.MessageEventHandlerExecutionException;
import org.pentaho.di.trans.ael.websocket.handler.MessageEventHandler;
import org.pentaho.di.trans.ael.websocket.impl.DaemonMessageEvent;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;

import java.util.Collections;

import static org.pentaho.di.engine.api.model.Rows.TYPE.OUT;

/**
 * Adapts AEL Operation events to the StepInterface. This class will register handlers to engine events and translate
 * them to corresponding StepInterface updates.
 */
public class StepInterfaceWebSocketEngineAdapter extends BaseStep {

  private final Operation operation;
  private final MessageEventService messageEventService;

  public StepInterfaceWebSocketEngineAdapter( Operation op, MessageEventService messageEventService, StepMeta stepMeta,
                                              TransMeta transMeta, StepDataInterface dataInterface, Trans trans )
    throws KettleException {
    super( stepMeta, dataInterface, 0, transMeta, trans );
    operation = op;
    this.messageEventService = messageEventService;
    setInputRowSets( Collections.emptyList() );
    setOutputRowSets( Collections.emptyList() );
    init();
  }

  @Override public void dispatch() {
    // No thanks. I'll take it from here.
  }

  private void init() throws KettleException {
    createHandlerToMetrics();
    createHandlerToStatus();
    createHandlerToRows();
  }

  private void createHandlerToRows() throws KettleException {
    messageEventService.addHandler( new DaemonMessageEvent( MessageEventType.ROWS, operation.getId() ),
      new MessageEventHandler<MessageEvent>() {
        @Override
        public void execute( Message message ) throws MessageEventHandlerExecutionException {
          PDIEvent<Operation, Rows> data = (PDIEvent<Operation, Rows>) message;
          if ( data.getData().getType().equals( OUT ) ) {
            data.getData().stream().forEach( r -> putRow( r ) );
          }
        }

        @Override
        public boolean isInterested( MessageEvent event ) {
          return event.getType() == MessageEventType.ROWS && operation.getId().equals( event.getObjectId() );
        }

        @Override
        public String getIdentifier() {
          return MessageEventType.ROWS.name() + operation.getId();
        }
      } );
  }

  private void createHandlerToStatus() throws KettleException {
    messageEventService.addHandler( new DaemonMessageEvent( MessageEventType.OPERATION_STATUS, operation.getId() ),
      new MessageEventHandler<MessageEvent>() {
        @Override
        public void execute( Message message ) throws MessageEventHandlerExecutionException {
          PDIEvent<Operation, Status> data = (PDIEvent<Operation, Status>) message;
          switch ( data.getData() ) {
            case RUNNING:
              StepInterfaceWebSocketEngineAdapter.this.setRunning( true );
              break;
            case PAUSED:
              StepInterfaceWebSocketEngineAdapter.this.setPaused( true );
              break;
            case STOPPED:
              StepInterfaceWebSocketEngineAdapter.this.setStopped( true );
              break;
            case FAILED:
            case FINISHED:
              StepInterfaceWebSocketEngineAdapter.this.setRunning( false );
              break;
          }
        }

        @Override
        public boolean isInterested( MessageEvent event ) {
          return event.getType() == MessageEventType.OPERATION_STATUS && operation.getId()
            .equals( event.getObjectId() );
        }

        @Override
        public String getIdentifier() {
          return MessageEventType.OPERATION_STATUS.name() + operation.getId();
        }
      } );
  }

  private void createHandlerToMetrics() throws KettleException {
    messageEventService.addHandler( new DaemonMessageEvent( MessageEventType.METRICS, operation.getId() ),
      new MessageEventHandler<MessageEvent>() {
        @Override
        public void execute( Message message ) throws MessageEventHandlerExecutionException {
          PDIEvent<Operation, Metrics> data = (PDIEvent<Operation, Metrics>) message;

          StepInterfaceWebSocketEngineAdapter.this.setLinesRead( data.getData().getIn() );
          StepInterfaceWebSocketEngineAdapter.this.setLinesWritten( data.getData().getOut() );
        }

        @Override
        public boolean isInterested( MessageEvent event ) {
          return event.getType() == MessageEventType.METRICS && operation.getId().equals( event.getObjectId() );
        }

        @Override
        public String getIdentifier() {
          return MessageEventType.METRICS.name() + operation.getId();
        }
      } );
  }

  /**
   * Writes a Row to all rowListeners
   **/
  private void putRow( Row row ) {
    //TODO:implement
  }
}
