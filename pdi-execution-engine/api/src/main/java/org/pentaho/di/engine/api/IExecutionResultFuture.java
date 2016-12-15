package org.pentaho.di.engine.api;

import java.util.concurrent.Future;

/**
 * Created by nbaker on 5/31/16.
 */
public interface IExecutionResultFuture extends IProgressReporting<ITransformationEvent>, Future<IExecutionResult> {
  IExecutionContext getExecutionContext();

  ITransformation getTransformation();

}
