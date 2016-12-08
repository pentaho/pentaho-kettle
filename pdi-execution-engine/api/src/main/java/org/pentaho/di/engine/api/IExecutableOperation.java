package org.pentaho.di.engine.api;

/**
 * ICallableOperation is the "materialized" version of
 * an IOperation.  It represents the mapping of the
 * structural specification of the op to it's concrete
 * executable form, as applicable to the IEngine in which
 * it is running.
 *
 * An ICallableOperation encapsulates a function capable of transforming input
 * tuples received "From" parent operations and published
 * "To" child ops.
 */
import rx.Observable;
import rx.Observer;
import rx.Subscriber;

public interface IExecutableOperation extends IOperation, Observer<ITuple> {

  void start();

  /**
   * Applies this operation to an incoming tuple
   */
  void next( ITuple tuple );

  void subscribe( Subscriber<? super ITuple> subscriber );

  void done();

  boolean isRunning();

  void onCompleted();

  void onError( Throwable throwable );

  void onNext( ITuple tuple );
}
