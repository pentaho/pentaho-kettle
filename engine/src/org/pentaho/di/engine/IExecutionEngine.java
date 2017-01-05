package org.pentaho.di.engine;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.Trans;

public interface IExecutionEngine {

  void execute( Trans trans ) throws KettleException;

}
