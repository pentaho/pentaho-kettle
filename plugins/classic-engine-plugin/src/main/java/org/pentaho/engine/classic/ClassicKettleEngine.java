package org.pentaho.engine.classic;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.engine.ExecutionEngine;
import org.pentaho.di.engine.IExecutionEngine;
import org.pentaho.di.trans.Trans;

@ExecutionEngine( id = "ClassicKettleEngine", name= "Classic Kettle Engine" )
public class ClassicKettleEngine implements IExecutionEngine {

  @Override public void execute( Trans trans ) throws KettleException {
  }
}
