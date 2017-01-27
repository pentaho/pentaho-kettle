package org.pentaho.di.trans.nextgen;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.engine.api.Engine;
import org.pentaho.di.engine.api.ExecutionContext;
import org.pentaho.di.engine.api.ExecutionResult;
import org.pentaho.di.engine.api.model.Operation;
import org.pentaho.di.engine.api.model.Transformation;
import org.pentaho.di.engine.api.reporting.ReportingEvent;
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

  private final Transformation transformation;
  private final ExecutionContext executionContext;
  private CompletableFuture<ExecutionResult>
    executionResultFuture;
  private Map<Operation, StepInterfaceAdapter>
    operationToStep;
  private Map<Operation, StepMetaDataCombi>
    operationToCombi;

  public TransAdapter( Engine engine, TransMeta transMeta ) {
    transformation = ClassicUtils.convert( transMeta );
    executionContext = engine.prepare( transformation );
    this.transMeta = transMeta;
  }

  // ======================== Need to implement ================================= //


  @Override public void killAll() {
    System.out.println("killAll");
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
      new Subscriber<ReportingEvent<Transformation, Status>>() {
        @Override public void onSubscribe( Subscription s ) {
          s.request( Long.MAX_VALUE );
        }

        @Override public void onNext( ReportingEvent<Transformation, Status> iTransformationStatusIReportingEvent ) {
          addStepPerformanceSnapShot();
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
                  setFinished( true );
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
      ExecutionResult iExecutionResult = executionResultFuture.get();
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
