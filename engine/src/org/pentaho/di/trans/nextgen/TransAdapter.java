package org.pentaho.di.trans.nextgen;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.engine.api.IEngine;
import org.pentaho.di.engine.api.IExecutionContext;
import org.pentaho.di.engine.api.IExecutionResult;
import org.pentaho.di.engine.api.model.IOperation;
import org.pentaho.di.engine.api.model.ITransformation;
import org.pentaho.di.engine.api.reporting.IReportingEvent;
import org.pentaho.di.engine.api.reporting.Status;
import org.pentaho.di.engine.kettleclassic.ClassicUtils;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.pentaho.di.engine.kettleclassic.ClassicUtils.STEP_META_CONF_KEY;

/**
 * Created by nbaker on 1/24/17.
 */
public class TransAdapter extends Trans {

  private final ITransformation transformation;
  private final IExecutionContext executionContext;
  private CompletableFuture<IExecutionResult>
    executionResultFuture;
  private Map<IOperation, StepInterfaceAdapter>
    operationToStep;
  private Map<IOperation, StepMetaDataCombi>
    operationToCombi;

  public TransAdapter( IEngine engine, TransMeta transMeta ) {
    transformation = ClassicUtils.convert( transMeta );
    executionContext = engine.prepare( transformation );
    this.transMeta = transMeta;
  }

  // ======================== Need to implement ================================= //


  @Override public void killAll() {
  }

  @Override public void prepareExecution( String[] arguments ) throws KettleException {
    // TODO: May need to provide RowSets


    operationToCombi = transformation.getOperations().stream().collect( Collectors.toMap( Function.identity(), op -> {
      StepMetaDataCombi combi = new StepMetaDataCombi();
      combi.stepMeta = (StepMeta) op.getConfig( STEP_META_CONF_KEY )
        .orElseThrow( () -> new IllegalStateException( "StepMeta not found in Operation" ) );
      combi.data = new StepDataInterfaceAdapter( op, executionContext );
      combi.step = new StepInterfaceAdapter( op, executionContext, combi.stepMeta, transMeta,
        combi.data, this );
      combi.meta = combi.stepMeta.getStepMetaInterface();
      combi.stepname = combi.stepMeta.getName();
      return combi;
    } ) );
    steps = new ArrayList<>( operationToCombi.values() );


    // Subscribe to status and notify trans listeners
    executionContext.subscribe( transformation, Status.class,
      new Subscriber<IReportingEvent<ITransformation, Status>>() {
        @Override public void onSubscribe( Subscription s ) {

        }

        @Override public void onNext( IReportingEvent<ITransformation, Status> iTransformationStatusIReportingEvent ) {
          getTransListeners().forEach( l -> {
            try {
              switch( iTransformationStatusIReportingEvent.getData() ) {
                case RUNNING:
                  l.transStarted( TransAdapter.this );
                  l.transActive( TransAdapter.this );
                  break;
                case PAUSED:
                  break;
                case STOPPED:
                  break;
                case FAILED:
                  break;
                case FINISHED:
                  l.transFinished( TransAdapter.this );
                  break;
              }
            } catch ( KettleException e ) {
              e.printStackTrace();
            }
          } );
        }

        @Override public void onError( Throwable t ) {
          t.printStackTrace();
        }

        @Override public void onComplete() {
        }
      } );
    readyToStart = true;
  }

  @Override public void startThreads() throws KettleException {
    executionResultFuture = executionContext.execute();
  }

  @Override public void waitUntilFinished() {
    try {
      IExecutionResult iExecutionResult = executionResultFuture.get();
    } catch ( InterruptedException e ) {
      throw new RuntimeException("Waiting for transformation to be finished interrupted!", e );
    } catch ( ExecutionException e ) {
      throw new RuntimeException( "Error executing Transformation or waiting for it to stop", e );
    }
  }

  // todo: update status

  // ======================== May want to implement ================================= //


  @Override public RowProducer addRowProducer( String stepname, int copynr ) throws KettleException {
    throw new KettleException( "Not yet Implemented" );
  }

}
