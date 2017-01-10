package org.pentaho.di.ui.spoon.trans.executionstate.api;

public interface ExecutionStatePublisher {

  void subscribe( ExecutionStateSubscriber subscriber );

}
