package org.pentaho.di.ui.core.runconfiguration.impl;

import org.pentaho.di.core.runconfiguration.api.RunConfiguration;
import org.pentaho.di.core.runconfiguration.impl.pentaho.DefaultRunConfiguration;
import org.pentaho.di.ui.core.runconfiguration.api.RunConfigurationUI;
import org.pentaho.di.ui.core.runconfiguration.api.RunConfigurationUIFactory;
import org.pentaho.di.ui.core.runconfiguration.impl.pentaho.DefaultRunConfigurationUIFactory;

import java.util.HashMap;

public class RunConfigurationUIFactoryManager {
  private static RunConfigurationUIFactoryManager instance;

  private HashMap<String, RunConfigurationUIFactory> factories;

  public static RunConfigurationUIFactoryManager getInstance() {
    if ( null == instance ) {
      instance = new RunConfigurationUIFactoryManager();
    }
    return instance;
  }

  public RunConfigurationUIFactoryManager() {
    factories = new HashMap<>();
    registerFactory( new DefaultRunConfigurationUIFactory() );
  }

  public void registerFactory( RunConfigurationUIFactory runConfigurationUIFactory ){
    factories.put( runConfigurationUIFactory.getType(), runConfigurationUIFactory );
  }

  public RunConfigurationUI generateUI( RunConfiguration runConfiguration ){
    return factories.get( runConfiguration.getClass().getName() ).generateUI( runConfiguration );
  }
}
