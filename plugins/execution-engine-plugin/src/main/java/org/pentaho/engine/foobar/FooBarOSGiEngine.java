package org.pentaho.engine.foobar;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.engine.ExecutionEngine;
import org.pentaho.di.engine.IExecutionEngine;
import org.pentaho.di.trans.Trans;

@ExecutionEngine( id = "FooBarOSGiEngine", name= "Foo Bar OSGi Engine" )
public class FooBarOSGiEngine implements IExecutionEngine {

  @Override public void execute( Trans trans ) throws KettleException {

  }
}
