package org.pentaho.engine.spark.core;

/**
 * Spark Engine Exception
 * <p>
 * Generic RuntimeException to be thrown within this project.
 */
public class SparkEngineException extends RuntimeException {

  public SparkEngineException( String message ) {
    super( message );
  }

  public SparkEngineException( String message, Throwable throwable ) {
    super( message, throwable );
  }
}
