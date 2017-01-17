package org.pentaho.engine.spark.launcher.api;

import org.apache.spark.launcher.SparkAppHandle;

/**
 * ISparkLauncher
 * <p>
 * Interface to allow us to create launchers for different runtime environments.
 */
public interface ISparkLauncher {

  /**
   * Instantiates a specific Apache SparkLauncher and returns its app handle
   *
   * @return SparkAppHandle
   */
  SparkAppHandle launch();
}
