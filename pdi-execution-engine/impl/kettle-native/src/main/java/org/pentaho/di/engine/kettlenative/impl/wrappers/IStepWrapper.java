package org.pentaho.di.engine.kettlenative.impl.wrappers;

import org.pentaho.di.engine.api.ITuple;

public interface IStepWrapper {


  public void start();

  public void next( ITuple tuple );

  public void finished();

}
