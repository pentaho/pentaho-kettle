package org.pentaho.di.ui.spoon.trans.executionstate.api;

public interface ExecutionStateSubscriber {

  void execStateChanged( ExecutionStateEvent event );
}
