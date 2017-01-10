package org.pentaho.di.ui.spoon.trans.executionstate.api;

public class ExecutionStateEvent {

  private final ExecutionState state;

  public ExecutionStateEvent( ExecutionState state ) {
    this.state = state;
  }

  public ExecutionState getState() {
    return state;
  }
}
