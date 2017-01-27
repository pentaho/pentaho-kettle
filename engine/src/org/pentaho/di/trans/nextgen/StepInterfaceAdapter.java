package org.pentaho.di.trans.nextgen;

import org.pentaho.di.engine.api.ExecutionContext;
import org.pentaho.di.engine.api.model.Operation;
import org.pentaho.di.engine.api.reporting.ReportingEvent;
import org.pentaho.di.engine.api.reporting.Metrics;
import org.pentaho.di.engine.api.reporting.Status;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Created by nbaker on 1/24/17.
 */
public class StepInterfaceAdapter extends BaseStep {


  private Operation operation;
  private ExecutionContext executionContext;

  public StepInterfaceAdapter( Operation op, ExecutionContext executionContext, StepMeta stepMeta,
                               TransMeta transMeta, StepDataInterface dataInterface, Trans trans ) {
    super( stepMeta, dataInterface, 0, transMeta, trans );
    operation = op;
    this.executionContext = executionContext;
    init();
  }

  @Override public void dispatch() {
    // No thanks. I'll take it from here.
  }

  private void init() {

    executionContext.subscribe( operation, Metrics.class, data -> {
        StepInterfaceAdapter.this.setLinesRead( data.getIn() );
        StepInterfaceAdapter.this.setLinesWritten( data.getOut() );
    } );



    executionContext.subscribe( operation, Status.class, data -> {
        switch( data ) {

          case RUNNING:
            StepInterfaceAdapter.this.setRunning( true );
            break;
          case PAUSED:
            StepInterfaceAdapter.this.setPaused( true );
            break;
          case STOPPED:
            StepInterfaceAdapter.this.setStopped( true );
            break;
          case FAILED:
          case FINISHED:
            StepInterfaceAdapter.this.setRunning( false);
            break;
        }

    } );
  }
}
