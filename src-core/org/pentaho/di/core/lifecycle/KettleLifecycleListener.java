package org.pentaho.di.core.lifecycle;

/**
 * A callback to be notified when the Kettle environment is initialized and shut
 * down.
 */
public interface KettleLifecycleListener {
  /**
   * Called during {@link KettleEnvironment} initialization.
   * 
   * @throws LifecycleException to indicate the listener did not complete 
   * successfully. Severe {@link LifecycleException}s will stop the 
   * initialization of the {@link KettleEnvironment}.
   */
  void onEnvironmentInit() throws LifecycleException;

  /**
   * Called when the VM that initialized {@link KettleEnvironment} terminates.
   */
  void onEnvironmentShutdown();
}
