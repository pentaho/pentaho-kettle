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
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.engine.api.ExecutionResult;
import org.pentaho.di.engine.api.events.PDIEvent;
import org.pentaho.di.engine.api.model.Operation;
import org.pentaho.di.engine.api.model.Transformation;
import org.pentaho.di.engine.api.remote.ExecutionRequest;
import org.pentaho.di.engine.api.remote.Message;
import org.pentaho.di.engine.api.remote.StopMessage;
import org.pentaho.di.engine.api.reporting.LogEntry;
import org.pentaho.di.engine.api.reporting.LogLevel;
import org.pentaho.di.engine.api.reporting.Status;
import org.pentaho.di.engine.model.ActingPrincipal;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.ael.adapters.TransMetaConverter;
import org.pentaho.di.trans.ael.websocket.event.MessageEvent;
import org.pentaho.di.trans.ael.websocket.event.MessageEventService;
import org.pentaho.di.trans.ael.websocket.event.MessageEventType;
import org.pentaho.di.trans.ael.websocket.exception.HandlerRegistrationException;
import org.pentaho.di.trans.ael.websocket.exception.MessageEventHandlerExecutionException;
import org.pentaho.di.trans.ael.websocket.handler.MessageEventHandler;
import org.pentaho.di.trans.ael.websocket.impl.DaemonMessageEvent;
import org.pentaho.di.trans.ael.websocket.impl.MessageEventServiceImpl;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaDataCombi;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.Collection;

import static java.util.stream.Collectors.toMap;

/**
 * Created by fcamara on 8/17/17.
 */
public class TransWebSocketEngineAdapter extends Trans {

  public static final String ANONYMOUS_PRINCIPAL = "anonymous";
  private final Transformation transformation;
  private ExecutionRequest executionRequest;
  private final DaemonMessagesClientEndpoint daemonMessagesClientEndpoint;
  private final MessageEventService messageEventService;
  private LogLevel logLevel = null;

  private CompletableFuture<ExecutionResult>
    executionResultFuture;


  public static final Map<org.pentaho.di.core.logging.LogLevel, LogLevel> LEVEL_MAP = new HashMap<>();

  static {
    LEVEL_MAP.put( org.pentaho.di.core.logging.LogLevel.BASIC, LogLevel.BASIC );
    LEVEL_MAP.put( org.pentaho.di.core.logging.LogLevel.DEBUG, LogLevel.DEBUG );
    LEVEL_MAP.put( org.pentaho.di.core.logging.LogLevel.DETAILED, LogLevel.DETAILED );
    LEVEL_MAP.put( org.pentaho.di.core.logging.LogLevel.ERROR, LogLevel.ERROR );
    LEVEL_MAP.put( org.pentaho.di.core.logging.LogLevel.MINIMAL, LogLevel.MINIMAL );
    LEVEL_MAP.put( org.pentaho.di.core.logging.LogLevel.ROWLEVEL, LogLevel.TRACE );
  }

  public TransWebSocketEngineAdapter( TransMeta transMeta, String host, String port, boolean ssl ) {
    transformation = TransMetaConverter.convert( transMeta );
    this.transMeta = transMeta;
    this.messageEventService = new MessageEventServiceImpl();
    this.daemonMessagesClientEndpoint = new DaemonMessagesClientEndpoint( host, port, ssl, messageEventService );
  }

  @Override public void setLogLevel( org.pentaho.di.core.logging.LogLevel logLogLevel ) {
    this.logLevel = LEVEL_MAP.getOrDefault( logLogLevel, LogLevel.MINIMAL );
  }

  @Override public void killAll() {
    throw new UnsupportedOperationException( "Not yet implemented" );
  }

  @Override public void stopAll() {
    daemonMessagesClientEndpoint.sendMessage( new StopMessage( "User Request" ) );
  }

  @Override public void prepareExecution( String[] arguments ) throws KettleException {
    activateParameters();
    transMeta.activateParameters();
    transMeta.setInternalKettleVariables();

    Map<String, Object> env = Arrays.stream( transMeta.listVariables() )
      .collect( toMap( Function.identity(), transMeta::getVariable ) );

    this.executionRequest = new ExecutionRequest( new HashMap<>(), env, transformation, new HashMap<>(), logLevel,
      getActingPrincipal( transMeta ) );

    setSteps( new ArrayList<>( opsToSteps() ) );
    wireStatusToTransListeners();

    subscribeToOpLogging();
    subscribeToTransLogging();

    setReadyToStart( true );
  }

  private void logToChannel( LogChannelInterface logChannel, LogEntry data ) {
    LogLevel logLogLevel = data.getLogLogLevel();
    switch ( logLogLevel ) {
      case ERROR:
        logChannel.logError( data.getMessage() );
        break;
      case MINIMAL:
        logChannel.logMinimal( data.getMessage() );
        break;
      case BASIC:
        logChannel.logBasic( data.getMessage() );
        break;
      case DETAILED:
        logChannel.logDetailed( data.getMessage() );
        break;
      case DEBUG:
        logChannel.logDebug( data.getMessage() );
        break;
      case TRACE:
        logChannel.logRowlevel( data.getMessage() );
        break;
    }
  }

  private void subscribeToOpLogging() throws KettleException {
    transformation.getOperations().stream().forEach( operation -> {
      try {
        messageEventService.addHandler( new DaemonMessageEvent( MessageEventType.OPERATION_LOG, operation.getId() ),
          new MessageEventHandler<MessageEvent>() {
            @Override
            public void execute( Message message ) throws MessageEventHandlerExecutionException {
              PDIEvent<Operation, LogEntry> event = (PDIEvent<Operation, LogEntry>) message;
              LogEntry logEntry = event.getData();
              StepInterface stepInterface = findStepInterface( operation.getId(), 0 );
              if ( stepInterface != null ) {
                LogChannelInterface logChannel = stepInterface.getLogChannel();
                logToChannel( logChannel, logEntry );
              } else {
                // Could not find step, log at transformation level instead
                logToChannel( getLogChannel(), logEntry );
              }
            }

            @Override
            public boolean isInterested( MessageEvent event ) {
              return event.getType() == MessageEventType.OPERATION_LOG && operation.getId()
                .equals( event.getObjectId() );
            }

            @Override
            public String getIdentifier() {
              return MessageEventType.OPERATION_LOG + operation.getId();
            }
          } );
      } catch ( HandlerRegistrationException e ) {
        //TODO: treat the exception
        e.printStackTrace();
      }
    } );
  }

  private void subscribeToTransLogging() throws KettleException {
    messageEventService.addHandler( new DaemonMessageEvent( MessageEventType.TRANSFORMATION_LOG ),
      new MessageEventHandler<MessageEvent>() {
        @Override
        public void execute( Message message ) throws MessageEventHandlerExecutionException {
          PDIEvent<Transformation, LogEntry> event = (PDIEvent<Transformation, LogEntry>) message;
          LogEntry data = event.getData();
          logToChannel( getLogChannel(), data );
        }

        @Override
        public boolean isInterested( MessageEvent event ) {
          return event.getType() == MessageEventType.TRANSFORMATION_LOG && event.getObjectId() == null;
        }

        @Override
        public String getIdentifier() {
          return MessageEventType.TRANSFORMATION_LOG.name();
        }
      } );
  }

  private void wireStatusToTransListeners() throws KettleException {
    messageEventService.addHandler( new DaemonMessageEvent( MessageEventType.TRANSFORMATION_STATUS ),
      new MessageEventHandler<MessageEvent>() {
        @Override
        public void execute( Message message ) throws MessageEventHandlerExecutionException {
          PDIEvent<Transformation, Status> transStatusEvent = (PDIEvent<Transformation, Status>) message;
          addStepPerformanceSnapShot();
          getTransListeners().forEach( l -> {
            try {
              switch ( transStatusEvent.getData() ) {
                case RUNNING:
                  l.transStarted( TransWebSocketEngineAdapter.this );
                  l.transActive( TransWebSocketEngineAdapter.this );
                  break;
                case PAUSED:
                  break;
                case STOPPED:
                  break;
                case FAILED:
                case FINISHED:
                  l.transFinished( TransWebSocketEngineAdapter.this );
                  setFinished( true );
                  break;
              }
            } catch ( KettleException e ) {
              throw new RuntimeException( e );
            }
          } );
        }

        @Override
        public boolean isInterested( MessageEvent event ) {
          return event.getType() == MessageEventType.TRANSFORMATION_STATUS && event.getObjectId() == null;
        }

        @Override
        public String getIdentifier() {
          return MessageEventType.TRANSFORMATION_STATUS.name();
        }
      } );

    messageEventService
      .addHandler( new DaemonMessageEvent( MessageEventType.ERROR ), new MessageEventHandler<MessageEvent>() {
        @Override
        public void execute( Message message ) throws MessageEventHandlerExecutionException {
          //Throwable throwable = null;

          //getLogChannel().logError( "Error Executing Transformation", throwable );
          setFinished( true );
          // emit error on all steps
          getSteps().stream().map( stepMetaDataCombi -> stepMetaDataCombi.step ).forEach( step -> {
            step.setStopped( true );
            step.setRunning( false );
          } );
          getTransListeners().forEach( l -> {
            try {
              l.transFinished( TransWebSocketEngineAdapter.this );
            } catch ( KettleException e ) {
              getLogChannel().logError( "Error notifying trans listener", e );
            }
          } );
        }

        @Override
        public boolean isInterested( MessageEvent event ) {
          return event.getType() == MessageEventType.ERROR;
        }

        @Override
        public String getIdentifier() {
          return MessageEventType.ERROR.name();
        }
      } );

    messageEventService
      .addHandler( new DaemonMessageEvent( MessageEventType.COMPLETE ), new MessageEventHandler<MessageEvent>() {
        @Override
        public void execute( Message message ) throws MessageEventHandlerExecutionException {
          setFinished( true );
          getTransListeners().forEach( l -> {
            try {
              l.transFinished( TransWebSocketEngineAdapter.this );
            } catch ( KettleException e ) {
              getLogChannel().logError( "Error notifying trans listener", e );
            }
          } );
        }

        @Override
        public boolean isInterested( MessageEvent event ) {
          return event.getType() == MessageEventType.COMPLETE;
        }

        @Override
        public String getIdentifier() {
          return MessageEventType.COMPLETE.name();
        }
      } );

    messageEventService
      .addHandler( new DaemonMessageEvent( MessageEventType.STOP ), new MessageEventHandler<MessageEvent>() {
        @Override
        public void execute( Message message ) throws MessageEventHandlerExecutionException {
          setFinished( true );
          getTransListeners().forEach( l -> {
            try {
              l.transFinished( TransWebSocketEngineAdapter.this );
            } catch ( KettleException e ) {
              getLogChannel().logError( "Error notifying trans listener", e );
            }
          } );
        }

        @Override
        public boolean isInterested( MessageEvent event ) {
          return event.getType() == MessageEventType.STOP;
        }

        @Override
        public String getIdentifier() {
          return MessageEventType.STOP.name();
        }
      } );

  }

  private Collection<StepMetaDataCombi> opsToSteps() {
    Map<Operation, StepMetaDataCombi> operationToCombi = transformation.getOperations().stream()
      .collect( toMap( Function.identity(),
        op -> {
          StepMetaDataCombi combi = new StepMetaDataCombi();
          combi.stepMeta = StepMeta.fromXml( (String) op.getConfig().get( TransMetaConverter.STEP_META_CONF_KEY ) );
          try {
            combi.data = new StepDataInterfaceWebSocketEngineAdapter( op, messageEventService );
            combi.step = new StepInterfaceWebSocketEngineAdapter( op, messageEventService, combi.stepMeta, transMeta,
              combi.data, this );
          } catch ( KettleException e ) {
            //TODO: treat the exception
            e.printStackTrace();
          }
          combi.meta = combi.stepMeta.getStepMetaInterface();
          combi.stepname = combi.stepMeta.getName();
          return combi;
        } ) );
    return operationToCombi.values();
  }

  @Override public void startThreads() throws KettleException {
    daemonMessagesClientEndpoint.sendMessage( executionRequest );
  }

  @Override public void waitUntilFinished() {
    try {
      ExecutionResult result = executionResultFuture.get();
    } catch ( InterruptedException e ) {
      throw new RuntimeException( "Waiting for transformation to be finished interrupted!", e );
    } catch ( ExecutionException e ) {
      throw new RuntimeException( "Error executing Transformation or waiting for it to stop", e );
    }
  }


  // ======================== May want to implement ================================= //


  @Override public RowProducer addRowProducer( String stepname, int copynr ) throws KettleException {
    throw new UnsupportedOperationException( "Not yet implemented" );
  }

  private Principal getActingPrincipal( TransMeta transMeta ) {
    if ( transMeta.getRepository() == null || transMeta.getRepository().getUserInfo() == null ) {
      return new ActingPrincipal( ANONYMOUS_PRINCIPAL );
    }
    return new ActingPrincipal( transMeta.getRepository().getUserInfo().getName() );
  }
}
