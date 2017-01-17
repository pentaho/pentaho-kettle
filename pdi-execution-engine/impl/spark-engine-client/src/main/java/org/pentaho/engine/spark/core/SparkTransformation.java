package org.pentaho.engine.spark.core;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.engine.api.IHop;
import org.pentaho.di.engine.api.IOperation;
import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.engine.spark.core.SparkEngineException;

import java.util.List;

/**
 * SparkTransformation
 *
 * This implementation of the ITransformation serves as a simple transport mechanism to pass the TransMeta XML contents
 * to the Spark Driver.  All other classes are not used from a client perspective.
 */
public class SparkTransformation implements ITransformation {

  private static String ERROR_MSG = "Method '%s' is unsupported for Spark Transforamtions.";
  private String config;

  public SparkTransformation( TransMeta transMeta ) {
    try {
      this.config = transMeta.getXML();
    } catch ( KettleException e ) {
      throw new SparkEngineException( "Error converting TransMeta to XML.", e );
    }
  }

  @Override
  public List<IOperation> getOperations() {
    throw new UnsupportedOperationException( String.format(ERROR_MSG, "getOperations()") );
  }

  @Override
  public List<IOperation> getSourceOperations() {
    throw new UnsupportedOperationException( String.format(ERROR_MSG, "getSourceOperations()") );
  }

  @Override
  public List<IOperation> getSinkOperations() {
    throw new UnsupportedOperationException( String.format(ERROR_MSG, "getSinkOperations()") );
  }

  @Override
  public List<IHop> getHops() {
    throw new UnsupportedOperationException( String.format(ERROR_MSG, "getHops()") );
  }

  @Override
  public String getConfig() {
    return config;
  }

  @Override
  public String getId() {
    throw new UnsupportedOperationException( String.format(ERROR_MSG, "getId()") );
  }
}
