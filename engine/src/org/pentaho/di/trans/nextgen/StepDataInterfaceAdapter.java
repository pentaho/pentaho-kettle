package org.pentaho.di.trans.nextgen;

import org.pentaho.di.engine.api.IExecutionContext;
import org.pentaho.di.engine.api.model.IOperation;
import org.pentaho.di.engine.api.reporting.IReportingEvent;
import org.pentaho.di.engine.api.reporting.Status;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Created by nbaker on 1/24/17.
 */
public class StepDataInterfaceAdapter implements StepDataInterface {
  private final IOperation op;
  private final IExecutionContext executionContext;

  public StepDataInterfaceAdapter( IOperation op, IExecutionContext executionContext ) {
    this.op = op;
    this.executionContext = executionContext;

    executionContext.subscribe( op, Status.class, new Subscriber<IReportingEvent<IOperation, Status>>() {
      @Override public void onSubscribe( Subscription s ) {

      }

      @Override public void onNext( IReportingEvent<IOperation, Status> iOperationStatusIReportingEvent ) {

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
