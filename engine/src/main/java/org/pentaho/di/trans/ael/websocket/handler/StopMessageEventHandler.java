/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2019-2020 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.trans.ael.websocket.handler;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.engine.api.remote.Message;
import org.pentaho.di.engine.api.remote.StopMessage;
import org.pentaho.di.trans.ael.websocket.TransWebSocketEngineAdapter;
import org.pentaho.di.trans.ael.websocket.exception.MessageEventHandlerExecutionException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class StopMessageEventHandler implements MessageEventHandler {
  private static final String TRANSFORMATION_STOP = "TRANSFORMATION_STOP_TRANS_WEBSOCK";
  private transient LogChannelInterface log;
  private transient CountDownLatch transFinishedSignal;
  private transient TransWebSocketEngineAdapter transAdapter;

  private AtomicInteger errors;
  private boolean cancelling;

  public StopMessageEventHandler( LogChannelInterface log,
                                  AtomicInteger errors,
                                  CountDownLatch transFinishedSignal,
                                  TransWebSocketEngineAdapter transAdapter,
                                  boolean cancelling ) {
    this.log = log;
    this.errors = errors;
    this.transFinishedSignal = transFinishedSignal;
    this.transAdapter = transAdapter;
    this.cancelling = cancelling;
  }

  @Override
  public void execute( Message message ) throws MessageEventHandlerExecutionException {
    StopMessage stopMessage = (StopMessage) message;

    if ( stopMessage.operationFailed() ) {
      log.logError( "Could not verify termination status: " + stopMessage.getReasonPhrase() );
      log.logError( "Please check with cluster administrator." );
    } else if ( stopMessage.sessionWasKilled() ) {
      log.logError( "Finalizing execution: " + stopMessage.getReasonPhrase() );
      errors.incrementAndGet();
    } else {
      log.logBasic( "Finalizing execution: " + stopMessage.getReasonPhrase() );
    }

    if ( !cancelling ) {
      transAdapter.finishProcess( false );
    }
    try {
      transAdapter.getDaemonEndpoint().close( stopMessage.getReasonPhrase() );
    } catch ( KettleException e ) {
      log.logError( "Error finalizing", e );
    }

    //let's shutdown the session monitor thread
    transAdapter.closeSessionMonitor();
    // Signal for the the waitUntilFinished blocker...
    transFinishedSignal.countDown();
  }

  @Override
  public String getIdentifier() {
    return TRANSFORMATION_STOP;
  }
}
