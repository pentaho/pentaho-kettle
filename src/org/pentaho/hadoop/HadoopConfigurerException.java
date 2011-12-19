package org.pentaho.hadoop;

/**
 * Represents an error when interacting with a {@link HadoopConfigurer}.
 */
public class HadoopConfigurerException extends Exception {
  public HadoopConfigurerException(String s) {
    super(s);
  }

  public HadoopConfigurerException(String s, Throwable throwable) {
    super(s, throwable);
  }
}
