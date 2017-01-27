package org.pentaho.di.trans.nextgen;

import org.pentaho.di.engine.api.ExecutionContext;
import org.pentaho.di.engine.api.model.Operation;
import org.pentaho.di.engine.api.reporting.ReportingEvent;
import org.pentaho.di.engine.api.reporting.Status;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Created by nbaker on 1/24/17.
 */
public class StepDataInterfaceAdapter implements StepDataInterface {
  private final Operation op;
  private final ExecutionContext executionContext;

  public StepDataInterfaceAdapter( Operation op, ExecutionContext executionContext ) {
    this.op = op;
    this.executionContext = executionContext;

    executionContext.subscribe( op, Status.class, new Subscriber<ReportingEvent<Operation, Status>>() {
      @Override public void onSubscribe( Subscription s ) {

      }

      @Override public void onNext( ReportingEvent<Operation, Status> iOperationStatusIReportingEvent ) {

      }

      @Override public void onError( Throwable t ) {

      }

      @Override public void onComplete() {

      }
    } );
  }

  @Override public void setStatus( BaseStepData.StepExecutionStatus stepExecutionStatus ) {

  }

  @Override public BaseStepData.StepExecutionStatus getStatus() {
    return BaseStepData.StepExecutionStatus.STATUS_IDLE;
  }

  @Override public boolean isEmpty() {
    return false;
  }

  @Override public boolean isInitialising() {
    return false;
  }

  @Override public boolean isRunning() {
    return false;
  }

  @Override public boolean isIdle() {
    return false;
  }

  @Override public boolean isFinished() {
    return false;
  }

  @Override public boolean isDisposed() {
    return false;
  }
}
