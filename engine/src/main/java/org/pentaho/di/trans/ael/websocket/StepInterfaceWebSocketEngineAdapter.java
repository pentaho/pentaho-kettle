/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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
import org.pentaho.di.engine.api.remote.RemoteSource;
import org.pentaho.di.engine.api.reporting.Metrics;
import org.pentaho.di.engine.api.reporting.Status;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.ael.websocket.exception.MessageEventHandlerExecutionException;
import org.pentaho.di.trans.ael.websocket.handler.MessageEventHandler;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.pentaho.di.trans.step.StepStatus;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.pentaho.di.engine.api.model.Rows.TYPE.OUT;

/**
 * Adapts AEL Operation events to the StepInterface. This class will register handlers to engine events and translate
 * them to corresponding StepInterface updates.
 */
public class StepInterfaceWebSocketEngineAdapter extends BaseStep {

  private static final String ROWS_HANDLER_ID = "ROWS_STEP_INTERFACE_";
  private static final String METRICS_HANDLER_ID = "METRICS_STEP_INTERFACE_";
  private static final String OPERATION_STATUS_HANDLER_ID = "OPERATION_STATUS_STEP_INTERFACE_";

  private final Operation operation;
  private final MessageEventService messageEventService;
  private List<StepMetaDataCombi> subSteps;

  public StepInterfaceWebSocketEngineAdapter( Operation op, MessageEventService messageEventService, StepMeta stepMeta,
                                              TransMeta transMeta, StepDataInterface dataInterface, Trans trans,
                                              List<StepMetaDataCombi> subSteps )
    throws KettleException {
    super( stepMeta, dataInterface, 0, transMeta, trans );
    operation = op;
    this.messageEventService = messageEventService;
    this.subSteps = subSteps;
    setInputRowSets( Collections.emptyList() );
    setOutputRowSets( Collections.emptyList() );
    init();
  }

  @Override public void dispatch() {
    // No thanks. I'll take it from here.
  }

  @Override public Collection<StepStatus> subStatuses() {
    return subSteps.stream().map( combi -> new StepStatus( combi.step ) ).collect( Collectors.toList() );
  }

  private void init() throws KettleException {
    createHandlerToMetrics();
    createHandlerToStatus();
    createHandlerToRows();
  }

  private void createHandlerToRows() throws KettleException {
    messageEventService.addHandler( Util.getOperationRowEvent( operation.getId() ),
      new MessageEventHandler() {
        @Override
        public void execute( Message message ) throws MessageEventHandlerExecutionException {
          PDIEvent<RemoteSource, Rows> data = (PDIEvent<RemoteSource, Rows>) message;
          if ( data.getData().getType().equals( OUT ) ) {
            data.getData().stream().forEach( r -> putRow( r ) );
          }
        }

        @Override
        public String getIdentifier() {
          return ROWS_HANDLER_ID + operation.getKey();
        }
      } );
  }

  private void createHandlerToStatus() throws KettleException {
    messageEventService.addHandler( Util.getOperationStatusEvent( operation.getId() ),
      new MessageEventHandler() {
        @Override
        public void execute( Message message ) throws MessageEventHandlerExecutionException {
          PDIEvent<RemoteSource, Status> data = (PDIEvent<RemoteSource, Status>) message;
          switch ( data.getData() ) {
            case RUNNING:
              StepInterfaceWebSocketEngineAdapter.this.setRunning( true );
              break;
            case PAUSED:
              StepInterfaceWebSocketEngineAdapter.this.setPaused( true );
              break;
            case FAILED:
              StepInterfaceWebSocketEngineAdapter.this.setErrors( 1L );
            case STOPPED:
              StepInterfaceWebSocketEngineAdapter.this.setStopped( true );
              break;
            case FINISHED:
              StepInterfaceWebSocketEngineAdapter.this.setRunning( false );
              break;
          }
        }

        @Override
        public String getIdentifier() {
          return OPERATION_STATUS_HANDLER_ID + operation.getKey();
        }
      } );
  }

  private void createHandlerToMetrics() throws KettleException {
    messageEventService.addHandler( Util.getMetricEvents( operation.getId() ),
      new MessageEventHandler() {
        @Override
        public void execute( Message message ) throws MessageEventHandlerExecutionException {
          PDIEvent<RemoteSource, Metrics> data = (PDIEvent<RemoteSource, Metrics>) message;

          if ( data.getData().getIn() > 0 ) {
            StepInterfaceWebSocketEngineAdapter.this.setLinesRead( data.getData().getIn() );
          }
          if ( data.getData().getOut() > 0 ) {
            StepInterfaceWebSocketEngineAdapter.this.setLinesWritten( data.getData().getOut() );
          }
        }

        @Override
        public String getIdentifier() {
          return METRICS_HANDLER_ID + operation.getKey();
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
