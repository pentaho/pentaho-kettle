package org.pentaho.di.trans.nextgen;

import org.pentaho.di.engine.api.IExecutionContext;
import org.pentaho.di.engine.api.model.IOperation;
import org.pentaho.di.engine.api.reporting.IReportingEvent;
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


  private IOperation operation;
  private IExecutionContext executionContext;

  public StepInterfaceAdapter( IOperation op, IExecutionContext executionContext, StepMeta stepMeta,
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
        StepInterfaceAdapter.this.setLinesInput( data.getIn() );
        StepInterfaceAdapter.this.setLinesOutput( data.getOut() );
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
