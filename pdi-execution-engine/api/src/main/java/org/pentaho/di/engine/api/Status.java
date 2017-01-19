package org.pentaho.di.engine.api;

import java.io.Serializable;

/**
 * Created by hudak on 1/6/17.
 */
public enum Status implements Serializable {
  RUNNING( false ),
  PAUSED( false ),
  STOPPED( true ),
  FAILED( true ),
  FINISHED( true );

  final boolean finalState;

  Status( Boolean finalState ) {
    this.finalState = finalState;
  }

  public boolean isFinal() {
    return finalState;
  }
}
