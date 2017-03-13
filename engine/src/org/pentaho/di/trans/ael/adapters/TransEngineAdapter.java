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

package org.pentaho.di.trans.ael.adapters;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.engine.api.Engine;
import org.pentaho.di.engine.api.ExecutionContext;
import org.pentaho.di.engine.api.ExecutionResult;
import org.pentaho.di.engine.api.events.PDIEvent;
import org.pentaho.di.engine.api.model.Operation;
import org.pentaho.di.engine.api.model.Transformation;
import org.pentaho.di.engine.api.reporting.Status;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by nbaker on 1/24/17.
 */
public class TransEngineAdapter extends Trans {

  private final Transformation transformation;
  private final ExecutionContext executionContext;
  private CompletableFuture<ExecutionResult>
    executionResultFuture;

  public TransEngineAdapter( Engine engine, TransMeta transMeta ) {
    transformation = TransMetaConverter.convert( transMeta );
    executionContext = engine.prepare( transformation );
    this.transMeta = transMeta;
  }

  @Override public void killAll() {
    throw new UnsupportedOperationException( "Not yet implemented" );
  }

  @Override public void prepareExecution( String[] arguments ) throws KettleException {
    setSteps( new ArrayList<>( opsToSteps() ) );
    wireStatusToTransListeners();
    setReadyToStart( true );
  }

  private void wireStatusToTransListeners() {
    executionContext.subscribe( transformation, Status.class,
      new Subscriber<PDIEvent<Transformation, Status>>() {
        @Override public void onSubscribe( Subscription s ) {
          s.request( Long.MAX_VALUE );
        }

        @Override public void onNext( PDIEvent<Transformation, Status> transStatusEvent ) {
          addStepPerformanceSnapShot();
          getTransListeners().forEach( l -> {
            try {
              switch ( transStatusEvent.getData() ) {
                case RUNNING:
                  l.transStarted( TransEngineAdapter.this );
                  l.transActive( TransEngineAdapter.this );
                  break;
                case PAUSED:
                  break;
                case STOPPED:
                  break;
                case FAILED:
                case FINISHED:
                  l.transFinished( TransEngineAdapter.this );
                  setFinished( true );
                  break;
              }
            } catch ( KettleException e ) {
              throw new RuntimeException( e );
            }
          } );
        }

        @Override public void onError( Throwable t ) {

          setFinished( true );
          t.printStackTrace();
          getTransListeners().forEach( l -> {
            try {
              l.transFinished( TransEngineAdapter.this );
            } catch ( KettleException e ) {
              e.printStackTrace();
            }
          });
        }

        @Override public void onComplete() {

          setFinished( true );
          getTransListeners().forEach( l -> {
            try {
              l.transFinished( TransEngineAdapter.this );
            } catch ( KettleException e ) {
              e.printStackTrace();
            }
          });
        }
      } );
  }

  private Collection<StepMetaDataCombi> opsToSteps() {
    Map<Operation, StepMetaDataCombi> operationToCombi = transformation.getOperations().stream()
      .collect( Collectors.toMap( Function.identity(),
        op -> {
          StepMetaDataCombi combi = new StepMetaDataCombi();
          combi.stepMeta = StepMeta.fromXml( (String) op.getConfig().get( TransMetaConverter.STEP_META_CONF_KEY ) );
          combi.data = new StepDataInterfaceEngineAdapter( op, executionContext );
          combi.step = new StepInterfaceEngineAdapter( op, executionContext, combi.stepMeta, transMeta,
            combi.data, this );
          combi.meta = combi.stepMeta.getStepMetaInterface();
          combi.stepname = combi.stepMeta.getName();
          return combi;
        } ) );
    return operationToCombi.values();
  }

  @Override public void startThreads() throws KettleException {
    executionResultFuture = executionContext.execute();
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

}
