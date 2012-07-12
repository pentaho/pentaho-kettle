package org.pentaho.di.core.hadoop;

import org.pentaho.di.core.annotations.KettleLifecyclePlugin;
import org.pentaho.di.core.lifecycle.KettleLifecycleListener;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.hadoop.shim.ConfigurationException;

/**
 * This class serves to initialize the Hadoop Configuration registry
 */
@KettleLifecyclePlugin(id = "HadoopConfigurationBootstrap", name = "Hadoop Configuration Bootstrap")
public class HadoopConfigurationBootstrap implements KettleLifecycleListener {
  public static final String PLUGIN_ID = "HadoopConfigurationBootstrap";

  @Override
  public void onEnvironmentInit() throws LifecycleException {
    // Initialize the Registry
    // TODO Don't rely on getInstance() to do this work now that we have this init method!
    try {
      HadoopConfigurationRegistry.getInstance();
    } catch (ConfigurationException ex) {
      throw new LifecycleException(ex, true);
    }
  }

  @Override
  public void onEnvironmentShutdown() {
  }

}
