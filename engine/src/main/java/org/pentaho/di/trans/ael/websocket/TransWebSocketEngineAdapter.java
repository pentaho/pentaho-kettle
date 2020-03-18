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

import com.google.common.collect.Maps;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.engine.api.events.PDIEvent;
import org.pentaho.di.engine.api.model.Operation;
import org.pentaho.di.engine.api.model.Transformation;
import org.pentaho.di.engine.api.remote.ExecutionRequest;
import org.pentaho.di.engine.api.remote.Message;
import org.pentaho.di.engine.api.remote.RemoteSource;
import org.pentaho.di.engine.api.remote.StopMessage;
import org.pentaho.di.engine.api.reporting.LogEntry;
import org.pentaho.di.engine.api.reporting.LogLevel;
import org.pentaho.di.engine.api.reporting.Status;
import org.pentaho.di.engine.model.ActingPrincipal;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.ael.adapters.TransMetaConverter;
import org.pentaho.di.trans.ael.websocket.exception.HandlerRegistrationException;
import org.pentaho.di.trans.ael.websocket.exception.MessageEventHandlerExecutionException;
import org.pentaho.di.trans.ael.websocket.handler.MessageEventHandler;
import org.pentaho.di.trans.ael.websocket.handler.StopMessageEventHandler;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.pentaho.di.trans.step.StepMetaInterface;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.stream.Collectors.toMap;
import static org.pentaho.di.trans.step.BaseStepData.StepExecutionStatus.STATUS_HALTING;
import static org.pentaho.di.trans.step.BaseStepData.StepExecutionStatus.STATUS_RUNNING;

/**
 * Created by fcamara on 8/17/17.
 */
public class TransWebSocketEngineAdapter extends Trans {

  private static final String OPERATION_LOG = "OPERATION_LOG_TRANS_WEBSOCK_";
  private static final String TRANSFORMATION_LOG = "TRANSFORMATION_LOG_TRANS_WEBSOCK";
  private static final String TRANSFORMATION_STATUS = "TRANSFORMATION_STATUS_TRANS_WEBSOCK";
  private static final String TRANSFORMATION_ERROR = "TRANSFORMATION_ERROR_TRANS_WEBSOCK";

  //session monitor properties
  private static final int SLEEP_TIME_MS = 10000;
  private static final int MAX_TEST_FAILED = 3;
  private ExecutorService sessionMonitor = null;

  private final Transformation transformation;
  private ExecutionRequest executionRequest;
  private DaemonMessagesClientEndpoint daemonMessagesClientEndpoint = null;
  protected final MessageEventService messageEventService;
  private LogLevel logLevel = null;

  private final String host;
  private final int port;
  private final boolean ssl;
  private boolean cancelling = false;

  //completion signal used to wait until Transformation is finished
  private CountDownLatch transFinishedSignal = new CountDownLatch( 1 );
  private AtomicInteger errors = new AtomicInteger();


  private static final Map<org.pentaho.di.core.logging.LogLevel, LogLevel> LEVEL_MAP = new HashMap<>();

  static {
    LEVEL_MAP.put( org.pentaho.di.core.logging.LogLevel.BASIC, LogLevel.BASIC );
    LEVEL_MAP.put( org.pentaho.di.core.logging.LogLevel.DEBUG, LogLevel.DEBUG );
    LEVEL_MAP.put( org.pentaho.di.core.logging.LogLevel.DETAILED, LogLevel.DETAILED );
    LEVEL_MAP.put( org.pentaho.di.core.logging.LogLevel.ERROR, LogLevel.ERROR );
    LEVEL_MAP.put( org.pentaho.di.core.logging.LogLevel.MINIMAL, LogLevel.MINIMAL );
    LEVEL_MAP.put( org.pentaho.di.core.logging.LogLevel.ROWLEVEL, LogLevel.TRACE );
  }

  public TransWebSocketEngineAdapter( TransMeta transMeta, String host, int port, boolean ssl ) {
    transformation = TransMetaConverter.convert( transMeta );
    this.transMeta = transMeta;
    this.messageEventService = new MessageEventService();
    this.host = host;
    this.port = port;
    this.ssl = ssl;
  }

  public DaemonMessagesClientEndpoint getDaemonEndpoint() throws KettleException {
    try {
      if ( daemonMessagesClientEndpoint == null ) {
        daemonMessagesClientEndpoint = new DaemonMessagesClientEndpoint( host, port, ssl, messageEventService );
      }
      return daemonMessagesClientEndpoint;
    } catch ( KettleException e ) {
      finishProcess( true );
      transFinishedSignal.countDown();
      throw e;
    }
  }

  @Override public void setLogLevel( org.pentaho.di.core.logging.LogLevel logLogLevel ) {
    this.logLevel = LEVEL_MAP.getOrDefault( logLogLevel, LogLevel.MINIMAL );
  }

  @Override public void killAll() {
    throw new UnsupportedOperationException( "Not yet implemented" );
  }

  @Override public void stopAll() {
    try {
      cancelling = true;
      getDaemonEndpoint().sendMessage( new StopMessage( getErrors() == 0 ? "User Request" : "Error reported" ) );
      if ( getErrors() == 0 ) {
        waitUntilFinished();
        finishProcess( true );
      }
    } catch ( KettleException e ) {
      getLogChannel().logDebug( e.getMessage() );
    } finally {
      cancelling = false;
    }
  }

  @Override public void safeStop() {
    try {
      getDaemonEndpoint().sendMessage(
        StopMessage.builder()
          .reasonPhrase( "User Request" )
          .safeStop( true )
          .build() );

      //stopped but still running will yield status Halting
      getSteps().stream().map( stepMetaDataCombi -> stepMetaDataCombi.step )
        .filter( stepInterface -> stepInterface.getInputRowSets().isEmpty() )
        .forEach( step -> step.setStopped( true ) );
      Executors.newSingleThreadExecutor().submit( () -> {
        waitUntilFinished();
        finishProcess( true );
      } );
    } catch ( KettleException e ) {
      getLogChannel().logDebug( e.getMessage(), e );
    }
  }

  @Override public void prepareExecution( String[] arguments ) throws KettleException {
    activateParameters();
    transMeta.activateParameters();
    transMeta.setInternalKettleVariables();

    Map<String, Object> env = Arrays.stream( transMeta.listVariables() )
      .collect( toMap( Function.identity(), transMeta::getVariable ) );

    this.executionRequest = new ExecutionRequest( new HashMap<>(), env, transformation, new HashMap<>(), logLevel,
      getActingPrincipal( transMeta ) );

    setSteps( opsToSteps() );
    wireStatusToTransListeners();

    subscribeToOpLogging();
    subscribeToTransLogging();

    setReadyToStart( true );
  }

  private void logToChannel( LogChannelInterface logChannel, LogEntry data ) {
    LogLevel logLogLevel = data.getLogLogLevel();
    switch ( logLogLevel ) {
      case ERROR:
        if ( data.getThrowable() != null ) {
          logChannel.logError( data.getMessage(), data.getThrowable() );
        } else {
          logChannel.logError( data.getMessage() );
        }
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
        messageEventService.addHandler( Util.getOperationLogEvent( operation.getId() ),
          new MessageEventHandler() {
            @Override
            public void execute( Message message ) throws MessageEventHandlerExecutionException {
              PDIEvent<RemoteSource, LogEntry> event = (PDIEvent<RemoteSource, LogEntry>) message;
              LogEntry logEntry = event.getData();
              StepInterface stepInterface = findStepInterface( operation.getId(), 0 );
              if ( stepInterface != null ) {
                // This is intended to put a red error (dash) on the step in PDI.
                // In order to do that 3 things are needed: errors have to be set
                // to a positive number, the state is stopped state (not finished)
                // and Error log on the step (done just below this if statement)
                if ( LogLevel.ERROR.equals( logEntry.getLogLogLevel() ) ) {
                  stepInterface.setErrors( 1 );
                  stepInterface.setStopped( true );
                }

                LogChannelInterface logChannel = stepInterface.getLogChannel();
                logToChannel( logChannel, logEntry );
              } else {
                // Could not find step, log at transformation level instead
                logToChannel( getLogChannel(), logEntry );
              }
            }

            @Override
            public String getIdentifier() {
              return OPERATION_LOG + operation.getKey();
            }
          } );
      } catch ( HandlerRegistrationException e ) {
        getLogChannel().logError( "Error registering message handlers", e );
      }
    } );
  }

  private void subscribeToTransLogging() throws KettleException {
    messageEventService.addHandler( Util.getTransformationLogEvent(),
      new MessageEventHandler() {
        @Override
        public void execute( Message message ) throws MessageEventHandlerExecutionException {
          PDIEvent<RemoteSource, LogEntry> event = (PDIEvent<RemoteSource, LogEntry>) message;
          LogEntry data = event.getData();
          logToChannel( getLogChannel(), data );
        }

        @Override
        public String getIdentifier() {
          return TRANSFORMATION_LOG;
        }
      } );
  }

  private void wireStatusToTransListeners() throws KettleException {
    messageEventService.addHandler( Util.getTransformationStatusEvent(),
      new MessageEventHandler() {
        @Override
        public void execute( Message message ) throws MessageEventHandlerExecutionException {
          PDIEvent<RemoteSource, Status> transStatusEvent = (PDIEvent<RemoteSource, Status>) message;
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
                  getSteps().stream()
                    .map( c -> c.step )
                    .filter( s -> STATUS_RUNNING.equals( s.getStatus() ) || STATUS_HALTING.equals( s.getStatus() ) )
                    .forEach( si -> {
                      si.setStopped( true );
                      si.setRunning( false );
                    } );
                  break;
              }
            } catch ( KettleException e ) {
              throw new RuntimeException( e );
            }
          } );
        }

        @Override
        public String getIdentifier() {
          return TRANSFORMATION_STATUS;
        }
      } );

    messageEventService
      .addHandler( Util.getTransformationErrorEvent(), new MessageEventHandler() {
        @Override
        public void execute( Message message ) throws MessageEventHandlerExecutionException {
          String errorMessage = "Error Executing Transformation";
          LogEntry data = ( (PDIEvent<RemoteSource, LogEntry>) message ).getData();

          if ( !isNullOrEmpty( data.getMessage() ) ) {
            errorMessage = errorMessage + System.lineSeparator() + data.getMessage();
          }

          if ( data.getThrowable() != null ) {
            getLogChannel().logError( errorMessage, data.getThrowable() );
          } else {
            getLogChannel().logError( errorMessage );
          }
          errors.incrementAndGet();
          finishProcess( true );
        }

        @Override
        public String getIdentifier() {
          return TRANSFORMATION_ERROR;
        }
      } );

    messageEventService
      .addHandler( Util.getStopMessage(), new StopMessageEventHandler(
        getLogChannel(),
        errors,
        transFinishedSignal,
        this,
        cancelling
      ) );
  }

  private List<StepMetaDataCombi> opsToSteps() {
    return opsToSteps( transformation );
  }

  private List<StepMetaDataCombi> opsToSteps( Transformation transformation ) {
    Map<Operation, StepMetaDataCombi> operationToCombi = transformation.getOperations().stream()
      .collect( toMap( Function.identity(),
        op -> {
          StepMetaDataCombi combi = new StepMetaDataCombi();
          combi.stepMeta = StepMeta.fromXml( (String) op.getConfig().get( TransMetaConverter.STEP_META_CONF_KEY ) );
          try {
            combi.data = new StepDataInterfaceWebSocketEngineAdapter( op, messageEventService );
            List<StepMetaDataCombi> subSteps = getSubSteps( transformation, combi );
            combi.step = new StepInterfaceWebSocketEngineAdapter(
              op, messageEventService, combi.stepMeta, transMeta, combi.data, this, subSteps );
          } catch ( KettleException e ) {
            //TODO: treat the exception
            e.printStackTrace();
          }
          combi.meta = combi.stepMeta.getStepMetaInterface();
          combi.stepname = combi.stepMeta.getName();
          return combi;
        } ) );
    return new ArrayList<>( operationToCombi.values() );
  }

  @SuppressWarnings( "unchecked" )
  private List<StepMetaDataCombi> getSubSteps( Transformation transformation, StepMetaDataCombi combi ) {
    HashMap<String, Transformation> config =
      ( (Optional<HashMap<String, Transformation>>) transformation
        .getConfig( TransMetaConverter.SUB_TRANSFORMATIONS_KEY ) )
        .orElse( Maps.newHashMap() );
    StepMetaInterface smi = combi.stepMeta.getStepMetaInterface();
    return config.keySet().stream()
      .filter( key -> stepHasDependency( combi, smi, key ) )
      .flatMap( key -> opsToSteps( config.get( key ) ).stream() )
      .collect( Collectors.toList() );
  }

  private boolean stepHasDependency( StepMetaDataCombi combi, StepMetaInterface smi, String path ) {
    return smi.getResourceDependencies( transMeta, combi.stepMeta ).stream()
      .flatMap( resourceReference -> resourceReference.getEntries().stream() )
      .filter( entry -> ResourceEntry.ResourceType.ACTIONFILE.equals( entry.getResourcetype() ) )
      .anyMatch( entry -> entry.getResource().equals( path ) );
  }

  @Override public void startThreads() throws KettleException {
    getDaemonEndpoint().sendMessage( executionRequest );
    //let's start the session monitor
    startSessionMonitor();
  }

  private void startSessionMonitor() {
    getLogChannel().logDebug( "Starting Session Monitor." );
    sessionMonitor = Executors.newSingleThreadExecutor();
    sessionMonitor.submit( () -> {
      int failedTests = 0;
      while ( !isFinished() ) {
        try {
          if ( failedTests > MAX_TEST_FAILED ) {
            // Too many tests missed let's close
            errors.incrementAndGet();
            getLogChannel().logError(
              "Session Monitor detected that communication with the server was lost. Finalizing execution." );
            if ( !cancelling ) {
              finishProcess( false );
            }
            // Signal for the the waitUntilFinished blocker...
            transFinishedSignal.countDown();
          } else {
            TimeUnit.MILLISECONDS.sleep( SLEEP_TIME_MS );
            getDaemonEndpoint().sessionValid();
            if ( failedTests > 0 ) {
              getLogChannel()
                .logDebug( "Session Monitor - Server Communication restored." );
            }
            failedTests = 0;
          }
        } catch ( KettleException e ) {
          failedTests++;
          getLogChannel()
            .logDebug(
              "Session Monitor detected communication problem with the server. Retry (" + failedTests + "/"
                + MAX_TEST_FAILED + ")." );
        } catch ( InterruptedException e ) {
          getLogChannel().logDebug( "Session Monitor was interrupted." );
          Thread.currentThread().interrupt();
        }
      }
      closeSessionMonitor();
    } );
  }

  public void closeSessionMonitor() {
    if ( sessionMonitor != null && !sessionMonitor.isShutdown() ) {
      try {
        getLogChannel().logDebug( "Shutting down the Session Monitor." );
        sessionMonitor.shutdown();
      } finally {
        if ( !sessionMonitor.isTerminated() ) {
          sessionMonitor.shutdownNow();
        }
        getLogChannel().logDebug( "Session Monitor shutdown." );
      }
    }
  }

  @Override public void waitUntilFinished() {
    try {
      transFinishedSignal.await();
    } catch ( InterruptedException e ) {
      throw new RuntimeException( "Waiting for transformation to be finished interrupted!", e );
    }
  }

  @Override
  public int getErrors() {
    int nrErrors = errors.get();

    if ( getSteps() != null ) {
      for ( int i = 0; i < getSteps().size(); i++ ) {
        nrErrors += getSteps().get( i ).step.getErrors();
      }
    }
    return nrErrors;
  }

  @Override
  public Result getResult() {
    Result toRet = new Result();
    toRet.setNrErrors( getErrors() );
    return toRet;
  }

  public void finishProcess( boolean emitToAllSteps ) {
    setFinished( true );
    if ( emitToAllSteps ) {
      // emit error on all steps
      getSteps().stream().map( stepMetaDataCombi -> stepMetaDataCombi.step ).forEach( step -> {
        step.setStopped( true );
        step.setRunning( false );
      } );
    }
    getTransListeners().forEach( l -> {
      try {
        l.transFinished( TransWebSocketEngineAdapter.this );
      } catch ( KettleException e1 ) {
        getLogChannel().logError( "Error notifying trans listener", e1 );
      }
    } );
  }

  // ======================== May want to implement ================================= //


  @Override public RowProducer addRowProducer( String stepname, int copynr ) throws KettleException {
    throw new UnsupportedOperationException( "Not yet implemented" );
  }

  private Principal getActingPrincipal( TransMeta transMeta ) {
    if ( transMeta.getRepository() == null || transMeta.getRepository().getUserInfo() == null ) {
      return ActingPrincipal.ANONYMOUS;
    }
    return new ActingPrincipal( transMeta.getRepository().getUserInfo().getName() );
  }
}
