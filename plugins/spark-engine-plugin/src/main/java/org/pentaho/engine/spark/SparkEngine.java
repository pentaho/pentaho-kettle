package org.pentaho.engine.spark;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.engine.ExecutionEngine;
import org.pentaho.di.engine.IExecutionEngine;
import org.pentaho.di.trans.Trans;

@ExecutionEngine( id = "SparkEngine", name= "Spark Engine" )
public class SparkEngine implements IExecutionEngine {

  @Override public void execute( Trans trans ) throws KettleException {
  }
}
