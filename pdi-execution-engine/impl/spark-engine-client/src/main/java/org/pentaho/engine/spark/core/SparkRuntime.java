package org.pentaho.engine.spark.core;

/**
 * Spark Runtime
 * <p>
 * Enumeration that defines the different master/deploy methods of running spark.  This is used in creating instances
 * of the ISparkLauncher.
 */
public enum SparkRuntime {
  LOCAL,
  //  STANDALONE_CLIENT,
  //  STANDALONE_CLUSTER,
  //  MESOS_CLIENT,
  //  MESOS_CLUSTER,
  //  YARN_CLIENT,
  YARN_CLUSTER;
}
