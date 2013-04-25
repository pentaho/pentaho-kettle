package org.pentaho.di.core.lifecycle;

/**
 * A callback to be notified when the Kettle environment is initialized and shut
 * down.
 */
public interface KettleLifecycleListener {
  /**
   * Called during KettleEnvironment initialization.
   * 
   * @throws LifecycleException to indicate the listener did not complete 
   * successfully. Severe LifecycleException will stop the 
   * initialization of the KettleEnvironment.
   */
  void onEnvironmentInit() throws LifecycleException;

  /**
   * Called when the VM that initialized KettleEnvironment terminates.
   */
  void onEnvironmentShutdown();
}
